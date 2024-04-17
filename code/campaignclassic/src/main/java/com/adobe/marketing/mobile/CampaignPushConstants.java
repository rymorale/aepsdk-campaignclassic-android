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

/**
 * This class holds all constant values used only by the Campaign Classic extension for handling
 * push notifications
 */
final class CampaignPushConstants {

    private CampaignPushConstants() {
    }

    static final String LOG_TAG = "CampaignClassicExtension";

    static final class NotificationAction {
        static final String OPENED = "Notification Opened";
        static final String BUTTON_CLICKED = "Notification Button Clicked";

        private NotificationAction() {
        }
    }

    static final class Tracking {
        static final class Keys {
            static final String ACTION_URI = "actionUri";
            static final String DELIVERY_ID = "_dId";
            static final String MESSAGE_ID = "_mId";

            private Keys() {
            }
        }

        private Tracking() {
        }
    }

    static final class IntentActions {
        static final String FILMSTRIP_LEFT_CLICKED = "filmstrip_left";
        static final String FILMSTRIP_RIGHT_CLICKED = "filmstrip_right";
        static final String REMIND_LATER_CLICKED = "remind_clicked";
        static final String SCHEDULED_NOTIFICATION_BROADCAST = "scheduled_notification_broadcast";
        static final String MANUAL_CAROUSEL_LEFT_CLICKED = "manual_left";
        static final String MANUAL_CAROUSEL_RIGHT_CLICKED = "manual_right";

        private IntentActions() {
        }
    }

    static final class IntentKeys {
        static final String MESSAGE_ID = "messageId";
        static final String REMIND_EPOCH_TS = "remindEpochTimestamp";
        static final String REMIND_DELAY_TS = "remindDelayTimestamp";
        static final String TAG = "tag";

        private IntentKeys() {
        }
    }

    static final class PushPayloadKeys {
        public static final String TITLE = "adb_title";
        public static final String BODY = "adb_body";
        public static final String SOUND = "adb_sound";
        public static final String BADGE_NUMBER = "adb_n_count";
        public static final String NOTIFICATION_VISIBILITY = "adb_n_visibility";
        public static final String NOTIFICATION_PRIORITY = "adb_n_priority";
        public static final String CHANNEL_ID = "adb_channel_id";
        public static final String SMALL_ICON = "adb_small_icon";
        public static final String IMAGE_URL = "adb_image";
        public static final String TAG = "adb_tag";
        public static final String TICKER = "adb_ticker";
        public static final String STICKY = "adb_sticky";
        public static final String ACTION_URI = "adb_uri";

        private PushPayloadKeys() {
        }
    }
}
