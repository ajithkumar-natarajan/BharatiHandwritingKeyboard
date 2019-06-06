package com.iitm.bharatikeyboard;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import static java.lang.Boolean.TRUE;

public class GurmukhiActivity extends Activity {
    Button closeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gurmukhi_manual);
        closeButton = (Button)findViewById(R.id.done);
        closeButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                //Finish method is used to close all open activities.
                //finish();
                onBackPressed();
            }
        });
    }

    @Override
    public void onBackPressed(){
        moveTaskToBack(TRUE);
    }
}
