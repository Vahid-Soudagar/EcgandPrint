package com.vcreate.ecgchart;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

public class EcgGraphView extends SurfaceView implements SurfaceHolder.Callback {

    private Paint gridPaint;

    public EcgGraphView(Context context) {
        super(context);;
        init();
    }

    public EcgGraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public EcgGraphView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    private void init() {
        getHolder().addCallback(this);
        setBackgroundColor(ContextCompat.getColor(getContext(), R.color.ecg));
        gridPaint = new Paint();
        gridPaint.setColor(ContextCompat.getColor(getContext(), R.color.r1));
        gridPaint.setStrokeWidth(2);
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        // Draw horizontal grid lines
        int numHorizontalLines = 5; // Number of horizontal lines
        float deltaY = (float) getHeight() / (numHorizontalLines + 1); // Gap between each horizontal line
        for (int i = 1; i <= numHorizontalLines; i++) {
            float y = i * deltaY;
            canvas.drawLine(0, y, getWidth(), y, gridPaint);
        }

        // Draw vertical grid lines
        int numVerticalLines = 10; // Number of vertical lines
        float deltaX = (float) getWidth() / (numVerticalLines + 1); // Gap between each vertical line
        for (int i = 1; i <= numVerticalLines; i++) {
            float x = i * deltaX;
            canvas.drawLine(x, 0, x, getHeight(), gridPaint);
        }
    }
}
