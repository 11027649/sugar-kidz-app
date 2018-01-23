package e.natasja.sugar_kidz;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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

        ImageView pokemonImage = view.findViewById(R.id.imageviewPokemon);
        TextView pokemonName = view.findViewById(R.id.name);
        TextView pokemonPrice = view.findViewById(R.id.cost);

        pokemonPrice.setTextColor(Color.BLACK);
        pokemonName.setTextColor(Color.BLACK);

        pokemonPrice.setText(pokemon.price);

        String pokemonNameText = "Hey, ik ben een " + pokemon.name + "!";
        pokemonName.setText(pokemonNameText);

        pokemonImage.setImageBitmap(pokemon.sprite);

        return view;
    }
}
