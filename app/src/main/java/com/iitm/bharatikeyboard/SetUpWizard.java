package com.iitm.bharatikeyboard;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;
import com.iitm.bharatikeyboard.SetupService;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.content.Intent.FLAG_ACTIVITY_NO_HISTORY;

/**
 * Created by srinath on 30/12/16.
 */

public class SetUpWizard extends Activity{

    public Button enableButton,chooseButton,bharatiButton,startedButton;
    public TextView textview1,textview2,textview3;
    public ImageView number1,number2;
//    private static final int NONE = 0;
    private static final int ENABLE = 1;
    private static final int ENABLED = 2;
    private static final int PICKING = 3;
    private static final int CHOSEN = 4;
    private int mState;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        //setTheme(android.R.style.Theme_Translucent_NoTitleBar);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.setup_activity);

        enableButton = (Button) findViewById(R.id.enable_keyboard);
        chooseButton = (Button) findViewById(R.id.choose_keyboard);
        bharatiButton = (Button) findViewById(R.id.about_bharati);
        startedButton = (Button) findViewById(R.id.getting_started);
        textview1 = (TextView) findViewById(R.id.textView1);
        textview2 = (TextView) findViewById(R.id.textView2);
        textview3 = (TextView) findViewById(R.id.textView3);
        number1 = (ImageView) findViewById(R.id.number1);
        number2 = (ImageView) findViewById(R.id.number2);

        final Intent intentService = new Intent(this,SetupService.class);
        startService(intentService);

        onInputMethodPicked();

        enableButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                final Intent intent = new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS);
                startActivity(intent.addFlags(FLAG_ACTIVITY_NO_HISTORY));
                mState = ENABLE;
                //finish();

                //startActivityForResult(new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK), 2000);

            }
        });

        chooseButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showInputMethodPicker();
                mState = PICKING;

            }
        });

        bharatiButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                bharatiButtonClicked();
                //startActivityForResult(new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK), 2000);

            }
        });

        startedButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                startedButtonClicked();
                //startActivityForResult(new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK), 2000);

            }
        });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(mState == PICKING) {
            mState = CHOSEN;
        }
        else if(mState == ENABLE){
            mState = ENABLED;
        }
        else if((mState == CHOSEN)||(mState == ENABLED)) {
            onInputMethodPicked();
        }
        Log.v("mState", String.valueOf(mState));
    }


    protected void onInputMethodPicked(){

        int enableCheck,chooseCheck;
        enableCheck = 0;
        chooseCheck = 0;
        InputMethodManager im = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        List <InputMethodInfo> mInputMethodProperties = im.getEnabledInputMethodList();
        final int n = mInputMethodProperties.size();
        for (int i = 0; i < n; i++) {
            InputMethodInfo imeInfo = mInputMethodProperties.get(i);
            String serviceName = imeInfo.getServiceName();

            Log.v("wizard","imi"+serviceName);
            if (serviceName.compareTo("com.iitm.bharatikeyboard.BharatiIME")==0) {
                // Disable button, if the keyboard is enabled
                enableButton.setEnabled(false);
                number1.setImageResource(R.drawable.tick);
                enableCheck = 1;
            }

            if (imeInfo.getId().equals(Settings.Secure.getString(getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD))) {

                if(serviceName.compareTo("com.iitm.bharatikeyboard.BharatiIME")==0){
                    // Disable button, if the keyboard is current keyboard
                    chooseButton.setEnabled(false);
                    textview1.setText("All is set now. Start using Bharati keyboard");
                    textview2.setText("in any text entry based applications such");
                    textview3.setText("as Facebook, Twitter, WhatsApp, etc.");
                    number2.setImageResource(R.drawable.tick);
                    chooseCheck = 1;

                    //imeInfo contains the information about the keyboard you are using
                    break;
                }
            }
        }

        if(chooseCheck == 0){
            chooseButton.setEnabled(true);
            textview1.setText("");
            textview2.setText("");
            textview3.setText("");
            number2.setImageResource(R.drawable.number_two);
        }
        if(enableCheck==0){
            enableButton.setEnabled(true);
            number1.setImageResource(R.drawable.number_one);
        }

    }

    public void bharatiButtonClicked(){
        final Intent intent = new Intent(this,AboutBharati.class);
        startActivityForResult(intent,1);
    }

    public void startedButtonClicked(){
        final Intent intent = new Intent(this,GettingStarted.class);
        startActivity(intent);
    }


}
