package com.friendmatch_frontend.friendmatch.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.TaskParams;

public class EventsTodayIntentService extends IntentService {

    public EventsTodayIntentService(){
        super(EventsTodayIntentService.class.getName());
    }

    public EventsTodayIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(EventsTodayIntentService.class.getSimpleName(), "Events Today Intent Service");
        EventsTodayTaskService eventsTodayTaskService = new EventsTodayTaskService(getApplicationContext());
        Bundle args = new Bundle();
        if (intent.getStringExtra("TAG").equals("UPDATE")){
            args.putString("ACTION", intent.getStringExtra("ACTION"));
            args.putParcelable("EVENT", intent.getParcelableExtra("EVENT"));
        }
        // We can call OnRunTask from the intent service to force it to run immediately instead of
        // scheduling a task.
        eventsTodayTaskService.onRunTask(new TaskParams(intent.getStringExtra("TAG"), args));
    }
}
