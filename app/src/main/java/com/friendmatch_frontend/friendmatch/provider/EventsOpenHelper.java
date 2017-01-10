package com.friendmatch_frontend.friendmatch.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

// The SQLiteOpenHelper implementation for events.

class EventsOpenHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = DbSchema.DB_NAME;
    private static final int VERSION = 1;

    EventsOpenHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DbSchema.DDL_CREATE_TBL_EVENTS);
        db.execSQL(DbSchema.DDL_CREATE_TRIGGER_DELETE_EVENT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DbSchema.DDL_DROP_TBL_EVENT);
        db.execSQL(DbSchema.DDL_DROP_TRIGGER_DELETE_EVENT);
        onCreate(db);
    }

}