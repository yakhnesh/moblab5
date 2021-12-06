package com.uoit.noteme.activites.views;

import android.Manifest;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.uoit.noteme.R;

import java.text.AttributedCharacterIterator;

public class CustomViews extends View {

    private Rect mRect;
    private Paint mPaintSquare;

    private Paint mPaintLine;

    private Paint mPaintCircle;
    private int mSquareColor;
    private int mSquareSize;

    private float mCircleX,mCircleY;
    private float mCircleRadius = 100f;

    public CustomViews(Context context) {
        super(context);
        init(null);
    }

    public CustomViews(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public CustomViews(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    public CustomViews(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    public void init(@Nullable AttributeSet set){
        mRect = new Rect();

        mPaintLine = new Paint();
        mPaintLine.setColor(Color.BLACK);

        mPaintSquare = new Paint();

        mPaintCircle = new Paint();
        mPaintCircle.setAntiAlias(true);
        mPaintCircle.setColor(Color.RED);

        if(set == null) {
            return;
        }

        TypedArray ta = getContext().obtainStyledAttributes(set, R.styleable.CustomViews);

        mSquareColor = ta.getColor(R.styleable.CustomViews_square_color, Color.BLUE);
        mSquareSize = ta.getDimensionPixelSize(R.styleable.CustomViews_square_size, 200);

        ta.recycle();
    }

    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);

        mRect.left= 10;
        mRect.top = 10;
        mRect.right = mRect.left + 100;
        mRect.bottom = mRect.top + 100;

        mPaintSquare.setColor(Color.BLUE);
        canvas.drawRect(mRect,mPaintSquare);

        if (mCircleX == 0f || mCircleY == 0f) {
            mCircleX = getWidth() / 2;
            mCircleY = getHeight() / 2;
        }


        canvas.drawCircle(mCircleX,mCircleY,mCircleRadius,mPaintCircle);


        canvas.drawLine(500,500,50,50,mPaintLine);

    }

    public boolean onTouchEvent(MotionEvent event) {
        boolean value = super.onTouchEvent(event);

        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN: {
                return true;
            }
            case MotionEvent.ACTION_MOVE: {
                float cx = event.getX();
                float cy = event.getY();

                double dcx = Math.pow(cx - mCircleX, 2 );
                double dcy = Math.pow(cy - mCircleY, 2);

                if (dcx + dcy < Math.pow(mCircleRadius,2)){
                    mCircleX = cx;
                    mCircleY = cy;

                    postInvalidate();

                    return true;
                }



                return  value;

            }
        }

        return value;
    }
}

