package e.natasja.sugar_kidz;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Iterator;

public class RegisterActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private DatabaseReference mRef;

    private static final String TAG = "RegisterActivity";

    private String email;
    private String password;
    private String username;
    private String uid;

    private Boolean usernameExists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        // check if user is signed in (non-null) and update UI accordingly
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            // if this happens the user will be redirected to the login activity where there will be
            // checked if the user is a parent and will be send to the right starting screen.
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
    }

    /**
     * Create account for users that want to register.
     */
    public void register(View view) {
        EditText emailEditText = findViewById(R.id.email);
        EditText passwordEditText = findViewById(R.id.password);
        EditText verifyPasswordEditText = findViewById(R.id.verifyPassword);

        email = emailEditText.getText().toString();
        password = passwordEditText.getText().toString();

        // check if password length is long enough for FireBase
        if (password.length() < 7) {
            LoginActivity.Toaster(RegisterActivity.this, "Het wachtwoord moet 7 karakters of langer zijn.");
        } else {
            String verifyPassword = verifyPasswordEditText.getText().toString();

            if (verifyPassword.equals(password)) {
                checkIfUsernameExists();
            } else {
                LoginActivity.Toaster(RegisterActivity.this, "Wachtwoorden zijn niet gelijk");
            }
        }
    }

    public void checkIfUsernameExists() {
        EditText usernameEditText = findViewById(R.id.username);
        username = usernameEditText.getText().toString();
        usernameExists = false;

        Log.d("Registreren", "hier");

        mRef = FirebaseDatabase.getInstance().getReference("users/");
        mRef.addListenerForSingleValueEvent(usernameExistsListener);

        Log.d("Registreren", "erna");
    }

    ValueEventListener usernameExistsListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Iterator<DataSnapshot> mIterator = dataSnapshot.getChildren().iterator();
            Log.d("Registreren", "Gelijk? daar" + dataSnapshot.getChildrenCount());

            while (mIterator.hasNext()) {
                DataSnapshot user = mIterator.next();

                String userID = String.valueOf(user.getKey());
                String usernameSearch = String.valueOf(user.child(userID).child("username").getValue());

                Log.d("Registreren", "Gelijk? " + usernameSearch + " " + username);

                if (usernameSearch.equals(username)) {
                    Toast.makeText(getApplicationContext(), "Deze gebruikersnaam is al in gebruik!", Toast.LENGTH_SHORT).show();
                    usernameExists = true;
                }
            }

            if (!usernameExists) {
                // if the while loop doesn't break, create an account
                createAccount();
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.w(TAG, "Failed to read data." + databaseError.toException());
        }
    };

    public void createAccount() {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // make a new user and find user id
                            FirebaseUser currentUser = mAuth.getCurrentUser();
                            uid = currentUser.getUid();

                            // check if new user is a parent
                            CheckBox parent = findViewById(R.id.parentCheckbox);
                            User aUser;

                            if (parent.isChecked()) {
                                aUser = new User(username, true,null);
                                mRef.child("users").child(uid).setValue(aUser);
                                mRef.child("users").child(uid).child("coupled").setValue(false);

                                Intent intent = new Intent(getApplicationContext(), LogbookActivity.class);
                                finish();
                                startActivity(intent);
                            } else {
                                aUser = new User(username, false,1000);
                                mRef.child("users").child(uid).setValue(aUser);

                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                finish();
                                startActivity(intent);
                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            LoginActivity.Toaster(RegisterActivity.this,
                                    "Registreren is mislukt. Geef alsjeblieft een " +
                                            "geldig wachtwoord en email adres op.");
                        }
                    }
                });
    }

}

