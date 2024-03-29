package it.silleellie.dndsync;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.content.Context;
import android.content.pm.PackageManager;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class BluetoothDeviceSelectionActivity extends AppCompatActivity {

    public static final String EXTRA_DEVICE = "device";
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private BluetoothAdapter bluetoothAdapter;
    private ListView listView;
    private List<BluetoothDevice> devices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_device_selection);

        // Get the Bluetooth adapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Initialize the list view
        ListView listView = findViewById(R.id.list_view);
        devices = new ArrayList<>();

        // Get a list of paired Bluetooth devices


        // Register the permission request launcher
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                // Permission granted, execute the code on line 38
                // Add the paired devices to the list view
            } else {
                // Permission denied, handle the denial gracefully
                // ...
            }
        });

        // Check if the permission is granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
            // Permission granted, execute the code on line 38
            List<BluetoothDevice> pairedDevices = (List<BluetoothDevice>) bluetoothAdapter.getBondedDevices();
            // Add the paired devices to the list view
            pairedDevices.forEach(device -> devices.add(device));
        } else {
            // Permission not granted, request it from the user
            requestPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT);
        }


        List<BluetoothDevice> pairedDevices = (List<BluetoothDevice>) bluetoothAdapter.getBondedDevices();
        pairedDevices.forEach(device -> devices.add(device));

        // Set an adapter to the list view

        ArrayAdapter<BluetoothDevice> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, devices);
        listView.setAdapter(adapter);

        // Set a listener to handle device selection
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get the selected device
                BluetoothDevice selectedDevice = devices.get(position);

                // Return the selected device to the BluetoothDevicePreferenceFragment
                Intent intent = new Intent();
                intent.putExtra(EXTRA_DEVICE, selectedDevice);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }
}