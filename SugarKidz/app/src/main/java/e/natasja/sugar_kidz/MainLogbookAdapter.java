package e.natasja.sugar_kidz;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

/**
 * Created by Natasja on 17-1-2018.
 * MainLogbookAdapter is an ArrayAdapter that shows the measurements of the user, but only of today.
 */

public class MainLogbookAdapter extends ArrayAdapter {
    ArrayList<Measurement> measurement;
    String uid;
    Context mContext;

    MainLogbookAdapter(Context context, ArrayList<Measurement> measurements) {
        super(context, 0, measurements);

        measurement = measurements;
        mContext = context;
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

    void deleteOnLongClick(ListView lv) {
        lv.setLongClickable(true);
        lv.setOnItemLongClickListener(longClickListener);

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    private AdapterView.OnItemLongClickListener longClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {

            Measurement toDelete = measurement.get(position);
            dialogBuilder(toDelete);
            return true;
        }
    };

    private void dialogBuilder(final Measurement toDelete) {

        // instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.Theme_AppCompat_Dialog_Alert);

        // chain together various setter methods to set the dialog characteristics
        builder.setMessage("Weet je zeker dat je deze meting van " +
                toDelete.heightMeasurement + " om " +
                toDelete.timeMeasurement + " wilt verwijderen?")
                .setTitle("Weet je zeker dat je deze meting wilt verwijderen?");

        // add the buttons
        builder.setPositiveButton("Ja, verwijder", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // user clicked OK button
                DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("users/");
                mRef.child(uid).child("Measurements").child(toDelete.dateMeasurement)
                        .child(toDelete.timeMeasurement).setValue(null);
            }
        });

        builder.setNegativeButton("Nee, ga terug", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // user cancelled the dialog
                dialog.cancel();
            }
        });

        // create the AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }





}
