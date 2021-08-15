package com.example.android.miwok;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;

/**
 * FRAGMENT CLASS
 * **/
public class NumberFragment extends Fragment {
    MediaPlayer media;
    AudioManager audioManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.word_list, container, false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        }

        ArrayList<Word> words=new ArrayList<>();
        words.add(new Word("one","lutti",R.raw.number_one,R.drawable.number_one));
        words.add(new Word("two","otiiko",R.raw.number_two,R.drawable.number_two));
        words.add(new Word("three","tolookosu",R.raw.number_three,R.drawable.number_three));
        words.add(new Word("four","oyyisa",R.raw.number_four,R.drawable.number_four));
        words.add(new Word("five","massokka",R.raw.number_five,R.drawable.number_five));
        words.add(new Word("six","temmokka",R.raw.number_six,R.drawable.number_six));
        words.add(new Word("seven","kenekaku",R.raw.number_seven,R.drawable.number_seven));
        words.add(new Word("eight","kawinta",R.raw.number_eight,R.drawable.number_eight));
        words.add(new Word("nine","wo’e",R.raw.number_nine,R.drawable.number_nine));
        words.add(new Word("ten","na’aacha",R.raw.number_ten,R.drawable.number_ten));

        CustomAdapter myAdapter=new CustomAdapter(getActivity(),words,R.color.category_numbers, View.VISIBLE);

        ListView listView=rootView.findViewById(R.id.listview);
        listView.setAdapter(myAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Word word=words.get(position);

                media=MediaPlayer.create(getActivity(),word.getMedia());
                media.start();

                media.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        Toast.makeText(getActivity(), "Completed", Toast.LENGTH_SHORT).show();
                        media.release();

                        media.setOnCompletionListener(mCompletionListener);
                    }
                });


            }
        });

        return rootView;
    }

    /**================================== CREATED A AUDIO FOCUS CHANGE LISTENER AT ONCE =====================**/
    private AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener=new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT ||
                    focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {

                media.pause();
                media.seekTo(0);
            } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {

                media.start();
            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {

                releaseMediaPlayer();
            }
        }
    };

    private MediaPlayer.OnCompletionListener mCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            releaseMediaPlayer();
        }
    };

    /**=============================== RELEASE MEDIA PLAYER FOR OTHER SOUNDS ================================**/
    private void releaseMediaPlayer() {

        if (media != null) {
            media.release();
            media = null;

            audioManager.abandonAudioFocus(onAudioFocusChangeListener);
        }
    }


    /**================================== WHEN HOME BUTTON IS PRESSED =======================================**/
    @Override
    public void onStop() {
        super.onStop();
        releaseMediaPlayer();
    }

}
