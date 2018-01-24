package e.natasja.sugar_kidz;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Timer;
import java.util.TimerTask;

public class CoupleActivity extends AppCompatActivity {
    FirebaseAuth mAuth;
    DatabaseReference mDatabaseRef;

    LinearLayout usernameLayout;
    LinearLayout codeLayout;
    LinearLayout usernameFillLayout;
    LinearLayout codeFillLayout;

    TextView generatedCodeTextView;

    public String userID;

    Boolean isParent;

    private static final String TAG = "CoupleActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_couple);

        usernameLayout = findViewById(R.id.username);
        codeLayout = findViewById(R.id.code);
        usernameFillLayout = findViewById(R.id.fillUsername);
        codeFillLayout = findViewById(R.id.fillCode);

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() == null) {
            Intent notLoggedIn = new Intent(this, LoginActivity.class);
            finish();
            startActivity(notLoggedIn);
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

                    final String username = (String) dataSnapshot.child("username").getValue();

                    if (isParent) {
                        setParentUI();
                    } else {
                        setKidUI(username);
                    }

                }
                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value
                    Log.w(TAG, "Failed to read value.", error.toException());
                }
            });
        }
    }

    public void setParentUI() {
        usernameFillLayout.setVisibility(View.VISIBLE);
        codeFillLayout.setVisibility(View.VISIBLE);
    }

    public void setKidUI(String username) {
        usernameLayout.setVisibility(View.VISIBLE);
        codeLayout.setVisibility(View.VISIBLE);

        TextView usernameTextView = findViewById(R.id.usernameTextView);
        usernameTextView.setText(username);
    }

    public void generateCode(View view) {
        if (!isParent) {
            final String code = CodeGenerator.nextSessionId();
            generatedCodeTextView = findViewById(R.id.generatedCode);

            generatedCodeTextView.setText(code);
            mDatabaseRef.child("couple code").setValue(code);

            RepeatTask();
        }
    }

    private void RepeatTask() {

        final Handler handler = new Handler();
        Timer timer = new Timer();
        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            //your method here
                            mDatabaseRef.child("couple code").setValue(null);
                        } catch (Exception e) {
                            Log.w(TAG, "Couldn't reset code");
                        }
                    }
                });
            }
        };

        // reset the code every 10 minutes
        timer.schedule(doAsynchronousTask, 150000, 150000);
    }

    public void goBackFromCouple(View view) {
        if (!isParent) {
            Intent intent = new Intent(this, MainActivity.class);
            finish();
            startActivity(intent);
        } else {
            Intent intent2 = new Intent(this, LogbookActivity.class);
            finish();
            startActivity(intent2);
        }
    }
}
