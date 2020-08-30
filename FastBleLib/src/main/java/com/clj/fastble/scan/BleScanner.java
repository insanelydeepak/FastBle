/*
 *   BleScanner.java
 *   FastBLE
 *
 *  Created by Deepak Sharma (InsanelyDeepak) on 30/8/2020 .
 *  Copyright © 2020 InsanelyDeepak. All rights reserved.
 *
 */

package com.clj.fastble.scan;


import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcel;
import android.os.ParcelUuid;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleScanAndConnectCallback;
import com.clj.fastble.callback.BleScanCallback;
import com.clj.fastble.callback.BleScanPresenterImp;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.data.BleScanState;
import com.clj.fastble.utils.BleLog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BleScanner {

    public static BleScanner getInstance() {
        return BleScannerHolder.sBleScanner;
    }

    private static class BleScannerHolder {
        private static final BleScanner sBleScanner = new BleScanner();
    }

    private BleScanState mBleScanState = BleScanState.STATE_IDLE;

    private BleScanPresenter mBleScanPresenter = new BleScanPresenter() {

        @Override
        public void onScanStarted(boolean success) {
            BleScanPresenterImp callback = mBleScanPresenter.getBleScanPresenterImp();
            if (callback != null) {
                callback.onScanStarted(success);
            }
        }

        @Override
        public void onLeScan(BleDevice bleDevice) {
            if (mBleScanPresenter.ismNeedConnect()) {
                BleScanAndConnectCallback callback = (BleScanAndConnectCallback)
                        mBleScanPresenter.getBleScanPresenterImp();
                if (callback != null) {
                    callback.onLeScan(bleDevice);
                }
            } else {
                BleScanCallback callback = (BleScanCallback) mBleScanPresenter.getBleScanPresenterImp();
                if (callback != null) {
                    callback.onLeScan(bleDevice);
                }
            }
        }

        @Override
        public void onScanning(BleDevice result) {
            BleScanPresenterImp callback = mBleScanPresenter.getBleScanPresenterImp();
            if (callback != null) {
                callback.onScanning(result);
            }
        }

        @Override
        public void onScanFinished(List<BleDevice> bleDeviceList) {
            if (mBleScanPresenter.ismNeedConnect()) {
                final BleScanAndConnectCallback callback = (BleScanAndConnectCallback)
                        mBleScanPresenter.getBleScanPresenterImp();
                if (bleDeviceList == null || bleDeviceList.size() < 1) {
                    if (callback != null) {
                        callback.onScanFinished(null);
                    }
                } else {
                    if (callback != null) {
                        callback.onScanFinished(bleDeviceList.get(0));
                    }
                    final List<BleDevice> list = bleDeviceList;
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            BleManager.getInstance().connect(list.get(0), callback);
                        }
                    }, 100);
                }
            } else {
                BleScanCallback callback = (BleScanCallback) mBleScanPresenter.getBleScanPresenterImp();
                if (callback != null) {
                    callback.onScanFinished(bleDeviceList);
                }
            }
        }
    };
    private BLEScannerCallback mBleScanCallback = new BLEScannerCallback() {
        @Override
        public void onScanStarted(boolean success) {
            BleScanPresenterImp callback = mBleScanCallback.getBleScanPresenterImp();
            if (callback != null) {
                callback.onScanStarted(success);
            }
        }

        @Override
        public void onLeScan(BleDevice bleDevice) {
            if (mBleScanCallback.ismNeedConnect()) {
                BleScanAndConnectCallback callback = (BleScanAndConnectCallback)
                        mBleScanCallback.getBleScanPresenterImp();
                if (callback != null) {
                    callback.onLeScan(bleDevice);
                }
            } else {
                BleScanCallback callback = (BleScanCallback) mBleScanCallback.getBleScanPresenterImp();
                if (callback != null) {
                    callback.onLeScan(bleDevice);
                }
            }
        }

        @Override
        public void onScanning(BleDevice result) {
            BleScanPresenterImp callback = mBleScanCallback.getBleScanPresenterImp();
            if (callback != null) {
                callback.onScanning(result);
            }
        }

        @Override
        public void onScanFinished(List<BleDevice> bleDeviceList) {
            if (mBleScanCallback.ismNeedConnect()) {
                final BleScanAndConnectCallback callback = (BleScanAndConnectCallback)
                        mBleScanCallback.getBleScanPresenterImp();
                if (bleDeviceList == null || bleDeviceList.size() < 1) {
                    if (callback != null) {
                        callback.onScanFinished(null);
                    }
                } else {
                    if (callback != null) {
                        callback.onScanFinished(bleDeviceList.get(0));
                    }
                    final List<BleDevice> list = bleDeviceList;
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            BleManager.getInstance().connect(list.get(0), callback);
                        }
                    }, 100);
                }
            } else {
                BleScanCallback callback = (BleScanCallback) mBleScanCallback.getBleScanPresenterImp();
                if (callback != null) {
                    callback.onScanFinished(bleDeviceList);
                }
            }
        }
    };

    public void scan(UUID[] serviceUuids, String[] names, String mac, boolean fuzzy,
                     long timeOut, final BleScanCallback callback) {

        startLeScan(serviceUuids, names, mac, fuzzy, false, timeOut, callback);
    }

    public void scanAndConnect(UUID[] serviceUuids, String[] names, String mac, boolean fuzzy,
                               long timeOut, BleScanAndConnectCallback callback) {

        startLeScan(serviceUuids, names, mac, fuzzy, true, timeOut, callback);
    }

    private synchronized void startLeScan(UUID[] serviceUuids, String[] names, String mac, boolean fuzzy,
                                          boolean needConnect, long timeOut, BleScanPresenterImp imp) {

        if (mBleScanState != BleScanState.STATE_IDLE) {
            BleLog.w("scan action already exists, complete the previous scan action first");
            if (imp != null) {
                imp.onScanStarted(false);
            }
            return;
        }


        // Check before call the function
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBleScanCallback.prepare(names, mac, fuzzy, needConnect, timeOut, imp);
            List<ScanFilter> scanFilter = buildScanFilters(serviceUuids);
            BleManager.getInstance().getBluetoothAdapter().getBluetoothLeScanner().startScan(scanFilter, buildScanSettings(), mBleScanCallback);
            boolean success = true;
            mBleScanState = success ? BleScanState.STATE_SCANNING : BleScanState.STATE_IDLE;
            mBleScanCallback.notifyScanStarted(success);
        } else {
            mBleScanPresenter.prepare(names, mac, fuzzy, needConnect, timeOut, imp);
            boolean success = BleManager.getInstance().getBluetoothAdapter()
                    .startLeScan(serviceUuids, mBleScanPresenter);
            mBleScanState = success ? BleScanState.STATE_SCANNING : BleScanState.STATE_IDLE;
            mBleScanPresenter.notifyScanStarted(success);
        }


    }

    public synchronized void stopLeScan() {
        // Check before call the function
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (BleManager.getInstance().getBluetoothAdapter() != null) {
                BluetoothLeScanner bluetoothLeScanner =  BleManager.getInstance().getBluetoothAdapter().getBluetoothLeScanner();
                if (bluetoothLeScanner != null) {
                    bluetoothLeScanner.stopScan(mBleScanCallback);
                    mBleScanState = BleScanState.STATE_IDLE;
                    mBleScanCallback.notifyScanStopped();
                }
            }
        }else {
            BleManager.getInstance().getBluetoothAdapter().stopLeScan(mBleScanPresenter);
            mBleScanState = BleScanState.STATE_IDLE;
            mBleScanPresenter.notifyScanStopped();
        }

    }

    public BleScanState getScanState() {
        return mBleScanState;
    }

    /**
     * Return a List of {@link ScanFilter} objects to filter by Service UUID.
     */
    private List<ScanFilter> buildScanFilters(UUID[] serviceUuids) {
        List<ScanFilter> scanFilters = new ArrayList<>();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            if (serviceUuids != null) {
                ScanFilter.Builder builder = null;
                builder = new ScanFilter.Builder();
                for (UUID uid : serviceUuids) {
                    ParcelUuid uuid = new ParcelUuid(uid);
                    builder.setServiceUuid(uuid);
                }
                scanFilters.add(builder.build());
            } else {
                ScanFilter scanFilterName = new ScanFilter.Builder().setDeviceName(null).build();
                scanFilters.add(scanFilterName);
            }
        }else {
            BleLog.e("ScanFilter requries SDK greater than LOLLIPOP");
        }
        return scanFilters;
    }

    /**
     * Return a {@link ScanSettings} object set to use low power (to preserve battery life).
     */
    @SuppressLint("NewApi")
    private ScanSettings buildScanSettings() {
        ScanSettings.Builder builder = new ScanSettings.Builder();
        builder.setScanMode(ScanSettings.SCAN_MODE_LOW_POWER);
        return builder.build();
    }
}