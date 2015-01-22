package net.lmag.connectornot.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import net.lmag.connectornot.NetCounterApplication;
import net.lmag.connectornot.R;
import net.lmag.connectornot.br.com.thinkti.android.filechooser.FileChooser;
import net.lmag.connectornot.model.MyLog;
import net.lmag.connectornot.model.NewModelAPI;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class ImportExportActivity extends Activity {
    private static final int FILE_CHOOSER = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_export);
        TextView v = (TextView) findViewById(R.id.export_text);
        v.setText(Html.fromHtml(getString(R.string.export_html)));
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exportData();
            }
        });

        v = (TextView) findViewById(R.id.import_text);
        v.setText(Html.fromHtml(getString(R.string.import_html)));
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder d = new AlertDialog.Builder(ImportExportActivity.this);
                d.setTitle(R.string.importTitle);
                d.setMessage(R.string.importAlertText);
                d.setNegativeButton(R.string.no, null);
                d.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        chooseFile();
                    }
                });
                d.create().show();
            }
        });

        v = (TextView) findViewById(R.id.upload_text);
        v.setText(Html.fromHtml(getString(R.string.upload_html)));
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadData();
            }
        });
    }


    private void exportData() {
        final ProgressDialog pd = ProgressDialog.show(ImportExportActivity.this,
                getString(R.string.exportDialogTitle), getString(R.string.exportDialogText), true);
        final NetCounterApplication app = (NetCounterApplication) getApplication();
//		final NetCounterModel m = app.getAdapter(NetCounterModel.class);
//		HandlerContainer hdlr = app.getAdapter(HandlerContainer.class);
        new Handler().post(new Runnable() {
            public void run() {

                try {
//					String f = m.exportDataToCsv();
                    String f = NewModelAPI.exportToCsv(app);

                    app.toast(getString(R.string.exportSuccessful, f));
                } catch (IOException e) {
                    MyLog.e("NetCounterPreferences", "IO in exportToCSV", e);
                    app.toast(R.string.exportFailed);
                }
                finally {
                    pd.dismiss();
                }
            }
        });
    }

    private void uploadData() {
        final NetCounterApplication app = (NetCounterApplication) getApplication();

        NewModelAPI.exportDataToServ(app);
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

        final ProgressDialog pd = ProgressDialog.show(ImportExportActivity.this,
                getString(R.string.importDialogTitle), getString(R.string.importDialogText), true);
        final NetCounterApplication app = (NetCounterApplication) getApplication();
//		final NetCounterModel m = app.getAdapter(NetCounterModel.class);
        //HandlerContainer hdlr = app.getAdapter(HandlerContainer.class);
        new Handler().post(new Runnable() {
            public void run() {
                try {
                    NewModelAPI.importFromCsv(app, new File(file));
//					m.importDataFromCsv();
//					app.toast(R.string.importSuccessful);
                } catch (IOException e) {
                    MyLog.e("NetCounterPreferences", "IO in importFromCSV", e);
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
