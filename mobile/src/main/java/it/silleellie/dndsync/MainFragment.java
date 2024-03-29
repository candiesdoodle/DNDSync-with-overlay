package it.silleellie.dndsync;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

public class MainFragment extends PreferenceFragmentCompat {
    private Preference dndPref;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);

        dndPref = findPreference("dnd_permission_key");
        assert(dndPref != null);

        // Add listener to power save key so that it is dependant from the "dnd_as_bedtime_key"
        // and "bedtime_sync_key" in logical OR (it should be enabled when one of the two is true)
        SwitchPreferenceCompat dndAsBedtime = findPreference("dnd_as_bedtime_key");
        SwitchPreferenceCompat bedtimeSync = findPreference("bedtime_sync_key");
        SwitchPreferenceCompat powerSave = findPreference("power_save_key");
        SwitchPreferenceCompat AASync = findPreference("android_auto_sync_key");
        SwitchPreferenceCompat powerSaveAA = findPreference("power_save_with_AA_key");
        SwitchPreferenceCompat watchDnDSync = findPreference("watch_dnd_sync_key");

        assert(dndAsBedtime != null);
        assert(bedtimeSync != null);
        assert(powerSave != null);
        assert watchDnDSync != null;
        // right at the start of app we should check if the powersave toggle should be enabled
        if(dndAsBedtime.isChecked() || bedtimeSync.isChecked()) {
            powerSave.setEnabled(true);
        }
        assert powerSaveAA != null;
        assert AASync != null;
        if(AASync.isChecked()) {
            powerSaveAA.setEnabled(true);
            watchDnDSync.setEnabled(false);
            watchDnDSync.setChecked(false);

        }

        if(!AASync.isChecked()) {
            watchDnDSync.setEnabled(true);
            watchDnDSync.setChecked(true);

        }

        dndAsBedtime.setOnPreferenceChangeListener((preference, newValue) -> {

            if ((boolean) newValue) {
                powerSave.setEnabled(true);
            } else if (!bedtimeSync.isChecked()) {
                powerSave.setEnabled(false);
            }
            // return true to update the preference with newValue
            return true;
        });

        bedtimeSync.setOnPreferenceChangeListener((preference, newValue) -> {
            if ((boolean) newValue) {
                powerSave.setEnabled(true);
            } else if (!dndAsBedtime.isChecked()) {
                powerSave.setEnabled(false);
            }
            // return true to update the preference with newValue
            return true;
        });

        dndPref.setOnPreferenceClickListener(preference -> {
            if (!checkDNDPermission()) {
                openDNDPermissionRequest();
            } else {
                Toast.makeText(getContext(), "DND-Bedtime Permission allowed", Toast.LENGTH_SHORT).show();
            }
            return true;
        });

        AASync.setOnPreferenceChangeListener((preference, newValue) -> {

            if ((boolean) newValue) {
                powerSaveAA.setEnabled(true);
                watchDnDSync.setEnabled(false);
                watchDnDSync.setChecked(false);
                Log.d("DNDNotificationService:", "watchDnDSync" + watchDnDSync.isChecked());
            } else  {
                powerSaveAA.setEnabled(false);
                watchDnDSync.setEnabled(true);
            }
            // return true to update the preference with newValue
            return true;
        });

        checkDNDPermission();
    }

    private boolean checkDNDPermission() {
        NotificationManager mNotificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        boolean allowed = mNotificationManager.isNotificationPolicyAccessGranted();
        if (allowed) {
            dndPref.setSummary(R.string.dnd_permission_allowed);
        } else {
            dndPref.setSummary(R.string.dnd_permission_not_allowed);
        }
        return allowed;
    }

    private void openDNDPermissionRequest() {
       Intent intent = new Intent(android.provider.Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
       startActivity(intent);
    }
}