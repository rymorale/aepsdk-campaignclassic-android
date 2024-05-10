package com.adobe.marketing.mobile;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.adobe.marketing.mobile.services.Log;

public class InputBoxFeedbackReceiver extends BroadcastReceiver {
    private static final String SELF_TAG = "InputBoxFeedbackReceiver";

    @Override
    public void onReceive(final Context context, final Intent intent) {
        if (intent == null || intent.getExtras() == null || intent.getExtras().isEmpty()) {
            Log.trace(CampaignPushConstants.LOG_TAG,
                    SELF_TAG, "Received empty intent. Ignoring.");
            return;
        }
        final String input = intent.getStringExtra(CampaignPushConstants.IntentKeys.INPUT_BOX_CONTENTS);
        Log.trace(CampaignPushConstants.LOG_TAG,
                SELF_TAG, "InputBoxFeedbackReceiver received input: %s", input);
        final SharedPreferences preferences = context.getSharedPreferences("inputBoxFeedback", Context.MODE_PRIVATE);
        preferences.edit().putString("inputBoxFeedback", input).apply();
    }
}


