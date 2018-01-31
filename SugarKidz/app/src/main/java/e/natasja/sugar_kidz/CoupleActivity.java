package e.natasja.sugar_kidz;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

public class CoupleActivity extends AppCompatActivity implements ConnectionInterface {
    private static final String TAG = "CoupleActivity";

    FirebaseAuth mAuth;
    DatabaseReference mRef;

    LinearLayout usernameLayout;
    LinearLayout codeLayout;
    LinearLayout usernameFillLayout;
    LinearLayout codeFillLayout;

    public static MainActivity delegate = null;

    TextView generatedCodeTextView;

    private String uid;
    private String kidID;

    String usernameToSearch;

    Boolean isParent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_couple);

        mAuth = FirebaseAuth.getInstance();

        MainActivity.delegate = this;

        // check if the user is logged in and if it's a parent or not
        if (mAuth.getCurrentUser() == null) {
            Intent notLoggedIn = new Intent(this, LoginActivity.class);
            finish();
            startActivity(notLoggedIn);
        } else {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);

            usernameLayout = findViewById(R.id.username);
            codeLayout = findViewById(R.id.code);
            usernameFillLayout = findViewById(R.id.fillUsername);
            codeFillLayout = findViewById(R.id.fillCode);

            uid = mAuth.getCurrentUser().getUid();
            mRef = FirebaseDatabase.getInstance().getReference("users/" + uid);
            mRef.addValueEventListener(isParentListener);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // if the back button is pressed, send the user back to the right screen
                if (!isParent) {
                    Intent intent = new Intent(this, MainActivity.class);
                    finish();
                    startActivity(intent);
                } else {
                    Intent intent2 = new Intent(this, LogbookActivity.class);
                    finish();
                    startActivity(intent2);
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void closeActivity() {
        // when the internet connection is lost, finish this activity
        finish();
    }

    /**
     * This ValueEventListener checks and saves if the user is a parent or not. It also sets
     * the UI for the corresponding user.
     */
    ValueEventListener isParentListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            isParent = (boolean) dataSnapshot.child("isParent").getValue();

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
    };

    /**
     * Sets the parent UI.
     */
    public void setParentUI() {
        usernameFillLayout.setVisibility(View.VISIBLE);
    }

    /**
     * Sets the kid UI.
     */
    public void setKidUI(String username) {
        usernameLayout.setVisibility(View.VISIBLE);
        codeLayout.setVisibility(View.VISIBLE);

        TextView usernameTextView = findViewById(R.id.usernameTextView);
        usernameTextView.setText(username);
    }

    /**
     * Let the kid generate a safe code that will be used as a sort of password to couple with their
     * parent. The code will expire after 10 minutes, because of the RepeatTask function.
     */
    public void generateCode(View view) {
        if (!isParent) {
            final String code = CodeGenerator.nextSessionId();
            generatedCodeTextView = findViewById(R.id.generatedCode);

            // show the code to the user and safe it to their FireBase
            generatedCodeTextView.setText(code);
            mRef.child("coupleCode").setValue(code);

            // call this method to reset the couple code
            RepeatTask();
        }
    }

    /**
     * This Task resets the code every ten minutes, after being called for the first time.
     */
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
                            mRef.child("couple code").setValue(null);
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

    /**
     * This method let the parent search to their kid by typing in their username. If the name is
     * found, the edit text where you can fill in the code appears and the parent has to fill in the
     * code they have to ask their kid.
     */
    public void searchToUser(View view) {
        final EditText usernameEditText = findViewById(R.id.usernameEditText);
        usernameToSearch = usernameEditText.getText().toString();

        // use a listener to find the username
        mRef = FirebaseDatabase.getInstance().getReference("users/");
        mRef.addListenerForSingleValueEvent(searchUsernameListener);
    }

    /**
     * This ValueEventListener allows the parent to search through the database to find their kids
     * name. If it's found, you can continue with coupling your account.
     */
    ValueEventListener searchUsernameListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Boolean foundKid = false;

            for (DataSnapshot user : dataSnapshot.getChildren()) {
                kidID = user.getKey();
                String usernameSearch = String.valueOf(user.child("username").getValue());

                // if the user is found, break from the while loop
                if (usernameSearch.equals(usernameToSearch)) {
                    LoginActivity.Toaster(CoupleActivity.this, "Gebruiker gevonden!");
                    foundKid = true;
                    codeFillLayout.setVisibility(View.VISIBLE);
                    break;
                }
            }
            if (!foundKid) {
                LoginActivity.Toaster(CoupleActivity.this, "Gebruiker niet gevonden!");
            }
        }
        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.w(TAG, "Failed to read data.");
        }
    };

    /**
     * This is the onClickListener from the coupleButton. If the parent has filled in the code,
     * checks if it is equal to the code the kid has generated by calling the checkCoupleCode function.
     */
    public void couple(View view) {
        if (isParent) {
            EditText codeEditText = findViewById(R.id.codeEditText);
            String code = codeEditText.getText().toString();

            if (code.length() == 0) {
                LoginActivity.Toaster(CoupleActivity.this, "Vraag de koppelcode aan je kind.");
            } else if (code.length() < 8 || code.length() > 8) {
                LoginActivity.Toaster(CoupleActivity.this, "De code moet 8 cijfers zijn.");
            } else {
                checkCoupleCode(code);
            }
        }
    }

    /**
     * Check the couple code by searching in the firebase to the kidID and save and compare
     * the couple code that the kid generated to the one that the parent filled in.
     */
    public void checkCoupleCode(final String code) {
        mRef = FirebaseDatabase.getInstance().getReference("users/" + kidID + "/coupleCode");

        ValueEventListener codeCheckListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String codeToCheckWith = String.valueOf(dataSnapshot.getValue());

                if (code.equals(codeToCheckWith) && (!codeToCheckWith.equals("null"))) {
                    LoginActivity.Toaster(
                            CoupleActivity.this,
                            "Je hebt je account succesvol gekoppeld!");

                    mRef = FirebaseDatabase.getInstance().getReference("users/" + uid + "/coupled");
                    mRef.child("kidID").setValue(kidID);
                    mRef.child("kidName").setValue(usernameToSearch);
                } else {
                    LoginActivity.Toaster(
                            CoupleActivity.this,
                            "De code klopt niet of kan niet worden gevonden.");
                }

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "Failed to read value from database.");
            }
        };
        mRef.addListenerForSingleValueEvent(codeCheckListener);
    }

}
