/*
 * (C) Copyright 2016 HP Development Company, L.P.
 * All Rights Reserved Worldwide
 * 07/29/2016 SMKAB
 *
 * ScanResultAdapter.java
 */

package com.vcreate.ecgchart.scanner;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hp.mobile.scan.sdk.model.ScanPage;
import com.vcreate.ecgchart.R;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ScanResultAdapter extends BaseAdapter {

    private static final String MIME_TYPE_JPEG = "image/jpeg";
    private static final String MIME_TYPE_PDF = "application/pdf";
    private static final String MIME_TYPE_RAW = "application/octet-stream";

    private final Context mContext;
    private final LayoutInflater mLayoutInflater;
    private List<ScanPage> mScanPages = new ArrayList<>();
    private Handler mHandler = new Handler();
    private LruCache<Uri, Bitmap> mPreviewCache;
    private boolean mReleased;

    public ScanResultAdapter(Context aContext) {
        mContext = aContext;
        mLayoutInflater = LayoutInflater.from(aContext);
        mPreviewCache = new LruCache<Uri, Bitmap>(4 * 1024 * 1024) {

            @Override
            protected int sizeOf(Uri aKey, Bitmap aValue) {
                return aValue != null ? aValue.getByteCount() : 0;
            }
        };
    }

    public void add(ScanPage aScanPage) {
        mScanPages.add(aScanPage);
        notifyDataSetChanged();
    }

    public void clear() {
        mScanPages.clear();
        notifyDataSetChanged();
    }

    public void release() {
        mReleased = true;
        mPreviewCache.evictAll();
    }

    @Override
    public int getCount() {
        return mScanPages.size();
    }

    @Override
    public ScanPage getItem(int aPosition) {
        return mScanPages.get(aPosition);
    }

    @Override
    public long getItemId(int aPosition) {
        return aPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getView(int aPosition, View aConvertView, ViewGroup aParent) {
        View theView = aConvertView;
        if (theView == null) {
            theView = mLayoutInflater.inflate(R.layout.scanned_result_item, aParent, false);
        }

        ScanPage theItem = getItem(aPosition);

        ImageView theScannedImageView = (ImageView) theView.findViewById(R.id.scanned_image_view);
        TextView theScannedImageNameView =
                (TextView) theView.findViewById(R.id.scanned_image_name_view);

        theScannedImageView.setImageResource(R.drawable.empty_preview_drawable);

        Uri theUri = theItem.getUri();
        theScannedImageNameView.setText(theUri.getLastPathSegment());

        int theRequiredWidth = mContext.getResources().getDimensionPixelSize(
                R.dimen.scanned_image_preview_width);
        int theRequiredHeight = mContext.getResources().getDimensionPixelSize(
                R.dimen.scanned_image_preview_height);

        if (MIME_TYPE_JPEG.equals(theItem.getMimeType())) {
            Bitmap theBitmap = mPreviewCache.get(theUri);
            if (theBitmap != null) {
                theScannedImageView.setImageBitmap(theBitmap);
            } else {
                ImageLoader theImageLoader = new ImageLoader(theScannedImageView, mHandler,
                        theUri, theRequiredWidth, theRequiredHeight);
                AsyncTask.execute(theImageLoader);
                // keep in tag to detect if this view is still waiting for this image
                theScannedImageView.setTag(theUri);
            }
        } else {
            if (MIME_TYPE_PDF.equals(theItem.getMimeType())) {
                theScannedImageView.setImageResource(R.drawable.ic_pdf);
            } else if (MIME_TYPE_RAW.equals(theItem.getMimeType())) {
                theScannedImageView.setImageResource(R.drawable.ic_raw);
            }
            theScannedImageView.setTag(null);
        }

        return theView;
    }

    private class ImageLoader implements Runnable {
        private static final String TAG = "ImageLoader";

        private ImageView mView;
        private Handler mHandler;
        private Uri mUri;
        private final int mWidth;
        private final int mHeight;

        public ImageLoader(ImageView aView, Handler aHandler, Uri aUri, int aWidth, int aHeight) {
            mView = aView;
            mHandler = aHandler;
            mUri = aUri;
            mWidth = aWidth;
            mHeight = aHeight;
        }

        @Override
        public void run() {
            BitmapFactory.Options theOptions = new BitmapFactory.Options();
            theOptions.inJustDecodeBounds = true;
            InputStream theInputStream = null;
            try {
                theInputStream = mContext.getContentResolver().openInputStream(mUri);

                BitmapFactory.decodeStream(theInputStream, null, theOptions);
                Utils.close(theInputStream);

                int theImageWidth = theOptions.outWidth;
                int theImageHeight = theOptions.outHeight;

                if (theImageWidth <= 0 || theImageHeight <= 0) {
                    Log.d(TAG, "ImageLoader: bad dimensions " + theImageWidth + " "
                            + theImageHeight);
                    return;
                }
                int theScaleX = theImageWidth / mWidth;
                int theScaleY = theImageHeight / mHeight;
                // to handle cases where width match larger than height or otherwise use max scale.
                int theSampleSize = Integer.highestOneBit(Math.max(theScaleX, theScaleY));

                theOptions.inSampleSize = theSampleSize;
                theOptions.inJustDecodeBounds = false;

                theInputStream = mContext.getContentResolver().openInputStream(mUri);
                final Bitmap theBitmap = BitmapFactory.decodeStream(
                        theInputStream, null, theOptions);
                Log.d(TAG, "ImageLoader: " + theBitmap + " inSampleSize: " + theSampleSize
                        + " original w/h: " + theImageWidth + " " + theImageHeight
                        + " required: " + mWidth + " " + mHeight);
                if (theBitmap != null) {
                    Log.d(TAG, "ImageLoader bitmap size: " + theBitmap.getWidth()
                            + " " + theBitmap.getHeight());
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (!mReleased && mUri.equals(mView.getTag())) {
                                mView.setImageBitmap(theBitmap);
                                mPreviewCache.put(mUri, theBitmap);
                            }
                        }
                    });
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } finally {
                Utils.close(theInputStream);
            }
        }
    }
}
