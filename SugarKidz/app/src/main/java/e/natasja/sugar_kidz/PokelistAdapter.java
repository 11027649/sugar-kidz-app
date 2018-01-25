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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Created by Natasja on 10-1-2018.
 */

public class PokelistAdapter extends ArrayAdapter {
    Button buy;
    int pokemonNumber;
    TextView pokemonPrice;

    String uid;

    public PokelistAdapter(Context context, ArrayList<Pokemon> pokemons) {
        super(context, 0, pokemons);
    }

    @NonNull
    @Override
    public View getView(int position, View view, @NonNull ViewGroup parent) {

        Pokemon pokemon = (Pokemon) getItem(position);

        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.row_shoplist, parent, false);
        }

        ImageView pokemonImage = view.findViewById(R.id.imageviewPokemon);
        TextView pokemonName = view.findViewById(R.id.name);
        pokemonPrice = view.findViewById(R.id.cost);

        pokemonPrice.setTextColor(Color.BLACK);
        pokemonName.setTextColor(Color.BLACK);

        pokemonPrice.setText(pokemon.price);

        String pokemonNameText = "Hey, ik ben een " + pokemon.name + "!";
        pokemonName.setText(pokemonNameText);

        Bitmap pokemonSprite = getBitmap(pokemon.sprite);
        pokemonImage.setImageBitmap(pokemonSprite);

        // the pokemons start at 1, and position at 0, so the clicked pokemon's number is position + 1
        pokemonNumber = position + 1;

        buy = view.findViewById(R.id.buy);
        buy.setText("Koop mij!");
        buy.setOnClickListener(buyListener);

        return view;
    }

    private View.OnClickListener buyListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            buy.setText("Gekocht");

            addPokemon(String.valueOf(pokemonNumber));
            payForPokemon(pokemonPrice.getText().toString());
        }
    };

    private void payForPokemon(String price) {
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("users/" + uid);

        mRef.addListenerForSingleValueEvent(payListener);

    }

    private ValueEventListener payListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            String xpAmount = (dataSnapshot.child("xpAmount").getValue().toString());
            int XP = Integer.valueOf(xpAmount);

            if (XP > 1000) {
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
    };



    public void addPokemon(String position) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String uid = mAuth.getCurrentUser().getUid();
        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("users/" + uid);

        mRef.child("Pokemons").child(position).setValue(true);
    }

    public static Bitmap getBitmap(String photo) {
        byte[] decodedString = Base64.decode(photo, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
    }
}
