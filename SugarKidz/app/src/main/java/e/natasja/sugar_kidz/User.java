package e.natasja.sugar_kidz;

/**
 * Created by Natasja on 15-1-2018.
 * This is a user class to add accounts to the FireBase database.
 */

public class User {
    //properties of the class
    public String username;
    public Boolean isParent;
    public Integer xpAmount;
    public Boolean coupled;


    // default, empty, constructor of the class for FireBase
    public User() {}

    // 'normal' constructor of the class
    public User(String anUsername, Boolean isParent, Integer aXPAmount, Boolean isCoupled) {
        this.username = anUsername;
        this.isParent = isParent;
        this.xpAmount = aXPAmount;
        this.coupled = isCoupled;
    }
}
