package com.friendmatch_frontend.friendmatch.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.friendmatch_frontend.friendmatch.R;
import com.friendmatch_frontend.friendmatch.activities.UserActivity;
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


public class FriendSuggestionFragment extends Fragment {

    private final String TAG = getClass().getSimpleName();
    ProgressDialog pDialog;
    TextView friendError;

    public FriendSuggestionFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // initialize progress dialog
        pDialog = new ProgressDialog(getContext());
        pDialog.setMessage(getString(R.string.suggestion_friend_progress_dialog_message));
        pDialog.setCancelable(false);

        getFriendSuggestion(container);

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_friend_suggestion, container, false);
    }

    public void getFriendSuggestion(final ViewGroup container) {

        showProgressDialog();

        String urlString = SERVER_URL + "/user/suggest/friends";

        // handle cookies
        CookieManager cookieManager = new CookieManager(new PersistentCookieStore(getContext()),
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
                                JSONArray friendJSONArray = (response.getJSONObject("message")).getJSONArray("suggestions");
                                final ArrayList<User> friendArrayList = new ArrayList<>();

                                for (int i = 0; i < friendJSONArray.length(); i++) {
                                    JSONObject friend = friendJSONArray.getJSONObject(i);
                                    User user = new User(friend.getInt("id"), friend.getString("user_name"),
                                            friend.getString("gender"), false);     // only non-friend users are showed
                                    friendArrayList.add(user);
                                }

                                if (friendArrayList.isEmpty()) {
                                    friendError = (TextView) container.findViewById(R.id.friendError);
                                    friendError.setVisibility(View.VISIBLE);
                                    friendError.setText(R.string.hobby_empty_error);
                                }

                                ExpandableHeightGridView friendGrid =
                                        (ExpandableHeightGridView) container.findViewById(R.id.friendGrid);
                                FriendGridAdapter friendGridAdapter =
                                        new FriendGridAdapter(getContext(), friendArrayList);
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

                                hideProgressDialog();

                            } else {
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