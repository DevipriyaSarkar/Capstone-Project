package com.friendmatch_frontend.friendmatch.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import static com.friendmatch_frontend.friendmatch.provider.DbSchema.COL_EVENT_ID;
import static com.friendmatch_frontend.friendmatch.provider.EventsContract.Events.*;

// The actual provider class for the events provider.

public class EventsProvider extends ContentProvider {

    private static final String TAG = "EventsProvider";

    // helper constants for use with the UriMatcher
    private static final int EVENT_LIST = 1;
    private static final int EVENT_ID = 2;
    private static final UriMatcher URI_MATCHER;
    private EventsOpenHelper mHelper = null;

    // prepare the UriMatcher
    static {
        URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        URI_MATCHER.addURI(EventsContract.AUTHORITY, "events", EVENT_LIST);
        URI_MATCHER.addURI(EventsContract.AUTHORITY, "events/#", EVENT_ID);
    }

    @Override
    public boolean onCreate() {
        mHelper = new EventsOpenHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Log.d(TAG, "query");

        SQLiteDatabase db = mHelper.getReadableDatabase();
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        switch (URI_MATCHER.match(uri)) {
            case EVENT_LIST:
                builder.setTables(DbSchema.TBL_EVENTS);
                if (TextUtils.isEmpty(sortOrder)) {
                    sortOrder = SORT_ORDER_DEFAULT;
                }
                break;
            case EVENT_ID:
                builder.setTables(DbSchema.TBL_EVENTS);
                // limit query to one row at most
                builder.appendWhere(EVENT_ID + " = "
                        + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        Cursor cursor = builder.query(db, projection, selection, selectionArgs,
                null, null, sortOrder);
        return cursor;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        Log.d(TAG, "getType");

        switch (URI_MATCHER.match(uri)) {
            case EVENT_LIST:
                return CONTENT_TYPE;
            case EVENT_ID:
                return CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        Log.d(TAG, "insert");

        if (URI_MATCHER.match(uri) != EVENT_LIST) {
            throw new IllegalArgumentException(
                    "Unsupported URI for insertion: " + uri);
        }
        SQLiteDatabase db = mHelper.getWritableDatabase();
        if (URI_MATCHER.match(uri) == EVENT_LIST) {
            long id = db.insert(DbSchema.TBL_EVENTS, null, values);
            return getUriForId(id, uri);
        } else {
            // this insertWithOnConflict is a special case; CONFLICT_REPLACE
            // means that an existing entry which violates the UNIQUE constraint
            // on the item_id column gets deleted. That is this INSERT behaves
            // nearly like an UPDATE. Though the new row has a new primary key.
            // See how I mentioned this in the Contract class.
            long id = db.insertWithOnConflict(DbSchema.TBL_EVENTS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            return getUriForId(id, uri);
        }
    }


    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        Log.d(TAG, "update");

        SQLiteDatabase db = mHelper.getWritableDatabase();
        int updateCount = 0;
        switch (URI_MATCHER.match(uri)) {
            case EVENT_LIST:
                updateCount = db.update(DbSchema.TBL_EVENTS, values, selection,
                        selectionArgs);
                break;
            case EVENT_ID:
                String idStr = uri.getLastPathSegment();
                String where = EVENT_ID + " = " + idStr;
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }
                updateCount = db.update(DbSchema.TBL_EVENTS, values, where,
                        selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        // notify all listeners of changes
        if (updateCount > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return updateCount;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        Log.d(TAG, "delete");

        SQLiteDatabase db = mHelper.getWritableDatabase();
        int delCount = 0;
        switch (URI_MATCHER.match(uri)) {
            case EVENT_LIST:
                delCount = db.delete(DbSchema.TBL_EVENTS, selection, selectionArgs);
                break;
            case EVENT_ID:
                String idStr = uri.getLastPathSegment();
                String where = COL_EVENT_ID + " = " + idStr;
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }
                delCount = db.delete(DbSchema.TBL_EVENTS, where, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        // notify all listeners of changes
        if (delCount > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return delCount;
    }

    private Uri getUriForId(long id, Uri uri) {
        if (id > 0) {
            Uri itemUri = ContentUris.withAppendedId(uri, id);
            // notify all listeners of changes and return itemUri
            getContext().getContentResolver().notifyChange(itemUri, null);
            return itemUri;
        }
        // something went wrong
        throw new SQLException("Problem while inserting into uri: " + uri);
    }

}