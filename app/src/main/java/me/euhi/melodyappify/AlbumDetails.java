package me.euhi.melodyappify;

import static me.euhi.melodyappify.MusicList.songsList;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.ColorUtils;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class AlbumDetails extends AppCompatActivity {
    RecyclerView recyclerView;
    String alName;
    ImageView img;
    ArrayList<Song> songs=new ArrayList<>();
    SongAdapter albumAdapter;
    ExoPlayer player;
    ConstraintLayout playerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.album_details);
        recyclerView=findViewById(R.id.recyclerView);
        img=findViewById(R.id.albumphoto);
        playerView=findViewById(R.id.playerView);
        alName=getIntent().getStringExtra("albumName");
        int j=0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            for(int i=0;i<songsList.size();i++){
                assert alName != null;
                if(alName.equals(songsList.get(i).getTitle())){
                    songs.add(j,songsList.get(i));
                    img.setImageURI(songs.get(i).getAlbumArt());
                    j++;
                }
            }
        }
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        player = new ExoPlayer.Builder(this).build();
        //song adapter

        albumAdapter = new SongAdapter(songs, this, player,playerView);

        recyclerView.setAdapter(albumAdapter);
    }

    private void exitPlayerView() {
        playerView.setVisibility(View.GONE);
    }
}