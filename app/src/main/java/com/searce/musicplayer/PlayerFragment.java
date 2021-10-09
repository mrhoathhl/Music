package com.searce.musicplayer;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.text.SimpleDateFormat;

public class PlayerFragment extends Fragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener, SeekBar.OnSeekBarChangeListener {
    Button bPlay, bPrev, bNext;
    ImageView ivAlbumArt;
    ToggleButton tbRep, tbShuf;
    SeekBar seekBar;
    TextView tvElapsed, tvRemaining, tvDuration;
    TextView tvTitle;
    TextView tvAlbum;
    TextView tvArtist;
    Button bList, bVolume;
    Communicator comm;
    AsyncPlay asyncPlay;
    int new_progress;
    boolean skip_progress_updates;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player,container,false);
        return view;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        comm = (Communicator) getActivity();
        bPlay = (Button) getActivity().findViewById(R.id.bPlay);
        bPrev = (Button) getActivity().findViewById(R.id.bPrev);
        bNext = (Button) getActivity().findViewById(R.id.bNext);
        tbShuf = (ToggleButton) getActivity().findViewById(R.id.tbShuf);
        tbRep = (ToggleButton) getActivity().findViewById(R.id.tbRep);
        seekBar = (SeekBar) getActivity().findViewById(R.id.sbTime);
        tvElapsed = (TextView) getActivity().findViewById(R.id.tvElapsed);
        tvDuration = (TextView) getActivity().findViewById(R.id.tvDuration_Player);
        tvTitle = (TextView) getActivity().findViewById(R.id.tvSongTitle_TitleFrag);
        tvAlbum = (TextView) getActivity().findViewById(R.id.tvAlbum_TitleFrag);
        tvArtist = (TextView) getActivity().findViewById(R.id.tvArtist_TitleFrag);
        bList = (Button) getActivity().findViewById(R.id.bBrowse);
        bVolume = (Button) getActivity().findViewById(R.id.bVolume);
        ivAlbumArt = (ImageView) getActivity().findViewById(R.id.ivAlbumArt);

        bList.setOnClickListener(this);
        bPlay.setOnClickListener(this);
        bPrev.setOnClickListener(this);
        bNext.setOnClickListener(this);
        bVolume.setOnClickListener(this);

        tbRep.setOnCheckedChangeListener(this);
        tbShuf.setOnCheckedChangeListener(this);
        seekBar.setOnSeekBarChangeListener(this);

        ivAlbumArt.setOnTouchListener(new OnSwipeTouchListener(ivAlbumArt.getContext()) {
            // For next/prev operation based on swipes.
            @Override
            public void onSwipeRight() {
                onClick(bPrev);
            }

            @Override
            public void onSwipeLeft() {
                onClick(bNext);
            }

            // For inc/dec volume based on scrolls.
            @Override
            public void onSlideUp(float distance) {
                distance = distance / ivAlbumArt.getHeight();
                comm.set_volume(distance);
                // We want positive diff on upward slide.
            }

            @Override
            public void onSlideDown(float distance) {
                distance = distance / ivAlbumArt.getHeight();
                comm.set_volume(distance);
                // We want negative diff on downward slide.
            }
        });

        tvTitle.setSelected(true);
        tvArtist.setSelected(true);
        updateAlbumArt();
        updateTags();
        setMaxDuration(comm.get_duration());
    }

    //stop asynctask when user close app
    @Override
    public void onStop() {
        asyncPlay.cancel(true);
        super.onStop();
    }

    //determine and run new async
    //check song play or not
    //update time current song and set button play
    @Override
    public void onResume() {
        asyncPlay = new AsyncPlay();
        asyncPlay.execute();
        if (!comm.is_playing()) {
            updateTimers(comm.get_elapsed());
            bPlay.setBackgroundResource(R.drawable.ic_play_24dp);
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        asyncPlay.cancel(true);
        super.onPause();
    }

    @Override
    public void onClick(View v) {
        comm.song_operations(v.getId());
        if (comm.is_playing()) {
            bPlay.setBackgroundResource(R.drawable.ic_pause_24dp);
        }
        else {
            bPlay.setBackgroundResource(R.drawable.ic_play_24dp);
        }
        seekBar.setProgress(comm.get_elapsed());
        updateTimers(comm.get_elapsed());
    }

    //set state for 2 mode of play song
    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        comm.playback_mode(compoundButton.getId(), b);
    }

    //update new time and new position of seek bar when song play
    @Override
    public void onProgressChanged(final SeekBar seekBar, int i, boolean b) {
        if (b) {
            new_progress = i;
            updateTimers(i);
        }
    }

    //when user start their action in seekbar
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        skip_progress_updates = true;
    }

    //when user finish their action in seekbar
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        comm.set_progress(new_progress);
        skip_progress_updates = false;
    }

    //do in parallel with main thread
    //detect when user do their action in seekbar
    //this will listen and change value with value user done
    public class AsyncPlay extends AsyncTask<Void, Void, Void> {
        //on start this function will set max for seekbar
        @Override
        protected void onPreExecute() {
            seekBar.setMax(comm.get_duration());
        }

        //in background this function will check state of seekbar and
        //state of song is play or not
        @Override
        protected Void doInBackground(Void... voids) {
            while (true) {
                if (skip_progress_updates) {
                    continue;
                }
                // Revert to original colors on playing.
                //update current time of song was play
                if (comm.is_playing()) {
                    seekBar.setProgress(comm.get_elapsed());
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateTimers(comm.get_elapsed());
                        }
                    });
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //get current time
    private void updateTimers(int progress) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss");
        tvElapsed.setText(" " + dateFormat.format(progress) + " ");
    }

    //update avatar of song, if song have not avatar it will replace with
    //default avatar
    public void updateAlbumArt() {
        byte[] bytes = comm.get_album_art();
        if (bytes == null)
            ivAlbumArt.setImageDrawable(getResources().getDrawable(R.drawable.splash));
        else
            ivAlbumArt.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
    }

    //update info of song
    //name song
    //name artist
    //time duration
    public void updateTags() {
        tvTitle.setText(comm.get_title());
        tvArtist.setText(comm.get_artist());
        tvDuration.setText(comm.get_song_list().get(comm.get_song_id()).getDuration());
    }

    //set max of seekbar by the lenght of song
    public void setMaxDuration(int duration) {
        seekBar.setMax(duration);
    }
}
