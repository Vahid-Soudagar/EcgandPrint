/*
 * (C) Copyright 2016 HP Development Company, L.P.
 * All Rights Reserved Worldwide
 * 08/30/2016 SMKAB
 *
 * ScanAreaSetting.java
 */

package com.vcreate.ecgchart.scanner.settings;

import android.graphics.Rect;

public class ScanAreaSetting extends Setting<Rect> {

    private final Rect mSupportedRange;
    private final int mMinWidth;
    private final int mMinHeight;
    private Rect mRect;

    public ScanAreaSetting(String aKey, String aName, Rect aSupportedRange,
            int aMinWidth, int aMinHeight) {
        super(aKey, aName);
        mSupportedRange = new Rect(aSupportedRange);
        mMinWidth = aMinWidth;
        mMinHeight = aMinHeight;
    }

    @Override
    public Rect getValue() {
        return mRect != null ? new Rect(mRect) : null;
    }

    @Override
    public boolean setValue(Rect aValue) {
        Rect theOldValue = mRect;
        if (aValue == null) {
            mRect = null;
            notifySettingChanged(theOldValue, null);
            return true;
        }
        if (mSupportedRange.contains(aValue)
                && mMinWidth <= aValue.width() && mMinHeight <= aValue.height()) {
            if (mRect == null) {
                mRect = new Rect(aValue);
            } else {
                mRect.set(aValue);
            }
            notifySettingChanged(theOldValue, aValue);
            return true;
        }
        return false;
    }

    public int getMinX() {
        return mSupportedRange.left;
    }

    public int getMinY() {
        return mSupportedRange.top;
    }

    public int getMaxX() {
        return mSupportedRange.right;
    }

    public int getMaxY() {
        return mSupportedRange.bottom;
    }

    public int getMinWidth() {
        return mMinWidth;
    }

    public int getMinHeight() {
        return mMinHeight;
    }
}
