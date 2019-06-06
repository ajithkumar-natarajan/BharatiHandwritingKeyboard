package com.iitm.bharatikeyboard;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import static java.lang.Boolean.TRUE;

public class MalayalamActivity extends Activity {

    Button closeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.malayalam_manual);

        closeButton = (Button)findViewById(R.id.done);
        closeButton.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                //Finish method is used to close all open activities.
                //finish();

                // Push current activity task to background
                onBackPressed();
            }
        });
    }

    @Override
    public void onBackPressed(){
        moveTaskToBack(TRUE);
    }
}
