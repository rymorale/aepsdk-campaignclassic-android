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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.adobe.marketing.mobile.util.StringUtils;

/**
 * Broadcast receiver for handling custom push template notification interactions.
 */
public class AEPPushTemplateBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        final String action = intent.getAction();
        if (StringUtils.isNullOrEmpty(action)) {
            return;
        }

        switch (action) {
            case CampaignPushConstants.IntentActions.FILMSTRIP_LEFT_CLICKED:
            case CampaignPushConstants.IntentActions.FILMSTRIP_RIGHT_CLICKED:
            case CampaignPushConstants.IntentActions.MANUAL_CAROUSEL_LEFT_CLICKED:
            case CampaignPushConstants.IntentActions.MANUAL_CAROUSEL_RIGHT_CLICKED:
                CampaignClassicIntentHandler.handleCarouselArrowClickedIntent(context, intent);
                break;
            case CampaignPushConstants.IntentActions.REMIND_LATER_CLICKED:
                CampaignClassicIntentHandler.handleRemindIntent(context, intent);
                break;
            case CampaignPushConstants.IntentActions.SCHEDULED_NOTIFICATION_BROADCAST:
                CampaignClassicIntentHandler.handleScheduledIntent(context, intent);
                break;
            case CampaignPushConstants.IntentActions.INPUT_RECEIVED:
                CampaignClassicIntentHandler.handleInputBoxIntent(context, intent);
                break;
            case CampaignPushConstants.IntentActions.CATALOG_THUMBNAIL_1_CLICKED:
            case CampaignPushConstants.IntentActions.CATALOG_THUMBNAIL_2_CLICKED:
            case CampaignPushConstants.IntentActions.CATALOG_THUMBNAIL_3_CLICKED:
                CampaignClassicIntentHandler.handleProductCatalogThumbnailIntent(context, intent);
                break;
        }
    }
}
