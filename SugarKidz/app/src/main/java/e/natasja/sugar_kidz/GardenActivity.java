package e.natasja.sugar_kidz;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class GardenActivity extends AppCompatActivity implements ConnectionInterface {
    private static final String TAG = "GardenActivity.java";

    FirebaseDatabase mDatabase;
    DatabaseReference mRef;
    String uid;

    ImageView pokemon1, pokemon2, pokemon3, pokemon4, pokemon5;
    ImageView background1, background2, background3, background4, background5;
    int currentPokemon = 0;

    public static MainActivity delegate = null;

    ArrayList<Integer> ownedPokemon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_garden);

        FirebaseUser aUser = FirebaseAuth.getInstance().getCurrentUser();

        MainActivity.delegate = this;

        // check if user is logged in, you can come here only as a kid not as a parent
        // so we don't have to check that
        if (aUser == null) {
            Intent unauthorized = new Intent (this, LoginActivity.class);
            finish();
            startActivity(unauthorized);
        } else {
            uid = aUser.getUid();
            mDatabase = FirebaseDatabase.getInstance();
            LoginActivity.Toaster(GardenActivity.this, "Klik op de pokemons om ze te veranderen!");

            // hide the navigation and notification bars
            setUIOptions();

            // check which pokemons you've buyed
            findOwnedPokemon();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUIOptions();
    }

    @Override
    public void closeActivity() {
        // when the internet connection is lost, finish this activity
        finish();
    }

    /**
     * This function hides the navigation and notification bars in this activity. With the Sticky
     * Flag there is made sure that the notification bar stays away, but you can swipe down and
     * see it, but it will hide again.
     */
    public void setUIOptions() {
        View decorView = getWindow().getDecorView();

        int uiOptions =   View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

    /**
     * Load the Pokemon that where displayed the previous time the user left the garden. This can be
     * none if you've never been to the garden yet or if you don't have any Pokemon yet.
     */
    public void loadPokemons() {
        pokemon1 = findViewById(R.id.pokemon1);
        pokemon2 = findViewById(R.id.pokemon2);
        pokemon3 = findViewById(R.id.pokemon3);
        pokemon4 = findViewById(R.id.pokemon4);
        pokemon5 = findViewById(R.id.pokemon5);

        // set displayed pokemons to imageviews
        mRef = mDatabase.getReference("users/" + uid + "/displayedPokemons");
        mRef.addListenerForSingleValueEvent(displayedPokemonsListener);
    }

    /**
     * This Value Event Listener checks what pokemons the user has already displayed the previous
     * time they where in the garden (this can be none, the first time you enter the garden).
     * It then loads the sprites of these pokemons to the pokemon views.
     */
    ValueEventListener displayedPokemonsListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {

            for (DataSnapshot display : dataSnapshot.getChildren()) {
                int whatImageView = Integer.valueOf(display.getKey());
                int pokemonToDisplay = Integer.valueOf(String.valueOf(display.getValue()));

                // set the right pokemon to the right imageview
                if (whatImageView == 1) {
                    findSprite(pokemonToDisplay, pokemon1);
                } else if (whatImageView == 2) {
                    findSprite(pokemonToDisplay, pokemon2);
                } else if (whatImageView == 3) {
                    findSprite(pokemonToDisplay, pokemon3);
                } else if (whatImageView == 4) {
                    findSprite(pokemonToDisplay, pokemon4);
                } else {
                    findSprite(pokemonToDisplay, pokemon5);
                }
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.w(TAG, "Failed to read data from database." + databaseError.toException());
        }
    };

    /**
     * This function finds the backgrounds of the pokemons, and makes them clickable. It also sets
     * the animations of the pokemons.
     */
    public void addClickListener() {
        // add the onclicklistener to background: because of the animation, the onclick listener
        // doesn't work (good) at the pokemon imageview
        background1 = findViewById(R.id.background1);
        background2 = findViewById(R.id.background2);
        background3 = findViewById(R.id.background3);
        background4 = findViewById(R.id.background4);
        background5 = findViewById(R.id.background5);

        // make the backgrounds clickable and give the function what pokemon to change
        clickOn(background1, pokemon1, 1);
        clickOn(background2, pokemon2, 2);
        clickOn(background3, pokemon3, 3);
        clickOn(background4, pokemon4, 4);
        clickOn(background5, pokemon5, 5);

        // give all the pokemons their own animation
        pokemon1.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.flying1));
        pokemon2.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.flying2));
        pokemon3.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.walking));
        pokemon4.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.walking2));
        pokemon5.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.walking3));
    }

    /**
     * This is a function that implements an onClickListener on the backgrounds of the pokemons. If
     * you've clicked on a background, it changes the pokemon to display (if you have more than 1
     * pokemon.)
     */
    public void clickOn(final ImageView background, final ImageView pokemon, final int whatPokemon) {
        background.setClickable(true);

        View.OnClickListener changePokemonOnClick = new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // because you increase current pokemon, it can become bigger than the size of the
                // array, that will cause an error so we need to use modulo
                if (currentPokemon >= ownedPokemon.size()) {
                    currentPokemon = currentPokemon % ownedPokemon.size();
                }

                // get the pokemonToDisplay now from the Array of your owned pokemons
                int pokemonToDisplay = ownedPokemon.get(currentPokemon);
                currentPokemon += 1;

                // find sprite that belongs to this pokemon and display it
                findSprite(pokemonToDisplay, pokemon);

                // save the new displayed pokemon in firebase
                mRef = FirebaseDatabase.getInstance().getReference("users/" + uid);
                mRef.child("displayedPokemons").child(String.valueOf(whatPokemon)).setValue(pokemonToDisplay);
            }
        };

        background.setOnClickListener(changePokemonOnClick);
    }

    /**
     * This function finds the sprite of the pokemonNumber that we have to search for in FireBase.
     * It sets this sprite to the imageView.
     */
    public void findSprite(final Integer findSpritePokemon, final ImageView pokemon) {

        mRef = mDatabase.getReference("pokemons/" + findSpritePokemon);

        ValueEventListener findSpriteListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String toDecode = String.valueOf(dataSnapshot.child("sprite").getValue());

                // the sprite is saved as a string in firebase, so we need to decode it
                Bitmap decoded = PokeshopAdapter.getBitmap(toDecode);

                // set the pokemon to the imageview
                pokemon.setBackgroundColor(getResources().getColor(R.color.transparent));
                pokemon.setImageBitmap(decoded);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "Failed to read value.");
            }
        };

        mRef.addListenerForSingleValueEvent(findSpriteListener);
    }

    /**
     * This function finds the pokemons a user owns and puts them in an array.
     */
    public void findOwnedPokemon() {
        // load pokemons from previous session
        loadPokemons();

        // find owned pokemons so the user will be able to change their pokemons to other ones they own
        mRef = mDatabase.getReference("users/" + uid + "/Pokemons");
        ownedPokemon = new ArrayList<>();

        mRef.addListenerForSingleValueEvent(findOwnedPokemonListener);

        // make the views clickable to make you able to change pokemons
        addClickListener();
    }

    /**
     * ValueEventListener that searches the FireBase for the Pokemon the user has bought.
     */
    ValueEventListener findOwnedPokemonListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {

            // save the integers pokemonNumbers in the FireBase
            for (DataSnapshot pokemonNumber : dataSnapshot.getChildren()) {
                Integer pokemonNumberValue = Integer.valueOf(pokemonNumber.getKey());
                ownedPokemon.add(pokemonNumberValue);
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.w(TAG, "Failed to read data.");
        }
    };

    /**
     * This is the function that sends you back to the Main screen.
     */
    public void goBack(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        finish();
        startActivity(intent);
    }

}
