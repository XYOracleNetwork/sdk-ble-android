package network.xyo.sdk.ble.sample.java;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import network.xyo.ble.generic.scanner.XYSmartScanListener;
import network.xyo.ble.utilities.XYPromise;
import network.xyo.ble.devices.xy.XY2BluetoothDevice;
import network.xyo.ble.devices.xy.XY3BluetoothDevice;
import network.xyo.ble.devices.xy.XY4BluetoothDevice;
import network.xyo.ble.generic.devices.XYBluetoothDevice;
import network.xyo.ble.generic.scanner.XYSmartScanPromise;
import network.xyo.ble.generic.scanner.XYSmartScanLegacy;
import network.xyo.ble.generic.scanner.XYSmartScanModern;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;

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

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show());

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

        XY4BluetoothDevice.Companion.enable(true);
        XY3BluetoothDevice.Companion.enable(true);
        XY2BluetoothDevice.Companion.enable(true);
    }

    private void setupRecyclerView(RecyclerView recyclerView) {
        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(this, mTwoPane));
    }

    private XYSmartScanPromise _scanner;

    private XYSmartScanPromise getScanner() {
        synchronized (this) {
            if (_scanner == null) {
                if (Build.VERSION.SDK_INT >= 21) {
                    _scanner = new XYSmartScanPromise(new XYSmartScanModern(this.getApplicationContext()));
                } else {
                    _scanner = new XYSmartScanPromise(new XYSmartScanLegacy(this.getApplicationContext()));
                }
            }
            return _scanner;
        }
    }

    @Override
    protected void onPause() {
        getScanner().stop(new XYPromise<Boolean>() {
            @Override
            public void resolve(Boolean value) {
                super.resolve(value);
            }
        });
        super.onPause();
    }

    public static class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final ItemListActivity mParentActivity;
        private final List<XYBluetoothDevice> mValues;
        private final boolean mTwoPane;
        private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mTwoPane) {
                    ItemDetailFragment fragment = new ItemDetailFragment();
                    mParentActivity.getSupportFragmentManager().beginTransaction()
                            .replace(R.id.item_detail_container, fragment)
                            .commit();
                } else {
                    Context context = view.getContext();
                    Intent intent = new Intent(context, ItemDetailActivity.class);
                    context.startActivity(intent);
                }
            }
        };

        SimpleItemRecyclerViewAdapter(final ItemListActivity parent,
                                      boolean twoPane) {
            mValues = new ArrayList<>();
            mParentActivity = parent;
            mTwoPane = twoPane;

            parent.getScanner().getScanner().addListener("Wrapper", new XYSmartScanListener() {
                @Override
                public void entered(@NotNull XYBluetoothDevice device) {
                    super.entered(device);
                    mValues.add(device);
                    mParentActivity.runOnUiThread(() -> notifyDataSetChanged());
                }

                @Override
                public void exited(@NotNull XYBluetoothDevice device) {
                    super.exited(device);
                    mValues.remove(device);
                    mParentActivity.runOnUiThread(() -> notifyDataSetChanged());
                }
            });
        }

        @Override
        public @NotNull ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
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

        static class ViewHolder extends RecyclerView.ViewHolder {
            final TextView mIdView;
            final TextView mContentView;

            ViewHolder(View view) {
                super(view);
                mIdView = view.findViewById(R.id.id_text);
                mContentView = view.findViewById(R.id.content);
            }
        }
    }
}
