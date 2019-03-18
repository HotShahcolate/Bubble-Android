package com.bubbleinc.bubble;


import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bubbleinc.bubble.Adapters.RecyclerViewAdapter2;
import com.bubbleinc.bubble.Models.bubbleMessageNotification;

import java.util.ArrayList;

import static com.bubbleinc.bubble.App.CHANNEL_2_ID;

public class NotificationsFragment extends Fragment {

    private ArrayList<bubbleMessageNotification> mBubbleMessageNotifications;
    private Bundle bubbleMessageNotificationBundle;
    private RecyclerViewAdapter2 listOfBubbleMessageNotificationsAdapter;
    private RecyclerView listOfBubbleMessageNotifications;
    private NotificationsListener listener;
    private NotificationManagerCompat notificationManager;


//                            bubbleMessageListAdapter.notifyItemInserted(mBubbleMessages.size());
//                            bubbleMessagesList.scrollToPosition(mBubbleMessages.size() - 1);

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState){
        notificationManager = NotificationManagerCompat.from(getContext());
        mBubbleMessageNotifications = new ArrayList<>();

        bubbleMessageNotificationBundle = this.getArguments();




        View view = inflater.inflate(R.layout.fragment_notifications, container, false);
        listOfBubbleMessageNotifications = (RecyclerView) view.findViewById(R.id.list_of_notifications);

        listOfBubbleMessageNotifications.setHasFixedSize(true);

        listOfBubbleMessageNotificationsAdapter = new RecyclerViewAdapter2(mBubbleMessageNotifications);
        listOfBubbleMessageNotifications.setAdapter(listOfBubbleMessageNotificationsAdapter);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        ((LinearLayoutManager) layoutManager).setReverseLayout(true);
        ((LinearLayoutManager) layoutManager).setStackFromEnd(true);
        listOfBubbleMessageNotifications.setLayoutManager(layoutManager);

        listOfBubbleMessageNotificationsAdapter.setOnItemClickListener(new RecyclerViewAdapter2.onItemClickListener() {
            @Override
            public void onItemClick(int position) {
                listener.onDataPass(mBubbleMessageNotifications.get(position).getBubbleMessageLat(), mBubbleMessageNotifications.get(position).getBubbleMessageLong());
                mBubbleMessageNotifications.remove(position);
                listOfBubbleMessageNotificationsAdapter.notifyItemRemoved(position);
            }

            @Override
            public void onDeleteClick(int position) {
                mBubbleMessageNotifications.remove(position);
                listOfBubbleMessageNotificationsAdapter.notifyItemRemoved(position);
            }
        });


        return view;

    }

//    public void deleteBubbleMessageFromNotifications()
//    {
//        if(bubbleMessageNotificationBundle != null)
//        {
//            for (bubbleMessageNotification i : mBubbleMessageNotifications) {
//                if (i.getKey().equals(bubbleMessageNotificationBundle.getString("keyToRemove"))) {
//                    mBubbleMessageNotifications.remove(i);
//                    listOfBubbleMessageNotificationsAdapter.notifyItemRemoved(mBubbleMessageNotifications.indexOf(i));
//                    break;
//                }
//            }
//        }
//    }

    // pass data to main, do everything needed in main, delete notification clicked on, and other notifications if applicable
    public interface NotificationsListener {
        public void onDataPass(Double lat, Double lng);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof NotificationsListener)
        {
            listener = (NotificationsListener) context;
        }else
        {
            throw new RuntimeException(context.toString()
            + " must implement NotificationListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    public void sendToChannel2() {
        Intent openApp = new Intent(getContext(), MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(getContext(),
                0, openApp, 0);

        Notification notification = new NotificationCompat.Builder(getContext(), CHANNEL_2_ID)
                .setContentTitle("A Bubble message has been sent to your vicinity!")
                .setContentText("Tap here to chat with this Bubble.")
                .setSmallIcon(R.drawable.ic_bubble_chart_black_24dp)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .build();

        notificationManager.notify(2, notification);
    }

    public void updateBubbleMessageNotificationsList()
    {
        if(bubbleMessageNotificationBundle != null)
        {
            mBubbleMessageNotifications.add(new bubbleMessageNotification(bubbleMessageNotificationBundle.getString("key"),
                    bubbleMessageNotificationBundle.getString("name"), bubbleMessageNotificationBundle.getString("message"),
                    bubbleMessageNotificationBundle.getDouble("lat"), bubbleMessageNotificationBundle.getDouble("long")));
            listOfBubbleMessageNotificationsAdapter.notifyItemInserted(mBubbleMessageNotifications.size());
            listOfBubbleMessageNotifications.scrollToPosition(mBubbleMessageNotifications.size() - 1);
            sendToChannel2();
//            final Handler handler = new Handler();
//            handler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    sendToChannel2();
//                }
//            }, 5000);
        }
    }
}
