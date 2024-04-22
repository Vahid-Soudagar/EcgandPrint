/*
 * (C) Copyright 2016 HP Development Company, L.P.
 * All Rights Reserved Worldwide
 * 07/28/2016 SMKAB
 *
 * ScannersBrowsingFragment.java
 */

package com.vcreate.ecgchart.scanner;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;

import com.hp.mobile.scan.sdk.Scanner;
import com.hp.mobile.scan.sdk.browsing.ScannersBrowser;
import com.vcreate.ecgchart.R;
import com.vcreate.ecgchart.ecg.MainActivity;


/**
 * Fragment for scanners browsing.
 * Activities that contain this fragment must implement the
 * {@link OnScannerSelectedListener} interface
 * to handle interaction events.
 */
public class ScannersBrowsingFragment extends Fragment {

    private OnScannerSelectedListener mListener;

    private TextView mDevicesCountView;
    private ScannerAdapter mScannersAdapter;
    private ScannersBrowser mScannerBrowser;
    private CompoundButton mStartStopDiscoveringButton;
    private ScannersBrowser.ScannerAvailabilityListener mAvailabilityListener
            = new ScannersBrowser.ScannerAvailabilityListener() {
        @Override
        public void onScannerFound(Scanner aScanner) {
            if (mScannersAdapter != null) {
                mScannersAdapter.add(aScanner);
            }
            updateCounter();
        }

        @Override
        public void onScannerLost(Scanner aScanner) {
            if (mScannersAdapter != null) {
                mScannersAdapter.remove(aScanner);
            }
            updateCounter();
        }

    };
    private Context mContext;

    public ScannersBrowsingFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater aInflater, ViewGroup aContainer,
            Bundle aSavedInstanceState) {
        // Inflate the layout for this fragment
        View theView = aInflater.inflate(R.layout.fragment_scanners_browsing, aContainer, false);

        mDevicesCountView = (TextView) theView.findViewById(R.id.devices_count_view);

        ListView theDevicesList = (ListView) theView.findViewById(R.id.devices_list);
        mScannersAdapter = new ScannerAdapter(getActivity());
        theDevicesList.setAdapter(mScannersAdapter);

        theDevicesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Scanner theItem = (Scanner) parent.getAdapter().getItem(position);
                if (mListener != null) {
                    mListener.onScannerSelected(theItem);
                }
            }
        });

        mStartStopDiscoveringButton = (CompoundButton) theView.findViewById(R.id
                .start_stop_discovering);
        mStartStopDiscoveringButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mScannersAdapter.clear();
                    updateCounter();
                    mScannerBrowser.start(mAvailabilityListener);
                } else {
                    mScannerBrowser.stop();
                }
            }
        });
        updateCounter();

        return theView;
    }

    private void updateCounter() {
        mDevicesCountView.setText(String.valueOf(mScannersAdapter.getCount()));
    }

    @SuppressWarnings("deprecation") // this method should be used for compatibility reasons
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
        if (activity instanceof OnScannerSelectedListener) {
            mListener = (OnScannerSelectedListener) activity;
        } else {
            throw new RuntimeException(activity.toString()
                    + " must implement OnScannerSelectedListener");
        }
        mScannerBrowser = new ScannersBrowser(activity.getApplicationContext());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mContext != null) {
            ((ScanMainActivity) mContext).setActionBarTitle(
                    mContext.getString(R.string.browsing_devices_title));
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        if (mScannerBrowser != null) {
            mScannerBrowser.stop();
            mScannerBrowser = null;
        }
        mContext = null;
    }

    @Override
    public void onStop() {
        super.onStop();
        mScannerBrowser.stop();
        mStartStopDiscoveringButton.setChecked(false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mScannersAdapter.clear();
        mScannersAdapter.release();
        mScannersAdapter = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnScannerSelectedListener {
        void onScannerSelected(Scanner aScanner);
    }
}
