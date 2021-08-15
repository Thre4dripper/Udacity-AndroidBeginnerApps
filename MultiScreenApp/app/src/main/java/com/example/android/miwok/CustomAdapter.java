package com.example.android.miwok;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import java.util.ArrayList;

public class CustomAdapter extends ArrayAdapter<Word> {

    int imageVisibility;
    int colorId;

    public CustomAdapter(Activity context, ArrayList<Word> objects,int color, int imageVisibility) {
        super(context, 0, objects);
        this.imageVisibility=imageVisibility;
        this.colorId=color;
    }

    @Override
    public View getView(int position, View convertView,  ViewGroup parent) {

        View listview=convertView;
        if(listview==null)
            listview=LayoutInflater.from(getContext()).inflate(R.layout.list_item,parent,false);

        Word currentWord=getItem(position);

        TextView textView1=listview.findViewById(R.id.MiwokTranslation);
        textView1.setText(currentWord.getMiwokTranslation());

        TextView textView2=listview.findViewById(R.id.defaultTranslation);
        textView2.setText(currentWord.getDefaultTranslation());

        ImageView imageView=listview.findViewById(R.id.imageview);
        imageView.setImageResource(currentWord.getImageId());
        imageView.setVisibility(imageVisibility);

        ImageView mediaControls=listview.findViewById(R.id.mediaControls);

        View textContainer = listview.findViewById(R.id.linearLayout);
        int color = ContextCompat.getColor(getContext(), colorId);
        textContainer.setBackgroundColor(color);
        mediaControls.setBackgroundColor(color);

        return  listview;

    }
}
