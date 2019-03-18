package com.bubbleinc.bubble;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import java.util.Random;

public class App extends Application
{
    public static final String CHANNEL_1_ID = "Bubble Message Notifications";
    public static final String CHANNEL_2_ID = "Vicinity Message Notifications";

    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannels();
    }

    private void createNotificationChannels()
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            NotificationChannel channel1 = new NotificationChannel(
                    CHANNEL_1_ID,
                    "Bubble Chat Messages",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel1.setDescription("These are messages your Bubble receives when you're not in the app.");

            NotificationChannel channel2 = new NotificationChannel(
                    CHANNEL_2_ID,
                    "Vicinity Chat Messages",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel2.setDescription("These are messages sent to your vicinity.");

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel1);
            manager.createNotificationChannel(channel2);
        }
    }
}
