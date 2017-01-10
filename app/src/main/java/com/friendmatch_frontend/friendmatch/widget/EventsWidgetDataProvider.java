package com.friendmatch_frontend.friendmatch.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.friendmatch_frontend.friendmatch.R;

import java.util.ArrayList;

import static com.friendmatch_frontend.friendmatch.provider.DbSchema.COL_CITY;
import static com.friendmatch_frontend.friendmatch.provider.DbSchema.COL_EVENT_NAME;
import static com.friendmatch_frontend.friendmatch.provider.EventsContract.Events.CONTENT_URI;


// EventsWidgetDataProvider acts as the adapter for the collection view widget,
// providing RemoteViews to the widget in the getViewAt method.

class EventsWidgetDataProvider implements RemoteViewsService.RemoteViewsFactory {

    private Context context = null;
    private ArrayList<String> eventNameArray;  // array list to hold all event names
    private ArrayList<String> eventCityArray;    // array list to hold all event cities

    EventsWidgetDataProvider(Context context, Intent intent) {
        this.context = context;
        int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    @Override
    public void onCreate() {
        // fetch the event data
        initData();
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        return (eventNameArray.size());
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews row = new RemoteViews(context.getPackageName(), R.layout.widget_row);

        // set texts on the widget row
        row.setTextViewText(R.id.widgetEventName, eventNameArray.get(position));
        row.setTextViewText(R.id.widgetEventCity, eventCityArray.get(position));

        // launches the EventActivity on row click
        Intent intent = new Intent();
        row.setOnClickFillInIntent(R.id.widgetRow, intent);

        // set content description
        row.setContentDescription(R.id.widgetEventName, String.format("%s %s",
                context.getString(R.string.list_name_content_desc), eventNameArray.get(position)));
        row.setContentDescription(R.id.widgetEventCity, String.format("%s %s",
                context.getString(R.string.list_city_content_desc), eventCityArray.get(position)));

        return row;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public void onDataSetChanged() {
        // fetch the event data
        initData();
    }

    private void initData() {

        final long token = Binder.clearCallingIdentity();
        try {
            // retrieve today's events
            Uri eventsUri = CONTENT_URI;
            Cursor cur = context.getContentResolver().query(eventsUri, null, null, null, COL_EVENT_NAME);

            if(cur != null) {
                // initialise
                eventNameArray = new ArrayList<>();
                eventCityArray = new ArrayList<>();
                cur.moveToFirst();
                while (!cur.isAfterLast()) {
                    String eventName = cur.getString(cur.getColumnIndex(COL_EVENT_NAME));
                    String eventCity = cur.getString(cur.getColumnIndex(COL_CITY));
                    Log.d("QUERY", "\nEvent Name: " + eventName + ", Event City: " + eventCity);
                    // update the array lists
                    eventNameArray.add(eventName);
                    eventCityArray.add(eventCity);
                    cur.moveToNext();
                }
            }
            if (cur != null) {
                cur.close();
            }
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }
}