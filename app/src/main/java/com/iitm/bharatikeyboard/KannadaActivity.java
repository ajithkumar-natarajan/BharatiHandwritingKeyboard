package com.iitm.bharatikeyboard;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import static java.lang.Boolean.TRUE;

public class KannadaActivity extends Activity {

    Button closeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

/*        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(getResources().getColor(android.R.color.holo_red_dark));
        }*/
        setContentView(R.layout.kannada_manual);

        closeButton = (Button)findViewById(R.id.done);
        closeButton.setOnClickListener(new OnClickListener() {

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
