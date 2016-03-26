package org.ipctabernacle.tabernacle;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

/**
 * Created by Charles Koshy on 3/25/2016.
 */
public class ContactDetailsActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_details);
        final ImageView contactImage = (ImageView) findViewById(R.id.contact_picture);
        final TextView contactName = (TextView) findViewById(R.id.contact_name);
        final TextView contactNick = (TextView) findViewById(R.id.contact_nickname);
        final TextView contactAddress = (TextView) findViewById(R.id.contact_address);
        final TextView contactPhone = (TextView) findViewById(R.id.contact_phone);

        Firebase ref = new Firebase("https://tabernacle-directory.firebaseio.com/Members/1");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //for (DataSnapshot memberSnapshot : dataSnapshot.getChildren()) {
                final Member member = dataSnapshot.getValue(Member.class);
                contactName.setText(member.getName());
                contactNick.setText("(" + member.getNicknames() + ")");
                contactAddress.setText(member.getAddress() + "\n" + member.getCity() + ", " + member.getState() + " " + member.getZip());
                contactPhone.setText(member.getHomephone());
                //}
                FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.contact_fab);
                fab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent dial = new Intent(Intent.ACTION_DIAL);
                        dial.setData(Uri.parse("tel:" + member.getHomephone()));
                        startActivity(dial);
                    }
                });
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Member {
        private String name;
        private String city;
        private String nicknames;
        private String children;
        private String address;
        private String state;
        private String zip;
        private String homephone;
        private String cellphone;
        private String other;
        private String otherdescription;

        //public Member(String stringJSON) {

        //}

        public Member() {

        }

        public String getName() {
            return name;
        }

        public String getNicknames() {
            return nicknames;
        }

        public String getChildren() {
            return children;
        }

        public String getOther() {
            return other;
        }

        public String getOtherdescription() {
            return otherdescription;
        }

        public String getAddress() {
            return address;
        }

        public String getState() {
            return state;
        }

        public String getZip() {
            return zip;
        }

        public String getHomephone() {
            return homephone;
        }

        public String getCellphone() {
            return cellphone;
        }

        public String getCity() {
            return city;
        }
    }
}

