package e.natasja.sugar_kidz;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Iterator;

public class LogbookActivity extends AppCompatActivity {
    Boolean isParent;

    private static final String TAG = "LogbookActivity";
    private LogbookAdapter mAdapter;

    String uid;
    String kidID;

    DatabaseReference mRef;

    Boolean notificationNeeded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logbook);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser aUser = mAuth.getCurrentUser();

        if (aUser == null) {
            Intent unauthorized = new Intent(this, LoginActivity.class);
            finish();
            startActivity(unauthorized);
        } else {
            uid = aUser.getUid();
            notificationNeeded = false;

            mRef = FirebaseDatabase.getInstance().getReference("users/" + uid);

            // check if user is a parent or a kid
            mRef.addListenerForSingleValueEvent(isParentListener);

        }
    }

    ValueEventListener isParentListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            isParent = (boolean) dataSnapshot.child("isParent").getValue();
            Log.d(TAG, "Value is: " + isParent);

            if (isParent) {
                checkIfCoupled();
            } else {
                doKidStuff();
            }
        }

        @Override
        public void onCancelled(DatabaseError error) {
            // failed to read value
            Log.w(TAG, "Failed to read value.", error.toException());
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(LogbookActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void doKidStuff() {
        TextView backbutton = findViewById(R.id.navigate);
        backbutton.setVisibility(View.INVISIBLE);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        TextView logoutParent = findViewById(R.id.parentLogout);
        logoutParent.setVisibility(View.INVISIBLE);

        // populate listview
        populateListView(uid);
    }

    public void setUncoupledParentUI() {
        // set back button invisible and disable navigation
        TextView backbutton = findViewById(R.id.navigate);
        String backbuttonText = "Nog geen account gekoppeld. Klik hier om een account te koppelen.";
        backbutton.setText(backbuttonText);

        TextView logoutParent = findViewById(R.id.parentLogout);
        logoutParent.setVisibility(View.VISIBLE);
    }

    public void setCoupledParentUI(String kidID) {
        // set back button invisible and disable navigation
        TextView backbutton = findViewById(R.id.navigate);
        String backbuttonText = "Je account is gekoppeld aan: " + kidID;
        backbutton.setText(backbuttonText);

        TextView logoutParent = findViewById(R.id.parentLogout);
        logoutParent.setVisibility(View.VISIBLE);

        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("users/" + kidID + "/Measurements");
        mRef.addValueEventListener(kidAddsMeasurementListener);
    }

    public void sendNotification() {

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, "Bae")
                .setSmallIcon(R.drawable.meter3)
                .setContentTitle("Nieuwe meting beschikbaar")
                .setContentText("Je kind heeft een nieuwe meting toegevoegd!");

        Intent resultIntent = new Intent(this, LogbookActivity.class);

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(resultPendingIntent);

        mBuilder.setLights(Color.BLUE, 500, 500);
        long[] pattern = {500,500,500,500,500,500,500,500,500};
        mBuilder.setVibrate(pattern);
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        mBuilder.setSound(alarmSound);

        int mNotificationId = 1;
        NotificationManager mManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mManager.notify(mNotificationId, mBuilder.build());
    }

    public void checkIfCoupled() {
        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("users/" + uid + "/coupled");

        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String coupledTo = String.valueOf(dataSnapshot.getValue());
                if (coupledTo.equals("false")){
                    setUncoupledParentUI();
                } else {
                    kidID = coupledTo;
                    setCoupledParentUI(coupledTo);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "Failed to read data from database.");
            }
        });
    }

    public void populateListView(String uidToDisplay) {
        mAdapter = new LogbookAdapter(this);

        FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();

        final DatabaseReference mDatabaseRef = mDatabase.getReference("users/" + uidToDisplay + "/Measurements");
        Query myTopSolvedQuery = mDatabaseRef.orderByChild("Measurements");

        myTopSolvedQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange (DataSnapshot dataSnapshot) {
               // iterate over dates
                Iterator<DataSnapshot> dateIterator = dataSnapshot.getChildren().iterator();

                while (dateIterator.hasNext()) {
                    DataSnapshot measurements = dateIterator.next();
                    Log.d(TAG, String.valueOf(measurements.getChildrenCount()));

                    // get date
                    String dateMeasurement = measurements.getKey();
                    mAdapter.addSectionHeaderItem(dateMeasurement);

                    // iterate over measurements at that date
                    Iterator<DataSnapshot> measurementIterator = dataSnapshot.child(dateMeasurement).getChildren().iterator();
                    Log.d(TAG, String.valueOf(measurements.getChildrenCount()));

                    while (measurementIterator.hasNext()) {
                        DataSnapshot measurement = measurementIterator.next();

                        // get time of measurement
                        String timeMeasurement = measurement.getKey();

                        // get label and height
                        SimpleMeasurement simple = measurement.getValue(SimpleMeasurement.class);
                        Measurement newMeasurement = new Measurement(simple.getLabel(), "Today", timeMeasurement, simple.getHeight());

                        mAdapter.addItem(newMeasurement);
                    }
                }

                // if you're a kid you don't need notifications
                // set notification needed to true after populating the listview to make sure it doesn't
                // send you a notification the moment you open the app
                if (isParent) {
                    notificationNeeded = true;
                }

                ListView mListView = findViewById(R.id.totalLogbookListView);
                mListView.setAdapter(mAdapter);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w(TAG, "Failed to read value.");
            }
        });
    }

    public ValueEventListener kidAddsMeasurementListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if (notificationNeeded) {
                mAdapter.removeAllItems();
                sendNotification();
            }
            populateListView(kidID);
        }
        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.w(TAG, "Failed to read value.");
        }
    };

    public void goToMain(View view) {
        if (isParent) {
            // if you're a parent, this is the couple button: so go to couple activity
            Intent toLogbook = new Intent(this, CoupleActivity.class);
            finish();
            startActivity(toLogbook);
        }
    }

    public void logout(View view) {
        if (isParent) {
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            mAuth.signOut();
            Intent logoutParent = new Intent(this, LoginActivity.class);
            finish();
            startActivity(logoutParent);
        }
    }
}
