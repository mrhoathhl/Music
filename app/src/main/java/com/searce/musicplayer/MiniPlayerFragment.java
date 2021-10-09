package com.searce.musicplayer;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class MiniPlayerFragment extends Fragment implements View.OnClickListener {
    RelativeLayout rlMiniPlayer;
    TextView tvSongTitle;
    TextView tvArtist;
    Button bPlayPause;
    Communicator comm;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_mini_player, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        rlMiniPlayer = (RelativeLayout) getActivity().findViewById(R.id.rlPlayer);
        tvSongTitle = (TextView) getActivity().findViewById(R.id.tvSongTitle_MiniPlayerFrag);
        tvArtist = (TextView) getActivity().findViewById(R.id.tvArtist_MiniPlayerFrag);
        bPlayPause = (Button) getActivity().findViewById(R.id.bPlay_MiniPlayerFrag);
        rlMiniPlayer.setOnClickListener(this);
        tvSongTitle.setSelected(true);
        tvArtist.setSelected(true);
        bPlayPause.setOnClickListener(this);
        comm = (Communicator) getActivity();
        if (comm.is_playing()) {
            bPlayPause.setBackgroundResource(R.drawable.ic_pause_24dp);
        }
        else{
            bPlayPause.setBackgroundResource(R.drawable.ic_play_24dp);
        }
        updateTags();
    }

    //update info of song
    public void updateTags() {
        tvArtist.setText(comm.get_artist());
        tvSongTitle.setText(comm.get_title());
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rlPlayer:
                comm.goToPlayer();
                break;
            case R.id.bPlay_MiniPlayerFrag:
                comm.song_operations(R.id.bPlay);//sync 2 button in miniplayer and mainplayer is the same status
                if (comm.is_playing()) {
                    bPlayPause.setBackgroundResource(R.drawable.ic_pause_24dp);
                }
                else{
                    bPlayPause.setBackgroundResource(R.drawable.ic_play_24dp);
                }
                break;
        }
    }
}
