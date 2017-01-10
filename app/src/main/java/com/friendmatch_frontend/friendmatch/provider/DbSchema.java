package com.friendmatch_frontend.friendmatch.provider;

//A helper interface which defines constants for work with the DB.

interface DbSchema {

    String DB_NAME = "friend_match_events.db";

    String TBL_EVENTS = "events";

    String COL_EVENT_ID = "event_id";
    String COL_EVENT_NAME = "event_name";
    String COL_CITY = "event_city";
    String COL_DATE = "event_date";

    String DDL_CREATE_TBL_EVENTS =
            "CREATE TABLE events (" +
                    "event_id       INTEGER PRIMARY KEY, \n" +
                    "event_name     TEXT, \n" +
                    "event_city     TEXT, \n" +
                    "event_date     TEXT \n" +
                    ")";

    String DDL_CREATE_TRIGGER_DELETE_EVENT =
            "CREATE TRIGGER delete_event DELETE ON events \n"
                    + "begin\n"
                    + "  delete from event where event_id = old.event_id;\n"
                    + "end\n";

    String DDL_DROP_TBL_EVENT =
            "DROP TABLE IF EXISTS events";

    String DDL_DROP_TRIGGER_DELETE_EVENT =
            "DROP TRIGGER IF EXISTS delete_event";

    String DML_WHERE_EVENT_ID_CLAUSE = "event_id = ? ";

    String DEFAULT_TBL_EVENT_SORT_ORDER = "event_name ASC";
}