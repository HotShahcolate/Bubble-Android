package com.bubbleinc.bubble.Adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bubbleinc.bubble.Models.bubbleMessageNotification;
import com.bubbleinc.bubble.R;

import java.util.ArrayList;
import java.util.List;

//An adapter "adapts" the data for the recyclerview. It is the bridge between the two. I would watch a YouTube video on RecyclerViews if you're confused.
public class RecyclerViewAdapter2 extends RecyclerView.Adapter<RecyclerViewAdapter2.ViewHolder>{
    private static final String TAG = "RecyclerViewAdapter";

    private ArrayList<bubbleMessageNotification> mBubbleMessageNotificationsList;
    private onItemClickListener mListener;

    public interface onItemClickListener
    {
        void onItemClick(int position);
        void onDeleteClick(int position);
    }

    public void setOnItemClickListener(onItemClickListener listener)
    {
        mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.bubble_notification_layout, parent, false);
        ViewHolder vh = new ViewHolder(v, mListener);
        return vh;
    }

    public RecyclerViewAdapter2(ArrayList<bubbleMessageNotification> bubbleMessageNotificationsList)
    {
        mBubbleMessageNotificationsList = bubbleMessageNotificationsList;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position)
    {
        bubbleMessageNotification currentBubbleMessageNotification = mBubbleMessageNotificationsList.get(position);

        holder.mBubbleMessageNotificationName.setText(currentBubbleMessageNotification.getName());
        //holder.recyclerviewBubbleMessageTimestamp.put(currentBubbleMessage.getTimestamp());
        holder.mBubbleMessageNotificationMessage.setText(currentBubbleMessageNotification.getMessage());
    }

    @Override
    public int getItemCount() {
        return mBubbleMessageNotificationsList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        ImageView mBubbleMessageNotificationImage;
        TextView mBubbleMessageNotificationTitle;
        TextView mBubbleMessageNotificationName;
        TextView mBubbleMessageNotificationMessage;
        TextView mBubbleMessageNotificationTapHere;
        ImageButton mBubbleMessageNotificationDelete;

        public ViewHolder(View itemView, final onItemClickListener listener) {
            super(itemView);
            mBubbleMessageNotificationImage = itemView.findViewById(R.id.bubble_message_notification_image);
            mBubbleMessageNotificationTitle = itemView.findViewById(R.id.bubble_message_notification_title);
            mBubbleMessageNotificationName = itemView.findViewById(R.id.bubble_message_notification_name);
            mBubbleMessageNotificationMessage = itemView.findViewById(R.id.bubble_message_notification_message);
            mBubbleMessageNotificationTapHere = itemView.findViewById(R.id.bubble_message_notification_tap_here);
            mBubbleMessageNotificationDelete = itemView.findViewById(R.id.bubble_message_notification_delete);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener != null)
                    {
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION)
                        {
                            listener.onItemClick(position);
                        }
                    }
                }
            });

            mBubbleMessageNotificationDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener != null)
                    {
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION)
                        {
                            listener.onDeleteClick(position);
                        }
                    }
                }
            });
        }
    }
}
