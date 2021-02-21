package com.example.flappybird;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;

import java.util.Random;

public class Pipe {

    private static final float GAP_BETWEEN_UP_DOWN = 1 / 4F; //  pipes間距離
    private static final float TPIPE_MAX_HEIGHT = 2 / 5F; // 上pipe的最大高度
    private static final float TPIPE_MIN_HEIGHT = 1 / 5F; // 上pipe的最小高度

    private int x; // pipe x座標
    private int height; // 上pipe的高度
    private int margin; // 上下pipes間距離
    private Bitmap pTopBmp; // upper pipe image
    private Bitmap pBaseBmp; // lower pipe image

    private static Random random = new Random();

    public Pipe(Context context, int gameWidth, int gameHeight, Bitmap top, Bitmap base)
    {
        margin = (int) (gameHeight * GAP_BETWEEN_UP_DOWN);
        x = gameWidth;
        pTopBmp = top;
        pBaseBmp = base;
        randomHeight(gameHeight);
    }

    private void randomHeight(int gameHeight) // 隨機 top pipe 的高度
    {
        height = random.nextInt((int) (gameHeight * (TPIPE_MAX_HEIGHT - TPIPE_MIN_HEIGHT))); //  隨機取中間值
        height = (int) (height + gameHeight * TPIPE_MIN_HEIGHT); //  最小高度加上中間值
    }

    public void draw(Canvas mCanvas, RectF rect)  // 由左上往右下畫pipes
    {
        mCanvas.save(); // 儲存當前的狀態
        mCanvas.translate(x, -(rect.bottom - height)); // 不需畫出的上pipe部分 向上偏移(整體長度 - 需要的長度)
        mCanvas.drawBitmap(pTopBmp, null, rect, null);
        mCanvas.translate(0, rect.bottom + margin); // 下pipe 偏移 (上pipe長度+margin)
        mCanvas.drawBitmap(pBaseBmp, null, rect, null);
        mCanvas.restore(); // 重置到上次儲存的狀態
    }
    public boolean isCollide(Bird bird){ // 檢查 bird 是否撞到pipe
        if ((bird.getX() + bird.getWidth() > x && bird.getY() < height - bird.getHeight()) ||
                (bird.getX() + bird.getWidth() > x && bird.getY() + bird.getHeight() > height + margin + bird.getHeight())) {
            return true;
        }
        return false;
    }

    public int getX()
    {
        return x;
    }

    public void setX(int x)
    {
        this.x = x;
    }

}
