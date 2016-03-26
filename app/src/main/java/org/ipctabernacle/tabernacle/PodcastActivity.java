package org.ipctabernacle.tabernacle;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Xml;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

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
        implements NavigationView.OnNavigationItemSelectedListener {

    List<PodcastParser.Entry> entries = new ArrayList<>();
    StringBuilder htmlString = new StringBuilder();
    String result = "";

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
        loadPage();
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
    private static final String URL = "http://ipctabernacle.org/component/podcastmanager/?format=raw&feedname=1";

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
    }

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
