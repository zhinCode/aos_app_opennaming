/********************************************************************************************************************************************************************/
// Create : 2015-01-10
// Modify : 2015-01-10
// Zingoo@Opennaming.com
// GCM Intent service
/********************************************************************************************************************************************************************/
package com.opennaming.app;
import  com.opennaming.app.receiver.GcmBroadcastReceiver;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
public class GcmIntentService extends IntentService
{
    // 현재 시간을 msec으로 구한다.
    long now = System.currentTimeMillis();
    // 현재 시간을 저장 한다.
    Date date = new Date(now);
    // 시간 포맷으로 만든다.
    SimpleDateFormat sdfNow = new SimpleDateFormat("HHmmss");
    String strNow = sdfNow.format(date);
    //public static final int NOTIFICATION_ID = 1;
    public int NOTIFICATION_ID = Integer.parseInt(strNow);
    // 이걸왜 이렇게 줬냐면... 같은 아이디 값으로 노티피케이션을 보내면 걍 덮어 써버림.. 나중에 정리 필요함..
  public GcmIntentService()
  {
    super("GcmIntentService");
  }
  
  @Override
  protected void onHandleIntent(Intent intent)
  {
    Bundle extras = intent.getExtras();
    GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
    // The getMessageType() intent parameter must be the intent you received
    // in your BroadcastReceiver.
    String messageType = gcm.getMessageType(intent);
    
    if (!extras.isEmpty())
    { // has effect of unparcelling Bundle
      /*
       * Filter messages based on message type. Since it is likely that GCM will
       * be extended in the future with new message types, just ignore any
       * message types you're not interested in, or that you don't recognize.
       */
      if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType))
      {
        sendNotification("ENUS","OpenNaming.com","Send error: " + extras.toString(),"99","/");
      }
      else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType))
      {
        sendNotification("ENUS","OpenNaming.com","Deleted messages on server: " + extras.toString(),"99","/");
        // If it's a regular GCM message, do some work.
      }
      else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType))
      {
        String GCM_Title = intent.getStringExtra("GCM_Title");
        String GCM_msg = intent.getStringExtra("GCM_msg");
        String GCM_Type = intent.getStringExtra("GCM_Type");
        String GCM_Language = intent.getStringExtra("GCM_Language");
        String GCM_URL = intent.getStringExtra("GCM_URL");

        // Post notification of received message.
        //sendNotification("Received: " + extras.toString());
        //sendNotification("Received: " + msg);
          sendNotification(GCM_Language,GCM_Title,GCM_msg,GCM_Type,GCM_URL);
        Log.i("GcmIntentService.java | onHandleIntent", "Received: " + extras.toString());
      }
    }
    // Release the wake lock provided by the WakefulBroadcastReceiver.
    GcmBroadcastReceiver.completeWakefulIntent(intent);
  }

  // Put the message into a notification and post it.
  // This is just one simple example of what you might choose to do with
  // a GCM message.
  private void sendNotification(String GCM_Language,String GCM_Title, String GCM_msg, String GCM_Type, String GCM_URL)
  {
    NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    //Intent intent = new Intent(getApplicationContext(), MainActivity.class);
    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    intent.putExtra("GCM_Title", GCM_Title);
    intent.putExtra("GCM_msg", GCM_msg);
    intent.putExtra("GCM_Type", GCM_Type);
    intent.putExtra("GCM_Language", GCM_Language);
    intent.putExtra("GCM_URL", GCM_URL);

    PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this).setSmallIcon(R.drawable.ic_launcher).setContentTitle(GCM_Title).setStyle(
        new NotificationCompat.BigTextStyle().bigText(GCM_msg)).setContentText(GCM_msg).setAutoCancel(true).setVibrate(new long[] { 0, 500 });

    mBuilder.setContentIntent(contentIntent);
    //mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build()); // 푸시 덮어 씌우기
    mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
  }
}
