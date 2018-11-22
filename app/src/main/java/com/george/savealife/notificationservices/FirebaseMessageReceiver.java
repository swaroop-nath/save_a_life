package com.george.savealife.notificationservices;
;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FirebaseMessageReceiver extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        /* TODO: Handle FCM messages here.
                 If the application is in the foreground handle both data and notification messages here.
                 Also if you intend on generating your own notifications as a result of a received FCM
                 message, here is where that should be initiated. */

        Log.e("Message Received", remoteMessage.getNotification().getBody().toString());
    }
}
