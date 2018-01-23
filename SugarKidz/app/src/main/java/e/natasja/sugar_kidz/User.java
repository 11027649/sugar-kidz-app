package e.natasja.sugar_kidz;

/**
 * Created by Natasja on 15-1-2018.
 */

public class User {
    //properties of the class
    public String username;
    public Boolean isParent;
    public Integer xpAmount;


    // default constructor is important for firebase
    public User() {}

    // 'normal' constructor of the class
    public User(String anUsername, Boolean isParent, Integer aXPAmount) {
        this.username = anUsername;
        this.isParent = isParent;
        this.xpAmount = aXPAmount;
    }
}
