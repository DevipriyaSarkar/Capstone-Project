package com.friendmatch_frontend.friendmatch.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.friendmatch_frontend.friendmatch.R;
import com.friendmatch_frontend.friendmatch.models.Hobby;

import java.util.ArrayList;

public class SelectableHobbyAdapter extends RecyclerView.Adapter<SelectableHobbyAdapter.MyViewHolder> {

    private Context context;
    private ArrayList<Hobby> selectableHobbyList;

    public SelectableHobbyAdapter(Context context, ArrayList<Hobby> modelList) {
        this.context = context;
        this.selectableHobbyList = modelList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_hobby_list_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        final Hobby hobby = selectableHobbyList.get(position);
        String hobbyName = hobby.getHobbyName();
        hobbyName = hobbyName.substring(0, 1).toUpperCase() + hobbyName.substring(1);
        holder.hobbyRowText.setText(hobbyName);
        holder.rowView.setBackgroundColor(hobby.isHobby()
                ? ContextCompat.getColor(context, R.color.colorAccent) : Color.TRANSPARENT);
        holder.hobbyRowText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hobby.setHobby(!hobby.isHobby());
                holder.rowView.setBackgroundColor(hobby.isHobby()
                        ? ContextCompat.getColor(context, R.color.colorAccent) : Color.TRANSPARENT);
            }
        });
    }

    @Override
    public int getItemCount() {
        return selectableHobbyList == null ? 0 : selectableHobbyList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        private View rowView;
        private TextView hobbyRowText;

        MyViewHolder(View itemView) {
            super(itemView);
            rowView = itemView;
            hobbyRowText = (TextView) itemView.findViewById(R.id.singleHobbyRowText);
        }
    }
}