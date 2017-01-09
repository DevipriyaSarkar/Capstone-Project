package com.friendmatch_frontend.friendmatch.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.friendmatch_frontend.friendmatch.R;
import com.friendmatch_frontend.friendmatch.application.AppController;
import com.friendmatch_frontend.friendmatch.utilities.PersistentCookieStore;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import static com.friendmatch_frontend.friendmatch.application.AppController.FIRST_HOBBY_ENTRY;
import static com.friendmatch_frontend.friendmatch.application.AppController.SERVER_URL;

public class EditProfileActivity extends AppCompatActivity {

    final String TAG = this.getClass().getSimpleName();
    static final int SOCKET_TIMEOUT_MS = 5000;
    ScrollView contentEditProfile;
    ProgressDialog pDialog;
    RadioGroup genderRadioGroup;
    RadioButton genderRadioButton, maleButton, femaleButton;
    FloatingActionButton doneFAB;
    EditText editAge, editPhone, editLocation, editCity;
    Bundle bundle;
    int userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SharedPreferences sp = getSharedPreferences("USER_LOGIN", MODE_PRIVATE);
        userID = sp.getInt("user_id", -1);

        contentEditProfile = (ScrollView) findViewById(R.id.contentEditProfile);
        pDialog = new ProgressDialog(this);
        pDialog.setMessage(getString(R.string.profile_progress_dialog_message));
        pDialog.setCancelable(false);

        genderRadioGroup = (RadioGroup) findViewById(R.id.genderRadioGroup);
        maleButton = (RadioButton) findViewById(R.id.maleRadioButton);
        femaleButton = (RadioButton) findViewById(R.id.femaleRadioButton);

        editAge = (EditText) findViewById(R.id.editAge);
        editPhone = (EditText) findViewById(R.id.editPhone);
        editLocation = (EditText) findViewById(R.id.editLocation);
        editCity = (EditText) findViewById(R.id.editCity);
        doneFAB = (FloatingActionButton) findViewById(R.id.doneFAB);

        bundle = getIntent().getExtras();
        if (bundle != null) {
            if (bundle.getString("gender") != null)
                setOldValues();
        }

        doneFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                boolean isFormFilled = true;

                char gender = ' ';
                int selectedId = genderRadioGroup.getCheckedRadioButtonId();
                if (selectedId != -1) {
                    genderRadioButton = (RadioButton) findViewById(selectedId);
                    gender = (genderRadioButton.getText().toString().equals("Male")) ? 'M' : 'F';
                } else {
                    isFormFilled = false;
                }

                String ageStr = editAge.getText().toString();
                int age = 0;

                if (isEmpty(ageStr)) {
                    isFormFilled = false;
                } else
                    age = Integer.parseInt(ageStr);

                String phone = editPhone.getText().toString();
                String location = editLocation.getText().toString();
                String city = editCity.getText().toString();

                if (isEmpty(phone) || isEmpty(location) || isEmpty(city)) {
                    isFormFilled = false;
                }

                if (isFormFilled) {
                    try {
                        saveEditedProfile(gender, age, phone, location, city);
                    } catch (MalformedURLException | URISyntaxException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Please fill all the details to proceed.", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void setOldValues() {
        String pGender = bundle.getString("gender");
        int pAge = bundle.getInt("age");
        String pPhone = bundle.getString("phone");
        String pLoc = bundle.getString("location");
        String pCity = bundle.getString("city");

        if (pGender != null) {
            if (pGender.equals("Male"))
                maleButton.setChecked(true);
            else
                femaleButton.setChecked(true);
        }

        editAge.setText(String.valueOf(pAge));
        editPhone.setText(pPhone);
        editLocation.setText(pLoc);
        editCity.setText(pCity);
    }

    private void saveEditedProfile(final char gender, int age, String phone, String location, String city)
            throws MalformedURLException, URISyntaxException {
        pDialog.setMessage(getString(R.string.saving_progress_dialog_message));
        showProgressDialog();

        String urlString = SERVER_URL + "/user/" + userID + "/edit/profile?gender="
                + gender + "&age=" + age + "&phone=" + phone + "&location=" + location + "&city=" + city;

        // URL encode the string
        URL url = new URL(urlString);
        URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(),
                url.getPath(), url.getQuery(), url.getRef());

        urlString = uri.toASCIIString();

        // handle cookies
        CookieManager cookieManager = new CookieManager(new PersistentCookieStore(getApplicationContext()),
                CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(cookieManager);

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                urlString, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, response.toString());
                        try {
                            if (response.getInt("code") == 200) {

                                SharedPreferences sharedPref = getSharedPreferences("USER_LOGIN", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.putString("gender", String.valueOf(gender));
                                editor.commit();

                                hideProgressDialog();
                                Toast.makeText(getApplicationContext(), R.string.profile_saved_message, Toast.LENGTH_SHORT).show();
                                finish();

                                if (FIRST_HOBBY_ENTRY) {
                                    Intent intent = new Intent(getApplicationContext(), HobbyActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                    startActivity(intent);
                                } else {
                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                    startActivity(intent);
                                }
                            } else {
                                hideProgressDialog();
                                Toast.makeText(getApplicationContext(), R.string.network_error, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.d(TAG, e.getMessage());
                            hideProgressDialog();
                            Toast.makeText(getApplicationContext(), R.string.network_error, Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                hideProgressDialog();
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

    private void showProgressDialog() {
        if (!pDialog.isShowing()) {
            contentEditProfile.setVisibility(View.GONE);
            pDialog.show();
        }
    }

    private void hideProgressDialog() {
        if (pDialog.isShowing()) {
            contentEditProfile.setVisibility(View.VISIBLE);
            pDialog.dismiss();
        }
    }

    private boolean isEmpty(String string) {
        return string == null || string.equals("") || string.equals(" ");
    }

}
