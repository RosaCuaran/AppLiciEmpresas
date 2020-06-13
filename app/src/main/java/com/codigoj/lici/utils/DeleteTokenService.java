package com.codigoj.lici.utils;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;

import java.io.IOException;

/**
 * Created by Jhon on 13/07/17.
 */

public class DeleteTokenService extends IntentService {

    public static final String TAG = DeleteTokenService.class.getSimpleName();

    public DeleteTokenService() {
        super(TAG);
    }
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        try
        {
            // Check for current token
            Log.d(TAG, "Token before deletion: " + FirebaseInstanceId.getInstance().getToken());

            // Resets Instance ID and revokes all tokens.
            FirebaseInstanceId.getInstance().deleteInstanceId();

            // Now manually call onTokenRefresh()
            FirebaseInstanceId.getInstance().getToken();
            Log.d(TAG, "I had removed the token");
        }
        catch (IOException e)
        {
            Log.d("error-token", e.getMessage());
        }
    }
}
