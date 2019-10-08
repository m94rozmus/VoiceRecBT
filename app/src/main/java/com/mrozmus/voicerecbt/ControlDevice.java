package com.mrozmus.voicerecbt;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

public class ControlDevice extends AppCompatActivity {

    String address = null;
    private ProgressBar progressBar;
    private BluetoothSocket bluetoothSocket = null;
    private BluetoothAdapter bluetoothAdapter = null;
    private boolean bluetoothConnected = false;
    private UUID myUUID = UUID.randomUUID();
    public static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        address = intent.getStringExtra("EXTRA_ADDRESS");

        setContentView(R.layout.activity_control_device);
        progressBar = (ProgressBar)findViewById(R.id.progress_circular);

        new ConnectBluetoothDevice().execute();
    }

    public void getSpeechInput(View view){
        String language = "pl-PL";
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, language);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, language);
        intent.putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, language);
        startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
    }

    public boolean containsIgnoreCase(String str, ArrayList<String> statementsList){
        for(String statement : statementsList){
            if(statement.equalsIgnoreCase(str))
                return true;
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK){
            ArrayList results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            if(containsIgnoreCase("włącz", results)){
                sendMsg("Y");
            } else if(containsIgnoreCase("wyłącz", results)){
                sendMsg("N");
            } else if(containsIgnoreCase("rozłącz", results)){
                disconnectBluetoothDevice();
            }
        }
    }

    private void sendMsg(String str){
        if(bluetoothSocket != null){
            try{
                bluetoothSocket.getOutputStream().write(str.toString().getBytes());
            } catch (IOException e){
                Toast.makeText(getApplicationContext(), "Error while sending message", Toast.LENGTH_LONG);
            }
        }
    }

    private void disconnectBluetoothDevice(){
        if(bluetoothSocket != null){
            try{
                bluetoothSocket.close();
            } catch (IOException e){
                Toast.makeText(getApplicationContext(), "Error while disconnectiong", Toast.LENGTH_LONG);
            }
        }
        finish();
    }

    private class ConnectBluetoothDevice extends AsyncTask<Void, Void, Void> {
        private boolean connectionFailure = false;

        @Override
        protected void onPreExecute(){
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... voids){
            try{
                if(bluetoothSocket==null || !bluetoothConnected){
                    bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);
                    bluetoothSocket = bluetoothDevice.createInsecureRfcommSocketToServiceRecord(myUUID);
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    bluetoothSocket.connect();
                }
            } catch (IOException e) {
                connectionFailure = true;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result){
            super.onPostExecute(result);

            if(connectionFailure){
                Toast.makeText(getApplicationContext(), "Connection failure", Toast.LENGTH_LONG).show();
                finish();
            } else {
                Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_LONG).show();
                bluetoothConnected = true;
            }

            progressBar.setVisibility(View.INVISIBLE);
        }
    }
}
