/*
 * (C) Copyright 2016 HP Development Company, L.P.
 * All Rights Reserved Worldwide
 * 08/17/2016 SMKAB
 *
 * ChoiceSetting.java
 */

package com.vcreate.ecgchart.scanner.settings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ChoiceSetting<T> extends Setting<T> {

    private String mName;
    private List<String> mChoiceNames;
    private List<T> mChoiceValues;
    private int mValueIndex = -1;

    public ChoiceSetting(String aKey, String aName, Collection<String> aChoiceNames,
            Collection<T> aChoiceValues) {
        super(aKey, aName);
        mName = aName;
        if (aChoiceNames.size() != aChoiceValues.size()) {
            throw new IllegalArgumentException("Choice names and values size should be the same");
        }
        mChoiceNames = Collections.unmodifiableList(new ArrayList<>(aChoiceNames));
        mChoiceValues = Collections.unmodifiableList(new ArrayList<>(aChoiceValues));
    }

    public String getName() {
        return mName;
    }

    public List<String> getChoiceNames() {
        return mChoiceNames;
    }

    public List<T> getChoiceValues() {
        return mChoiceValues;
    }

    @Override
    public T getValue() {
        if (mValueIndex >= 0) {
            return mChoiceValues.get(mValueIndex);
        }
        return null;
    }

    public String getValueName() {
        if (mValueIndex >= 0) {
            return mChoiceNames.get(mValueIndex);
        }
        return null;
    }

    public int getValueIndex() {
        return mValueIndex;
    }

    public void setValueIndex(int aValueIndex) {
        if (aValueIndex < mChoiceValues.size()) {
            T theOldValue = getValue();
            mValueIndex = aValueIndex;
            notifySettingChanged(theOldValue, getValue());
        }
    }

    @Override
    public boolean setValue(T aValue) {
        int theValueIndex = mChoiceValues.indexOf(aValue);
        if (theValueIndex >= 0) {
            setValueIndex(theValueIndex);
            return true;
        } else if (aValue == null) {
            setValueIndex(-1);
            return true;
        }
        return false;
    }

}
