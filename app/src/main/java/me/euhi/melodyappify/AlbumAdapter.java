package me.euhi.melodyappify;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.media3.common.MediaMetadata;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.ViewHolder> {
private final Context context;
private final List<Song> albumfiles;

    public AlbumAdapter(Context context, List<Song> albumfiles){
        this.context=context;
        this.albumfiles=albumfiles;
    }

    @NonNull
    public AlbumAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.album_item, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull AlbumAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Song songData = albumfiles.get(position);
        //Set value to view
        holder.album_name.setText(songData.getTitle());
        //artwork set
        Uri artworkUri = songData.getAlbumArt();
        if(artworkUri != null){
            holder.album_image.setImageURI(artworkUri);
            if(holder.album_image.getDrawable() == null){
                holder.album_image.setImageResource(R.drawable.ic_launcher_foreground);
            }
        }
        holder.itemView.setOnClickListener(view -> {
            Intent i=new Intent(context, AlbumDetails.class);
            i.putExtra("albumName",albumfiles.get(position).getTitle());
            context.startActivity(i);
        });
    }
    private MediaMetadata getMetadata(Song song) {
        return new MediaMetadata.Builder()
                .setTitle(song.getTitle())
                .setArtworkUri(song.getAlbumArt())
                .build();
    }
    @Override
    public int getItemCount() {
        return albumfiles.size();
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView album_image;
        TextView album_name;


        public ViewHolder(View itemView) {
            super(itemView);
            album_image = itemView.findViewById(R.id.album_image);
            album_name = itemView.findViewById(R.id.album_name);

        }
    }
}
