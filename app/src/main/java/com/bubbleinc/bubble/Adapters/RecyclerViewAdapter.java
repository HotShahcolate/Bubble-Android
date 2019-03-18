package com.bubbleinc.bubble.Adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bubbleinc.bubble.Models.bubbleMessage;
import com.bubbleinc.bubble.R;

import java.util.ArrayList;
import java.util.List;

//An adapter "adapts" the data for the recyclerview. It is the bridge between the two. I would watch a YouTube video on RecyclerViews if you're confused.
public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>{
    private static final String TAG = "RecyclerViewAdapter";

    private ArrayList<bubbleMessage> mBubbleMessageList;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.bubble_message_layout, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    public RecyclerViewAdapter(ArrayList<bubbleMessage> bubbleMessageList)
    {
        mBubbleMessageList = bubbleMessageList;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position)
    {
        bubbleMessage currentBubbleMessage = mBubbleMessageList.get(position);

        holder.recyclerviewBubbleMessageName.setText(currentBubbleMessage.getName());
        //holder.recyclerviewBubbleMessageTimestamp.put(currentBubbleMessage.getTimestamp());
        holder.recyclerviewBubbleMessage.setText(currentBubbleMessage.getMessage());
    }

    @Override
    public int getItemCount() {
        return mBubbleMessageList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        TextView recyclerviewBubbleMessageName;
        TextView recyclerviewBubbleMessage;
        public ViewHolder(View itemView) {
            super(itemView);
            recyclerviewBubbleMessageName = itemView.findViewById(R.id.recyclerview_name);
            recyclerviewBubbleMessage = itemView.findViewById(R.id.recyclerview_message);
        }
    }
}
