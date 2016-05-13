package org.ipctabernacle.tabernacle;

import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.firebase.client.Firebase;
import com.firebase.ui.FirebaseRecyclerAdapter;
import java.io.IOException;

public class PodcastActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, SeekBar.OnSeekBarChangeListener {

    MediaPlayerService mediaPlayerService;
    boolean bound = false;
    Intent bindingIntent;
    Intent serviceIntent;
    ImageButton buttonPlayStop;
    ImageButton buttonBack;
    ImageButton buttonForward;
    TextView currentDuration;
    TextView totalDuration;
    private boolean boolMusicPlaying = false;
    String strAudioLink = "http://ipctabernacle.org/media/com_podcastmanager/PODCAST_EPISODES/2016/Bible_Study_04_24_16.mp3";
    private boolean isOnline;
    boolean mBufferBroadcastIsRegistered;
    private ProgressDialog pdBuff = null;
    ProgressBar progress;
    private SeekBar seekBar;
    private int seekMax;
    private static int songEnded = 0;
    boolean mSeekBroadcastIsRegisterd;
    public static final String BROADCAST_SEEKBAR = "org.ipctabernacle.mediatest.sendseekbar";
    Intent seekIntent;
    boolean isPaused = false;
    private RecyclerView podcastRecycleView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_podcast);

        Toolbar toolbar = (Toolbar) findViewById(R.id.podcast_toolbar);
        setSupportActionBar(toolbar);

        AppBarLayout appbar = (AppBarLayout) findViewById(R.id.podcast_appbar);
        float heightDp = getResources().getDisplayMetrics().heightPixels / 2;
        CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams)appbar.getLayoutParams();
        lp.height = (int)heightDp;

        CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.podcast_collapsing_toolbar);
        collapsingToolbar.setExpandedTitleColor(getResources().getColor(R.color.colorWhite));
        collapsingToolbar.setCollapsedTitleTextColor(getResources().getColor(R.color.colorWhite));

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        try {
            serviceIntent = new Intent(this, MediaPlayerService.class);
            seekIntent = new Intent(BROADCAST_SEEKBAR);
            bindingIntent = new Intent(this, MediaPlayerService.class);
            bindService(bindingIntent, mConnection, Context.BIND_AUTO_CREATE);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), e.getClass().getName() + " " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
        initViews();
        setListeners();
        Firebase.setAndroidContext(this);
        loadPodcastEpisodes();
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            mediaPlayerService = binder.getService();
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bound = false;
        }
    };

    private BroadcastReceiver broadcastSeekReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent serviceintent) {
            updateUI(serviceintent);
        }
    };

    private void initViews() {
        buttonPlayStop = (ImageButton) findViewById(R.id.play_pause_toggle);
        buttonPlayStop.setEnabled(false);
        seekBar = (SeekBar) findViewById(R.id.podcast_seekbar);
        seekBar.setProgress(0);
        buttonBack = (ImageButton) findViewById(R.id.podcast_skip_back);
        buttonForward = (ImageButton) findViewById(R.id.podcast_skip_forward);
        currentDuration = (TextView) findViewById(R.id.podcast_current_position);
        totalDuration = (TextView) findViewById(R.id.podcast_total_duration);
    }

    private void setListeners() {
        buttonPlayStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonPlayStopClick();
            }
        });
        seekBar.setOnSeekBarChangeListener(this);
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayerService.skipBack();
            }
        });
        buttonForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayerService.skipForward();
            }
        });
    }

    private void loadPodcastEpisodes() {
        Firebase firebase = new Firebase("https://incandescent-fire-1206.firebaseio.com/podcast");
        podcastRecycleView = (RecyclerView) findViewById(R.id.podcast_recycler);
        podcastRecycleView.setHasFixedSize(true);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        podcastRecycleView.setLayoutManager(layoutManager);
        FirebaseRecyclerAdapter<PodcastList, PodcastViewHolder> fbRA = new FirebaseRecyclerAdapter<PodcastList, PodcastViewHolder>(PodcastList.class, R.layout.podcast_cards, PodcastViewHolder.class, firebase) {
            @Override
            protected void populateViewHolder(PodcastViewHolder podcastViewHolder, final PodcastList podcastList, int i) {
                podcastViewHolder.pubDate.setText(podcastList.getPubDate());
                podcastViewHolder.podcastTitle.setText(podcastList.getTitle());
                podcastViewHolder.podcastDescription.setText(podcastList.getDescription());
                podcastViewHolder.podcastDuration.setText(podcastList.getDuration());
                podcastViewHolder.cardPlayButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        strAudioLink = podcastList.getGuid();
                        playAudio();
                    }
                });
            }
        };
        podcastRecycleView.setAdapter(fbRA);
    }

    private void buttonPlayStopClick() {
        if (!boolMusicPlaying) {
            if (isPaused) {
                mediaPlayerService.playMedia();
                buttonPlayStop.setImageResource(R.drawable.ic_pause_circle_filled_pink_48dp);
                seekBar.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        return false;
                    }
                });
                isPaused = false;
                boolMusicPlaying = true;
            }
        } else if (boolMusicPlaying) {
            buttonPlayStop.setImageResource(R.drawable.ic_play_circle_filled_pink_48dp);
            mediaPlayerService.pauseMedia();
            //seekBar.setEnabled(false);
            seekBar.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return false;
                }
            });
            //stopMyPlayService();
            isPaused = true;
            boolMusicPlaying = false;
        }
    }

    private void buttonPlayStopChange() {
        buttonPlayStop.setImageResource(R.drawable.ic_pause_circle_filled_pink_48dp);
        boolMusicPlaying = true;
    }

    private void playAudio() {
        checkConnectivity();
        if (isOnline) {
            //if (!isPaused) {
            stopMyPlayService();
            buttonPlayStop.setImageResource(R.drawable.ic_play_circle_filled_grey_500_48dp);
            serviceIntent.putExtra("sentAudioLink", strAudioLink);
            try {
                startService(serviceIntent);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), e.getClass().getName() + " " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
            registerReceiver(broadcastSeekReceiver, new IntentFilter(mediaPlayerService.BROADCAST_ACTION));
            mSeekBroadcastIsRegisterd = true;
            //} else if (isPaused) {
            //myService.playMedia();
            //buttonPlayStopChange();
            //}
        } else {
            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setTitle("Network Not Connected...");
            alertDialog.setMessage("Please connect to the internet and try again.");
            alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            alertDialog.setIcon(R.mipmap.ic_launcher);
            buttonPlayStop.setImageResource(R.drawable.ic_play_circle_filled_grey_500_48dp);
            alertDialog.show();
        }
    }

    private void stopMyPlayService() {
        if (mSeekBroadcastIsRegisterd) {
            try {
                unregisterReceiver(broadcastSeekReceiver);
                mSeekBroadcastIsRegisterd = false;
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), e.getClass().getName() + " " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
        try {
            //unbindService(mConnection);
            //unbindService(mConnection);
            stopService(serviceIntent);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), e.getClass().getName() + " " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
        boolMusicPlaying = false;
    }

    @Override
    protected void onPause() {
        if (mSeekBroadcastIsRegisterd) {
            try {
                unregisterReceiver(broadcastSeekReceiver);
                mSeekBroadcastIsRegisterd = false;
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), e.getClass().getName() + " " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
        if (mBufferBroadcastIsRegistered) {
            unregisterReceiver(broadcastBufferReceiver);
            mBufferBroadcastIsRegistered = false;
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (!mSeekBroadcastIsRegisterd) {
            registerReceiver(broadcastSeekReceiver, new IntentFilter(mediaPlayerService.BROADCAST_ACTION));
            mSeekBroadcastIsRegisterd = true;
        }
        if (!mBufferBroadcastIsRegistered) {
            registerReceiver(broadcastBufferReceiver, new IntentFilter(mediaPlayerService.BROADCAST_BUFFER));
            mBufferBroadcastIsRegistered = true;
        }
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopMyPlayService();
        if (bound) {
            try {
                unbindService(mConnection);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
    }

    private void checkConnectivity() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnectedOrConnecting() || connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting()) {
            isOnline = true;
        } else {
            isOnline = false;
        }
    }

    private void showPD(Intent bufferIntent) {
        progress = (ProgressBar) findViewById(R.id.podcast_loading_progress);
        String bufferValue = bufferIntent.getStringExtra("buffering");
        int bufferIntValue = Integer.parseInt(bufferValue);

        switch (bufferIntValue) {
            case 0:
                if (progress.getVisibility() == View.VISIBLE) {
                    progress.setVisibility(View.GONE);
                    buttonPlayStopChange();
                    buttonPlayStop.setEnabled(true);
                }
                break;
            case 1:
                progress.setVisibility(View.VISIBLE);
                break;
            case 2:
                buttonPlayStop.setImageResource(R.drawable.ic_play_circle_filled_grey_500_48dp);
                break;
        }
    }

    private BroadcastReceiver broadcastBufferReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent bufferIntent) {
            showPD(bufferIntent);
        }
    };

    private void updateUI(Intent serviceIntent) {
        String counter = serviceIntent.getStringExtra("counter");
        String mediamax = serviceIntent.getStringExtra("mediamax");
        String strSongEnded = serviceIntent.getStringExtra("song_ended");
        int seekProgress = Integer.parseInt(counter);
        seekMax = Integer.parseInt(mediamax);
        songEnded = Integer.parseInt(strSongEnded);
        seekBar.setMax(seekMax);
        seekBar.setProgress(seekProgress);
        if (songEnded == 1) {
            buttonPlayStop.setImageResource(R.drawable.ic_play_circle_filled_grey_500_48dp);
            seekBar.setProgress(0);
        }
        currentDuration.setText(Utilities.milliSecondsToTimer(seekProgress));
        int timeLeft = seekMax - seekProgress;
        totalDuration.setText("" + Utilities.milliSecondsToTimer(timeLeft));
    }

    @Override
    public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
        if (fromUser) {
            int seekPos = sb.getProgress();
            seekIntent.putExtra("seekpos", seekPos);
            sendBroadcast(seekIntent);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    public static class Utilities {
        public static String milliSecondsToTimer(long milliseconds) {
            String finalTimerString = "";
            String secondsString = "";

            int hours = (int) (milliseconds / (1000 * 60 * 60));
            int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
            int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);
            if (hours > 0) {
                finalTimerString = hours + ":";
            }
            if (seconds < 10) {
                secondsString = "0" + seconds;
            } else {
                secondsString = "" + seconds;
            }

            finalTimerString = finalTimerString + minutes + ":" + secondsString;
            return finalTimerString;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PodcastList {
        private String title;
        private String pubDate;
        private String guid;
        private String description;
        private String duration;

        public PodcastList() {}

        public PodcastList(String title, String pubDate, String guid, String description) {
            this.title = title;
            this.pubDate = pubDate;
            this.guid = guid;
            this.description = description;
            this.duration = duration;
        }

        public String getTitle() { return title; }

        public String getPubDate() { return pubDate; }

        public String getGuid() { return guid; }

        public String getDescription() { return description; }

        public String getDuration() { return duration; }
    }

    public static class PodcastViewHolder extends RecyclerView.ViewHolder {
        TextView pubDate;
        TextView podcastTitle;
        TextView podcastDescription;
        TextView podcastDuration;
        Button cardPlayButton;

        public PodcastViewHolder(View podcastView) {
            super(podcastView);
            pubDate = (TextView)podcastView.findViewById(R.id.podcast_date);
            podcastTitle = (TextView)podcastView.findViewById(R.id.podcast_title);
            podcastDescription = (TextView)podcastView.findViewById(R.id.podcast_description);
            podcastDuration = (TextView)podcastView.findViewById(R.id.podcast_duration);
            cardPlayButton = (Button)podcastView.findViewById(R.id.play_episode_button);
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent home = new Intent(PodcastActivity.this, MainActivity.class);
                    startActivity(home);
                }
            }, 250);

        } else if (id == R.id.nav_directory) {

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    Intent directory=new Intent(PodcastActivity.this, DirectoryActivity.class);
                    startActivity(directory);
                }
            }, 250);

        } else if (id == R.id.nav_podcast) {

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    //Intent podcast = new Intent(PodcastActivity.this, PodcastActivity.class);
                    //startActivity(podcast);
                }
            }, 250);

        } else if (id == R.id.nav_giving) {

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    Intent giving = new Intent(PodcastActivity.this, GivingActivity.class);
                    startActivity(giving);
                }
            }, 250);

        } else if (id == R.id.nav_settings) {

        }
        return true;
    }
}