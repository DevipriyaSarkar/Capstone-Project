package com.friendmatch_frontend.friendmatch.adapters;

import android.content.Context;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.friendmatch_frontend.friendmatch.R;
import com.friendmatch_frontend.friendmatch.utilities.DateHelper;

import static com.friendmatch_frontend.friendmatch.provider.DbSchema.COL_CITY;
import static com.friendmatch_frontend.friendmatch.provider.DbSchema.COL_DATE;
import static com.friendmatch_frontend.friendmatch.provider.DbSchema.COL_EVENT_NAME;

public class TodayEventCursorAdapter extends RecyclerView.Adapter<TodayEventCursorAdapter.MyViewHolder> {

    private Context context;
    private Cursor cursor;
    private DataSetObserver dataSetObserver;

    public TodayEventCursorAdapter(Context context, Cursor cursor) {
        this.context = context;
        this.cursor = cursor;
        this.dataSetObserver = new NotifyingDataSetObserver();
        cursor.registerDataSetObserver(dataSetObserver);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_event_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        cursor.moveToPosition(position);
        holder.eventName.setText(cursor.getString(cursor.getColumnIndex(COL_EVENT_NAME)));
        holder.eventCity.setText(cursor.getString(cursor.getColumnIndex(COL_CITY)));
        holder.eventDate.setText((new DateHelper(cursor.getString(cursor.getColumnIndex(COL_DATE)))).changeDateFormatLong());
        holder.eventImage.setImageResource(R.drawable.event);
    }

    @Override
    public int getItemCount() {
        return cursor.getCount();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView eventName, eventDate, eventCity;
        private ImageView eventImage;

        MyViewHolder(View itemView) {
            super(itemView);
            eventName = (TextView) itemView.findViewById(R.id.eventName);
            eventDate = (TextView) itemView.findViewById(R.id.eventDate);
            eventCity = (TextView) itemView.findViewById(R.id.eventCity);
            eventImage = (ImageView) itemView.findViewById(R.id.eventImage);
        }
    }

    public Cursor swapCursor(Cursor newCursor){
        if (newCursor == cursor){
            return null;
        }
        final Cursor oldCursor = cursor;
        if (oldCursor != null && dataSetObserver != null){
            oldCursor.unregisterDataSetObserver(dataSetObserver);
        }
        cursor = newCursor;
        if (cursor != null){
            if (dataSetObserver != null){
                cursor.registerDataSetObserver(dataSetObserver);
            }
            notifyDataSetChanged();
        } else{
            notifyDataSetChanged();
        }
        return oldCursor;
    }

    private class NotifyingDataSetObserver extends DataSetObserver{
        @Override public void onChanged() {
            super.onChanged();
            notifyDataSetChanged();
        }

        @Override public void onInvalidated() {
            super.onInvalidated();
            notifyDataSetChanged();
        }
    }
}