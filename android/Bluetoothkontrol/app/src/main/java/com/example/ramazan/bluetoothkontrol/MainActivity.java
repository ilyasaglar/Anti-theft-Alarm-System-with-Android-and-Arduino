package com.example.ramazan.bluetoothkontrol;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;
import java.util.Set;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private Button list, disconnect;
    private ListView lv;
    private BluetoothAdapter BA;
    private Set<BluetoothDevice> pairedDevices;
    private Toast toast;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        list = (Button) findViewById(R.id.button1);
        disconnect = (Button) findViewById(R.id.button2);
        lv = (ListView) findViewById(R.id.listView1);


        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object obj = lv.getAdapter().getItem(position);
                String str = obj.toString();
                int strStartIndex = str.indexOf("\n");
                str = str.substring(strStartIndex + 1);


                //servisin başlatılması
                if (BA.isEnabled()) {
                    toast = Toast.makeText(getApplicationContext(),
                            "Bağlanıyor: " + str, Toast.LENGTH_SHORT);
                    toast.show();
                    //connects to item you tapped, creating a service
                    Intent btConnectIntent = new Intent(MainActivity.this, ForegroundService.class);
                    btConnectIntent.setAction("btConnect");
                    btConnectIntent.putExtra("mac", str);
                    startService(btConnectIntent);
                }

                else{
                    toast = Toast.makeText(getApplicationContext(),
                            "Bluetooth etkin olmalıdır!", Toast.LENGTH_SHORT);
                    toast.show();
                }

            }
        });
    }



    public void list(View view) {

        BA = BluetoothAdapter.getDefaultAdapter();

        if (BA != null) {
            if (BA.isEnabled()) {
                bluetoothList();
            }

            else {

                Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(turnOn, 0);
                toast = Toast.makeText(getApplicationContext(),
                        "Bluetooth açık olmalıdır!", Toast.LENGTH_SHORT);
                toast.show();
            }
        }

        else {
            toast = Toast.makeText(getApplicationContext(),
                    "Cihazınız bluetooth'u desteklemiyor!", Toast.LENGTH_SHORT);
            toast.show();
        }

    }


    public void onActivityResult(int requestCode, int resultCode, Intent turnOn) {

        if (resultCode == RESULT_OK) {
            bluetoothList();
        }
    }


    public void disconnect(View view) {
        Intent stopIntent = new Intent(MainActivity.this, ForegroundService.class);
        stopIntent.setAction("stopForeground");
        startService(stopIntent);
    }


    private void bluetoothList() {
        pairedDevices = BA.getBondedDevices();

        ArrayList list = new ArrayList();
        for (BluetoothDevice bt : pairedDevices)
            list.add(bt.getName() + "\n" + bt.getAddress());


        final ArrayAdapter adapter = new ArrayAdapter
                (this, android.R.layout.simple_list_item_1, list);

        lv.setAdapter(adapter);

        toast = Toast.makeText(getApplicationContext(),
                "Eşleştirilmiş cihazlar gösteriliyor", Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
}

