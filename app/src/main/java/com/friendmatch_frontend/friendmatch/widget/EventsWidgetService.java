package com.friendmatch_frontend.friendmatch.widget;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.widget.RemoteViewsService;

// EventsWidgetService is the {@link RemoteViewsService} that will return our RemoteViewsFactory

public class EventsWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

        return (new EventsWidgetDataProvider(this.getApplicationContext(), intent));
    }

}