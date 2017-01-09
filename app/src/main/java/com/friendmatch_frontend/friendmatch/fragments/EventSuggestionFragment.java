package com.friendmatch_frontend.friendmatch.fragments;


import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.friendmatch_frontend.friendmatch.R;
import com.friendmatch_frontend.friendmatch.adapters.EventListAdapter;
import com.friendmatch_frontend.friendmatch.application.AppController;
import com.friendmatch_frontend.friendmatch.models.Event;
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


public class EventSuggestionFragment extends Fragment {

    private final String TAG = getClass().getSimpleName();
    ProgressDialog pDialog;
    TextView eventError;
    ArrayList<Event> eventArrayList;
    EventListAdapter eventListAdapter;
    int eventImageID = R.drawable.event;

    public EventSuggestionFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // initialize progress dialog
        pDialog = new ProgressDialog(getContext());
        pDialog.setMessage(getString(R.string.suggestion_event_progress_dialog_message));
        pDialog.setCancelable(false);

        getEventsSuggestion(container);

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_event_suggestion, container, false);
    }

    public void getEventsSuggestion(final ViewGroup container) {

        showProgressDialog();

        String urlString = SERVER_URL + "/user/suggest/events";

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
                                JSONArray eventJSONArray = (response.getJSONObject("message")).getJSONArray("suggested_event");
                                eventArrayList = new ArrayList<>();

                                for (int i = 0; i < eventJSONArray.length(); i++) {
                                    JSONObject eventObj = eventJSONArray.getJSONObject(i);
                                    Event event = new Event(eventObj.getInt("event_id"), eventObj.getString("event_name"),
                                            eventObj.getString("event_city"), eventObj.getString("event_date"), eventImageID,
                                            false);     // only events that the user is not already attending are suggested
                                    eventArrayList.add(event);
                                }

                                if (eventArrayList.isEmpty()) {
                                    LinearLayout eventSuggestionLayout = (LinearLayout)
                                            container.findViewById(R.id.eventSuggestionLayout);
                                    eventSuggestionLayout.setVisibility(View.GONE);
                                    eventError = (TextView) container.findViewById(R.id.eventError);
                                    eventError.setVisibility(View.VISIBLE);
                                    eventError.setText(R.string.event_empty_error);
                                }

                                RecyclerView eventList = (RecyclerView) container.findViewById(R.id.eventList);
                                LinearLayoutManager manager = new LinearLayoutManager(container.getContext());
                                eventListAdapter = new EventListAdapter(container.getContext(), eventArrayList);
                                eventList.setHasFixedSize(true);
                                eventList.setLayoutManager(manager);
                                eventList.setAdapter(eventListAdapter);

                                eventList.addOnItemTouchListener(new RecyclerViewClickListener(getContext(),
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

    private void addEvent(final Event event) {
        pDialog.setMessage(getString(R.string.add_event_progress_dialog_message));
        showProgressDialog();

        String urlString = SERVER_URL + "/user/add/event/" + event.getEventID();

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
                                eventArrayList.remove(event);
                                eventListAdapter.notifyDataSetChanged();
                                hideProgressDialog();
                                Toast.makeText(getContext(), R.string.add_event_success, Toast.LENGTH_SHORT).show();
                            } else {
                                hideProgressDialog();
                                Toast.makeText(getContext(), R.string.add_event_error, Toast.LENGTH_SHORT).show();
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
