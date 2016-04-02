package org.ipctabernacle.tabernacle;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Xml;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.firebase.client.Firebase;
import com.firebase.ui.FirebaseRecyclerAdapter;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class PodcastActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, SeekBar.OnSeekBarChangeListener {

    private Firebase mFirebaseRef;
    private FirebaseRecyclerAdapter<PodcastList, PodcastViewHolder> mAdapter;
    private Handler mHandler = new Handler();
    private MediaPlayer mp;
    private RelativeLayout playerPanel;
    private SeekBar seekBar;
    private ImageButton playPauseToggle;
    private ImageButton skipBackward;
    private ImageButton skipForward;
    private ProgressBar loadingProgress;
    private TextView currentTimeLabel;
    private TextView totalDuration;
    private RecyclerView recycler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_podcast);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mp = new MediaPlayer();
        playerPanel = (RelativeLayout) findViewById(R.id.player_panel);
        seekBar = (SeekBar) findViewById(R.id.podcast_seekbar);
        loadingProgress = (ProgressBar) findViewById(R.id.podcast_loading_progress);
        playPauseToggle = (ImageButton) findViewById(R.id.play_pause_toggle);
        totalDuration = (TextView) findViewById(R.id.podcast_total_duration);
        skipForward = (ImageButton) findViewById(R.id.podcast_skip_forward);
        skipBackward = (ImageButton) findViewById(R.id.podcast_skip_back);
        currentTimeLabel = (TextView) findViewById(R.id.podcast_current_position);

        Firebase.setAndroidContext(this);
        mFirebaseRef = new Firebase("https://incandescent-fire-1206.firebaseio.com/podcast/");

        seekBar.setOnSeekBarChangeListener(this);

        float scale = getResources().getDisplayMetrics().density;
        final int dpAsPixels = (int) (72*scale + 0.5f);

        recycler = (RecyclerView) findViewById(R.id.members_recycler);
        recycler.setHasFixedSize(true);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recycler.setLayoutManager(layoutManager);
        mAdapter = new FirebaseRecyclerAdapter<PodcastList, PodcastViewHolder>(PodcastList.class, R.layout.podcast_cards, PodcastViewHolder.class, mFirebaseRef) {
            @Override
            public void populateViewHolder(final PodcastViewHolder podcastViewHolder, final PodcastList podcastList, int position) {
                podcastViewHolder.pubDate.setText(podcastList.getPubDate());
                podcastViewHolder.podcastTitle.setText(podcastList.getTitle());
                podcastViewHolder.podcastDescription.setText(podcastList.getDescription());
                podcastViewHolder.podcastDuration.setText(podcastList.getDuration());
                podcastViewHolder.cardPlayButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        playerPanel.setVisibility(View.VISIBLE);
                        loadingProgress.setVisibility(View.VISIBLE);
                        seekBar.setVisibility(View.VISIBLE);
                        playPauseToggle.setImageResource(R.drawable.ic_play_circle_filled_grey_500_48dp);
                        recycler.setPadding(0, 0, 0, dpAsPixels);
                        try {
                            mp.reset();
                            mp.setDataSource(podcastList.getGuid());
                            mp.prepareAsync();
                            mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                @Override
                                public void onPrepared(MediaPlayer mp) {
                                    playPauseToggle.setImageResource(R.drawable.ic_pause_circle_filled_pink_48dp);
                                    loadingProgress.setVisibility(View.GONE);
                                    totalDuration.setText(podcastList.getDuration());
                                    mp.start();
                                    updateProgressBar();
                                }
                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        };
        recycler.setAdapter(mAdapter);
        playPauseToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mp.isPlaying()) {
                    mp.pause();
                    playPauseToggle.setImageResource(R.drawable.ic_play_circle_filled_pink_48dp);
                } else {
                    mp.start();
                    playPauseToggle.setImageResource(R.drawable.ic_pause_circle_filled_pink_48dp);
                }
            }
        });
        skipForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentPosition = mp.getCurrentPosition();
                if (currentPosition + 30000 <= mp.getDuration()) {
                    mp.seekTo(currentPosition + 30000);
                } else {
                    mp.seekTo(mp.getDuration());
                }
            }
        });
        skipBackward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentPosition = mp.getCurrentPosition();
                if (currentPosition - 30000 >= 0) {
                    mp.seekTo(currentPosition - 30000);
                } else {
                    mp.seekTo(0);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.podcast, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
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

                    Intent directory = new Intent(PodcastActivity.this, DirectoryActivity.class);
                    startActivity(directory);
                }
            }, 250);

        } else if (id == R.id.nav_podcast) {

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

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }

    private Runnable mUpdateTimeTask = new Runnable() {
        @Override
        public void run() {
            long totalDuration = mp.getDuration();
            long currentDuration = mp.getCurrentPosition();

            currentTimeLabel.setText("" + Utilities.milliSecondsToTimer(currentDuration));
            int progress = (Utilities.getProgressPercentage(currentDuration, totalDuration));
            seekBar.setProgress(progress);
            mHandler.postDelayed(this, 100);
        }
    };

    @Override
    public void onProgressChanged(SeekBar seekbar, int progress, boolean fromTouch) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        mHandler.removeCallbacks(mUpdateTimeTask);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mHandler.removeCallbacks(mUpdateTimeTask);
        int totalDuration = mp.getDuration();
        int currentPosition = Utilities.progressToTimer(seekBar.getProgress(), totalDuration);
        mp.seekTo(currentPosition);
        updateProgressBar();
    }

    public static class Utilities {
        public static String milliSecondsToTimer(long milliseconds) {
            String finalTimerString = "";
            String secondsString = "";

            int hours = (int)(milliseconds/(1000*60*60));
            int minutes = (int)(milliseconds % (1000*60*60))/(1000*60);
            int seconds = (int)((milliseconds % (1000*60*60)) % (1000*60)/1000);
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

        public static int getProgressPercentage(long currentDuration, long totalDuration) {
            Double percentage = (double) 0;
            long currentSeconds = (int) (currentDuration/1000);
            long totalSeconds = (int) (totalDuration/1000);
            percentage = (((double) currentSeconds)/totalSeconds)*100;
            return percentage.intValue();
        }

        public static int progressToTimer(int progress, int totalDuration) {
            int currentDuration = 0;
            totalDuration = (int) (totalDuration/1000);
            currentDuration = (int) ((((double)progress)/100)*totalDuration);
            return currentDuration*1000;
        }
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

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MemberList {
        private String name;
        private String city;

        public MemberList() {}

        public MemberList(String name, String city) {
            this.name = name;
            this.city = city;
        }

        public String getName() {
            return name;
        }

        public String getCity() {
            return city;
        }
    }

    /*private static final String URL = "http://ipctabernacle.org/component/podcastmanager/?format=raw&feedname=1";

    private void loadPage() {
        new DownloadXmlTask().execute(URL);
    }

    private class DownloadXmlTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            try {
                return loadXmlFromNetwork(urls[0]);
            } catch (IOException e) {
                return "Unable to load content. Check your network connection";
            } catch (XmlPullParserException e) {
                return "Error parsing XML.";
            }
        }

        @Override
        protected void onPostExecute(String result){
            ListView listView = (ListView) findViewById(R.id.podcastlist);
            ArrayAdapter adapter = new ArrayAdapter(PodcastActivity.this, android.R.layout.simple_list_item_1, entries);
            listView.setAdapter(adapter);
            //TextView myTextView = (TextView) findViewById(R.id.myTextView);
            //myTextView.setText(result);
            //ListView listView = (ListView) findViewById(R.id.myListView);
            //ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, result);
            //listView.setAdapter(adapter);
        }
    }

    private String loadXmlFromNetwork(String urlString) throws XmlPullParserException, IOException {
        InputStream stream = null;
        PodcastParser podcastParser = new PodcastParser();
        List<PodcastParser.Entry> entries = null;
        String title = null;
        String url = null;
        String summary = null;
        String author = null;
        String date = null;



        try {
            stream = downloadUrl(urlString);
            entries = podcastParser.parse(stream);
        } finally {
            if (stream != null) {
                stream.close();
            }
        }

        for (PodcastParser.Entry entry : entries) {
            htmlString.append(entry.title + "\n" + entry.date + "\n" + entry.summary + "\n" + "\n");
        }
        return htmlString.toString();
    }

    private InputStream downloadUrl(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000);
        conn.setConnectTimeout(15000);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        conn.connect();
        InputStream stream = conn.getInputStream();
        return stream;
    }

    public class PodcastParser {
        private final String ns = null;

        public List<Entry> parse(InputStream in) throws XmlPullParserException, IOException {
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                parser.setInput(in, null);
                parser.nextTag();
                return readFeed(parser);
            } finally {
                in.close();
            }
        }

        private List<Entry> readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
            parser.require(XmlPullParser.START_TAG, ns, "rss");
            parser.nextTag();
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }
                String name = parser.getName();
                if (name.equals("item")) {
                    entries.add(readEntry(parser));
                } else {
                    skip(parser);
                }
            }
            return entries;
        }

        public class Entry {
            public final String title;
            public final String summary;
            public final String author;
            public final String date;

            public Entry(String title, String summary, String author, String date) {
                this.title = title;
                this.summary = summary;
                this.author = author;
                this.date = date;
            }
        }

        private Entry readEntry(XmlPullParser parser) throws XmlPullParserException, IOException {
            parser.require(XmlPullParser.START_TAG, ns, "item");
            String title = null;
            String summary = null;
            String author = null;
            String date = null;
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }
                String name = parser.getName();
                if (name.equals("title")) {
                    title = readTitle(parser);
                } else if (name.equals("description")) {
                    summary = readSummary(parser);
                } else if (name.equals("itunes:author")) {
                    author = readAuthor(parser);
                } else if (name.equals("pubDate")) {
                    date = readDate(parser);
                } else {
                    skip(parser);
                }
            }
            return new Entry(title, summary, author, date);
        }

        private String readTitle(XmlPullParser parser) throws IOException, XmlPullParserException {
            parser.require(XmlPullParser.START_TAG, ns, "title");
            String title = readText(parser);
            parser.require(XmlPullParser.END_TAG, ns, "title");
            return title;
        }

        private String readSummary(XmlPullParser parser) throws IOException, XmlPullParserException {
            parser.require(XmlPullParser.START_TAG, ns, "description");
            String summary = readText(parser);
            parser.require(XmlPullParser.END_TAG, ns, "description");
            return summary;
        }

        private String readAuthor(XmlPullParser parser) throws IOException, XmlPullParserException {
            parser.require(XmlPullParser.START_TAG, ns, "itunes:author");
            String author = readText(parser);
            parser.require(XmlPullParser.END_TAG, ns, "itunes:author");
            return author;
        }

        private String readDate(XmlPullParser parser) throws IOException, XmlPullParserException {
            parser.require(XmlPullParser.START_TAG, ns, "pubDate");
            String date = readText(parser);
            parser.require(XmlPullParser.END_TAG, ns, "pubDate");
            return date;
        }

        private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
            String result = "";
            if (parser.next() == XmlPullParser.TEXT) {
                result = parser.getText();
                parser.nextTag();
            }
            return result;
        }

        private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                throw new IllegalStateException();
            }
            int depth = 1;
            while (depth != 0) {
                switch (parser.next()) {
                    case XmlPullParser.END_TAG:
                        depth--;
                        break;
                    case XmlPullParser.START_TAG:
                        depth++;
                        break;
                }
            }
        }
    }*/

/*    public void onClickEp1 (View view) {

        Uri myUri = Uri.parse("http://ipctabernacle.org/media/com_podcastmanager/PODCAST_EPISODES/Bible_Study_01_03_16.mp3");
        Intent ep1play = new Intent(android.content.Intent.ACTION_VIEW);
        ep1play.setDataAndType(myUri, "audio/*");
        startActivity(ep1play);
    }

    public void onClickEp2 (View view) {

        Uri myUri = Uri.parse("http://ipctabernacle.org/media/com_podcastmanager/PODCAST_EPISODES/Bible_Study_12_20_15.mp3");
        Intent ep3play = new Intent(android.content.Intent.ACTION_VIEW);
        ep3play.setDataAndType(myUri, "audio/*");
        startActivity(ep3play);
    }

    public void onClickEp3 (View view) {

        Uri myUri = Uri.parse("http://ipctabernacle.org/media/com_podcastmanager/PODCAST_EPISODES/Bible_Study_12_13_15.mp3");
        Intent ep3play = new Intent(android.content.Intent.ACTION_VIEW);
        ep3play.setDataAndType(myUri, "audio/*");
        startActivity(ep3play);
    }

    public void onClickEp4 (View view) {

        Uri myUri = Uri.parse("http://ipctabernacle.org/media/com_podcastmanager/PODCAST_EPISODES/Bible_Study_12_06_15.mp3");
        Intent ep3play = new Intent(android.content.Intent.ACTION_VIEW);
        ep3play.setDataAndType(myUri, "audio*//*");
        startActivity(ep3play);
    }*/
}
