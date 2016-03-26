package org.ipctabernacle.tabernacle;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.MenuInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DirectoryActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    //ListView listView;
    //ArrayList<String> results = new ArrayList();
    //DataBaseHelper myDBHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_directory);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ProgressDialog progress = new ProgressDialog(DirectoryActivity.this);
        progress.setTitle("Loading");
        progress.setMessage("Wait while loading...");
        progress.show();

        Firebase.setAndroidContext(this);
        final ListView listView = (ListView) findViewById(R.id.directory_list);
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.directory_list_item, R.id.myTextView);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), ContactDetailsActivity.class);
                startActivity(intent);
                //Toast.makeText(getApplicationContext(), "This is item " + position, Toast.LENGTH_LONG).show();
            }
        });

        new Firebase("https://tabernacle-directory.firebaseio.com/Members").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                adapter.add((String) dataSnapshot.child("name").getValue());
            }

            public void onChildRemoved(DataSnapshot dataSnapshot) {
                adapter.remove((String) dataSnapshot.child("name").getValue());
            }

            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            public void onCancelled(FirebaseError firebaseError) {
            }
        });

        progress.dismiss();
        //loadDataBase();
        //viewList();
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        //mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        //mViewPager = (ViewPager) findViewById(R.id.container);
        //mViewPager.setAdapter(mSectionsPagerAdapter);
        //handleIntent(getIntent());
    }

    //@Override
    //protected void onNewIntent(Intent intent) {
    //handleIntent(intent);
    //}

    //private void handleIntent(Intent intent) {
    //if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
    //String query = intent.getStringExtra(SearchManager.QUERY);
    //}
    //}

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    //private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    //private ViewPager mViewPager;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.directory, menu);

        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));

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

                    Intent home = new Intent(DirectoryActivity.this, MainActivity.class);
                    startActivity(home);
                }
            }, 250);

        } else if (id == R.id.nav_directory) {

        } else if (id == R.id.nav_podcast) {

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    Intent podcast = new Intent(DirectoryActivity.this, PodcastActivity.class);
                    startActivity(podcast);
                }
            }, 250);

        } else if (id == R.id.nav_giving) {

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    Intent giving = new Intent(DirectoryActivity.this, GivingActivity.class);
                    startActivity(giving);
                }
            }, 250);

        } else if (id == R.id.nav_settings) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /*private void loadDataBase() {
        myDBHelper = new DataBaseHelper(this);
        try {
            myDBHelper.createDataBase();
        } catch (IOException ioe) {
            throw new Error("Unable to create database");
        }
        try {
            myDBHelper.openDataBase();
        } catch (SQLException sqle) {
            throw new Error ("Unable to open database");
        }
    }

    private void viewList() {
        myDBHelper.openAndQueryDatabase();
        listView = (ListView) findViewById(R.id.myListView);
        ArrayAdapter adapter = new ArrayAdapter(this, R.layout.directory_list_item, R.id.myTextView, myDBHelper.results);
        listView.setAdapter(adapter);
    }*/


/*    public void onClickMelvin(View view) {

        Intent melvindetails = new Intent(this, MelvinDetails.class);

        final int result = 1;

        startActivity(melvindetails);
    }

    public void onClickCharles(View view) {

        Intent charlesdetails = new Intent(this, CharlesDetails.class);

        final int result = 1;

        startActivity(charlesdetails);
    }

    public void onClickTimo(View view) {

        Intent timodetails = new Intent(this, TimoDetails.class);

        final int result = 1;

        startActivity(timodetails);
    }*/
}


