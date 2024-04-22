/*
 * (C) Copyright 2016 HP Development Company, L.P.
 * All Rights Reserved Worldwide
 * 07/20/2016 SMKAB
 *
 * ScannerAdapter.java
 */

package com.vcreate.ecgchart.scanner;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.SSLCertificateSocketFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hp.mobile.scan.sdk.Scanner;
import com.vcreate.ecgchart.R;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;

public class ScannerAdapter extends BaseAdapter {
    private static final String TAG = "ScannerAdapter";

    private List<Scanner> mScanners = new ArrayList<>();
    private ImageLoadingHandler mImageLoadingHandler;
    private LayoutInflater mLayoutInflater;

    public ScannerAdapter(Context aContext) {
        mLayoutInflater = LayoutInflater.from(aContext);
        mImageLoadingHandler = new ImageLoadingHandler(aContext);
    }

    public void add(Scanner aServiceInfo) {
        mScanners.add(aServiceInfo);
        notifyDataSetChanged();
    }

    public void remove(Scanner aServiceInfo) {
        mScanners.remove(aServiceInfo);
        notifyDataSetChanged();
    }

    public void clear() {
        mScanners.clear();
        notifyDataSetChanged();
    }

    public void release() {
        mImageLoadingHandler.release();
    }

    @Override
    public int getCount() {
        return mScanners.size();
    }

    @Override
    public Scanner getItem(int aPosition) {
        return mScanners.get(aPosition);
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
        Scanner theScanner = getItem(aPosition);
        View theView = aConvertView;
        if (theView == null) {
            theView = mLayoutInflater.inflate(R.layout.device_item_view, aParent, false);
        }
        TextView theNameView = (TextView) theView.findViewById(R.id.device_name_view);
        theNameView.setText(theScanner.getHumanReadableName());
        TextView theDescriptionView = (TextView) theView.findViewById(R.id.device_description_view);
        theDescriptionView.setText(theScanner.getModelName());

        ImageView theIconView = (ImageView) theView.findViewById(R.id.device_icon_view);
        theIconView.setImageResource(R.drawable.empty_preview_drawable);

        URL theIconUrl = theScanner.getIconUrl();
        theIconView.setTag(theIconUrl != null ? theIconUrl.toString() : null);
        if (theIconUrl != null) {
            mImageLoadingHandler.loadImage(theIconUrl, theIconView);
        }

        return theView;
    }

    private class ImageLoadingHandler {
        private Map<String, ImageView> mUrlToViewMap = new HashMap<>();
        private Set<String> mLoadingImages = new HashSet<>();
        private final LruCache<String, Bitmap> mImageCache;
        private Handler mHandler = new Handler();
        private boolean mReleased;
        private Context mContext;
        private SSLSocketFactory mSSLSocketFactory;

        public ImageLoadingHandler(Context aContext) {
            mContext = aContext;
            mImageCache = new LruCache<String, Bitmap>(4 * 1024 * 1024) {

                @Override
                protected int sizeOf(String aKey, Bitmap aValue) {
                    return aValue != null ? aValue.getByteCount() : 0;
                }
            };
        }

        public synchronized void loadImage(final URL aIconUrl, ImageView aImageView) {
            final String theStringUrl = aIconUrl.toString();
            mUrlToViewMap.put(theStringUrl, aImageView);
            Bitmap theCachedBitmap = mImageCache.get(theStringUrl);
            if (theCachedBitmap != null) {
                aImageView.setImageBitmap(theCachedBitmap);
            } else if (!mLoadingImages.contains(theStringUrl)) {
                AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
                    @Override
                    public void run() {
                        final Bitmap theLoadedBitmap = loadImageByUrl(aIconUrl);
                        synchronized (ImageLoadingHandler.this) {
                            mLoadingImages.remove(theStringUrl);
                            if (theLoadedBitmap != null) {
                                mImageCache.put(theStringUrl, theLoadedBitmap);
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        synchronized (ImageLoadingHandler.this) {
                                            if (!mReleased) {
                                                ImageView theImageView = mUrlToViewMap.get(
                                                        theStringUrl);
                                                if (theStringUrl.equals(theImageView.getTag())) {
                                                    theImageView.setImageBitmap(theLoadedBitmap);
                                                }
                                            }
                                        }
                                    }
                                });
                            }
                        }
                    }
                });
                mLoadingImages.add(theStringUrl);
            }
        }

        private Bitmap loadImageByUrl(URL aImageUrl) {
            InputStream theInputStream = null;
            try {
                URLConnection theURLConnection = openConnection(aImageUrl);
                theURLConnection.setReadTimeout(30000);
                theURLConnection.setConnectTimeout(30000);
                if (theURLConnection instanceof HttpsURLConnection) {
                    HttpsURLConnection theHttpsURLConnection = (HttpsURLConnection) theURLConnection;
                    theHttpsURLConnection.setHostnameVerifier(new HostnameVerifier() {
                        @SuppressLint("BadHostnameVerifier")
                        @Override
                        public boolean verify(String hostname, SSLSession session) {
                            return true;
                        }
                    });
                    if (mSSLSocketFactory == null) {
                        @SuppressLint("SSLCertificateSocketFactoryGetInsecure")
                        SSLSocketFactory theSocketFactory =
                                SSLCertificateSocketFactory.getInsecure(0, null);
                        mSSLSocketFactory = theSocketFactory;
                    }
                    theHttpsURLConnection.setSSLSocketFactory(mSSLSocketFactory);
                }
                theURLConnection.connect();
                theInputStream = theURLConnection.getInputStream();
                return BitmapFactory.decodeStream(theInputStream);
            } catch (IOException e) {
                Log.d(TAG, "loadImageByUrl: " + aImageUrl, e);
                return null;
            } finally {
                Utils.close(theInputStream);
            }
        }

        private URLConnection openConnection(URL aURL) throws IOException {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                // scanner url request should be performed via wi-fi network, so manually specify
                // network
                ConnectivityManager theConnectivityManager = (ConnectivityManager)
                        mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
                Network[] theAllNetworks = theConnectivityManager.getAllNetworks();
                if (theAllNetworks == null) {
                    throw new IOException("Network info is not available");
                }
                for (Network theNetwork : theAllNetworks) {
                    NetworkInfo theNetworkInfo = theConnectivityManager.getNetworkInfo(theNetwork);
                    if (theNetworkInfo != null
                            && theNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                        if (theNetworkInfo.isConnected() && theNetworkInfo.isAvailable()) {
                            return theNetwork.openConnection(aURL);
                        } else {
                            break;
                        }
                    }
                }
                throw new IOException("Network is not available");
            } else {
                return aURL.openConnection();
            }
        }

        public synchronized void release() {
            mReleased = true;
            mImageCache.evictAll();
            mUrlToViewMap.clear();
            mLoadingImages.clear();
        }
    }

}
