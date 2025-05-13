package me.euhi.melodyappify;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.RecoverableSecurityException;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int REQUEST_CODE_DELETE = 1001;
    List<Song> songList;
    Context context;
    ExoPlayer player;
    ConstraintLayout playerView;

    public SongAdapter(List<Song> songList, Context context, ExoPlayer player, ConstraintLayout playerView) {
        this.songList = songList;
        this.context = context;
        this.player = player;
        this.playerView = playerView;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_song, parent, false);
        return new ViewHolder(view);
    }

    @OptIn(markerClass = UnstableApi.class)
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Song song = songList.get(position);
        ViewHolder viewHolder = (ViewHolder) holder;
        //Set value to view
        viewHolder.titleTextView.setText(song.getTitle());
        viewHolder.artistTextView.setText(song.getArtist());
        //artwork set
        Uri artworkUri = song.getAlbumArt();
        if(artworkUri != null){
            viewHolder.iconImageView.setImageURI(artworkUri);

            if(viewHolder.iconImageView.getDrawable() == null){
                viewHolder.iconImageView.setImageResource(R.drawable.ic_launcher_foreground);
            }
        }

        //play on item click
        viewHolder.itemView.setOnClickListener(view ->{
            //start the player service
            context.startService(new Intent(context.getApplicationContext(), PlayerService.class));

            //show player view
            playerView.setVisibility(View.VISIBLE);

            //playing the song
            if(!player.isPlaying()){
                player.setMediaItems(getMediaItems(), position, 0);
            }else{
                player.pause();
                player.seekTo(position, 0);
            }
            //prepare and play
            player.prepare();
            player.play();
            Toast.makeText(context, song.getTitle(), Toast.LENGTH_SHORT).show();

        });
        viewHolder.button.setOnClickListener(view -> {
            PopupMenu p=new PopupMenu(context,view);
            p.getMenuInflater().inflate(R.menu.options_menu,p.getMenu());
            p.show();
            p.setOnMenuItemClickListener(item -> {
               if(item.getItemId()==R.id.delete){
                   Toast.makeText(context,"Delete Successfully!!",Toast.LENGTH_SHORT).show();
                   if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                       deleteSong(position);
                   }
               }
                return true;
            });
        });
    }
    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void deleteSong(int position) {
        Song songData = songList.get(position);
        Uri songUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, Long.parseLong(songData.getId()));

        try {
            // Attempt to delete the song
            int deletedRows = context.getContentResolver().delete(songUri, null, null);

            if (deletedRows > 0) {
                // Remove song from list and notify adapter
                songList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, songList.size());
                Toast.makeText(context, "Deleted successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Failed to delete!", Toast.LENGTH_SHORT).show();
            }
        } catch (RecoverableSecurityException e) {
            // Handle RecoverableSecurityException for Android 10+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                IntentSender intentSender = e.getUserAction().getActionIntent().getIntentSender();
                // Check if context is an instance of Activity
                if (context instanceof Activity) {
                    try {
                        ((Activity) context).startIntentSenderForResult(
                                intentSender,
                                REQUEST_CODE_DELETE,
                                null,
                                0,
                                0,
                                0
                        );
                    } catch (IntentSender.SendIntentException ex) {
                        ex.printStackTrace();
                    }
                } else {
                    Toast.makeText(context, "Cannot delete: Not an Activity context", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    private List<MediaItem> getMediaItems() {
        //define a list of media items
        List<MediaItem> mediaItems = new ArrayList<>();

        for(Song song : songList){
            MediaItem mediaItem = new MediaItem.Builder()
                    .setUri(song.getUri())
                    .setMediaMetadata(getMetadata(song))
                    .build();

            //add the media item to media item list
            mediaItems.add(mediaItem);
        }
        return mediaItems;
    }

    private MediaMetadata getMetadata(Song song) {
        return new MediaMetadata.Builder()
                .setTitle(song.getTitle())
                .setArtworkUri(song.getAlbumArt())
                .build();
    }


    @Override
    public int getItemCount() {
        return songList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView artistTextView;
        ImageView iconImageView;
        ImageView button;
        public ViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.music_title_text);
            artistTextView = itemView.findViewById(R.id.music_artist_text);
            iconImageView = itemView.findViewById(R.id.icon_view);
            button=itemView.findViewById(R.id.button);
        }
    }

    //filter and search in this path
}
