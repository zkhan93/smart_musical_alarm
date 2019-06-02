package io.github.zkhan93.alarmandplayer.job;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.File;
import java.lang.ref.WeakReference;

import io.github.zkhan93.alarmandplayer.R;

public class AlarmJob extends BroadcastReceiver {
    public static final String TAG = AlarmJob.class.getSimpleName();
    private AudioManager audioManager;
    private MediaPlayer mediaPlayer;
    private SharedPreferences sharedPreferences;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "bingo!!!");
        if (intent.getAction().equals("com.google.android.things.RING_ALARM")) {
            Log.d(TAG, "alarm will ring now");
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            startRinging(context);
            Stopper stopper = new Stopper(10, mediaPlayer);
            stopper.execute();
        }
    }

  /*  @Override
    public boolean onStartJob(final JobParameters params) {
        Log.d(TAG, "alarm will ring now");
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        startRinging();
        Stopper stopper = new Stopper(10, mediaPlayer, this, params);
        stopper.execute();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG, "alarm requested to be stopped by system");
        if (mediaPlayer != null && mediaPlayer.isPlaying())
            mediaPlayer.stop();
        return true;
    }*/

    private void setFullVolume() {
        int amStreamMusicMaxVol = audioManager.getStreamMaxVolume(audioManager.STREAM_MUSIC);
        audioManager.setStreamVolume(audioManager.STREAM_MUSIC, amStreamMusicMaxVol, 0);
    }

    private void startRinging(Context context) {
        setFullVolume();
        String path =
                sharedPreferences.getString(context.getString(R.string.pref_setting_alarmsound_key)
                        , null);
        Log.d(TAG, path);
        if (path != null)
            mediaPlayer = MediaPlayer.create(context, Uri.fromFile(new File(path)));
        else
            mediaPlayer = MediaPlayer.create(context, R.raw.alarm);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

    }

    static class Stopper extends AsyncTask<Void, Void, Void> {
        private int millisec;
        private WeakReference<MediaPlayer> mediaPlayerWeakReference;
//        private WeakReference<AlarmJob> alarmJobWeakReference;
//        private JobParameters params;

        public Stopper(int mins, MediaPlayer mediaPlayer) {
            this.millisec = mins * 60 * 1000;
            mediaPlayerWeakReference = new WeakReference<>(mediaPlayer);
//            alarmJobWeakReference = new WeakReference<>(alarmJob);
//            this.params = params;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            MediaPlayer mediaPlayer = mediaPlayerWeakReference.get();
            if (mediaPlayer != null && mediaPlayer.isPlaying())
                mediaPlayer.stop();
//            AlarmJob alarmJob = alarmJobWeakReference.get();
//            if (alarmJob != null) {
////                alarmJob.jobFinished(params, false);
//            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Thread.sleep(millisec);
            } catch (InterruptedException ex) {
                Log.e(TAG, "sleep interrupted");
            }
            return null;
        }
    }
}
