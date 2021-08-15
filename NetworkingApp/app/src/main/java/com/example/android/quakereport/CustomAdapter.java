package com.example.android.quakereport;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.util.List;

public class CustomAdapter extends ArrayAdapter<Earthquake> {
    String locationString;

    public CustomAdapter(Context context, List<Earthquake> objects) {
        super(context, 0, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listView = convertView;
        if (listView == null)
            listView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);

        Earthquake currentItem = getItem(position);

        TextView magnitude = listView.findViewById(R.id.magnitude);
        magnitude.setText(currentItem.getMagnitude() + "");

        GradientDrawable magnitudeCircle = (GradientDrawable) magnitude.getBackground();
        magnitudeCircle.setColor(getMagnitudeColor(currentItem.getMagnitude()));


        locationString = currentItem.getLocation();
        TextView location = listView.findViewById(R.id.location);
        TextView city = listView.findViewById(R.id.city);

        if (!locationString.contains("of")) {

            locationString = "Near the" + locationString;
            location.setText(locationString.substring(0, locationString.indexOf("the") + 3));
            city.setText(locationString.substring(locationString.indexOf("the") + 3));

        } else {
            location.setText(locationString.substring(0, locationString.indexOf("of") + 2));
            city.setText(locationString.substring(locationString.indexOf("of") + 2));
        }


        TextView date = listView.findViewById(R.id.date);
        date.setText(currentItem.getDateToDisplay().substring(0, 12));

        TextView time = listView.findViewById(R.id.time);
        time.setText(currentItem.getDateToDisplay().substring(12));
        return listView;
    }

    private int getMagnitudeColor(double magnitude) {
        int colorId;
        switch ((int) magnitude) {
            case 0:
            case 1:
                colorId = R.color.magnitude1;
                break;
            case 2:
                colorId = R.color.magnitude2;
                break;
            case 3:
                colorId = R.color.magnitude3;
                break;
            case 4:
                colorId = R.color.magnitude4;
                break;
            case 5:
                colorId = R.color.magnitude5;
                break;
            case 6:
                colorId = R.color.magnitude6;
                break;
            case 7:
                colorId = R.color.magnitude7;
                break;
            case 8:
                colorId = R.color.magnitude8;
                break;
            case 9:
                colorId = R.color.magnitude9;
                break;
            default:
                colorId = R.color.magnitude10plus;
                break;

        }
        colorId = ContextCompat.getColor(getContext(), colorId);
        return colorId;
    }
}
