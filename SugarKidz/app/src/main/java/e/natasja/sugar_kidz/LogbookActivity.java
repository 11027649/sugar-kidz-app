package e.natasja.sugar_kidz;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logbook);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser aUser = mAuth.getCurrentUser();
        String userID = aUser.getUid();

        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("users/" + userID);

        // check if the person that comes here is a parent or a kid
        // this is the only activity in the app where they both can come
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                isParent = (boolean) dataSnapshot.child("isParent").getValue();
                Log.d(TAG, "Value is: " + isParent);

                if (isParent) {
                    doParentStuff();
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
        // set back button visible and enable navigation
        TextView backbutton = findViewById(R.id.backToMain);
        backbutton.setVisibility(View.VISIBLE);

        // populate listview
        populateListView();
    }

    public void doParentStuff() {
        // set back button invisible and disable navigation
        TextView backbutton = findViewById(R.id.backToMain);
        backbutton.setVisibility(View.INVISIBLE);

        // only populate listview if account is coupled
    }

    public void populateListView() {
        mAdapter = new TotalLogbookAdapter(this);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();

        final FirebaseUser user = mAuth.getCurrentUser();
        final DatabaseReference mDatabaseRef = mDatabase.getReference("users/" + user.getUid() + "/Measurements");

        mDatabaseRef.addValueEventListener(new ValueEventListener() {
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

                ListView mListView = findViewById(R.id.totalLogbookListView);
                mListView.setAdapter(mAdapter);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    public void goToMain(View view) {
        if (isParent) {
            Log.w(TAG, "Tapped on invisible backbutton");
        } else {
            // if not a parent, go back
            Intent intent = new Intent(this, MainActivity.class);
            finish();
            startActivity(intent);
        }

    }
}
