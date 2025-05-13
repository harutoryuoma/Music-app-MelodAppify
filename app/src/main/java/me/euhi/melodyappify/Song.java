package me.euhi.melodyappify;

import android.net.Uri;

import java.io.Serializable;

public class Song implements Serializable {
    private String title;
    private Uri uri;
    private String artist;
    private String data;
    private Uri albumArt;
    private String albumId;
    private String Id;

    public Song(String title, Uri uri, String artist, String data, Uri albumArt, String albumId,String Id) {
        this.title = title;
        this.uri = uri;
        this.artist = artist;
        this.data = data;
        this.albumArt = albumArt;
        this.albumId = albumId;
        this.Id=Id;
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getAlbumId() {
        return albumId;
    }

    public void setAlbumId(String albumId) {
        this.albumId = albumId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Uri getAlbumArt() {
        return albumArt;
    }

    public void setAlbumArt(Uri albumArt) {
        this.albumArt = albumArt;
    }
}
