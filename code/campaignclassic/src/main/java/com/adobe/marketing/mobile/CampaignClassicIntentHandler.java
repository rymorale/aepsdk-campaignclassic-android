package com.adobe.marketing.mobile;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationManagerCompat;

import com.adobe.marketing.mobile.services.Log;
import com.adobe.marketing.mobile.services.ui.pushtemplate.NotificationConstructionFailedException;
import com.adobe.marketing.mobile.services.ui.pushtemplate.PushTemplateType;
import com.adobe.marketing.mobile.services.ui.pushtemplate.TemplateUtils;
import com.adobe.marketing.mobile.util.StringUtils;

import java.util.Calendar;

class CampaignClassicIntentHandler {
    static final String SELF_TAG = "CampaignClassicIntentHandler";

    static void handleCarouselArrowClickedIntent(final Context context, final Intent intent) {
        final Bundle intentExtras = intent.getExtras();
        if (intentExtras == null) {
            Log.trace(
                    CampaignPushConstants.LOG_TAG,
                    SELF_TAG,
                    "Intent extras are null, will not handle the manual filmstrip intent with"
                            + " action %s",
                    intent.getAction());
            return;
        }

        try {
            final NotificationManagerCompat notificationManager =
                    NotificationManagerCompat.from(context);
            final Notification notification = TemplateUtils.constructNotificationBuilder(context, CampaignPushNotificationBuilder.trackerActivity, CampaignPushNotificationBuilder.broadcastReceiver, intent, PushTemplateType.CAROUSEL).build();

            // get the tag from the intent extras. if no tag was present in the payload use the
            // message id instead as its guaranteed to always be present.
            final String tag =
                    !StringUtils.isNullOrEmpty(
                            intentExtras.getString(CampaignPushConstants.IntentKeys.TAG))
                            ? intentExtras.getString(CampaignPushConstants.IntentKeys.TAG)
                            : intentExtras.getString(CampaignPushConstants.IntentKeys.MESSAGE_ID);
            notificationManager.notify(tag.hashCode(), notification);
        } catch (final NotificationConstructionFailedException exception) {
            Log.error(
                    CampaignPushConstants.LOG_TAG,
                    SELF_TAG,
                    "Failed to create a push notification, a notification construction failed"
                            + " exception occurred: %s",
                    exception.getLocalizedMessage());
        }
    }

    static void handleScheduledIntent(final Context context, final Intent intent) {
        final Bundle intentExtras = intent.getExtras();
        if (intentExtras == null) {
            Log.trace(
                    CampaignPushConstants.LOG_TAG,
                    SELF_TAG,
                    "Intent extras are null, will not handle the scheduled intent with action %s",
                    intent.getAction());
            return;
        }

        final NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(context);
        try {
            final Notification notification = TemplateUtils.constructNotificationBuilder(context, CampaignPushNotificationBuilder.trackerActivity, CampaignPushNotificationBuilder.broadcastReceiver, intent, PushTemplateType.BASIC).build();

            // get the tag from the intent extras. if no tag was present in the payload use the
            // message id instead as its guaranteed to always be present.
            final String tag =
                    !StringUtils.isNullOrEmpty(
                            intentExtras.getString(CampaignPushConstants.IntentKeys.TAG))
                            ? intentExtras.getString(CampaignPushConstants.IntentKeys.TAG)
                            : intentExtras.getString(CampaignPushConstants.IntentKeys.MESSAGE_ID);
            notificationManager.notify(tag.hashCode(), notification);
        } catch (final NotificationConstructionFailedException exception) {
            Log.error(
                    CampaignPushConstants.LOG_TAG,
                    SELF_TAG,
                    "Failed to create a push notification, a notification construction failed"
                            + " exception occurred: %s",
                    exception.getLocalizedMessage());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    static void handleRemindIntent(final Context context, final Intent intent) {
        // get basic notification values from the intent extras
        final Bundle intentExtras = intent.getExtras();
        if (intentExtras == null) {
            Log.trace(
                    CampaignPushConstants.LOG_TAG,
                    SELF_TAG,
                    "Intent extras are null, will not schedule a notification from the received"
                            + " intent with action %s",
                    intent.getAction());
            return;
        }

        // set the calender time to the remind timestamp to allow the notification to be displayed
        // at the later time
        final long remindLaterTimestamp =
                intentExtras.getLong(CampaignPushConstants.IntentKeys.REMIND_TS);
        final Calendar calendar = Calendar.getInstance();
        final NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(context);

        // get the tag from the intent extras. if no tag was present in the payload use the message
        // id instead as its guaranteed to always be present.
        final String tag =
                !StringUtils.isNullOrEmpty(
                        intentExtras.getString(CampaignPushConstants.IntentKeys.TAG))
                        ? intentExtras.getString(CampaignPushConstants.IntentKeys.TAG)
                        : intentExtras.getString(CampaignPushConstants.IntentKeys.MESSAGE_ID);

        if (remindLaterTimestamp > 0) {
            // calculate difference in fire date. if fire date is greater than 0 then we want to
            // schedule a reminder notification.
            final long secondsUntilFireDate =
                    remindLaterTimestamp - calendar.getTimeInMillis() / 1000;
            if (secondsUntilFireDate <= 0) {
                Log.trace(
                        CampaignPushConstants.LOG_TAG,
                        SELF_TAG,
                        "Remind later date is before the current date. Will not reschedule the"
                                + " notification.",
                        secondsUntilFireDate);
                // cancel the displayed notification
                notificationManager.cancel(tag.hashCode());
                return;
            }

            Log.trace(
                    CampaignPushConstants.LOG_TAG,
                    SELF_TAG,
                    "Remind later pressed, will reschedule the notification to be displayed %d"
                            + " seconds from now",
                    secondsUntilFireDate);
            calendar.add(Calendar.SECOND, (int) secondsUntilFireDate);
            // schedule a pending intent to be broadcast at the specified timestamp
            final PendingIntent pendingIntent =
                    createPendingIntentForScheduledNotification(context, intent);
            final AlarmManager alarmManager =
                    (AlarmManager) context.getSystemService(android.content.Context.ALARM_SERVICE);

            if (alarmManager != null) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);

                // cancel the displayed notification
                notificationManager.cancel(tag.hashCode());
            }
        }
    }

    private static PendingIntent createPendingIntentForScheduledNotification(
            final Context context, final Intent intent) {
        final Intent scheduledIntent =
                new Intent(
                        CampaignPushConstants.IntentActions.SCHEDULED_NOTIFICATION_BROADCAST,
                        null,
                        context,
                        AEPPushTemplateBroadcastReceiver.class);
        scheduledIntent.setClass(context, AEPPushTemplateBroadcastReceiver.class);
        scheduledIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        scheduledIntent.putExtras(intent.getExtras());

        return PendingIntent.getBroadcast(
                context,
                0,
                scheduledIntent,
                PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
