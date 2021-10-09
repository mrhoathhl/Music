package com.searce.musicplayer;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class MusicService extends Service implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {
    private MediaPlayer player;
    private ArrayList<Song> playlist;
    private int songPosn;
    private boolean shuffle, repeat;
    private final IBinder musicBind = new MusicBinder();
    private ArrayList<Integer> listPosSong;
    private int startIndex;
    private float volume;

    @Override
    public void onCreate() {
        super.onCreate();
        volume = .50f;
        songPosn = 0;
        startIndex = 0;
        shuffle = false;
        repeat = false;
        listPosSong = new ArrayList<Integer>();
        player = new MediaPlayer();
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

    //set list of position of list song
    public void setList(ArrayList<Song> list) {
        playlist = list;
        for (int i = 0; i < playlist.size(); i++)
            listPosSong.add(i);
    }

    //shuffle function
    public void shuffleSongs(boolean status) {
        if (!shuffle && status) {
            Toast.makeText(getBaseContext(), "Shuffle Enabled", Toast.LENGTH_SHORT).show();
            long seed = System.nanoTime();
            Collections.shuffle(listPosSong, new Random(seed));
            songPosn = listPosSong.lastIndexOf(songPosn);
            setStartIndex(songPosn);
        } else if (shuffle && !status) {
            Toast.makeText(getBaseContext(), "Shuffle Disabled", Toast.LENGTH_SHORT).show();
            songPosn = listPosSong.get(songPosn);
            for (int i = 0; i < playlist.size(); i++)
                listPosSong.set(i, i);
        }
        shuffle = status;
    }

    //repeat function
    public void repeatSongs(boolean status, int song_id) {
        if (!repeat && status) {
            Toast.makeText(getBaseContext(), "Repeat Enabled", Toast.LENGTH_SHORT).show();
            player.setLooping(true);
        }
        else if (repeat && !status) {
            Toast.makeText(getBaseContext(), "Repeat Disabled", Toast.LENGTH_SHORT).show();
            player.setLooping(false);
            setStartIndex(song_id); // Whenever repeat is disabled, the current song should become the start index for repeat.
        }
        repeat = status;
    }

    //check song is play or not to set reserve
    public void togglePlayPause() {
        if (player.isPlaying()) {
            player.pause();
        } else {
            player.start();
        }
    }

    //receive a position of song, find that song in list(song in device with id)
    //and prepare for new song to play
    public void setSong(int newPosn) {
        songPosn = newPosn;
        player.reset();
        Song curSong = playlist.get(newPosn);
        Uri fileUri = curSong.getUri();
        try {
            player.setDataSource(getBaseContext(), fileUri);
            player.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //previous function
    public void prevSong() {
        //if current song is play > 3s it will be playback that song
        if (player.getCurrentPosition() > 3000) {
            player.seekTo(0);
            return;
        }
        //if user click one more time when current time <3 it will be move to previous song
        songPosn -= 1;
        //if position of song < 0, the next song is the last song of list music
        if (songPosn < 0) {
            songPosn = listPosSong.size() - 1;
            System.out.println(songPosn + "=============");
        }
        setSong(listPosSong.get(songPosn));
        playSong();
    }

    //next function
    public void nextSong() {
        //check if repeat is enable
        if(repeat == true){
            songPosn += 1;
            setSong(listPosSong.get(songPosn));
            player.setLooping(true);
            playSong();
        } else if (songPosn == listPosSong.size() - 1) {//will return the first song if current song is the last
            setSong(listPosSong.get(0));
            playSong();
        } else {//move to next song
            songPosn += 1;
            setSong(listPosSong.get(songPosn));
            playSong();
        }
    }

    public void seekTo(int posn) {
        player.seekTo(posn);
    }

    public void setVolume(float vol) {
        volume = vol;
        player.setVolume(vol, vol);
    }

    public float getVolume() {
        return volume;
    }

    public int playingIndex() {
        return listPosSong.get(songPosn);
    }

    public boolean isPlaying() {
        return player.isPlaying();
    }

    public int getElapsed() {
        return player.getCurrentPosition();
    }

    public void setStartIndex(int startIndex) {
        // To set a starting point for repeat.
        // Once playback reaches this point and repeat is disabled, stop playback.
        this.startIndex = startIndex;
    }

    //creat a binder to bind every single function of app
    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        player.stop();
        player.release();
        return false;
    }

    public void playSong() {
        player.start();
    }

    public int getDuration() {
        return player.getDuration();
    }

    //when seekbar run out it mean this song is over
    //will send a broadcast
    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        nextSong();
        sendBroadcast(new Intent("Refresh the Song Info"));
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i2) {
        return false;
    }
}
