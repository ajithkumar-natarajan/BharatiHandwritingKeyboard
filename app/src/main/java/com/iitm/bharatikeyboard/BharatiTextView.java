package com.iitm.bharatikeyboard;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

public class BharatiTextView extends TextView {

    /*
 * Caches typefaces based on their file path and name, so that they don't have to be created every time when they are referenced.
 */
    private static Typeface tf;

    public BharatiTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if(!isInEditMode()) {

            init();

        }
    }

    public BharatiTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if(!isInEditMode()) {

            init();

        }
    }

    public BharatiTextView(Context context) {
        super(context);
        if(!isInEditMode()) {

            init();

        }
    }

    private void init() {
        tf = Typeface.createFromAsset(getContext().getAssets(),
                "fonts/NavBharati.ttf");
        setTypeface(tf);
    }

}