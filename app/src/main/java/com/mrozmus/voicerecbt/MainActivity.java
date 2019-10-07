package com.mrozmus.voicerecbt;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    public ListView devicesResultList;
    public Map<String, String> devicesResultMap = new HashMap();
    public static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;
    public TextView command;

    private BluetoothAdapter bluetoothAdapter = null;
    private Set<BluetoothDevice> pairedDevicesSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        devicesResultList = (ListView) findViewById(R.id.devicesResultList);
        command = (TextView) findViewById(R.id.voiceRecognitionResult);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "Bluetooth device not available", Toast.LENGTH_LONG).show();
            finish();
        } else if ( !bluetoothAdapter.isEnabled()) {
            Intent turnOnBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOnBT, 1);
        }
        getPairedDevices();
    }

    private void getPairedDevices() {
        pairedDevicesSet = bluetoothAdapter.getBondedDevices();
        ArrayList devicesList = new ArrayList();

        if (pairedDevicesSet.size() > 0) {
            for (BluetoothDevice bluetoothDevice : pairedDevicesSet) {
                devicesList.add(bluetoothDevice.getName().toString());
                devicesResultMap.put(bluetoothDevice.getName().toString(), bluetoothDevice.getAddress().toString());
            }
        } else {
            Toast.makeText(getApplicationContext(), "No paired devices", Toast.LENGTH_LONG).show();
        }

        final ArrayAdapter arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, devicesList);
        devicesResultList.setAdapter(arrayAdapter);
    }

    public void getSpeechInput(View view) {

        String language = "pl-PL";
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, language);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, language);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, language);
        intent.putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, language);
        startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
            ArrayList results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            if (results.contains("sparuj")) {
                command.setText("SPARUJ");
                getPairedDevices();
            } else if (results.contains("")) {

            }
        }
    }
}
