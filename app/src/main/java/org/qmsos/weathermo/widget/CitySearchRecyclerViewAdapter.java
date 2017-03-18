package org.qmsos.weathermo.widget;

import android.content.Context;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.qmsos.weathermo.R;
import org.qmsos.weathermo.model.City;

/**
 * Implementation that shows a list of candidate cities from data that passed.
 */
public class CitySearchRecyclerViewAdapter
        extends BaseArrayRecyclerViewAdapter<City, ViewHolder> {

    private OnInsertCityClickedListener mListener;

    public CitySearchRecyclerViewAdapter(Context context, City[] dataArray) {
        super(context, dataArray);

        try {
            mListener = (OnInsertCityClickedListener) context;
        } catch (ClassCastException e) {
            String listenerName = OnInsertCityClickedListener.class.getSimpleName();

            throw new ClassCastException(context.toString()
                    + " must implements "
                    + listenerName);
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, City[] dataArray) {
        final City fData = dataArray[holder.getAdapterPosition()];

        int maxLengthOfCityName = 15;
        String cityName = fData.getCityName();
        if (cityName.length() > maxLengthOfCityName) {
            cityName = cityName.substring(0, maxLengthOfCityName) + "...";
        }
        String country = fData.getCountry();
        String raw = cityName + " " + country;

        SpannableString spanned = new SpannableString(raw);
        spanned.setSpan(new RelativeSizeSpan(0.5f),
                cityName.length() + 1,
                raw.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        double longitude = fData.getLongitude();
        double latitude = fData.getLatitude();
        String lon = longitude > 0
                ? Math.abs(longitude) + "\u00b0E"
                : Math.abs(longitude) + "\u00b0W";
        String lat = latitude > 0
                ? Math.abs(latitude) + "\u00b0N"
                : Math.abs(latitude) + "\u00b0S";

        String coordinateInfo = lon + " " + lat;

        ((RecyclerViewHolder) holder).mCityInfoView.setText(spanned);
        ((RecyclerViewHolder) holder).mCityGeoView.setText(coordinateInfo);
        ((RecyclerViewHolder) holder).mInsertButton.setText(R.string.button_insert);
        ((RecyclerViewHolder) holder).mInsertButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mListener.onInsertCityClicked(fData);
            }
        });
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = View.inflate(parent.getContext(), R.layout.view_holder_city, null);

        return new RecyclerViewHolder(view);
    }

    /**
     * Implementation of ViewHolder that will be displayed.
     */
    static class RecyclerViewHolder extends ViewHolder {
        TextView mCityInfoView;
        TextView mCityGeoView;
        Button mInsertButton;

        public RecyclerViewHolder(View itemView) {
            super(itemView);

            mCityInfoView = (TextView) itemView.findViewById(R.id.city_main);
            mCityGeoView = (TextView) itemView.findViewById(R.id.city_sub);
            mInsertButton = (Button) itemView.findViewById(R.id.city_button);
        }
    }

    /**
     * Interface of the callback to be invoked when the button of adding city
     * that inside of ViewHolder is clicked.
     */
    public interface OnInsertCityClickedListener {
        /**
         * Callback to insert city.
         *
         * @param city
         *            The city to insert.
         */
        void onInsertCityClicked(City city);
    }
}