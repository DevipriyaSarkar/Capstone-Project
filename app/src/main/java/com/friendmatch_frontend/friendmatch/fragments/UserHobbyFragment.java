package com.friendmatch_frontend.friendmatch.fragments;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.friendmatch_frontend.friendmatch.R;
import com.friendmatch_frontend.friendmatch.adapters.HobbyGridAdapter;
import com.friendmatch_frontend.friendmatch.application.AppController;
import com.friendmatch_frontend.friendmatch.models.Hobby;
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

public class UserHobbyFragment extends Fragment {

    private final String TAG = getClass().getSimpleName();
    ProgressDialog pDialog;
    HobbyGridAdapter hobbyGridAdapter;
    ArrayList<Hobby> hobbyArrayList;
    ExpandableHeightGridView hobbyGrid;
    int hobbyImageID = R.drawable.hobby;

    View userHobbyView;
    LinearLayout hobbyLayout;
    TextView hobbyError;
    TextView hobbySectionHeading;

    public UserHobbyFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_hobby, container, false);

        // initialize progress dialog
        pDialog = new ProgressDialog(getContext());
        pDialog.setMessage(getString(R.string.hobby_progress_dialog_message));
        pDialog.setCancelable(false);

        // getUserHobbies(view);
        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onResume() {
        getUserHobbies(getView());
        super.onResume();
    }

    public void getUserHobbies(View view) {

        showProgressDialog();

        String urlString = SERVER_URL + "/user/hobby";

        // handle cookies
        CookieManager cookieManager = new CookieManager(new PersistentCookieStore(getContext()),
                CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(cookieManager);

        userHobbyView = view.findViewById(R.id.userHobbyView);
        hobbyLayout = (LinearLayout) userHobbyView.findViewById(R.id.hobbyLayout);
        hobbyError = (TextView) userHobbyView.findViewById(R.id.hobbyError);
        hobbySectionHeading = (TextView) userHobbyView.findViewById(R.id.hobbySectionHeading);
        hobbySectionHeading.setText(R.string.user_hobbies_heading);

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
                                JSONArray hobbyJSONArray = (response.getJSONObject("message")).getJSONArray("hobby");
                                hobbyArrayList = new ArrayList<>();

                                for (int i = 0; i < hobbyJSONArray.length(); i++) {
                                    JSONObject hObj = hobbyJSONArray.getJSONObject(i);
                                    Hobby h = new Hobby(hObj.getInt("hobby_id"), hObj.getString("hobby_name"), hobbyImageID,
                                            true);  // only user hobbies are showed
                                    hobbyArrayList.add(h);
                                }

                                if (hobbyArrayList.isEmpty()) {
                                    hobbyError.setVisibility(View.VISIBLE);
                                    hobbyError.setText(R.string.hobby_empty_error);
                                }

                                hobbyGrid = (ExpandableHeightGridView)
                                        userHobbyView.findViewById(R.id.hobbyGrid);
                                hobbyGridAdapter =
                                        new HobbyGridAdapter(userHobbyView.getContext(), hobbyArrayList);
                                hobbyGrid.setAdapter(hobbyGridAdapter);
                                hobbyGrid.setExpanded(true);
                                hobbyGrid.setEmptyView(hobbyError);

                                hobbyGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {

                                        // ask if they wanna remove that hobby
                                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(view.getContext());

                                        if (hobbyArrayList.get(i).isHobby()){
                                            alertDialogBuilder.setTitle(R.string.remove_hobby_dialog_title);
                                            alertDialogBuilder.setMessage(R.string.remove_hobby_dialog_message);
                                            alertDialogBuilder.setPositiveButton(R.string.dialog_positive_button,
                                                    new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface arg0, int arg1) {
                                                                deleteHobby(hobbyArrayList.get(i));
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

                                hideProgressDialog();

                            } else {
                                hobbyLayout.setVisibility(View.GONE);
                                hobbyError.setVisibility(View.VISIBLE);
                                hideProgressDialog();
                                Toast.makeText(getContext(), R.string.network_error, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.d(TAG, "JSON Error: " + e.getMessage());
                            hideProgressDialog();
                            Toast.makeText(getContext(), R.string.network_error, Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error in " + TAG + " : " + error.getMessage());
                hideProgressDialog();
                Toast.makeText(getContext(), R.string.network_error, Toast.LENGTH_SHORT).show();
            }
        });

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(jsonObjReq);
    }

    private void addHobby(final Hobby hobby) {
        pDialog.setMessage(getString(R.string.add_hobby_progress_dialog_message));
        showProgressDialog();

        String urlString = SERVER_URL + "/user/add/hobby/" + hobby.getHobbyID();

        // handle cookies
        CookieManager cookieManager = new CookieManager(new PersistentCookieStore(getContext()),
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
                                hobby.setHobby(true);
                                hobbyArrayList.add(hobby);
                                hobbyGridAdapter = new HobbyGridAdapter(userHobbyView.getContext(), hobbyArrayList);
                                hobbyGrid.setAdapter(hobbyGridAdapter);
                                hobbyGrid.invalidateViews();
                                hobbyGridAdapter.notifyDataSetChanged();
                                hideProgressDialog();
                                Toast.makeText(getContext(), R.string.add_hobby_success, Toast.LENGTH_SHORT).show();
                            } else {
                                hideProgressDialog();
                                Toast.makeText(getContext(), R.string.add_hobby_error, Toast.LENGTH_SHORT).show();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.d(TAG, "JSON Error: " + e.getMessage());
                            hideProgressDialog();
                            Toast.makeText(getContext(), R.string.network_error, Toast.LENGTH_SHORT).show();
                        }

                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error in " + TAG + " : " + error.getMessage());
                hideProgressDialog();
                Toast.makeText(getContext(), R.string.network_error, Toast.LENGTH_SHORT).show();
            }
        });

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(jsonObjReq);

    }

    private void deleteHobby(final Hobby hobby) {
        pDialog.setMessage(getString(R.string.remove_hobby_progress_dialog_message));
        showProgressDialog();

        String urlString = SERVER_URL + "/user/delete/hobby/" + hobby.getHobbyID();

        // handle cookies
        CookieManager cookieManager = new CookieManager(new PersistentCookieStore(getContext()),
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
                                hobby.setHobby(false);
                                hobbyArrayList.remove(hobby);
                                hobbyGridAdapter = new HobbyGridAdapter(userHobbyView.getContext(), hobbyArrayList);
                                hobbyGrid.setAdapter(hobbyGridAdapter);
                                hobbyGrid.invalidateViews();
                                hobbyGridAdapter.notifyDataSetChanged();
                                hideProgressDialog();
                                Toast.makeText(getContext(), R.string.remove_hobby_success, Toast.LENGTH_SHORT).show();
                            } else {
                                hideProgressDialog();
                                Toast.makeText(getContext(), R.string.remove_hobby_error, Toast.LENGTH_SHORT).show();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.d(TAG, "JSON Error: " + e.getMessage());
                            hideProgressDialog();
                            Toast.makeText(getContext(), R.string.network_error, Toast.LENGTH_SHORT).show();
                        }

                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error in " + TAG + " : " + error.getMessage());
                hideProgressDialog();
                Toast.makeText(getContext(), R.string.network_error, Toast.LENGTH_SHORT).show();
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
