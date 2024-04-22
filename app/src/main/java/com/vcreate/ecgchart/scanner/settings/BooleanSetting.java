/*
 * (C) Copyright 2016 HP Development Company, L.P.
 * All Rights Reserved Worldwide
 * 08/29/2016 SMKAB
 *
 * BooleanSetting.java
 */

package com.vcreate.ecgchart.scanner.settings;

public class BooleanSetting extends Setting<Boolean> {

    private Boolean mValue;

    public BooleanSetting(String aKey, String aName) {
        super(aKey, aName);
    }

    @Override
    public Boolean getValue() {
        return mValue;
    }

    @Override
    public boolean setValue(Boolean aValue) {
        Boolean theOldValue = mValue;
        mValue = aValue;
        notifySettingChanged(theOldValue, aValue);
        return true;
    }
}
