package e.natasja.sugar_kidz;

/**
 * Created by Natasja on 17-1-2018.
 */

public class Measurement {
    //properties of the class
    public String labelMeasurement;
    public String dateMeasurement;
    public String timeMeasurement;
    public String heightMeasurement;

    // default constructor is important for firebase
    public Measurement() {}

    // 'normal' constructor of the class
    public Measurement(String aLabel, String aDate, String aTime, String aHeight) {
        this.labelMeasurement = aLabel;
        this.dateMeasurement = aDate;
        this.timeMeasurement = aTime;
        this.heightMeasurement = aHeight;
    }

    public Measurement(String aLabel) {
        this.labelMeasurement = aLabel;
        this.dateMeasurement = "";
        this.timeMeasurement = "";
        this.heightMeasurement = "";
    }
}
