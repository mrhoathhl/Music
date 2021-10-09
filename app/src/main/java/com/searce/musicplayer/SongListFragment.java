package com.searce.musicplayer;

import android.app.Fragment;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.PathMeasure;
import android.graphics.Typeface;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.Layout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AlphabetIndexer;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;

public class SongListFragment extends Fragment implements AdapterView.OnItemClickListener {
    ArrayList<Song> songFiles;
    SongListAdapter songAdapter;
    ListView lvSongs;
    ImageView ivListItem;
    Communicator comm;
    int songId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_songlist, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        comm = (Communicator) getActivity();
        lvSongs = (ListView) getActivity().findViewById(R.id.lvSongs);
        ivListItem = (ImageView) getActivity().findViewById(R.id.ivListItem);
        lvSongs.setOnItemClickListener(this);

        songFiles = comm.get_song_list();
        songId = comm.get_song_id();
        songAdapter = new SongListAdapter(songFiles);
        lvSongs.setAdapter(songAdapter);
        lvSongs.setSelectionFromTop(songId, lvSongs.getHeight() / 2);
        Log.e("MP3 files found...", String.valueOf(songFiles.size()));
    }

    //reset list when user choose shuffle mode
    public void refreshList() {
        songId = comm.get_song_id();
        songAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        comm.open_song(i);
    }

    static class ViewHolderItem {
        TextView titleHolder;
        TextView artistHolder;
        TextView durationHolder;
        ImageView imageHolder;
    }

    public class SongListAdapter extends BaseAdapter implements SectionIndexer {
        ArrayList<Song> songs;
        HashMap<String, Integer> mapSectionToPosn;
        SparseArray<String> mapPosnToSection;
        String[] sections;

        SongListAdapter(ArrayList<Song> songs) {
            this.songs = songs;
            mapSectionToPosn = new HashMap<String, Integer>();
            mapPosnToSection = new SparseArray<String>();
            for (int i = 0; i < songs.size(); i++) {
                //set uppercase for first letter of song name
                String firstchar = songs.get(i).getTitle().substring(0, 1);
                firstchar = firstchar.toUpperCase(Locale.US);
                if (firstchar.matches("[0-9]"))
                    firstchar = "#";
                if (!mapSectionToPosn.containsKey(firstchar))
                    mapSectionToPosn.put(firstchar, i);
                mapPosnToSection.put(i, firstchar);
            }
            Set<String> sectionLetters = mapSectionToPosn.keySet();
            ArrayList<String> sectionList = new ArrayList<String>(sectionLetters);

            Collections.sort(sectionList);

            sections = new String[sectionList.size()];

            sectionList.toArray(sections);
        }


        @Override
        public int getCount() {
            return songs.size();
        }

        @Override
        public Object getItem(int i) {
            return songs.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }


        //set content for detail of each song
        //tilte, name, artist, image
        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolderItem viewHolder;
            if (view == null) {
                // Inflate the view
                LayoutInflater inflater = getActivity().getLayoutInflater();
                view = inflater.inflate(R.layout.list_item_song, viewGroup, false);
                //Set up the View Holder
                viewHolder = new ViewHolderItem();
                viewHolder.imageHolder = (ImageView) view.findViewById(R.id.ivListItem);
                viewHolder.titleHolder = (TextView) view.findViewById(R.id.tvSongTitle_ListItem);
                viewHolder.artistHolder = (TextView) view.findViewById(R.id.tvArtist_ListItem);
                viewHolder.durationHolder = (TextView) view.findViewById(R.id.tvDuration);
                //Store the holder with the view
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolderItem) view.getTag();
            }
            viewHolder.titleHolder.setText(songs.get(i).getTitle());
            viewHolder.artistHolder.setText(songs.get(i).getArtist());
            viewHolder.durationHolder.setText(songs.get(i).getDuration());

            if (songs.get(i).getId() == songFiles.get(songId).getId()) {
                viewHolder.titleHolder.setTextColor(getResources().getColor(R.color.holo));
                viewHolder.artistHolder.setTextColor(getResources().getColor(R.color.holo));
                viewHolder.durationHolder.setTextColor(getResources().getColor(R.color.holo));
            } else {
                viewHolder.titleHolder.setTextColor(getResources().getColor(R.color.white));
                viewHolder.artistHolder.setTextColor(getResources().getColor(R.color.white));
                viewHolder.durationHolder.setTextColor(getResources().getColor(R.color.white));
            }
            byte[] bytes = comm.getSpecialArt(i);
            if (bytes == null) {
                viewHolder.imageHolder.setImageDrawable(getResources().getDrawable(R.drawable.splash));
            }
            else {
                viewHolder.imageHolder.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
            }
            return view;
        }

        @Override
        public String[] getSections() {
            return sections;
        }

        @Override
        public int getPositionForSection(int section) {
            return mapSectionToPosn.get(sections[section]);
        }

        @Override
        public int getSectionForPosition(int position) {
            for (int i = 0; i < sections.length; i++) {
                if (mapPosnToSection.get(position).equals(sections[i])) {
                    return i;
                }
            }
            return 0;
        }
    }
}
