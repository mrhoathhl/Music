package com.searce.musicplayer;

import android.content.ContentUris;
import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;

import java.io.Serializable;
import java.text.SimpleDateFormat;

public class Song implements Serializable, Comparable<Song> {
    private long id;
    private String file_name;
    private String title;
    private String artist;
    private String album;
    private String duration;

    public String getFile_Name() {
        return file_name;
    }

    public Song(long id, String title, String file_name, String artist, String album, String duration) {
        this.id = id;
        this.title = title;
        this.file_name = file_name;
        this.artist = artist;
        this.album = album;
        this.duration = duration;
    }

    //set lenght of song
    public String getDuration() {
        String songDuration;
        SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss");
        songDuration = dateFormat.format(Integer.valueOf(duration));
        return songDuration;
    }

    public long getId() {
        return id;
    }

    //set title of song
    //if song without title it will be replace with 4 first character of their id
    public String getTitle() {
        if (title.equals("") || title == null) {
            return file_name.substring(0, file_name.length() - 4);
        }
        return title;
    }

    //set artist name
    //if unknown artist it will instead with default name
    public String getArtist() {
        if (artist.equals("<unknown>")) {
            return "Unknown Artist";
        }
        return artist;
    }

    public String getAlbum() {
        return album;
    }

    //get avartar of song
    public byte[] getAlbum_Art(Context context) {
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(context, getUri());
        return mediaMetadataRetriever.getEmbeddedPicture();
    }

    public Uri getUri() {
        return ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
    }

    @Override
    public int compareTo(Song song) {
        return this.title.toUpperCase().compareTo(song.getTitle().toUpperCase());
    }
}
