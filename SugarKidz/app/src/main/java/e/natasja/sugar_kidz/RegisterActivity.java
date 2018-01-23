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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mRef;

    private static final String TAG = "RegisterActivity";

    private String email;
    private String password;
    private String verifyPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mRef = mDatabase.getReference();

    }

    @Override
    public void onStart() {
        super.onStart();

        // check if user is signed in (non-null) and update UI accordingly
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            // if already logged in, go to main menu
            Log.d(TAG, currentUser.getUid());
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }

    }

    /**
     * Log in existing users, or create account for users that want to register.
     */
    public void register(View view) {
        EditText emailEditText = findViewById(R.id.email);
        EditText passwordEditText = findViewById(R.id.password);
        EditText verifyPasswordEditText = findViewById(R.id.verifyPassword);

        email = emailEditText.getText().toString();
        password = passwordEditText.getText().toString();

        // check if password length is long enough for FireBase
        if (password.length() < 7) {
            Toast.makeText(this, "Password must 7 characters or longer.",
                    Toast.LENGTH_SHORT).show();
        } else {
            verifyPassword = verifyPasswordEditText.getText().toString();

            if (verifyPassword.equals(password)) {
                createAccount();

            } else {
                Toast.makeText(RegisterActivity.this, "Passwords are not equal",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void createAccount() {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");

                            EditText usernameEditText = findViewById(R.id.username);
                            String username = usernameEditText.getText().toString();

                            // make a new user and find user id
                            FirebaseUser currentUser = mAuth.getCurrentUser();
                            String userId = currentUser.getUid();

                            // check if new user is a parent
                            CheckBox parent = findViewById(R.id.parentCheckbox);
                            User aUser;

                            if (parent.isChecked()) {
                                aUser = new User(username, true,null);
                                mRef.child("users").child(userId).setValue(aUser);

                                Intent intent = new Intent(getApplicationContext(), LogbookActivity.class);
                                finish();
                                startActivity(intent);
                            } else {
                                aUser = new User(username, false,1000);
                                mRef.child("users").child(userId).setValue(aUser);

                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                finish();
                                startActivity(intent);
                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(RegisterActivity.this, "Registration failed, " +
                                            "please pick a valid email and password.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

}

