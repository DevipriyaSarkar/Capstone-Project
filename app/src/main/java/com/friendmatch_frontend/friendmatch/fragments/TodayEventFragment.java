package com.friendmatch_frontend.friendmatch.fragments;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.friendmatch_frontend.friendmatch.R;
import com.friendmatch_frontend.friendmatch.adapters.TodayEventAdapter;

import static com.friendmatch_frontend.friendmatch.provider.EventsContract.Events.CONTENT_URI;
import static com.friendmatch_frontend.friendmatch.provider.EventsContract.Events.EVENT_NAME;

public class TodayEventFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private final String TAG = getClass().getSimpleName();
    RecyclerView eventList;
    TodayEventAdapter todayEventAdapter;
    View todayEventView;
    LinearLayout eventLayout;
    TextView eventError;
    TextView eventSectionHeading;

    public TodayEventFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_today_event, container, false);

        todayEventView = view.findViewById(R.id.todayEventView);
        eventLayout = (LinearLayout) todayEventView.findViewById(R.id.eventLayout);
        eventError = (TextView) todayEventView.findViewById(R.id.eventError);
        eventSectionHeading = (TextView) todayEventView.findViewById(R.id.eventSectionHeading);
        eventSectionHeading.setText(R.string.today_event_heading);

        eventList = (RecyclerView) todayEventView.findViewById(R.id.eventList);
        LinearLayoutManager manager = new LinearLayoutManager(todayEventView.getContext());
        eventList.setHasFixedSize(true);
        eventList.setLayoutManager(manager);

        getLoaderManager().initLoader(0, null, this);

        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        // retrieve today's events
        Uri eventsUri = CONTENT_URI;
        CursorLoader cursorLoader = new CursorLoader(getContext(), eventsUri, null, null, null, EVENT_NAME);
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.getCount() == 0) {
            eventLayout.setVisibility(View.GONE);
            eventError.setVisibility(View.VISIBLE);
            eventError.setText(R.string.no_event_today_message);
        } else {
            todayEventAdapter = new TodayEventAdapter(getContext(), data);
            eventList.setAdapter(todayEventAdapter);
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {

    }
}
