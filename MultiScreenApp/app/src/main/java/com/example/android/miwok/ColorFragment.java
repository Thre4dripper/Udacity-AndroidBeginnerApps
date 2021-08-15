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

import androidx.fragment.app.Fragment;

import java.util.ArrayList;

/**
 * FRAGMENT CLASS
 **/
public class ColorFragment extends Fragment {
    MediaPlayer media;
    AudioManager audioManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView=inflater.inflate(R.layout.word_list, container, false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        }

        ArrayList<Word> words=new ArrayList<>();
        words.add(new Word("red","weṭeṭṭi",R.raw.color_red,R.drawable.color_red));
        words.add(new Word("green","chokokki",R.raw.color_green,R.drawable.color_green));
        words.add(new Word("brown","ṭakaakki",R.raw.color_brown,R.drawable.color_brown));
        words.add(new Word("gray","ṭopoppi",R.raw.color_gray,R.drawable.color_gray));
        words.add(new Word("black","kululli",R.raw.color_black,R.drawable.color_black));
        words.add(new Word("white","kelelli",R.raw.color_white,R.drawable.color_white));
        words.add(new Word("dusty yellow","ṭopiisә",R.raw.color_dusty_yellow,R.drawable.color_dusty_yellow));
        words.add(new Word("mustard yellow","chiwiiṭә",R.raw.color_mustard_yellow,R.drawable.color_mustard_yellow));

        CustomAdapter myAdapter=new CustomAdapter(getActivity(),words,R.color.category_colors, View.VISIBLE);

        ListView listView=rootView.findViewById(R.id.listview);
        listView.setAdapter(myAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Word word=words.get(position);

                media=MediaPlayer.create(getActivity(),word.getMedia());
                media.start();

                media.setOnCompletionListener(mCompletionListener);
            }
        });

      //  media.setOnCompletionListener(mCompletionListener);

        return rootView;
    }

/**================================== CREATED A AUDIO FOCUS CHANGE LISTENER AT ONCE =====================**/
    private AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener=new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT ||
                    focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
                // The AUDIOFOCUS_LOSS_TRANSIENT case means that we've lost audio focus for a
                // short amount of time. The AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK case means that
                // our app is allowed to continue playing sound but at a lower volume. We'll treat
                // both cases the same way because our app is playing short sound files.

                // Pause playback and reset player to the start of the file. That way, we can
                // play the word from the beginning when we resume playback.
                media.pause();
                media.seekTo(0);
            } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                // The AUDIOFOCUS_GAIN case means we have regained focus and can resume playback.
                media.start();
            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                // The AUDIOFOCUS_LOSS case means we've lost audio focus and
                // Stop playback and clean up resources
                releaseMediaPlayer();
            }
        }
    };

    private MediaPlayer.OnCompletionListener mCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            // Now that the sound file has finished playing, release the media player resources.
            releaseMediaPlayer();
        }
    };

    /**=============================== RELEASE MEDIA PLAYER FOR OTHER SOUNDS ================================**/
    private void releaseMediaPlayer() {
        // If the media player is not null, then it may be currently playing a sound.
        if (media != null) {
            // Regardless of the current state of the media player, release its resources
            // because we no longer need it.
            media.release();

            // Set the media player back to null. For our code, we've decided that
            // setting the media player to null is an easy way to tell that the media player
            // is not configured to play an audio file at the moment.
            media = null;

            // Regardless of whether or not we were granted audio focus, abandon it. This also
            // unregisters the AudioFocusChangeListener so we don't get anymore callbacks.
            audioManager.abandonAudioFocus(onAudioFocusChangeListener);
        }
    }

    /**================================== WHEN HOME BUTTON IS PRESSED =======================================**/
    @Override
    public void onStop() {
        super.onStop();

        // When the activity is stopped, release the media player resources because we won't
        // be playing any more sounds.
        releaseMediaPlayer();
    }
}