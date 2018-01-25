package e.natasja.sugar_kidz;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;

public class MyGardenActivity extends AppCompatActivity {
    ImageView pokemon1, pokemon2, pokemon3, pokemon4, pokemon5;
    int currentPokemon = 0;

    private static final String TAG = "GardenActivity.java";
    FirebaseAuth mAuth;
    FirebaseDatabase mDatabase;
    DatabaseReference mRef;
    FirebaseUser user;

    String uid;

    ArrayList<Integer> ownedPokemon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_garden);
        setUIOptions();

        mAuth = FirebaseAuth.getInstance();

        user = mAuth.getCurrentUser();

        if (user != null) {
            uid = user.getUid();
        }

        mDatabase = FirebaseDatabase.getInstance();

        findOwnedPokemon();
        findViewsAndMakeClickable();

    }

    public void findViewsAndMakeClickable() {
        pokemon1 = findViewById(R.id.pokemon1);
        pokemon2 = findViewById(R.id.pokemon2);
        pokemon3 = findViewById(R.id.pokemon3);
        pokemon4 = findViewById(R.id.pokemon4);
        pokemon5 = findViewById(R.id.pokemon5);

        clickOn(pokemon1);
        clickOn(pokemon2);
        clickOn(pokemon3);
        clickOn(pokemon4);
        clickOn(pokemon5);

        pokemon2.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.flying));
    }

    public void clickOn(final ImageView pokemon) {
        pokemon.setClickable(true);
        pokemon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                Log.d(TAG, "current after click" + String.valueOf(currentPokemon));
                currentPokemon = currentPokemon + 1;

                if (currentPokemon >= ownedPokemon.size()) {
                    currentPokemon = currentPokemon % ownedPokemon.size();
                }

                Log.d(TAG, String.valueOf(ownedPokemon.get(currentPokemon)));

                findSprite(ownedPokemon.get(currentPokemon), pokemon);
            }
        });
    }

    public void findSprite(final Integer findSpritePokemon, final ImageView pokemon) {

        mRef = mDatabase.getReference("pokemons/" + findSpritePokemon);

        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                    String toDecode = dataSnapshot.child("sprite").getValue().toString();
                    Bitmap decoded = PokelistAdapter.getBitmap(toDecode);

                    // search light.png
                    pokemon.setBackgroundColor(getResources().getColor(R.color.transparent));
                    pokemon.setImageBitmap(decoded);
                }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "Failed to read value.");
            }
        });
    }

    public void findOwnedPokemon() {
        mRef = mDatabase.getReference("users/" + uid + "/Pokemons");
        ownedPokemon = new ArrayList<>();

        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterator<DataSnapshot> mIterator = dataSnapshot.getChildren().iterator();

                while (mIterator.hasNext()) {
                    DataSnapshot pokemonNumber = mIterator.next();

                    Integer pokemonNumberValue = Integer.valueOf(pokemonNumber.getKey());
                    ownedPokemon.add(pokemonNumberValue);
                    Log.d(TAG, Integer.toString(ownedPokemon.size()));
                    Log.d(TAG, ownedPokemon.toString());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "Failed to read data.");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUIOptions();
    }

    public void setUIOptions() {
        View decorView = getWindow().getDecorView();

        // Hide both the navigation bar and the status bar.
        // SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
        // a general rule, you should design your app to hide the status bar whenever you
        // hide the navigation bar.
        int uiOptions =   View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

    public void goBack(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        finish();
        startActivity(intent);
    }

}
