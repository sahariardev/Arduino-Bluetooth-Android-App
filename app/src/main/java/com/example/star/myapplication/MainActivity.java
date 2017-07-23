package com.example.star.myapplication;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ArrayList list = new ArrayList();
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            msg("No bluetooth device on the device");
        }
        else
        {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
            Set <BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();


            if (pairedDevices.size()>0)
            {
                for(BluetoothDevice bt : pairedDevices)
                {
                    list.add(bt.getName() + "\n" + bt.getAddress()); //Get the device's name and the address
                }
            }
            else
            {
                Toast.makeText(getApplicationContext(), "No Paired Bluetooth Devices Found.", Toast.LENGTH_LONG).show();
            }
        }
        mBluetoothAdapter.cancelDiscovery();

        String macAddress[]={"Ad1","Ad2"};
        ListAdapter allAddresses= new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,list);
        ListView macAddressList=(ListView) findViewById(R.id.macAddressList);
        macAddressList.setAdapter(allAddresses);
        macAddressList.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String address=String.valueOf(parent.getItemAtPosition(position));
                System.out.println(address);
                Intent i=new Intent(MainActivity.this,DisplaySensorOutput.class);
                i.putExtra("MacAddress",address);
                startActivity(i);
                finish();

                //pass the mac address to the new Intent

            }
        });

    }
    private void msg(String s)
    {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
    }

}
