package com.friendmatch_frontend.friendmatch.fragments;

import android.app.ProgressDialog;
import android.content.ContentValues;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import static com.friendmatch_frontend.friendmatch.application.AppController.SERVER_URL;
import static com.friendmatch_frontend.friendmatch.provider.EventsContract.Events.CITY;
import static com.friendmatch_frontend.friendmatch.provider.EventsContract.Events.CONTENT_URI;
import static com.friendmatch_frontend.friendmatch.provider.EventsContract.Events.DATE;
import static com.friendmatch_frontend.friendmatch.provider.EventsContract.Events.EVENT_ID;
import static com.friendmatch_frontend.friendmatch.provider.EventsContract.Events.EVENT_NAME;

public class UserEventFragment extends Fragment {

    private final String TAG = getClass().getSimpleName();
    ProgressDialog pDialog;
    EventListAdapter eventListAdapter;
    ArrayList<Event> eventArrayList;
    int eventImageID = R.drawable.event;

    View userEventView;
    LinearLayout eventLayout;
    TextView eventError;
    TextView eventSectionHeading;

    public UserEventFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_event, container, false);

        // initialize progress dialog
        pDialog = new ProgressDialog(getContext());
        pDialog.setMessage(getString(R.string.event_progress_dialog_message));
        pDialog.setCancelable(false);

        // getUserEvents(view);
        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onResume() {
        getUserEvents(getView());
        super.onResume();
    }

    public void getUserEvents(View view) {

        showProgressDialog();

        String urlString = SERVER_URL + "/user/event";

        // handle cookies
        CookieManager cookieManager = new CookieManager(new PersistentCookieStore(getContext()),
                CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(cookieManager);

        userEventView = view.findViewById(R.id.userEventView);
        eventLayout = (LinearLayout) userEventView.findViewById(R.id.eventLayout);
        eventError = (TextView) userEventView.findViewById(R.id.eventError);
        eventSectionHeading = (TextView) userEventView.findViewById(R.id.eventSectionHeading);
        eventSectionHeading.setText(R.string.user_event_heading);

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
                                JSONArray eventJSONArray = (response.getJSONObject("message")).getJSONArray("event");
                                eventArrayList = new ArrayList<>();

                                for (int i = 0; i < eventJSONArray.length(); i++) {
                                    JSONObject eObj = eventJSONArray.getJSONObject(i);
                                    Event event = new Event(eObj.getInt("event_id"), eObj.getString("event_name"),
                                            eObj.getString("event_city"), eObj.getString("event_date"), eventImageID,
                                            true);  // only the events to be attended by users are mentioned
                                    eventArrayList.add(event);
                                }

                                if (eventArrayList.isEmpty()) {
                                    eventLayout.setVisibility(View.GONE);
                                    eventError.setVisibility(View.VISIBLE);
                                    eventError.setText(R.string.event_empty_error);
                                } else {
                                    storeEventsToDB(eventArrayList);
                                }

                                RecyclerView eventList = (RecyclerView) userEventView.findViewById(R.id.eventList);
                                LinearLayoutManager manager = new LinearLayoutManager(userEventView.getContext());
                                eventListAdapter = new EventListAdapter(userEventView.getContext(), eventArrayList);
                                eventList.setHasFixedSize(true);
                                eventList.setLayoutManager(manager);
                                eventList.setAdapter(eventListAdapter);

                                eventList.addOnItemTouchListener(new RecyclerViewClickListener(getContext(),
                                        new RecyclerViewClickListener.OnItemClickListener() {
                                            @Override
                                            public void onItemClick(View view, final int position) {

                                                // ask if they wanna remove that event
                                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(view.getContext());

                                                if (eventArrayList.get(position).isAttending()) {

                                                    alertDialogBuilder.setTitle(R.string.remove_event_dialog_title);
                                                    alertDialogBuilder.setMessage(R.string.remove_event_dialog_message);
                                                    alertDialogBuilder.setPositiveButton(R.string.dialog_positive_button,
                                                            new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface arg0, int arg1) {
                                                                    removeEvent(eventArrayList.get(position));
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
                                event.setAttending(true);
                                eventArrayList.add(event);
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

    private void removeEvent(final Event event) {
        pDialog.setMessage(getString(R.string.remove_event_progress_dialog_message));
        showProgressDialog();

        String urlString = SERVER_URL + "/user/delete/event/" + event.getEventID();

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
                                event.setAttending(false);
                                eventArrayList.remove(event);
                                eventListAdapter.notifyDataSetChanged();
                                hideProgressDialog();
                                Toast.makeText(getContext(), R.string.remove_event_success, Toast.LENGTH_SHORT).show();
                            } else {
                                hideProgressDialog();
                                Toast.makeText(getContext(), R.string.remove_event_error, Toast.LENGTH_SHORT).show();
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

    // add events that the user is supposed to attend today
    private void storeEventsToDB(ArrayList<Event> eventArrayList) {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        String dateToday = df.format(c.getTime());

        for (Event event : eventArrayList) {
            if (event.getEventDate().equals(dateToday)) {
                ContentValues values = new ContentValues();
                values.put(EVENT_ID, event.getEventID());
                values.put(EVENT_NAME, event.getEventName());
                values.put(CITY, event.getEventCity());
                values.put(DATE, event.getEventDate());

                getContext().getContentResolver().insert(CONTENT_URI, values);
            }
        }
    }

}
