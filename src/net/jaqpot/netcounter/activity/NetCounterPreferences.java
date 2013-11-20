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

package net.jaqpot.netcounter.activity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import net.jaqpot.netcounter.HandlerContainer;
import net.jaqpot.netcounter.NetCounterApplication;
import net.jaqpot.netcounter.R;
import net.jaqpot.netcounter.br.com.thinkti.android.filechooser.FileChooser;
import net.jaqpot.netcounter.model.NewModelAPI;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.widget.Toast;

/**
 * {@link NetCounterPreferences} is the preference screen for NetCounter.
 */
public class NetCounterPreferences extends PreferenceActivity {
	
	private static final int FILE_CHOOSER = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Initializes the preference activity.
		addPreferencesFromResource(R.xml.preferences);

		// Export.
		Preference pref = findPreference("export");
		pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				exportData();
				return true;
			}
		});

		// Import.
		pref = findPreference("import");
		pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				// Creates the dialog.
				AlertDialog.Builder d = new AlertDialog.Builder(NetCounterPreferences.this);
				d.setTitle(R.string.importTitle);
				d.setMessage(R.string.importAlertText);
				d.setNegativeButton(R.string.no, null);
				d.setPositiveButton(R.string.yes, new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						chooseFile();
					}
				});
				d.show();
				return true;
			}
		});
	}

	/**
	 * Exports the data in a separate thread.
	 */
	private void exportData() {
		final ProgressDialog pd = ProgressDialog.show(NetCounterPreferences.this,
				getString(R.string.exportDialogTitle), getString(R.string.exportDialogText), true);
		final NetCounterApplication app = (NetCounterApplication) getApplication();
//		final NetCounterModel m = app.getAdapter(NetCounterModel.class);
		HandlerContainer hdlr = app.getAdapter(HandlerContainer.class);
		hdlr.getSlowHandler().post(new Runnable() {
			public void run() {
				try {
//					String f = m.exportDataToCsv();
					String f = NewModelAPI.exportToCsv(app);
					app.toast(getString(R.string.exportSuccessful, f));
				} catch (IOException e) {
					Log.e("NetCounterPreferences", "IO in exportToCSV", e);
					app.toast(R.string.exportFailed);
				} finally {
					pd.dismiss();
				}
			}
		});
	}
	
	private void chooseFile() {
		Intent intent = new Intent(this, FileChooser.class);
		ArrayList<String> extensions = new ArrayList<String>();
		extensions.add(".csv");
		intent.putStringArrayListExtra("filterFileExtension", extensions);
		startActivityForResult(intent, FILE_CHOOSER);
	}

	/**
	 * Imports the data in a separate thread.
	 */
	private void importData(final String file) {
		
		final ProgressDialog pd = ProgressDialog.show(NetCounterPreferences.this,
				getString(R.string.importDialogTitle), getString(R.string.importDialogText), true);
		final NetCounterApplication app = (NetCounterApplication) getApplication();
//		final NetCounterModel m = app.getAdapter(NetCounterModel.class);
		HandlerContainer hdlr = app.getAdapter(HandlerContainer.class);
		hdlr.getSlowHandler().post(new Runnable() {
			public void run() {
				try {
					NewModelAPI.importFromCsv(app, new File(file));
//					m.importDataFromCsv();
//					app.toast(R.string.importSuccessful);
				} catch (IOException e) {
					Log.e("NetCounterPreferences", "IO in importFromCSV", e);
					app.toast(R.string.importFailed);
				} finally {
					pd.dismiss();
				}
			}
		});
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if ((requestCode == FILE_CHOOSER) && (resultCode == -1)) {
    		String fileSelected = data.getStringExtra("fileSelected");
    		Toast.makeText(this, fileSelected, Toast.LENGTH_SHORT).show();
    		if (fileSelected != null && fileSelected.endsWith(".csv")) {
    			importData(fileSelected);
    		}
    	}
    		
    }

}
