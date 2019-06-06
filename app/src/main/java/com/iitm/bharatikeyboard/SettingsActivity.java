package com.iitm.bharatikeyboard;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;

public class SettingsActivity extends Activity{
    public int fillDefault;
    public int time,rt;
    public SeekBar seek;
    public Button closeButton;
/*
    @Override
    void onStart ()
    {
        super.onStart();
        Intent intent = getIntent();
        if (intent !=null && intent.getExtras()!=null)
        {
            //String key = "RT";
            int value = intent.getIntExtra("RT",800);
            // float value = intent.getParcelableExtra(key);
            fillDefault =  (int) value/200;
        }

        seek.setProgress(fillDefault);
    }*/

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("TIME",rt);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        seek=(SeekBar) findViewById(R.id.Time);
        closeButton = (Button)findViewById(R.id.done);
        fillDefault = 4;
        rt = fillDefault;

        if (savedInstanceState != null) {
            // Then the application is being reloaded
            rt = savedInstanceState.getInt("TIME");
        }
        if(rt<fillDefault)
            rt = fillDefault;
        seek.setProgress(rt);
        /*
        Intent intent = getIntent();
        if (intent !=null && intent.getExtras()!=null)
        {
            //String key = "RT";
            int value = intent.getIntExtra("RT",800);
            // float value = intent.getParcelableExtra(key);
            fillDefault =  (int) value/200;
        }

        if(fillDefault<0.4)
            fillDefault = 4;

        seek.setProgress(fillDefault);
        */


        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
                // TODO Auto-generated method stub
                if (progress < fillDefault){
                    seekBar.setProgress(fillDefault); // magic solution, ha
                }

                time=progress;

            }
        });

        closeButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                //Finish method is used to close all open activities.
                rt = time;
                Intent intent = new Intent(SettingsActivity.this, BharatiIME.class);
                intent.putExtra("TIME",rt);
                startService(intent);

                finish();
            }
        });


    }
}
