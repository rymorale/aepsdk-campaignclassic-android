/*
  Copyright 2023 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
 */

package com.adobe.marketing.mobile;

import android.app.Notification;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is the entry point for all push notifications received from Firebase.
 * <p>
 * Once the MessagingService is registered in the AndroidManifest.xml.
 * This class will automatically handle display and tracking of Adobe Journey Optimizer push notifications.
 */
public class AEPMessagingService extends FirebaseMessagingService {
    final static List<Integer> hashedMessageIds = new ArrayList<>();

    @Override
    public void onNewToken(final @NonNull String token) {
        super.onNewToken(token);
        MobileCore.setPushIdentifier(token);
    }

    @Override
    public void onMessageReceived(final @NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        handleRemoteMessage(this, remoteMessage);
    }

    public static boolean handleRemoteMessage(final @NonNull Context context, final @NonNull RemoteMessage remoteMessage) {
        final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        final AEPPushPayload payload = new AEPPushPayload(remoteMessage);
        final PushTemplate pushTemplate = payload.getPushTemplate();
        Notification notification;

        switch (pushTemplate) {
            case BASIC:
                notification = AEPPushNotificationBuilder.createBasicTemplatePushNotification(payload, context);
                break;
//                case AUTO_CAROUSEL:
//                    createAutoCarouselPushNotification(payload, notificationManager);
//                    break;
//                case MANUAL_CAROUSEL:
//                    createManualCarouselPushNotification(payload, notificationManager);
//                    break;
//                case INPUT_BOX:
//                    createInputBoxPushNotification(payload, notificationManager);
//                    break;
            default:
                notification = AEPPushNotificationBuilder.build(payload, context);
        }

        // display notification
        final int hashedMessageId = remoteMessage.getMessageId().hashCode();
        hashedMessageIds.add(hashedMessageId);
        notificationManager.notify(hashedMessageId, notification);
        return true;
    }
}