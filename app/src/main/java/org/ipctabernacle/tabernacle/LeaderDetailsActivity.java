package org.ipctabernacle.tabernacle;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.github.clans.fab.FloatingActionMenu;

import java.io.InputStream;
import java.net.URL;

/**
 * Created by Charles Koshy on 5/3/2016.
 */
public class LeaderDetailsActivity extends AppCompatActivity {

    ImageView backdrop;
    Bitmap bitmap;
    ProgressDialog pDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_details);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.contact_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_48dp);

        final CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.contact_collapsing_toolbar);

        //final ImageView contactImage = (ImageView) findViewById(R.id.contact_picture);
        //final TextView contactName = (TextView) findViewById(R.id.contact_name);
        backdrop = (ImageView) findViewById(R.id.contact_backdrop);
        final TextView contactSubtitle = (TextView) findViewById(R.id.contact_subtitle);
        //final TextView contactPosition = (TextView) findViewById(R.id.contact_nickname);
        final TextView contactAddress = (TextView) findViewById(R.id.contact_address);
        final TextView contactPhone = (TextView) findViewById(R.id.contact_phone);
        final ImageView phoneIcon1 = (ImageView) findViewById(R.id.phone_type_icon_1);
        final ImageView phoneIcon2 = (ImageView) findViewById(R.id.phone_type_icon_2);
        final TextView contactPhone2 = (TextView) findViewById(R.id.contact_phone_2);
        final ImageView phoneIcon3 = (ImageView) findViewById(R.id.phone_type_icon_3);
        final TextView contactPhone3 = (TextView) findViewById(R.id.contact_phone_3);
        final ImageButton mapButton = (ImageButton) findViewById(R.id.contact_button_navigation);
        final FloatingActionMenu callOptions = (FloatingActionMenu) findViewById(R.id.call_options_button);

        int clickedPosition = 1 + getIntent().getIntExtra("pos", 0);
        String listname = getIntent().getStringExtra("listname");
        String contactID = getIntent().getStringExtra("contactID");

        String fbURL = "https://tabernacle-directory.firebaseio.com/" + listname + contactID;
        Firebase ref = new Firebase(fbURL);

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final Leader leader = dataSnapshot.getValue(Leader.class);
                if (leader.getImage() != null) {
                    new LoadImage().execute(leader.getImage());
                } else {
                    backdrop.setImageResource(R.mipmap.contact_picture);
                }
                if (leader.getTitle() != null) {
                    if (leader.getNickname() != null) {
                        collapsingToolbar.setTitle(leader.getTitle() + " " + leader.getName() + " (" + leader.getNickname() + ")");
                    } else {
                        collapsingToolbar.setTitle(leader.getTitle() + " " + leader.getName());
                    }
                } else {
                    if (leader.getNickname() != null) {
                        collapsingToolbar.setTitle(leader.getName() + " (" + leader.getNickname() + ")");
                    } else {
                        collapsingToolbar.setTitle(leader.getName());
                    }
                }
                if (leader.getPosition() != null) {
                    contactSubtitle.setText(leader.getPosition());
                } else {
                    contactSubtitle.setVisibility(View.GONE);
                }
                if (leader.getAddress() != null) {
                    contactAddress.setText(leader.getAddress() + "\n" + leader.getCity() + ", " + leader.getState() + " " + leader.getZip());
                    mapButton.setVisibility(View.VISIBLE);
                } else {
                    contactAddress.setText("Oh no! There is no valid information here. Please let the administrator know.");
                }
                if (leader.getHomephone() == null) {
                    phoneIcon1.setVisibility(View.GONE);
                    contactPhone.setVisibility(View.GONE);
                } else {
                    contactPhone.setText(leader.getHomephone());
                }
                if (leader.getCellphone() != null) {
                    contactPhone2.setText(leader.getCellphone());
                } else {
                    contactPhone2.setVisibility(View.GONE);
                    phoneIcon2.setVisibility(View.GONE);
                }
                if (leader.getHomephone() == null) {
                    phoneIcon3.setVisibility(View.VISIBLE);
                    contactPhone3.setVisibility(View.VISIBLE);
                    contactPhone3.setText(leader.getCellphone2());
                }
                if (leader.getHomephone() == null && leader.getCellphone() == null) {
                    contactPhone.setText("Oh no! There is no valid information here. Please let the administrator know.");
                    contactPhone.setVisibility(View.VISIBLE);
                    phoneIcon3.setVisibility(View.GONE);
                    callOptions.setVisibility(View.GONE);
                }

                contactAddress.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=" + leader.getAddress() + " " + leader.getCity() + ", " + leader.getState() + " " + leader.getZip()));
                        startActivity(mapIntent);
                    }
                });
                contactPhone.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent dial = new Intent(Intent.ACTION_DIAL);
                        dial.setData(Uri.parse("tel:" + contactPhone.getText()));
                        startActivity(dial);
                    }
                });
                if (contactPhone2.getVisibility() == View.VISIBLE) {
                    contactPhone2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent dial = new Intent(Intent.ACTION_DIAL);
                            dial.setData(Uri.parse("tel:" + contactPhone2.getText()));
                            startActivity(dial);
                        }
                    });
                }
                if (contactPhone3.getVisibility() == View.VISIBLE) {
                    contactPhone3.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent dial = new Intent(Intent.ACTION_DIAL);
                            dial.setData(Uri.parse("tel:" + contactPhone3.getText()));
                            startActivity(dial);
                        }
                    });
                }

                mapButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=" + leader.getAddress() + " " + leader.getCity() + ", " + leader.getState() + " " + leader.getZip()));
                        startActivity(mapIntent);
                    }
                });

                createCustomAnimation();

                com.github.clans.fab.FloatingActionButton callButton1 = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.call_button_1);
                if (leader.getHomephone() == null && leader.getCellphone2() == null) {
                    callButton1.setLabelText("Call Cell");
                }
                if (leader.getHomephone() == null && leader.getCellphone() != null && leader.getCellphone2() != null) {
                    callButton1.setLabelText("Call Cell 1");
                }
                callButton1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent dialPrimary = new Intent(Intent.ACTION_DIAL);
                        if (leader.getHomephone() != null) {
                            dialPrimary.setData(Uri.parse("tel:" + leader.getHomephone()));
                        } else {
                            dialPrimary.setData(Uri.parse("tel:" + leader.getCellphone()));
                        }
                        startActivity(dialPrimary);
                    }
                });

                com.github.clans.fab.FloatingActionButton callButton2 = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.call_button_2);
                if (leader.getCellphone() == null) {
                    callButton2.setVisibility(View.GONE);
                }
                if (leader.getCellphone2() != null) {
                    callButton2.setLabelText("Call Cell 2");
                } else {
                    callButton2.setLabelText("Call Cell");
                }
                callButton2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent dialSecondary = new Intent(Intent.ACTION_DIAL);
                        if (leader.getHomephone() != null) {
                            dialSecondary.setData(Uri.parse("tel:" + leader.getCellphone()));
                        } else {
                            //if (member.getHomephone() == null && member.getCellphone2() != null) {
                            dialSecondary.setData(Uri.parse("tel:" + leader.getCellphone2()));
                        }
                        startActivity(dialSecondary);
                    }
                });
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

    }

    private class LoadImage extends AsyncTask<String, String, Bitmap> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected Bitmap doInBackground(String... args) {
            try {
                bitmap = BitmapFactory.decodeStream((InputStream)new URL(args[0]).getContent());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        protected void onPostExecute(Bitmap image) {
            if(image != null) {
                backdrop = (ImageView) findViewById(R.id.contact_backdrop);
                backdrop.setImageBitmap(image);
            } else {
                Toast.makeText(getApplicationContext(), "Image does not exist or network error", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void createCustomAnimation() {
        final FloatingActionMenu callOptions = (FloatingActionMenu) findViewById(R.id.call_options_button);
        AnimatorSet set = new AnimatorSet();
        ObjectAnimator scaleOutX = ObjectAnimator.ofFloat(callOptions.getMenuIconView(), "scaleX", 1.0f, 0.2f);
        ObjectAnimator scaleOutY = ObjectAnimator.ofFloat(callOptions.getMenuIconView(), "scaleY", 1.0f, 0.2f);
        ObjectAnimator scaleInX = ObjectAnimator.ofFloat(callOptions.getMenuIconView(), "scaleX", 0.2f, 1.0f);
        ObjectAnimator scaleInY = ObjectAnimator.ofFloat(callOptions.getMenuIconView(), "scaleY", 0.2f, 1.0f);
        scaleOutX.setDuration(50);
        scaleOutY.setDuration(50);
        scaleInX.setDuration(150);
        scaleInY.setDuration(150);
        scaleInX.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                callOptions.getMenuIconView().setImageResource(callOptions.isOpened() ? R.drawable.ic_call_white_24dp : R.drawable.ic_close_white_24dp);
            }
        });
        set.play(scaleOutX).with(scaleOutY);
        set.play(scaleInX).with(scaleInY).after(scaleOutX);
        set.setInterpolator(new OvershootInterpolator(2));
        callOptions.setIconToggleAnimatorSet(set);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Leader {
        private String image;
        private String title;
        private String name;
        private String nickname;
        private String position;
        //private String children;
        private String address;
        private String city;
        private String state;
        private String zip;
        private String homephone;
        private String cellphone;
        private String cellphone2;
        //private String other;
        //private String otherdescription;

        public Leader() {

        }

        public String getImage() { return image; }

        public String getTitle() { return title; }

        public String getName() {
            return name;
        }

        public String getNickname() {
            return nickname;
        }

        public String getPosition() { return position; }

/*        public String getChildren() {
            return children;
        }

        public String getOther() {
            return other;
        }

        public String getOtherdescription() {
            return otherdescription;
        }*/

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

        public String getCellphone2() { return cellphone2; }

        public String getCity() {
            return city;
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

}
