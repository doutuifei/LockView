package com.muzi.library;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by muzi on 2018/3/28.
 * 727784430@qq.com
 */

public class LockChildView extends View {

    private Paint topPaint, innerPaint, trianglePaint;

    private int screenWidth, screenHeight;

    private float topCircleRadius;//外圆半径
    private float innerCircleRadius;//内圆半径
    private float topCircleStroke = 2;//外圆线条宽度

    private int topCircleNormalColor;
    private int topCircleSelectColor;
    private int topCircleErrorColor;
    private int innerCircleNormalColor;

    private Path path;
    private int angle = 0;//旋转角度

    private float width, height;
    private int widthMode, heightMode;

    private
    @LockChildView.STATE
    int state = LockChildView.STATE_NORMALE;
    public static final int STATE_NORMALE = 1;
    public static final int STATE_SELECT = 2;
    public static final int STATE_ERROR = 3;


    @IntDef({STATE_NORMALE, STATE_SELECT, STATE_ERROR})
    @Retention(RetentionPolicy.SOURCE)
    public @interface STATE {
    }

    public LockChildView(Context context) {
        this(context, null);
    }

    public LockChildView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LockChildView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        topCircleNormalColor = ContextCompat.getColor(context, R.color.topCircleNormalColor);
        innerCircleNormalColor = ContextCompat.getColor(context, R.color.innerCircleNormalColor);
        topCircleSelectColor = ContextCompat.getColor(context, R.color.topCircleSelectColor);
        topCircleErrorColor = ContextCompat.getColor(context, R.color.topCircleErrorColor);
        topPaint = new Paint();
        innerPaint = new Paint();
        trianglePaint = new Paint();

        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);
        screenWidth = metrics.widthPixels;
        screenHeight = metrics.heightPixels;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = MeasureSpec.getSize(widthMeasureSpec);
        widthMode = MeasureSpec.getMode(widthMeasureSpec);

        height = MeasureSpec.getSize(heightMeasureSpec);
        heightMode = MeasureSpec.getMode(heightMeasureSpec);

        if (widthMode == MeasureSpec.AT_MOST) {
            width = screenWidth / 2.0f;
        }

        if (heightMode == MeasureSpec.AT_MOST) {
            height = screenHeight / 2.0f;
        }

        height = width = width < height ? width : height;

        topCircleRadius = width / 2.0f - topCircleStroke;
        innerCircleRadius = width / 8.0f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        switch (state) {
            case STATE_NORMALE:
                topPaint.setColor(topCircleNormalColor);
                topPaint.setStyle(Paint.Style.FILL);
                topPaint.setAntiAlias(true);

                innerPaint.setColor(innerCircleNormalColor);
                innerPaint.setStyle(Paint.Style.FILL);
                innerPaint.setAntiAlias(true);
                drawTopCircle(canvas);
                drawInnerCircle(canvas);
                break;
            case STATE_SELECT:
                topPaint.setColor(topCircleSelectColor);
                topPaint.setStyle(Paint.Style.STROKE);
                topPaint.setStrokeWidth(topCircleStroke);

                trianglePaint.setColor(ContextCompat.getColor(getContext(), R.color.topCircleSelectColor));

                innerPaint.setColor(topCircleSelectColor);
                innerPaint.setStyle(Paint.Style.FILL);
                drawTopCircle(canvas);
                drawInnerCircle(canvas);
//                drawTriangle(canvas);
                break;
            case STATE_ERROR:
                topPaint.setColor(topCircleErrorColor);
                topPaint.setStyle(Paint.Style.STROKE);
                topPaint.setStrokeWidth(topCircleStroke);

                trianglePaint.setColor(ContextCompat.getColor(getContext(), R.color.topCircleErrorColor));

                innerPaint.setColor(topCircleErrorColor);
                innerPaint.setStyle(Paint.Style.FILL);
                drawTopCircle(canvas);
                drawInnerCircle(canvas);
//                drawTriangle(canvas);
                break;
        }
    }

    /**
     * 绘制外圆
     *
     * @param canvas
     */
    private void drawTopCircle(Canvas canvas) {
        canvas.drawCircle(width / 2.0f, height / 2.0f, topCircleRadius, topPaint);
    }

    /**
     * 绘制内圆
     *
     * @param canvas
     */
    private void drawInnerCircle(Canvas canvas) {
        canvas.drawCircle(width / 2.0f, height / 2.0f, innerCircleRadius, innerPaint);
    }

    /**
     * 绘制三角形
     *
     * @param canvas
     */
    private void drawTriangle(Canvas canvas) {
        if (path == null) {
            path = new Path();
            path.moveTo(width / 2.0f, innerCircleRadius);
            path.lineTo(width / 2.0f - innerCircleRadius, innerCircleRadius * 2);
            path.lineTo(width / 2.0f + innerCircleRadius, innerCircleRadius * 2);
            path.lineTo(width / 2.0f, innerCircleRadius);
            trianglePaint.setStyle(Paint.Style.FILL);
        }
        canvas.rotate(angle, width / 2.0f, height / 2.0f);
        canvas.drawPath(path, trianglePaint);
        canvas.restore();
    }

    /**
     * 设置状态
     *
     * @param state
     */
    public void setState(@STATE int state) {
        if (state == this.state) {
            return;
        }
        this.state = state;
        invalidate();
    }

    /**
     * 旋转
     *
     * @param angle
     */
    public void setAngle(int angle) {
        if (this.angle == angle) {
            return;
        }
        this.angle = angle;
        invalidate();
    }

}
