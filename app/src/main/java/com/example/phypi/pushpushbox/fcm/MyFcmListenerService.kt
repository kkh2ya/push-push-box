package com.example.phypi.pushpushbox.fcm

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import android.support.v4.app.NotificationCompat
import android.app.NotificationChannel
import android.os.Build
import com.example.phypi.pushpushbox.MainActivity
import com.example.phypi.pushpushbox.R

class MyFcmListenerService : FirebaseMessagingService() {

    //ログ用タグ
    private val TAG = MyFcmListenerService::class.java.simpleName

    override fun onNewToken(token: String?) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed token: " + token.toString())

        //TODO ここで取得したFCM InstanceIDをサーバー管理者に伝える
        //テスト用。取得したFCM InstanceID確認用。
        if (token != null) saveTokenToPrefs(token.toString())

        Log.d(TAG, "Saved token: " + getTokenFromPrefs())
    }

    override fun onMessageReceived(message: RemoteMessage?) {
        super.onMessageReceived(message)
        val from = message?.getFrom()
        val data = message?.getData()

        Log.d(TAG, "from:$from")
        Log.d(TAG, "data:" + data.toString())

        val title = data?.get("title").toString()
        val subtitle = data?.get("subtitle").toString()
        val content = data?.get("content").toString()
        sendNotification(title, subtitle, content)
    }

    private fun saveTokenToPrefs(token: String) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = preferences.edit()
        editor.putString("registration_id", token)
        editor.apply()
    }

    private fun getTokenFromPrefs(): String? {
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        return preferences.getString("registration_id", null)
    }

    private fun sendNotification(title: String, subtitle: String, content: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val idChannel = "my_channel_01"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mChannel: NotificationChannel =
                NotificationChannel(idChannel, "my_channel", NotificationManager.IMPORTANCE_HIGH)
            mChannel.setDescription("Push通知")
            mChannel.setVibrationPattern(longArrayOf(100, 250))
            notificationManager.createNotificationChannel(mChannel)
        }

        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT
        )

        val notificationBuilder = NotificationCompat.Builder(this, idChannel)
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.mipmap.ic_launcher_old)
            .setContentTitle(title)
            .setSubText(subtitle)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setVibrate(longArrayOf(100, 250))
            .setAutoCancel(true)

        notificationManager.notify(0, notificationBuilder.build())

        saveMessageToPrefs(title, content)

        if (MainActivity.isActivityVisible()) {
            val intentMain = Intent(this, MainActivity::class.java)
            startActivity(intentMain)
        }
    }

    private fun saveMessageToPrefs(title: String, content: String) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = preferences.edit()
        editor.putString("title_new", title)
        editor.putString("content_new", content)
        editor.apply()
    }
}
