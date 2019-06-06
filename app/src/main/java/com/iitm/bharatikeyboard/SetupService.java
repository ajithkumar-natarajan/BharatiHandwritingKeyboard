package com.iitm.bharatikeyboard;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;

import java.util.List;

/**
 * Created by srinath on 9/1/17.
 */

public class SetupService extends IntentService {

    /**
     * Creates an IntentService
     */
    public SetupService() {
        super("SetupService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {


        final String packageLocal = getPackageName();
        boolean isInputDeviceEnabled = false;

        while(!isInputDeviceEnabled) {

            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            List<InputMethodInfo> list = inputMethodManager.getEnabledInputMethodList();

            // check if our keyboard is enabled as input method
            for(InputMethodInfo inputMethod : list) {
                String packageName = inputMethod.getPackageName();
                if(packageName.equals(packageLocal)) {
                    isInputDeviceEnabled = true;
                }
            }
            Log.v("Inside setupService",packageLocal);
        }


        // open activity
        Intent newIntent = new Intent(getApplicationContext(),SetUpWizard.class);
        newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(newIntent);
    }
}
