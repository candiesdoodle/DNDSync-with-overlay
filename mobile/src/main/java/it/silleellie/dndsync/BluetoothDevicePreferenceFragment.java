package it.silleellie.dndsync;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

public class BluetoothDevicePreferenceFragment extends PreferenceFragmentCompat {
    private static final int REQUEST_CODE_SELECT_DEVICE = 1;
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey); // Replace with your actual preference resource ID
        // Find the Bluetooth device preference
        Preference bluetoothDevicePreference = findPreference("bluetooth_device_selection");
        // Set a click listener on the preference
        assert bluetoothDevicePreference != null;
        bluetoothDevicePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                // Start an activity or fragment to select a Bluetooth device
                Intent intent = new Intent(getActivity(), BluetoothDeviceSelectionActivity.class);
                startActivityForResult(intent, REQUEST_CODE_SELECT_DEVICE);
                return true;
            }
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        // Initialize BluetoothManager and BluetoothAdapter
        bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_SELECT_DEVICE && resultCode == Activity.RESULT_OK) {
            // Get the selected Bluetooth device
            BluetoothDevice selectedDevice = data.getParcelableExtra(BluetoothDeviceSelectionActivity.EXTRA_DEVICE);

            // Update the preference summary
            Preference bluetoothDevicePreference = findPreference("bluetooth_device");
            bluetoothDevicePreference.setSummary("Selected device: " + selectedDevice.getName());
        }
    }
}
