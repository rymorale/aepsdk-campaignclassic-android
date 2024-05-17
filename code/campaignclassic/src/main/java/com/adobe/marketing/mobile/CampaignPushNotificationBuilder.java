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

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.adobe.marketing.mobile.notificationbuilder.NotificationBuilder;
import com.adobe.marketing.mobile.notificationbuilder.NotificationConstructionFailedException;
import java.util.Map;

/**
 * Class for building push notifications.
 *
 * <p>The {@link #buildPushNotification(Map)} method in this class takes the message data
 * {@code Map} from the {@link com.google.firebase.messaging.RemoteMessage} and builds the notification. This class
 * is used internally by the {@link CampaignMessagingService} to build the push notification.
 */
class CampaignPushNotificationBuilder {

    /**
     * Builds a notification for the provided message data {@code Map}.
     *
     * @param messageData {@link Map<String, String>} retrieved from the received push notification
     * @return the notification
     */
    @NonNull
    static Notification buildPushNotification(final Map<String, String> messageData)
            throws IllegalArgumentException, NotificationConstructionFailedException {
        final NotificationCompat.Builder builder = NotificationBuilder.constructNotificationBuilder(messageData, CampaignPushTrackerActivity.class, AEPPushTemplateBroadcastReceiver.class);
        return builder.build();
    }
}
