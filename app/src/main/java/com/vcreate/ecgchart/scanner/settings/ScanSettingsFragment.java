/*
 * (C) Copyright 2016 HP Development Company, L.P.
 * All Rights Reserved Worldwide
 * 08/18/2016 SMKAB
 *
 * ScanSettingsFragment.java
 */

package com.vcreate.ecgchart.scanner.settings;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.hp.mobile.scan.sdk.ScannerCapabilitiesFetcher;
import com.hp.mobile.scan.sdk.ScanTicketValidator;
import com.hp.mobile.scan.sdk.Scanner;
import com.hp.mobile.scan.sdk.ScannerException;
import com.hp.mobile.scan.sdk.ValidationException;
import com.hp.mobile.scan.sdk.model.ScannerCapabilities;
import com.hp.mobile.scan.sdk.model.ScanTicket;
import com.vcreate.ecgchart.R;
import com.vcreate.ecgchart.scanner.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class ScanSettingsFragment extends Fragment {

    private Scanner mScanner;
    private ScannerCapabilities mScannerCapabilities;
    private ScanTicket mScanTicket;
    private ScanSettings mScanSettings;
    private SettingsAdapter mSettingsAdapter;
    private ScannerCapabilitiesFetcher.ScannerCapabilitiesListener mCapabilitiesListener =
            new ScannerCapabilitiesFetcher.ScannerCapabilitiesListener() {
                @Override
                public void onFetchCapabilities(ScannerCapabilities aCapabilities) {
                    mScannerCapabilities = aCapabilities;

                    if (isAdded()) {
                        updateScanSettings();

                        mRetryContainer.setVisibility(View.GONE);
                        mProgressMessageView.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onFetchCapabilitiesError(ScannerException aException) {
                    Activity theContext = getActivity();
                    if (theContext != null) {
                        Toast.makeText(theContext, "onFetchCapabilitiesError " + aException,
                                Toast.LENGTH_LONG).show();

                        mRetryContainer.setVisibility(View.VISIBLE);

                        mRetryMessageView.setText(String.format(Locale.getDefault(),
                                "Fetching exception, reason: %d, message: %s, cause: %s",
                                aException.getReason(), aException.getMessage(),
                                aException.getCause()));
                        mProgressMessageView.setVisibility(View.GONE);
                    }
                }
            };
    private TextView mProgressMessageView;
    private OnSettingsSelectedListener mListener;
    private View mRetryContainer;
    private TextView mRetryMessageView;
    private View mValidationProgressView;
    private Setting.SettingChangedListener<Integer> mInputSourceChangedListener =
            new Setting.SettingChangedListener<Integer>() {
                @Override
                public void onSettingChanged(Integer aOld, Integer aNew) {
                    if (!Objects.equals(aOld, aNew)) {
                        validateScanTicket(aOld, mScanSettings.getDuplex());
                    }
                }
            };
    private Setting.SettingChangedListener<Boolean> mDuplexChangedListener =
            new Setting.SettingChangedListener<Boolean>() {
                @Override
                public void onSettingChanged(Boolean aOld, Boolean aNew) {
                    if (!Objects.equals(aOld, aNew)) {
                        validateScanTicket(mScanSettings.getCurrentInputSource(), aOld);
                    }
                }
            };
    private Setting.SettingChangedListener<Rect> mScanAreaChangedListener =
            new Setting.SettingChangedListener<Rect>() {
                @Override
                public void onSettingChanged(Rect aOld, Rect aNew) {
                    if (aOld == null || aNew == null) {
                        updateScanSettings();
                    }
                }
            };

    public ScanSettingsFragment() {
    }

    public void setScanner(Scanner aScanner) {
        mScanner = aScanner;
    }

    public void setScanTicket(ScanTicket aScanTicket) {
        mScanTicket = aScanTicket;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View theRootView = inflater.inflate(R.layout.fragment_scan_settings, container, false);
        ListView theSettingsView = (ListView) theRootView.findViewById(R.id.scan_settings_view);
        mProgressMessageView = (TextView) theRootView.findViewById(R.id.progress_message_view);
        mRetryContainer = theRootView.findViewById(R.id.retry_container);
        View theRetryButton = mRetryContainer.findViewById(R.id.retry_button);
        mRetryMessageView = (TextView) mRetryContainer.findViewById(R.id.retry_message_view);
        theRetryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startFetchingCapabilities();
            }
        });
        View theDoneButton = theRootView.findViewById(R.id.done_button);
        theDoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onSettingsSelected(createScanTicket());
            }
        });
        mValidationProgressView = theRootView.findViewById(R.id.validation_progress_view);
        mValidationProgressView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        mSettingsAdapter = new SettingsAdapter();
        theSettingsView.setAdapter(mSettingsAdapter);

        updateScanSettings();

        theSettingsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Setting theSetting = (Setting) parent.getItemAtPosition(position);
                if (theSetting instanceof ChoiceSetting) {
                    final ChoiceSetting theChoiceSetting = (ChoiceSetting) theSetting;
                    List theChoiceNames = theChoiceSetting.getChoiceNames();
                    String[] theChoices = (String[]) theChoiceNames.toArray(
                            new String[theChoiceNames.size()]);
                    AlertDialog theAlertDialog = new AlertDialog.Builder(getActivity())
                            .setSingleChoiceItems(
                                    theChoices, theChoiceSetting.getValueIndex(),
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            theChoiceSetting.setValueIndex(which);
                                            mSettingsAdapter.notifyDataSetChanged();
                                            dialog.dismiss();
                                        }
                                    }
                            ).create();
                    theAlertDialog.setCanceledOnTouchOutside(true);
                    theAlertDialog.show();
                } else if (theSetting instanceof ScanAreaSetting) {
                    final ScanAreaSetting theScanAreaSetting = (ScanAreaSetting) theSetting;
                    Map<String, Rect> theAreas = ScanSettingsHelper
                            .createPredefinedAreas(theScanAreaSetting);
                    ArrayList<String> theNames = new ArrayList<>(theAreas.keySet());
                    final ArrayList<Rect> theValues = new ArrayList<>(theAreas.values());
                    theNames.add("Custom");
                    theValues.add(new Rect());
                    final Rect theValue = theScanAreaSetting.getValue();
                    int theIndex = theValues.indexOf(theValue);
                    if (theIndex < 0 && theValue != null) {
                        // custom
                        theIndex = theValues.size() - 1;
                    }

                    String[] theChoices = theNames.toArray(
                            new String[theNames.size()]);
                    AlertDialog theAlertDialog = new AlertDialog.Builder(getActivity())
                            .setSingleChoiceItems(theChoices, theIndex,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (theValues.size() - 1 != which) {
                                                theScanAreaSetting.setValue(theValues.get(which));
                                            } else {
                                                showCustomAreaDialog(theScanAreaSetting);
                                            }
                                            mSettingsAdapter.notifyDataSetChanged();
                                            dialog.dismiss();
                                        }
                                    }
                            ).create();
                    theAlertDialog.setCanceledOnTouchOutside(true);
                    theAlertDialog.show();
                }
            }
        });

        return theRootView;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof OnSettingsSelectedListener) {
            mListener = (OnSettingsSelectedListener) activity;
        } else {
            throw new RuntimeException("Activity should implements "
                    + OnSettingsSelectedListener.class);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        startFetchingCapabilities();
    }

    private void startFetchingCapabilities() {
        if (!isAdded()) {
            return;
        }
        if (mScanner != null && mScannerCapabilities == null && !mScanner.isFetchingCapabilities()) {
            mScanner.fetchCapabilities(mCapabilitiesListener);
            mRetryContainer.setVisibility(View.GONE);
            mProgressMessageView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mScanner != null) {
            if (mScanner.isFetchingCapabilities()) {
                mScanner.cancelFetchingCapabilities();
            }
            if (mScanner.isValidating()) {
                mScanner.cancelValidation();
            }
            mValidationProgressView.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mSettingsAdapter != null) {
            unsubscribeFromSettingsChanged();
        }
        mScanSettings = null;
        mScannerCapabilities = null;
        mScanner = null;
    }

    private void updateScanSettings() {
        updateScanSettings(null, null, null);
    }

    private void updateScanSettings(Integer aPreviousInputSource, Boolean aPreviousDuplex,
            ScanTicket aScanTicket) {
        if (!isAdded()) {
            return;
        }

        if (mScannerCapabilities != null) {
            mProgressMessageView.setVisibility(View.INVISIBLE);

            unsubscribeFromSettingsChanged();

            if (mScanSettings == null) {
                ScanSettings theSettingsFromCapabilities = ScanSettingsHelper
                        .createSettingsFromCapabilities(mScannerCapabilities);

                mScanSettings = theSettingsFromCapabilities;
                ChoiceSetting<Integer> theInputSourceChoiceSetting = theSettingsFromCapabilities
                        .getInputSourceChoiceSetting();

                if (theInputSourceChoiceSetting.getChoiceValues().size() == 1) {
                    theInputSourceChoiceSetting.setValueIndex(0);
                }

                mScanSettings.updateSettings(mScanTicket);
            } else if (aScanTicket != null) {
                mScanSettings.updateSettings(aScanTicket);
            } else {
                Integer theCurrentInputSource = mScanSettings.getCurrentInputSource();
                Boolean theDuplex = mScanSettings.getDuplex();
                if (!Objects.equals(theCurrentInputSource, aPreviousInputSource)
                        || !Objects.equals(theDuplex, aPreviousDuplex)) {
                    mScanSettings.updateCurrentInputSourceSettings(
                            aPreviousInputSource, aPreviousDuplex);
                }
            }

            List<Setting> theCurrentSettingsList = mScanSettings.createCurrentSettingsList();
            @SuppressWarnings("unchecked")
            Setting<Rect> theScanAreaSetting = ScanSettingsHelper.findSettingByKey(
                    theCurrentSettingsList, ScanTicket.SCAN_SETTING_SCAN_AREAS);
            if (theScanAreaSetting == null || theScanAreaSetting.getValue() == null) {
                ScanSettingsHelper.removeSettingByKey(theCurrentSettingsList,
                        ScanTicket.SCAN_SETTING_MUST_HONOR_SCAN_AREAS);
            }

            mSettingsAdapter.replaceSettings(theCurrentSettingsList);

            subscribeToSettingsChanged();
        } else {
            mProgressMessageView.setVisibility(View.VISIBLE);
        }
    }

    private void subscribeToSettingsChanged() {
        List<Setting> theSettings = mSettingsAdapter.getSettings();
        ScanSettingsHelper.addSettingChangedListener(theSettings,
                ScanTicket.SCAN_TICKET_INPUT_SOURCE_KEY, mInputSourceChangedListener);
        ScanSettingsHelper.addSettingChangedListener(theSettings,
                ScanTicket.SCAN_SETTING_DUPLEX, mDuplexChangedListener);
        ScanSettingsHelper.addSettingChangedListener(theSettings,
                ScanTicket.SCAN_SETTING_SCAN_AREAS, mScanAreaChangedListener);
    }

    private void unsubscribeFromSettingsChanged() {
        List<Setting> theSettings = mSettingsAdapter.getSettings();

        ScanSettingsHelper.removeSettingChangedListener(theSettings,
                ScanTicket.SCAN_TICKET_INPUT_SOURCE_KEY, mInputSourceChangedListener);
        ScanSettingsHelper.removeSettingChangedListener(theSettings,
                ScanTicket.SCAN_SETTING_DUPLEX, mDuplexChangedListener);
        ScanSettingsHelper.removeSettingChangedListener(theSettings,
                ScanTicket.SCAN_SETTING_SCAN_AREAS, mScanAreaChangedListener);
    }

    private ScanTicket createScanTicket() {
        List<Setting> theSettings = mSettingsAdapter.getSettings();
        if (Utils.isNotEmpty(theSettings)) {
            return ScanSettingsHelper.createScanTicket(theSettings);
        }
        return null;
    }

    private void validateScanTicket(final Integer aInputSource, final Boolean aDuplex) {
        final ScanTicket theScanTicket = createScanTicket();
        if (theScanTicket == null) {
            return;
        }
        mValidationProgressView.setVisibility(View.VISIBLE);
        if (mScanner.isValidating()) {
            mScanner.cancelValidation();
        }
        mScanner.validateTicket(theScanTicket, new ScanTicketValidator.ScanTicketValidationListener() {
            @Override
            public void onScanTicketValidationComplete(ScanTicket aValidScanTicket) {
                updateScanSettings(null, null, aValidScanTicket);
                if (isAdded()) {
                    mValidationProgressView.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onScanTicketValidationError(ScannerException aException) {
                if (isAdded()) {
                    mValidationProgressView.setVisibility(View.INVISIBLE);
                }
                try {
                    throw aException;
                } catch (ValidationException e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), "Invalid settings: " + e.getInvalidSettings(),
                            Toast.LENGTH_LONG).show();
                    for (String theInvalidSetting : e.getInvalidSettings()) {
                        theScanTicket.setSetting(theInvalidSetting, null);
                    }
                    updateScanSettings(null, null, theScanTicket);
                } catch (ScannerException e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), "Validation failed " + e,
                            Toast.LENGTH_LONG).show();
                    updateScanSettings(aInputSource, aDuplex, null);
                }
            }
        });
    }

    public interface OnSettingsSelectedListener {
        void onSettingsSelected(ScanTicket aScanTicket);
    }

    private void showCustomAreaDialog(final ScanAreaSetting aScanAreaSetting) {
        Activity theContext = getActivity();
        @SuppressLint("InflateParams")
        View theRootView = LayoutInflater.from(theContext).inflate(R.layout.custom_area_view, null);

        TextView theDescription = (TextView) theRootView.findViewById(R.id.custom_area_description);
        theDescription.setText(String.format(Locale.ENGLISH,
                "Min width: %.2f, min height: %.2f, max width: %.2f, max height: %.2f",
                toInches(aScanAreaSetting.getMinWidth()),
                toInches(aScanAreaSetting.getMinHeight()),
                toInches(aScanAreaSetting.getMaxX()),
                toInches(aScanAreaSetting.getMaxY())));

        final EditText theXOriginView = (EditText) theRootView.findViewById(R.id.x_origin_edit_text);
        final EditText theYOriginView = (EditText) theRootView.findViewById(R.id.y_origin_edit_text);
        final EditText theWidthView = (EditText) theRootView.findViewById(R.id.width_edit_text);
        final EditText theHeightView = (EditText) theRootView.findViewById(R.id.height_edit_text);

        Rect theRect = aScanAreaSetting.getValue();
        if (theRect != null) {
            theXOriginView.setText(formatInches(theRect.left));
            theYOriginView.setText(formatInches(theRect.top));
            theWidthView.setText(formatInches(theRect.width()));
            theHeightView.setText(formatInches(theRect.height()));
        } else {
            theXOriginView.setText(formatInches(0));
            theYOriginView.setText(formatInches(0));
            theWidthView.setText(formatInches(aScanAreaSetting.getMaxX()));
            theHeightView.setText(formatInches(aScanAreaSetting.getMaxY()));
        }

        AlertDialog theAlertDialog = new AlertDialog.Builder(theContext)
                .setTitle(R.string.custom_area_dialog_title)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Context theContext = getActivity();
                        try {
                            float theXInches = Float.parseFloat(
                                    String.valueOf(theXOriginView.getText()));
                            float theYInches = Float.parseFloat(
                                    String.valueOf(theYOriginView.getText()));
                            float theWidthInches = Float.parseFloat(
                                    String.valueOf(theWidthView.getText()));
                            float theHeightInches = Float.parseFloat(String.valueOf(
                                    theHeightView.getText()));
                            int theX = (int) (theXInches * 300);
                            int theY = (int) (theYInches * 300);
                            int theWidth = (int) (theWidthInches * 300);
                            int theHeight = (int) (theHeightInches * 300);
                            Rect theResultRect = new Rect(theX, theY,
                                    theX + theWidth, theY + theHeight);
                            boolean theValid = aScanAreaSetting.setValue(theResultRect);
                            if (theValid) {
                                mSettingsAdapter.notifyDataSetChanged();
                            } else {
                                Toast.makeText(theContext, "Unsupported range",
                                        Toast.LENGTH_LONG).show();
                            }
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                            Toast.makeText(theContext, "Number format error",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setView(theRootView)
                .create();
        theAlertDialog.show();
    }

    private float toInches(int aOneOfTreeHundredInches) {
        return (float) aOneOfTreeHundredInches / 300;
    }

    private String formatInches(int aOneOfTreeHundredInches) {
        return String.format(Locale.ENGLISH, "%.2f", toInches(aOneOfTreeHundredInches));
    }
}
