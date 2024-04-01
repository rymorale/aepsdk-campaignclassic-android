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

import android.app.Activity;
import android.app.Notification;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.adobe.marketing.mobile.services.ServiceProvider;
import com.adobe.marketing.mobile.services.ui.pushtemplate.AEPPushPayload;
import com.adobe.marketing.mobile.services.ui.pushtemplate.BasicPushTemplate;
import com.adobe.marketing.mobile.services.ui.pushtemplate.CarouselPushTemplate;
import com.adobe.marketing.mobile.services.ui.pushtemplate.NotificationConstructionFailedException;
import com.adobe.marketing.mobile.services.ui.pushtemplate.PushTemplateType;
import com.adobe.marketing.mobile.services.ui.pushtemplate.TemplateUtils;

import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * Class for building push notifications.
 *
 * <p>The {@link #buildPushNotification(AEPPushPayload, Context)} method in this class takes the
 * {@link AEPPushPayload} created from the push notification and builds the notification. This class
 * is used internally by the {@link CampaignMessagingService} to build the push notification.
 */
class CampaignPushNotificationBuilder {
    public static CampaignPushTrackerActivity trackerActivity;
    public static AEPPushTemplateBroadcastReceiver broadcastReceiver;

    /**
     * Builds a notification for the provided {@code AEPPushPayload}.
     *
     * @param payload {@link AEPPushPayload} created from the received push notification
     * @param context the application {@link Context}
     * @return the notification
     */
    @NonNull static Notification buildPushNotification(final AEPPushPayload payload, final Context context)
            throws IllegalArgumentException, NotificationConstructionFailedException {
        // initialize tracker activity and broadcast receiver
        final Activity currentActivity = ServiceProvider.getInstance().getAppContextService().getCurrentActivity();
        final CountDownLatch latch = new CountDownLatch(1);
        if (currentActivity != null) {
            currentActivity.runOnUiThread(() -> {
                trackerActivity = new CampaignPushTrackerActivity();
                broadcastReceiver = new AEPPushTemplateBroadcastReceiver();
                latch.countDown();
            });
        }
        try {
            latch.await(1000, java.util.concurrent.TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        NotificationCompat.Builder builder;
        final Map<String, String> messageData = payload.getMessageData();
        final PushTemplateType pushTemplateType =
                messageData.get(CampaignPushConstants.PushPayloadKeys.TEMPLATE_TYPE) == null
                        ? PushTemplateType.UNKNOWN
                        : PushTemplateType.fromString(
                                messageData.get(
                                        CampaignPushConstants.PushPayloadKeys.TEMPLATE_TYPE));
        switch (pushTemplateType) {
            case CAROUSEL:
                final CarouselPushTemplate carouselPushTemplate =
                        new CarouselPushTemplate(messageData);
                builder =
                        TemplateUtils.constructNotificationBuilder(context, trackerActivity, broadcastReceiver, carouselPushTemplate, pushTemplateType);
                break;
            case UNKNOWN:
            default:
                final BasicPushTemplate legacyPushTemplate = new BasicPushTemplate(messageData);
                builder = TemplateUtils.constructNotificationBuilder(context, trackerActivity, broadcastReceiver, legacyPushTemplate, pushTemplateType);
                break;
        }

        return builder.build();
    }
}
