package org.ipctabernacle.tabernacle;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.media.session.MediaSessionManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.Random;

/**
 * Created by Charles Koshy on 5/10/2016.
 */
public class MediaPlayerService extends Service
        implements
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnSeekCompleteListener,
        MediaPlayer.OnInfoListener,
        MediaPlayer.OnBufferingUpdateListener {

    private final IBinder binder = new LocalBinder();
    private final Random random = new Random();
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private String strAudioLink;
    private static final int NOTIFICATION_ID = 001;
    private boolean isPausedInCall = false;
    private PhoneStateListener phoneStateListener;
    private TelephonyManager telephonyManager;
    private static final String TAG = "TELSERVICE";
    public static final String BROADCAST_BUFFER = "org.ipctabernacle.mediatest.broadcastbuffer";
    Intent bufferIntent;
    private int headsetSwitch = 1;
    Intent seekIntent;
    String strSeekPos;
    int intSeekPos = 0;
    int mediaPosition;
    int mediaMax;
    private final Handler handler = new Handler();
    private static int songEnded;
    public static final String BROADCAST_ACTION = "org.ipctabernacle.mediatest.broadcastseek";
    int seekWhilePaused;

    public class LocalBinder extends Binder {
        MediaPlayerService getService() {
            return MediaPlayerService.this;
        }
    }

    @Override
    public void onCreate() {
        bufferIntent = new Intent(BROADCAST_BUFFER);
        seekIntent = new Intent(BROADCAST_ACTION);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnSeekCompleteListener(this);
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setOnInfoListener(this);
        mediaPlayer.reset();

        registerReceiver(headsetReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        registerReceiver(broadcasseektoreceiver, new IntentFilter(PodcastActivity.BROADCAST_SEEKBAR));
        Log.v(TAG, "Starting Telephony");
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        Log.v(TAG, "Starting Listener");
        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                Log.v(TAG, "Starting CallStateChange");
                switch (state) {
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                    case TelephonyManager.CALL_STATE_RINGING:
                        if (mediaPlayer != null) {
                            pauseMedia();
                            isPausedInCall = true;
                        }
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        if (mediaPlayer != null) {
                            if (isPausedInCall) {
                                isPausedInCall = false;
                                playMedia();
                            }
                        }
                        break;
                }
            }
        };
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        initNotification();
        strAudioLink = intent.getExtras().getString("sentAudioLink");
        //if (!mediaPlayer.isPlaying()) {
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(strAudioLink);
            sendBufferingBroadcast();
            mediaPlayer.prepareAsync();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //}
        setupHandler();
        return START_STICKY;
    }

    private BroadcastReceiver headsetReceiver = new BroadcastReceiver() {
        private boolean headsetConnected = false;

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra("state")) {
                if (headsetConnected && intent.getIntExtra("state", 0) == 0) {
                    headsetConnected = false;
                    headsetSwitch = 0;
                } else if (headsetConnected && intent.getIntExtra("state", 0) == 1) {
                    headsetConnected = true;
                    headsetSwitch = 1;
                }
            }
            switch (headsetSwitch) {
                case (0):
                    headsetDisconnected();
                    break;
                case (1):
                    break;
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
        }
        if (phoneStateListener != null) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }
        cancelNotification();
        try {
            unregisterReceiver(headsetReceiver);
            unregisterReceiver(broadcasseektoreceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
        handler.removeCallbacks(sendUpdatesToUI);
        resetButtonPlayStopBroadcast();
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        songEnded = 1;
        stopMedia();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        switch (what) {
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                Toast.makeText(this, "MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK " + extra, Toast.LENGTH_SHORT).show();
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                Toast.makeText(this, "MEDIA ERROR SERVER DIED " + extra, Toast.LENGTH_SHORT).show();
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Toast.makeText(this, "MEDIA ERROR UNKNOWN " + extra, Toast.LENGTH_SHORT).show();
                break;
        }
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        sendBufferCompleteBroadcast();
        playMedia();
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
        if (!mediaPlayer.isPlaying()) {
            playMedia();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void playMedia() {
        if (!mediaPlayer.isPlaying()) {
            if (seekWhilePaused == 0) {
                mediaPlayer.start();
            } else if (seekWhilePaused != 0) {
                mediaPlayer.seekTo(seekWhilePaused);
                mediaPlayer.start();
            }
        }
    }

    public void stopMedia() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
    }

    public void pauseMedia() {
        if (mediaPlayer.isPlaying()) {
            if (seekWhilePaused != 0) {
                seekWhilePaused = 0;
            }
            mediaPlayer.pause();
        }
    }

    private void initNotification() {
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager notificationManager = (NotificationManager) getSystemService(ns);
        int icon = R.drawable.ic_play_circle_filled_pink_48dp;
        CharSequence tickerText = "Tutorial: Music In Service";
        long when = System.currentTimeMillis();
        android.support.v4.app.NotificationCompat.Builder builder = new android.support.v4.app.NotificationCompat.Builder(this)
                .setSmallIcon(icon)
                .setTicker(tickerText)
                .setWhen(when)
                .setContentTitle("Music In Service App Tutorial")
                .setContentText("Listen To Music While Performing Other Tasks");
        Context context = getApplicationContext();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
        builder.setContentIntent(contentIntent);
        Notification notification = builder.build();
        notification.flags = Notification.FLAG_ONGOING_EVENT;
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    private void cancelNotification() {
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager notificationManager = (NotificationManager) getSystemService(ns);
        notificationManager.cancel(NOTIFICATION_ID);
    }

    private void sendBufferingBroadcast() {
        bufferIntent.putExtra("buffering", "1");
        sendBroadcast(bufferIntent);
    }

    private void sendBufferCompleteBroadcast() {
        bufferIntent.putExtra("buffering", "0");
        sendBroadcast(bufferIntent);
    }

    private void resetButtonPlayStopBroadcast() {
        bufferIntent.putExtra("buffering", "2");
        sendBroadcast(bufferIntent);
    }

    private void headsetDisconnected() {
        stopMedia();
        stopSelf();
    }

    private void setupHandler() {
        handler.removeCallbacks(sendUpdatesToUI);
        handler.postDelayed(sendUpdatesToUI, 1000);
    }

    private Runnable sendUpdatesToUI = new Runnable() {
        @Override
        public void run() {
            LogMediaPosition();
            handler.postDelayed(this, 1000);
        }
    };

    private void LogMediaPosition() {
        if (mediaPlayer.isPlaying()) {
            mediaPosition = mediaPlayer.getCurrentPosition();
            mediaMax = mediaPlayer.getDuration();
            seekIntent.putExtra("counter", String.valueOf(mediaPosition));
            seekIntent.putExtra("mediamax", String.valueOf(mediaMax));
            seekIntent.putExtra("song_ended", String.valueOf(songEnded));
            sendBroadcast(seekIntent);
        }
    }

    private BroadcastReceiver broadcasseektoreceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateSeekPos(intent);
        }
    };

    private void updateSeekPos(Intent intent) {
        int seekPos = intent.getIntExtra("seekpos", 0);
        if (mediaPlayer.isPlaying()) {
            seekWhilePaused = 0;
            handler.removeCallbacks(sendUpdatesToUI);
            mediaPlayer.seekTo(seekPos);
            setupHandler();
        } else if (!mediaPlayer.isPlaying()) {
            seekWhilePaused = seekPos;
            seekIntent.putExtra("counter", String.valueOf(seekWhilePaused));
            sendBroadcast(seekIntent);
        }
    }

    public void skipBack() {
        int currentPosition = mediaPlayer.getCurrentPosition();
        int newPosition = currentPosition - 30000;
        if (newPosition > 30000) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.seekTo(newPosition);
            } else if (!mediaPlayer.isPlaying()) {
                seekWhilePaused = newPosition;
                seekIntent.putExtra("counter", String.valueOf(newPosition));
                sendBroadcast(seekIntent);
            }
        } else if (newPosition < 30000) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.seekTo(0);
            } else if (!mediaPlayer.isPlaying()) {
                newPosition = 0;
                seekWhilePaused = newPosition;
            }
        }
    }

    public void skipForward() {
        int currentPosition = mediaPlayer.getCurrentPosition();
        int newPosition = currentPosition + 30000;

        if (newPosition < mediaPlayer.getDuration() - 30000) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.seekTo(newPosition);
            } else if (!mediaPlayer.isPlaying()) {
                seekWhilePaused = newPosition;
                seekIntent.putExtra("counter", String.valueOf(newPosition));
                sendBroadcast(seekIntent);
            }
        } else if (newPosition > mediaPlayer.getDuration() - 30000) {
            //do nothing
        }
    }

}
