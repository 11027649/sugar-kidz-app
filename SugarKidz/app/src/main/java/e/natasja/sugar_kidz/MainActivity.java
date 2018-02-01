package e.natasja.sugar_kidz;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

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

    private DatabaseReference mRef;

    public static ConnectionInterface delegate = null;
    private ConnectivityManager connectivityManager;
    private NetworkRequest.Builder builder;

    Boolean isInFront;
    Boolean isParent;
    Boolean isConnected;

    ListView myList;

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        builder = new NetworkRequest.Builder();
        isInFront = true;

        // check current status of network connection and start a connection listener
        checkConnectionOnce();
        setConnectionListener();

        // check if user is logged in
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
            populateLogbook();
            populateSpinner();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        // save isInFront state
        isInFront = true;
    }

    @Override
    public void onPause() {
        // save isInFront state
        super.onPause();
        isInFront = false;
    }

    /**
     * This method checks the connection if this Activity is opened.
     */
    private void checkConnectionOnce() {
        // call internet status check
        isConnected = isNetworkAvailable();
    }

    /**
     * Check the internet status.
     */
    private boolean isNetworkAvailable() {
        // instantiate a ConnectivityManager
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = null;

        if (connectivityManager != null) {
            // get network info
            activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        }

        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * This function listens to the Network Connection. If the user is in another activity than the
     * Main Activity when the connection is lost, this function will close that activity via an
     * Interface. The functionality of the app is disable untill you have a connection again.
     */
    private void setConnectionListener() {
        // this method sets a internet connection change listener
        connectivityManager.registerNetworkCallback(
                builder.build(),
                new ConnectivityManager.NetworkCallback() {

                    @Override
                    public void onAvailable(Network network) {
                        // when the network is available again, enable navigation
                        isConnected = true;
                    }
                    @Override
                    public void onLost(Network network) {
                        isConnected = false;

                        // network unavailable, go back to MainActivity from current activity via ConnectionInterface
                        if (!isInFront) {
                            delegate.closeActivity();
                        }
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

        // set the moments array to the adapter
        ArrayAdapter<CharSequence> momentsAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.moments,
                android.R.layout.simple_dropdown_item_1line);

        momentsAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        moments.setAdapter(momentsAdapter);
    }

    /**
     * This ValueEventListener checks if the user that is in this Activity is a parent or not.
     */
    ValueEventListener isParentListener = new ValueEventListener() {
       @Override
       public void onDataChange(DataSnapshot dataSnapshot) {
           isParent = (boolean) dataSnapshot.child("isParent").getValue();

           if (isParent) {
               Intent unauthorized = new Intent(MainActivity.this, LogbookActivity.class);
               finish();
               startActivity(unauthorized);
           }
       }
       @Override
       public void onCancelled(DatabaseError error) {
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

        // disable navigation if the connection is lost
        if (isConnected) {
            menuSwitch(id);
        } else {
            LoginActivity.Toaster(
                    MainActivity.this,
                    "Deze actie kan niet worden uitgevoerd omdat je geen internet verbinding hebt.");
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * This is the switch that belongs to the MaincActivity menu.
     */
    public void menuSwitch(int id){
        switch(id) {

            // if clicked on couple, go to couple, etc.
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
    }

    /**
     * This function gets the date and time of right now and displays it in the date and time
     * TextViews to the user. These are the default date and time that belong to a measurement.
     */
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

    /**
     * This function populates the ListView with Measurements of today. If there are no measurements
     * yet it will display a "There are no Measurements yet" in the listview.
     */
    public void populateLogbook() {
        mRef = FirebaseDatabase.getInstance().getReference("users/" + uid + "/Measurements/" + dateToday);
        myList = findViewById(R.id.listView);
        measurementArray = new ArrayList<>();

        mRef.addValueEventListener(measurementsListener);
    }

    /**
     * This ValueEventListener adds the measurements from today to a List and gives that to the
     * MainLogbookAdapter.
     */
    ValueEventListener measurementsListener = new ValueEventListener() {
        @Override
        public void onDataChange (DataSnapshot dataSnapshot) {
            measurementArray.clear();

            for (DataSnapshot measurement : dataSnapshot.getChildren()) {
                String timeMeasurement = measurement.getKey();
                SimpleMeasurement simple = measurement.getValue(SimpleMeasurement.class);

                if (simple != null) {
                    Measurement newMeasurement = new Measurement(simple.label, dateToday, timeMeasurement, simple.height);
                    measurementArray.add(newMeasurement);
                }
            }

            if (measurementArray.isEmpty()) {
                // there are no measurements yet today
                Measurement noMeasurementsYet = new Measurement("Nog geen metingen vandaag");
                measurementArray.add(noMeasurementsYet);
            } else {
                // reverse the array to show the newest measurement the highest in the listview
                Collections.reverse(measurementArray);
                setLastMeasurement();
            }

            MainLogbookAdapter mAdapter = new MainLogbookAdapter(MainActivity.this, measurementArray);
            // set long click listener on the list and add the adapter
            mAdapter.deleteOnLongClick(myList);
            myList.setAdapter(mAdapter);
        }

        @Override
        public void onCancelled(DatabaseError error) {
            Log.w(TAG, "Failed to read value.", error.toException());
        }
    };

    /**
     * This Measurement sets the last measurement from today in the MainActivity. If there's no
     * measurement from today, it'll say that there are no measurements yet.
     */
    private void setLastMeasurement() {
        TextView date = findViewById(R.id.lastMeasurementDate);
        TextView height = findViewById(R.id.lastMeasurementHeight);

        // get the first measurement from the array: this is the last one
        Measurement lastMeasurement = measurementArray.get(0);

        String dateString = lastMeasurement.dateMeasurement + ", " + lastMeasurement.timeMeasurement;
        String heightString = lastMeasurement.heightMeasurement;

        date.setText(dateString);
        height.setText(heightString);
    }

    /**
     * When the edit pencil behind the date is clicked, open a datepicker. If a date is picked,
     * display that date in the date TextView.
     */
    public void datePicker(View view) {
        myCalendar = Calendar.getInstance();

        // get the year, month and day of today
        int year = myCalendar.get(Calendar.YEAR);
        int month = myCalendar.get(Calendar.MONTH);
        int day = myCalendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog mDatePicker = new DatePickerDialog(MainActivity.this,
                new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                // set the date in the calendar to the selected day
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, month);
                myCalendar.set(Calendar.DAY_OF_MONTH, day);

                // show the picked date to the user
                date.setText(dateSDF.format(myCalendar.getTime()));
            }
        }, year, month, day);

        mDatePicker.setTitle("Kies de datum van je meting");
        mDatePicker.show();
    }

    /**
     * When the edit pencil behind the date is clicked, open a timepicker. If a time is picked,
     * display that time in the time TextView.
     */
    public void timePicker(View view) {
        myCalendar = Calendar.getInstance();

        // get the hour and minute of today, right now
        int hour = myCalendar.get(Calendar.HOUR_OF_DAY);
        int minute = myCalendar.get(Calendar.MINUTE);

        TimePickerDialog mTimePicker = new TimePickerDialog(MainActivity.this,
                new TimePickerDialog.OnTimeSetListener() {

            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                // set the time in the calendar to the selected time
                myCalendar.set(Calendar.HOUR_OF_DAY, selectedHour);
                myCalendar.set(Calendar.MINUTE, selectedMinute);

                // show the picked time to the user
                time.setText(timeSDF.format(myCalendar.getTime()));
            }
        }, hour, minute, true);

        mTimePicker.setTitle("Kies de tijd van je meting");
        mTimePicker.show();
    }

    /**
     * This function adds the Measurement the user just added to the Database.
     * It's the onclick listener of the OK button.
     */
    public void addMeasurement(View view) {

        // disable navigation if you're not connected
        if (isConnected ) {
            Measurement newMeasurement = getMeasurementInput();

            if (newMeasurement.heightMeasurement.equals("")) {
                LoginActivity.Toaster(this, "Vul de hoogte van je bloedsuiker in!");
            } else {
                mRef = FirebaseDatabase.getInstance().getReference("users/" + uid + "/Measurements");
                SimpleMeasurement simple = new SimpleMeasurement(newMeasurement.labelMeasurement, newMeasurement.heightMeasurement);

                // check if there's already a measurement at this time (to make sure a user can't
                // keep submitting at the same minute for XP)
                checkIfTimeAlreadyExists(simple, newMeasurement.timeMeasurement, newMeasurement.dateMeasurement);
            }
        } else {
            LoginActivity.Toaster(MainActivity.this,
                    "Je kan geen metingen toevoegen omdat je geen internet verbinding hebt.");
        }
    }

    /**
     * This function gets the info the user has inputted, and saves it in a Measurement.
     */
    public Measurement getMeasurementInput() {
        // find the TextViews, EditText and spinner
        TextView dateTextView = findViewById(R.id.date);
        TextView timeTextView = findViewById(R.id.time);
        Spinner labelSpinner = findViewById(R.id.labelMeasurement);
        EditText heightMeasurement = findViewById(R.id.hightMeasurement);

        // get all the Strings
        String timeMeasurement = timeTextView.getText().toString();
        String dateMeasurement = dateTextView.getText().toString();
        String label = labelSpinner.getSelectedItem().toString();
        String height = heightMeasurement.getText().toString();

        return new Measurement(label,dateMeasurement, timeMeasurement, height);
    }

    /**
     * This is a function that adds a listener to the firebase
     */
    public void checkIfTimeAlreadyExists(final SimpleMeasurement simple, final String time, final String date) {
        ValueEventListener existListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // check if there's already a measurement at this minute
                String check = String.valueOf(dataSnapshot.child(date).child(time).getValue());

                // if it's false, it doesn't exist yet
                if (check.equals("null")) {
                    addMeasurementToFirebase(simple, time, date);
                } else {
                    LoginActivity.Toaster(
                            MainActivity.this,
                            "Je hebt op dit tijdstip al een meting toegevoegd.");
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "Failed to read value");
            }
        };

        mRef.addListenerForSingleValueEvent(existListener);
    }

    /**
     * Add the Measurement to Firebase.
     */
    public void addMeasurementToFirebase(SimpleMeasurement simple, String time, String date) {
        // add the Measurement to Firebase
        // add this measurement to firebase (if there isn't a measurement yet at this time)
        mRef.child(date).child(time).setValue(simple)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "Exception occured: " + task.getException());
                            LoginActivity.Toaster(getApplicationContext(), "Iets ging fout... :(");
                        } else {
                            // if adding went fine, add 100 XP to the user's XP
                            LoginActivity.Toaster(MainActivity.this, "Je hebt deze meting toegevoegd!");

                            mRef = FirebaseDatabase.getInstance().getReference("users/" + uid);
                            gainXP();
                        }
                    }
                });
    }

    /**
     * This function searches to the amount of XP of the user in the database and adds 100 XP to it
     * when a measurement is added.
     */
    public void gainXP() {
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Long XPamount = (long) dataSnapshot.child("xpAmount").getValue();

                // if the XP is loaded, add the XP and set the value to the new xp amount
                Long XPamountNew = XPamount + 100;
                mRef.child("xpAmount").setValue(XPamountNew);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        };


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
