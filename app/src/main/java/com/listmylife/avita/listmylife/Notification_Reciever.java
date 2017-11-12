package com.listmylife.avita.listmylife;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

/**
 * Created by avita on 7/1/2017.
 */

public class Notification_Reciever extends BroadcastReceiver{
    int value=0;
    String title="";
    String setMessage="";

    @Override
    public void onReceive(Context context, Intent intent) {
        value = intent.getStringExtra("value").hashCode();
        Log.d("TESTING_VALUES", Integer.toString(value));
        title = intent.getStringExtra("title");
        setMessage = intent.getStringExtra("message");
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent notificationintent = new Intent(context,Private_Lists.class);
        notificationintent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(context,value,notificationintent,PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context).setContentIntent(pendingIntent)
                .setSmallIcon(com.listmylife.avita.listmylife.R.drawable.notepad)
                .setContentTitle(title)
                .setContentText(setMessage)
                .setAutoCancel(true)
                .setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 })
                .setLights(Color.BLUE, 3000, 3000);
        notificationManager.notify(value,builder.build());
    }

}
