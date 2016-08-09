package com.example.guaguaka.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import com.example.guaguaka.R;

/**
 * Created by Administrator on 2016/8/9.
 */
public class GuaGuaKa extends View {

    private Paint mOutterPaint;

    private Path mPath;

    private Canvas mCanvas;

    private Bitmap mBitmap;

    private int mLastX;
    private int mLastY;

    private Bitmap mOutterBitmap;

    // ------------------------

//    private Bitmap bitmap;

    // 最底下的中奖信息
    private String mText;

    private Paint mBackPaint;

    // 记录中奖信息文本的宽和高
    private Rect mTextBound;

    private int mTextSize;

    private int mTextColor;

    // 判断遮盖层区域是否消除达到阈值
    // volatile 子线程更新，主线程判断时的可见性
    private volatile boolean mComplete = false;

    /**
     * 刮刮卡刮完的回调
     */
    public interface OnGuaGuaKaCompleteListener {
        void complete();
    }

    private OnGuaGuaKaCompleteListener mListener;

    public void setOnGuaGuaKaCompleteListener(OnGuaGuaKaCompleteListener listener) {
        this.mListener = listener;
    }

    public GuaGuaKa(Context context) {
        this(context, null);
    }

    public GuaGuaKa(Context context, AttributeSet attrs) {
        this(context, attrs, 0);

//        context.obtainStyledAttributes();

    }

    public GuaGuaKa(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();

        TypedArray a = null;

        try {
            a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.GuaGuaKa, defStyleAttr, 0);

            int n = a.getIndexCount();

            for (int i = 0; i < n; i++) {
                int attr = a.getIndex(i);

                switch (attr) {
                    case R.styleable.GuaGuaKa_text:
                        mText = a.getString(attr);
                        break;
                    case R.styleable.GuaGuaKa_textSize:
                        mTextSize = (int) a.getDimension(attr, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 22, getResources().getDisplayMetrics()));
                        break;
                    case R.styleable.GuaGuaKa_textColor:
                        mTextColor = a.getColor(attr, 0x000000);
                        break;
                }

            }

        } finally {
            if (a != null) {
                a.recycle();
            }
        }

    }

    public void setText(String text) {
        this.mText = text;

        // 获得当前画笔绘制文本的宽和高
        mBackPaint.getTextBounds(mText, 0, mText.length(), mTextBound);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = getMeasuredWidth();
        int height = getMeasuredHeight();

        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        mCanvas = new Canvas(mBitmap);

        // 设置绘制path画笔的一些属性
        setupOutPaint();
        setupBackPaint();

//        mCanvas.drawColor(Color.parseColor("#c0c0c0"));
        mCanvas.drawRoundRect(new RectF(0, 0, width, height), 30, 30, mOutterPaint);

        mCanvas.drawBitmap(mOutterBitmap, null, new Rect(0, 0, width, height), null);

    }

    /**
     * 设置绘制获奖信息的画笔属性S
     */
    private void setupBackPaint() {
        mBackPaint.setColor(Color.DKGRAY);
        mBackPaint.setStyle(Paint.Style.FILL);
        mBackPaint.setTextSize(mTextSize);

        // 获得当前画笔绘制文本的宽和高
        mBackPaint.getTextBounds(mText, 0, mText.length(), mTextBound);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int action = event.getAction();

        int x = (int) event.getX();
        int y = (int) event.getY();

        switch (action) {
            case MotionEvent.ACTION_DOWN:

                mLastX = x;
                mLastY = y;

                mPath.moveTo(mLastX, mLastY);

                break;
            case MotionEvent.ACTION_MOVE:

                int dx = Math.abs(x - mLastX);
                int dy = Math.abs(y - mLastY);

                if (dx > 3 || dy > 3) {
                    mPath.lineTo(x, y);
                }

                mLastX = x;
                mLastY = y;

                break;
            case MotionEvent.ACTION_UP:
                new Thread(mRunnable).start();
                break;
        }

        invalidate();

        return true;
    }

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            int w = getWidth();
            int h = getHeight();

            // 擦除区域的面积
            float wipeArea = 0;
            // 总共的像素值
            float totalArea = w * h;

            Bitmap bitmap = mBitmap;

            int[] mPixels = new int[w * h];

            // 获得Bitmap上所有的像素信息
            bitmap.getPixels(mPixels, 0, w, 0, 0, w, h);

            for (int i = 0; i < w; i++) {
                for (int j = 0; j < h; j++) {
                    int index = i + j * w;

                    if (mPixels[index] == 0) {
                        wipeArea++;
                    }

                }
            }

            if (wipeArea > 0 && totalArea > 0) {
                int percent = (int) (wipeArea * 100 / totalArea);

                Log.e("TAG", percent + "");

                if (percent > 60) {
                    // 清除掉图层区域

                    mComplete = true;

                    postInvalidate();

                }

            }

        }
    };

    @Override
    protected void onDraw(Canvas canvas) {

//        canvas.drawBitmap(bitmap, 0, 0, null);

        canvas.drawText(mText, getWidth() / 2 - mTextBound.width() / 2, getHeight() / 2 + mTextBound.height() / 2, mBackPaint);

        if (mComplete) {

            if (mListener != null) {
                mListener.complete();
            }

        }

        if (!mComplete) {

            drawPath();

            canvas.drawBitmap(mBitmap, 0, 0, null);

        }

    }

    private void drawPath() {

        mOutterPaint.setStyle(Paint.Style.STROKE);

        mOutterPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));

        mCanvas.drawPath(mPath, mOutterPaint);
    }

    /**
     * 设置绘制path画笔的一些属性
     *
     * 橡皮擦功能的画笔
     */
    private void setupOutPaint() {

//        mOutterPaint.setColor(Color.RED);
        mOutterPaint.setColor(Color.parseColor("#c0c0c0"));
        mOutterPaint.setAntiAlias(true);
        mOutterPaint.setDither(true);
        mOutterPaint.setStrokeJoin(Paint.Join.ROUND);
        mOutterPaint.setStrokeCap(Paint.Cap.ROUND);
//        mOutterPaint.setStyle(Paint.Style.STROKE);
        mOutterPaint.setStyle(Paint.Style.STROKE);
        mOutterPaint.setStrokeWidth(40);

    }

    private void init() {
        mOutterPaint = new Paint();
        mPath = new Path();

//        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.meizi);

//        mOutterBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.meizi);

        mOutterBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.yrm);

        mText = "谢谢惠顾";

        mTextBound = new Rect();

        mBackPaint = new Paint();

//        mTextSize = 50;
        mTextSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 22, getResources().getDisplayMetrics());

    }

}
