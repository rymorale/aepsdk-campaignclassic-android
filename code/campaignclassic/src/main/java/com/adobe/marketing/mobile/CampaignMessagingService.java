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
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.adobe.marketing.mobile.services.Log;
import com.adobe.marketing.mobile.notificationbuilder.NotificationConstructionFailedException;
import com.adobe.marketing.mobile.util.StringUtils;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is the entry point for all Adobe Campaign Classic out-of-the-box push template
 * notifications received from Firebase.
 */
public class CampaignMessagingService {
    private static final String SELF_TAG = "CampaignMessagingService";
    private static String messageId;
    private static String deliveryId;

    private static final class NotificationPriority{
        static final String PRIORITY_DEFAULT = "PRIORITY_DEFAULT";
        static final String PRIORITY_MIN = "PRIORITY_MIN";
        static final String PRIORITY_LOW = "PRIORITY_LOW";
        static final String PRIORITY_HIGH = "PRIORITY_HIGH";
        static final String PRIORITY_MAX = "PRIORITY_MAX";
    }

    private static final class NotificationVisibility {
        static final String PUBLIC = "PUBLIC";
        static final String PRIVATE = "PRIVATE";
        static final String SECRET = "SECRET";
    }

    private static final Map<Integer, String> notificationCompatPriorityMap = new HashMap<Integer, String>() {{
        put(NotificationCompat.PRIORITY_MIN, NotificationPriority.PRIORITY_MIN);
        put(NotificationCompat.PRIORITY_LOW, NotificationPriority.PRIORITY_LOW);
        put(NotificationCompat.PRIORITY_DEFAULT, NotificationPriority.PRIORITY_DEFAULT);
        put(NotificationCompat.PRIORITY_HIGH, NotificationPriority.PRIORITY_HIGH);
        put(NotificationCompat.PRIORITY_MAX, NotificationPriority.PRIORITY_MAX);
    }};

    private static final Map<Integer, String> notificationCompatVisibilityMap = new HashMap<Integer, String>() {{
        put(NotificationCompat.VISIBILITY_PRIVATE, NotificationVisibility.PRIVATE);
        put(NotificationCompat.VISIBILITY_PUBLIC, NotificationVisibility.PUBLIC);
        put(NotificationCompat.VISIBILITY_SECRET, NotificationVisibility.SECRET);
    }};

    /**
     * Builds a {@link Notification} using the {@code RemoteMessage} data payload. The built notification is then passed to the {@link
     * NotificationManagerCompat} to be displayed. If any exceptions are thrown when building the {@code Notification},
     * this method will return false signaling that the remote message was not handled by the {@code CampaignMessagingService}.
     *
     * @param context       the application {@link Context}
     * @param remoteMessage the {@link RemoteMessage} containing a push notification payload
     * @return {@code boolean} signaling if the {@link CampaignMessagingService} handled the remote
     * message
     */
    public static boolean handleRemoteMessage(
            @NonNull final Context context, @NonNull final RemoteMessage remoteMessage) {
        final NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(context);
        Map<String, String> messageData;
        try {
            // validate the received message data
            messageData = remoteMessage.getData();
            if (messageData.isEmpty())
                throw new IllegalArgumentException("Received message data is empty.");
            messageId = messageData.get(CampaignPushConstants.Tracking.Keys.MESSAGE_ID);
            if (StringUtils.isNullOrEmpty(messageId))
                throw new IllegalArgumentException("Required field message id not found.");
            deliveryId = messageData.get(CampaignPushConstants.Tracking.Keys.DELIVERY_ID);
            if (StringUtils.isNullOrEmpty(deliveryId))
                throw new IllegalArgumentException("Required field delivery id not found.");

            // use the acc "_msg" value as the message body if it is present
            final String accMessageBody = messageData.get(CampaignPushConstants.PushPayloadKeys.ACC_BODY);
            if (!StringUtils.isNullOrEmpty(accMessageBody)) {
                messageData.put(CampaignPushConstants.PushPayloadKeys.BODY, accMessageBody);
            }

            // if we have a notification object, we can migrate the notification key value pairs to the message data if needed
            final RemoteMessage.Notification receivedNotification = remoteMessage.getNotification();
            if (receivedNotification != null) {
                final Map<String, String> messageDataCopy = new HashMap<>(messageData);
                messageData = convertNotificationPayloadData(receivedNotification, messageDataCopy);
            }

            // use the tag if present, otherwise use the message id as the tag
            final String tag = !StringUtils.isNullOrEmpty(messageData.get(CampaignPushConstants.PushPayloadKeys.TAG)) ? messageData.get(CampaignPushConstants.PushPayloadKeys.TAG) : messageId;
            messageData.put(CampaignPushConstants.PushPayloadKeys.TAG, tag);
            final Notification notification =
                    CampaignPushNotificationBuilder.buildPushNotification(messageData);
            notificationManager.notify(tag.hashCode(), notification);
        } catch (final IllegalArgumentException exception) {
            Log.error(
                    CampaignPushConstants.LOG_TAG,
                    SELF_TAG,
                    "Failed to create a push notification, an illegal argument exception occurred:"
                            + " %s",
                    exception.getLocalizedMessage());
            return false;
        } catch (final NotificationConstructionFailedException exception) {
            Log.error(
                    CampaignPushConstants.LOG_TAG,
                    SELF_TAG,
                    "Failed to create a push notification, a notification construction failed"
                            + " exception occurred: %s",
                    exception.getLocalizedMessage());
            return false;
        }

        // call track notification receive as we know that the push payload data is valid
        trackNotificationReceive(messageData);

        return true;
    }

    private static void trackNotificationReceive(final Map<String, String> messageData) {
        Log.trace(
                CampaignPushConstants.LOG_TAG,
                SELF_TAG,
                "Received push payload is valid, sending notification receive track request.");
        final Map<String, String> trackInfo =
                new HashMap<String, String>() {
                    {
                        put(CampaignPushConstants.Tracking.Keys.MESSAGE_ID, messageId);
                        put(CampaignPushConstants.Tracking.Keys.DELIVERY_ID, deliveryId);
                    }
                };
        CampaignClassic.trackNotificationReceive(trackInfo);
    }

    static String getMessageId() {
        return messageId;
    }

    static String getDeliveryId() {
        return deliveryId;
    }

    /**
     * Migrates any android.notification key value pairs to the equivalent adb prefixed keys.
     * Note, the key value pairs present in the data payload are preferred over the notification key value pairs.
     * The notification key value pairs will only be added to the message data if the corresponding key
     * does not have a value.
     * The following notification key value pairs are migrated:
     * message.android.notification.icon to adb_small_icon
     * message.android.notification.sound to adb_sound
     * message.android.notification.tag to adb_tag
     * message.android.notification.click_action to adb_uri
     * message.android.notification.channel_id to adb_channel_id
     * message.android.notification.ticker to adb_ticker
     * message.android.notification.sticky to adb_sticky
     * message.android.notification.visibility to adb_n_visibility
     * message.android.notification.notification_priority to adb_n_priority
     * message.android.notification.notification_count to adb_n_count
     * message.notification.body to adb_body
     * message.notification.title to adb_title
     * message.notification.image to adb_image
     *
     * @param notification {@link RemoteMessage.Notification} received from the {@link com.google.firebase.messaging.FirebaseMessagingService}
     * @param messageData  {@link Map} containing the {@link RemoteMessage} data payload
     * @return {@code Map} containing the data payload with optionally migrated key value pairs
     */
    private static Map<String, String> convertNotificationPayloadData(final RemoteMessage.Notification notification, final Map<String, String> messageData) {
        if (StringUtils.isNullOrEmpty(messageData.get(CampaignPushConstants.PushPayloadKeys.TAG))) {
            messageData.put(CampaignPushConstants.PushPayloadKeys.TAG, notification.getTag());
        }

        if (StringUtils.isNullOrEmpty(
                messageData.get(CampaignPushConstants.PushPayloadKeys.SMALL_ICON))) {
            messageData.put(
                    CampaignPushConstants.PushPayloadKeys.SMALL_ICON, notification.getIcon());
        }

        if (StringUtils.isNullOrEmpty(
                messageData.get(CampaignPushConstants.PushPayloadKeys.SOUND))) {
            messageData.put(CampaignPushConstants.PushPayloadKeys.SOUND, notification.getSound());
        }

        if (StringUtils.isNullOrEmpty(
                messageData.get(CampaignPushConstants.PushPayloadKeys.ACTION_URI))) {
            messageData.put(
                    CampaignPushConstants.PushPayloadKeys.ACTION_URI,
                    notification.getClickAction());
        }

        if (StringUtils.isNullOrEmpty(
                messageData.get(CampaignPushConstants.PushPayloadKeys.CHANNEL_ID))) {
            messageData.put(
                    CampaignPushConstants.PushPayloadKeys.CHANNEL_ID, notification.getChannelId());
        }

        if (StringUtils.isNullOrEmpty(
                messageData.get(CampaignPushConstants.PushPayloadKeys.TICKER))) {
            messageData.put(CampaignPushConstants.PushPayloadKeys.TICKER, notification.getTicker());
        }

        if (StringUtils.isNullOrEmpty(
                messageData.get(CampaignPushConstants.PushPayloadKeys.STICKY))) {
            messageData.put(
                    CampaignPushConstants.PushPayloadKeys.STICKY,
                    String.valueOf(notification.getSticky()));
        }

        if (StringUtils.isNullOrEmpty(
                messageData.get(CampaignPushConstants.PushPayloadKeys.NOTIFICATION_VISIBILITY))) {
            messageData.put(
                    CampaignPushConstants.PushPayloadKeys.NOTIFICATION_VISIBILITY,
                    notificationCompatVisibilityMap.get(notification.getVisibility()));
        }

        if (StringUtils.isNullOrEmpty(
                messageData.get(CampaignPushConstants.PushPayloadKeys.NOTIFICATION_PRIORITY))) {
            messageData.put(
                    CampaignPushConstants.PushPayloadKeys.NOTIFICATION_PRIORITY,
                    notificationCompatPriorityMap.get(notification.getNotificationPriority()));
        }

        if (StringUtils.isNullOrEmpty(
                messageData.get(CampaignPushConstants.PushPayloadKeys.BADGE_NUMBER))) {
            messageData.put(
                    CampaignPushConstants.PushPayloadKeys.BADGE_NUMBER,
                    String.valueOf(notification.getNotificationCount()));
        }

        if (StringUtils.isNullOrEmpty(
                messageData.get(CampaignPushConstants.PushPayloadKeys.BODY))) {
            messageData.put(
                    CampaignPushConstants.PushPayloadKeys.BODY,
                    String.valueOf(notification.getBody()));
        }

        if (StringUtils.isNullOrEmpty(
                messageData.get(CampaignPushConstants.PushPayloadKeys.TITLE))) {
            messageData.put(
                    CampaignPushConstants.PushPayloadKeys.TITLE,
                    String.valueOf(notification.getTitle()));
        }

        if (StringUtils.isNullOrEmpty(
                messageData.get(CampaignPushConstants.PushPayloadKeys.IMAGE_URL))) {
            messageData.put(
                    CampaignPushConstants.PushPayloadKeys.IMAGE_URL,
                    String.valueOf(notification.getImageUrl()));
        }

        return messageData;
    }
}
