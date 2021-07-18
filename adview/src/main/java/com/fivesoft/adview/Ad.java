package com.fivesoft.adview;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.View.OnClickListener;

public class Ad {

    String topText;
    String bottomText;
    Drawable icon;
    int iconRes;
    OnClickListener clickListener;

    private Ad(String topText, String bottomText, Drawable icon, View.OnClickListener clickListener){
        this.topText = topText;
        this.bottomText = bottomText;
        this.icon = icon;
        this.clickListener = clickListener;
    }

    private Ad(String topText, String bottomText, int iconRes, View.OnClickListener clickListener){
        this.topText = topText;
        this.bottomText = bottomText;
        this.icon = null;
        this.iconRes = iconRes;
        this.clickListener = clickListener;
    }

}
