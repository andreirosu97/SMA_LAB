package com.upt.cti.sartwallet.model;

import android.graphics.Color;

public class PaymentType {
    public static int getColorFromPaymentType(String type) {
        type = type.toLowerCase();
        if (type.equals("entertainment"))
            return Color.rgb(200, 50, 50);
        else if (type.equals("food"))
            return Color.rgb(50, 150, 50);
        else if (type.equals("taxes"))
            return Color.rgb(20, 20, 150);
        else if (type.equals("travel"))
            return Color.rgb(230, 140, 0);
        else
            return Color.rgb(100, 100, 100);
    }

    public static String[] getTypes()
    {
        String[] s = {"entertainment","food","taxes","travel"};
        return s;
    }
}