package com.muzi.library;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.support.annotation.IntRange;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by muzi on 2018/3/28.
 * 727784430@qq.com
 */

public class LockView extends RelativeLayout {

    private int screenWidth, screenHeight;

    private int mCount;//数量
    private int tryNum;//次数
    private int tryTimes;

    private int mWidth, mHeight;

    private LockChildView[] gestureLockViews;
    private int mLockViewWidth;//LockView的宽度
    private int mMarginBetweenLockView;//LockView的间距

    private Paint mPaint;
    private Path mPath;

    private int mLastPathX, mLastPathY;
    /**
     * 指引下的结束位置
     */
    private Point mTmpTarget = new Point();

    /**
     * 记录按下的路径顺序
     */
    private List<Integer> choose = new ArrayList<>();

    /**
     * 密码
     */
    private List<Integer> psd = new ArrayList<>();


    public LockView(Context context) {
        this(context, null);
    }

    public LockView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.LockView);
        mCount = typedArray.getInt(R.styleable.LockView_count, 3);
        tryTimes = tryNum = typedArray.getInt(R.styleable.LockView_try_num, -1);
        typedArray.recycle();

        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeJoin(Paint.Join.ROUND);

        mPath = new Path();

        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(metrics);
        screenWidth = metrics.widthPixels;
        screenHeight = metrics.heightPixels;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = MeasureSpec.getSize(widthMeasureSpec);
        mHeight = MeasureSpec.getSize(heightMeasureSpec);

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        if (widthMode == MeasureSpec.AT_MOST) {
            mWidth = screenWidth;
        }
        if (heightMode == MeasureSpec.AT_MOST) {
            mHeight = screenHeight;
        }

        mHeight = mWidth = mWidth < mHeight ? mWidth : mHeight;

        if (gestureLockViews == null) {
            gestureLockViews = new LockChildView[mCount * mCount];

            // 计算每个GestureLockView的宽度
            mLockViewWidth = (int) (4 * mWidth * 1.0f / (5 * mCount + 1));

            //计算每个GestureLockView的间距
            mMarginBetweenLockView = (int) (mLockViewWidth * 0.25);

            // 设置画笔的宽度为GestureLockView的内圆直径稍微小点（不喜欢的话，随便设）
            mPaint.setStrokeWidth(mLockViewWidth / 8.0f);

            for (int i = 0; i < gestureLockViews.length; i++) {
                gestureLockViews[i] = new LockChildView(getContext());
                gestureLockViews[i].setId(i + 1);
                LayoutParams lockerParams = new LayoutParams(
                        mLockViewWidth, mLockViewWidth);
                //如果不是第一个就设置为前一个右边
                if (i % mCount != 0) {
                    lockerParams.addRule(RelativeLayout.RIGHT_OF, gestureLockViews[i - 1].getId());
                }

                // 从第二行开始，设置为上一行同一位置View的下面
                if (i > mCount - 1) {
                    lockerParams.addRule(RelativeLayout.BELOW, gestureLockViews[i - mCount].getId());
                }

                //设置右下左上的边距
                int rightMargin = mMarginBetweenLockView;
                int bottomMargin = mMarginBetweenLockView;
                int leftMagin = 0;
                int topMargin = 0;

                /**
                 * 每个View都有右外边距和底外边距 第一行的有上外边距 第一列的有左外边距
                 */
                if (i >= 0 && i < mCount) {
                    // 第一行
                    topMargin = mMarginBetweenLockView;
                }
                if (i % mCount == 0) {
                    // 第一列
                    leftMagin = mMarginBetweenLockView;
                }

                lockerParams.setMargins(leftMagin, topMargin, rightMargin,
                        bottomMargin);

                gestureLockViews[i].setState(LockChildView.STATE_NORMALE);
                addView(gestureLockViews[i], lockerParams);
            }
        }
        setMeasuredDimension(mWidth, mHeight);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (tryTimes == 0) {
            return true;
        }
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                reset();
                break;
            case MotionEvent.ACTION_MOVE:
                mPaint.setColor(ContextCompat.getColor(getContext(), R.color.topCircleSelectColor));
                mPaint.setAlpha(80);

                LockChildView child = getChildIdByPos(x, y);
                if (child != null) {
                    int id = child.getId();
                    if (!choose.contains(id)) {
                        choose.add(id);
                        child.setState(LockChildView.STATE_SELECT);

                        mLastPathX = child.getLeft() / 2 + child.getRight() / 2;
                        mLastPathY = child.getTop() / 2 + child.getBottom() / 2;

                        if (choose.size() == 1) {// 当前添加为第一个
                            mPath.moveTo(mLastPathX, mLastPathY);
                        } else {
                            // 非第一个，将两者使用线连上
                            mPath.lineTo(mLastPathX, mLastPathY);
                        }
                    }
                }
                // 指引线的终点
                mTmpTarget.x = (int) x;
                mTmpTarget.y = (int) y;
                break;
            case MotionEvent.ACTION_UP:
                mTmpTarget.x = mLastPathX;
                mTmpTarget.y = mLastPathY;
                checkSuccess();
                break;
        }
        invalidate();
        return true;
    }

    @Override
    public void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        //绘制GestureLockView间的连线
        if (mPath != null) {
            canvas.drawPath(mPath, mPaint);
        }
        //绘制指引线
        if (choose.size() > 0) {
            if (mLastPathX != 0 && mLastPathY != 0)
                canvas.drawLine(mLastPathX, mLastPathY, mTmpTarget.x,
                        mTmpTarget.y, mPaint);
        }

    }

    /**
     * 检查当前左边是否在child中
     *
     * @param child
     * @param x
     * @param y
     * @return
     */
    private boolean checkPositionInChild(View child, float x, float y) {
        if (x >= child.getLeft() && x <= child.getRight()
                && y >= child.getTop()
                && y <= child.getBottom()) {
            return true;
        }
        return false;
    }

    /**
     * 获取范围内的childView
     *
     * @param x
     * @param y
     * @return
     */
    private LockChildView getChildIdByPos(float x, float y) {
        for (LockChildView gestureLockView : gestureLockViews) {
            if (checkPositionInChild(gestureLockView, x, y)) {
                return gestureLockView;
            }
        }
        return null;
    }

    /**
     * 恢复初始状态
     */
    private void reset() {
        setViewState(LockChildView.STATE_NORMALE);
        mPath.reset();
        choose.clear();
    }

    /**
     * 设置状态
     *
     * @param state
     */
    private void setViewState(@LockChildView.STATE int state) {
        for (LockChildView gestureLockView : gestureLockViews) {
            if (choose.contains(gestureLockView.getId())) {
                gestureLockView.setState(state);
            }
        }
    }

    /**
     * 检查是否正确
     */
    private boolean checkSuccess() {
        if (choose.size() <= 0) {
            return false;
        }

        if (onLockState != null) {
            onLockState.onPassWord(choose);
        }

        /**
         * 如果没有设置密码，不检测结果
         */
        if (psd.size() == 0) {
            return true;
        }

        if (choose.size() != psd.size()) {
            onError();
            return false;
        }
        for (int i = 0; i < choose.size(); i++) {
            if (choose.get(i) != psd.get(i)) {
                onError();
                return false;
            }
        }
        onSuccess();
        return true;
    }

    /**
     * 密码输入错误
     */
    private void onError() {
        setViewState(LockChildView.STATE_ERROR);
        mPaint.setColor(ContextCompat.getColor(getContext(), R.color.topCircleErrorColor));
        mPaint.setAlpha(80);
        tryTimes--;
        if (onLockState != null) {
            onLockState.onError();
        }

        if (tryTimes == 0) {
            reset();
            invalidate();
            if (onLockState != null) {
                onLockState.onUnable();
            }
        }
    }

    /**
     * 输入正确
     */
    private void onSuccess() {
        tryTimes = tryNum;
        if (onLockState != null) {
            onLockState.onSuccess();
        }
    }

    /**
     * 设置密码
     *
     * @param psd
     */
    public void setPsd(List<Integer> psd) {
        this.psd = psd;
        reset();
        invalidate();
    }

    /**
     * 重置，错误次数过多时，可以重置
     */
    public void setReset() {
        tryTimes = tryNum;
        reset();
        invalidate();
    }

    /**
     * 设置数量
     *
     * @param count
     */
    public void setCount(@IntRange(from = 1) int count) {
        mCount = count;
        requestLayout();
    }

    private OnLockState onLockState;

    public void setOnLockState(OnLockState onLockState) {
        this.onLockState = onLockState;
    }

    public interface OnLockState {
        void onError();

        void onSuccess();

        void onUnable();

        void onPassWord(List<Integer> psd);
    }
}
