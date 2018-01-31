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

public class PokeshopActivity extends AppCompatActivity implements ConnectionInterface {
    private static String TAG = "PokeshopActivity";

    FirebaseAuth mAuth;
    DatabaseReference mRef;

    String uid;

    ArrayList<Pokemon> pokemons;
    ArrayList<Integer> ownedPokemons;

    public static MainActivity delegate = null;

    TextView XPAmountTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pokeshop);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser aUser = mAuth.getCurrentUser();

        // internet connection triggers closing of all activities except MainActivity
        MainActivity.delegate = this;

        // check if user is already logged in
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
    public void closeActivity() {
        // when the internet connection is lost, finish this activity
        finish();
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

    /**
     * Load all the Pokemons from FireBase by looping through the "Pokemons" branch.
     */
    public void showPokemons() {
        mRef = FirebaseDatabase.getInstance().getReference("pokemons");

        ValueEventListener allPokemonsListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                pokemons.clear();

                for (DataSnapshot datasnap : dataSnapshot.getChildren()) {
                    Pokemon pokemon = datasnap.getValue(Pokemon.class);
                    pokemons.add(pokemon);
                }

                // loading all the pokemons is complete, now check which ones the user owns
                checkIfOwned();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "Not able to load pokemons from Firebase");
            }
        };

        mRef.addListenerForSingleValueEvent(allPokemonsListener);
    }

    /**
     * Adds a Listener to the FireBase to check if a pokemon is owned by the user.
     */
    private void checkIfOwned() {
        mRef = FirebaseDatabase.getInstance().getReference("users/" + uid + "/Pokemons/");
        mRef.addValueEventListener(isOwnedListener);
    }

    /**
     * This ValueEventListener loops through the owned pokemons in the FireBase and adds them to an
     * array.
     */
    private ValueEventListener isOwnedListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            for (DataSnapshot pokemonNumber : dataSnapshot.getChildren()) {
                Integer pokemonNumberValue = Integer.valueOf(pokemonNumber.getKey());
                ownedPokemons.add(pokemonNumberValue);
            }

            // now the lists of all pokemons and the pokemons you own are complete,
            // so you can show the list to the user now
            updateList();
        }
        @Override
        public void onCancelled(DatabaseError error) {
            // Failed to read value
            Log.w(TAG, "Failed to read value.", error.toException());
        }
    };

    /**
     * This function sets the XP Amount TextView to show how much XP the user has.
     */
    public void showXPAMount() {
        TextView XPTextView = findViewById(R.id.XP);
        XPAmountTextView = findViewById(R.id.xpAmount);

        // add nice colors using Html
        String text =   "<font color='red'>X</font>" +
                "<font color='orange'>P</font>" + " " +
                "<font color='yellow'>A</font>" +
                "<font color='green'>m</font>" +
                "<font color='blue'>o</font>" +
                "<font color='indigo'>u</font>" +
                "<font color='violet'>n</font>" +
                "<font color='red'>t</font>" + ": ";

        XPTextView.setText(Html.fromHtml(text), TextView.BufferType.SPANNABLE);

        // check how much xp you have in real time
        mRef = FirebaseDatabase.getInstance().getReference("users/" + uid + "/xpAmount");
        mRef.addValueEventListener(xpListener);
    }

    /**
     * This ValueEventLIstener checks how much XP the user has.
     */
    ValueEventListener xpListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Long xpAmount = (Long) dataSnapshot.getValue();

            Log.d(TAG, "Value is: " + xpAmount);
            XPAmountTextView.setText(String.valueOf(xpAmount));
        }
        @Override
        public void onCancelled(DatabaseError error) {
            Log.w(TAG, "Failed to read value.", error.toException());
        }
    };

    /**
     * Update the List by instantiating the Pokeshop Adapter. It needs the two lists of all the
     * Pokemons and owned Pokemons.
     */
    public void updateList() {
        PokeshopAdapter adapter = new PokeshopAdapter(getApplicationContext(), pokemons, ownedPokemons);
        ListView pokemonlist = findViewById(R.id.pokemonListView);
        pokemonlist.setAdapter(adapter);
    }
}
