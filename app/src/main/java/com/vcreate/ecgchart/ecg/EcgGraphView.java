/**
 * EcgGraphView is a custom view for displaying an ECG graph with square grid lines.
 * Author: Vahid Soudagar
 */

package com.vcreate.ecgchart.ecg;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.vcreate.ecgchart.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class EcgGraphView extends SurfaceView implements SurfaceHolder.Callback {

    private Paint gridPaint;
    private int mHeight;
    private int mWidth;

    private boolean isSurfaceViewAvailable;
    private int mDataBufferIndex;
    private ArrayList<Float> list;

    private float cellSize = 0;


    public EcgGraphView(Context context) {
        this(context, null);
        init();
    }

    public EcgGraphView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        init();
    }

    public EcgGraphView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        isSurfaceViewAvailable = true;

    }

    private void drawGridLines(Canvas canvas) {
         // Define graph dimensions
        float graphWidthMM = 250; // in mm
        float graphHeightMM = 40; // in mm

        // Convert mm to pixels
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        float dpi = metrics.xdpi;
        float pixelsPerMM = dpi / 25.4f; // 1 inch = 25.4 mm
        int graphWidth = Math.round(graphWidthMM * pixelsPerMM);
        int graphHeight = Math.round(graphHeightMM * pixelsPerMM);

        // Ensure landscape orientation
        int canvasWidth = getWidth();
        int canvasHeight = getHeight();
        if (canvasWidth < canvasHeight) {
            // Swap canvasWidth and canvasHeight for landscape orientation
            int temp = canvasWidth;
            canvasWidth = canvasHeight;
            canvasHeight = temp;
        }

        // Calculate the number of horizontal and vertical lines
        int horizontalLines = (int) (graphHeightMM / 5); // Each line represents 5mm
        int verticalLines = (int) (graphWidthMM / 5); // Each line represents 5mm

        // Calculate spacing between grid lines
        float deltaX = (float) graphWidth / verticalLines;
        float deltaY = (float) graphHeight / horizontalLines;

        // Ensure equal spacing for square grid cells
        cellSize = Math.min(deltaX, deltaY);
        deltaX = cellSize;
        deltaY = cellSize;

        Log.d("TestData", "Length of each square (mm): " + cellSize);

        // Draw horizontal grid lines
        for (int i = 0; i <= horizontalLines; i++) {
            float y = i * deltaY;
            canvas.drawLine(0, y, canvasWidth, y, gridPaint);
        }

        // Draw vertical grid lines
        for (int i = 0; i <= verticalLines; i++) {
            float x = i * deltaX;
            canvas.drawLine(x, 0, x, canvasHeight, gridPaint);
        }
    }


    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        drawGridLines(canvas);
        drawWaveform(canvas);
    }




    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        mWidth = width;
        mHeight = height;
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = (MeasureSpec.getSize(widthMeasureSpec));
        if (width > mWidth) mWidth = width;
        int height = (int) (MeasureSpec.getSize(heightMeasureSpec) * 0.95);
        if (height > mHeight) mHeight = height;
    }

    private void init() {
        mDataBufferIndex = 0;

        setBackgroundColor(ContextCompat.getColor(getContext(), R.color.ecg));
        gridPaint = new Paint();
        gridPaint.setColor(ContextCompat.getColor(getContext(), R.color.r1));
        gridPaint.setStrokeWidth(1);
    }

    public void drawWaveform(Canvas canvas) {
        // use the list of points and draw the graph here
        if (list != null && !list.isEmpty()) {
            Paint waveformPaint = new Paint();
            waveformPaint.setColor(ContextCompat.getColor(getContext(), R.color.black));
            waveformPaint.setStrokeWidth(2);
            waveformPaint.setAntiAlias(true);


            float gridSize = getCellSize();
            float gridSizeInMillivolts = 1.0f;

            // Define the scale for mapping millivolts to pixels
            float millivoltsPerPixel = gridSizeInMillivolts / gridSize;



            // Define the initial position of the waveform
            float startX = 0;
            float startY = getHeight() / 2f;

            // Iterate through the list of points and draw the waveform
            for (int i = 0; i < list.size() - 1; i++) {
                float x1 = startX + i;
                float y1 = startY - (list.get(i) / millivoltsPerPixel);
                float x2 = startX + i + 1;
                float y2 = startY - (list.get(i + 1) / millivoltsPerPixel);

                canvas.drawLine(x1, y1, x2, y2, waveformPaint);
            }
        }
    }

    public void addAmp(@NotNull ArrayList<Float> millivoltsList) {
        this.list = millivoltsList;
    }

    private float getCellSize() {
        return cellSize;
    }
}
