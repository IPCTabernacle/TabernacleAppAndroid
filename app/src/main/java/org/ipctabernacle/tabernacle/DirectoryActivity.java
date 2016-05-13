package org.ipctabernacle.tabernacle;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.view.ViewPager;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.firebase.client.Firebase;
import com.firebase.ui.FirebaseListAdapter;

public class DirectoryActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private SectionsPagerAdapter mSectionsPaperAdapter;
    private ViewPager mViewPager;

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

        mSectionsPaperAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPaperAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(mViewPager);

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
        getMenuInflater().inflate(R.menu.directory, menu);

        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.directory_search).getActionView();
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

    public static class MembersFragment extends Fragment {

        Firebase membersRef;
        ListView lv;
        FirebaseListAdapter<MembersList> membersAdapters;

        public MembersFragment() {
        }

        public static MembersFragment newInstance() {
            MembersFragment membersFragment = new MembersFragment();
            return membersFragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_members, container, false);
            membersRef = new Firebase("https://tabernacle-directory.firebaseio.com/Members");
            lv = (ListView) rootView.findViewById(R.id.listview_members);
            membersAdapters = new FirebaseListAdapter<MembersList>(getActivity(), MembersList.class, R.layout.directory_list_item, membersRef) {
                @Override
                protected void populateView(View view, MembersList membersList, int i) {
                    if (membersList.getTitle() != null) {
                        ((TextView) view.findViewById(R.id.contact_name_holder)).setText(membersList.getTitle() + " " + membersList.getName());
                    } else {
                        ((TextView) view.findViewById(R.id.contact_name_holder)).setText(membersList.getName());
                    }
                    if (membersList.getNicknames() != null) {
                        ((TextView) view.findViewById(R.id.contact_subtitle_holder)).setText(membersList.getNicknames());
                    } else {
                        ((TextView) view.findViewById(R.id.contact_subtitle_holder)).setText(null);
                    }
                    ((TextView) view.findViewById(R.id.id_holder)).setText(membersList.getId());
                }
            };
            lv.setAdapter(membersAdapters);
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    TextView textView = (TextView) view.findViewById(R.id.id_holder);
                    String contactID = textView.getText().toString();
                    Intent intent = new Intent(getActivity(), MemberDetailsActivity.class);
                    intent.putExtra("listname", "Members/");
                    intent.putExtra("contactID", contactID);
                    startActivity(intent);
                }
            });
            return rootView;
        }
    }

    public static class LeadersFragment extends Fragment {

        Firebase leadersRef;
        ListView lv;
        FirebaseListAdapter<LeadersList> leadersAdapter;

        public LeadersFragment() {
        }

        public static LeadersFragment newInstance() {
            LeadersFragment leadersFragment = new LeadersFragment();
            return leadersFragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_leaders, container, false);
            leadersRef = new Firebase("https://tabernacle-directory.firebaseio.com/Leaders");
            lv = (ListView) rootView.findViewById(R.id.listview_leaders);
            leadersAdapter = new FirebaseListAdapter<LeadersList>(getActivity(), LeadersList.class, R.layout.directory_list_item, leadersRef) {
                @Override
                protected void populateView(View view, LeadersList leadersList, int i) {
                    if (leadersList.getTitle() != null) {
                        ((TextView) view.findViewById(R.id.contact_name_holder)).setText(leadersList.getTitle() + " " + leadersList.getName());
                    } else {
                        ((TextView) view.findViewById(R.id.contact_name_holder)).setText(leadersList.getName());
                    }
                    ((TextView) view.findViewById(R.id.contact_subtitle_holder)).setText(leadersList.getPosition());
                    ((TextView) view.findViewById(R.id.id_holder)).setText(leadersList.getId());
                }
            };
            lv.setAdapter(leadersAdapter);
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    TextView textView = (TextView) view.findViewById(R.id.id_holder);
                    String contactID = textView.getText().toString();
                    Intent intent = new Intent(getActivity(), LeaderDetailsActivity.class);
                    intent.putExtra("listname", "Leaders/");
                    intent.putExtra("contactID", contactID);
                    startActivity(intent);
                }
            });
            return rootView;
        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return MembersFragment.newInstance();
                case 1:
                    return LeadersFragment.newInstance();
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "MEMBERS";
                case 1:
                    return "LEADERS";
            }
            return null;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MembersList {
        private String name;
        private String id;
        private String nicknames;
        private String title;

        public MembersList() {}

        public MembersList(String title, String name, String nicknames, String id) {
            this.name = name;
            this.id = id;
            this.nicknames = nicknames;
            this.title = title;
        }

        public String getName() {
            return name;
        }

        public String getId() { return id; }

        public String getNicknames() { return nicknames; }

        public String getTitle() { return title; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LeadersList {
        private String title;
        private String name;
        private String position;
        private String id;

        public LeadersList() {
        }

        public LeadersList(String title, String name, String position, String id) {
            this.title = title;
            this.name = name;
            this.position = position;
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public String getName() {
            return name;
        }

        public String getPosition() {
            return position;
        }

        public String getId() {
            return id;
        }
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


