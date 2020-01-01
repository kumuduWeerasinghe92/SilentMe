package com.planit.planit;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.planit.planit.R;
import com.planit.planit.utils.Event;
import com.planit.planit.utils.FirebaseTables;
import com.planit.planit.utils.User;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;



public class EditEventActivity extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

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

    AppCompatButton editEventButton;

    User currentUser;
    Event currentEvent;

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
                Intent intent = new Intent(EditEventActivity.this, MapActivity.class);
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

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(EditEventActivity.this);

        if(available == ConnectionResult.SUCCESS){
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(EditEventActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }else{
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_event);

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
                    Intent loginIntent = new Intent(EditEventActivity.this, LoginActivity.class);
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

        setSupportActionBar((Toolbar) findViewById(R.id.edit_event_toolbar));
        getSupportActionBar().setTitle(null);

        Bundle extras = getIntent().getExtras();

        currentUser = new Gson().fromJson(extras.getString("user"), User.class);
        currentEvent = new Gson().fromJson(extras.getString("event"), Event.class);

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

        eventName.setText(currentEvent.getName());
        eventLocation.setText(currentEvent.getLocation());
        eventAbout.setText(currentEvent.getAbout());

        eventDate.setText(currentEvent.getDate());
        eventendDate.setText(currentEvent.getendDate());
        eventTime.setText(currentEvent.getTime());
        eventendTime.setText(currentEvent.getendTime());
        eventArea.setText(currentEvent.getArea());
        locaLatitude = currentEvent.getLatitude();
        locaLongitude = currentEvent.getLongitude();

        if(currentEvent.getIspublic().equals("1"))
        {
            isPublicSwitch.setChecked(true);
            ispublic="1";

        }else
            {
                isPublicSwitch.setChecked(false);
                ispublic="0";
            }

        if(currentEvent.getIssilent().equals("1"))
        {
            isSilentSwitch.setChecked(true);
            issilent="1";

        }else
        {
            isSilentSwitch.setChecked(false);
            issilent="0";
        }
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

        editEventButton = (AppCompatButton) findViewById(R.id.add_event_button);
        editEventButton.setOnClickListener(this);
        editEventButton.setText(R.string.edit);
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

        if (evebtLatitudeStr.isEmpty())
        {

            Toast.makeText(this, "Please set the event location.", Toast.LENGTH_SHORT).show();

            return false;
        }

        if (evebtLongitudeStr.isEmpty())
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

        editEvent(eventNameStr, eventDateStr,eventendDateStr, eventTimeStr,eventendTimeStr, eventLocationStr, eventAboutStr, evebtLatitudeStr, evebtLongitudeStr, eventAreaStr, eventIspublic,eventIssilent);

        finish();
    }

    private void editEvent(final String name, final String date,final String enddate, final String time, final String endtime,
                               final String location, final String about,final String locaLatitude, final String locaLongitude, final String eventarea, final String eventispublic, final String eventIssilent){
        String eventKey = currentEvent.getKey();
        Event event = new Event(name, date,enddate, time,endtime, location, about, currentUser.getPhoneNumber(),locaLatitude,locaLongitude,eventarea,eventispublic,eventIssilent);
        Map<String, Object> postValues = event.toMapBaseEventInfoTable();

        Map<String, Object> childUpdates = new HashMap<>();
        // rewrites the current event with the given values
        childUpdates.put(FirebaseTables.eventsInfoTable + "/" + eventKey, postValues);
        mDatabase.updateChildren(childUpdates);
        currentEvent.setEvent(event);
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
