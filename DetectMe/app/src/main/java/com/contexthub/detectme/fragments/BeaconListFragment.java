package com.contexthub.detectme.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.chaione.contexthub.sdk.BeaconProxy;
import com.chaione.contexthub.sdk.ContextHub;
import com.chaione.contexthub.sdk.ProximityService;
import com.chaione.contexthub.sdk.SensorPipelineEvent;
import com.chaione.contexthub.sdk.SensorPipelineListener;
import com.chaione.contexthub.sdk.callbacks.Callback;
import com.chaione.contexthub.sdk.model.Beacon;
import com.contexthub.detectme.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by andy on 10/15/14.
 */
public class BeaconListFragment extends Fragment implements Callback<List<Beacon>>, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, SensorPipelineListener {

    @InjectView(android.R.id.list) ListView list;
    @InjectView(android.R.id.empty) TextView empty;

    BeaconAdapter adapter;
    BeaconProxy proxy = new BeaconProxy();
    Listener listener;

    public interface Listener {
        public void onItemClick(Beacon beacon);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onResume() {
        super.onResume();

        // start listening to events
        ContextHub.getInstance().addSensorPipelineListener(this);

        // enable beacon ranging
        ProximityService.getInstance().setBackgroundMode(false);
    }

    @Override
    public void onPause() {
        super.onPause();

        // stop listening to events
        ContextHub.getInstance().removeSensorPipelineListener(this);

        // disable beacon ranging
        ProximityService.getInstance().setBackgroundMode(true);
    }

    @Override
    public void onAttach(Activity activity) {
        if(activity instanceof Listener) {
            listener = (Listener) activity;
        }
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_beacon_list, container, false);
        ButterKnife.inject(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        getActivity().setTitle(R.string.beacons);
        adapter = new BeaconAdapter(getActivity(), new ArrayList<Beacon>());
        list.setAdapter(adapter);
        list.setEmptyView(empty);
        list.setOnItemClickListener(this);
        list.setOnItemLongClickListener(this);
        loadItems();
    }

    private void loadItems() {
        getActivity().setProgressBarIndeterminateVisibility(true);
        proxy.listBeacons(this);
    }

    /**
     * Called after successfully fetching beacons from ContextHub
     * @param beacons the resulting beacons
     */
    @Override
    public void onSuccess(List<Beacon> beacons) {
        getActivity().setProgressBarIndeterminateVisibility(false);
        adapter.clear();
        adapter.addAll(beacons);
    }

    /**
     * Called when an error occurs fetching beacons from ContextHub
     * @param e the exception details
     */
    @Override
    public void onFailure(Exception e) {
        getActivity().setProgressBarIndeterminateVisibility(false);
        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Beacon beacon = (Beacon) adapterView.getAdapter().getItem(i);
        listener.onItemClick(beacon);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        Beacon beacon = (Beacon) adapterView.getAdapter().getItem(i);
        showDeleteConfirmDialog(beacon);
        return true;
    }

    private void showDeleteConfirmDialog(final Beacon beacon) {
        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.confirm_delete_title)
                .setMessage(R.string.confirm_delete_message)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        getActivity().setProgressBarIndeterminateVisibility(true);
                        // Submit a request to ContextHub to delete the specified beacon
                        proxy.deleteBeacon(beacon.getId(), deleteCallback);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .create();
        dialog.show();
    }

    private Callback<Object> deleteCallback = new Callback<Object>() {

        /**
         * Called after successfully deleting a beacon from ContextHub
         * @param o
         */
        @Override
        public void onSuccess(Object o) {
            getActivity().setProgressBarIndeterminateVisibility(false);

            /* If you do not have push properly set up, you need to explicitly call synchronize on
               ProximityService so it will stop generating events for this device */
            ProximityService.getInstance().synchronize();

            Toast.makeText(getActivity(), R.string.beacon_deleted, Toast.LENGTH_SHORT).show();
            loadItems();
        }

        /**
         * Called when an error occurs deleting a beacon from ContextHub
         * @param e the exception details
         */
        @Override
        public void onFailure(Exception e) {
            getActivity().setProgressBarIndeterminateVisibility(false);
            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    };

    class BeaconAdapter extends ArrayAdapter<Beacon> {

        public BeaconAdapter(Context context, List<Beacon> objects) {
            super(context, -1, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if(convertView == null) {
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.beacon_item, parent, false);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            }
            else {
                holder = (ViewHolder) convertView.getTag();
            }

            Beacon beacon = getItem(position);
            holder.name.setText(getString(R.string.name_label, beacon.getName()));
            holder.major.setText(getString(R.string.major_label, String.valueOf(beacon.getMajor())));
            holder.minor.setText(getString(R.string.minor_label, String.valueOf(beacon.getMinor())));

            boolean beaconVisible = beacon.getDistance() > 0;
            holder.inOut.setText(beaconVisible ? R.string.in : R.string.out);
            holder.inOut.setTextColor(getResources().getColor(beaconVisible ?
                    android.R.color.holo_green_dark : android.R.color.holo_red_dark));
            holder.proximity.setText(beaconVisible ?
                    getProximityStringResource(beacon.getDistance()) : R.string.not_applicable);
            holder.uuid.setText(beacon.getUuid());

            return convertView;
        }

        private int getProximityStringResource(double distance) {
            if(distance <= .3) {
                return R.string.immediate;
            }
            else if(distance <= 2) {
                return R.string.near;
            }
            else {
                return R.string.far;
            }
        }
    }

    class ViewHolder {
        @InjectView(R.id.name) TextView name;
        @InjectView(R.id.major) TextView major;
        @InjectView(R.id.minor) TextView minor;
        @InjectView(R.id.in_out) TextView inOut;
        @InjectView(R.id.proximity) TextView proximity;
        @InjectView(R.id.uuid) TextView uuid;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }

    @Override
    public void onEventReceived(final SensorPipelineEvent event) {
        if(event.getName().startsWith("beacon_")) {
            // called on background thread, so use a Runnable to perform work on UI thread
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adapter.clear();
                    adapter.addAll(ProximityService.getInstance().getMonitoredBeacons());
                    adapter.notifyDataSetChanged();
                }
            });
        }
    }

    @Override
    public boolean shouldPostEvent(SensorPipelineEvent event) {
        // return true to allow events to post, false to prevent them from posting
        return true;
    }

    @Override
    public void onBeforeEventPosted(SensorPipelineEvent event) {
        // add any extra details to the event before it is posted
    }

    @Override
    public void onEventPosted(SensorPipelineEvent event) {
        // handle an event after it has been posted to ContextHub
    }
}
