package e.natasja.sugar_kidz;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
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

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Iterator;
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
    public String kidID;

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

    public void searchToUser(View view) {
        final EditText usernameEditText = findViewById(R.id.usernameEditText);
        final String usernameToSearch = usernameEditText.getText().toString();

        mDatabaseRef = FirebaseDatabase.getInstance().getReference("users/");

        mDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterator<DataSnapshot> mIterator = dataSnapshot.getChildren().iterator();
                Log.d(TAG, String.valueOf(dataSnapshot.getChildrenCount()));

                Boolean foundKid = false;

                while (mIterator.hasNext()) {
                    DataSnapshot user = mIterator.next();

                    kidID = user.getKey();
                    String usernameSearch = user.child("username").getValue().toString();

                    if (usernameSearch.equals(usernameToSearch)) {
                        Toast.makeText(getApplicationContext(), "Gebruiker gevonden!" + kidID, Toast.LENGTH_SHORT).show();
                        foundKid = true;
                        codeFillLayout.setVisibility(View.VISIBLE);

                        break;
                    }
                }

                if (!foundKid) {
                    Toast.makeText(getApplicationContext(), "Gebruiker niet gevonden!", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "Failed to read data.");
            }
        });
    }

    public void couple(View view) {
        if (isParent) {
            EditText codeEditText = findViewById(R.id.codeEditText);
            String code = codeEditText.getText().toString();

            if (code.length() == 0) {
                Toast.makeText(getApplicationContext(), "Vraag de koppelcode aan je kind.", Toast.LENGTH_SHORT).show();
            } else if (code.length() < 8) {
                Toast.makeText(getApplicationContext(), "De code moet 8 cijfers zijn.", Toast.LENGTH_SHORT).show();
            } else {
                checkCoupleCode(code);
            }
        }
    }

    public void checkCoupleCode(final String code) {
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("users/" + kidID + "/couple code");

        mDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String codeToCheckWith = String.valueOf(dataSnapshot.getValue());

                if (code.equals(codeToCheckWith) && (!codeToCheckWith.equals("null"))) {
                    Toast.makeText(CoupleActivity.this, "Je hebt je account succesvol gekoppeld!", Toast.LENGTH_SHORT).show();

                    mDatabaseRef = FirebaseDatabase.getInstance().getReference("users/" + userID + "/coupled");
                    mDatabaseRef.setValue(kidID);
                } else {
                    Toast.makeText(CoupleActivity.this, "De code klopt niet of kan niet worden gevonden. ", Toast.LENGTH_SHORT).show();
                }

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "Failed to read value from database.");
            }
        });

    }
}
