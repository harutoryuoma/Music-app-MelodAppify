package me.euhi.melodyappify;

import static androidx.core.app.NotificationManagerCompat.IMPORTANCE_LOW;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Binder;
import android.os.IBinder;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.media3.common.AudioAttributes;
import androidx.media3.common.C;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerNotificationManager;


import java.util.Objects;

@UnstableApi
public class PlayerService extends Service {

    private final IBinder serviceBinder = new ServiceBinder();

    ExoPlayer player;
    PlayerNotificationManager notificationManager;

    public class ServiceBinder extends Binder {
        public PlayerService getPlayerService(){
            return PlayerService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return serviceBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //assign valuables
        player = new ExoPlayer.Builder(getApplicationContext()).build();

        //audio focus attributes
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .build();
        player.setAudioAttributes(audioAttributes, true);

        //notification manager
        final String channelId = getResources().getString(R.string.app_name) + " Music Channel ";
        final int notificationId = 1111111;
        notificationManager = new PlayerNotificationManager.Builder(this,notificationId,channelId)
                .setNotificationListener(notificationListener)
                .setMediaDescriptionAdapter(descriptionAdapter)
                .setChannelImportance(IMPORTANCE_LOW)
                .setSmallIconResourceId(R.drawable.ic_small_notification)
                .setChannelDescriptionResourceId(R.string.app_name)
                .setNextActionIconResourceId(R.drawable.ic_skip_next)
                .setPreviousActionIconResourceId(R.drawable.ic_skip_previous)
                .setPauseActionIconResourceId(R.drawable.ic_pause)
                .setPlayActionIconResourceId(R.drawable.ic_play)
                .setChannelNameResourceId(R.string.app_name)
                .build();

        //set player to notification manager
        notificationManager.setPlayer(player);
        notificationManager.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        notificationManager.setUseRewindAction(true);
        notificationManager.setUseFastForwardAction(true);
    }

    @Override
    public void onDestroy() {
        //release the player
        if(player.isPlaying()){
            player.stop();
        }
        notificationManager.setPlayer(null);
        player.release();
        player = null;
        stopForeground(true);
        stopSelf();
        super.onDestroy();
    }

    //notification listener
    PlayerNotificationManager.NotificationListener notificationListener = new PlayerNotificationManager.NotificationListener() {
        @Override
        public void onNotificationCancelled(int notificationId, boolean dismissedByUser) {
            PlayerNotificationManager.NotificationListener.super.onNotificationCancelled(notificationId, dismissedByUser);
            stopForeground(true);
            if(player.isPlaying()){
                player.pause();
            }
        }

        @Override
        public void onNotificationPosted(int notificationId, @NonNull Notification notification, boolean ongoing) {
            PlayerNotificationManager.NotificationListener.super.onNotificationPosted(notificationId, notification, ongoing);
            startForeground(notificationId, notification);
        }
    };

    //notification adapter
    PlayerNotificationManager.MediaDescriptionAdapter descriptionAdapter = new PlayerNotificationManager.MediaDescriptionAdapter() {

        @NonNull
        @Override
        public CharSequence getCurrentContentTitle(Player player) {
            return Objects.requireNonNull(Objects.requireNonNull(player.getCurrentMediaItem()).mediaMetadata.title);
        }

        @Nullable
        @Override
        public PendingIntent createCurrentContentIntent(@NonNull Player player) {
            //intent to open the app when clicked
            Intent openAppIntent = new Intent(getApplicationContext(), MusicList.class);
            return PendingIntent.getActivity(getApplicationContext(),0,openAppIntent,PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        }

        @Nullable
        @Override
        public CharSequence getCurrentContentText(@NonNull Player player) {
            return null;
        }

        @Nullable
        @Override
        public Bitmap getCurrentLargeIcon(Player player, @NonNull PlayerNotificationManager.BitmapCallback callback) {
            //try creating an Image view on the fly then get its drawable
            ImageView view = new ImageView(getApplicationContext());
            view.setImageURI(Objects.requireNonNull(player.getCurrentMediaItem()).mediaMetadata.artworkUri);

            //get view drawable
            BitmapDrawable bitmapDrawable = (BitmapDrawable) view.getDrawable();
            if(bitmapDrawable == null){
                bitmapDrawable = (BitmapDrawable) ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_music_note);
            }
            assert bitmapDrawable != null;
            return bitmapDrawable.getBitmap();
        }
    };
}
