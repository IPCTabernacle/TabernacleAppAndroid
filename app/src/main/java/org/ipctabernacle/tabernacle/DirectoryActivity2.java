package org.ipctabernacle.tabernacle;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.ListFragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.ui.FirebaseListAdapter;

/**
 * Created by Charles Koshy on 3/29/2016.
 */
public class DirectoryActivity2 extends AppCompatActivity
    implements NavigationView.OnNavigationItemSelectedListener {

    private SwipeRefreshLayout refreshDirectory;
    //Firebase mFirebaseRef;
    FirebaseListAdapter<MemberList> fblaMembers;

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

        Firebase.setAndroidContext(this);
        Firebase.getDefaultConfig().setPersistenceEnabled(true);

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(new DirectoryFragmentPagerAdapter(getSupportFragmentManager(), DirectoryActivity2.this));

        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);
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

                    Intent home = new Intent(DirectoryActivity2.this, MainActivity.class);
                    startActivity(home);
                }
            }, 250);

        } else if (id == R.id.nav_directory) {

        } else if (id == R.id.nav_podcast) {

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    Intent podcast = new Intent(DirectoryActivity2.this, PodcastActivity.class);
                    startActivity(podcast);
                }
            }, 250);

        } else if (id == R.id.nav_giving) {

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    Intent giving = new Intent(DirectoryActivity2.this, GivingActivity.class);
                    startActivity(giving);
                }
            }, 250);

        } else if (id == R.id.nav_settings) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public class DirectoryFragmentPagerAdapter extends FragmentPagerAdapter {
        final int PAGE_COUNT = 2;
        private String tabTitles[] = new String[] { "Members", "Leaders",};
        private Context context;

        public DirectoryFragmentPagerAdapter(FragmentManager fm, Context context) {
            super(fm);
            this.context = context;
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    MembersListFragment members = new MembersListFragment();
                    return members;
                case 1:
                    LeadersListFragment leaders = new LeadersListFragment();
                    return leaders;
                default:
                    return null;
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabTitles[position];
        }
    }



    public static class MembersListFragment extends ListFragment {

        private Firebase fbMembersRef;
        FirebaseListAdapter<MemberList> fblaMembers;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_page, container, false);
            return view;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            loaddirectory();
        }

        public void loaddirectory() {
            fbMembersRef = new Firebase("https://tabernacle-directory.firebaseio.com/Members");
            fblaMembers = new FirebaseListAdapter<MemberList>(getActivity(), MemberList.class, R.layout.directory_list_item, fbMembersRef) {
                @Override
                protected void populateView(View view, MemberList memberList, int i) {
                    ((TextView)view.findViewById(R.id.myTextView)).setText(memberList.getName());
                    ((TextView)view.findViewById(R.id.contact_city_holder)).setText(memberList.getCity() + ", " + memberList.getState());
                    }
            };
            getListView().setAdapter(fblaMembers);
            getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(getActivity(), ContactDetailsActivity.class);
                    intent.putExtra("listname", "Members/");
                    intent.putExtra("pos", position);
                    startActivity(intent);
                }
            });
        }
    }

    public static class LeadersListFragment extends ListFragment {

        private Firebase fbLeadersRef;
        FirebaseListAdapter<LeaderList> fblaLeaders;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            //mPage = getArguments().getInt(ARG_PAGE);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_page, container, false);
            return view;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            loadleaders();
        }

        public void loadleaders() {
            fbLeadersRef = new Firebase("https://tabernacle-directory.firebaseio.com/Leaders");
            fblaLeaders = new FirebaseListAdapter<LeaderList>(getActivity(), LeaderList.class, R.layout.directory_list_item, fbLeadersRef) {
                @Override
                protected void populateView(View view, LeaderList leaderList, int i) {
                    ((TextView)view.findViewById(R.id.myTextView)).setText(leaderList.getName());
                    ((TextView)view.findViewById(R.id.contact_city_holder)).setText(leaderList.getPosition());
                }
            };
            getListView().setAdapter(fblaLeaders);
            getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(getActivity(), ContactDetailsActivity.class);
                    intent.putExtra("listname", "Leaders/");
                    intent.putExtra("pos", position);
                    startActivity(intent);
                }
            });
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MemberList {
        private String name;
        private String city;
        private String state;

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

        public String getState() {
            return state;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LeaderList {
        private String name;
        private String position;

        public LeaderList() {}

        public LeaderList(String name, String position) {
            this.name = name;
            this.position = position;
        }

        public String getName() {
            return name;
        }

        public String getPosition() { return position; }
    }

}
