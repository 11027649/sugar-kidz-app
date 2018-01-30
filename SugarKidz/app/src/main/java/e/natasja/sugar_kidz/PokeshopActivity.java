package e.natasja.sugar_kidz;

import android.content.Context;
import android.content.Intent;

import android.graphics.Bitmap;

import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;

import android.text.Html;

import android.util.Base64;
import android.util.Log;

import android.view.MenuItem;

import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HttpStack;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Iterator;

public class PokeshopActivity extends AppCompatActivity {

    RequestQueue serialRequestQueue;
    private static String TAG = "PokeshopActivity";

    ArrayList<Pokemon> pokemons;
    ArrayList<Integer> ownedPokemons;

    String uid;
    FirebaseAuth mAuth;
    DatabaseReference mRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pokeshop);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser aUser = mAuth.getCurrentUser();

        if (aUser == null) {
            Intent intent = new Intent(PokeshopActivity.this, MainActivity.class);
            finish();
            startActivity(intent);
        } else {
            uid = aUser.getUid();

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);

            pokemons = new ArrayList<>();
            ownedPokemons = new ArrayList<>();

            showXPAMount();
            showPokemons();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(PokeshopActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void showPokemons() {
        mRef = FirebaseDatabase.getInstance().getReference("pokemons");

        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterator<DataSnapshot> mIterator = dataSnapshot.getChildren().iterator();

                pokemons.clear();

                while (mIterator.hasNext()) {
                    DataSnapshot datasnap = mIterator.next();
                    Pokemon pokemon = datasnap.getValue(Pokemon.class);
                    pokemons.add(pokemon);

                    checkIfOwned();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "Not able to load pokemons from Firebase");
            }
        });
    }

    private void checkIfOwned() {
        mRef = FirebaseDatabase.getInstance().getReference("users/" + uid + "/Pokemons/");
        mRef.addValueEventListener(isOwnedListener);
    }

    private ValueEventListener isOwnedListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Iterator<DataSnapshot> mIterator = dataSnapshot.getChildren().iterator();

            while (mIterator.hasNext()) {
                DataSnapshot pokemonNumber = mIterator.next();

                Integer pokemonNumberValue = Integer.valueOf(pokemonNumber.getKey());
                ownedPokemons.add(pokemonNumberValue);
            }

            updateList();
        }
        @Override
        public void onCancelled(DatabaseError error) {
            // Failed to read value
            Log.w(TAG, "Failed to read value.", error.toException());
        }
    };

    public void showXPAMount() {
        TextView XPTextView = findViewById(R.id.XP);
        final TextView XPAmountTextView = findViewById(R.id.xpAmount);

        String text =   "<font color='red'>X</font>" +
                "<font color='orange'>P</font>" + " " +
                "<font color='yellow'>A</font>" +
                "<font color='green'>m</font>" +
                "<font color='blue'>o</font>" +
                "<font color='indigo'>u</font>" +
                "<font color='violet'>n</font>" +
                "<font color='red'>t</font>" + ": ";

        XPTextView.setText(Html.fromHtml(text), TextView.BufferType.SPANNABLE);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String userID = mAuth.getCurrentUser().getUid();
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("users/" + userID + "/xpAmount");

        // Read from the database
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                Long xpAmount = (Long) dataSnapshot.getValue();

                Log.d(TAG, "Value is: " + xpAmount);
                XPAmountTextView.setText(String.valueOf(xpAmount));
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    public void updateList() {
        PokeshopAdapter adapter = new PokeshopAdapter(getApplicationContext(), pokemons, ownedPokemons);
        ListView pokemonlist = findViewById(R.id.pokemonListView);

        pokemonlist.setAdapter(adapter);
    }
}
