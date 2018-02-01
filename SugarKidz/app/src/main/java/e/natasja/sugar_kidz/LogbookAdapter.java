package e.natasja.sugar_kidz;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.TreeSet;

/**
 * Created by Natasja on 22-1-2018.
 * This is an adapter made to show the Logbook of the user or the user's kid. It contains
 * headers with the date that the measurements have been made.
 *
 * Source: http://stacktips.com/tutorials/android/listview-with-section-header-in-android
 */
public class LogbookAdapter extends BaseAdapter {
    private static final int TYPE_ITEM = 0;
    private static final int TYPE_SEPARATOR = 1;

    private MeasurementViewHolder mHolder;
    private HeaderViewHolder hHolder;

    private ArrayList<Measurement> measurements = new ArrayList<>();
    private TreeSet<Integer> sectionHeader = new TreeSet<>();

    private LayoutInflater mInflater;

    /**
     * When making the adapter, get the layout inflater.
     */
    public LogbookAdapter(Context context) {
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getItemViewType(int position) {
        return sectionHeader.contains(position) ? TYPE_SEPARATOR : TYPE_ITEM;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getCount() {
        return measurements.size();
    }

    @Override
    public Measurement getItem(int position) {
        return measurements.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Add an item to the Adapter.
     */
    public void addItem (Measurement measurement) {
        measurements.add(measurement);
        notifyDataSetChanged();
    }

    /**
     * Add a header to the adapter. This is a "Measurement" because the list only contains
     * measurements.
     */
    public void addSectionHeaderItem(final String dateString) {
        Measurement notARealMeasurement = new Measurement(dateString);

        measurements.add(notARealMeasurement);
        sectionHeader.add(measurements.size() - 1);
        notifyDataSetChanged();
    }

    /**
     * This is a functions that removes all items from the list.
     */
    public void removeAllItems() {
        measurements.clear();
    }

    /**
     * This is the getView method that inflates the layout if it's null, and if it's not fills it
     * with measurements and headers.
     */
    public View getView(int position, View convertView, ViewGroup parent) {

        int rowType = getItemViewType(position);

        if (convertView == null) {
            mHolder = new MeasurementViewHolder();
            hHolder = new HeaderViewHolder();

            // depending on what rowtype, inflate the layout and set the text
            switch (rowType) {
                case TYPE_ITEM:
                    convertView = mInflater.inflate(R.layout.row_logbook, null);
                    setTypeItemView(position, convertView);
                    break;

                case TYPE_SEPARATOR:
                    convertView = mInflater.inflate(R.layout.row_header_logbook, null);
                    setTypeSeparatorView(position, convertView);
                    break;
            }
        } else {
            // the measurements are more abundant than the headers so try a measurement first
            setView(position, convertView);
        }

        return convertView;
    }

    /**
     * In this function, the measurement is loaded into the list. This function is only needed if the
     * convertView was null.
     */
    public void setTypeItemView(int position, View convertView) {
        mHolder.timeTextView = convertView.findViewById(R.id.time);
        mHolder.labelTextView = convertView.findViewById(R.id.label);
        mHolder.heightTextView = convertView.findViewById(R.id.height);

        setTextmHolder(position);
        convertView.setTag(mHolder);
    }

    /**
     * In this function, the header is loaded into the list. This function is only needed if the
     * convertView was null.
     */
    public void setTypeSeparatorView(int position, View convertView) {
        hHolder.headerTextView = convertView.findViewById(R.id.dateTextView);

        setTexthHolder(position);
        convertView.setTag(hHolder);
    }

    /**
     * This function sets the logbook view if it wasn't zero (so after scrolling the listview)
     */
    public void setView(int position, View convertView) {
        try {
            mHolder = (MeasurementViewHolder) convertView.getTag();
            setTextmHolder(position);
        } catch(ClassCastException e) {
            hHolder = (HeaderViewHolder) convertView.getTag();
            setTexthHolder(position);
        }
    }

    /**
     * Set the text in the Header Holder View.
     */
    public void setTexthHolder(int position) {
        hHolder.headerTextView.setText(measurements.get(position).labelMeasurement);
    }

    /**
     * Set the text in the Measurement Holder View.
     */
    public void setTextmHolder(int position) {
        mHolder.labelTextView.setText(measurements.get(position).labelMeasurement);
        mHolder.heightTextView.setText(measurements.get(position).heightMeasurement);
        mHolder.timeTextView.setText(measurements.get(position).timeMeasurement);
    }

    /**
     * A class for the MeasurementViewHolder.
     */
    public static class MeasurementViewHolder {
        public TextView labelTextView;
        public TextView heightTextView;
        public TextView timeTextView;
    }

    /**
     * A class for the HeaderViewHolder.
     */
    public static class HeaderViewHolder {
        public TextView headerTextView;
    }


}
