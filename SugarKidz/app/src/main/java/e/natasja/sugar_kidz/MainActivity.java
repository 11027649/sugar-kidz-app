package e.natasja.sugar_kidz;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    TextView date;
    TextView time;
    SimpleDateFormat dateSDF;
    Calendar myCalendar;

    private GestureDetectorCompat gestureObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gestureObject = new GestureDetectorCompat(this, new LearnGesture());
        setDate();
    }

    public void setDate() {
        date = findViewById(R.id.date);
        time = findViewById(R.id.time);

        long datetime = System.currentTimeMillis();
        dateSDF = new SimpleDateFormat("dd-MM-yy");
        SimpleDateFormat timeSDF = new SimpleDateFormat("hh:mm");

        String dateToday = dateSDF.format(datetime);
        String timeToday = timeSDF.format(datetime);

        date.setText(dateToday);
        time.setText(timeToday);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.gestureObject.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    public void logout(View view) {
        FirebaseAuth.getInstance().signOut();
        Intent logout = new Intent(this, LoginActivity.class);
        finish();
        startActivity(logout);
    }

    public void datePicker(View view) {
        myCalendar = Calendar.getInstance();

        new DatePickerDialog(MainActivity.this, datePickerDialog,
                myCalendar.get(Calendar.YEAR),
                myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)).show();

    }

    DatePickerDialog.OnDateSetListener datePickerDialog = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker datePicker, int year, int month, int day) {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, month);
            myCalendar.set(Calendar.DAY_OF_MONTH, day);

            date.setText(dateSDF.format(myCalendar.getTime()));
        }
    };

    class LearnGesture extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2,
                               float velocityX, float velocityY) {

            if (event2.getX() > event1.getX()) {
                // this will listen to swipes from left to right
                Intent intent = new Intent(MainActivity.this, PokeshopActivity.class);
                finish();
                startActivity(intent);
            } else {
                // same in opposite direction
            }

            return true;
        }
    }

}
