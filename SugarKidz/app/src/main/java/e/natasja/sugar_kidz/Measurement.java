package e.natasja.sugar_kidz;

/**
 * Created by Natasja on 17-1-2018.
 * This is a Measurement class, to add Measurements to the database. It is also used for the ListViews
 * in the Logbook and in the part of the Logbook that you can see in the MainActivity.
 */

public class Measurement {
    //properties of the class
    public String labelMeasurement;
    public String dateMeasurement;
    public String timeMeasurement;
    public String heightMeasurement;

    // default, empty, constructor of the class for FireBase
    public Measurement() {}

    // 'normal' constructor of the class
    public Measurement(String aLabel, String aDate, String aTime, String aHeight) {
        this.labelMeasurement = aLabel;
        this.dateMeasurement = aDate;
        this.timeMeasurement = aTime;
        this.heightMeasurement = aHeight;
    }

    // this constructor is used to create the headers and messages in the ListViews,
    // because these needs to be a Measurement to be saved in the same List as the real Measurements
    public Measurement(String aLabel) {
        this.labelMeasurement = aLabel;
        this.dateMeasurement = "";
        this.timeMeasurement = "";
        this.heightMeasurement = "";
    }
}
