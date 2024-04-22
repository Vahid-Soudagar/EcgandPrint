/*
 * (C) Copyright 2016 HP Development Company, L.P.
 * All Rights Reserved Worldwide
 * 08/17/2016 SMKAB
 *
 * SettingsAdapter.java
 */

package com.vcreate.ecgchart.scanner.settings;

import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.vcreate.ecgchart.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class SettingsAdapter extends BaseAdapter {

    private static final int CHOICE_TYPE = 0;
    private static final int BOOLEAN_TYPE = 1;
    private static final int SCAN_AREA_TYPE = 2;

    private List<Setting> mSettings = new ArrayList<>();

    public SettingsAdapter() {
    }

    public List<Setting> getSettings() {
        return mSettings;
    }

    public void replaceSettings(Collection<Setting> aSettings) {
        mSettings.clear();
        mSettings.addAll(aSettings);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mSettings.size();
    }

    @Override
    public Setting getItem(int position) {
        return mSettings.get(position);
    }

    @Override
    public int getItemViewType(int position) {
        Setting theItem = getItem(position);
        if (theItem instanceof ChoiceSetting) {
            return CHOICE_TYPE;
        } else if (theItem instanceof BooleanSetting) {
            return BOOLEAN_TYPE;
        } else if (theItem instanceof ScanAreaSetting) {
            return SCAN_AREA_TYPE;
        } else {
            throw new RuntimeException("Unsupported setting type: " + theItem);
        }
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View theView = convertView;
        Setting theItem = getItem(position);
        int theViewType = getItemViewType(position);
        if (theView == null) {
            theView = inflateView(LayoutInflater.from(parent.getContext()), parent, theViewType);
        }
        TextView theNameView = (TextView) theView.findViewById(R.id.setting_name_view);
        theNameView.setText(theItem.getName());
        updateView(theView, theItem);
        return theView;
    }

    private View inflateView(LayoutInflater aInflater, ViewGroup aParent, int aType) {
        switch (aType) {
            case CHOICE_TYPE:
                return aInflater.inflate(R.layout.choice_setting_item, aParent, false);
            case BOOLEAN_TYPE:
                return aInflater.inflate(R.layout.boolean_setting_item, aParent, false);
            case SCAN_AREA_TYPE:
                return aInflater.inflate(R.layout.scan_area_setting_item, aParent, false);
            default:
                throw new RuntimeException("Unsupported type " + aType);
        }
    }

    private void updateView(View aView, Setting aSetting) {
        if (aSetting instanceof ChoiceSetting) {
            ChoiceSetting theChoiceSetting = (ChoiceSetting) aSetting;
            String theValueName = theChoiceSetting.getValueName();

            TextView theValueView = (TextView) aView.findViewById(R.id.setting_value_view);
            if (theValueName != null) {
                theValueView.setText(theValueName);
            } else {
                theValueView.setText(R.string.choose_setting);
            }
        } else if (aSetting instanceof BooleanSetting) {
            final BooleanSetting theBooleanSetting = (BooleanSetting) aSetting;

            Switch theSwitch = (Switch) aView.findViewById(R.id.setting_switch_view);
            theSwitch.setOnCheckedChangeListener(null);
            theSwitch.setChecked(Boolean.TRUE.equals(theBooleanSetting.getValue()));
            theSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    theBooleanSetting.setValue(isChecked);
                }
            });
        } else if (aSetting instanceof ScanAreaSetting) {
            ScanAreaSetting theScanAreaSetting = (ScanAreaSetting) aSetting;
            Rect theValue = theScanAreaSetting.getValue();
            TextView theValueView = (TextView) aView.findViewById(R.id.setting_value_view);
            if (theValue != null) {
                theValueView.setText(formatRectAreaInInches(theScanAreaSetting.getValue()));
            } else {
                theValueView.setText(R.string.choose_setting);
            }
        } else {
            throw new RuntimeException("Unsupported setting type " + aSetting);
        }
    }

    private String formatRectAreaInInches(Rect aRect) {
        return String.format(Locale.ENGLISH,
                "x: %.2f, y: %.2f, width: %.2f, height: %.2f",
                (float) aRect.left / 300, (float) aRect.top / 300,
                (float) aRect.width() / 300, (float) aRect.height() / 300);
    }
}