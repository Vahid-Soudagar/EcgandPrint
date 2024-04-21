package com.vcreate.ecgchart;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

public class EcgView extends SurfaceView implements SurfaceHolder.Callback {

    private Paint unitBoxPaint;
    private int mHeight;
    private int mWidth;
    private int totalPixels;
    private Paint gridPaint;
    private int pixelPerBeat;
    private final int totalBeats = 3000;

    private void init() {
        gridPaint = new Paint();
        gridPaint.setColor(ContextCompat.getColor(getContext(), R.color.r2));
        gridPaint.setStrokeWidth(2);
        setBackgroundColor(ContextCompat.getColor(getContext(), R.color.ecg));

    }

    private void drawGridLines(Canvas canvas) {
        // Calculate the number of grid lines
        int numGridLines = 3000; // Total number of grid lines

        // Calculate the spacing between each grid line
        float spacing = (float) totalPixels / numGridLines;

        // Draw the vertical grid lines
        for (int i = 0; i <= numGridLines; i++) {
            float x = i * spacing;
            canvas.drawLine(x, 0, x, totalPixels, gridPaint);
        }

        // Draw the horizontal grid lines
        for (int i = 0; i <= numGridLines; i++) {
            float y = i * spacing;
            canvas.drawLine(0, y, totalPixels, y, gridPaint);
        }
    }


    public EcgView(Context context) {
        super(context);
        init();
    }

    public EcgView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public EcgView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        mWidth = width;
        mHeight = height;
        totalPixels = mWidth * mHeight;
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        mHeight = getHeight();
        mWidth = getWidth();
        totalPixels = mWidth * mHeight;
        pixelPerBeat = totalPixels / totalBeats;
        Log.d("EcgView", mWidth+" ");
        Log.d("EcgView", mHeight+" ");
        Log.d("EcgView", "Total area in pixels: " + totalPixels);
        Log.d("EcgView", "pixelPerBeat: " + pixelPerBeat);
        drawGridLines(canvas);
    }
}

