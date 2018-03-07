package com.cxh.passwordview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import java.util.List;

/**
 * @project JQL
 * @time 2018/3/6/006
 * @desc
 */

public class PasswordView extends View {

    // 颜色,项目默认
    public static final int DEFAULT_COLOR = 0xFFE7E7E7;
    public static final int DEFAULT_SELECTED_COLOR_INNER = 0xFFFF8C37;
    public static final int DEFAULT_SELECTED_COLOR_OUTER = 0xFFFFECD9;
    public static final int DEFAULT_ERROR_COLOR_INNER = 0xFFFC4700;
    public static final int DEFAULT_ERROR_COLOR_OUTER = 0xFFFFE2DA;

    // 内部圆半径
    public static final int CIRCLE_INNER_RADIUS = 9;
    // 外部圆半径
    public static final int CIRCLE_OUTER_RADIUS = 30;
    // 线的粗细
    public static final int SELECTED_LINE_WIDTH = 5;
    // 默认每个单元格的大小，根据measure结果计算
    public static final int DEFAULT_UNIT = 100;
    // 操作完后的延时
    public static final int DELAY_TIME = 1000;

    private int defaultColor = DEFAULT_COLOR;
    private int selectedColorInner = DEFAULT_SELECTED_COLOR_INNER;
    private int selectedColorOuter = DEFAULT_SELECTED_COLOR_OUTER;
    private int errorColorInner = DEFAULT_ERROR_COLOR_INNER;
    private int errorColorOuter = DEFAULT_ERROR_COLOR_OUTER;
    private int circleRadiusInner;
    private int circleRadiusOuter;
    private int lineStroke;
    // 把九宫格分成三行三列格子
    private int unitSize;

    private boolean isError = false;
    private boolean isPressed = false;
    // 操作完后的延时
    private boolean isDelaying = false;
    private float mTouchX;
    private float mTouchY;
    private float mPressedX;
    private float mPressedY;

    // 画圆
    private Paint mPaintCicle;
    // 画线
    private Paint mPaintLine;

    private OnFinishedListener mListener;

    // 存放点
    private final List<PasswordPoint> mPoints = new java.util.ArrayList<>();
    private final List<PasswordPoint> mSelectedPoints = new java.util.ArrayList<>();

    // 密码
    private String mPassword = "";


    public PasswordView(Context context) {
        this(context, null);
    }

    public PasswordView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PasswordView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setBackgroundColor(Color.WHITE);
        // 默认数据
        circleRadiusInner = (int) getDptoPx(context, CIRCLE_INNER_RADIUS);
        circleRadiusOuter = (int) getDptoPx(context, CIRCLE_OUTER_RADIUS);
        lineStroke = (int) getDptoPx(context, SELECTED_LINE_WIDTH);
        // 获取自定义属性
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.PasswordView);
        int styleCount = ta.getIndexCount();
        for (int i = 0; i < styleCount; i++) {
            int index = ta.getIndex(i);
            if (index == R.styleable.PasswordView_gesture_default_color) {
                defaultColor = ta.getColor(R.styleable.PasswordView_gesture_default_color,
                        DEFAULT_COLOR);

            } else if (index == R.styleable.PasswordView_gesture_selected_circle_inner_color) {
                selectedColorInner = ta.getColor(R.styleable
                                .PasswordView_gesture_error_circle_inner_color,
                        DEFAULT_SELECTED_COLOR_INNER);

            } else if (index == R.styleable.PasswordView_gesture_selected_circle_outer_color) {
                selectedColorOuter = ta.getColor(R.styleable
                                .PasswordView_gesture_selected_circle_outer_color,
                        DEFAULT_SELECTED_COLOR_OUTER);

            } else if (index == R.styleable.PasswordView_gesture_error_circle_inner_color) {
                errorColorInner = ta.getColor(R.styleable
                                .PasswordView_gesture_error_circle_inner_color,
                        DEFAULT_ERROR_COLOR_INNER);

            } else if (index == R.styleable.PasswordView_gesture_error_circle_outer_color) {
                errorColorOuter = ta.getColor(R.styleable
                                .PasswordView_gesture_error_circle_outer_color,
                        DEFAULT_ERROR_COLOR_OUTER);

            } else if (index == R.styleable.PasswordView_gesture_circle_inner_radius) {
                circleRadiusInner = (int) ta.getDimension(R.styleable
                        .PasswordView_gesture_circle_inner_radius, getDptoPx(context,
                        CIRCLE_INNER_RADIUS));

            } else if (index == R.styleable.PasswordView_gesture_circle_outer_radius) {
                circleRadiusOuter = (int) ta.getDimension(R.styleable
                        .PasswordView_gesture_circle_outer_radius, getDptoPx(context,
                        CIRCLE_OUTER_RADIUS));

            } else if (index == R.styleable.PasswordView_gesture_line_stroke) {
                lineStroke = (int) ta.getDimension(R.styleable
                        .PasswordView_gesture_line_stroke, getDptoPx(context,
                        SELECTED_LINE_WIDTH));
            }
        }
        ta.recycle();

        // 初始化画笔
        mPaintCicle = new Paint();
        mPaintCicle.setAntiAlias(true);
        mPaintCicle.setDither(true);
        mPaintCicle.setStyle(Paint.Style.FILL);

        mPaintLine = new Paint();
        mPaintLine.setAntiAlias(true);
        mPaintLine.setDither(true);
        mPaintLine.setStrokeWidth(lineStroke);
        mPaintLine.setStyle(Paint.Style.FILL);
        mPaintLine.setStrokeCap(Paint.Cap.ROUND);

        // 添加点
        mPoints.clear();
        for (int i = 0; i < 9; i++) {
            mPoints.add(new PasswordPoint());
        }
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getMeasureValue(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getMode
                (widthMeasureSpec));
        int height = getMeasureValue(MeasureSpec.getSize(heightMeasureSpec), MeasureSpec.getMode
                (heightMeasureSpec));
        setMeasuredDimension(width, height);
        // 如果宽度和高度小于指定高度，什么都不画
        unitSize = Math.min(width, height) / 3;
        int mCenterX = getMeasuredWidth() / 2;
        int mCenterY = getMeasuredHeight() / 2;
        // 初始化点
        mPoints.get(0).set(mCenterX - unitSize, mCenterY - unitSize, '1');
        mPoints.get(1).set(mCenterX, mCenterY - unitSize, '2');
        mPoints.get(2).set(mCenterX + unitSize, mCenterY - unitSize, '3');
        mPoints.get(3).set(mCenterX - unitSize, mCenterY, '4');
        mPoints.get(4).set(mCenterX, mCenterY, '5');
        mPoints.get(5).set(mCenterX + unitSize, mCenterY, '6');
        mPoints.get(6).set(mCenterX - unitSize, mCenterY + unitSize, '7');
        mPoints.get(7).set(mCenterX, mCenterY + unitSize, '8');
        mPoints.get(8).set(mCenterX + unitSize, mCenterY + unitSize, '9');
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 先画出外圆
        for (PasswordPoint point : mSelectedPoints) {
            if (isError) {
                mPaintCicle.setColor(errorColorOuter);
            } else {
                mPaintCicle.setColor(selectedColorOuter);
            }
            canvas.drawCircle(point.getX(), point.getY(), circleRadiusOuter, mPaintCicle);
        }

        /**
         *  当按下时，至少有一个点才有线
         *  当按下结束时，至少两个点才能连成线
         *
         */
        if (mSelectedPoints.size() > 0) {
            if (isError) {
                mPaintLine.setColor(errorColorInner);
            } else {
                mPaintLine.setColor(selectedColorInner);
            }
            // 两个点才能形成线
            if (mSelectedPoints.size() >= 2) {
                for (int i = 1; i < mSelectedPoints.size(); i++) {
                    drawPointToPoint(canvas, mSelectedPoints.get(i), mSelectedPoints.get(i - 1),
                            mPaintLine);
                }
            }

            // 最后一个点与手指之间的线
            if (mSelectedPoints.size() < 9 && isPressed) {
                PasswordPoint point = mSelectedPoints.get(mSelectedPoints.size() - 1);
                if (!touchPoint(point, mTouchX, mTouchY)) {
                    drawPointToPoint(canvas, point, (int) mTouchX, (int) mTouchY, mPaintLine);
                }
            }
        }

        // 画内圆
        for (PasswordPoint point : mPoints) {
            if (point.isSelected()) {
                if (isError) {
                    mPaintCicle.setColor(errorColorInner);
                } else {
                    mPaintCicle.setColor(selectedColorInner);
                }
            } else {
                mPaintCicle.setColor(defaultColor);
            }
            canvas.drawCircle(point.getX(), point.getY(), circleRadiusInner, mPaintCicle);
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                if (!isDelaying) {

                    isPressed = true;
                    mPressedX = event.getX();
                    mPressedY = event.getY();
                    mTouchX = mPressedX;
                    mTouchY = mPressedY;

                    for (PasswordPoint point : mPoints) {
                        if (touchPoint(point, mTouchX, mTouchY) && !point.isSelected() &&
                                !mSelectedPoints.contains(point)) {
                            point.setSelected(true);
                            mSelectedPoints.add(point);
                            mPassword += point.getCh();
                            if(null != mListener){
                                mListener.selected(point.getCh());
                            }
                        }
                    }
                    invalidateView();
                }
                return isPressed;
            }
            case MotionEvent.ACTION_MOVE: {
                if (!isDelaying) {

                    mTouchX = event.getX();
                    mTouchY = event.getY();

                    for (PasswordPoint point : mPoints) {
                        if (touchPoint(point, mTouchX, mTouchY) && !point.isSelected() &&
                                !mSelectedPoints.contains(point)) {
                            point.setSelected(true);
                            mSelectedPoints.add(point);
                            mPassword += point.getCh();
                            if(null != mListener){
                                mListener.selected(point.getCh());
                            }
                        }
                    }
                    invalidateView();
                }
            }
            break;
            case MotionEvent.ACTION_UP: {
                if (!isDelaying) {
                    isPressed = false;
                    isDelaying = true;
                    // 回调结果
                    if(null != mListener){
                        mListener.onResult(mPassword);
                    }
                    // 回调结果
                    clearSelectedPointDelay();
                    invalidateView();
                }
            }
            break;
        }
        return super.onTouchEvent(event);
    }


    /**
     * Author: cxh
     * Time  : 2018/3/6/006 15:01
     * Desc  : 清除选中并延迟一定时间
     */
    private void clearSelectedPointDelay() {

        postDelayed(new Runnable() {
            @Override
            public void run() {
                clearSelectedPoint();
            }
        }, DELAY_TIME);
    }


    /**
     * Author: cxh
     * Time  : 2018/3/6/006 15:04
     * Desc  :
     */
    private void clearSelectedPoint() {

        for (PasswordPoint point : mPoints) {
            point.setSelected(false);
        }
        mSelectedPoints.clear();
        mPassword = "";
        isError = false;
        isDelaying = false;
        mPressedX = 0;
        mPressedY = 0;
        mTouchX = 0;
        mTouchY = 0;
        invalidateView();
    }

    /**
     * Author: cxh
     * Time  : 2018/3/6/006 14:12
     * Desc  : 画线
     */
    private void drawPointToPoint(Canvas canvas, PasswordPoint from, PasswordPoint to, Paint
            paint) {
        drawPointToPoint(canvas, from, to.getX(), to.getY(), paint);
    }

    /**
     * Author: cxh
     * Time  : 2018/3/6/006 14:10
     * Desc  :
     */
    private void drawPointToPoint(Canvas canvas, PasswordPoint from, int px, int py, Paint paint) {
        canvas.drawLine(from.getX(), from.getY(), px, py, paint);
    }

    /**
     * Author: cxh
     * Time  : 2018/3/6/006 11:13
     * Desc  : 标准模式
     */
    private int getMeasureValue(int measure, int mode) {
        int result = measure;
        if (mode == MeasureSpec.AT_MOST) {
            result = (int) (3 * getDptoPx(getContext(), DEFAULT_UNIT));
        }
        return result;
    }

    /**
     * Author: cxh
     * Time  : 2018/3/6/006 10:34
     * Desc  :
     */
    private float getDptoPx(Context context, int dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources()
                .getDisplayMetrics());
    }


    /**
     * Author: cxh
     * Time  : 2018/3/6/006 12:00
     * Desc  : 更新UI
     */
    private void invalidateView() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            invalidate();
        } else {
            postInvalidate();
        }
    }


    /**
     * Author: cxh
     * Time  : 2018/3/6/006 14:34
     * Desc  : 两点间距离
     */
    private float getDistance(float x1, float y1, float x2, float y2) {
        if (x1 == x2 && y1 == y2) {
            return 0;
        }
        return (float) Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
    }


    /**
     * Author: cxh
     * Time  : 2018/3/6/006 14:35
     * Desc  :
     */
    private boolean touchPoint(PasswordPoint point, float x, float y) {
        if (getDistance(point.getX(), point.getY(), x, y) <= circleRadiusOuter) {
            return true;
        }
        return false;
    }

    /**
     * Author: cxh
     * Time  : 2018/3/7/007 10:12
     * Desc  : 回调
     */
    public interface OnFinishedListener {
        void selected(char ch);

        void onResult(String password);
    }

    /**
     * 每个圆圈对应的点
     * 点存储字符，对应相应位置
     */
    private static class PasswordPoint {

        private int x;
        private int y;
        private char ch;
        private boolean selected = false;

        public void set(int x, int y, char c) {
            this.x = x;
            this.y = y;
            this.ch = c;
        }

        /**
         * 两点间距离
         * <p>
         * Author : cxh
         * Create Time : 2017/8/15 0015 下午 4:13
         * Modify :
         */
        public float getDistance(float x1, float y1, float x2, float y2) {
            if (x1 == x2 && y1 == y2) {
                return 0;
            }
            return (float) Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
        }

        public char getCh() {
            return ch;
        }

        public void setCh(char ch) {
            this.ch = ch;
        }

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }
    }

    /**
     * Author: cxh
     * Time  : 2018/3/7/007 10:14
     * Desc  : 手势操作后设置密码错误
     */
    public void setPasswordError(boolean error) {
        // 只有在延时时才能设置错误
        if(isDelaying) {
            this.isError = error;
            invalidateView();
        }
    }

    /**
     * Author: cxh
     * Time  : 2018/3/7/007 10:17
     * Desc  : 回调
     */
    public void setListener(OnFinishedListener listener) {
        mListener = listener;
    }
}
