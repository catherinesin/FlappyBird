package com.example.flappybird;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.util.TypedValue;

import androidx.appcompat.widget.DrawableUtils;

public class Bird {
    private Bitmap birdBmp; //bird image
    public int x,y;  // bird 座標

    private int bHeight;// bird body高度
    private int bWidth;// bird body寬度

    private static final float BIRD_Y = 1/2F; // bird y 座標

    private static final int BIRD_SIZE = 33; // bird 寬度 33dp

    private RectF rect = new RectF(); //  bird 畫的範圍

    public Bird(Context context, int gameWidth, int gameHeight, Bitmap birdBmp) {
        this.birdBmp = birdBmp;
        // set bird position
        x = gameWidth / 2 - birdBmp.getWidth() / 2;
        y = (int) (gameHeight * BIRD_Y);
        // set bird size
        bWidth = dip2px(context, BIRD_SIZE);
        bHeight = (int) (bWidth * 1.0f / birdBmp.getWidth() * birdBmp.getHeight());
    }
    public void draw(Canvas canvas) {
        rect.set(x, y, x + bWidth, y + bHeight);
        canvas.drawBitmap(birdBmp, null, rect, null);
    }

    public static int dip2px(Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public int getX()
    {
        return x;
    }

    public void setX(int x)
    {
        this.x = x;
    }


    public int getY()
    {
        return y;
    }

    public void setY(int y)
    {
        this.y = y;
    }

    public int getWidth()
    {
        return bWidth;
    }

    public int getHeight()
    {
        return bHeight;
    }

}
