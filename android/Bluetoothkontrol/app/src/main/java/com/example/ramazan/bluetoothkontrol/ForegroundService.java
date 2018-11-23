package com.example.ramazan.bluetoothkontrol;

import android.app.Service;
import android.bluetooth.BluetoothClass;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * Created by RAMAZAN on 11.12.2017.
 */

public class ForegroundService extends Service {
    int FOREGROUND_ID = 1997;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothAdapter BA;
    private BluetoothSocket BS;
    private StringBuilder sb = new StringBuilder();
    private Toast toast;
    private String mac;
    private Handler h;


    public void onCreate() {
        startForeground(FOREGROUND_ID, buildForegroundNotification());
        toast = Toast.makeText(getApplicationContext(), "SERVİS BAŞLATILDI", Toast.LENGTH_SHORT);
        toast.show();


        BA = BluetoothAdapter.getDefaultAdapter();



        h = new Handler() {
            public void handleMessage(Message msg) {
                Bundle bundle = msg.getData();
                toast = Toast.makeText(getApplicationContext(), bundle.getString("str"), Toast.LENGTH_SHORT);
                toast.show();
            }

            ;
        };
    }


    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent.getAction().equals("btConnect")) {
            if (BS == null) {
                Bundle extras = intent.getExtras();
                mac = extras.getString("mac");

                final Handler mHandler = new Handler();

                final Runnable hUnsuccessful = new Runnable() {
                    public void run() {
                        toast = Toast.makeText(getApplicationContext(),
                                "BAŞARISIZ", Toast.LENGTH_SHORT);
                        toast.show();
                        toast = Toast.makeText(getApplicationContext(), "SERVİS DURDURULDU", Toast.LENGTH_SHORT);
                        toast.show();

                        stopForeground(true);
                        stopSelf();
                    }
                };


                final Runnable hSuccessful = new Runnable() {
                    public void run() {

                        ConnectedThread(BS);
                        toast = Toast.makeText(getApplicationContext(),
                                "BAŞARILI", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                };

                Thread thread = new Thread() {
                    public void run() {


                        BluetoothDevice device = BA.getRemoteDevice(mac);
                        try {

                            BS = device.createRfcommSocketToServiceRecord(MY_UUID);
                        }

                        catch (IOException e) {
                            mHandler.post(hUnsuccessful);
                            return;
                        }

                        BA.cancelDiscovery();
                        try {
                            BS.connect();
                        }
                        catch (IOException connectException) {
                            try {
                                if (BS != null) {
                                    BS.close();
                                    BS = null;
                                }
                                mHandler.post(hUnsuccessful);
                                return;
                            }
                            catch (IOException closeException) {
                            }
                        }

                        mHandler.post(hSuccessful);
                    }
                };

                thread.start();
            }

            else {
                toast = Toast.makeText(getApplicationContext(), "ÖNCE BAĞLANTIYI KESMELİSİN", Toast.LENGTH_SHORT);
                toast.show();
            }
        }


        if (intent.getAction().equals("stopForeground")) {
            toast = Toast.makeText(getApplicationContext(), "SERVİS DURDURULDU", Toast.LENGTH_SHORT);
            toast.show();
            try {
                if (BS != null) {
                    BS.close();
                    BS = null;
                    toast = Toast.makeText(getApplicationContext(),
                            "BLUETOOTH AYGIT BAĞLANTISI KESİLDİ", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
            catch (IOException e) {
            }

            stopForeground(true);
            stopSelf();
        }

        return START_STICKY;
    }

    private Notification buildForegroundNotification() {
        NotificationCompat.Builder notification = new NotificationCompat.Builder(this);

        notification.setOngoing(true);
        notification.setContentTitle("BLUETOOTH SERVİSİ")
                .setContentText("Bluetooth verilerini dinleme servisi")
                .setSmallIcon(android.R.drawable.sym_def_app_icon)
                .setPriority(Notification.PRIORITY_MIN);
        return (notification.build());
    }

    private void notification(String title, String text) {
        int mId = 0;
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(android.R.drawable.sym_def_app_icon)
                        .setContentTitle(title)
                        .setContentText(text)
                        .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000})
                        .setPriority(NotificationCompat.PRIORITY_MAX);
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        mBuilder.setSound(alarmSound);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(mId, mBuilder.build());
    }

    private void ConnectedThread(BluetoothSocket socket) {
        final InputStream mmInStream;
        InputStream tmpIn = null;

        try {
            tmpIn = socket.getInputStream();
        } catch (IOException e) {
        }

        mmInStream = tmpIn;


        Thread thread = new Thread() {
            public void run() {
                byte[] buffer = new byte[256];  //buffer store for the stream
                int bytes;

                while (true) {
                    try {
                        Message msg = new Message();
                        Bundle bundle = new Bundle();
                        bytes = mmInStream.read(buffer);        // Get number of bytes and message in "buffer"
                        String strIncom = new String(buffer, 0, bytes);
                        sb.append(strIncom);
                        int endOfLineIndex = sb.indexOf("\r\n");
                        if (endOfLineIndex > 0) {
                            String sbprint = sb.substring(0, endOfLineIndex);
                            sb.delete(0, sb.length());
                            bundle.putString("str", sbprint);
                            msg.setData(bundle);
                            h.sendMessage(msg);
                            notification("Sensor VERİSİ:", sbprint);
                        }
                    }
                    catch (IOException e) {
                        Message msg = new Message();
                        Bundle bundle = new Bundle();
                        bundle.putString("str", "BAĞLANTI KOPTU");
                        msg.setData(bundle);
                        h.sendMessage(msg);
                        notification("HATA", "BAĞLANTI KOPTU");
                        Intent stopIntent = new Intent(getApplicationContext(), ForegroundService.class);
                        stopIntent.setAction("stopForeground");
                        startService(stopIntent);
                        break;
                    }
                }
            }
        };
        thread.start();
    }

    public IBinder onBind(Intent intent) {
        return null;
    }
}