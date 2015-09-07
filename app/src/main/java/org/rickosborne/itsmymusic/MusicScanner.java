package org.rickosborne.itsmymusic;

import android.os.AsyncTask;

import java.util.Collection;
import java.util.LinkedList;

import eu.chainfire.libsuperuser.Shell;

public class MusicScanner extends AsyncTask<MusicScanner.Handler, Void, Void> {
  protected final static String DELIM = "\t\t\t";
  protected final static String DB_PATH = "/data/data/com.google.android.music/databases/music.db";
  protected final static String DB_SQL = "" +
    "SELECT" +
    "  LocalCopyPath, " +
    "  Artist, " +
    "  Album, " +
    "  Title, " +
    "  Year, " +
    "  TrackNumber," +
    "  AlbumArtist," +
    "  Genre," +
    "  TrackCount," +
    "  DiscNumber," +
    "  DiscCount," +
    "  AlbumArtLocation " +
    "FROM music " +
    "WHERE (COALESCE(LocalCopyPath,'') <> '')";
  protected final static String QUERY = String.format("sqlite3 -separator \"%s\" \"%s\" \"%s\"", DELIM, DB_PATH, DB_SQL);

  interface Handler {
    void onScanComplete(Collection<Song> result);
  }

  @Override
  protected Void doInBackground(Handler... handlers) {
    final LinkedList<Song> songs = new LinkedList<Song>();
    for (String line : Shell.SU.run(QUERY)) {
      songs.add(new Song(line, DELIM));
    }
    for (Handler handler : handlers) {
      handler.onScanComplete(songs);
    }
    return null;
  }
}
