package com.contexthub.detectme;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

import com.chaione.contexthub.sdk.model.Beacon;
import com.contexthub.detectme.fragments.AboutFragment;
import com.contexthub.detectme.fragments.BeaconListFragment;
import com.contexthub.detectme.fragments.EditBeaconFragment;


public class MainActivity extends FragmentActivity implements BeaconListFragment.Listener, FragmentManager.OnBackStackChangedListener {

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
            super.onCreate(savedInstanceState);
            setProgressBarIndeterminate(true);

            if(savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(android.R.id.content, new BeaconListFragment())
                        .commit();
                getSupportFragmentManager().addOnBackStackChangedListener(this);
            }
        }

        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            getMenuInflater().inflate(R.menu.main, menu);
            return true;
        }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean isMainFragment = getSupportFragmentManager().getBackStackEntryCount() <= 0;
        menu.findItem(R.id.action_add).setVisible(isMainFragment);
        menu.findItem(R.id.action_about).setVisible(isMainFragment);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_add:
                launchEditBeaconFragment(null);
                return true;
            case R.id.action_about:
                getSupportFragmentManager().beginTransaction()
                        .addToBackStack(null)
                        .replace(android.R.id.content, new AboutFragment())
                        .commit();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void launchEditBeaconFragment(Beacon beacon) {
        getSupportFragmentManager().beginTransaction()
                .addToBackStack(null)
                .replace(android.R.id.content, EditBeaconFragment.newInstance(beacon))
                .commit();
    }

    @Override
    public void onItemClick(Beacon beacon) {
        launchEditBeaconFragment(beacon);
    }

    @Override
    public void onBackStackChanged() {
        supportInvalidateOptionsMenu();
    }
}
