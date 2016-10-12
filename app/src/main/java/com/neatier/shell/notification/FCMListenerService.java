/**
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.neatier.shell.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.TextUtils;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.neatier.commons.helpers.KeyValuePairs;
import com.neatier.shell.R;
import com.neatier.shell.activities.MainActivity;
import com.neatier.shell.activities.NotificationActivity;
import trikita.log.Log;

public class FCMListenerService extends FirebaseMessagingService {

    private static final String TAG = "FCMListenerService";

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // TODO(developer): Handle FCM messages here.
        // If the application is in the foreground handle both data and notification messages here.
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
        Log.d("From: " + remoteMessage.getFrom())
           .d("Remote Message: " + remoteMessage)
           .d("Notification: " + remoteMessage.getNotification())
           .d("Data: " + remoteMessage.getData());
        sendNotification(remoteMessage);
    }

    /**
     * Create and show a simple notification containing the received FCM remoteMessage.
     *
     * @param remoteMessage FCM remoteMessage body received.
     */
    private void sendNotification(RemoteMessage remoteMessage) {
        RemoteMessage.Notification noti = remoteMessage.getNotification();
        KeyValuePairs<String, String> data = new KeyValuePairs<>();

        String title = getString(R.string.app_name);
        String message = getString(R.string.fcm_message_default);
        StringBuffer contentText = new StringBuffer();
        if (noti != null) {
            title = TextUtils.isEmpty(noti.getTitle())
                    ? getString(R.string.app_name)
                    : noti.getTitle();
            contentText.append(TextUtils.isEmpty(noti.getBody())
                               ? getString(R.string.fcm_message_default)
                               : noti.getBody()
            );
        }
        if (remoteMessage.getData() != null) {
            data.putAll(remoteMessage.getData());
            title = data.getOrDefault("title", getString(R.string.app_name));
            contentText.append(
                  data.getOrDefault("message", getString(R.string.fcm_message_default))
            );
        }
        Context context = this;
        Intent intent = new Intent(this, NotificationActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        int requestCode = (int) (System.currentTimeMillis() & 0xfffffff);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        // Adds the back stack
        //stackBuilder.addParentStack(NotificationActivity.class);
        stackBuilder.addNextIntentWithParentStack(new Intent(context, MainActivity.class));
        stackBuilder.addNextIntent(intent);
        // Adds the Intent to the top of the stack
        //stackBuilder.addNextIntent(intent);
        // Gets a PendingIntent containing the entire back stack
        //Need to set PendingIntent.FLAG_UPDATE_CURRENT to work with the starting intent.
        //See issue: http://goo.gl/lJrMNg
        PendingIntent resultPendingIntent =
              PendingIntent.getActivity(this, requestCode, intent,
                                        PendingIntent.FLAG_UPDATE_CURRENT);
        //PendingIntent resultPendingIntent =
        //      stackBuilder.getPendingIntent(requestCode, PendingIntent.FLAG_UPDATE_CURRENT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
              .setSmallIcon(R.drawable.ic_neat_logo)
              .setContentTitle(title)
              .setContentText(contentText.toString())
              .setAutoCancel(true)
              .setSound(defaultSoundUri)
              .setContentIntent(resultPendingIntent)
              .setDefaults(Notification.DEFAULT_VIBRATE);
        NotificationManager notificationManager =
              (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(requestCode, notificationBuilder.build());
    }
}
