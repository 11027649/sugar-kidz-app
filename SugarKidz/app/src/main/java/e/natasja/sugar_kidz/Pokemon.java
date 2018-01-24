package e.natasja.sugar_kidz;

import android.graphics.Bitmap;

/**
 * Created by Natasja on 10-1-2018.
 */

public class Pokemon {
    // properties of the class
    public String name;
    public String price;
    public String sprite;


    // constructor for firebase
    public Pokemon() {};

    // constructor of the class
    public Pokemon(String aName, String aPrice, String aSprite) {
        this.name = aName;
        this.price = aPrice;
        this.sprite = aSprite;
    }
}
