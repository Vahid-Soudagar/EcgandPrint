package com.vcreate.ecgchart;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

public class EcgView extends SurfaceView implements SurfaceHolder.Callback {

    private Paint linePaint;
    private int beatsPerPixel = 10; // Number of beats represented by each pixel
    private int pixelsPerBeat = 10; // Number of pixels representing each beat
    private int backgroundColor = Color.WHITE;
    private int graphColor = Color.BLACK;

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

    private void init() {
        // Initialize the line paint for drawing
        linePaint = new Paint();
        linePaint.setColor(graphColor);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(2);
        linePaint.setAntiAlias(true);

        // Register SurfaceHolder callback
        getHolder().addCallback(this);
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        // Draw the graph when the surface is created
        drawGraph();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        // Redraw the graph when the surface size changes
        drawGraph();
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        // Surface destroyed, no action needed
    }

    private void drawGraph() {
        Canvas canvas = getHolder().lockCanvas(); // Get the canvas for drawing
        if (canvas != null) {
            // Clear the canvas with the background color
            canvas.drawColor(backgroundColor);

            // Draw horizontal lines representing beats
            int numBeats = canvas.getWidth() / pixelsPerBeat;
            int startY = canvas.getHeight() / 2; // Start drawing from the middle of the canvas
            int endY = startY;
            for (int i = 0; i < numBeats; i++) {
                canvas.drawLine(i * pixelsPerBeat, startY, (i + 1) * pixelsPerBeat, endY, linePaint);
            }

            // Unlock the canvas to show the changes
            getHolder().unlockCanvasAndPost(canvas);
        }
    }
}
