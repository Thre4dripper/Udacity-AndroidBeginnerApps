package com.example.android.miwok;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class PhraseFragment extends Fragment {
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
        words.add(new Word("Where are you going?","minto wuksus",R.raw.phrase_where_are_you_going));
        words.add(new Word("What is your name?","tinnә oyaase'nә",R.raw.phrase_what_is_your_name));
        words.add(new Word("My name is...","oyaaset...",R.raw.phrase_my_name_is));
        words.add(new Word("How are you feeling?","michәksәs?",R.raw.phrase_how_are_you_feeling));
        words.add(new Word("I’m feeling good.","kuchi achit",R.raw.phrase_im_feeling_good));
        words.add(new Word("Are you coming?","әәnәs'aa?",R.raw.phrase_are_you_coming));
        words.add(new Word("Yes, I’m coming.","hәә’ әәnәm",R.raw.phrase_yes_im_coming));
        words.add(new Word("I’m coming.","әәnәm",R.raw.phrase_im_coming));
        words.add(new Word("Let’s go.","yoowutis",R.raw.phrase_lets_go));
        words.add(new Word("Come here.","әnni'nem",R.raw.phrase_come_here));

        CustomAdapter myAdapter=new CustomAdapter(getActivity(),words,R.color.category_phrases, View.GONE);

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