package com.iitm.bharatikeyboard;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.webkit.WebView;

/**
 * Created by srinath on 10/1/17.
 */

public class GettingStarted extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.getting_started_activity);

        String text = "<html><body style=\"text-align:justify\"> %s </body></Html>";
        String data1 = "<table align=\"center\"> " +
                "<col align=\"right\">" +
                "<col align=\"left\">" +
                "<tr valign=\"top\"> " +
                "<td>1.</td> " +
                "<td>Tap the text box in any application to get Bharati Handwriting Keyboard</td> " +
                "</tr>" +
                "<tr valign=\"top\"> " +
                "<td>2.</td> " +
                "<td>Choose a script from the menu to the left of space bar</td> " +
                "</tr>" +
                "<tr valign=\"top\"> " +
                "<td>3.</td> " +
                "<td>Write Bharati characters on the writing area with a stylus or a finger</td> " +
                "</tr>" +
                "<tr valign=\"top\"> " +
                "<td>4.</td> " +
                "<td>The handwritten characters will be recognized by the engine and transliterated to the Indian script selected automatically</td> " +
                "</tr>" +
                "</table>" +
                "<br/> <br/>" +
        "<table border=\"1\" align=\"center\"> " +
                "<col align=\"right\">" +
                "<col align=\"left\">" +
                "<tr> " +
                "<th>Auto <br/> on/off</th> " +
                "<td>Turn off/on automatic transliteration</td> " +
                "</tr>" +
                "<tr> " +
                "<th>&#x2713</th> " +
                "<td>Transliterate to the Indian Script</td> " +
                "</tr>" +
                "<tr> " +
                "<th>&#x27F2;</th> " +
                "<td>Clear the last stroke on the writing area <br/> Long press to clear all the strokes</td> " +
                "</tr>" +
                "<tr> " +
                "<th>&#63;</th> " +
                "<td>Show the mappings between bharati <br/>characters and characters of the Indian <br/>script selected</td> " +
                "</tr>" +
                "<tr> " +
                "<th>Space <br/> bar</th> " +
                "<td>Long press to change keyboard</td> " +
                "</tr>" +
                "</table>";

        WebView webView1 = (WebView) findViewById(R.id.getting_content1);
        webView1.loadData(String.format(text, data1), "text/html", "utf-8");


    }
}
