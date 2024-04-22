/*
 * (C) Copyright 2016 HP Development Company, L.P.
 * All Rights Reserved Worldwide
 * 07/28/2016 SMKAB
 *
 * ScanningFragment.java
 */


package com.vcreate.ecgchart.scanner;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hp.mobile.scan.sdk.AdfException;
import com.hp.mobile.scan.sdk.DeviceStatusMonitor;
import com.hp.mobile.scan.sdk.ScanCapture;
import com.hp.mobile.scan.sdk.Scanner;
import com.hp.mobile.scan.sdk.ScannerException;
import com.hp.mobile.scan.sdk.model.ScanPage;
import com.hp.mobile.scan.sdk.model.ScanTicket;
import com.vcreate.ecgchart.R;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ScanningFragment extends Fragment {
    private static final String TAG = "ScanningFragment";

    private static final Map<Integer, String> SCANNER_STATUS_MAP = new HashMap<>();

    static {
        SCANNER_STATUS_MAP.put(DeviceStatusMonitor.SCANNER_STATUS_IDLE, "Idle");
        SCANNER_STATUS_MAP.put(DeviceStatusMonitor.SCANNER_STATUS_PROCESSING, "Processing");
        SCANNER_STATUS_MAP.put(DeviceStatusMonitor.SCANNER_STATUS_STOPPED, "Stopped");
        SCANNER_STATUS_MAP.put(DeviceStatusMonitor.SCANNER_STATUS_TESTING, "Testing");
        SCANNER_STATUS_MAP.put(DeviceStatusMonitor.SCANNER_STATUS_UNAVAILABLE, "Unavailable");
        SCANNER_STATUS_MAP.put(DeviceStatusMonitor.SCANNER_STATUS_UNKNOWN, "Unknown");
    }

    private static final Map<Integer, Integer> SCANNER_STATUS_TO_COLOR_MAP = new HashMap<>();

    static {
        SCANNER_STATUS_TO_COLOR_MAP.put(DeviceStatusMonitor.SCANNER_STATUS_IDLE, Color.GREEN);
        SCANNER_STATUS_TO_COLOR_MAP.put(DeviceStatusMonitor.SCANNER_STATUS_PROCESSING, Color.YELLOW);
        SCANNER_STATUS_TO_COLOR_MAP.put(DeviceStatusMonitor.SCANNER_STATUS_STOPPED, Color.RED);
        SCANNER_STATUS_TO_COLOR_MAP.put(DeviceStatusMonitor.SCANNER_STATUS_TESTING, Color.YELLOW);
        SCANNER_STATUS_TO_COLOR_MAP.put(DeviceStatusMonitor.SCANNER_STATUS_UNAVAILABLE, Color.GRAY);
        SCANNER_STATUS_TO_COLOR_MAP.put(DeviceStatusMonitor.SCANNER_STATUS_UNKNOWN, Color.GRAY);
    }

    private static final Map<Integer, String> ADF_STATUS_MAP = new HashMap<>();

    static {
        ADF_STATUS_MAP.put(DeviceStatusMonitor.ADF_STATUS_UNKNOWN, "Unknown");
        ADF_STATUS_MAP.put(DeviceStatusMonitor.ADF_STATUS_UNSUPPORTED, "Unsupported");
        ADF_STATUS_MAP.put(DeviceStatusMonitor.ADF_STATUS_PROCESSING, "Processing");
        ADF_STATUS_MAP.put(DeviceStatusMonitor.ADF_STATUS_EMPTY, "Empty");
        ADF_STATUS_MAP.put(DeviceStatusMonitor.ADF_STATUS_JAM, "Jam");
        ADF_STATUS_MAP.put(DeviceStatusMonitor.ADF_STATUS_LOADED, "Loaded");
        ADF_STATUS_MAP.put(DeviceStatusMonitor.ADF_STATUS_MISPICK, "Mispick");
        ADF_STATUS_MAP.put(DeviceStatusMonitor.ADF_STATUS_HATCH_OPEN, "Hatch Open");
        ADF_STATUS_MAP.put(DeviceStatusMonitor.ADF_STATUS_DUPLEX_PAGE_TOO_SHORT,
                "Duplex page too short");
        ADF_STATUS_MAP.put(DeviceStatusMonitor.ADF_STATUS_DUPLEX_PAGE_TOO_LONG,
                "Duplex page too long");
        ADF_STATUS_MAP.put(DeviceStatusMonitor.ADF_STATUS_MULTIPICK_DETECTED, "Multipick Detected");
        ADF_STATUS_MAP.put(DeviceStatusMonitor.ADF_STATUS_INPUT_TRAY_FAILED, "Input Tray Failed");
        ADF_STATUS_MAP.put(DeviceStatusMonitor.ADF_STATUS_INPUT_TRAY_OVERLOADED,
                "Input Tray Overloaded");
    }

    private Scanner mScanner;
    private ScanTicket mScanTicket;

    private CompoundButton mConnectButton;
    private View mProgressContainer;
    private TextView mDeviceStatusView;
    private TextView mAdfStatusView;

    private ScanCapture.ScanningProgressListener mScanProgressListener =
            new ScanCapture.ScanningProgressListener() {

                @Override
                public void onScanningPageDone(final ScanPage aScanPage) {
                    Log.d(TAG, "onScanningPageDone: " + aScanPage);
                    Context theContext = mContext;
                    if (theContext != null) {
                        Toast.makeText(theContext, "onScanningPageDone " + aScanPage.getUri(),
                                Toast.LENGTH_SHORT).show();
                    }
                    mScanResultAdapter.add(aScanPage);
                }

                @Override
                public void onScanningComplete() {
                    Log.d(TAG, "onScanningComplete: ");
                    Context theContext = mContext;
                    if (theContext != null) {
                        Toast.makeText(theContext, "onScanningComplete ",
                                Toast.LENGTH_SHORT).show();
                    }
                    onScanStopped();
                }

                @Override
                public void onScanningError(final ScannerException aException) {
                    Log.d(TAG, "onScanningError: ", aException);
                    closeSession();
                    onScanStopped();

                    Context theContext = mContext;
                    if (theContext == null) {
                        return;
                    }
                    try {
                        throw aException;
                    } catch (AdfException e) {
                        Toast.makeText(theContext, "AdfError, status: " + ADF_STATUS_MAP.get(
                                e.getAdfStatus()), Toast.LENGTH_LONG).show();
                    } catch (ScannerException e) {
                        Toast.makeText(theContext, "onScanError, reason: " + aException.getReason()
                                        + " " + aException.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }

                }
            };
    private ScanResultAdapter mScanResultAdapter;
    private Context mContext;
    private OnScanSettingsListener mListener;
    private TextView mDeviceStatusDescriptionView;
    private ImageView mDeviceStatusImageView;

    public ScanningFragment() {
        // Required empty public constructor
    }

    public void setScanner(Scanner aScanner) {
        mScanner = aScanner;
    }

    public void setScanTicket(ScanTicket aScanTicket) {
        mScanTicket = aScanTicket;
    }

    @Override
    public View onCreateView(LayoutInflater aInflater, ViewGroup aContainer,
            Bundle aSavedInstanceState) {
        View theView = aInflater.inflate(R.layout.fragment_scanning, aContainer, false);

        AbsListView theScanResultsView = (AbsListView) theView.findViewById(R.id.scan_result_grid_view);
        if (mScanResultAdapter == null) {
            mScanResultAdapter = new ScanResultAdapter(getActivity());
        }
        theScanResultsView.setAdapter(mScanResultAdapter);
        theScanResultsView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                ScanPage thePage = (ScanPage) parent.getAdapter().getItem(position);
                Intent theIntent = new Intent(Intent.ACTION_VIEW);
                theIntent.setDataAndType(thePage.getUri(), thePage.getMimeType());
                Intent theChooser = Intent.createChooser(theIntent, "");
                startActivity(theChooser);
                return true;
            }
        });

        mProgressContainer = theView.findViewById(R.id.scanning_progress_container);
        mDeviceStatusView = (TextView) theView.findViewById(R.id.device_status_view);
        mAdfStatusView = (TextView) theView.findViewById(R.id.adf_status_view);
        mDeviceStatusDescriptionView = (TextView) theView.findViewById(
                R.id.device_status_description_view);
        mDeviceStatusImageView = (ImageView) theView.findViewById(R.id.device_status_image_view);

        showDeviceName();
        updateButtonState();

        mConnectButton = (CompoundButton) theView.findViewById(R.id.device_connect_button);
        mConnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((CompoundButton) v).isChecked()) {
                    File theExternalStorageDirectory = Environment.getExternalStorageDirectory();
                    File theScan_demo = new File(theExternalStorageDirectory, "scan_demo");
                    mScanner.scan(theScan_demo.getAbsolutePath(),
                            mScanTicket, mScanProgressListener);
                    onScanStarted();
                } else {
                    closeSession();
                    onScanStopped();
                }
            }
        });

        View theSettingsButton = theView.findViewById(R.id.settings_button);
        theSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onScanSettings(mScanner, mScanTicket);
            }
        });
        return theView;
    }

    @SuppressWarnings("deprecation") // this method should be used for compatibility reasons
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
        if (activity instanceof OnScanSettingsListener) {
            mListener = (OnScanSettingsListener) activity;
        } else {
            throw new RuntimeException(activity.toString()
                    + " must implement OnScanSettingsListener");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        showDeviceName();
        updateButtonState();
        startMonitoringState();
    }

    private void showDeviceName() {
        Context theContext = mContext;
        if (theContext != null) {
            String theTitle = mScanner != null
                    ? mScanner.getHumanReadableName()
                    : theContext.getString(R.string.no_device);
            ((ScanMainActivity) theContext).setActionBarTitle(theTitle);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        closeSession();
        onScanStopped();
        stopMonitoringState();
    }

    private void updateButtonState() {
        if (mConnectButton != null) {
            mConnectButton.setEnabled(mScanner != null);
            if (mScanner != null && mScanner.isScanning()) {
                mConnectButton.setChecked(true);
            } else {
                mConnectButton.setChecked(false);
            }
        }
    }

    private void onScanStarted() {
        mConnectButton.setChecked(true);
        mProgressContainer.setVisibility(View.VISIBLE);
        mProgressContainer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
    }

    private void onScanStopped() {
        mConnectButton.setChecked(false);
        mProgressContainer.setVisibility(View.INVISIBLE);
        mProgressContainer.setOnTouchListener(null);
    }

    private void closeSession() {
        if (mScanner != null) {
            mScanner.cancelScanning();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        mContext = null;
    }

    public interface OnScanSettingsListener {
        void onScanSettings(Scanner aScanner, ScanTicket aScanTicket);
    }

    private void startMonitoringState() {
        if (mScanner != null) {
            if (mScanner.isDeviceStatusMonitoring()) {
                return;
            }
            updateStatus(DeviceStatusMonitor.SCANNER_STATUS_UNKNOWN,
                    DeviceStatusMonitor.ADF_STATUS_UNKNOWN, null);
            mScanner.monitorDeviceStatus(0, new DeviceStatusMonitor.ScannerStatusListener() {
                @Override
                public void onStatusChanged(int aScannerStatus, int aAdfStatus) {
                    Log.d(TAG, "onStatusChanged: " + aScannerStatus + " adf: " + aAdfStatus);
                    updateStatus(aScannerStatus, aAdfStatus, null);
                }

                @Override
                public void onStatusError(final ScannerException aException) {
                    Log.d(TAG, "onStatusError: ", aException);
                    updateStatus(DeviceStatusMonitor.SCANNER_STATUS_UNKNOWN,
                            DeviceStatusMonitor.ADF_STATUS_UNKNOWN, aException);
                }
            });
        }
    }

    private void stopMonitoringState() {
        if (mScanner != null) {
            mScanner.stopMonitoringDeviceStatus();
            updateStatus(DeviceStatusMonitor.SCANNER_STATUS_UNKNOWN,
                    DeviceStatusMonitor.ADF_STATUS_UNKNOWN, null);
        }
    }

    private void updateStatus(int aScannerStatus, int aAdfStatus, ScannerException aException) {
        if (!isAdded()) {
            return;
        }
        mDeviceStatusView.setText(SCANNER_STATUS_MAP.get(aScannerStatus));
        mAdfStatusView.setText(ADF_STATUS_MAP.get(aAdfStatus));
        mDeviceStatusDescriptionView.setText(aException != null ? aException.toString() : null);
        Integer theColor = SCANNER_STATUS_TO_COLOR_MAP.get(aScannerStatus);
        if (theColor == null) {
            theColor = Color.WHITE;
        }
        mDeviceStatusImageView.setColorFilter(theColor);
    }
}
