package com.friendmatch_frontend.friendmatch.provider;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

import static com.friendmatch_frontend.friendmatch.provider.DbSchema.COL_CITY;
import static com.friendmatch_frontend.friendmatch.provider.DbSchema.COL_DATE;
import static com.friendmatch_frontend.friendmatch.provider.DbSchema.COL_EVENT_ID;
import static com.friendmatch_frontend.friendmatch.provider.DbSchema.COL_EVENT_NAME;
import static com.friendmatch_frontend.friendmatch.provider.DbSchema.DEFAULT_TBL_EVENT_SORT_ORDER;
import static com.friendmatch_frontend.friendmatch.provider.DbSchema.DML_WHERE_EVENT_ID_CLAUSE;

// The contract between clients and the events content provider.

public final class EventsContract {

    // The authority of the events provider.
    static final String AUTHORITY = "com.friendmatch_frontend.friendmatch";

    // The content URI for the top-level events authority.
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    // A selection clause for ID based queries.
    public static final String SELECTION_ID_BASED = DML_WHERE_EVENT_ID_CLAUSE;

    // Constants for the Items table of the events provider.
    public static final class Events implements BaseColumns {

        // The content URI for this table.
        public static final Uri CONTENT_URI = Uri.withAppendedPath(EventsContract.CONTENT_URI, "events");

        // The mime type of a directory of items.
        static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/com.friendmatch_frontend.friendmatch_events";

        // The mime type of a single item.
        static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/com.friendmatch_frontend.friendmatch_events";

        public static final String EVENT_ID = COL_EVENT_ID;
        public static final String EVENT_NAME = COL_EVENT_NAME;
        public static final String CITY = COL_CITY;
        public static final String DATE = COL_DATE;

        // A projection of all columns in the items table.
        public static final String[] PROJECTION_ALL = {EVENT_ID, EVENT_NAME, CITY, DATE};

        // The default sort order for queries containing EVENT_NAME fields.
        public static final String SORT_ORDER_DEFAULT = DEFAULT_TBL_EVENT_SORT_ORDER;
    }
}