package com.planit.planit.FireBaseService.MyFirebaseInstanceIDService;

import android.content.Intent;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.planit.planit.Home;
import com.planit.planit.LoginActivity;
import com.planit.planit.R;
import com.google.firebase.database.DatabaseReference;
import com.planit.planit.utils.User;
import com.planit.planit.utils.Utilities;

public class MyFirebaseInstanceIDService extends FirebaseMessagingService {
    private static final String TAG = "MyFirebaseIIDService";
    String refreshedToken;



    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        refreshedToken =s;
        sendRegistrationToServer(refreshedToken);
    }

    private void sendRegistrationToServer(final String token) {

        FirebaseAuth fAuth = FirebaseAuth.getInstance();
        if(fAuth.getCurrentUser() != null)
        {
            final DatabaseReference fDatabase = FirebaseDatabase.getInstance().getReference();
            fDatabase.child("emailsToPhones").child(Utilities.encodeKey(fAuth.getCurrentUser().getEmail()))
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            fDatabase.child("users").child(dataSnapshot.getValue(String.class))
                                    .child("token").setValue(token);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
        }
    }

    

}