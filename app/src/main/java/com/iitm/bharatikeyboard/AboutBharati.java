package com.iitm.bharatikeyboard;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.webkit.WebView;

/**
 * Created by srinath on 10/1/17.
 * Edited by Ajith Kumar on 22/11/18.
 */

public class AboutBharati extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_bharati_activity);

        String text = "<html><body style=\"text-align:justify\"> %s </body></Html>";
        String data = "Bharati is a simple and unified script which can be used to write most major Indian languages. " +
                "It is designed using simplest glyphs and borrowing characters from various Indian scripts often to make it look familiar. " +
                "Bharati characters are designed such that the phonetics of the character is reflected in its shape, and " +
                "will therefore be easy to remember. Scripts currently supported are: Devanagari (Hindi, Marathi,etc.), Bengali, Gujarati, Gurmukhi, " +
                "Kannada, Malayalam, Odia, Tamil, and Telugu.<br/><br/>" +
                "Prof. V. Srinivasa Chakravarthy, <br/>Indian Institute of Technology - Madras. <br/><br/> " +
                "For further information, <br/>Visit us at <a href=\"http://www.bharatiscript.com/\">www.bharatiscript.com</a>";

        WebView webView = (WebView) findViewById(R.id.about_content);
        webView.loadData(String.format(text, data), "text/html", "utf-8");
        webView.setBackgroundColor(Color.TRANSPARENT);


    }
}
