package com.friendmatch_frontend.friendmatch.activities;

import android.app.IntentService;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.friendmatch_frontend.friendmatch.R;
import com.friendmatch_frontend.friendmatch.adapters.ViewPagerAdapter;
import com.friendmatch_frontend.friendmatch.application.AppController;
import com.friendmatch_frontend.friendmatch.fragments.EventSuggestionFragment;
import com.friendmatch_frontend.friendmatch.fragments.FriendSuggestionFragment;
import com.friendmatch_frontend.friendmatch.fragments.TodayEventFragment;
import com.friendmatch_frontend.friendmatch.services.EventsTodayIntentService;
import com.friendmatch_frontend.friendmatch.services.EventsTodayTaskService;
import com.friendmatch_frontend.friendmatch.utilities.PersistentCookieStore;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.PeriodicTask;
import com.google.android.gms.gcm.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import static com.friendmatch_frontend.friendmatch.application.AppController.SERVER_URL;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private final String TAG = this.getClass().getSimpleName();
    private TabLayout tabLayout;
    private String[] pageTitle;
    private TypedArray pageIcon;
    private NavigationView navigationView;
    private ProgressDialog pDialog;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // The intent service is for executing immediate pulls
        // GCMTaskService can only schedule tasks, they cannot execute immediately
        SharedPreferences sp = getSharedPreferences("FIRST_LAUNCH", MODE_PRIVATE);
        boolean firstInit = sp.getBoolean("first_init", true);
        Intent intentService = new Intent(this, EventsTodayIntentService.class);
        if (firstInit){
            // Run the initialize task service so that some stocks appear upon an empty database
            intentService.putExtra("TAG", "INIT");
            if (isInternetAvailable()){
                startService(intentService);
                SharedPreferences.Editor editor = sp.edit();
                editor.putBoolean("first_init", false);
                editor.apply();
            } else {
                Toast.makeText(getApplicationContext(), R.string.no_internet_error, Toast.LENGTH_LONG).show();
            }
        }

        pageTitle = getResources().getStringArray(R.array.page_title);
        pageIcon = getResources().obtainTypedArray(R.array.page_icon);

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        setupTabIcons();

        pageIcon.recycle();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        updateNavUserInfo();

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };

        MobileAds.initialize(getApplicationContext(), getString(R.string.banner_ad_unit_id));
        AdView adView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        if (isInternetAvailable()){
            long period = 86400L;
            long flex = 10L;
            String periodicTag = "Periodic";

            // create a periodic task to pull current day's event
            PeriodicTask periodicTask = new PeriodicTask.Builder()
                    .setService(EventsTodayTaskService.class)
                    .setPeriod(period)
                    .setFlex(flex)
                    .setTag(periodicTag)
                    .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
                    .build();

            GcmNetworkManager.getInstance(this).schedule(periodicTask);
        }

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_profile) {
            // show current user profile
            Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_hobby) {
            // show hobby activity
            Intent intent = new Intent(getApplicationContext(), HobbyActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_event) {
            // show event activity
            Intent intent = new Intent(getApplicationContext(), EventActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_log_out) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle(R.string.log_out_dialog_title);
            alertDialogBuilder.setMessage(R.string.log_out_dialog_message);
            alertDialogBuilder.setPositiveButton(R.string.dialog_positive_button,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            logOut();
                        }
                    });
            alertDialogBuilder.setNegativeButton(R.string.dialog_negative_button,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // do nothing
                        }
                    });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();

        } else if (id == R.id.nav_share) {

            // share the app with others
            Intent share = new Intent(android.content.Intent.ACTION_SEND);
            share.setType("text/plain");
            share.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_action_text));
            startActivity(Intent.createChooser(share, getString(R.string.share_action_intent_chooser)));

        } else if (id == R.id.nav_about) {

            // credits :P
            final SpannableString spannableString = new SpannableString(getString(R.string.about_message));
            Linkify.addLinks(spannableString, Linkify.EMAIL_ADDRESSES);

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle(R.string.about_title);
            alertDialogBuilder.setMessage(spannableString);
            alertDialogBuilder.setNeutralButton(R.string.about_neutral_button,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            // do nothing
                        }
                    });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
            // make emails clickable
            TextView dialogMessage = (TextView) alertDialog.findViewById(android.R.id.message);
            if (dialogMessage != null) {
                dialogMessage.setMovementMethod(LinkMovementMethod.getInstance());
                dialogMessage.setLineSpacing(0.0f, 1.3f);
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void updateNavUserInfo() {

        final SharedPreferences spNav = getSharedPreferences("USER_LOGIN", Context.MODE_PRIVATE);
        if (spNav.getString("name", null) != null) {
            String userName = spNav.getString("name", null);
            String userEmail = spNav.getString("email", null);
            String userGender = spNav.getString("gender", null);

            updateNavUI(userName, userEmail, userGender);
        } else {

            String urlString = SERVER_URL + "/user/info";

            // handle cookies
            CookieManager cookieManager = new CookieManager(new PersistentCookieStore(getApplicationContext()),
                    CookiePolicy.ACCEPT_ALL);
            CookieHandler.setDefault(cookieManager);

            JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                    urlString, null,
                    new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d(TAG, response.toString());
                            try {
                                int code = response.getInt("code");
                                Log.d(TAG, "Code: " + code);
                                if (code == 200) {
                                    JSONObject info = (response.getJSONObject("message")).getJSONObject("info");
                                    Log.d(TAG, "Info: " + info.toString());

                                    String userName = info.getString("user_name");
                                    String userEmail = info.getString("user_email");
                                    String userGender = info.getString("gender");

                                    SharedPreferences.Editor editor = spNav.edit();
                                    editor.putString("name", userName);
                                    editor.putString("gender", userGender);
                                    editor.apply();

                                    updateNavUI(userName, userEmail, userGender);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Log.d(TAG, "JSON Error: " + e.getMessage());
                            }
                        }
                    }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    VolleyLog.d(TAG, "Error in " + TAG + " : " + error.getMessage());
                }
            });

            // Adding request to request queue
            AppController.getInstance().addToRequestQueue(jsonObjReq);
        }
    }

    private void updateNavUI(String userName, String userEmail, String userGender) {
        View headerView = navigationView.getHeaderView(0);

        TextView navUserName = (TextView) headerView.findViewById(R.id.nav_user_name);
        TextView navUserEmail = (TextView) headerView.findViewById(R.id.nav_user_email);
        ImageView navUserImage = (ImageView) headerView.findViewById(R.id.nav_user_image);

        navUserName.setText(userName);
        navUserEmail.setText(userEmail);

        if (userGender.equals("M")) {
            navUserImage.setImageResource(R.drawable.male);
        } else {
            navUserImage.setImageResource(R.drawable.female);
        }
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new FriendSuggestionFragment(), pageTitle[0], pageIcon.getResourceId(0, 0));
        //noinspection ResourceType
        adapter.addFragment(new EventSuggestionFragment(), pageTitle[1], pageIcon.getResourceId(1, 0));
        adapter.addFragment(new TodayEventFragment(), pageTitle[2], pageIcon.getResourceId(2, 0));
        viewPager.setAdapter(adapter);
    }

    private void setupTabIcons() {
        tabLayout.getTabAt(0).setIcon(pageIcon.getDrawable(0));
        //noinspection ResourceType
        tabLayout.getTabAt(1).setIcon(pageIcon.getDrawable(1));
        tabLayout.getTabAt(2).setIcon(pageIcon.getDrawable(2));
    }

    private void logOut() {

        // initialize progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setMessage(getString(R.string.logout_progress_dialog_message));
        pDialog.setCancelable(false);

        showProgressDialog();

        mAuth.signOut();

        String urlString = SERVER_URL + "/logout";

        // handle cookies
        CookieManager cookieManager = new CookieManager(new PersistentCookieStore(getApplicationContext()),
                CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(cookieManager);

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                urlString, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, response.toString());
                        try {
                            int code = response.getInt("code");
                            Log.d(TAG, "Code: " + code);

                            if (code == 200) {
                                SharedPreferences sp1 = getSharedPreferences("USER_LOGIN", MODE_PRIVATE);
                                SharedPreferences sp2 = getSharedPreferences("FIRST_LAUNCH", MODE_PRIVATE);
                                SharedPreferences.Editor editor1 = sp1.edit();
                                SharedPreferences.Editor editor2 = sp2.edit();
                                editor1.clear();
                                editor1.commit();
                                editor2.clear();
                                editor2.commit();

                                finish();
                                Intent intent = new Intent(getApplicationContext(), SplashActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            } else {
                                hideProgressDialog();
                                Toast.makeText(getApplicationContext(), R.string.network_error, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.d(TAG, "JSON Error: " + e.getMessage());
                            hideProgressDialog();
                            Toast.makeText(getApplicationContext(), R.string.network_error, Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error in " + TAG + " : " + error.getMessage());
                hideProgressDialog();
                Toast.makeText(getApplicationContext(), R.string.network_error, Toast.LENGTH_SHORT).show();
            }
        });

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(jsonObjReq);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    private void showProgressDialog() {
        if (!pDialog.isShowing()) {
            pDialog.show();
        }
    }

    private void hideProgressDialog() {
        if (pDialog.isShowing()) {
            pDialog.dismiss();
        }
    }

    //check for internet connectivity
    private boolean isInternetAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null &&
                cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }

}