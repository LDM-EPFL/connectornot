/*
 * Copyright (C) 2009 Cyril Jaquier
 *
 * This file is part of NetCounter.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; If not, see <http://www.gnu.org/licenses/>.
 */

package net.lmag.connectornot.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.widget.DrawerLayout;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import net.lmag.connectornot.NetCounterApplication;
import net.lmag.connectornot.R;
import net.lmag.connectornot.model.MyLog;
import net.lmag.connectornot.model.NewModelAPI;
import net.lmag.connectornot.service.BleController;

import org.redpin.android.ApplicationContext;
import org.redpin.android.core.Fingerprint;
import org.redpin.android.core.Location;
import org.redpin.android.core.Measurement;
import org.redpin.android.net.InternetConnectionManager;
import org.redpin.android.net.Response;
import org.redpin.android.net.SynchronizationManager;
import org.redpin.android.net.home.FingerprintRemoteHome;
import org.redpin.android.net.home.LocationRemoteHome;
import org.redpin.android.net.home.RemoteEntityHomeCallback;
import org.redpin.android.net.wifi.WifiSniffer;
import org.redpin.android.ui.NewMapActivity;

public class NetCounterActivity extends Activity/*ExpandableListActivity*/ /*implements IModelListener,
		IOperation*/ {

//	private static final String EXPANDED_GROUP = "expand.group";

//	private ListAdapter mAdapter = null;

//	private NetCounterModel mModel = null;

//    private ProgressDialog progressDialog;

    private static final String TAG = "NetCounterActivity";

    private BleController mBleController;




//	private Counter mCounter;

//	private HandlerContainer mContainer;

    private Handler mRedpinHandler = new Handler();

    private Runnable mRedpinLocator = new Runnable() {
        @Override
        public void run() {
            MyLog.d("NetCounterActivity", "Running locator.");
            if(mServRegistered) {
                MyLog.d("NetCounterActivity", "Forcing measurement!");
                mWifiService.forceMeasurement();
            }
            mRedpinHandler.removeCallbacks(mRedpinLocator);
            mRedpinHandler.postDelayed(mRedpinLocator,
                    Long.valueOf(mSharedPreferences
                            .getLong("inter", 10)) * 1000);
        }
    };

	private NetCounterApplication mApp;
	
	private boolean mRecMode = true;

    private WifiSniffer mWifiService;

    private boolean firstMeasurement = false;

    private boolean mAskBT = true;

    private Location mLocation = null;

    private boolean mServRegistered = false;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;

    private SharedPreferences mSharedPreferences = null;


    private Dialog getAboutDialog() {

        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.about_dialog);

        final TextView text = (TextView) dialog.findViewById(R.id.aboutText);
        text.setClickable(true);
        text.setText(Html.fromHtml(getString(R.string.aboutText)));
        text.setMovementMethod(LinkMovementMethod.getInstance());
//        final SharedPreferences prefs = mApp.getAdapter(SharedPreferences.class);

        Button okButton = (Button) dialog.findViewById(R.id.hpAbout);
        okButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse(getString(R.string.aboutHomepageUrl));
                startActivity(new Intent(Intent.ACTION_VIEW, uri));
            }
        });
        Button cancelButton = (Button) dialog.findViewById(R.id.closeAbout);
        cancelButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });


        return dialog;
    }

    private Dialog getEditTimeDialog() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setView(R.layout.edite_interval_dialog);
//        builder.setTitle(R.string.prefsTimerTitle);
//        final EditText input = new EditText(this);
//        final SharedPreferences prefs = mApp.getAdapter(SharedPreferences.class);
//        input.setText(Long.toString(prefs.getLong("inter", 10)));
//        input.setInputType(InputType.TYPE_CLASS_NUMBER);
//        builder.setView(input);
//        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                try {
//                    long val = Long.valueOf(input.getText().toString());
//                    Editor editor = prefs.edit();
//                    editor.putLong("inter", val);
//                    editor.commit();
//                } catch (NumberFormatException e) {
//
//                }
//            }
//        });
//        builder.setNegativeButton(getString(R.string.cancel), null);
//        return builder.create();

//        LayoutInflater inflater = (LayoutInflater)
//                this.getSystemService(LAYOUT_INFLATER_SERVICE);
//        View layout = inflater.inflate(R.layout.edite_interval_dialog, (ViewGroup) findViewById(R.id.inter_dialog));

        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.edite_interval_dialog);

        final EditText input = (EditText) dialog.findViewById(R.id.interInput);
        final SharedPreferences prefs = mApp.getAdapter(SharedPreferences.class);
        input.setText(Long.toString(prefs.getLong("inter", 10)));

        Button okButton = (Button) dialog.findViewById(R.id.okInter);
        okButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    long val = Long.valueOf(input.getText().toString());
                    Editor editor = prefs.edit();
                    editor.putLong("inter", val);
                    editor.commit();
                } catch (NumberFormatException e) {

                } finally {
                    dialog.dismiss();
                }
            }
        });
        Button cancelButton = (Button) dialog.findViewById(R.id.cancelInter);
        cancelButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        return dialog;
    }


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        ApplicationContext.init(getApplicationContext());

		// Hide the title bar.
		//requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		// Set our own layout.
		setContentView(R.layout.main);

		mApp = (NetCounterApplication) getApplication();
//		mModel = mApp.getAdapter(NetCounterModel.class);
//		mContainer = mApp.getAdapter(HandlerContainer.class);


        mDrawerLayout = (DrawerLayout) findViewById(R.id.main_drawer);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        boolean useOSC = mApp.getAdapter(SharedPreferences.class).getBoolean("enableOSC", false);
        DrawerItem mItems[] = new DrawerItem[]{
                new DrawerItem(getString(R.string.prefsCatImportExportTitle), DrawerItem.ItemKind.NO_SWITCH),
                new DrawerItem(getString(R.string.prefsTimerTitle), DrawerItem.ItemKind.NO_SWITCH),
                new DrawerItem(getString(R.string.reset), DrawerItem.ItemKind.NO_SWITCH),
                new DrawerItem(getString(R.string.location), DrawerItem.ItemKind.NO_SWITCH),
                new DrawerItem(getString(R.string.prefsOSC), useOSC ?
                        DrawerItem.ItemKind.ON_SWITCH : DrawerItem.ItemKind.OFF_SWITCH),
                new DrawerItem(getString(R.string.prefsRP), DrawerItem.ItemKind.NO_SWITCH),
                new DrawerItem(getString(R.string.aboutTitle), DrawerItem.ItemKind.NO_SWITCH),
        };


        mDrawerList.setAdapter(new DrawerAdapter(this,
                R.layout.drawer_list_item, mItems));

        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DrawerAdapter.ItemHolder holder = (DrawerAdapter.ItemHolder) view.getTag();
                String name = holder.getName().getText().toString();
                Log.d(TAG, "clicked drawer");
                if(getString(R.string.prefsCatImportExportTitle).equals(name)) {
                    startActivity(new Intent(NetCounterActivity.this, ImportExportActivity.class));
                } else if(getString(R.string.prefsTimerTitle).equals(name)) {
                    getEditTimeDialog().show();
                } else if(getString(R.string.reset).equals(name)) {
                    resetData(null);
                } else if(getString(R.string.location).equals(name)) {
                    startActivity(new Intent(NetCounterActivity.this, MapChooserActivity.class));
                } else if(getString(R.string.prefsRP).equals(name)) {
                    startActivity(new Intent(NetCounterActivity.this, NewMapActivity.class));
                } else if(getString(R.string.aboutTitle).equals(name)) {
                    getAboutDialog().show();
                }
            }
        });

        mDrawerLayout.setDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View view, float v) {
                findViewById(R.id.ham).setPressed(true);
            }

            @Override
            public void onDrawerOpened(View view) {

            }

            @Override
            public void onDrawerClosed(View view) {
                findViewById(R.id.ham).setPressed(false);
            }

            @Override
            public void onDrawerStateChanged(int i) {

            }
        });

        MyLog.d(getClass().getName(), "Activity created.");

//		Button rstButton = (Button) findViewById(R.id.rstButton);

//        rstButton.setOn

	}


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        openDrawer(mDrawerLayout);
        return true;
    }


	@Override
	protected void onDestroy() {
//		setListAdapter(null);
//		mAdapter.setInput(null);
//		unregisterForContextMenu(getExpandableListView());

//        unregisterReceiver(connectionChangeReceiver);

//        stopWifiSniffer();

        stopService(new Intent(this,
                SynchronizationManager.class));


//        stopService(new Intent(this,InternetConnectionManager.class));

//        unbindService(mICMConnection);


        mApp.stopService();
		mApp = null;
//		mModel = null;
//		mContainer = null;
//		mAdapter = null;
//		mCounter = null;

        if(mServRegistered) {
            stopWifiSniffer();
            mWifiService = null;
        }

		super.onDestroy();


		MyLog.d(getClass().getName(), "Activity destroyed.");

	}

	@Override
	protected void onPause() {
		super.onPause();

		//NetCounterApplication.setUpdatePolicy(NetCounterApplication.SERVICE_LOW);
		//NetCounterApplication app = (NetCounterApplication) getApplication();
//		app.startService();



		MyLog.d(getClass().getName(), "Activity paused.");

	}

	@Override
	protected void onResume() {
		super.onResume();

        startService(new Intent(this,
                SynchronizationManager.class));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2){


            BluetoothManager bluetoothManager =
                    (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            BluetoothAdapter btAdapter = bluetoothManager.getAdapter();
            if (!mAskBT && (btAdapter == null || !btAdapter.isEnabled())) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 0);
                mAskBT = false;
            }
        }


        mSharedPreferences = mApp.getAdapter(SharedPreferences.class);
        boolean send = mSharedPreferences.getBoolean("shareData", false);
        if(send) {
//            MyLog.d("NetCounterActivity", "Starting BleController");
            NetCounterApplication.setUpdatePolicy(NetCounterApplication.SERVICE_HIGH);
            NetCounterApplication app = (NetCounterApplication) getApplication();
            MyLog.d("NetCounterActivity", "Calling app.startService()");
            app.startService();
            startService(new Intent(this, BleController.class));
            startWifiSniffer();
            mRedpinLocator.run();
        }


        setStateLayout(send);
//		NetCounterApplication.setUpdatePolicy(NetCounterApplication.SERVICE_HIGH);
//		NetCounterApplication app = (NetCounterApplication) getApplication();
//		app.startService();

//		mModel.addModelListener(this);
//		mModel.addOperationListener(this);

        Intent i = getIntent();
        Bundle extras = i.getExtras();
        if(extras != null) {
            long locationID = extras.getLong("locationID");
//            NewModelAPI.setLocationID(this, locationID);
//            mLocation = new Location();
//            if(locationID < Integer.MAX_VALUE) {
//                mLocation.setRemoteId((int)locationID);
//            }
        }
		
		mRecMode = setModeText();

		enableDirButtons(!mRecMode);


		MyLog.d(getClass().getName(), "Activity resumed.");

	}
	
	private void enableDirButtons(boolean en) {
        MyLog.d(TAG, "dir buttons are now set to " + en);
		final ImageButton fwdBtn = (ImageButton) findViewById(R.id.fwdButton);
		final ImageButton bcdBtn = (ImageButton) findViewById(R.id.bkwButton);
		fwdBtn.setVisibility(en ? View.VISIBLE : View.INVISIBLE);
		bcdBtn.setVisibility(en ? View.VISIBLE : View.INVISIBLE);
		fwdBtn.setEnabled(en);
		bcdBtn.setEnabled(en);

		if (en) {
            fwdBtn.setImageResource(R.drawable.icon_forward_active);
            bcdBtn.setImageResource(R.drawable.icon_backward_unselcted);
//            Spannable fwdLabel = new SpannableString(" ");
//            fwdLabel.setSpan(new ImageSpan(getApplicationContext(), R.drawable.icon_forward_active,
//                    ImageSpan.ALIGN_BOTTOM), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//            fwdBtn.setText(fwdLabel);
//            Spannable bcdLabel = new SpannableString(" ");
//            bcdLabel.setSpan(new ImageSpan(getApplicationContext(), R.drawable.icon_backward_unselcted,
//                    ImageSpan.ALIGN_BOTTOM), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//            bcdBtn.setText(bcdLabel);
//            fwdBtn.setBackgroundDrawable(getResources().getDrawable(R.drawable.icon_forward_active));
//            bcdBtn.setBackgroundDrawable(getResources().getDrawable(R.drawable.icon_backward_unselcted));
            NewModelAPI.setRepDirection(NetCounterActivity.this, true);
			fwdBtn.setOnClickListener(new OnClickListener() {
				
				public void onClick(View v) {
                    fwdBtn.setImageResource(R.drawable.icon_forward_active);
                    bcdBtn.setImageResource(R.drawable.icon_backward_unselcted);
					NewModelAPI.setRepDirection(NetCounterActivity.this, true);
				}
			});
			bcdBtn.setOnClickListener(new OnClickListener() {
				
				public void onClick(View v) {
                    fwdBtn.setImageResource(R.drawable.icon_forward_unselcted);
                    bcdBtn.setImageResource(R.drawable.icon_backward_active);
					NewModelAPI.setRepDirection(NetCounterActivity.this, false);					
				}
			});
		}
	}
	

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
//		if (mCounter != null) {
//			long interfaceId = mCounter.getInterface().getId();
//			long counterId = mCounter.getId();
//			outState.putLongArray("counter", new long[] { interfaceId, counterId });
//		}
		if (NetCounterApplication.LOG_ENABLED) {
			MyLog.d(getClass().getName(), "onSaveInstanceState");
		}
	}

	/**
	 * Returns the version of the application.
	 * 
	 * @return The version.
	 */
	private String getVersion() {
		try {
			return getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			// Ignore.
		}
		return "";
	}

	
	private boolean setModeText() {
//		SharedPreferences preferences = mApp.getAdapter(SharedPreferences.class);
		
		boolean recMode = mSharedPreferences.getString("mode", "0").equals("0");

        TextView live = (TextView) findViewById(R.id.liveMode);
        TextView playback = (TextView) findViewById(R.id.pbMode);
        if(recMode) {
            live.setTextColor(getResources().getColor(R.color.text_white));
            live.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_border));
            playback.setTextColor(getResources().getColor(R.color.text_dark));
            playback.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_border_dark));
        } else {
            live.setTextColor(getResources().getColor(R.color.text_dark));
            live.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_border_dark));
            playback.setTextColor(getResources().getColor(R.color.text_white));
            playback.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_border));
        }
		
//		TextView modeTitle = (TextView) findViewById(R.id.currentMode);
		
//		modeTitle.setText(recMode ? getString(R.string.modeRec) : getString(R.string.modeRep));
//		modeTitle.setTextColor("#FFF0FFFF");
		
		return recMode;
	}
	
	public void changeSendState(View v) {
		
		ImageButton strtButton = (ImageButton) findViewById(R.id.startButton);
		
//		SharedPreferences preferences = mApp.getAdapter(SharedPreferences.class);
		
		boolean start = !mSharedPreferences.getBoolean("shareData", false);
		
		Editor editor = mSharedPreferences.edit();
		editor.putBoolean("shareData", start).commit();

        strtButton.setImageResource(start ? R.drawable.icon_stop : R.drawable.icon_play);

//		strtButton.getBackground().setColorFilter(start ? Color.RED : Color.GREEN, PorterDuff.Mode.MULTIPLY);
//		strtButton.setText(start ? R.string.stop : R.string.start);
		
		
		stopService(new Intent(this, BleController.class));
		
		//Switch to high/low freq data points
		if (start) {
//			MyLog.d("DEBUG", msg)
			NetCounterApplication.setUpdatePolicy(NetCounterApplication.SERVICE_HIGH);
			NetCounterApplication app = (NetCounterApplication) getApplication();
            MyLog.d("NetCounterActivity", "Calling app.startService()");
			app.startService();
			startService(new Intent(this, BleController.class));
            startWifiSniffer();
            mRedpinLocator.run();
//            mWifiService.forceMeasurement();
//			startService(new Intent(this, CallDetectService.class));

//			mModel.addModelListener(this);
//			mModel.addOperationListener(this);
			
			mRecMode = setModeText();
		} else {
			MyLog.d("DEBUG", "update = SERVICE_LOW");
			NetCounterApplication.setUpdatePolicy(NetCounterApplication.SERVICE_LOW);
			NetCounterApplication app = (NetCounterApplication) getApplication();
			app.stopService();
            if(mServRegistered) {
                stopWifiSniffer();
            }

            mRedpinHandler.removeCallbacks(mRedpinLocator);
			//app.startService();

//			mModel.removeModelListener(this);
//			mModel.removeOperationListener(this);
		}
		
	}
	
	private void setStateLayout(boolean send) {
		
		ImageButton strtButton = (ImageButton) findViewById(R.id.startButton);
		
		strtButton.setImageResource(send ? R.drawable.icon_stop : R.drawable.icon_play);
//		strtButton.getBackground().setColorFilter(send ? Color.RED : Color.GREEN, PorterDuff.Mode.MULTIPLY);
//		strtButton.setText(send ? R.string.stop : R.string.start);
	}
	
	public void resetData(View v) {
        MyLog.d(TAG, "resetData");
		NewModelAPI.clearDB(this);
	}

    public void locateMe(View v) {
        startActivity(new Intent(NetCounterActivity.this, MapChooserActivity.class));
    }

    public void liveClick(View v) {
        if(!mRecMode) {
//            SharedPreferences preferences = mApp.getAdapter(SharedPreferences.class);
            Editor editor = mSharedPreferences.edit();

            editor.putString("mode", "0");
            editor.commit();
            mRecMode = setModeText();
            enableDirButtons(false);
//            boolean recMode = preferences.getString("mode", "0").equals("0");
        }
    }

    public void playbackClick(View v) {
        if(mRecMode) {
//            SharedPreferences preferences = mApp.getAdapter(SharedPreferences.class);
            Editor editor = mSharedPreferences.edit();

            editor.putString("mode", "1");
            editor.commit();
            mRecMode = setModeText();
            enableDirButtons(true);

        }
    }

    public void openDrawer(View v) {
        mDrawerLayout.openDrawer(mDrawerList);
    }

    private ServiceConnection mICMConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            InternetConnectionManager mManager = ((InternetConnectionManager.LocalBinder) service)
                    .getService();
//            setOnlineMode(mManager.isOnline());
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }

    };

    /**
     * Receives notifications about connectivity changes
     */
    private BroadcastReceiver connectionChangeReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
//            setOnlineMode((intent.getFlags() & InternetConnectionManager.ONLINE_FLAG)== InternetConnectionManager.ONLINE_FLAG);
        }

    };

    /**
     * Starts the sniffer and registers the receiver
     */
    private void startWifiSniffer() {
        bindService(new Intent(this, WifiSniffer.class), mWifiConnection,
                Context.BIND_AUTO_CREATE);
        registerReceiver(wifiReceiver,
                new IntentFilter(WifiSniffer.WIFI_ACTION));
        MyLog.i("NetCounterActivity", "Started WifiSniffer");
    }

    /**
     * Stops the sniffer and unregisters the receiver
     */
    private void stopWifiSniffer() {
        mServRegistered = false;
        if (mWifiService != null) {
            mWifiService.stopMeasuring();
        }
        unbindService(mWifiConnection);
        unregisterReceiver(wifiReceiver);
        MyLog.i("NetCounterActivity", "Stopped WifiSniffer");
    }

    private BroadcastReceiver wifiReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            Log.d(TAG, "Received wifi measurement");
            Measurement m = mWifiService.retrieveLastMeasurement();

            if (m == null) {
                Log.d(TAG, "Measurement was null");
                return;
            }

            //we're editing a map
            if (mLocation != null) {
                // Interval Scanning
                Fingerprint fp = new Fingerprint(mLocation, m);
                FingerprintRemoteHome.setFingerprint(fp,
                        new RemoteEntityHomeCallback() {

                            @Override
                            public void onResponse(Response<?> response) {

                                if (firstMeasurement) {
//                                    progressDialog.hide();
//                                    mapView.addNewLocation(mLocation);
                                    firstMeasurement = false;
                                }

                                MyLog
                                        .i(TAG,
                                                "addNewLocation: setFingerprint successful");
                            }

                            @Override
                            public void onFailure(Response<?> response) {
//                                progressDialog.hide();
                                MyLog.i(TAG,
                                        "addNewLocation: setFingerprint failed: "
                                                + response.getStatus() + ", "
                                                + response.getMessage());
                            }
                        });

            } else {
                // Localization
                MyLog.d(TAG, "Localizing...");
                LocationRemoteHome.getLocation(m,
                        new RemoteEntityHomeCallback() {

                            @Override
                            public void onFailure(Response<?> response) {
//                                progressDialog.hide();

                                new AlertDialog.Builder(NetCounterActivity.this).setMessage(response.getMessage()).setPositiveButton(android.R.string.ok, null).create().show();

                            }

                            @Override
                            public void onResponse(Response<?> response) {
//                                progressDialog.hide();
                                MyLog.d(TAG, "Localization successful!");
                                Location l = (Location) response.getData();


                                NewModelAPI.setLocationID(NetCounterActivity.this, l !=null ? l.getRemoteId() : 0);

//                                showLocation(l);

                            }

                        });
                mWifiService.stopMeasuring();
            }

        }
    };
    /**
     * {@link ServiceConnection} for the {@link WifiSniffer}
     */
    private ServiceConnection mWifiConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            MyLog.d(TAG, "Wifi Connection connected.");
            mWifiService = ((WifiSniffer.LocalBinder) service).getService();
//            mWifiService.forceMeasurement();
            mServRegistered = true;
            mRedpinLocator.run();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            MyLog.d(TAG, "Wifi Connection deconnected");
            mWifiService = null;
            mServRegistered = false;
        }

    };

    private class DrawerAdapter extends ArrayAdapter<DrawerItem> {
        private Context context;
        private int layoutResourceId;
        private DrawerItem data[] = null;

        public DrawerAdapter(Context context, int layoutResourceId, DrawerItem[] data) {
            super(context, layoutResourceId, data);
            this.layoutResourceId = layoutResourceId;
            this.context = context;
            this.data = data;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ItemHolder holder = null;
            LayoutInflater inflater = getLayoutInflater();

            if(convertView == null) {
                convertView = inflater.inflate(layoutResourceId, null, false);
                holder = new ItemHolder(convertView);
                convertView.setTag(holder);
            } else {
//                Log.d(TAG, "view was not null");
                holder = (ItemHolder) convertView.getTag();
            }

            final String name = data[position].getName();
            holder.getName().setText(name);
            DrawerItem.ItemKind kind = data[position].getKind();
            holder.getToggle().setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    CheckBox cb = (CheckBox) v;
                    if(getString(R.string.prefsOSC).equals(name)) {
                        Log.d(TAG, "OSC = " + cb.isChecked());
                        Editor editor = mSharedPreferences.edit();
                        editor.putBoolean("enableOSC", cb.isChecked());
                        editor.commit();
                    }
                }
            });
//            Log.d(TAG, "Got item = " + data[position].getName() + "::" + data[position].getKind());
            switch (kind) {
                case NO_SWITCH:
                    holder.getToggle().setVisibility(View.GONE);
                    break;
                case ON_SWITCH:
                    holder.getToggle().setVisibility(View.VISIBLE);
                    holder.getToggle().setChecked(true);
                    break;
                case OFF_SWITCH:
                    holder.getToggle().setVisibility(View.VISIBLE);
                    holder.getToggle().setChecked(false);
                    break;
                default:
                    break;
            }
            return convertView;
        }

        private class ItemHolder {
            private View row;
            private TextView name = null;
            private CheckBox toggle = null;

            public ItemHolder(View v) {
                row = v;
            }

            public TextView getName() {
                if(name == null) {
                    name = (TextView) row.findViewById(R.id.drawer_text);
                }
                return name;
            }

            public CheckBox getToggle() {
                if(toggle == null) {
                    toggle = (CheckBox) row.findViewById(R.id.drawer_toggle);
                }
                return toggle;
            }
        }

    }


}