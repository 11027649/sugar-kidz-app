package e.natasja.sugar_kidz;

import android.content.Context;
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
    private DatabaseReference mRef;
    private String uid;

    Boolean isParent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        // call this method immediately to check if the user is already logged in
        checkIfParent();
    }

    /**
     * This method checks if the user is a parent, by adding the isParentListener to de database.
     * This Listener will send the user to the right UI.
     */
    public void checkIfParent() {
        // check if user is signed in (non-null) and update UI accordingly
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            uid = currentUser.getUid();
        }

        mRef = FirebaseDatabase.getInstance().getReference("users/"  + uid);
        mRef.addListenerForSingleValueEvent(isParentListener);
    }

    /**
     * This is the OnClick listener from the login button. It checks the users email and password
     * match and if the user is a parent, and sends you to the right start screen..
     */
    public void login(View view) {
        EditText emailEditText = findViewById(R.id.email);
        EditText passwordEditText = findViewById(R.id.password);

        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        if (email.equals("")) {
            Toaster(LoginActivity.this, "Vul alsjeblieft je emailadres in.");
        } else if (password.equals("")) {
            Toaster(LoginActivity.this, "Vul alsjeblieft je wachtwoord in.");
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            checkIfParent();
                        } else {
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toaster(LoginActivity.this, "De combinatie van email en " +
                                    "wachtwoord komt niet overeen, of je moet nog een account aanmaken.");
                        }
                    }
                });
    }

    /**
     * This is a Toaster method that can be used in the whole app.
     */
    public static void Toaster(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * This EventListener checks once if the user is a parent or not, and adjusts the boolean
     * isParent value.
     */
    ValueEventListener isParentListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            // this method is called once with the initial value
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
            // failed to read value
            Log.w(TAG, "Failed to read value.", error.toException());
        }
    };

    /**
     * This is the OnClick method of the "don't have an account yet?" button.
     */
    public void toRegisterActivity(View view) {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

}
