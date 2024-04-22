package com.vcreate.ecgchart.scanner;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.hp.mobile.scan.sdk.Scanner;
import com.hp.mobile.scan.sdk.model.ScanTicket;
import com.vcreate.ecgchart.R;
import com.vcreate.ecgchart.scanner.settings.ScanSettingsFragment;

public class ScanMainActivity extends AppCompatActivity implements
        ScannersBrowsingFragment.OnScannerSelectedListener, ScanningFragment.OnScanSettingsListener,
        ScanSettingsFragment.OnSettingsSelectedListener{

    private static final String SCANNING_FRAGMENT_TAG = "ScanningFragment";
    private static final String SCAN_SETTINGS_FRAGMENT_TAG = "ScanSettingsFragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_main);

        Fragment theFragment = getFragmentManager().findFragmentById(R.id.content_container);
        if (theFragment == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.content_container, new ScannersBrowsingFragment())
                    .commit();
        }
    }

    @Override
    public void onScannerSelected(Scanner aScanner) {
        if (aScanner != null) {
            ScanningFragment theScanningFragment = new ScanningFragment();
            theScanningFragment.setScanner(aScanner);
            getFragmentManager().beginTransaction()
                    .replace(R.id.content_container, theScanningFragment, SCANNING_FRAGMENT_TAG)
                    .setTransition(android.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .addToBackStack(SCANNING_FRAGMENT_TAG)
                    .commit();
        }
    }

    public void setActionBarTitle(CharSequence aTitle) {
        ActionBar theActionBar = getActionBar();
        if (theActionBar != null) {
            theActionBar.setTitle(aTitle);
        }
    }

    @Override
    public void onScanSettings(Scanner aScanner, ScanTicket aScanTicket) {
        if (aScanner != null) {
            ScanSettingsFragment theScanSettingsFragment = new ScanSettingsFragment();
            theScanSettingsFragment.setScanner(aScanner);
            theScanSettingsFragment.setScanTicket(aScanTicket);
            getFragmentManager().beginTransaction()
                    .replace(R.id.content_container, theScanSettingsFragment,
                            SCAN_SETTINGS_FRAGMENT_TAG)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .addToBackStack(SCAN_SETTINGS_FRAGMENT_TAG)
                    .commit();
        }
    }

    @Override
    public void onSettingsSelected(ScanTicket aScanTicket) {
        ScanningFragment theScanningFragment = (ScanningFragment) getFragmentManager()
                .findFragmentByTag(SCANNING_FRAGMENT_TAG);
        if (theScanningFragment != null) {
            theScanningFragment.setScanTicket(aScanTicket);
            getFragmentManager().popBackStack();
        }
    }
}