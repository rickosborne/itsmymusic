package org.rickosborne.itsmymusic;

import java.net.URL;

public class Song {

  public String path;
  public String artist;
  public String album;
  public String title;
  public Integer year;
  public Integer track;
  public String albumArtist;
  public String genre;
  public Integer trackCount;
  public Integer discNumber;
  public Integer discCount;
  public URL albumArtUrl;

  public Song(String csv, String delim) {
    DelimitedLine line = new DelimitedLine(csv, delim);
    this.path = line.next();
    this.artist = line.next();
    this.album = line.next();
    this.title = line.next();
    this.year = line.nextInt();
    this.track = line.nextInt();
    this.albumArtist = line.next();
    this.genre = line.next();
    this.trackCount = line.nextInt();
    this.discNumber = line.nextInt();
    this.discCount = line.nextInt();
    this.albumArtUrl = line.nextUrl();
  }
}
