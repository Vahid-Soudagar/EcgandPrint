/*
 * (C) Copyright 2016 HP Development Company, L.P.
 * All Rights Reserved Worldwide
 * 08/29/2016 SMKAB
 *
 * ScanSettings.java
 */

package com.vcreate.ecgchart.scanner.settings;

import com.hp.mobile.scan.sdk.model.ScanTicket;
import com.hp.mobile.scan.sdk.model.ScanValues;

import java.util.ArrayList;
import java.util.List;

public class ScanSettings {
    private ChoiceSetting<Integer> mInputSourceChoiceSetting;
    private List<Setting> mPlatenSettings;
    private List<Setting> mCameraSettings;
    private List<Setting> mAdfSimplexSettings;
    private List<Setting> mAdfDuplexSettings;
    private BooleanSetting mDuplexSetting;
    private List<Setting> mGeneralSettings;

    public ScanSettings(ChoiceSetting<Integer> aInputSourceChoiceSetting,
            List<Setting> aPlatenSettings, List<Setting> aCameraSettings,
            List<Setting> aAdfSimplexSettings, List<Setting> aAdfDuplexSettings,
            BooleanSetting aDuplexSetting, List<Setting> aGeneralSettings) {
        mInputSourceChoiceSetting = aInputSourceChoiceSetting;
        mPlatenSettings = aPlatenSettings;
        mCameraSettings = aCameraSettings;
        mAdfSimplexSettings = aAdfSimplexSettings;
        mAdfDuplexSettings = aAdfDuplexSettings;
        mDuplexSetting = aDuplexSetting;
        mGeneralSettings = aGeneralSettings;
    }

    public ChoiceSetting<Integer> getInputSourceChoiceSetting() {
        return mInputSourceChoiceSetting;
    }

    public Integer getCurrentInputSource() {
        return mInputSourceChoiceSetting != null ? mInputSourceChoiceSetting.getValue() : null;
    }

    public void updateSettings(ScanTicket aScanTicket) {
        Integer theInputSource = aScanTicket != null ? aScanTicket.getInputSource() : null;
        boolean theDuplex = aScanTicket != null &&
                aScanTicket.getBoolean(ScanTicket.SCAN_SETTING_DUPLEX);
        ScanSettingsHelper.updateSettings(aScanTicket, mInputSourceChoiceSetting,
                getSettingsForInputSource(theInputSource, theDuplex), mGeneralSettings);
    }

    public void updateCurrentInputSourceSettings(Integer aSourceInputSource, Boolean aSourceDuplex) {
        ScanSettingsHelper.updateSettings(
                getSettingsForInputSource(getCurrentInputSource(), getDuplex()),
                getSettingsForInputSource(aSourceInputSource, aSourceDuplex));
    }

    public List<Setting> createCurrentSettingsList() {
        List<Setting> theSettings = new ArrayList<>();
        theSettings.add(mInputSourceChoiceSetting);

        Integer theSelectedInputSource = mInputSourceChoiceSetting.getValue();
        List<Setting> theSettingsForSelectedInputSource = getSettingsForInputSource(
                theSelectedInputSource, getDuplex());

        if (theSettingsForSelectedInputSource != null) {
            theSettings.addAll(theSettingsForSelectedInputSource);
        }
        if (mGeneralSettings != null) {
            theSettings.addAll(mGeneralSettings);
        }
        return theSettings;
    }

    public Boolean getDuplex() {
        return mDuplexSetting != null ? mDuplexSetting.getValue() : null;
    }

    private List<Setting> getSettingsForInputSource(Integer aInputSource, Boolean aDuplex) {
        if (aInputSource == null) {
            return null;
        }
        switch (aInputSource) {
            case ScanValues.INPUT_SOURCE_PLATEN:
                return mPlatenSettings;
            case ScanValues.INPUT_SOURCE_CAMERA:
                return mCameraSettings;
            case ScanValues.INPUT_SOURCE_ADF:
                if (Boolean.TRUE.equals(aDuplex)) {
                    return mAdfDuplexSettings;
                } else {
                    return mAdfSimplexSettings;
                }
            case ScanValues.INPUT_SOURCE_AUTO:
            default:
                return null;
        }
    }
}
