package org.rickosborne.itsmymusic;

import android.os.AsyncTask;
import android.util.Log;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldDataInvalidException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.TagField;
import org.jaudiotagger.tag.datatype.Artwork;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MusicFixer extends AsyncTask<MusicFixer.Handler, Void, Void> {
  protected final static String LOG_NAME = MusicFixer.class.getSimpleName();
  protected final static String MUSIC_PATH = "/data/data/com.google.android.music/files/music/";
  protected boolean keepGoing = true;

  interface Handler {
    void onStart();
    void onStartFixing(Song song);
    void onDoneFixing(Song song, boolean success);
    void onComplete();
  }

  protected Collection<Song> songs;

  public MusicFixer(Collection<Song> songs) {
    this.songs = songs;
  }

  public void stop() {
    keepGoing = false;
  }

  public void update(Map<String, String> changes, Tag tag, FieldKey key, String value) {
    if ((value == null) || value.isEmpty()) return;
    try {
      String currentValue = tag.getFirst(key);
      if (currentValue != null && currentValue.equals(value)) return;
      tag.setField(key, value);
      changes.put(key.toString(), value);
    } catch (NullPointerException e) {
      e.printStackTrace();
    } catch (FieldDataInvalidException e) {
      e.printStackTrace();
    }
  }

  public void update(Map<String, String> changes, Tag tag, FieldKey key, Integer value) {
    if ((value != null) && (value > 0)) update(changes, tag, key, value.toString());
  }

  public void update(Map<String, String> changes, Tag tag, URL value) {
    if (value == null) return;
    try {
      Artwork artwork = new Artwork();
      artwork.setImageUrl(value.toString());
      artwork.setLinked(true);
      tag.setField(artwork);
//      changes.put("ARTWORK", value.toString());
    } catch (FieldDataInvalidException e) {
      e.printStackTrace();
    }
  }

  @Override
  protected Void doInBackground(Handler... handlers) {
    keepGoing = true;
    String musicPath = MUSIC_PATH;
    try {
      musicPath = (new File(musicPath)).getCanonicalPath();
    } catch (IOException e) {
      e.printStackTrace();
    }
    for (Handler handler : handlers) {
      handler.onStart();
    }
    for (Song song : songs) {
      if (!keepGoing) break;
      boolean success = false;
      for (Handler handler : handlers) {
        handler.onStartFixing(song);
      }
      if ((song.path != null) && !song.path.isEmpty()) {
        File file = new File(musicPath, song.path);
        if (file.exists() && file.canWrite()) {
          try {
            AudioFile audioFile = AudioFileIO.read(file);
            Tag tag = audioFile.getTagOrCreateAndSetDefault();
            Map<String, String> changes = new HashMap<String, String>();
            update(changes, tag, FieldKey.ARTIST, song.artist);
            update(changes, tag, FieldKey.ALBUM, song.album);
            update(changes, tag, FieldKey.ALBUM_ARTIST, song.albumArtist);
            update(changes, tag, FieldKey.TITLE, song.title);
            update(changes, tag, FieldKey.YEAR, song.year);
            update(changes, tag, FieldKey.GENRE, song.genre);
            update(changes, tag, FieldKey.TRACK, song.track);
            update(changes, tag, FieldKey.TRACK_TOTAL, song.trackCount);
            update(changes, tag, FieldKey.DISC_NO, song.discNumber);
            update(changes, tag, FieldKey.DISC_TOTAL, song.discCount);
            update(changes, tag, song.albumArtUrl);
            Log.d(LOG_NAME, String.format("Updating song %s: %s", song.path, changes));
            if (!changes.isEmpty()) audioFile.commit();
            success = true;
          } catch (CannotReadException e) {
            e.printStackTrace();
          } catch (IOException e) {
            e.printStackTrace();
          } catch (TagException e) {
            e.printStackTrace();
          } catch (ReadOnlyFileException e) {
            e.printStackTrace();
          } catch (InvalidAudioFrameException e) {
            e.printStackTrace();
          } catch (CannotWriteException e) {
            e.printStackTrace();
          }
        }
      }
      for (Handler handler : handlers) {
        handler.onDoneFixing(song, success);
      }
    }
    for (Handler handler : handlers) {
      handler.onComplete();
    }
    return null;
  }
}
