package com.bwl.lintdemo;

import android.graphics.Color;

/**
 * Created by baiwenlong on 1/14/21.
 */
public class TestParseColor {
    public static void parseColor(String color) {
        Color.parseColor(color);
        try {
            Color.parseColor(color);
        } catch (Exception ex) {

        }
    }
}
