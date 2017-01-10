package com.friendmatch_frontend.friendmatch.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import com.friendmatch_frontend.friendmatch.R;
import com.friendmatch_frontend.friendmatch.activities.EventActivity;

// Implementation of App Widget functionality.

public class EventsWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            Intent serviceIntent = new Intent(context, EventsWidgetService.class);

            serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            serviceIntent.setData(Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME)));

            RemoteViews widget = new RemoteViews(context.getPackageName(), R.layout.events_widget_layout);

            // Sets the remote adapter used to fill in the list items
            widget.setRemoteAdapter(R.id.widgetList, serviceIntent);
            // Display appropriate message if list view empty
            widget.setEmptyView(R.id.widgetList, R.id.empty_view);

            Intent clickIntent = new Intent(context, EventActivity.class);
            PendingIntent clickPI = PendingIntent
                    .getActivity(context, 0,
                            clickIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT);

            widget.setPendingIntentTemplate(R.id.widgetList, clickPI);

            // Instruct the widget manager to update the widget
            ComponentName component = new ComponentName(context, EventsWidgetProvider.class);
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widgetList);
            appWidgetManager.updateAppWidget(component, widget);
        }

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }
}