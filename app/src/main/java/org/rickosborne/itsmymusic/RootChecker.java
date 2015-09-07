package org.rickosborne.itsmymusic;

import android.os.AsyncTask;

import eu.chainfire.libsuperuser.Shell;

public class RootChecker extends AsyncTask<RootChecker.Handler, Void, Void> {
  interface Handler {
    void onRootCheck(boolean gotRoot);
  }

  @Override
  protected Void doInBackground(Handler... handlers) {
    for (Handler handler : handlers) {
      handler.onRootCheck(Shell.SU.available());
    }
    return null;
  }
}
