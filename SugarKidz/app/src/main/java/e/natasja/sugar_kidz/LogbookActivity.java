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

public class LogbookActivity extends AppCompatActivity implements ConnectionInterface {
    private static final String TAG = "LogbookActivity";

    DatabaseReference mRef;
    String uid;

    String kidUsername;
    String kidID;

    NotificationCompat.Builder mBuilder;

    private LogbookAdapter mAdapter;

    public static MainActivity delegate = null;

    Boolean isParent;
    Boolean notificationNeeded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logbook);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser aUser = mAuth.getCurrentUser();

        MainActivity.delegate = this;

        // check if the user is logged in and if the user is a parent
        if (aUser == null) {
            Intent unauthorized = new Intent(this, LoginActivity.class);
            finish();
            startActivity(unauthorized);
        } else {
            uid = aUser.getUid();

            // set the notifications to true if the listview is loaded and the user is a parent
            notificationNeeded = false;

            // check if user is a parent or a kid
            mRef = FirebaseDatabase.getInstance().getReference("users/" + uid);
            mRef.addListenerForSingleValueEvent(isParentListener);

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // if the kid clicks on back, go to Main, the parents can't see and use this button
            case android.R.id.home:
                Intent intent = new Intent(LogbookActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void closeActivity() {
        // when the internet connection is lost, finish this activity and go back to Main
        // (if the user is not a parent)
        if (!isParent) {
            finish();
        } else {
            LoginActivity.Toaster(
                    LogbookActivity.this,
                    "Je internetverbinding is verbroken");
        }
    }

    /**
     * This ValueEventListener listens to if the user is a parent, and chooses what UI to set
     * depending on if you're a coupled or uncoupled parent, or a kid.
     */
    ValueEventListener isParentListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            isParent = (boolean) dataSnapshot.child("isParent").getValue();
            Log.d(TAG, "Value is: " + isParent);

            if (isParent) {
                checkIfCoupled();
            } else {
                setKidUI();
            }
        }
        @Override
        public void onCancelled(DatabaseError error) {
            // failed to read value
            Log.w(TAG, "Failed to read value.", error.toException());
        }
    };

    /**
     * This function sets the page layout for the kid (user) to use it.
     */
    public void setKidUI() {
        TextView backbutton = findViewById(R.id.navigate);
        backbutton.setVisibility(View.INVISIBLE);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        TextView logoutParent = findViewById(R.id.parentLogout);
        logoutParent.setVisibility(View.INVISIBLE);

        // populate listview
        populateListView(uid);
    }

    /**
     * If the user is a parent, checks if it is coupled to an Account from a kid.
     */
    public void checkIfCoupled() {
        mRef = FirebaseDatabase.getInstance().getReference("users/" + uid + "/coupled");

        // this listener checks in firebase if you're coupled to a kid, and if yes, what there uid is
        ValueEventListener isCoupledListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String coupledTo = String.valueOf(dataSnapshot.getValue());
                if (coupledTo.equals("false")){
                    setUncoupledParentUI();
                } else {
                    kidID = String.valueOf(dataSnapshot.child("kidID").getValue());
                    kidUsername = String.valueOf(dataSnapshot.child("kidName").getValue());
                    setCoupledParentUI();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "Failed to read data from database.");
            }
        };

        mRef.addListenerForSingleValueEvent(isCoupledListener);
    }

    /**
     * Set the LayOut for the uncoupled parent. They can't see any measurements, but can go to the
     * couple activity to couple their account to their kid's.
     */
    public void setUncoupledParentUI() {
        // use the "backbutton" to navigate to the couple activity
        TextView backbutton = findViewById(R.id.navigate);
        String backbuttonText = "Klik hier om een account te koppelen.";
        backbutton.setText(backbuttonText);

        // make sure the parent can logout
        TextView logoutParent = findViewById(R.id.parentLogout);
        logoutParent.setVisibility(View.VISIBLE);
    }

    /**
     * The coupled parent can see the measurements from the kids account in the ListView.
     */
    public void setCoupledParentUI() {
        // set button to what kid you're coupled to
        TextView backbutton = findViewById(R.id.navigate);
        String backbuttonText = "Je account is gekoppeld aan: " + kidUsername;
        backbutton.setText(backbuttonText);

        // make sure the parent can logout
        TextView logoutParent = findViewById(R.id.parentLogout);
        logoutParent.setVisibility(View.VISIBLE);

        // add a listener to the firebase of the kid, to get a notification after they add a new measurement
        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("users/" + kidID + "/Measurements");
        mRef.addValueEventListener(kidAddsMeasurementListener);
    }

    /**
     * This is a ValueEventListener that gives a notification if the kid adds a measurement.
     * It also populates the ListView again.
     */
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

    /**
     * This function builds a notification to send to the parent as their kid adds a new measurement.
     * If you click on a notification you'll be send to the logbookactivity.
     */
    public void sendNotification() {
        mBuilder = new NotificationCompat.Builder(this, "001")
                .setSmallIcon(R.drawable.meter3)
                .setContentTitle("Nieuwe meting beschikbaar")
                .setContentText("Je kind heeft een nieuwe meting toegevoegd!");

        makeIntentForNotification();

        // prepare the vibration and lights pattern for the notification
        mBuilder.setLights(Color.BLUE, 500, 500);
        long[] pattern = {500,500,500,500,500,500};
        mBuilder.setVibrate(pattern);
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        mBuilder.setSound(alarmSound);

        int mNotificationId = 1;
        NotificationManager mManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // build and send the actual notfication
        mManager.notify(mNotificationId, mBuilder.build());
    }

    /**
     * This function makes the intent for when the receiver of the notfication clicks on it.
     */
    public void makeIntentForNotification() {
        // prepare the intent
        Intent resultIntent = new Intent(this, LogbookActivity.class);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(
                this,
                0,
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        // set the intent
        mBuilder.setContentIntent(resultPendingIntent);
    }

    /**
     * This function populates the ListView. It takes an UID as input, because it needs to know
     * with what measurements the ListView should be populated (as there's a parent logged in, it
     * needs to have the kid's uid, not the parents, while as the kid's logged in, it needs the
     * 'normal' uid.
     */
    public void populateListView(String uidToDisplay) {
        mAdapter = new LogbookAdapter(this);

        FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();

        mRef = mDatabase.getReference("users/" + uidToDisplay + "/Measurements");
        mRef.addListenerForSingleValueEvent(measurementListener);
    }

    /**
     * This Value Event Listener adds the measurements of today to a list and sets the adapter to
     * the ListView if that is done.
     */
    ValueEventListener measurementListener = new ValueEventListener() {
        @Override
        public void onDataChange (DataSnapshot dataSnapshot) {
            // iterate over dates
            for (DataSnapshot measurements : dataSnapshot.getChildren()) {
                // get date
                String dateMeasurement = measurements.getKey();
                mAdapter.addSectionHeaderItem(dateMeasurement);

                // iterate over measurements at that date
                for (DataSnapshot measurement : dataSnapshot.child(dateMeasurement).getChildren()) {
                    // get time of measurement
                    String timeMeasurement = measurement.getKey();

                    // get label and height
                    SimpleMeasurement simple = measurement.getValue(SimpleMeasurement.class);
                    Measurement newMeasurement = null;

                    if (simple != null) {
                        newMeasurement = new Measurement(simple.label, "Today", timeMeasurement, simple.height);
                    }
                    mAdapter.addItem(newMeasurement);
                }
            }

            setAdapter();
        }

        @Override
        public void onCancelled(DatabaseError error) {
            Log.w(TAG, "Failed to read value.");
        }
    };

    /**
     * This function actually sets the adapter to the listview, and scrolls it all the way down so
     * the user see the new measurement at first. It also changes the boolean NotificationNeeded
     * to true if you're a parent. The kids don't get notifications.
     */
    public void setAdapter() {
        // set notification needed to true after populating the listview to make sure it doesn't
        // send you a notification the moment you open the app
        if (isParent) {
            notificationNeeded = true;
        }

        ListView mListView = findViewById(R.id.totalLogbookListView);
        mListView.setAdapter(mAdapter);
        mListView.setSelection(mAdapter.getCount() - 1);
    }

    /**
     * This is the onClick listener of the button. It sends the parent to the couple activity.
     * Also if the parent is already coupled. They can couple their account to another account
     * overwriting the account that they're already coupled to.
     */
    public void goToCouple(View view) {
        if (isParent) {
            // if you're a parent, this is the couple button: so go to couple activity
            Intent toLogbook = new Intent(this, CoupleActivity.class);
            finish();
            startActivity(toLogbook);
        }
    }

    /**
     * This is the LogoutButton listener, it only listens to parent clicks.
     */
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
