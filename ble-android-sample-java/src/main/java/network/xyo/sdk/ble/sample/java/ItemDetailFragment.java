package network.xyo.sdk.ble.sample.java;

import android.app.Activity;

import com.google.android.material.appbar.CollapsingToolbarLayout;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Objects;

/**
 * A fragment representing a single Item detail screen.
 * This fragment is either contained in a {@link ItemListActivity}
 * in two-pane mode (on tablets) or a {@link ItemDetailActivity}
 * on handsets.
 */
class ItemDetailFragment extends Fragment {

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ItemDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Activity activity = this.getActivity();
        CollapsingToolbarLayout appBarLayout = Objects.requireNonNull(activity).findViewById(R.id.toolbar_layout);
        if (appBarLayout != null) {
            appBarLayout.setTitle("Title");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.item_detail, container, false);

        ((TextView) rootView.findViewById(R.id.item_detail)).setText("");

        return rootView;
    }
}
