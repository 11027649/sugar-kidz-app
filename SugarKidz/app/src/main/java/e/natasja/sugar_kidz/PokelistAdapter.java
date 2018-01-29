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

import static com.android.volley.VolleyLog.TAG;

/**
 * Created by Natasja on 10-1-2018.
 * This is a ListAdapter that loads pokemons and their price into a listview, with a button
 * that makes sure you can buy the pokemon.
 */
public class PokelistAdapter extends ArrayAdapter {
    private Button buy;
    private TextView pokemonPrice;
    ImageView pokemonImage;
    TextView pokemonName;
    ArrayList<Integer> owned;

    private String uid;

    PokelistAdapter(Context context, ArrayList<Pokemon> pokemons, ArrayList<Integer> ownedPokemons) {
        super(context, 0, pokemons);
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

        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.row_shoplist, parent, false);
        }

        buy = view.findViewById(R.id.buy);

        if (owned.contains(pokemonNumberInt)) {
            buy.setText("Al gekocht");
            buy.setBackgroundResource(R.color.colorPrimary);
        } else {
            buy.setText("Koop mij");
            buy.setBackgroundResource(R.color.colorAccent);
        }

        pokemonImage = view.findViewById(R.id.imageviewPokemon);
        pokemonName = view.findViewById(R.id.name);
        pokemonPrice = view.findViewById(R.id.cost);

        pokemonPrice.setTextColor(Color.BLACK);
        pokemonName.setTextColor(Color.BLACK);
        pokemonPrice.setText(pokemon.price);

        String pokemonNameText = "Hey, ik ben een " + pokemon.name + "!";
        pokemonName.setText(pokemonNameText);

        Bitmap pokemonSprite = getBitmap(pokemon.sprite);
        pokemonImage.setImageBitmap(pokemonSprite);

        buy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                payForPokemon(pokemonPrice.getText().toString(), pokemonNumber);
            }
        });


        return view;
    }

    private void payForPokemon(String price, final String pokemonNumber) {
        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("users/" + uid);

        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String xpAmount = (String.valueOf(dataSnapshot.child("xpAmount").getValue()));
                int XP = Integer.valueOf(xpAmount);

                String ownedOrNot = String.valueOf(dataSnapshot.child("Pokemons").child(pokemonNumber).getValue());

                if (ownedOrNot.equals("true")) {
                    Toast.makeText(getContext(), "Deze pokemon heb je al!", Toast.LENGTH_SHORT).show();
                } else if (XP >= 1000) {
                    addPokemon(pokemonNumber);
                    int newXP = XP - 1000;
                    FirebaseDatabase.getInstance().getReference("users/" + uid + "/xpAmount").setValue(newXP);
                } else {
                    Toast.makeText(getContext(), "Je hebt niet genoeg XP om deze pokemon te kopen.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("PokelistAdapter", "Failed to read data from database.");
            }
        });
    }

    private void addPokemon(String position) {
        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("users/" + uid);
        mRef.child("Pokemons").child(position).setValue(true);
    }

    static Bitmap getBitmap(String photo) {
        byte[] decodedString = Base64.decode(photo, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
    }
}
