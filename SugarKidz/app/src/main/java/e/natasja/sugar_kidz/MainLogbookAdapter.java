package e.natasja.sugar_kidz;

import android.content.Context;
import android.content.DialogInterface;
import android.icu.util.Measure;
import android.support.annotation.NonNull;
import android.app.AlertDialog;
import android.text.Html;
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
 * It allows the user to delete messages by long clicking on a list item. It builds a dialog to ask
 * the user if they are sure they want to delete that measurement.
 */
public class MainLogbookAdapter extends ArrayAdapter {
    private ArrayList<Measurement> measurement;
    private String uid;
    private Context mContext;

    /**
     * When making the adapter, give the context and the Measurement list.
     */
    MainLogbookAdapter(Context context, ArrayList<Measurement> measurements) {
        super(context, 0, measurements);

        // copy the measurements and context to make them available for use in the Adapter
        measurement = measurements;
        mContext = context;
    }

    @NonNull
    @Override
    public View getView(int position, View view, @NonNull ViewGroup parent) {

        Measurement measurement = (Measurement) getItem(position);

        // if the view is null, inflate the view
        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.row_logbook, parent, false);
        }

        // find the TextViews and populate them with measurements
        TextView timeTextView = view.findViewById(R.id.time);
        TextView labelTextView = view.findViewById(R.id.label);
        TextView heightTextView = view.findViewById(R.id.height);

        if (measurement != null) {
            timeTextView.setText(measurement.timeMeasurement);
            labelTextView.setText(measurement.labelMeasurement);
            heightTextView.setText(measurement.heightMeasurement);
        }

        return view;
    }

    /**
     * This method makes the items in the ListView clickable by adding a ClickListener.
     */
    public void deleteOnLongClick(ListView lv) {
        lv.setLongClickable(true);
        lv.setOnItemLongClickListener(longClickListener);

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    /**
     * This is the ClickListener that builds a confirm dialog when you long clicked on an item.
     * It gives this dialog the measurement that has been clicked on, to delete if the user confirms.
     */
    private AdapterView.OnItemLongClickListener longClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
            Measurement toDelete = measurement.get(position);
            dialogBuilder(toDelete);
            return true;
        }
    };

    /**
     * Dialog builder for confirmation of deleting a measurement.
     */
    private void dialogBuilder(final Measurement toDelete) {
        // instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.Theme_AppCompat_Dialog_Alert);

        String message = "Weet je zeker dat je deze meting van " + toDelete.heightMeasurement
                + " om " + toDelete.timeMeasurement + " wilt verwijderen?";

        String title = "Let op!";

        // chain together various setter methods to set the dialog characteristics
        builder.setMessage(Html.fromHtml("<font color='black'>" + message + "</font>"))
            .setTitle(Html.fromHtml("<font color='black'>" + title + "</font>"));

        setButtonActions(toDelete, builder);

        // create the AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * This functions sets the button texts and sets the right actions when one of the buttons is
     * clicked.
     */
    public void setButtonActions(final Measurement toDelete, AlertDialog.Builder builder) {
        // add the buttons
        builder.setPositiveButton("Ja, verwijder", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // user clicked OK button, delete Measurement
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
    }

}
