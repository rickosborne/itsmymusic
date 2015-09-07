package org.rickosborne.itsmymusic;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.LinkedList;

public class MainActivityFragment extends Fragment {
  private WeakReference<MusicFixer> musicFixer;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_main, container, false);
  }

  public void abortFixes() {
    if (musicFixer == null) return;
    MusicFixer fixer = musicFixer.get();
    if (fixer == null) return;
    fixer.stop();
    musicFixer.clear();
  }

  public MusicFixer newFixer(Collection<Song> songs) {
    MusicFixer fixer = new MusicFixer(songs);
    musicFixer = new WeakReference<MusicFixer>(fixer);
    return fixer;
  }

  @Override
  public void onPause() {
    super.onPause();
    abortFixes();
  }

  @Override
  public void onStop() {
    super.onStop();
    abortFixes();
  }

  protected void logAndToast(String message) {
    Log.d(MainActivityFragment.class.getSimpleName(), message);
    Toast.makeText(MainActivityFragment.this.getActivity(), message, Toast.LENGTH_LONG).show();
  }

  @Override
  public void onViewCreated(final View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    final TextView findMusic = (TextView) view.findViewById(R.id.findMusic);
    final TextView fixMusic = (TextView) view.findViewById(R.id.fixMusic);
    final TextView getRoot = (TextView) view.findViewById(R.id.getRoot);
    final Button stopFixing = (Button) view.findViewById(R.id.stopFixing);
    findMusic.setEnabled(false);
    fixMusic.setEnabled(false);
    getRoot.setEnabled(true);
    stopFixing.setEnabled(false);
    final LinkedList<Song> songs = new LinkedList<Song>();
    final TextView songCount = (TextView) view.findViewById(R.id.songCount);
    final TextView artistValue = (TextView) view.findViewById(R.id.artistValue);
    final TextView albumValue = (TextView) view.findViewById(R.id.albumValue);
    final TextView yearValue = (TextView) view.findViewById(R.id.yearValue);
    final TextView titleValue = (TextView) view.findViewById(R.id.titleValue);
    final TextView trackValue = (TextView) view.findViewById(R.id.trackValue);
    final TextView fileValue = (TextView) view.findViewById(R.id.fileValue);
    final ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
    final TableLayout songDetails = (TableLayout) view.findViewById(R.id.songDetails);
    final ProgressBar gettingRoot = (ProgressBar) view.findViewById(R.id.gettingRoot);

    class Helper {
      public void showSong(Song song) {
        if (song == null) {
          songDetails.setVisibility(View.INVISIBLE);
          artistValue.setText("");
          albumValue.setText("");
          yearValue.setText("");
          titleValue.setText("");
          trackValue.setText("");
          fileValue.setText("");
        }
        else {
          artistValue.setText(song.artist);
          albumValue.setText(song.album);
          yearValue.setText(song.year == null || song.year == 0 ? "" : song.year.toString());
          titleValue.setText(song.title);
          trackValue.setText(song.track == null || song.track == 0 ? "" : song.track.toString());
          fileValue.setText(song.path);
        }
      }

      public void startSongs(int count) {
        if (progressBar != null) {
          progressBar.setMax(count);
          progressBar.setProgress(0);
          songDetails.setVisibility(View.VISIBLE);
        }
      }

      public void nextSong() {
        if (progressBar != null) progressBar.setProgress(progressBar.getProgress() + 1);
      }

      public Runnable nextSonger = new Runnable() {
        @Override
        public void run() {
          nextSong();
        }
      };
    }
    final Helper helper = new Helper();

    getRoot.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        gettingRoot.setVisibility(View.VISIBLE);
        gettingRoot.setProgress(1);
        gettingRoot.setMax(2);
        (new RootChecker()).execute(new RootChecker.Handler() {
          @Override
          public void onRootCheck(final boolean rootAvailable) {
            Activity activity = getActivity();
            if (activity == null) return;
            activity.runOnUiThread(new Runnable() {
              @Override
              public void run() {
                findMusic.setEnabled(rootAvailable);
                getRoot.setEnabled(!rootAvailable);
                gettingRoot.setProgress(2);
                gettingRoot.setVisibility(View.INVISIBLE);
                if (!rootAvailable) {
                  logAndToast("Root not available");
                }
              }
            });
          }
        });
      }
    });

    findMusic.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        (new MusicScanner()).execute(new MusicScanner.Handler() {
          @Override
          public void onScanComplete(final Collection<Song> result) {
            Activity activity = getActivity();
            if (activity == null) return;
            activity.runOnUiThread(new Runnable() {
              @Override
              public void run() {
                boolean canProceed = (result != null) && (result.size() > 0);
                fixMusic.setEnabled(canProceed);
                findMusic.setEnabled(!canProceed);
                if (canProceed) {
                  songs.addAll(result);
                  songCount.setText(String.format("%d song%s", result.size(), result.size() == 1 ? "" : "s"));
                  logAndToast("Scan success");
                } else {
                  logAndToast("Scan empty");
                }
              }
            });
          }
        });
      }
    });

    fixMusic.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        newFixer(songs).execute(new MusicFixer.Handler() {
          @Override
          public void onStart() {
            Activity activity = getActivity();
            if (activity == null) return;
            activity.runOnUiThread(new Runnable() {
              @Override
              public void run() {
                helper.startSongs(songs.size());
                stopFixing.setEnabled(true);
                stopFixing.setVisibility(View.VISIBLE);
                fixMusic.setEnabled(false);
              }
            });
          }

          @Override
          public void onStartFixing(final Song song) {
            Activity activity = getActivity();
            if (activity == null) return;
            activity.runOnUiThread(new Runnable() {
              @Override
              public void run() {
                helper.showSong(song);
              }
            });
          }

          @Override
          public void onDoneFixing(final Song song, final boolean success) {
            Activity activity = getActivity();
            if (activity == null) return;
            activity.runOnUiThread(helper.nextSonger);
          }

          @Override
          public void onComplete() {
            Activity activity = getActivity();
            if (activity == null) return;
            activity.runOnUiThread(new Runnable() {
              @Override
              public void run() {
                helper.showSong(null);
                stopFixing.setEnabled(false);
                stopFixing.setVisibility(View.INVISIBLE);
                fixMusic.setEnabled(true);
                logAndToast("Fix finished");
              }
            });
          }
        });
      }
    });

    stopFixing.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Activity activity = getActivity();
        if (activity == null) return;
        activity.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            abortFixes();
            stopFixing.setEnabled(false);
          }
        });
      }
    });
  }
}
