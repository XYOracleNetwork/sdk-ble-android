package network.xyo.sdk.ble.sample.java;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import network.xyo.ble.devices.XY2BluetoothDevice;
import network.xyo.ble.devices.XY3BluetoothDevice;
import network.xyo.ble.devices.XY4BluetoothDevice;
import network.xyo.ble.devices.XYAppleBluetoothDevice;
import network.xyo.ble.devices.XYBluetoothDevice;
import network.xyo.ble.devices.XYFinderBluetoothDevice;
import network.xyo.ble.devices.XYGpsBluetoothDevice;
import network.xyo.ble.devices.XYIBeaconBluetoothDevice;
import network.xyo.ble.scanner.XYSmartScan;
import network.xyo.ble.scanner.XYSmartScanPromiseWrapper;
import network.xyo.ble.scanner.XYSmartScanLegacy;
import network.xyo.ble.scanner.XYSmartScanModern;
import network.xyo.sdk.ble.sample.java.dummy.DummyContent;

import java.util.ArrayList;
import java.util.List;

/**
 * An activity representing a list of Items. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link ItemDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class ItemListActivity extends AppCompatActivity {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        if (findViewById(R.id.item_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }

        View recyclerView = findViewById(R.id.item_list);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);

        XYAppleBluetoothDevice.Companion.enable(true);
        XYIBeaconBluetoothDevice.Companion.enable(true);
        XYFinderBluetoothDevice.Companion.enable(true);
        XY4BluetoothDevice.Companion.enable(true);
        XY3BluetoothDevice.Companion.enable(true);
        XY2BluetoothDevice.Companion.enable(true);
        XYGpsBluetoothDevice.Companion.enable(true);
    }

    private void setupRecyclerView(@NotNull RecyclerView recyclerView) {
        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(this, mTwoPane));
    }

    private XYSmartScanPromiseWrapper _scanner;

    protected XYSmartScanPromiseWrapper getScanner() {
        synchronized (this) {
            if (_scanner == null) {
                if (Build.VERSION.SDK_INT >= 21) {
                    _scanner = new XYSmartScanPromiseWrapper(new XYSmartScanModern(this.getApplicationContext()));
                } else {
                    _scanner = new XYSmartScanPromiseWrapper(new XYSmartScanLegacy(this.getApplicationContext()));
                }
            }
            return _scanner;
        }
    }

    @Override
    protected void onPause() {
        getScanner().stop();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public static class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final ItemListActivity mParentActivity;
        final SimpleItemRecyclerViewAdapter self;
        private final List<XYBluetoothDevice> mValues;
        private final boolean mTwoPane;
        private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DummyContent.DummyItem item = (DummyContent.DummyItem) view.getTag();
                if (mTwoPane) {
                    Bundle arguments = new Bundle();
                    arguments.putString(ItemDetailFragment.ARG_ITEM_ID, item.id);
                    ItemDetailFragment fragment = new ItemDetailFragment();
                    fragment.setArguments(arguments);
                    mParentActivity.getSupportFragmentManager().beginTransaction()
                            .replace(R.id.item_detail_container, fragment)
                            .commit();
                } else {
                    Context context = view.getContext();
                    Intent intent = new Intent(context, ItemDetailActivity.class);
                    intent.putExtra(ItemDetailFragment.ARG_ITEM_ID, item.id);

                    context.startActivity(intent);
                }
            }
        };

        SimpleItemRecyclerViewAdapter(final ItemListActivity parent,
                                      boolean twoPane) {
            mValues = new ArrayList<XYBluetoothDevice>();
            mParentActivity = parent;
            mTwoPane = twoPane;
            self = this;

            parent.getScanner().getScanner().addListener("Wrapper", new XYSmartScan.Listener() {
                @Override
                public void entered(@NotNull XYBluetoothDevice device) {
                    super.entered(device);
                    mValues.add(device);
                    self.mParentActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            self.notifyDataSetChanged();
                        }
                    });
                }

                @Override
                public void exited(@NotNull XYBluetoothDevice device) {
                    super.exited(device);
                    mValues.remove(device);
                    self.mParentActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            self.notifyDataSetChanged();
                        }
                    });
                }
            });
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.mIdView.setText(mValues.get(position).getId());
            holder.mContentView.setText(mValues.get(position).getAddress());

            holder.itemView.setTag(mValues.get(position));
            holder.itemView.setOnClickListener(mOnClickListener);
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final TextView mIdView;
            final TextView mContentView;

            ViewHolder(View view) {
                super(view);
                mIdView = (TextView) view.findViewById(R.id.id_text);
                mContentView = (TextView) view.findViewById(R.id.content);
            }
        }
    }
}
