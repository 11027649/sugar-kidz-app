package e.natasja.sugar_kidz;

import android.graphics.Bitmap;

/**
 * Created by Natasja on 10-1-2018.
 * This is a class that has been made to add Pokemons to Firebase. This was done once, so the
 * constructor of this class is not used in the app anymore. (See ReadMe.)
 *
 * The picture is saved as a String, using an encode method that wasn't needed anymore
 * (see GitHub page for the method, and also ReadMe for further explanation).
 * They are decoded by the decode message in the PokeshopAdapter.
 */

public class Pokemon {
    // properties of the class
    public String name;
    public String price;
    public String sprite;

    // default, empty, constructor of the class for FireBase
    public Pokemon() {};

    // constructor of the class, used when loading the JSON requests into FireBase
    public Pokemon(String aName, String aPrice, String aSprite) {
        this.name = aName;
        this.price = aPrice;
        this.sprite = aSprite;
    }
}
