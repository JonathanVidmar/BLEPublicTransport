package com.lth.thesis.blepublictransport.Utils;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Path;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;
import me.grantland.widget.AutofitTextView;

public class VerticalTextView extends AutofitTextView {

    public VerticalTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public VerticalTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VerticalTextView(Context context) {
        super(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        double mAngle = 60;
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        /*int w = getMeasuredWidth();
        int h = getMeasuredHeight();
        w = (int) Math.round(w * Math.cos(Math.toRadians(mAngle)) + h * Math.sin(Math.toRadians(mAngle)));
        h = (int) Math.round(h * Math.cos(Math.toRadians(mAngle)) + w * Math.sin(Math.toRadians(mAngle)));
        setMeasuredDimension(w, h);*/
    }
}
