package com.friendmatch_frontend.friendmatch.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.friendmatch_frontend.friendmatch.R;
import com.friendmatch_frontend.friendmatch.models.Hobby;

import java.util.ArrayList;


public class HobbyGridAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<Hobby> hobbyArrayList;

    public HobbyGridAdapter(Context context, ArrayList<Hobby> hobbyArrayList) {
        this.context = context;
        this.hobbyArrayList = hobbyArrayList;
    }

    @Override
    public int getCount() {
        return hobbyArrayList.size();
    }

    public Hobby getItem(int index) {
        return hobbyArrayList.get(index);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        TextView hobbyTextView;
        ImageView hobbyImageView;

        if (view == null) {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.single_hobby_item, viewGroup, false);

            hobbyTextView = (TextView) view.findViewById(R.id.hobbyText);
            hobbyImageView = (ImageView) view.findViewById(R.id.hobbyImage);

            String hobbyName = hobbyArrayList.get(position).getHobbyName();
            hobbyName = hobbyName.substring(0, 1).toUpperCase() + hobbyName.substring(1);
            hobbyTextView.setText(hobbyName);
            hobbyImageView.setImageResource(hobbyArrayList.get(position).getHobbyImg());
            hobbyImageView.setAdjustViewBounds(true);
        }

        return view;
    }

}