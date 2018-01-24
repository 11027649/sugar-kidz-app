package e.natasja.sugar_kidz;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Natasja on 10-1-2018.
 */

public class PokelistAdapter extends ArrayAdapter {
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

        Button buy = (Button) row.findViewById(R.id.buy);
        buy.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //...
            }
        });

        ImageView pokemonImage = view.findViewById(R.id.imageviewPokemon);
        TextView pokemonName = view.findViewById(R.id.name);
        TextView pokemonPrice = view.findViewById(R.id.cost);

        pokemonPrice.setTextColor(Color.BLACK);
        pokemonName.setTextColor(Color.BLACK);

        pokemonPrice.setText(pokemon.price);

        String pokemonNameText = "Hey, ik ben een " + pokemon.name + "!";
        pokemonName.setText(pokemonNameText);

        Bitmap pokemonSprite = getBitmap(pokemon.sprite);

        pokemonImage.setImageBitmap(pokemonSprite);

        return view;
    }

    public Bitmap getBitmap(String photo) {

        byte[] decodedString = Base64.decode(photo, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

        return decodedByte;

    }
}
