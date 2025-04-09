package ru.netology.nmedia.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import ru.netology.nmedia.R
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.dto.PushMessage
import kotlin.random.Random


class FCMService : FirebaseMessagingService() {
    private val content = "content"
    private val channelId = "remote"
    private val gson = Gson()

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_remote_name)
            val descriptionText = getString(R.string.channel_remote_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        println(message.data[content])

        val currentUserId = AppAuth.getInstance().authState.value?.userId

        val messageContent = gson.fromJson(message.data[content], PushMessage::class.java)

        when (messageContent.recipientId) {
            currentUserId, null -> pushTest(messageContent)
            0L -> AppAuth.getInstance().sendPushToken()
            else -> AppAuth.getInstance().sendPushToken()
        }
    }


    override fun onNewToken(token: String) {
        AppAuth.getInstance().sendPushToken(token)
    }

    private fun pushTest(message: PushMessage) {
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(
                getString(
                    R.string.notification,
                    message.recipientId,
                    message.content
                )
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notify(notification)
    }

    private fun notify(notification: Notification) {
        if (
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            checkSelfPermission(
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(this)
                .notify(Random.nextInt(100_000), notification)
        }
    }

}


//class FCMService : FirebaseMessagingService() {
//    private val action = "action"
//    private val content = "content"
//    private val channelId = "remote"
//    private val gson = Gson()
//
//    override fun onCreate() {
//        super.onCreate()
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val name = getString(R.string.channel_remote_name)
//            val descriptionText = getString(R.string.channel_remote_description)
//            val importance = NotificationManager.IMPORTANCE_DEFAULT
//            val channel = NotificationChannel(channelId, name, importance).apply {
//                description = descriptionText
//            }
//            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//            manager.createNotificationChannel(channel)
//        }
//    }
//
//    override fun onMessageReceived(message: RemoteMessage) {
//
//        message.data[action]?.let {
//            // handlePushTest(gson.fromJson(message.data[content], PushMessage::class.java))
//           when (Action.valueOf(it)) {
//              Action.LIKE -> handleLike(gson.fromJson(message.data[content], Like::class.java))
//              Action.PUSH -> handlePushTest(gson.fromJson(message.data[content], PushMessage::class.java))
//           }
//        }
//    }
//
//    override fun onNewToken(token: String) {
//        println(token)
//        AppAuth.getInstance().sendPushToken(token)
//    }
//
//    private fun handleLike(content: Like) {
//        val notification = NotificationCompat.Builder(this, channelId)
//            .setSmallIcon(R.drawable.ic_notification)
//            .setContentTitle(
//                getString(
//                    R.string.notification_user_liked,
//                    content.userName,
//                    content.postAuthor,
//                )
//            )
//            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//            .build()
//
//        notify(notification)
//    }
//
//
//    private fun handlePushTest (message: PushMessage) {
//        val notification = NotificationCompat.Builder(this, channelId)
//            .setSmallIcon(R.drawable.ic_notification)
//            .setContentTitle(
//                "${message.recipientId.toString()}, ${message.content}"
//            )
//            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//            .build()
//
//        notify(notification)
//    }
//
//
//
//
//
//    private fun notify(notification: Notification) {
//        if (
//            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
//            checkSelfPermission(
//                Manifest.permission.POST_NOTIFICATIONS
//            ) == PackageManager.PERMISSION_GRANTED
//        ) {
//            NotificationManagerCompat.from(this)
//                .notify(Random.nextInt(100_000), notification)
//        }
//    }
//}
//
//enum class Action {
//    LIKE, PUSH
//}
//
//data class Like(
//    val userId: Long,
//    val userName: String,
//    val postId: Long,
//    val postAuthor: String,
//)
