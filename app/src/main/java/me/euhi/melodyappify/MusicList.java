package me.euhi.melodyappify;

import android.Manifest;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
public class MusicList extends AppCompatActivity {

    RecyclerView recyclerView;
    SongAdapter musicListAdapter;
    ActivityResultLauncher<String[]> mPermissionLauncher;
    private boolean isReadPermissionGranted = false;
    private boolean isNotificationPermissionGranted = false;
    static ArrayList<Song> songsList = new ArrayList<>();
    ExoPlayer player;
    ConstraintLayout playerView;
    TextView playerCloseBtn;
    //control
    TextView songNameView;
    //TextView skipPreBtn, skipNextBtn, playPauseBtn ,repeatModeBtn, playlistBtn;
    TextView homeSongNameView, homeSkipPreBtn, homePlayPauseBtn, homeSkipNextBtn;
    //wrapper
    ConstraintLayout homeControlWrapper, controlWrapper;
    //ConstraintLayout headWrapper, artWorkWrapper, seekBarWrapper;
    //artwork

    //SeekBar
    //SeekBar seekBar;
    //status bar and navigation color
    int defaultStatusColor;
    //repeat mode
    //int repeatMode = 1; //repeat all = 1; repeat one = 2; shuffle all = 3
    //
    boolean isBound = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.music_list);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(item -> {
             if (item.getItemId() == R.id.library) {
                Intent homeIntent = new Intent(MusicList.this, PlayerList.class);
                startActivity(homeIntent);
            }

            return true;
        });
        setSupportActionBar(findViewById(R.id.toolbar));
        //save the status color
        defaultStatusColor= getWindow().getStatusBarColor();
        //set navigator color
        getWindow().setNavigationBarColor(ColorUtils.setAlphaComponent(defaultStatusColor,199));
        //set toolbar and app title

        //recycle view
        recyclerView = findViewById(R.id.recycle_view);

        mPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), o -> {
            if(o.get(Manifest.permission.READ_MEDIA_AUDIO)!= null){
                isReadPermissionGranted = Boolean.TRUE.equals(o.get(Manifest.permission.READ_MEDIA_AUDIO));
            }

            if(o.get(Manifest.permission.POST_NOTIFICATIONS) != null){
                isNotificationPermissionGranted = Boolean.TRUE.equals(o.get(Manifest.permission.POST_NOTIFICATIONS));
            }
            fetchSong();
        });

        //view
        playerView = findViewById(R.id.playerView);
        playerCloseBtn = findViewById(R.id.playerCloseBtn);
        songNameView = findViewById(R.id.songNameView);
/*
        skipPreBtn = findViewById(R.id);
        skipNextBtn = findViewById(R.id);
        playPauseBtn = findViewById(R.id);
        repeatModeBtn = findViewById(R.id);
        playlistBtn = findViewById(R.id);
*/

        homeSongNameView = findViewById(R.id.homeSongNameView);
        homeSkipPreBtn = findViewById(R.id.homeSkipPreBtn);
        homePlayPauseBtn = findViewById(R.id.homePlayPauseBtn);
        homeSkipNextBtn = findViewById(R.id.homeSkipNextBtn);

        //wrapper
        homeControlWrapper = findViewById(R.id.homeControlWrapper);
//        headWrapper = findViewById(R.id.);
//        artWorkWrapper = findViewById(R.id.);
//        seekBarWrapper = findViewById(R.id.);
        controlWrapper = findViewById(R.id.controlWrapper);

        //artwork

        //bind to control service
        requestPermission();

        doBindService();
    }

    @OptIn(markerClass = UnstableApi.class)
    private void doBindService(){
        Intent playerServiceIntent = new Intent(this, PlayerService.class);
        bindService(playerServiceIntent, playerServiceConnection, Context.BIND_AUTO_CREATE);
        isBound = true;
    }

    ServiceConnection playerServiceConnection = new ServiceConnection() {
        @OptIn(markerClass = UnstableApi.class)
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            //get service instance
            PlayerService.ServiceBinder binder = (PlayerService.ServiceBinder) iBinder;
            player = binder.getPlayerService().player;
            isBound = true;
            //ready to show song
            requestPermission();
            //call player control
            playerControl();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isBound = false;
            player = null;
        }
    };


    private void playerControl() {
        //song name marquee
        songNameView.setSelected(true);
        homeSongNameView.setSelected(true);

        if(!player.isPlaying()){
            homeControlWrapper.setVisibility(View.GONE);
        }

        //exit player view
        playerCloseBtn.setOnClickListener(view -> exitPlayerView());
//        playlistBtn.setOnClickListener(view -> exitPlayerView());
        //open player view on home control wrapper click
        homeControlWrapper.setOnClickListener(view -> showPlayerView());

        //player listener
        player.addListener(new Player.Listener() {
            @Override
            public void onMediaItemTransition(@Nullable MediaItem mediaItem, int reason) {
                Player.Listener.super.onMediaItemTransition(mediaItem, reason);
                //show the song title
                assert mediaItem != null;
                songNameView.setText(mediaItem.mediaMetadata.title);
                homeSongNameView.setText(mediaItem.mediaMetadata.title);
                //other song parameter show setup
                homePlayPauseBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_pause, 0, 0, 0);

                //show current artwork
                showCurrentArtwork();

                //update player view color
                updatePlayerColor();

                if(!player.isPlaying()){
                    player.play();
                    homeControlWrapper.setVisibility(View.VISIBLE);
                    homeControlWrapper.setOnClickListener(view -> showPlayerView());
                }
            }

            @Override
            public void onPlaybackStateChanged(int playbackState) {
                Player.Listener.super.onPlaybackStateChanged(playbackState);
                if(playbackState == ExoPlayer.STATE_READY){
                    //set value to player view
                    songNameView.setText(Objects.requireNonNull(player.getCurrentMediaItem()).mediaMetadata.title);
                    homeSongNameView.setText(Objects.requireNonNull(player.getCurrentMediaItem()).mediaMetadata.title);

                    homePlayPauseBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_pause, 0, 0, 0);
                    //show current artwork
                    showCurrentArtwork();

                    //update player view color
                    updatePlayerColor();
                }else{
                    homePlayPauseBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_play, 0, 0, 0);
                }
            }
        });

        //skip to next track
        homeSkipNextBtn.setOnClickListener(view -> skipToNextSong());
        homeSkipPreBtn.setOnClickListener(view -> skipToPreSong());
//        skipNextBtn.setOnClickListener(view -> skipToNextSong());
//        skipPreBtn.setOnClickListener(view -> skipToPreSong());

        //play pause the player
        homePlayPauseBtn.setOnClickListener(view -> playOrPausePlayer());
//        playPauseBtn.setOnClickListener(view -> playOrPausePlayer());

        //seekbar listener
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = new MenuInflater(this);
        menuInflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        doUnbindService();
    }

    private void doUnbindService() {
        if(isBound){
            unbindService(playerServiceConnection);
            isBound = false;
        }
    }

    private void requestPermission() {
        isReadPermissionGranted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_AUDIO
        ) == PackageManager.PERMISSION_GRANTED;

        isNotificationPermissionGranted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED;

        List<String> permissionRequest = new ArrayList<>();
        if(isNotificationPermissionGranted && isReadPermissionGranted){
            fetchSong();
        }
        if(!isReadPermissionGranted){
            permissionRequest.add(Manifest.permission.READ_MEDIA_AUDIO);
        }
        if(!isNotificationPermissionGranted){
            permissionRequest.add(Manifest.permission.POST_NOTIFICATIONS);
        }
        if(!permissionRequest.isEmpty()){
            mPermissionLauncher.launch(permissionRequest.toArray(new String[0]));
        }
    }

    private void fetchSong() {
        List<Song> songs = new ArrayList<>();
        Uri mediaStoreUri;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            mediaStoreUri = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
        } else {
            mediaStoreUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        }

        //define projection
        String[] projection = new String[]{
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ALBUM_ARTIST,
                MediaStore.Audio.Media.ALBUM_ID,
        };

        try (Cursor cursor = getContentResolver().query(mediaStoreUri, projection, null, null, null)) {
            assert cursor != null;
            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
            int titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME);
            int dataIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
            int artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ARTIST);
            int albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID);

            //clear previous loaded
            while (cursor.moveToNext()) {
                String id = cursor.getString(idColumn);
                String title = cursor.getString(titleColumn);
                String data = cursor.getString(dataIndex);
                String artist = cursor.getString(artistColumn);
                String albumId = cursor.getString(albumIdColumn);

                //song uri
                Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, Long.parseLong(id));

                //artwork uri
                Uri albumArtUri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), Long.parseLong(albumId));

                title = title.substring(0, title.lastIndexOf("."));

                Song song = new Song(title, uri, artist, data, albumArtUri,albumId,id);

                songs.add(song);
            }

            showSong(songs);

        }
    }

    private void showSong(List<Song> songs) {
        if (songs.isEmpty()) {
            Toast.makeText(this, "No Songs", Toast.LENGTH_SHORT).show();
            return;
        }

        songsList.clear();
        songsList.addAll(songs);

        //layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        //song adapter
        musicListAdapter = new SongAdapter(songsList, this, player, playerView);

        recyclerView.setAdapter(musicListAdapter);
    }

    private void playOrPausePlayer() {
        if(player.isPlaying()){
            player.pause();
            homePlayPauseBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_play,0,0,0);
        }else {
            player.play();
            homePlayPauseBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_pause,0,0,0);
        }

        //update player color
        updatePlayerColor();
    }

    private void skipToNextSong() {
        if(player.hasNextMediaItem()){
            player.seekToNext();
        }
    }

    private void skipToPreSong() {
        if(player.hasPreviousMediaItem()){
            player.seekToPrevious();
        }
    }

    private void showCurrentArtwork() {
    }

    private void showPlayerView() {
        playerView.setVisibility(View.VISIBLE);
        updatePlayerColor();
    }

    private void updatePlayerColor() {

    }

    private void exitPlayerView() {
        playerView.setVisibility(View.GONE);
        getWindow().setStatusBarColor(defaultStatusColor);
        getWindow().setNavigationBarColor(ColorUtils.setAlphaComponent(defaultStatusColor,199));
    }

}