package e.natasja.sugar_kidz;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    String dateToday;
    String timeToday;

    ArrayList<Measurement> measurementArray;

    TextView date;
    TextView time;

    SimpleDateFormat dateSDF;
    SimpleDateFormat timeSDF;
    Calendar myCalendar;

    String uid;

    private FirebaseDatabase mDatabase;
    private DatabaseReference mRef;

    Boolean isParent;
    Boolean isConnected;

    ListView myList;

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        isConnected = false;
        checkConnectivity();

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();

        if (mAuth.getCurrentUser() == null) {
            Intent unauthorized = new Intent(MainActivity.this, LoginActivity.class);
            finish();
            startActivity(unauthorized);
        } else {
            uid = mAuth.getCurrentUser().getUid();

            // check if user is a parent
            mRef = FirebaseDatabase.getInstance().getReference("users/" + uid);
            mRef.addListenerForSingleValueEvent(isParentListener);

            setDate();
            populateLogbook(dateToday);
            populateSpinner();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        setConnectionInfo();
    }

    private void setConnectionInfo() {
        TextView connected = findViewById(R.id.connectionStatus);
        if (!isConnected) {
            String notConnected = "Gebruikersprofiel: onbekend (Niet verbonden)";
            connected.setText(notConnected);
            connected.setTextColor(Color.RED);
        } else {
            String connectedString = "Gebruikersprofiel: Natasja (Verbonden)";
            connected.setText(connectedString);
            connected.setTextColor(Color.GREEN);
        }
    }

    /**
     * This is a function that checks whether you're connected to the internet: if you're not, it
     * sends you back to the MainActivity, but disables all further actions.
     */
    private void checkConnectivity() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkRequest.Builder builder = new NetworkRequest.Builder();

        connectivityManager.registerNetworkCallback(
                builder.build(),
                new ConnectivityManager.NetworkCallback() {
                    @Override
                    public void onAvailable(Network network) {
                        isConnected = true;
//                        setConnectionInfo();
                    }
                    @Override
                    public void onLost(Network network) {
                        isConnected = false;
                        Intent connectionLost = new Intent(getApplicationContext(), MainActivity.class);
                        finish();
                        startActivity(connectionLost);

                        LoginActivity.Toaster(getApplicationContext(), "Internet verbinding onderbroken.");
                    }
                }
        );
    }

    /**
     * This function sets the spinner that is used to label a measurement with a dutch time-of-the-day
     * that is common to measure your glucose levels.
     */
    public void populateSpinner() {
        Spinner moments = findViewById(R.id.labelMeasurement);
        ArrayAdapter<CharSequence> momentsAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.moments,
                android.R.layout.simple_dropdown_item_1line);

        momentsAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        moments.setAdapter(momentsAdapter);
    }

    ValueEventListener isParentListener = new ValueEventListener() {
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
   };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (isConnected) {
            switch(id) {
                case (R.id.menu_couple):
                    Intent toCouple = new Intent(this, CoupleActivity.class);
                    startActivity(toCouple);
                    break;

                case (R.id.menu_garden):
                    Intent toGarden = new Intent(this, GardenActivity.class);
                    startActivity(toGarden);
                    break;

                case (R.id.menu_logout):
                    FirebaseAuth.getInstance().signOut();
                    Intent logout = new Intent(this, LoginActivity.class);
                    startActivity(logout);
                    break;

                case (R.id.menu_pokeshop):
                    Intent toPokeshop = new Intent(MainActivity.this, PokeshopActivity.class);
                    startActivity(toPokeshop);
                    break;
            }
        } else {
            LoginActivity.Toaster(
                    MainActivity.this,
                    "Deze actie kan niet worden uitgevoerd omdat je geen internet verbinding hebt.");
        }

        return super.onOptionsItemSelected(item);
    }

    public void setDate() {
        date = findViewById(R.id.date);
        time = findViewById(R.id.time);

        long datetime = System.currentTimeMillis();
        dateSDF = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        timeSDF = new SimpleDateFormat("HH:mm", Locale.getDefault());

        dateToday = dateSDF.format(datetime);
        timeToday = timeSDF.format(datetime);

        date.setText(dateToday);
        time.setText(timeToday);
    }

    public void populateLogbook(final String dateToday) {
        mRef = mDatabase.getReference("users/" + uid + "/Measurements/" + dateToday);
        myList = findViewById(R.id.listView);
        measurementArray = new ArrayList<>();

        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange (DataSnapshot dataSnapshot) {
                Iterator<DataSnapshot> iterator = dataSnapshot.getChildren().iterator();
                Log.d(TAG, "Total Measurements: " + dataSnapshot.getChildrenCount());

                measurementArray.clear();

                while (iterator.hasNext()) {
                    DataSnapshot measurement = iterator.next();
                    Log.d(TAG, String.valueOf(measurement.getChildrenCount()));

                    String timeMeasurement;

                    timeMeasurement = measurement.getKey();
                    SimpleMeasurement simple = measurement.getValue(SimpleMeasurement.class);
                    Measurement newMeasurement = new Measurement(simple.label, dateToday, timeMeasurement, simple.height);

                    measurementArray.add(newMeasurement);
                }

                if (measurementArray.isEmpty()) {
                    Measurement noMeasurementsYet = new Measurement("Nog geen metingen vandaag");
                    measurementArray.add(noMeasurementsYet);
                } else {
                    Collections.reverse(measurementArray);
                    setLastMeasurement();
                }

                MainLogbookAdapter mAdapter = new MainLogbookAdapter(MainActivity.this, measurementArray);
                myList.setAdapter(mAdapter);
                mAdapter.deleteOnLongClick(myList);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });




    }

    private void setLastMeasurement() {
        TextView date = findViewById(R.id.lastMeasurementDate);
        TextView height = findViewById(R.id.lastMeasurementHeight);

        Measurement lastMeasurement = measurementArray.get(0);

        String dateString = lastMeasurement.dateMeasurement + ", " + lastMeasurement.timeMeasurement;
        String heightString = lastMeasurement.heightMeasurement;

        date.setText(dateString);
        height.setText(heightString);
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

        TimePickerDialog mTimePicker;

        mTimePicker = new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                myCalendar.set(Calendar.HOUR_OF_DAY, selectedHour);
                myCalendar.set(Calendar.MINUTE, selectedMinute);

                time.setText(timeSDF.format(myCalendar.getTime()));
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

            mRef = mDatabase.getReference("users/" + uid);
            SimpleMeasurement simple = new SimpleMeasurement(label, height);

            // add this measurement to firebase (if there isn't a measurement yet at this time)
            mRef.child("Measurements").child(dateMeasurement).child(timeMeasurement).setValue(simple).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (!task.isSuccessful()){
                        Log.w(TAG, "Exception occured: " + task.getException());
                        Toast.makeText(getApplicationContext(), "Iets ging fout... :(", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Je hebt deze meting toegevoegd!", Toast.LENGTH_SHORT).show();
                        gainXP();
                    }
                }
            });
        }
    }

    public void gainXP() {

        final ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.

                Long XPamount = (long) dataSnapshot.child("xpAmount").getValue();
                Log.d(TAG, "XPAmount is: " + XPamount);

                Long XPamountNew = XPamount + 100;
                mRef.child("xpAmount").setValue(XPamountNew);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        };

        // Read from the database
        mRef.addListenerForSingleValueEvent(listener);

    }

    /**
     * This is the onClick listener for the go to logbook button. If you're not connected, you can't
     * use this button.
     */
    public void goToLogbook(View view) {
        if (isConnected) {
            Intent intent = new Intent(this, LogbookActivity.class);
            finish();
            startActivity(intent);
        } else {
            LoginActivity.Toaster(
                    MainActivity.this,
                    "Deze actie kan niet worden uitgevoerd omdat je geen internet verbinding hebt.");
        }
    }
}
