package com.friendmatch_frontend.friendmatch.services;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.friendmatch_frontend.friendmatch.R;
import com.friendmatch_frontend.friendmatch.application.AppController;
import com.friendmatch_frontend.friendmatch.models.Event;
import com.friendmatch_frontend.friendmatch.provider.EventsContract;
import com.friendmatch_frontend.friendmatch.utilities.PersistentCookieStore;
import com.friendmatch_frontend.friendmatch.widget.EventsWidgetProvider;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;

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

public class EventsTodayTaskService extends GcmTaskService {

    private static final String TAG = EventsTodayTaskService.class.getSimpleName();
    private ArrayList<Event> eventArrayList;
    int eventImageID = R.drawable.event;
    private Context context;
    int result;

    public EventsTodayTaskService() {
    }

    public EventsTodayTaskService(Context context) {
        this.context = context;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onRunTask(TaskParams taskParams) {
        Log.d(TAG, "onRunTask: TAG: " + taskParams.getTag());
        result = GcmNetworkManager.RESULT_FAILURE;

        if (taskParams.getTag().equals("INIT") || taskParams.getTag().equals("PERIODIC")) {
            String urlString = SERVER_URL + "/user/event";
            // handle cookies
            CookieManager cookieManager = new CookieManager(new PersistentCookieStore(context),
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
                                    result = GcmNetworkManager.RESULT_SUCCESS;
                                    JSONArray eventJSONArray = (response.getJSONObject("message")).getJSONArray("event");
                                    eventArrayList = new ArrayList<>();

                                    for (int i = 0; i < eventJSONArray.length(); i++) {
                                        JSONObject eObj = eventJSONArray.getJSONObject(i);
                                        Event event = new Event(eObj.getInt("event_id"), eObj.getString("event_name"),
                                                eObj.getString("event_city"), eObj.getString("event_date"), eventImageID,
                                                true);  // only the events to be attended by users are mentioned
                                        eventArrayList.add(event);
                                    }

                                    if (!eventArrayList.isEmpty()) {
                                        storeEventsToDB(eventArrayList);
                                    }
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
            updateWidget();

        } else if (taskParams.getTag().equals("UPDATE")) {
            if (taskParams.getExtras().getString("ACTION").equals("ADD")) {
                // add event to db
                Event event = taskParams.getExtras().getParcelable("EVENT");
                ContentValues values = new ContentValues();
                values.put(EVENT_ID, event.getEventID());
                values.put(EVENT_NAME, event.getEventName());
                values.put(CITY, event.getEventCity());
                values.put(DATE, event.getEventDate());
                getContentResolver().insert(CONTENT_URI, values);
                updateWidget();

            } else if (taskParams.getExtras().getString("ACTION").equals("DELETE")) {
                // delete event from db
                Event event = taskParams.getExtras().getParcelable("EVENT");
                Uri delUri = ContentUris.withAppendedId(CONTENT_URI, event.getEventID());
                getContentResolver().delete(delUri, null, null);
                updateWidget();
            }
        }
        return result;
    }

    // add events that the user is supposed to attend today
    private void storeEventsToDB(ArrayList<Event> eventArrayList) {
        Log.d(TAG, "storing today's events to db");

        context.getContentResolver().delete(CONTENT_URI, null, null);

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

                context.getContentResolver().insert(CONTENT_URI, values);
            }
        }

        updateWidget();

    }

    private void updateWidget() {
        Intent intent = new Intent(getApplicationContext(), EventsWidgetProvider.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int appWidgetIds[] = AppWidgetManager.getInstance(getApplication())
                .getAppWidgetIds(new ComponentName(getApplication(), EventsWidgetProvider.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        sendBroadcast(intent);
    }
}
