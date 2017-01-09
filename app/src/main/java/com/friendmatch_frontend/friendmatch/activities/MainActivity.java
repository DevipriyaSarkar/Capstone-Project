package com.friendmatch_frontend.friendmatch.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
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
import com.friendmatch_frontend.friendmatch.utilities.PersistentCookieStore;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;

import static com.friendmatch_frontend.friendmatch.application.AppController.SERVER_URL;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private final String TAG = this.getClass().getSimpleName();
    private TabLayout tabLayout;
    private String[] pageTitle;
    private TypedArray pageIcon;
    private NavigationView navigationView;
    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
        viewPager.setAdapter(adapter);
    }

    private void setupTabIcons() {
        tabLayout.getTabAt(0).setIcon(pageIcon.getDrawable(0));
        //noinspection ResourceType
        tabLayout.getTabAt(1).setIcon(pageIcon.getDrawable(1));
    }

    private void logOut() {

        // initialize progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setMessage(getString(R.string.logout_progress_dialog_message));
        pDialog.setCancelable(false);

        showProgressDialog();

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

}
