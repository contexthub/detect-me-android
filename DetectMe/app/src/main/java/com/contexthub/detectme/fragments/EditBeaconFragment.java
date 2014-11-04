package com.contexthub.detectme.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.chaione.contexthub.sdk.BeaconProxy;
import com.chaione.contexthub.sdk.ProximityService;
import com.chaione.contexthub.sdk.callbacks.Callback;
import com.chaione.contexthub.sdk.model.Beacon;
import com.contexthub.detectme.R;

import java.util.ArrayList;
import java.util.Arrays;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by andy on 10/14/14.
 */
public class EditBeaconFragment extends Fragment implements Callback<Beacon> {

    private static final String ARG_BEACON_ID = "beacon_id";
    private static final String ARG_BEACON = "beacon";

    @InjectView(R.id.name) EditText name;
    @InjectView(R.id.major) EditText major;
    @InjectView(R.id.minor) EditText minor;
    @InjectView(R.id.uuid) EditText uuid;

    long beaconId = -1;
    Beacon beacon;

    public static EditBeaconFragment newInstance(Beacon beacon) {
        EditBeaconFragment fragment = new EditBeaconFragment();
        if(beacon != null) {
            Bundle args = new Bundle();
            args.putLong(ARG_BEACON_ID, beacon.getId());
            args.putParcelable(ARG_BEACON, beacon);
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_beacon, container, false);
        ButterKnife.inject(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        getActivity().getActionBar().setTitle(R.string.edit_beacon);
        if(getArguments() != null && getArguments().containsKey(ARG_BEACON) && getArguments().containsKey(ARG_BEACON_ID)) {
            beaconId = getArguments().getLong(ARG_BEACON_ID);
            beacon = Parcels.unwrap(getArguments().getParcelable(ARG_BEACON));
            bindPerson();
        }
        else {
            beacon = new Beacon();
        }
    }

    private void bindPerson() {
        name.setText(beacon.getName());
        major.setText(String.valueOf(beacon.getMajor()));
        minor.setText(String.valueOf(beacon.getMinor()));
        uuid.setText(beacon.getUuid());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.edit_beacon, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                hideSoftKeyboard();
                saveBeacon();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void hideSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(name.getWindowToken(), 0);
    }

    private void saveBeacon() {
        if(!isValid()) return;

        beacon.setName(name.getText().toString());
        beacon.setMajor(Long.parseLong(major.getText().toString()));
        beacon.setMinor(Long.parseLong(minor.getText().toString()));
        beacon.setUuid(uuid.getText().toString());

        getActivity().setProgressBarIndeterminateVisibility(true);
        BeaconProxy proxy = new BeaconProxy();
        if(beaconId < 0) {
            // Submit a request to ContextHub to create the beacon
            ArrayList<String> tags = new ArrayList<String>(Arrays.asList("beacon-tag"));
            proxy.createBeacon(beacon.getName(), beacon.getUuid(), beacon.getMajor(), beacon.getMinor(), tags, this);
        }
        else {
            // Submit a request to ContextHub to update the specified beacon
            proxy.updateBeacon(beaconId, beacon, this);
        }
    }

    private boolean isValid() {
        name.setError(null);
        uuid.setError(null);

        boolean isValid = true;
        if(TextUtils.isEmpty(name.getText())) {
            name.setError(getString(R.string.name_required));
            isValid = false;
        }
        if(TextUtils.isEmpty(uuid.getText())) {
            uuid.setError(getString(R.string.uuid_required));
            isValid = false;
        }
        return isValid;
    }

    /**
     * Called after successfully creating or updating a beacon on the ContextHub server
     * @param beacon the beacon that was created or updated
     */
    @Override
    public void onSuccess(Beacon beacon) {
        getActivity().setProgressBarIndeterminateVisibility(false);

        /* If you do not have push properly set up, you need to explicitly call synchronize on
           ProximityService so it will generate events for this device */
        ProximityService.getInstance().synchronize();

        Toast.makeText(getActivity(), beaconId < 0 ? R.string.beacon_created :
                R.string.beacon_updated, Toast.LENGTH_SHORT).show();
        getFragmentManager().popBackStack();
    }

    /**
     * Called when an error occurs creating or updating a beacon on the ContextHub server
     * @param e the exception details
     */
    @Override
    public void onFailure(Exception e) {
        getActivity().setProgressBarIndeterminateVisibility(false);
        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
    }
}
