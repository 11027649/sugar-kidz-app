package e.natasja.sugar_kidz;

/**
 * Created by Natasja on 19-1-2018.
 * This is a class to add measurements to the FireBase database. It is created because adding the
 * whole measurement wouldn't create a nice hierarchy in the database.
 */

public class SimpleMeasurement {
    // properties of the class
    public String label;
    public String height;

    // default, empty, constructor of the class for FireBase
    public SimpleMeasurement(){}

    // 'normal' constructor for ourselves
    public SimpleMeasurement(String label, String height){
        this.label = label;
        this.height = height;
    }
}
