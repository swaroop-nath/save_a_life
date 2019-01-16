package com.george.savealife.notificationflow;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.george.savealife.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MessageReception extends FirebaseMessagingService {
    private String notificationBody, notificationTitle;
    private String CHANNEL_ID = "SOME_ID";
    private int notificationID = 007;
    private Manager applicationManager = Manager.getInstance();
    public MessageReception() {
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // Check if message contains a data payload.
        String UIDhelp;
        int flag = -1; //0- message for donor, 1- message for help seeker.
        if (remoteMessage.getData().size() > 0) {
            //Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            /*
            Data contents -
            1. UID of the help seeker, for making an appropriate realtime database ref, which shall be used to store the list of donors.
            2. Flag whether the message is for donor or help seeker.
             */
            if (/* Check if data needs to be processed by long running job */ true) {
                // For long-running tasks (10 seconds or more) use Firebase Job Dispatcher.
                //scheduleJob();
            } else {
                // Handle message within 10 seconds
                //handleNow();
            }

        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            //Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            notificationTitle = remoteMessage.getNotification().getTitle();
            notificationBody = remoteMessage.getNotification().getBody();
            sendNotification(notificationTitle, notificationBody, flag);
        }
    }

    private void sendNotification(String notificationTitle, String notificationBody, int flag) {
        //Send Notification to the System Tray
        String snippet = "";
        Context context = null;
        Intent activityToBeStarted = null;
        if (flag == 0) {
            // Make an intent for the activity related to donor

            //In case of the expandable notification, set a snippet for the condensed notification
            snippet = "Help needed!";
        }
        else if (flag == 1) {
            //Make an intent for the activity related to help seeker

            //In case of the expandable notification, set a snippet for the condensed notification
            snippet = "Donor Found!";
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(applicationManager.getContext(),CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(notificationTitle)
                .setContentText(snippet)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(notificationBody))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        PendingIntent pendingIntent = PendingIntent.getActivity(applicationManager.getContext(), 0, activityToBeStarted, 0);

        builder.setContentIntent(pendingIntent);

        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(applicationManager.getContext());
        applicationManager.setNotificationID(notificationID);
        managerCompat.notify(notificationID, builder.build());
    }

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
    }

}
