package com.planit.planit;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.media.AudioManager;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.planit.planit.FireBaseService.MyFirebaseInstanceIDService.MyFirebaseInstanceIDService;
import com.planit.planit.utils.Event;
import com.planit.planit.utils.FirebaseTables;
import com.planit.planit.utils.User;
import com.planit.planit.utils.Utilities;
import com.planit.planit.SplashScreen.SplashScreen;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class SilentService extends Service {
    public int counter=0;
    FirebaseAuth fAuth;
    FirebaseUser fUser;


    private Timer timer,timersetSilent;
    private TimerTask timerTask, timersetSilenttask;

    boolean isEventUpdatingInvited =false;
    boolean isEventUpdatingInvitedOppo =false;
    boolean isEventUpdatingHost =false;
    boolean isEventUpdatingHostOppo =false;
    boolean IS_SILENTSET=false;
    int hostedCount,invitedCount;
    LatLng currentLoc;
    DatabaseReference fDatabase;
    private User currentUser;
    HashMap<String, Event> hostedEvents,tempHostedEvent;
    HashMap<String, Event> invitedEvents,tempInvitedEvent;
    private FusedLocationProviderClient mFusedLocationProviderClient;

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 15f;
    private static final String ACCESS_NOTIFICATION_ = Manifest.permission.ACCESS_NOTIFICATION_POLICY;
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 6666;

    @Override
    public void onCreate() {
        super.onCreate();

        fDatabase=FirebaseDatabase.getInstance().getReference();
        hostedEvents = new HashMap<String, Event>();
        invitedEvents = new HashMap<String, Event>();
        currentUser = new User();

        fAuth = FirebaseAuth.getInstance();
        if(fAuth.getCurrentUser() != null)
        {
            fAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    if (firebaseAuth.getCurrentUser() != null)
                    {
                        getUser();
                    }
                }
            });
        }



    }



    private void getDeviceLocation()
    {

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try
        {
                final Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {

                        if(task.isSuccessful())
                        {

                            Location currentLocation = (Location) task.getResult();
                            if(currentLocation !=null) {
                                currentLoc = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                            }else
                                {
                                    currentLoc=null;
                                }
                        }
                    }
                });


        }catch(SecurityException e)
        {
            Log.d("Location","getDeviceLocation: Security exception "+e.getMessage());
        }


    }



    @RequiresApi(Build.VERSION_CODES.O)
    private void startMyOwnForeground()
    {
        Log.i("Count", "=====================================  Notification==============================");
        String NOTIFICATION_CHANNEL_ID = "example.permanence";
        String channelName = "Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setContentTitle("SilentMe is checking for events...")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(2, notification);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        getDeviceLocation();
        Log.i("Count", "=====================================  Notification=============================="+Build.VERSION.SDK_INT+"     "+Build.VERSION_CODES.O);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            startMyOwnForeground();
        }else {

            Intent notificationIntent = new Intent(SilentService.this, SplashScreen.class);

            PendingIntent pendingIntent = PendingIntent.getActivity(SilentService.this, 0,
                    notificationIntent, 0);

            Notification notification = new NotificationCompat.Builder(SilentService.this,"1234682")
                    .setSmallIcon(R.mipmap.ic_launcher2)
                    .setContentTitle("SilentMe ")
                    .setContentText("Checking for events...")
                    .setContentIntent(pendingIntent).build();

            startForeground(1337, notification);


        }



        startTimer();

        return START_STICKY;
    }


    private int getEventState(String date, String time, String edate, String etime) {

        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String startdatetime = date + " " + time + ":00";
        String enddatetime = edate + " " + etime + ":00";
        Date eventStartDate = new Date();
        Date eventEndDate = new Date();


        try {
            eventStartDate = df.parse(startdatetime);
            eventEndDate = df.parse(enddatetime);
        } catch (ParseException e) {
            Log.e("DateParse", "Parse erroor");
        }

        Date dateNow = new Date();


        if (eventStartDate.compareTo(dateNow) > 0) {
            return 0;
        } else {

            if (eventEndDate.compareTo(dateNow) > 0) {
                return 1;
            } else {
                return -1;
            }
        }


    }

        @Override
    public void onDestroy() {

        stoptimertask();
        Log.i("Count", "=====================================  Dead=============================="+Build.VERSION.SDK_INT+"     "+Build.VERSION_CODES.O);

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("restartservice");
        broadcastIntent.setClass(this, Restarter.class);
        this.sendBroadcast(broadcastIntent);
        super.onDestroy();

    }

    public void getUser()
    {
        String email = fAuth.getCurrentUser().getEmail();
        fDatabase.child("emailsToPhones").child(Utilities.encodeKey(email)).
                addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String phone = dataSnapshot.getValue(String.class);
                        Log.d("getUser", "Retrieved phone, phone is " + phone);
                        if (phone == null)
                        {
                            Log.d("BUG", "PHONE IS NULL!");
                        }
                        // got phone, now retrieve user and populate currentUser
                        fDatabase.child("users").child(phone).addListenerForSingleValueEvent(
                                new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        User u = dataSnapshot.getValue(User.class);
                                        if (u == null)
                                        {
                                            Log.d("getUser", "user not found");
                                            fAuth.signOut();
                                        }
                                        u.setPhoneNumber(dataSnapshot.getKey());
                                        currentUser.setUser(u);
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                }
                        );
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    public void getEvent(final String eventID, final HashMap<String,Event> events)
    {
        fDatabase.child("events/" + eventID).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {


                        if(dataSnapshot.getKey() !=null) {

                            Event event = dataSnapshot.getValue(Event.class);
                            event.setKey(dataSnapshot.getKey());

                            if (events.containsKey(eventID)) {
                                events.remove(eventID);
                                events.put(eventID, event);

                            } else {
                                events.put(eventID, event);
                            }

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                }
        );
    }



    public void putInSilent()
    {
        AudioManager aManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        Log.w("Eventlistner", ")))))))))))))))))))))))))))))))))))))))))))))))))))))))) "+aManager.getRingerMode());
        if(!IS_SILENTSET) {
            aManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
            IS_SILENTSET = true;
        }

    }

    public void putInNormal()
    {

        AudioManager aManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        Log.w("Eventlistner", "&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&& "+currentLoc.toString());
        if(IS_SILENTSET ) {
            aManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            IS_SILENTSET =false;

        }
    }



    public void startTimer() {
        timer = new Timer();
        timerTask = new TimerTask() {
            public void run() {


                Query invitedQ =  fDatabase.child(FirebaseTables.usersToEvents + "/" + currentUser.getPhoneNumber() + "/invited");
                Query hostedQ =  fDatabase.child(FirebaseTables.usersToEvents + "/" + currentUser.getPhoneNumber() + "/hosted");


                invitedQ.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {


                        isEventUpdatingInvited =true;
                        for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {

                            if(postSnapshot.getValue().toString().equals("true")) {
                                getEvent(postSnapshot.getKey(), invitedEvents);
                            }

                        }
                        List<String> ievents=new ArrayList<String>();
                        for (Map.Entry mapElement2 : invitedEvents.entrySet()) {
                            String key2 = (String)mapElement2.getKey();
                            int x=0;
                            for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {

                                if(postSnapshot.getValue().toString().equals("true")) {
                                    if (key2.equals(postSnapshot.getKey())) {
                                        x = 1;
                                        break;
                                    }
                                }
                            }
                            if(x==0)
                            {
                                ievents.add(key2);
                            }

                        }


                        for(String s:ievents){
                            invitedEvents.remove(s);
                        }
                        if(!isEventUpdatingInvitedOppo) {
                            tempInvitedEvent = invitedEvents;
                        }
                        isEventUpdatingInvited =false;
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                        Log.w("Eventlistner", "loadPost:onCancelled", databaseError.toException());

                    }
                });

                hostedQ.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {


                        isEventUpdatingHost =true;
                        for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {

                            if(postSnapshot.getValue().toString().equals("true")) {
                                getEvent(postSnapshot.getKey(), hostedEvents);
                            }



                        }
                        List<String> hevents=new ArrayList<String>();

                        for (Map.Entry mapElement2 : hostedEvents.entrySet()) {
                            String key2 = (String)mapElement2.getKey();
                            int x=0;
                            for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {

                                if(postSnapshot.getValue().toString().equals("true")) {
                                    if (key2.equals(postSnapshot.getKey())) {
                                        x = 1;
                                        break;
                                    }
                                }
                            }
                            if(x==0)
                            {
                                hevents.add(key2);
                            }

                        }

                        for(String s:hevents){
                            hostedEvents.remove(s);
                        }

                        if(!isEventUpdatingHostOppo) {
                            tempHostedEvent = hostedEvents;




                        }
                        isEventUpdatingHost =false;
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                        Log.w("Eventlistner", "loadPost:onCancelled", databaseError.toException());

                    }
                });



            }
        };
        timer.schedule(timerTask, 10000, 10000); //





        timersetSilent = new Timer();
        timersetSilenttask = new TimerTask() {
            @Override
            public void run() {

                boolean isSilentModeDetect =false;

                if(currentLoc !=null)
                {

                    isEventUpdatingInvitedOppo = true;
                    isEventUpdatingHostOppo = true;
                    if(!isEventUpdatingHost && tempHostedEvent != null) {

                        getDeviceLocation();
                        hostedCount = tempHostedEvent.size();
                        for (Map.Entry mapElement : tempHostedEvent.entrySet()) {
                            String key = (String) mapElement.getKey();

                            Event eh = (Event) mapElement.getValue();
                            Log.w("Eventlistner", "=========== EventTitle========= " + eh.getName());


                            String teventStartDateh = eh.getDate();
                            String teventStartTimeh = eh.getTime();
                            String teventEndDateh = eh.getendDate();
                            String teventEndTimeh = eh.getendTime();

                            if(getEventState(teventStartDateh,teventStartTimeh,teventEndDateh,teventEndTimeh) ==1)
                            {



                                Location locationCu = new Location("current");
                                Location locationEv = new Location("Event");

                                locationCu.setLatitude(currentLoc.latitude);
                                locationCu.setLongitude(currentLoc.longitude);

                                locationEv.setLatitude(Double.valueOf(eh.getLatitude()));
                                locationEv.setLongitude(Double.valueOf(eh.getLongitude()));

                                double distanceto =locationCu.distanceTo(locationEv);

                                Log.w("Eventlistner", "##################################"+distanceto);

                                if(Double.valueOf(eh.getArea())>=distanceto && eh.getIssilent().equals("1"))
                                {
                                    putInSilent();
                                    hostedCount--;
                                }
                            }



                        }





                    }

                    if(!isEventUpdatingInvited && tempInvitedEvent != null) {

                        getDeviceLocation();
                        invitedCount = tempInvitedEvent.size();
                        for (Map.Entry mapElement : tempInvitedEvent.entrySet()) {
                            String key = (String) mapElement.getKey();

                            Event ei = (Event) mapElement.getValue();
                            Log.w("Eventlistner", "=========== EventTitle========= " + ei.getName());

                            String teventStartDatei = ei.getDate();
                            String teventStartTimei = ei.getTime();
                            String teventEndDatei = ei.getendDate();
                            String teventEndTimei = ei.getendTime();

                            if(getEventState(teventStartDatei,teventStartTimei,teventEndDatei,teventEndTimei) ==1)
                            {


                                Location locationCu = new Location("current");
                                Location locationEv = new Location("Event");

                                locationCu.setLatitude(currentLoc.latitude);
                                locationCu.setLongitude(currentLoc.longitude);

                                locationEv.setLatitude(Double.valueOf(ei.getLatitude()));
                                locationEv.setLongitude(Double.valueOf(ei.getLongitude()));

                                double distanceto =locationCu.distanceTo(locationEv);
                                Log.w("Eventlistner", "##################################"+distanceto);

                                if(Double.valueOf(ei.getArea())>=distanceto && ei.getIssilent().equals("1"))
                                {
                                    putInSilent();
                                    invitedCount--;
                                }
                            }
                        }







                    }

                    if(tempInvitedEvent !=null && tempHostedEvent !=null) {
                        if (invitedCount == tempInvitedEvent.size() && hostedCount == tempHostedEvent.size()) {
                            putInNormal();
                        }
                    }





                    isEventUpdatingInvitedOppo = false;
                    isEventUpdatingHostOppo = false;


                }else
                    {
                            getDeviceLocation();

                    }





            }
        };
        timersetSilent.schedule(timersetSilenttask,3000,3000);
    }

    public void stoptimertask() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
