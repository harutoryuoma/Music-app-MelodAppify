package me.euhi.melodyappify;

import static me.euhi.melodyappify.MusicList.songsList;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;


public class PlayerList extends AppCompatActivity {
RecyclerView recyclerView;
AlbumAdapter albumAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.album_list);
        recyclerView=findViewById(R.id.recycleView);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.home) {
                Intent homeIntent = new Intent(PlayerList.this, MusicList.class);
                startActivity(homeIntent);
            }
            return true;
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            albumAdapter=new AlbumAdapter(this,songsList);
        }
        recyclerView.setAdapter(albumAdapter);
                recyclerView.setLayoutManager(new GridLayoutManager(this,2));

            }
        }




