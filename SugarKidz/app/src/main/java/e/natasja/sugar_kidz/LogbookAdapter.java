package e.natasja.sugar_kidz;

import android.content.Context;
import android.graphics.Color;
import android.icu.util.Measure;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Natasja on 17-1-2018.
 * LogbookAdapter is an ArrayAdapter that shows the measurements of the user, but only of today.
 */

public class LogbookAdapter extends ArrayAdapter {
    LogbookAdapter(Context context, ArrayList<Measurement> measurements) {
        super(context, 0, measurements);
    }

    @NonNull
    @Override
    public View getView(int position, View view, @NonNull ViewGroup parent) {

        Measurement measurement = (Measurement) getItem(position);

        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.row_logbook, parent, false);
        }

        TextView timeTextView = view.findViewById(R.id.time);
        TextView labelTextView = view.findViewById(R.id.label);
        TextView heightTextView = view.findViewById(R.id.height);

        timeTextView.setText(measurement.timeMeasurement);
        labelTextView.setText(measurement.labelMeasurement);
        heightTextView.setText(measurement.heightMeasurement);


        return view;
    }
}
