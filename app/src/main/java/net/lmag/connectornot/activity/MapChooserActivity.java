package net.lmag.connectornot.activity;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import net.lmag.connectornot.R;

import org.redpin.android.core.Map;
import org.redpin.android.db.EntityHomeFactory;
import org.redpin.android.db.MapHome;
import org.redpin.android.net.InternetConnectionManager;
import org.redpin.android.net.home.MapRemoteHome;
import org.redpin.android.provider.RedpinContract;
import org.redpin.android.ui.MapViewActivity;
import org.redpin.android.ui.NewMapActivity;
import org.redpin.android.ui.list.LocationListActivity;
import org.redpin.android.ui.list.MapCursorAdapter;

public class MapChooserActivity extends ListActivity implements
        AdapterView.OnItemClickListener, View.OnCreateContextMenuListener {

    private boolean isOnline = false;
    private MapHome mapHome;

    private String TAG = MapChooserActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bindService(new Intent(this, InternetConnectionManager.class), mConnection, Context.BIND_AUTO_CREATE);

        //Update list upon reconnecting to the server.
        registerReceiver(connectionChangeReceiver, new IntentFilter(
                InternetConnectionManager.CONNECTIVITY_ACTION));

        setContentView(org.redpin.android.R.layout.list_view_rp);

        Button foot = (Button) findViewById(org.redpin.android.R.id.footer);
        foot.setVisibility(View.VISIBLE);
        foot.setText("Add Space");
        foot.setTextSize(17);
        foot.setPadding(5, 5, 5, 5);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            foot.setBackground(getDrawable(R.drawable.button_border));
//        } else {
            foot.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_border));
//        }
//        foot.setBackgroundColor(Color.TRANSPARENT);
        foot.setTextColor(getResources().getColor(R.color.text_white));

        foot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MapChooserActivity.this, NewMapActivity.class));
            }
        });

        Intent intent = getIntent();
        if (intent.getData() == null) {
            intent.setData(RedpinContract.Map.CONTENT_URI);
        }

        View v = findViewById(org.redpin.android.R.id.list_view_topbar_text);
        if (v instanceof TextView) {
            ((TextView) v).setText(org.redpin.android.R.string.list_view_topbar_maplist);
        }

        setListAdapter(new MapCursorAdapter(this, getIntent().getData()));
        ListView lv = getListView();

        registerForContextMenu(lv);
        lv.setClickable(true);
        lv.setOnItemClickListener(this);
    }


    @Override
    protected void onDestroy() {
        unbindService(mConnection);
        unregisterReceiver(connectionChangeReceiver);
        super.onDestroy();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        if (position < 0)
            return;

        Intent i = new Intent(this, NetCounterActivity.class);
//        i.setData(RedpinContract.Map.buildQueryUri(id));
        i.putExtra("locationID", id);
        startActivity(i);

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if (getListAdapter().isEmpty())
            return false;

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View view,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;

        Cursor cursor = (Cursor) getListAdapter().getItem(info.position);
        if (cursor == null) {
            return;
        }

        menu.setHeaderTitle(cursor.getString(cursor
                .getColumnIndex(RedpinContract.Map.NAME)));

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(org.redpin.android.R.menu.context_menu_map_list, menu);

        MenuItem deleteItem = menu.getItem(1);
        deleteItem.setEnabled(isOnline);

    }

    private AlertDialog getConfirmationDialog(final Map m) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        View v = getLayoutInflater().inflate(R.layout.map_del_dialog, null);

        builder.setView(v);

        final AlertDialog dialog = builder.create();

        Button yes = (Button) v.findViewById(R.id.map_del_yes);
        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                MapRemoteHome.removeMap(m);
            }
        });

        Button no = (Button) v.findViewById(R.id.map_del_no);
        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        return dialog;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
                .getMenuInfo();

        int id = item.getItemId();

        if(id == org.redpin.android.R.id.menu_delete) {
            System.out.println("Delete item with id:" + info.id);

            Cursor cursor = (Cursor) getListAdapter().getItem(info.position);
            if (cursor == null) {
                return true;
            }

            if (mapHome == null) {
                mapHome = EntityHomeFactory.getMapHome();
            }
            Map m = mapHome.fromCursorRow(cursor);
            // mapHome.remove(m);
            Log.i(TAG, "deleting map: " + m);

            getConfirmationDialog(m).show();

            return true;
        }

        if(id == org.redpin.android.R.id.menu_show_locations) {

            Intent i = new Intent(MapChooserActivity.this,
                    LocationListActivity.class);
            i.setData(RedpinContract.Location.buildFilterUri(info.id));
            startActivity(i);

            return true;
        }

        if(id == org.redpin.android.R.id.menu_show_map) {

            Intent i = new Intent(MapChooserActivity.this,
                    MapViewActivity.class);
            i.setData(RedpinContract.Map.buildQueryUri(info.id));
            startActivity(i);

            return true;
        }

        return false;
    }

    private BroadcastReceiver connectionChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            isOnline = (intent.getFlags() & InternetConnectionManager.ONLINE_FLAG)== InternetConnectionManager.ONLINE_FLAG;
        }
    };

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            InternetConnectionManager mManager = ((InternetConnectionManager.LocalBinder)service).getService();
            isOnline = mManager.isOnline();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }

    };
}
