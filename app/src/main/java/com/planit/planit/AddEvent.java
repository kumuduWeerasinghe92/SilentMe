package com.planit.planit;

//region android_base_imports
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ViewFlipper;
//endregion
//region firebase_imports
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.planit.planit.utils.Event;
import com.planit.planit.utils.FirebaseTables;
import com.planit.planit.utils.User;
//endregion
//region java_imports
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
//endregion

public class AddEvent extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private static final String TAG = "AddEvent";

    private static final int ERROR_DIALOG_REQUEST = 9001;
    // edit texts
    EditText eventName;
    EditText eventLocation;
    EditText eventAbout;
    String locaLatitude,locaLongitude;
    String ispublic="0";
    String issilent="0";


    // text views
    TextView eventDate;
    TextView eventendDate;
    TextView eventTime;
    TextView eventendTime;
    EditText eventArea;
    Switch isPublicSwitch;
    Switch isSilentSwitch;


    // Dialogs
    DatePickerDialog datePickerDialog;
    DatePickerDialog enddatePickerDialog;
    TimePickerDialog timePickerDialog;
    TimePickerDialog endtimePickerDialog;

    AppCompatButton addEventButton;

    User currentUser;

    //endregion

    //region FireBase
    private DatabaseReference mDatabase;
    FirebaseAuth fAuth;
    FirebaseUser fUser;
    //endregion

    private void init(){
        Button btnMap = (Button) findViewById(R.id.addMapBtn);
        btnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AddEvent.this, MapActivity.class);
                if(locaLatitude !=null && locaLongitude !=null)
                {
                    intent.putExtra("locaLatitude",locaLatitude);
                    intent.putExtra("locaLongitude",locaLongitude);
                }
                startActivityForResult(intent,1);
            }
        });
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if(resultCode == RESULT_OK) {
                locaLatitude = data.getStringExtra("locaLatitude");
                locaLongitude = data.getStringExtra("locaLongitude");


            }
        }
    }

    public boolean isServicesOK(){
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(AddEvent.this);

        if(available == ConnectionResult.SUCCESS){
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(AddEvent.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }else{
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        if(isServicesOK()){
            init();
        }
        //region firebase
        mDatabase = FirebaseDatabase.getInstance().getReference();
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
                    Intent loginIntent = new Intent(AddEvent.this, LoginActivity.class);
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
        //endregion

        setSupportActionBar((Toolbar) findViewById(R.id.add_event_toolbar));
        getSupportActionBar().setTitle(null);

        Bundle extras = getIntent().getExtras();

        currentUser = new Gson().fromJson(extras.getString("user"), User.class);

        Log.d("user between intents", "current users is " + currentUser.getPhoneNumber());

        eventName = (EditText) findViewById(R.id.event_name_input);
        eventLocation = (EditText) findViewById(R.id.event_location_input);
        eventAbout = (EditText) findViewById(R.id.event_about_input);

        eventDate = (TextView) findViewById(R.id.event_date_picker);
        eventendDate = (TextView) findViewById(R.id.event_edate_picker);
        eventTime = (TextView) findViewById(R.id.event_time_picker);
        eventendTime = (TextView) findViewById(R.id.event_etime_picker);
        isPublicSwitch = (Switch) findViewById(R.id.event_ispublic_input);
        isSilentSwitch = (Switch) findViewById(R.id.event_keepsilent_input);
        eventArea = (EditText) findViewById(R.id.event_area_input);

        eventDate.setOnClickListener(this);
        eventendDate.setOnClickListener(this);
        eventTime.setOnClickListener(this);
        eventendTime.setOnClickListener(this);

        isPublicSwitch.setOnCheckedChangeListener(this);
        isSilentSwitch.setOnCheckedChangeListener(this);

        Calendar calendar = Calendar.getInstance();

        //eventDate.setText(calendar.get(Calendar.DATE));

        datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                Log.i("PICKER CHECK", "Date is: " + new SimpleDateFormat("dd/MM/yyyy"));
                eventDate.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                datePickerDialog.onDateChanged(view, year, month, dayOfMonth);
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        enddatePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                eventendDate.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                enddatePickerDialog.onDateChanged(view, year, month, dayOfMonth);
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));


        timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                eventTime.setText((hourOfDay != 0 ? hourOfDay : hourOfDay + "0") + ":" +
                        (minute != 0 ? minute : minute + "0"));
            }
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);

        endtimePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                eventendTime.setText((hourOfDay != 0 ? hourOfDay : hourOfDay + "0") + ":" +
                        (minute != 0 ? minute : minute + "0"));
            }
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);

        addEventButton = (AppCompatButton) findViewById(R.id.add_event_button);
        addEventButton.setOnClickListener(this);
    }

    public boolean validate(){
        String eventNameStr = eventName.getText().toString();
        String eventLocationStr = eventLocation.getText().toString();
        String eventAboutStr = eventAbout.getText().toString();
        String eventDateStr = eventDate.getText().toString();
        String eventendDateStr = eventDate.getText().toString();
        String eventTimeStr = eventTime.getText().toString();
        String eventendTimeStr = eventTime.getText().toString();

        String evebtLatitudeStr = locaLatitude;
        String evebtLongitudeStr = locaLongitude;
        String eventAreaStr = eventArea.getText().toString();

        if (eventDateStr.isEmpty())
        {
            eventDate.setError("Event date can't be empty.");
            return false;
        }

        if (eventendDateStr.isEmpty())
        {
            eventendDate.setError("Event date can't be empty.");
            return false;
        }

        if (eventTimeStr.isEmpty())
        {
            eventTime.setError("Event time can't be empty.");
            return false;
        }

        if (eventendTimeStr.isEmpty())
        {
            eventendTime.setError("Event time can't be empty.");
            return false;
        }

        if (eventNameStr.isEmpty())
        {
            eventName.setError("Event name can't be empty.");
            return false;
        }
        if (eventLocationStr.isEmpty())
        {
            eventLocation.setError("Event location can't be empty.");
            return false;
        }

        if (evebtLatitudeStr ==null)
        {

            Toast.makeText(this, "Please set the event location.", Toast.LENGTH_SHORT).show();

            return false;
        }

        if (evebtLongitudeStr == null)
        {
            Toast.makeText(this, "Please set the event location.", Toast.LENGTH_SHORT).show();eventDate.setError("Please set the event location.");
            return false;
        }

        if (eventAreaStr.isEmpty())
        {
            eventArea.setError("Please set the event radius.");
            return false;
        }

        if (eventAboutStr.isEmpty())
        {
            eventAbout.setText(R.string.no_info);
        }


        return true;

    }

    public void AddEvent(){
        if(!validate())
        {
            return;
        }

        String eventNameStr = eventName.getText().toString();
        String eventLocationStr = eventLocation.getText().toString();
        String eventAboutStr = eventAbout.getText().toString();
        String eventDateStr = eventDate.getText().toString();
        String eventendDateStr = eventendDate.getText().toString();
        String eventTimeStr = eventTime.getText().toString();
        String eventendTimeStr = eventendTime.getText().toString();

        String evebtLatitudeStr = locaLatitude;
        String evebtLongitudeStr = locaLongitude;
        String eventAreaStr = eventArea.getText().toString();
        String eventIspublic = ispublic;
        String eventIssilent = issilent;

        writeNewEvent(eventNameStr, eventDateStr,eventendDateStr, eventTimeStr,eventendTimeStr, eventLocationStr, eventAboutStr, evebtLatitudeStr, evebtLongitudeStr, eventAreaStr, eventIspublic,eventIssilent);

        finish();
        //
    }

    private void writeNewEvent(final String name, final String date,final String enddate, final String time,final String endtime,
                               final String location, final String about,final String locaLatitude, final String locaLongitude, final String eventarea, final String eventispublic, final String eventIssilent){
        String eventKey = mDatabase.child("events").push().getKey();
        Log.e("New event id", "event id " + eventKey);
        Event event = new Event(name, date,enddate, time,endtime, location, about, currentUser.getPhoneNumber(),locaLatitude,locaLongitude,eventarea,eventispublic, eventIssilent);
        Map<String, Object> postValues = event.toMapBaseEventInfoTable();

        Map<String, Object> childUpdates = new HashMap<>();
        //puts the full event in events root in firebase
        childUpdates.put(FirebaseTables.eventsInfoTable + "/" + eventKey, postValues);
        mDatabase.updateChildren(childUpdates);

        currentUser.addHostedEvent(eventKey);

        //MAD FALSE//
        //puts new entry in events of this user
        childUpdates.clear();
        childUpdates.put(FirebaseTables.usersToEvents + "/" + currentUser.getPhoneNumber() + "/hosted/" + eventKey
                , false);
        mDatabase.updateChildren(childUpdates);

        //MAD FALSE//
        // put entries in this event's invited/hosted
        childUpdates.clear();
        childUpdates.put(FirebaseTables.eventsToUsers + "/" + eventKey + "/hosted/" +
                currentUser.getPhoneNumber(), true);
        mDatabase.updateChildren(childUpdates);
    }

    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.event_date_picker:
                datePickerDialog.show();
                break;

            case R.id.event_edate_picker:
                enddatePickerDialog.show();
                break;
            case R.id.event_time_picker:
                timePickerDialog.show();
                break;
            case R.id.event_etime_picker:
                endtimePickerDialog.show();
                break;
            case R.id.add_event_button:
                AddEvent();
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        switch(buttonView.getId()) {
            case R.id.event_ispublic_input:
                if (isChecked) {
                    ispublic = "1";
                } else {
                    ispublic = "0";
                }
                break;
            case R.id.event_keepsilent_input:
                if (isChecked) {
                    issilent = "1";
                } else {
                    issilent = "0";
                }
                break;
        }
    }
}
