package e.natasja.sugar_kidz;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Created by Natasja on 10-1-2018.
 * This is a ListAdapter that loads pokemons and their price into a listview, with a button
 * that makes sure you can buy the pokemon.
 */
public class PokeshopAdapter extends ArrayAdapter {
    private Button buy;
    private TextView pokemonPrice;
    ImageView pokemonImage;
    TextView pokemonName;
    ArrayList<Integer> owned;

    private String uid;

    /**
     * When initiating a PokeshopAdapter, give it the context, pokemons and ownedpokemons lists.
     */
    PokeshopAdapter(Context context, ArrayList<Pokemon> pokemons, ArrayList<Integer> ownedPokemons) {
        super(context, 0, pokemons);

        // copy this list to be able to use it in the rest of the adapter
        owned = ownedPokemons;
    }

    @NonNull
    @Override
    public View getView(int position, View view, @NonNull ViewGroup parent) {

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser aUser = mAuth.getCurrentUser();

        if (aUser != null) {
            uid = aUser.getUid();
        }

        Pokemon pokemon = (Pokemon) getItem(position);

        // the pokemons start at 1, and position at 0, so the clicked pokemon's number is position + 1
        int pokemonNumberInt = position + 1;
        final String pokemonNumber = String.valueOf(pokemonNumberInt);

        // if the view is null, inflate the layout
        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.row_pokeshop, parent, false);
        }

        buy = view.findViewById(R.id.buy);

        // set button text to buyed/not buyed
        setButtonText(owned.contains(pokemonNumberInt));

        findViews(view);

        if (pokemon != null) {
            setPokemon(pokemon);
        }

        setOnClickListener(pokemonNumber, buy);

        return view;
    }

    /**
     * Set the button text.
     */
    private void setButtonText(Boolean owned) {
        if (owned) {
            String buyed = "Al gekocht";
            buy.setText(buyed);
            buy.setBackgroundResource(R.color.colorPrimary);
        } else {
            String notBuyed = "Koop mij";
            buy.setText(notBuyed);
            buy.setBackgroundResource(R.color.colorAccent);
        }
    }

    /**
     * Set the onClick listener on the buy button. Let the user pay for the pokemon and add it to
     * their FireBase.
     */
    private void setOnClickListener(final String pokemonNumber, Button buy) {
        View.OnClickListener buyClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // distract the XP
                payForPokemon(pokemonPrice.getText().toString(), pokemonNumber);
            }
        };

        buy.setOnClickListener(buyClickListener);
    }

    /**
     * Load the Pokemons into the ListView.
     */
    private void setPokemon(Pokemon pokemon) {
        pokemonPrice.setText(pokemon.price);

        String pokemonNameText = "Hey, ik ben een " + pokemon.name + "!";
        pokemonName.setText(pokemonNameText);

        Bitmap pokemonSprite = getBitmap(pokemon.sprite);
        pokemonImage.setImageBitmap(pokemonSprite);
    }

    /**
     * Find the views in the custom ListView and set the TextColors.
     */
    private void findViews(View view) {
        pokemonImage = view.findViewById(R.id.imageviewPokemon);
        pokemonName = view.findViewById(R.id.name);
        pokemonPrice = view.findViewById(R.id.cost);

        pokemonPrice.setTextColor(Color.BLACK);
        pokemonName.setTextColor(Color.BLACK);
    }

    /**
     * Add a ValueEventListener to FireBase to get the current XP, and than distract the price (if
     * the user has enough XP to buy this pokemon.)
     */
    private void payForPokemon(String price, final String pokemonNumber) {
        ValueEventListener payListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String xpAmount = (String.valueOf(dataSnapshot.child("xpAmount").getValue()));
                int XP = Integer.valueOf(xpAmount);

                String ownedOrNot = String.valueOf(dataSnapshot.child("Pokemons").child(pokemonNumber).getValue());

                if (ownedOrNot.equals("true")) {
                    LoginActivity.Toaster(getContext(), "Deze pokemon heb je al!");
                } else if (XP >= 1000) {
                    addPokemon(pokemonNumber);
                    int newXP = XP - 1000;
                    FirebaseDatabase.getInstance().getReference("users/" + uid + "/xpAmount").setValue(newXP);
                } else {
                    LoginActivity.Toaster(getContext(), "Je hebt niet genoeg XP om deze pokemon te kopen.");
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("PokeshopAdapter", "Failed to read data from database.");
            }
        };

        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("users/" + uid);
        mRef.addListenerForSingleValueEvent(payListener);
    }

    /**
     * If the pokemon has been payed, add it to the Database of this user.
     */
    private void addPokemon(String position) {
        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("users/" + uid);
        mRef.child("Pokemons").child(position).setValue(true);
    }

    /**
     * Decodes de String of a given sprite and returns a Bitmap.
     */
    static Bitmap getBitmap(String photo) {
        byte[] decodedString = Base64.decode(photo, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
    }
}
