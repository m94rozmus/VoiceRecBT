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
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

public class ControlDevice extends AppCompatActivity {

    Button button;

    String address = null;
    private ProgressBar progressBar;
    private BluetoothSocket bluetoothSocket = null;
    private BluetoothAdapter bluetoothAdapter = null;
    private boolean bluetoothConnected = false;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        address = intent.getStringExtra("EXTRA_ADDRESS");

        Log.d("TAG", address);

        setContentView(R.layout.activity_control_device);
        progressBar = (ProgressBar)findViewById(R.id.progress_circular);

        button = (Button) findViewById(R.id.button);

        new ConnectBluetoothDevice().execute();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                sendMsg("1");
            }
        });

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

            if(containsIgnoreCase("wyłącz zielone", results)){
                sendMsg("1");
            } else if(containsIgnoreCase("włącz zielone", results)) {
                sendMsg("2");
            } else if(containsIgnoreCase("wyłącz żółte", results)) {
                sendMsg("3");
            } else if(containsIgnoreCase("włącz żółte", results)) {
                sendMsg("4");
            } else if(containsIgnoreCase("wyłącz czerwone", results)) {
                sendMsg("5");
            } else if(containsIgnoreCase("włącz czerwone", results)) {
                sendMsg("6");
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
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }

        @Override
        protected Void doInBackground(Void... voids){
            try{
                if(bluetoothSocket==null || !bluetoothConnected){
                    bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);
                    bluetoothSocket = bluetoothDevice.createInsecureRfcommSocketToServiceRecord(myUUID);
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    Log.d("tag", "tutaj");
                    bluetoothSocket.connect();
                }
            } catch (IOException e) {
                Log.d("error", e.getMessage());
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

            progressBar.setVisibility(View.GONE);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }
}
