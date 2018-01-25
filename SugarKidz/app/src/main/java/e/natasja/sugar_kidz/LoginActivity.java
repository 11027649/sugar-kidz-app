package e.natasja.sugar_kidz;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    private FirebaseAuth mAuth;

    Boolean isParent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onStart() {
        super.onStart();

        //check if user is signed in (non-null) and update UI accordingly
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String uid = currentUser.getUid();
            DatabaseReference mDatabaseRef = FirebaseDatabase.getInstance().getReference("users/"  + uid);

            mDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.
                    isParent = (boolean) dataSnapshot.child("isParent").getValue();
                    Log.d(TAG, "Value is: " + isParent);

                    if (isParent) {
                        Intent toParentUI = new Intent(LoginActivity.this, LogbookActivity.class);
                        finish();
                        startActivity(toParentUI);
                    } else {
                        Intent toKidUI = new Intent(LoginActivity.this, MainActivity.class);
                        finish();
                        startActivity(toKidUI);
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

    public void checkIfParent() {
        String userID = mAuth.getCurrentUser().getUid();
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("users/" + userID);

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                isParent = (boolean) dataSnapshot.child("isParent").getValue();
                Log.d(TAG, "Value is: " + isParent);

                // sign in succes, update UI with the signed-in user's information
                if (isParent) {
                    Intent intent = new Intent(getApplicationContext(), LogbookActivity.class);
                    finish();
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    finish();
                    startActivity(intent);
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    public void login(View view) {
        EditText emailEditText = findViewById(R.id.email);
        EditText passwordEditText = findViewById(R.id.password);

        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        if (email.equals("")) {
            Toast.makeText(LoginActivity.this, "Please fill in your email adress",
                    Toast.LENGTH_SHORT).show();
        }
        if (password.equals("")) {
            Toast.makeText(LoginActivity.this, "Please fill in your password",
                    Toast.LENGTH_SHORT).show();
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            checkIfParent();
                        } else {
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Email and password don't " +
                                    "match, or account doesn't exist yet.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void toRegisterActivity(View view) {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }
}
