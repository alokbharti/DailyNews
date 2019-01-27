package com.example.bhart.dailynews;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class notification extends BroadcastReceiver {
    String heading="";
    @Override
    public void onReceive(Context context, Intent intent) {

        //heading = intent.getStringExtra("newsTitle");
//        Log.d("Title",intent.getStringExtra("newsTitle"));

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        Intent intent1 = new Intent(context,MainActivity.class);
        intent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(context,100,intent1,PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Check all the latest news here")
                .setAutoCancel(true);
        if (intent.getAction().equals("MY_NOTIFICATION_MESSAGE"))
            notificationManager.notify(100,builder.build());


    }
}
