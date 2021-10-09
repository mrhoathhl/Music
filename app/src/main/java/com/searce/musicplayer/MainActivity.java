package com.searce.musicplayer;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;


public class MainActivity extends Activity implements Communicator {
    PlayerFragment playerFrag;
    SongListFragment songListFragment;
    MiniPlayerFragment miniPlayerFragment;
    ArrayList<Song> songFiles;
    private MusicService musicSvc;
    private Intent playIntent;
    private boolean musicBound = false;
    private SongCompletedListener songCompletedListener;
    private Toast vol_toast;
    FragmentManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        //will get the Bundle null when activity get starts first time and it will get in use when activity orientation get changed
        if (savedInstanceState == null) {
            playerFrag = new PlayerFragment();
            songListFragment = new SongListFragment();
            miniPlayerFragment = new MiniPlayerFragment();
            songFiles = (ArrayList<Song>) getIntent().getSerializableExtra("songs");
            Collections.sort(songFiles);
            vol_toast = Toast.makeText(getBaseContext(), "Volume", Toast.LENGTH_LONG);
        }

        if (playIntent == null) {
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
    }


    //connect to the service
    private ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            //get service
            musicSvc = binder.getService();
            //pass list of song to service
            musicSvc.setList(songFiles);
            musicSvc.setSong(0);
            musicBound = true;
            show_list();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    // Inflate the menu; this adds items to the action bar if it is present.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.player, menu);
        return true;
    }

    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                break;
            case R.id.action_settings:
                break;
            case R.id.action_rescan:
                Intent rescan = new Intent(MainActivity.this, SplashActivity.class);
                rescan.putExtra("rescan", true);
                startActivity(rescan);
                finish();
                break;
            case R.id.action_about:
                Intent about = new Intent(MainActivity.this, About.class);
                startActivity(about);
                break;
            case R.id.action_exit:
                finish();
                break;
            case android.R.id.home:
                if (getFragmentManager().getBackStackEntryCount() != 0) {
                    getFragmentManager().popBackStack();
                }
                return true;
        }
        return false;
    }

    @Override
    public boolean isSongExist() {
        return songFiles != null && songFiles.size() > 0;
    }

    //set state of 2 mode of player
    //repeat song will pass current id of song and status is repeated or not
    //shuffle will pass status to determined is shuffled or not
    @Override
    public void playback_mode(int id, boolean status) {
        switch (id) {
            case R.id.tbRep:
                musicSvc.repeatSongs(status, get_song_id());
                break;
            case R.id.tbShuf:
                musicSvc.shuffleSongs(status);
                break;
        }
    }

    //function of media player
    @Override
    public void song_operations(int id) {
        switch (id) {
            case R.id.bPlay:
                togglePlayPause();
                break;
            case R.id.bNext:
                musicSvc.nextSong();
                updateSongInfo();
                break;
            case R.id.bPrev:
                musicSvc.prevSong();
                updateSongInfo();
                break;
            case R.id.bBrowse:
                show_list();
                break;
            case R.id.bVolume:
                AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                audio.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_SAME, AudioManager.FLAG_SHOW_UI);
                break;
        }
    }

    //whenever user click song, next song or previous song this function
    //will update info of song
    private void updateSongInfo() {
        if (playerFrag.isVisible()) {
            playerFrag.updateAlbumArt();
            playerFrag.updateTags();
            playerFrag.setMaxDuration(musicSvc.getDuration());
        } else if (miniPlayerFragment.isVisible()) {
            miniPlayerFragment.updateTags();
            songListFragment.refreshList();
        }
    }

    //get state to set play or pause when user click
    private void togglePlayPause() {
        musicSvc.togglePlayPause();
    }

    //when user click each song this will set startindex with position song was click
    // and set song need to play after that will open player and play that song
    @Override
    public void open_song(int position) {
        musicSvc.setStartIndex(position);
        musicSvc.setSong(position);
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.remove(miniPlayerFragment);
        transaction.remove(songListFragment);

        transaction.add(R.id.container, playerFrag);
        transaction.addToBackStack(null);

        transaction.commit();
        musicSvc.playSong();
    }

    //show list song and miniplayer
    public void show_list() {
        manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.remove(playerFrag);
        transaction.add(R.id.container, songListFragment);
        transaction.add(R.id.container, miniPlayerFragment);
        transaction.commit();
    }

    @Override
    public ArrayList<Song> get_song_list() {
        return songFiles;
    }

    @Override
    protected void onDestroy() {
        stopService(playIntent);
        unbindService(musicConnection);
        musicSvc = null;
        super.onDestroy();
    }

    @Override
    public void set_progress(int i) {
        musicSvc.seekTo(i);
    }

    //set volume for gesture
    @Override
    public void set_volume(float diff_vol) {
        float current_vol = musicSvc.getVolume();
        float new_vol = current_vol + diff_vol;
        if (new_vol <= 0.0f)
            new_vol = 0.0f;
        if (new_vol >= 1.0f)
            new_vol = 1.0f;
        Log.d("Volume Diff", String.valueOf(diff_vol));
        String vol_text = "Volume: " + Math.round(new_vol / 1.0f * 100) + "%";
        vol_toast.setText(vol_text);
        vol_toast.show();
        musicSvc.setVolume(new_vol);
    }

    //move user to main player
    @Override
    public void goToPlayer() {
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.remove(miniPlayerFragment);
        transaction.remove(songListFragment);
        transaction.add(R.id.container, playerFrag);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public String get_artist() {
        return songFiles.get(musicSvc.playingIndex()).getArtist();
    }

    @Override
    public String get_album() {
        return songFiles.get(musicSvc.playingIndex()).getAlbum();
    }

    @Override
    public String get_title() {
        return songFiles.get(musicSvc.playingIndex()).getTitle();
    }

    @Override
    public byte[] get_album_art() {
        return songFiles.get(musicSvc.playingIndex()).getAlbum_Art(getBaseContext());
    }

    @Override
    public byte[] getSpecialArt(int i) {
        return songFiles.get(i).getAlbum_Art(getBaseContext());
    }

    @Override
    public int get_song_id() {
        return musicSvc.playingIndex();
    }

    @Override
    public int get_duration() {
        return musicSvc.getDuration();
    }

    @Override
    public int get_elapsed() {
        return musicSvc.getElapsed();
    }

    @Override
    public boolean is_playing() {
        return musicSvc.isPlaying();
    }

    //will prepare, set infor for the new song if the current song is play done
    @Override
    protected void onResume() {
        super.onResume();
        if (songCompletedListener == null) {
            songCompletedListener = new SongCompletedListener();
        }
        IntentFilter intentFilter = new IntentFilter("Refresh the Song Info");
        //register with system that they was listen event changed
        registerReceiver(songCompletedListener, intentFilter);
        updateSongInfo();
    }

    //if the song is pause this function will pause all other if
    // it involve with song complete listener
    @Override
    protected void onPause() {
        if (songCompletedListener != null) unregisterReceiver(songCompletedListener);
        super.onPause();
    }

    //create class to receive info of another source
    //this function will receive info of service send to it through broadcastreceiver
    private class SongCompletedListener extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("Refresh the Song Info")) {
                updateSongInfo();
            }
        }
    }
}
