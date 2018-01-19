package e.natasja.sugar_kidz;

/**
 * Created by Natasja on 19-1-2018.
 */

public class SimpleMeasurement {

    public String label;
    public String height;

    public SimpleMeasurement(){}

    public SimpleMeasurement(String label, String height){
        this.label = label;
        this.height = height;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }
}
