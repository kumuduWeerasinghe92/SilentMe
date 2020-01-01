package com.planit.planit;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.planit.planit.utils.Event;
import com.planit.planit.utils.FirebaseTables;
import com.planit.planit.utils.Item;
import com.planit.planit.utils.User;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.os.Handler;
public class EventActivity extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private static final String TAG = "EventActivity";

    private static final int ERROR_DIALOG_REQUEST = 9001;

    FirebaseAuth fAuth;
    FirebaseUser fUser;
    User currentUser;
    Event currentEvent;
    boolean isHost;
    DatabaseReference fDatabase;
    GoogleMap map;

    TextView eventTitle; // title in toolbar
    TextView eventLocation;
    TextView eventDate;
    TextView eventTime;
    TextView eventAbout;
    Switch eventIsEnable;
    private TextView countdown;
    private Handler handler;
    private Runnable runnable;

    CardView peopleCard;
    CardView planitCard;
    CardView mapCard;

    ChatFragment chatFragment;

    ValueEventListener eventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);




        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        Toolbar toolbar = (Toolbar) findViewById(R.id.event_activity_toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //need to change to the relevent event
        getSupportActionBar().setTitle(null);

        fAuth = FirebaseAuth.getInstance();
        if(fAuth.getCurrentUser() == null)
        {
            Intent loginIntent = new Intent(this, LoginActivity.class);
            startActivity(loginIntent);
            finish();
        }

        fAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null)
                {
                    Intent loginIntent = new Intent(EventActivity.this, LoginActivity.class);
                    startActivity(loginIntent);
                    finish();
                }
            }
        });
        fUser = fAuth.getCurrentUser();
        if(fUser == null)
        {
            Intent loginIntent = new Intent(this, LoginActivity.class);
            startActivity(loginIntent);
            finish();
        }

        Bundle extras = getIntent().getExtras();

        currentUser = new Gson().fromJson(extras.getString("user"), User.class);
        currentEvent = new Gson().fromJson(extras.getString("event"), Event.class);
        isHost = extras.getBoolean("isHost");

        chatFragment = new ChatFragment();
        Bundle chatArgs = new Bundle();
        chatArgs.putString("user", new Gson().toJson(currentUser));
        chatArgs.putString("event", new Gson().toJson(currentEvent));
        chatFragment.setArguments(chatArgs);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.chat_fragment_container, chatFragment);
        fragmentTransaction.commit();

        eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Event e = dataSnapshot.getValue(Event.class);
                e.setKey(dataSnapshot.getKey());
                currentEvent.setEvent(e);
                eventTitle.setText(currentEvent.getName());
                eventLocation.setText(currentEvent.getLocation());
                eventDate.setText(currentEvent.getDate());
                eventTime.setText(currentEvent.getTime());
                eventAbout.setText(currentEvent.getAbout());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        eventTitle = (TextView) findViewById(R.id.event_title);
        eventTitle.setMovementMethod(new ScrollingMovementMethod());
        eventTitle.requestFocus();
        eventLocation = (TextView) findViewById(R.id.info_location);
        eventLocation.setSelected(true);

        eventDate = (TextView) findViewById(R.id.info_date);
        eventTime = (TextView) findViewById(R.id.info_hour);
        eventAbout = (TextView) findViewById(R.id.about_text);
        eventIsEnable = (Switch) findViewById(R.id.event_isenable);

        fDatabase = FirebaseDatabase.getInstance().getReference();

        peopleCard = (CardView) findViewById(R.id.people_card);
        planitCard = (CardView) findViewById(R.id.planit_card);
        mapCard = (CardView) findViewById(R.id.map_view);

        peopleCard.setOnClickListener(this);
        planitCard.setOnClickListener(this);
        mapCard.setOnClickListener(this);
        eventIsEnable.setOnCheckedChangeListener(this);

        //added
        //added
        countdown = (TextView) findViewById(R.id.counter_text);


        if(isHost) {
            fDatabase.child(FirebaseTables.usersToEvents + "/" + currentUser.getPhoneNumber() + "/hosted/" + currentEvent.getKey()).
                    addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            if(dataSnapshot.getValue().toString().equals("true"))
                            {
                                eventIsEnable.setChecked(true);
                            }else
                                {
                                    eventIsEnable.setChecked(false);
                                }


                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

        }else
            {

                fDatabase.child(FirebaseTables.usersToEvents + "/" + currentUser.getPhoneNumber() + "/invited/" + currentEvent.getKey()).
                        addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {


                                if(dataSnapshot.getValue().toString().equals("true"))
                                {
                                    eventIsEnable.setChecked(true);
                                }else
                                {
                                    eventIsEnable.setChecked(false);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });


            }

        countDownStart();



    }


    public void countDownStart() {
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                handler.postDelayed(this, 1000);
                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat(
                            "dd/MM/yyyy hh:mm");
// Please here set your event date//YYYY-MM-DD
                    Date futureDate = dateFormat.parse(eventDate.getText().toString()+" "+eventTime.getText().toString());
                    Date currentDate = new Date();
                    if (!currentDate.after(futureDate)) {
                        long diff = futureDate.getTime()
                                - currentDate.getTime();
                        long days = diff / (24 * 60 * 60 * 1000);
                        diff -= days * (24 * 60 * 60 * 1000);
                        long hours = diff / (60 * 60 * 1000);
                        diff -= hours * (60 * 60 * 1000);
                        long minutes = diff / (60 * 1000);
                        diff -= minutes * (60 * 1000);
                        long seconds = diff / 1000;
                        countdown.setText("Starts In " + String.format("%02d", days)+" Days, "
                                + String.format("%02d", hours)+" Hours, "
                                + String.format("%02d", minutes)+" Min., "
                                + String.format("%02d", seconds) + " Sec.");
                    } else {
                        countdown.setText("The event started!");
                        //textViewGone();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        handler.postDelayed(runnable, 1 * 1000);

    }

    @Override
    protected void onStart() {
        super.onStart();
        eventTitle.setText(currentEvent.getName());
        eventLocation.setText(currentEvent.getLocation());
        eventDate.setText(currentEvent.getDate());
        eventTime.setText(currentEvent.getTime());
        eventAbout.setText(currentEvent.getAbout());
        fDatabase.child(FirebaseTables.eventsInfoTable + "/" + currentEvent.getKey()).
                addValueEventListener(eventListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        fDatabase.child(FirebaseTables.eventsInfoTable + "/" + currentEvent.getKey()).
                removeEventListener(eventListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (isHost)
        {
            getMenuInflater().inflate(R.menu.event_menu, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.action_edit:
                Intent editActivity = new Intent(this, EditEventActivity.class);
                editActivity.putExtra("user", new Gson().toJson(currentUser));
                editActivity.putExtra("event", new Gson().toJson(currentEvent));
                startActivity(editActivity);
                //finish();
                break;
        }
        return true;
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.planit_card:
                Intent planitPage = new Intent(this, PlanItActivity.class);
                planitPage.putExtra("user", new Gson().toJson(currentUser));
                planitPage.putExtra("event", new Gson().toJson(currentEvent));
                startActivity(planitPage);
                break;
            case R.id.people_card:
                Intent peoplePage = new Intent(this, InvitedPageActivity.class);
                peoplePage.putExtra("user", new Gson().toJson(currentUser));
                peoplePage.putExtra("event", new Gson().toJson(currentEvent));
                peoplePage.putExtra("isHost", isHost);
                startActivity(peoplePage);
                break;
            case R.id.map_view:
                Intent intent = new Intent(this, MapViewActivity.class);
                String locaLatitude = currentEvent.getLatitude();
                String locaLongitude = currentEvent.getLongitude();
                String eventtitle = currentEvent.getName();
                String ispublic = currentEvent.getIspublic();
                String radius = currentEvent.getArea();
                String issilent = currentEvent.getIssilent();
                if(currentEvent.getLatitude() !="" && currentEvent.getLatitude() !=null)
                {
                    intent.putExtra("locaLatitude",locaLatitude);
                    intent.putExtra("locaLongitude",locaLongitude);
                    intent.putExtra("eventtitle",eventtitle);
                    intent.putExtra("eventradius",radius);
                    intent.putExtra("ispublic",ispublic);
                    intent.putExtra("issilent",issilent);
                }
                startActivityForResult(intent,1);
        }
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {

            if(isHost)
            {

                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put(FirebaseTables.usersToEvents + "/" + currentUser.getPhoneNumber() + "/hosted/" + currentEvent.getKey()
                        , true);
                fDatabase.updateChildren(childUpdates);


            }else
                {

                    Map<String, Object> childUpdates = new HashMap<>();

                    childUpdates.put(FirebaseTables.usersToEvents + "/" + currentUser.getPhoneNumber() + "/invited/" + currentEvent.getKey()
                            , true);
                    fDatabase.updateChildren(childUpdates);


                }


        } else {

            if(isHost)
            {
                Map<String, Object> childUpdates = new HashMap<>();

                childUpdates.put(FirebaseTables.usersToEvents + "/" + currentUser.getPhoneNumber() + "/hosted/" + currentEvent.getKey()
                        , false);
                fDatabase.updateChildren(childUpdates);

            }else
            {
                Map<String, Object> childUpdates = new HashMap<>();


                childUpdates.put(FirebaseTables.usersToEvents + "/" + currentUser.getPhoneNumber() + "/invited/" + currentEvent.getKey()
                        , false);
                fDatabase.updateChildren(childUpdates);
            }

        }
    }
}
