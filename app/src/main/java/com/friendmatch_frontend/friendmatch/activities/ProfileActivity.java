package com.friendmatch_frontend.friendmatch.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.friendmatch_frontend.friendmatch.R;
import com.friendmatch_frontend.friendmatch.adapters.FriendGridAdapter;
import com.friendmatch_frontend.friendmatch.application.AppController;
import com.friendmatch_frontend.friendmatch.models.User;
import com.friendmatch_frontend.friendmatch.utilities.ExpandableHeightGridView;
import com.friendmatch_frontend.friendmatch.utilities.PersistentCookieStore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.ArrayList;

import static com.friendmatch_frontend.friendmatch.application.AppController.SERVER_URL;

public class ProfileActivity extends AppCompatActivity {

    private final String TAG = this.getClass().getSimpleName();
    static final int SOCKET_TIMEOUT_MS = 5000;
    ProgressDialog pDialog;
    ScrollView activityProfile;
    int userID;

    int age;
    String gender, email, phone, location, city;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        activityProfile = (ScrollView) findViewById(R.id.activity_profile);

        // initialize progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setMessage(getString(R.string.profile_progress_dialog_message));
        pDialog.setCancelable(false);

        getUserProfile();
    }

    /**
     * Represents an asynchronous login/registration task used to fetch profile data
     */
    public void getUserProfile() {

        showProgressDialog();

        String urlString = SERVER_URL + "/user/profile";

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
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(SOCKET_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(jsonObjReq);
    }

    public void updateUI(JSONArray response) throws JSONException {
        JSONObject infoObj, friendsObj;

        infoObj = response.getJSONObject(0);
        friendsObj = response.getJSONObject(1);

        updateInfo(infoObj);
        updateFriends(friendsObj);

        Button profEditButton = (Button) findViewById(R.id.profEditButton);
        profEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putString("gender", gender);
                bundle.putInt("age", age);
                bundle.putString("phone", phone);
                bundle.putString("location", location);
                bundle.putString("city", city);
                Intent intent = new Intent(getApplicationContext(), EditProfileActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        Log.d(TAG, "Updated UI");
        hideProgressDialog();
    }

    public void updateInfo(JSONObject infoObj) throws JSONException {
        int code = infoObj.getInt("code");
        Log.d(TAG, "Code (info): " + code);

        LinearLayout infoLayout = (LinearLayout) findViewById(R.id.infoLayout);
        TextView infoError = (TextView) findViewById(R.id.infoError);

        if (code == 200) {
            infoLayout.setVisibility(View.VISIBLE);
            infoError.setVisibility(View.GONE);

            // UI references
            ImageView infoImage = (ImageView) findViewById(R.id.infoImage);
            TextView infoName = (TextView) findViewById(R.id.infoName);
            TextView infoGender = (TextView) findViewById(R.id.infoGender);
            TextView infoAge = (TextView) findViewById(R.id.infoAge);
            TextView infoEmail = (TextView) findViewById(R.id.infoEmail);
            TextView infoPhone = (TextView) findViewById(R.id.infoPhone);
            TextView infoLocation = (TextView) findViewById(R.id.infoLocation);
            TextView infoCity = (TextView) findViewById(R.id.infoCity);

            // extract data
            JSONObject info = infoObj.getJSONObject("info");
            userID = info.getInt("id");
            if (info.getString("gender").equals("M")) {
                gender = "Male";
                setProfilePicture(infoImage, 0);
            } else {
                gender = "Female";
                setProfilePicture(infoImage, 1);
            }

            age = info.getInt("age");
            email = info.getString("user_email");
            phone = info.getString("phone_number");
            location = info.getString("location");
            city = info.getString("city");

            // set data
            infoName.setText(info.getString("user_name"));
            infoGender.setText(gender);
            infoAge.setText(String.valueOf(age));
            infoEmail.setText(email);
            Linkify.addLinks(infoEmail, Linkify.EMAIL_ADDRESSES);
            infoPhone.setText(phone);
            Linkify.addLinks(infoPhone, Linkify.PHONE_NUMBERS);
            infoLocation.setText(location);
            Linkify.addLinks(infoLocation, Linkify.MAP_ADDRESSES);
            infoCity.setText(city);

        } else {
            infoLayout.setVisibility(View.GONE);
            infoError.setVisibility(View.VISIBLE);
        }
    }


    public void updateFriends(JSONObject friendsObj) throws JSONException {
        int code = friendsObj.getInt("code");
        Log.d(TAG, "Code (friends): " + code);

        LinearLayout friendLayout = (LinearLayout) findViewById(R.id.friendLayout);
        TextView friendError = (TextView) findViewById(R.id.friendError);

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

            ExpandableHeightGridView friendGrid = (ExpandableHeightGridView) findViewById(R.id.friendGrid);
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

    public void setProfilePicture(ImageView imageView, int gender) {
        // 0 for male, 1 for female
        Bitmap iconBitmap;
        if (gender == 0) {
            iconBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.male);
        } else {
            iconBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.female);
        }

        RoundedBitmapDrawable roundDrawable =
                RoundedBitmapDrawableFactory.create(getResources(), iconBitmap);
        roundDrawable.setCornerRadius(Math.max(iconBitmap.getWidth(), iconBitmap.getHeight()) / 2.0f);
        imageView.setImageDrawable(roundDrawable);   // round profile picture
    }

    private void showProgressDialog() {
        if (!pDialog.isShowing()) {
            activityProfile.setVisibility(View.GONE);
            pDialog.show();
        }
    }

    private void hideProgressDialog() {
        if (pDialog.isShowing()) {
            activityProfile.setVisibility(View.VISIBLE);
            pDialog.dismiss();
        }
    }

}
