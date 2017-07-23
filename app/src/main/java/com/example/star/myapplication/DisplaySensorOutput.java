package com.example.star.myapplication;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

/**
 * Created by STAR on 7/23/2017.
 */

public class DisplaySensorOutput extends AppCompatActivity {

    Button btnOn, btnOff, btnDis;
    SeekBar brightness;
    String address = null;
    public int c=0;
    private ProgressDialog progress;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    private InputStream mmInStream=null;
    public TextView tv3;
    private Handler handler;
    public String tv3text="";
    public TextView temp;
    public TextView lpg;
    public TextView aq;
    public boolean flag=true;


    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.display_output);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Intent newint = getIntent();
        address = newint.getStringExtra("MacAddress").split("\\r?\\n")[1];
        new ConnectBT().execute();

        temp=(TextView) findViewById(R.id.tempValue);
        lpg=(TextView) findViewById(R.id.lpg);
        lpg.setText("Test");
        aq=(TextView) findViewById(R.id.aq);
        lpg.setText("Test");



        Log.v("Text ",address);

     handler =new Handler();
        Runnable runnable= new Runnable() {
            @Override
            public void run() {
                //handler.postDelayed(this,1000);
                try {
                    if (flag) {
                        String text[] = getTextBT().split("BB");
                        temp.setText(text[0]);
                        int mq135 = Integer.parseInt(text[1]);
                        int mq6 = Integer.parseInt(text[2]);
                        int c1= Color.rgb(66, 182, 244);
                        int c2=Color.rgb(66, 182, 244);
                        Log.e("Error ","Value S" +mq135+" Mq6 "+mq6);
                        String mq135Text = "Good";
                        if (mq135 <= 300) {
                            mq135Text = "Good";
                             c1= Color.rgb(66, 182, 244);

                        } else if (mq135 > 300 && mq135 <= 350) {
                            mq135Text = "Bad";
                             c1= Color.rgb(244, 107, 65);


                        } else if (mq135 > 350 && mq135 <= 600) {
                            mq135Text = "Very Bad";
                            c1= Color.rgb(244, 65, 65);

                        } else if( mq135>600){
                            mq135Text = "Unbearable";
                            c1= Color.rgb(244, 65, 65);

                        }
                        aq.setText(mq135Text);
                        aq.setTextColor(c1);


                        String mq6Text = "Very Safe";
                        if (mq6 <= 300) {
                            mq6Text = "Normal";
                            c2= Color.rgb(66, 182, 244);

                        } else if (mq6 > 300 && mq6 <= 400) {
                            mq6Text = "Safe";
                            c2= Color.rgb(244, 107, 65);

                        } else if (mq6 > 400 && mq6 < 600) {
                            mq6Text = "Dangerous";
                            c2= Color.rgb(244, 65, 65);

                        } else if(mq6>600){
                            mq6Text = "Explosion";
                            c2= Color.rgb(244, 65, 65);

                        }
                        lpg.setText(mq6Text);
                        lpg.setTextColor(c2);
                        //aq.setText(text[2]);
                    }
                }
                catch (Exception e)
                {
                    Log.v("Output","Read Line  here --"+e );
                }

               Log.v("Bt Socket"," the text");
                handler.postDelayed(this,2000);

            }
        };
        handler.postDelayed(runnable,2000);

    }


    public String getTextBT()
    {
        String s="";
        if (btSocket!=null)
        {
            try
            {

                mmInStream = btSocket.getInputStream();

                while(true)
                {
                    try
                    {

                        BufferedReader br=new BufferedReader(new InputStreamReader(mmInStream));
                        String line=br.readLine();
                        Log.v("Output","Read Line "+line );
                        flag=true;
                        return line;

                    }
                    catch(Exception e)
                    {
                        Log.v("Output","Read Line  here " );
                        flag=false;

                    }
                }



            }
            catch (IOException e)
            {
                msg("Error");
            }
        }



        return s;
    }

    //timer.schedule(new SayHello(), 0, 5000);
    private void Disconnect()
    {
        if (btSocket!=null) //If the btSocket is busy
        {
            try
            {
                btSocket.close(); //close connection
            }
            catch (IOException e)
            { msg("Error");}
        }
        finish(); //return to the first layout

    }

    public void reset(View view)
    {
        if (btSocket!=null)
        {
            try
            {
                btSocket.getOutputStream().write("0".toString().getBytes());
            }
            catch (IOException e)
            {
                msg("Error");
            }
        }
    }

    public void buzzerOf(View view)
    {
        Log.v("Clicked ","buzzerof");
        if (btSocket!=null)
        {
            try
            {
                btSocket.getOutputStream().write("1".toString().getBytes());
            }
            catch (IOException e)
            {
                msg("Error");
            }
        }
    }

    // fast way to call Toast
    private void msg(String s)
    {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
    }



    private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
    {
        private boolean ConnectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute()
        {
            progress = ProgressDialog.show(DisplaySensorOutput.this, "Connecting...", "Please wait!!!");  //show a progress dialog
        }

        @Override
        protected Void doInBackground(Void... devices) //while the progress dialog is shown, the connection is done in background
        {
            try
            {
                if (btSocket == null || !isBtConnected)
                {
                    Log.e("In the btSoce","Bt Socket ");
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device

                    Log.e("In the btSoce","Bt Socket  2");
                    BluetoothDevice device = myBluetooth.getRemoteDevice(address);//connects to the device's address and checks if it's available
                    Log.e("In the btSoce","Bt Socket 3"+address+"Rifat");
                    BluetoothSocket  tmp= device.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                    Log.e("In the btSoce","Bt Socket  4");
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    Log.e("In the btSoce","Bt Socket 5");
                    btSocket=tmp;
                    try {
                        btSocket.connect();//start connection
                        Log.e("In the btSoce","Bt Socket 6");
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e("In the btSoce","Bt Socket 7"+e);
                    }
                }
            }
            catch (IOException e)
            {
                ConnectSuccess = false;//if the try failed, you can check the exception here
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);

            if (!ConnectSuccess)
            {
                msg("Connection Failed. Is it a SPP Bluetooth? Try again.");
                finish();
            }
            else
            {
                msg("Connected.");
                isBtConnected = true;
            }
            progress.dismiss();
        }
    }



}

