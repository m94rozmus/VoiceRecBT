package com.mrozmus.voicerecbt;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    public ListView devicesResultList;
    public Map<String, String> devicesResultMap = new HashMap();
    public static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;
    public ListView command;

    private BluetoothAdapter bluetoothAdapter = null;
    private Set<BluetoothDevice> pairedDevicesSet;
    private BluetoothSocket bluetoothSocket = null;

    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private boolean isBluetoothConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        devicesResultList = (ListView) findViewById(R.id.devicesResultList);
        command = (ListView) findViewById(R.id.voiceRecognitionResult);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "Bluetooth device not available", Toast.LENGTH_LONG).show();
            finish();
        } else if ( !bluetoothAdapter.isEnabled()) {
            Intent turnOnBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOnBT, 1);
        }
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
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 200);
        startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
    }

    public boolean containsIgnoreCase(String str, ArrayList<String> statementsList) {
        for(String statement : statementsList){
            if(statement.equalsIgnoreCase(str))
                return true;
        }
        return false;
    }

    public boolean sendMsg(String deviceName, String msg) {
        Log.d("myTag", deviceName);
        try {
            if(bluetoothSocket == null) {
                String address = devicesResultMap.get(deviceName);
                Log.d("myTag", address);
                BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);
                bluetoothSocket = bluetoothDevice.createInsecureRfcommSocketToServiceRecord(myUUID);
                BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                bluetoothSocket.connect();
            }
        } catch (IOException e) {
            Log.d("", e.getMessage());
            return false;
        }
        try {
            bluetoothSocket.getOutputStream().write(msg.getBytes());
            Log.d("myTag", "msg send");
        } catch (IOException e) {
            return false;
        }
        try {
            bluetoothSocket.close();
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
            ArrayList results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            ArrayAdapter arrayAdapter = new ArrayAdapter(MainActivity.this, android.R.layout.simple_list_item_1, results);
            command.setAdapter(arrayAdapter);

            if (containsIgnoreCase("sparuj", results)) {
                getPairedDevices();
            } else if (containsIgnoreCase("salon włącz światło", results)) {
                if(false == sendMsg("SALON", "C")){
                    Log.d("myTag", "Unsecessfull");
                }
            } else if(containsIgnoreCase("salon", results)) {
                String address = devicesResultMap.get("SALON");

                Intent intent = new Intent(MainActivity.this, ControlDevice.class);
                intent.putExtra("EXTRA_ADDRESS", address);
                startActivity(intent);
            }
        }
    }

}
