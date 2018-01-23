package e.natasja.sugar_kidz;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.TreeSet;

/**
 * Created by Natasja on 22-1-2018.
 */

public class TotalLogbookAdapter extends BaseAdapter {
    private static final int TYPE_ITEM = 0;
    private static final int TYPE_SEPARATOR = 1;

    private ArrayList<Measurement> measurements = new ArrayList<>();
    private TreeSet<Integer> sectionHeader = new TreeSet<>();

    private LayoutInflater mInflater;

    public TotalLogbookAdapter(Context context) {
        mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void addItem (Measurement measurement) {
        measurements.add(measurement);
        notifyDataSetChanged();
    }

    public void addSectionHeaderItem(final String dateString) {
        Measurement notARealMeasurement = new Measurement(dateString, "", "", "");

        measurements.add(notARealMeasurement);
        sectionHeader.add(measurements.size() - 1);
        notifyDataSetChanged();
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
     *
     * Source: http://stacktips.com/tutorials/android/listview-with-section-header-in-android
     */
    public View getView(int position, View convertView, ViewGroup parent) {

        MeasurementViewHolder mHolder = null;
        HeaderViewHolder hHolder = null;

        int rowType = getItemViewType(position);
        boolean isHeader = false;

        if (convertView == null) {
            mHolder = new MeasurementViewHolder();
            hHolder = new HeaderViewHolder();

            switch (rowType) {
                case TYPE_ITEM:
                    convertView = mInflater.inflate(R.layout.row_logbook, null);

                    mHolder.timeTextView = convertView.findViewById(R.id.time);
                    mHolder.labelTextView = convertView.findViewById(R.id.label);
                    mHolder.heightTextView = convertView.findViewById(R.id.height);

                    Log.d("CONTROL LISTVIEW", "type_item");

                    convertView.setTag(mHolder);

                    break;

                case TYPE_SEPARATOR:
                    convertView = mInflater.inflate(R.layout.row_header, null);
                    hHolder.holderTextView = convertView.findViewById(R.id.dateTextView);
                    isHeader = true;

                    Log.d("CONTROL LISTVIEW", "type_separator");

                    convertView.setTag(hHolder);

                    break;
            }
        } else {
            try {
                mHolder = (MeasurementViewHolder) convertView.getTag();
            } catch(ClassCastException e) {
                hHolder = (HeaderViewHolder) convertView.getTag();
            }
        }

        if (isHeader) {
            hHolder.holderTextView.setText(measurements.get(position).labelMeasurement);
        } else {
            if (mHolder != null) {
                mHolder.labelTextView.setText(measurements.get(position).labelMeasurement);
                mHolder.heightTextView.setText(measurements.get(position).heightMeasurement);
                mHolder.timeTextView.setText(measurements.get(position).timeMeasurement);
            }
        }


        return convertView;
    }

    public static class MeasurementViewHolder {
        public TextView labelTextView;
        public TextView heightTextView;
        public TextView timeTextView;
    }

    public static class HeaderViewHolder {
        public TextView holderTextView;
    }


}
