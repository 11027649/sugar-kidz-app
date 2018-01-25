package e.natasja.sugar_kidz;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Logger;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity {
    String dateToday;

    ArrayList<Measurement> measurementArray;

    TextView date;
    TextView time;
    SimpleDateFormat dateSDF;
    Calendar myCalendar;

    String userID;

    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseRef;

    Boolean isParent;

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();

        if (mAuth.getCurrentUser() == null) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            finish();
            startActivity(intent);
        } else {
            userID = mAuth.getCurrentUser().getUid();
            mDatabaseRef = FirebaseDatabase.getInstance().getReference("users/" + userID);

            mDatabaseRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.
                    isParent = (boolean) dataSnapshot.child("isParent").getValue();
                    Log.d(TAG, "Value is: " + isParent);

                    if (isParent) {
                        Intent unauthorized = new Intent(MainActivity.this, LogbookActivity.class);
                        finish();
                        startActivity(unauthorized);
                    }
                }
                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value
                    Log.w(TAG, "Failed to read value.", error.toException());
                }
            });
        }

        setDate();

        populateLogbook(dateToday);

        Spinner moments = findViewById(R.id.labelMeasurement);
        ArrayAdapter<CharSequence> momentsAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.moments,
                android.R.layout.simple_dropdown_item_1line);

        momentsAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        moments.setAdapter(momentsAdapter);

   }

   @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch(id){
            case (R.id.menu_couple):
                Intent toCouple = new Intent(this, CoupleActivity.class);
                finish();
                startActivity(toCouple);
                break;

            case (R.id.menu_garden):
                Intent toGarden = new Intent(this, MyGardenActivity.class);
                finish();
                startActivity(toGarden);
                break;

            case (R.id.menu_logout):
                FirebaseAuth.getInstance().signOut();
                Intent logout = new Intent(this, LoginActivity.class);
                finish();
                startActivity(logout);
                break;

            case (R.id.menu_pokeshop):
                Intent toPokeshop = new Intent(MainActivity.this, PokeshopActivity.class);
                finish();
                startActivity(toPokeshop);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setDate() {
        date = findViewById(R.id.date);
        time = findViewById(R.id.time);

        long datetime = System.currentTimeMillis();
        dateSDF = new SimpleDateFormat("dd-MM-yy");
        SimpleDateFormat timeSDF = new SimpleDateFormat("HH:mm");

        dateToday = dateSDF.format(datetime);
        String timeToday = timeSDF.format(datetime);

        date.setText(dateToday);
        time.setText(timeToday);
    }

    public void populateLogbook(String dateToday) {
        FirebaseUser user = mAuth.getCurrentUser();
        String userId = user.getUid();
        DatabaseReference mDatabaseRef = mDatabase.getReference("users/" + userId + "/Measurements/" + dateToday);

        measurementArray = new ArrayList<>();
        measurementArray.clear();

        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange (DataSnapshot dataSnapshot) {
                Iterator<DataSnapshot> iterator = dataSnapshot.getChildren().iterator();
                Log.d(TAG, "Total Measurements: " + dataSnapshot.getChildrenCount());

                ListView myList = findViewById(R.id.listView);

                while (iterator.hasNext()) {
                    DataSnapshot measurement = iterator.next();
                    Log.d(TAG, String.valueOf(measurement.getChildrenCount()));

                    String timeMeasurement;

                    timeMeasurement = measurement.getKey();
                    SimpleMeasurement simple = measurement.getValue(SimpleMeasurement.class);
                    Measurement newMeasurement = new Measurement(simple.getLabel(), "Today", timeMeasurement, simple.getHeight());

                    measurementArray.add(newMeasurement);

                    LogbookAdapter mAdapter = new LogbookAdapter(getApplicationContext(), measurementArray);
                    myList.setAdapter(mAdapter);
                }

            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

    }

    public void datePicker(View view) {
        myCalendar = Calendar.getInstance();

        int year = myCalendar.get(Calendar.YEAR);
        int month = myCalendar.get(Calendar.MONTH);
        int day = myCalendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog mDatePicker;
        mDatePicker = new DatePickerDialog(MainActivity.this, new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, month);
                myCalendar.set(Calendar.DAY_OF_MONTH, day);

                date.setText(dateSDF.format(myCalendar.getTime()));
            }
        }, year, month, day);
        mDatePicker.setTitle("Kies de datum van je meting");
        mDatePicker.show();

    }

    public void timePicker(View view) {
        myCalendar = Calendar.getInstance();

        int hour = myCalendar.get(Calendar.HOUR_OF_DAY);
        int minute = myCalendar.get(Calendar.MINUTE);

        final TextView time = findViewById(R.id.time);

        TimePickerDialog mTimePicker;

        mTimePicker = new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                String timeString = selectedHour + ":" + selectedMinute;
                time.setText(timeString);
            }
        }, hour, minute, true);

        mTimePicker.setTitle("Kies de tijd van je meting");
        mTimePicker.show();
    }

    public void addMeasurement(View view) {
        TextView dateTextView = findViewById(R.id.date);
        TextView timeTextView = findViewById(R.id.time);
        Spinner labelSpinner = findViewById(R.id.labelMeasurement);
        EditText heightMeasurement = findViewById(R.id.hightMeasurement);

        final String timeMeasurement = timeTextView.getText().toString();
        final String dateMeasurement = dateTextView.getText().toString();
        final String label = labelSpinner.getSelectedItem().toString();
        final String height = heightMeasurement.getText().toString();

        if (height.equals("")) {
            Toast.makeText(this, "Vul de hoogte van je bloedsuiker in!", Toast.LENGTH_SHORT).show();
        } else {

            final FirebaseUser user = mAuth.getCurrentUser();
            final DatabaseReference mDatabaseRef = mDatabase.getReference("users/" + user.getUid());


            SimpleMeasurement simple = new SimpleMeasurement(label, height);

            mDatabaseRef.child("Measurements").child(dateMeasurement).child(timeMeasurement).setValue(simple).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (!task.isSuccessful()){
                        Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Je hebt deze meting toegevoegd!", Toast.LENGTH_SHORT).show();

                        gainXP(mDatabaseRef);
                    }
                }
            });
            }
    }

    public void gainXP(final DatabaseReference mDatabaseRef) {

        final ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.

                Long XPamount = (long) dataSnapshot.child("xpAmount").getValue();
                Log.d(TAG, "XPAmount is: " + XPamount);

                Long XPamountNew = XPamount + 100;
                mDatabaseRef.child("xpAmount").setValue(XPamountNew);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        };

        // Read from the database
        mDatabaseRef.addListenerForSingleValueEvent(listener);

    }

    public void goToLogbook(View view) {
        Intent intent = new Intent(this, LogbookActivity.class);
        finish();
        startActivity(intent);
    }

}
