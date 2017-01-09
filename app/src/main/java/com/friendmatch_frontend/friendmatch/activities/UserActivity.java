package com.friendmatch_frontend.friendmatch.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.friendmatch_frontend.friendmatch.R;
import com.friendmatch_frontend.friendmatch.adapters.EventListAdapter;
import com.friendmatch_frontend.friendmatch.adapters.FriendGridAdapter;
import com.friendmatch_frontend.friendmatch.adapters.HobbyGridAdapter;
import com.friendmatch_frontend.friendmatch.application.AppController;
import com.friendmatch_frontend.friendmatch.models.Event;
import com.friendmatch_frontend.friendmatch.models.Hobby;
import com.friendmatch_frontend.friendmatch.models.User;
import com.friendmatch_frontend.friendmatch.utilities.ExpandableHeightGridView;
import com.friendmatch_frontend.friendmatch.utilities.PersistentCookieStore;
import com.friendmatch_frontend.friendmatch.utilities.RecyclerViewClickListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.ArrayList;

import static com.friendmatch_frontend.friendmatch.application.AppController.SERVER_URL;

public class UserActivity extends AppCompatActivity {

    private final String TAG = this.getClass().getSimpleName();
    ProgressDialog pDialog;
    NestedScrollView contentUser;
    CollapsingToolbarLayout toolbarLayout;
    FloatingActionButton friendOperationFAB;
    View commonHobbyView, relatedHobbyView, allHobbyView, allEventView, allFriendsView;
    TextView commonHobbyHeading, relatedHobbyHeading, allHobbyHeading, allFriendsHeading;
    static final int SOCKET_TIMEOUT_MS = 5000;
    int userID; // the user of the app
    int friendID;   // user ID of the clicked friend/user
    int isFriend; // whether to show related hobby section - show only if not friends
    String friendName, friendGender;
    int eventImageID = R.drawable.event;
    int hobbyImageID = R.drawable.hobby;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SharedPreferences sp = getSharedPreferences("USER_LOGIN", MODE_PRIVATE);
        userID = sp.getInt("user_id", -1);

        // getting initial user data from calling activity
        Bundle bundle = getIntent().getExtras();
        friendID = bundle.getInt("FRIEND_ID");
        friendName = bundle.getString("FRIEND_NAME");
        friendGender = bundle.getString("FRIEND_GENDER");
        isFriend = bundle.getInt("IS_FRIEND");

        contentUser = (NestedScrollView) findViewById(R.id.contentUser);

        // setting up the section headings
        commonHobbyView = findViewById(R.id.userCommonHobbies);
        allHobbyView = findViewById(R.id.userAllHobbies);
        relatedHobbyView = findViewById(R.id.userRelatedHobbies);
        allEventView = findViewById(R.id.userAllEvents);
        allFriendsView = findViewById(R.id.userAllFriends);
        commonHobbyHeading = (TextView) commonHobbyView.findViewById(R.id.hobbySectionHeading);
        relatedHobbyHeading = (TextView) relatedHobbyView.findViewById(R.id.hobbySectionHeading);
        allHobbyHeading = (TextView) allHobbyView.findViewById(R.id.hobbySectionHeading);
        allFriendsHeading = (TextView) allFriendsView.findViewById(R.id.friendsSectionHeading);

        commonHobbyHeading.setText(R.string.mutual_hobby_section_heading);
        allHobbyHeading.setText(R.string.all_hobby_section_heading);
        relatedHobbyHeading.setText(R.string.related_hobby_section_heading);
        allFriendsHeading.setText(R.string.all_friends_section_heading);

        if (isFriend == 1)
            relatedHobbyView.setVisibility(View.GONE);

        if (friendID == userID) {
            commonHobbyView.setVisibility(View.GONE);
            relatedHobbyView.setVisibility(View.GONE);
        }

        // setting up the collapsing toolbar
        toolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        toolbarLayout.setTitle(friendName);
        ImageView userProfileImage = (ImageView) findViewById(R.id.userProfileImage);
        if (friendGender.equals("Male")) {
            userProfileImage.setImageResource(R.drawable.male);
        } else {
            userProfileImage.setImageResource(R.drawable.female);
        }
        Bitmap bitmap = ((BitmapDrawable) userProfileImage.getDrawable()).getBitmap();
        if (bitmap != null) {
            Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                @Override
                public void onGenerated(Palette palette) {
                    toolbarLayout.setContentScrimColor(palette.getMutedColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary)));
                    toolbarLayout.setStatusBarScrimColor(palette.getMutedColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryDark)));
                    toolbarLayout.setExpandedTitleColor(palette.getDarkMutedColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryText)));
                }
            });
        }

        // initialize progress dialog
        pDialog = new ProgressDialog(this);
        String firstName = friendName.substring(0, friendName.indexOf(" "));
        pDialog.setMessage(getString(R.string.user_profile_prog_dialog_msg1) + " " + firstName +
                getString(R.string.user_profile_prog_dialog_msg2));
        pDialog.setCancelable(false);

        getUserProfile();

        friendOperationFAB = (FloatingActionButton) findViewById(R.id.friendOperationFAB);
        if (isFriend == 1) {
            friendOperationFAB.setImageResource(R.drawable.account_minus);
        } else if (friendID == userID) {
            friendOperationFAB.setVisibility(View.GONE);
        } else {
            friendOperationFAB.setImageResource(R.drawable.account_plus);
        }
        friendOperationFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isFriend == 1) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(view.getContext());
                    alertDialogBuilder.setTitle(R.string.remove_friend_dialog_title);
                    alertDialogBuilder.setMessage(R.string.remove_friend_dialog_message);
                    alertDialogBuilder.setPositiveButton(R.string.dialog_positive_button,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
                                    removeFriend();
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
                } else {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(view.getContext());
                    alertDialogBuilder.setTitle(R.string.add_friend_dialog_title);
                    alertDialogBuilder.setMessage(R.string.add_friend_dialog_message);
                    alertDialogBuilder.setPositiveButton(R.string.dialog_positive_button,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
                                    addFriend();
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
                }
            }
        });
    }

    public void getUserProfile() {

        showProgressDialog();

        String urlString = SERVER_URL + "/user/" + friendID + "/profile";

        // handle cookies
        CookieManager cookieManager = new CookieManager(new PersistentCookieStore(getApplicationContext()),
                CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(cookieManager);

        JsonObjectRequest profObjRequest = new JsonObjectRequest(Request.Method.GET,
                urlString, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, response.toString());
                        try {
                            int code = response.getInt("code");
                            Log.d(TAG, "Code (message): " + code);
                            if (code == 200) {
                                JSONArray response_array = response.getJSONArray("message");
                                updateUI(response_array);
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

        //Set a retry policy in case of SocketTimeout & ConnectionTimeout Exceptions.
        //Volley does retry for you if you have specified the policy.
        profObjRequest.setRetryPolicy(new DefaultRetryPolicy(SOCKET_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(profObjRequest);
    }


    public void updateUI(JSONArray response) throws JSONException {
        JSONObject infoObj, friendsObj, allHobbyObj, commonHobbyObj, relatedHobbyObj, eventObj;

        infoObj = response.getJSONObject(0);
        friendsObj = response.getJSONObject(1);
        allHobbyObj = response.getJSONObject(2);
        commonHobbyObj = response.getJSONObject(3);
        relatedHobbyObj = response.getJSONObject(4);
        eventObj = response.getJSONObject(5);

        updateInfo(infoObj);
        if (friendID != userID)
            updateCommonHobby(commonHobbyObj);
        if (isFriend == 0 && friendID != userID)
            updateRelatedHobby(relatedHobbyObj);
        updateAllHobby(allHobbyObj);
        updateFriends(friendsObj);
        updateEvents(eventObj);

        Log.d(TAG, "Updated UI");
        hideProgressDialog();
    }

    public void updateInfo(JSONObject infoObj) throws JSONException {
        int code = infoObj.getInt("code");
        Log.d(TAG, "Code (info): " + code);


        if (code == 200) {

            // UI references
            TextView infoGender = (TextView) findViewById(R.id.infoGender);
            TextView infoAge = (TextView) findViewById(R.id.infoAge);
            TextView infoEmail = (TextView) findViewById(R.id.infoEmail);
            TextView infoPhone = (TextView) findViewById(R.id.infoPhone);
            TextView infoLocation = (TextView) findViewById(R.id.infoLocation);
            TextView infoCity = (TextView) findViewById(R.id.infoCity);

            // extract data
            JSONObject info = infoObj.getJSONObject("info");

            // set data
            infoGender.setText(friendGender);
            infoAge.setText(info.getString("age"));
            infoEmail.setText(info.getString("user_email"));
            Linkify.addLinks(infoEmail, Linkify.EMAIL_ADDRESSES);
            infoPhone.setText(info.getString("phone_number"));
            Linkify.addLinks(infoPhone, Linkify.PHONE_NUMBERS);
            infoLocation.setText(info.getString("location"));
            Linkify.addLinks(infoLocation, Linkify.MAP_ADDRESSES);
            infoCity.setText(info.getString("city"));

        }
    }

    public void updateCommonHobby(JSONObject commonHobbyObj) throws JSONException {
        int code = commonHobbyObj.getInt("code");
        Log.d(TAG, "Code (common hobby): " + code);

        LinearLayout hobbyLayout = (LinearLayout) commonHobbyView.findViewById(R.id.hobbyLayout);
        TextView hobbyError = (TextView) commonHobbyView.findViewById(R.id.hobbyError);

        if (code == 200) {
            hobbyLayout.setVisibility(View.VISIBLE);
            hobbyError.setVisibility(View.GONE);

            final ArrayList<Hobby> commonArrayList = new ArrayList<>();
            JSONArray hobbyJSONArray = commonHobbyObj.getJSONArray("common_hobby");
            for (int i = 0; i < hobbyJSONArray.length(); i++) {
                JSONObject hobby = hobbyJSONArray.getJSONObject(i);
                Hobby h = new Hobby(hobby.getInt("hobby_id"), hobby.getString("hobby_name"),
                        hobbyImageID, hobby.getBoolean("is_user_hobby"));
                commonArrayList.add(h);
            }

            if (commonArrayList.isEmpty()) {
                hobbyError.setVisibility(View.VISIBLE);
                hobbyError.setText(R.string.hobby_empty_error);
            }

            ExpandableHeightGridView hobbyGrid = (ExpandableHeightGridView) commonHobbyView.findViewById(R.id.hobbyGrid);
            HobbyGridAdapter hobbyGridAdapter = new HobbyGridAdapter(getApplicationContext(), commonArrayList);
            hobbyGrid.setAdapter(hobbyGridAdapter);
            hobbyGrid.setExpanded(true);
            hobbyGrid.setEmptyView(hobbyError);

            hobbyGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                }
            });

        } else if (code == 204) {
            hobbyLayout.setVisibility(View.GONE);
            hobbyError.setText(commonHobbyObj.getString("common_hobby"));
            hobbyError.setVisibility(View.VISIBLE);
        } else {
            hobbyLayout.setVisibility(View.GONE);
            hobbyError.setVisibility(View.VISIBLE);
        }
    }

    public void updateRelatedHobby(JSONObject relatedHobbyObj) throws JSONException {
        int code = relatedHobbyObj.getInt("code");
        Log.d(TAG, "Code (related hobby): " + code);

        LinearLayout hobbyLayout = (LinearLayout) relatedHobbyView.findViewById(R.id.hobbyLayout);
        TextView hobbyError = (TextView) relatedHobbyView.findViewById(R.id.hobbyError);

        if (code == 200) {
            hobbyLayout.setVisibility(View.VISIBLE);
            hobbyError.setVisibility(View.GONE);

            final ArrayList<Hobby> relatedArrayList = new ArrayList<>();
            JSONArray hobbyJSONArray = relatedHobbyObj.getJSONArray("related_hobby");
            for (int i = 0; i < hobbyJSONArray.length(); i++) {
                JSONObject hobby = hobbyJSONArray.getJSONObject(i);
                Hobby h = new Hobby(hobby.getInt("related_hobby_id"), hobby.getString("hobby_name"),
                        hobbyImageID, hobby.getBoolean("is_user_hobby"));
                relatedArrayList.add(h);
            }

            if (relatedArrayList.isEmpty()) {
                hobbyError.setVisibility(View.VISIBLE);
                hobbyError.setText(R.string.hobby_empty_error);
            }

            ExpandableHeightGridView hobbyGrid = (ExpandableHeightGridView) relatedHobbyView.findViewById(R.id.hobbyGrid);
            HobbyGridAdapter hobbyGridAdapter = new HobbyGridAdapter(getApplicationContext(), relatedArrayList);
            hobbyGrid.setAdapter(hobbyGridAdapter);
            hobbyGrid.setExpanded(true);
            hobbyGrid.setEmptyView(hobbyError);

            hobbyGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                }
            });

        } else if (code == 204) {
            hobbyLayout.setVisibility(View.GONE);
            hobbyError.setText(relatedHobbyObj.getString("related_hobby"));
            hobbyError.setVisibility(View.VISIBLE);
        } else {
            hobbyLayout.setVisibility(View.GONE);
            hobbyError.setVisibility(View.VISIBLE);
        }
    }

    public void updateAllHobby(JSONObject allHobbyObj) throws JSONException {
        int code = allHobbyObj.getInt("code");
        Log.d(TAG, "Code (hobby): " + code);

        LinearLayout hobbyLayout = (LinearLayout) allHobbyView.findViewById(R.id.hobbyLayout);
        TextView hobbyError = (TextView) allHobbyView.findViewById(R.id.hobbyError);

        if (code == 200) {
            hobbyLayout.setVisibility(View.VISIBLE);
            hobbyError.setVisibility(View.GONE);

            final ArrayList<Hobby> hobbyArrayList = new ArrayList<>();
            JSONArray hobbyJSONArray = allHobbyObj.getJSONArray("hobby");
            for (int i = 0; i < hobbyJSONArray.length(); i++) {
                JSONObject hobby = hobbyJSONArray.getJSONObject(i);
                Hobby h = new Hobby(hobby.getInt("hobby_id"), hobby.getString("hobby_name"),
                        hobbyImageID, hobby.getBoolean("is_user_hobby"));
                hobbyArrayList.add(h);
            }

            if (hobbyArrayList.isEmpty()) {
                hobbyError.setVisibility(View.VISIBLE);
                hobbyError.setText(R.string.hobby_empty_error);
            }

            ExpandableHeightGridView hobbyGrid = (ExpandableHeightGridView) allHobbyView.findViewById(R.id.hobbyGrid);
            HobbyGridAdapter hobbyGridAdapter = new HobbyGridAdapter(getApplicationContext(), hobbyArrayList);
            hobbyGrid.setAdapter(hobbyGridAdapter);
            hobbyGrid.setExpanded(true);
            hobbyGrid.setEmptyView(hobbyError);

            hobbyGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                }
            });

        } else {
            hobbyLayout.setVisibility(View.GONE);
            hobbyError.setVisibility(View.VISIBLE);
        }
    }

    public void updateFriends(JSONObject friendsObj) throws JSONException {
        int code = friendsObj.getInt("code");
        Log.d(TAG, "Code (friends): " + code);

        LinearLayout friendLayout = (LinearLayout) allFriendsView.findViewById(R.id.friendLayout);
        TextView friendError = (TextView) allFriendsView.findViewById(R.id.friendError);

        if (code == 200) {
            friendLayout.setVisibility(View.VISIBLE);
            friendError.setVisibility(View.GONE);

            final ArrayList<User> friendArrayList = new ArrayList<>();
            JSONArray friendJSONArray = friendsObj.getJSONArray("friends");
            for (int i = 0; i < friendJSONArray.length(); i++) {
                JSONObject friend = friendJSONArray.getJSONObject(i);
                User user = new User(friend.getInt("friend_id"), friend.getString("user_name"),
                        friend.getString("gender"), friend.getBoolean("is_your_friend"));
                friendArrayList.add(user);
            }

            if (friendArrayList.isEmpty()) {
                friendError.setVisibility(View.VISIBLE);
                friendError.setText(R.string.friend_empty_error);
            }

            ExpandableHeightGridView friendGrid = (ExpandableHeightGridView) allFriendsView.findViewById(R.id.friendGrid);
            FriendGridAdapter friendGridAdapter = new FriendGridAdapter(getApplicationContext(), friendArrayList);
            friendGrid.setAdapter(friendGridAdapter);
            friendGrid.setExpanded(true);
            friendGrid.setEmptyView(friendError);

            friendGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                    int friendID = friendArrayList.get(position).getId();
                    String friendName = friendArrayList.get(position).getName();
                    String friendGender = friendArrayList.get(position).getGender();
                    boolean isFriend = friendArrayList.get(position).isFriend();
                    int checkFriend = (isFriend) ? 1 : 0;
                    Intent intent = new Intent(view.getContext(), UserActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putInt("FRIEND_ID", friendID);
                    bundle.putString("FRIEND_NAME", friendName);
                    bundle.putString("FRIEND_GENDER", friendGender);
                    bundle.putInt("IS_FRIEND", checkFriend);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            });

        } else {
            friendLayout.setVisibility(View.GONE);
            friendError.setVisibility(View.VISIBLE);
        }

    }

    public void updateEvents(JSONObject eventObj) throws JSONException {
        int code = eventObj.getInt("code");
        Log.d(TAG, "Code (events): " + code);

        LinearLayout eventLayout = (LinearLayout) allEventView.findViewById(R.id.eventLayout);
        TextView eventError = (TextView) allEventView.findViewById(R.id.eventError);

        if (code == 200) {
            eventLayout.setVisibility(View.VISIBLE);
            eventError.setVisibility(View.GONE);

            final ArrayList<Event> eventArrayList = new ArrayList<>();
            JSONArray eventJSONArray = eventObj.getJSONArray("event");
            for (int i = 0; i < eventJSONArray.length(); i++) {
                JSONObject event = eventJSONArray.getJSONObject(i);
                Event e = new Event(event.getInt("event_id"), event.getString("event_name"),
                        event.getString("event_city"), event.getString("event_date"), eventImageID,
                        event.getBoolean("is_user_attending"));
                eventArrayList.add(e);
            }

            if (eventArrayList.isEmpty()) {
                eventLayout.setVisibility(View.GONE);
                eventError.setVisibility(View.VISIBLE);
                eventError.setText(R.string.event_empty_error);
            }

            RecyclerView eventList = (RecyclerView) allEventView.findViewById(R.id.eventList);
            LinearLayoutManager manager = new LinearLayoutManager(getApplicationContext());
            EventListAdapter eventListAdapter = new EventListAdapter(getApplicationContext(), eventArrayList);
            eventList.setHasFixedSize(true);
            eventList.setLayoutManager(manager);
            eventList.setAdapter(eventListAdapter);

            eventList.addOnItemTouchListener(new RecyclerViewClickListener(getApplicationContext(),
                    new RecyclerViewClickListener.OnItemClickListener() {
                        @Override
                        public void onItemClick(View view, final int position) {
                            // ask if they wanna attend that event
                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(view.getContext());
                            alertDialogBuilder.setTitle(R.string.add_event_dialog_title);
                            alertDialogBuilder.setMessage(R.string.add_event_dialog_message);
                            alertDialogBuilder.setPositiveButton(R.string.dialog_positive_button,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface arg0, int arg1) {
                                            if (eventArrayList.get(position).isAttending())
                                                removeEvent(eventArrayList.get(position));
                                            else
                                                addEvent(eventArrayList.get(position));
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
                        }
                    }));

        } else {
            eventLayout.setVisibility(View.GONE);
            eventError.setVisibility(View.VISIBLE);
        }

    }

    private void addFriend() {
        pDialog.setMessage(getString(R.string.add_friend_progress_dialog_message));
        showProgressDialog();

        String urlString = SERVER_URL + "/user/add/friend/" + friendID;

        // handle cookies
        CookieManager cookieManager = new CookieManager(new PersistentCookieStore(getApplicationContext()),
                CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(cookieManager);

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                urlString, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "Response: " + response.toString());

                        try {
                            String message = response.getString("message");
                            Log.d(TAG, "Message: " + message);
                            int code = response.getInt("code");
                            Log.d(TAG, "Code: " + code);

                            if (code == 200) {
                                friendOperationFAB.setImageResource(R.drawable.account_minus);
                                hideProgressDialog();
                                Snackbar.make(friendOperationFAB, R.string.add_friend_success, Snackbar.LENGTH_SHORT).show();
                            } else {
                                hideProgressDialog();
                                Snackbar.make(friendOperationFAB, R.string.add_friend_error, Snackbar.LENGTH_SHORT).show();
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

    private void removeFriend() {
        pDialog.setMessage(getString(R.string.remove_friend_progress_dialog_message));
        showProgressDialog();

        String urlString = SERVER_URL + "/user/delete/friend/" + friendID;

        // handle cookies
        CookieManager cookieManager = new CookieManager(new PersistentCookieStore(getApplicationContext()),
                CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(cookieManager);

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                urlString, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "Response: " + response.toString());

                        try {
                            String message = response.getString("message");
                            Log.d(TAG, "Message: " + message);
                            int code = response.getInt("code");
                            Log.d(TAG, "Code: " + code);

                            if (code == 200) {
                                friendOperationFAB.setImageResource(R.drawable.account_plus);
                                hideProgressDialog();
                                Snackbar.make(friendOperationFAB, R.string.remove_friend_success, Snackbar.LENGTH_SHORT).show();
                            } else {
                                hideProgressDialog();
                                Snackbar.make(friendOperationFAB, R.string.remove_friend_error, Snackbar.LENGTH_SHORT).show();
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

    private void addEvent(Event event) {
        pDialog.setMessage(getString(R.string.add_event_progress_dialog_message));
        showProgressDialog();

        String urlString = SERVER_URL + "/user/add/event/" + event.getEventID();

        // handle cookies
        CookieManager cookieManager = new CookieManager(new PersistentCookieStore(getApplicationContext()),
                CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(cookieManager);

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                urlString, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "Response: " + response.toString());

                        try {
                            String message = response.getString("message");
                            Log.d(TAG, "Message: " + message);
                            int code = response.getInt("code");
                            Log.d(TAG, "Code: " + code);

                            if (code == 200) {
                                hideProgressDialog();
                                Toast.makeText(getApplicationContext(), R.string.add_event_success, Toast.LENGTH_SHORT).show();
                            } else {
                                hideProgressDialog();
                                Toast.makeText(getApplicationContext(), R.string.add_event_error, Toast.LENGTH_SHORT).show();
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

    private void removeEvent(Event event) {
        pDialog.setMessage(getString(R.string.remove_event_progress_dialog_message));
        showProgressDialog();

        String urlString = SERVER_URL + "/user/delete/event/" + event.getEventID();

        // handle cookies
        CookieManager cookieManager = new CookieManager(new PersistentCookieStore(getApplicationContext()),
                CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(cookieManager);

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                urlString, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "Response: " + response.toString());

                        try {
                            String message = response.getString("message");
                            Log.d(TAG, "Message: " + message);
                            int code = response.getInt("code");
                            Log.d(TAG, "Code: " + code);

                            if (code == 200) {
                                hideProgressDialog();
                                Toast.makeText(getApplicationContext(), R.string.remove_event_success, Toast.LENGTH_SHORT).show();
                            } else {
                                hideProgressDialog();
                                Toast.makeText(getApplicationContext(), R.string.remove_event_error, Toast.LENGTH_SHORT).show();
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
            contentUser.setVisibility(View.GONE);
            pDialog.show();
        }
    }

    private void hideProgressDialog() {
        if (pDialog.isShowing()) {
            contentUser.setVisibility(View.VISIBLE);
            pDialog.dismiss();
        }
    }

}
