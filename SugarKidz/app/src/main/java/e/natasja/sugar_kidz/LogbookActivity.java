package e.natasja.sugar_kidz;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Iterator;

public class LogbookActivity extends AppCompatActivity {
    Boolean isParent;

    private static final String TAG = "LogbookActivity";
    private TotalLogbookAdapter mAdapter;

    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logbook);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser aUser = mAuth.getCurrentUser();

        userID = aUser.getUid();

        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("users/" + userID);

        // check if the person that comes here is a parent or a kid
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
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
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    public void doKidStuff() {
        TextView backbutton = findViewById(R.id.navigate);
        backbutton.setText("Terug naar hoofdscherm");

        TextView logoutParent = findViewById(R.id.parentLogout);
        logoutParent.setVisibility(View.INVISIBLE);

        // populate listview
        populateListView(userID);
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

        populateListView(kidID);
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

        int mNotificationId = 1;
        NotificationManager mManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mManager.notify(mNotificationId, mBuilder.build());
    }

    public void checkIfCoupled() {
        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("users/" + userID + "/coupled");

        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String coupledTo = String.valueOf(dataSnapshot.getValue());
                if (coupledTo.equals("null")){
                    setUncoupledParentUI();
                } else {
                    setCoupledParentUI(coupledTo);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "Failed to read data from database.");
            }
        });
    }

    public void populateListView(String uid) {
        mAdapter = new TotalLogbookAdapter(this);

        FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();

        final DatabaseReference mDatabaseRef = mDatabase.getReference("users/" + uid + "/Measurements");

        mDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange (DataSnapshot dataSnapshot) {
               // iterate over dates
                Iterator<DataSnapshot> dateIterator = dataSnapshot.getChildren().iterator();

                sendNotification();

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

                ListView mListView = findViewById(R.id.totalLogbookListView);
                mListView.setAdapter(mAdapter);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w(TAG, "Failed to read value.");
            }
        });
    }

    public void goToMain(View view) {
        if (isParent) {
            // if you're a parent, this is the couple button: so go to couple activity
            Intent toLogbook = new Intent(this, CoupleActivity.class);
            finish();
            startActivity(toLogbook);
        } else {
            // if not a parent, go back to main
            Intent intent = new Intent(this, MainActivity.class);
            finish();
            startActivity(intent);
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
