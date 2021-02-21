package com.example.flappybird;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.SurfaceHolder;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class GameView extends SurfaceView implements SurfaceHolder.Callback, Runnable{  // 遊戲介面

    private SurfaceHolder holder;
    private Canvas canvas;
    private Thread t;
    // View 的尺寸
    private int viewWidth;
    private int viewHeight;
    private RectF viewRectF = new RectF();
    // 背景
    private Bitmap backgroundBmp; // background
    // 鳥
    private Bird bird;
    private Bitmap birdBmp;
    private final int birdTouchUpDis = dip2px(getContext(), -16); // 點擊螢幕 bird上升的距離
    private int tmpBirdDis; //  bird 跳躍的臨時距離
    private final int fallingSpeed = dip2px(getContext(), 2); // bird 墜落速度
    private final int fallingSpeed2 = dip2px(getContext(), 3); // dead bird 墜落速度
    // 水管
    private List<Pipe> pipes = new ArrayList<Pipe>(); // 多組 pipes
    private List<Pipe> needRemovePipes = new ArrayList<Pipe>(); // 超出螢幕需要刪除的 pipes
    private Bitmap pipeTopBmp;
    private Bitmap pipeBaseBmp;
    private RectF pipeRectF; // pipe 繪製範圍
    private final int PIPE_WIDTH = dip2px(getContext(), 80);  // pipe 寬度 80dp
    private int removedPipes = 0; // 紀錄被刪除的 pipe 數
    private final int DIS_BETWEEN_PIPES = dip2px(getContext(), 160); // 左右 pipes 間隔距離
    private int tmpMoveDis = 0; //  記錄移動距離 達到 DIS_BETWEEN_PIPES 生成 new pipe
    private int speed; // pipe 向左移動速度
    // 分數
    private final int[] scores = new int[] { R.drawable.n0, R.drawable.n1,
            R.drawable.n2, R.drawable.n3, R.drawable.n4, R.drawable.n5,
            R.drawable.n6, R.drawable.n7, R.drawable.n8, R.drawable.n9 }; // 分數圖檔
    private Bitmap[] scoresBmp;
    //private static final float SCORE_HEIGHT = 1 / 15f;
    private int scoreDigitWidth; // 單一數字寬度
    private int scoreDigitHeight; // 單一數字高度
    private RectF scoreRectF; // 分數大小範圍
    private int score; // 分數

    private boolean isRunning;
    private GameStatus status = GameStatus.WAITING; // 初始等待狀態


    private enum GameStatus {
        WAITING, RUNNING, OVER
    }

    public GameView(Context context) {
        this(context, null);
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        holder = getHolder();
        holder.addCallback(this);
        setZOrderOnTop(true);
        holder.setFormat(PixelFormat.TRANSLUCENT);
        setFocusable(true);
        setFocusableInTouchMode(true);
        this.setKeepScreenOn(true);

        speed =dip2px(getContext(), 5);  // 初始化速度
        initBitmaps();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        isRunning = true; // open thread
        t = new Thread(this);
        t.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // TODO Auto-generated method stub

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isRunning = false; // close thread
    }

    @Override
    public void run() {
        while (isRunning) {
            long start = System.currentTimeMillis();

            draw();
            gameLogic(); // 遊戲設定
            long end = System.currentTimeMillis();
            try {
                if (end - start < 50) {
                    Thread.sleep(50 - (end - start));
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void gameLogic() {
        switch (status) {
            case RUNNING:
                score = 0;
                tmpBirdDis += fallingSpeed; // 讓 bird 自然下墜
                bird.setY(bird.getY() + tmpBirdDis); // 讓 bird 飛起來~
                // 移除不用的 pipes
                for (Pipe pipe : pipes) {
                    if (pipe.getX() < -PIPE_WIDTH) { // pipe 往左超出螢幕
                        needRemovePipes.add(pipe); //  加入移除清單
                        removedPipes++;
                        continue;
                    }
                    pipe.setX(pipe.getX() - speed); //  pipes moving
                }
                pipes.removeAll(needRemovePipes); // 移除 pipes
                // 新增 pipes
                tmpMoveDis += speed; // pipe 當下移動距離
                if (tmpMoveDis >= DIS_BETWEEN_PIPES) { // 移動距離大於預設的 pipes 間距時，產生新的 pipe
                    Pipe pipe = new Pipe(getContext(), getWidth(), getHeight(), pipeTopBmp, pipeBaseBmp);
                    pipes.add(pipe);
                    tmpMoveDis = 0;
                }
                // 計算 得分
                score += removedPipes; // 加總已經看不到的 pipe 數
                for (Pipe pipe : pipes) { // 加上還在螢幕上 bird 左邊的 pipe，即為 score
                    if (pipe.getX() + PIPE_WIDTH < bird.getX()) {
                        score++;
                    }
                }
                checkIfOver(); // 檢查遊戲終止條件
                break;

            case OVER: // bird 撞擊 遊戲結束
                if (bird.getY() < viewHeight - bird.getHeight()) { // 若還在空中，讓 bird 落地
                    tmpBirdDis += fallingSpeed2;
                    bird.setY(bird.getY() + tmpBirdDis);
                } else {
                    bird.setY(viewHeight + bird.getHeight());
                    status = GameStatus.WAITING; // 由結束畫面跳轉至等待模式
                    restore(); // 還原設定
                }
                break;

            default:
                break;
        }
    }

    private void checkIfOver() { // 檢查遊戲終止條件
        if (bird.getY() > viewHeight - bird.getHeight()) { // 掉到地板情況
            status = GameStatus.OVER;
        }
        for (Pipe p : pipes) {
            if (p.getX() + PIPE_WIDTH < bird.getX()) {
                continue;
            }
            if (p.isCollide(bird)) { // 撞到 pipe 情況
                status = GameStatus.OVER;
                break;
            }
        }
    }

    private void restore() // 還原設定
    {
        pipes.clear();
        needRemovePipes.clear();
        bird.setY(viewHeight * 1 / 2);
        tmpBirdDis = 0;
        tmpMoveDis = 0 ;
        removedPipes = 0;
    }

    private void initBitmaps() { // 初始化 圖片們
        backgroundBmp = loadImageByResId(R.drawable.background);
        birdBmp = loadImageByResId(R.drawable.bird);
        pipeTopBmp = loadImageByResId(R.drawable.pipe_top);
        pipeBaseBmp = loadImageByResId(R.drawable.pipe_base);
        scoresBmp = new Bitmap[scores.length];
        for (int i = 0; i < scoresBmp.length; i++) {
            scoresBmp[i] = loadImageByResId(scores[i]);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) { // 點擊
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            switch (status) {
                case WAITING:
                    status = GameStatus.RUNNING; // 由等待到開始遊戲
                    break;
                case RUNNING:
                    tmpBirdDis = birdTouchUpDis; // 鳥開始飛行
                    break;
            }
        }
        return true;
    }

    private void draw() { // 畫遊戲元素 : 背景、鳥、水管、分數
        try
        {
            canvas = holder.lockCanvas();
            if (canvas != null)
            {
                drawBkgd();
                drawBird();
                drawPipes();
                drawScores();
            }
        } catch (Exception e) {

        } finally {
            if (canvas != null)
                holder.unlockCanvasAndPost(canvas);
        }
    }

    private void drawBkgd() { // 背景
        canvas.drawBitmap(backgroundBmp, null, viewRectF, null);
    }
    private void drawBird() { // 鳥
        bird.draw(canvas);
    }

    private void drawPipes(){ // 水管
        for (Pipe pipe : pipes) {
            pipe.setX(pipe.getX() - speed);  // pipe 向左移動
            pipe.draw(canvas, pipeRectF);
        }
    }

    private void drawScores() { // 分數
        canvas.translate(viewWidth / 2 - scoreDigitWidth / 2, 1f / 8 * viewHeight); // 設定分數顯示位置
        canvas.save();
        while (score < 10)  // 畫個位數 score
        {
            canvas.drawBitmap(scoresBmp[score], null, scoreRectF, null);
            canvas.restore();
        }
        canvas.save();
        while ( score > 9 && score < 100){  // 畫十位數 score
            int n1 = score / 10;  // 十位數
            int n2 = score % 10;  // 個位數
            canvas.drawBitmap(scoresBmp[n1], null, scoreRectF, null);
            canvas.translate(scoreDigitWidth, 0); //　往右邊一格
            canvas.drawBitmap(scoresBmp[n2], null, scoreRectF, null);
            canvas.restore();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) { //  初始化 遊戲元素尺寸配合螢幕大小
        super.onSizeChanged(w, h, oldw, oldh);
        viewWidth = w;
        viewHeight = h;
        viewRectF.set(0, 0, w, h); // game view 範圍
        bird = new Bird(getContext(), viewWidth, viewHeight, birdBmp); // 初始化 bird
        pipeRectF = new RectF(0, 0, PIPE_WIDTH, viewHeight); // pipe 範圍
        scoreDigitHeight = (int) (h * 1 / 15f); // score 高
        scoreDigitWidth = (int) (scoreDigitHeight * 1.0f / scoresBmp[0].getHeight() * scoresBmp[0].getWidth()); // score 寬
        scoreRectF = new RectF(0, 0, scoreDigitWidth, scoreDigitHeight);  // score 範圍
    }

    private Bitmap loadImageByResId(int resId) { //  傳入圖檔公式
        return BitmapFactory.decodeResource(getResources(), resId);
    }

    public static int dip2px(Context context, float dpValue) { // dip 轉為 px 公式
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
