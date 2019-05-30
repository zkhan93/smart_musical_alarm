package io.github.zkhan93.alarmandplayer.job;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.util.Log;

import java.lang.ref.WeakReference;

import io.github.zkhan93.alarmandplayer.R;

public class AlarmJob extends JobService {
    public static final String TAG = AlarmJob.class.getSimpleName();
    private AudioManager audioManager;
    private MediaPlayer mediaPlayer;

    @Override
    public boolean onStartJob(final JobParameters params) {
        Log.d(TAG, "alarm will ring now");
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
    }

    private void setFullVolume() {
        int amStreamMusicMaxVol = audioManager.getStreamMaxVolume(audioManager.STREAM_MUSIC);
        audioManager.setStreamVolume(audioManager.STREAM_MUSIC, amStreamMusicMaxVol, 0);
    }

    private void startRinging() {
        setFullVolume();
        mediaPlayer = MediaPlayer.create(this, R.raw.alarm);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

    }

    static class Stopper extends AsyncTask<Void, Void, Void> {
        private int millisec;
        private WeakReference<MediaPlayer> mediaPlayerWeakReference;
        private WeakReference<AlarmJob> alarmJobWeakReference;
        private JobParameters params;

        public Stopper(int mins, MediaPlayer mediaPlayer, AlarmJob alarmJob, JobParameters params) {
            this.millisec = mins * 60 * 1000;
            mediaPlayerWeakReference = new WeakReference<>(mediaPlayer);
            alarmJobWeakReference = new WeakReference<>(alarmJob);
            this.params = params;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            MediaPlayer mediaPlayer = mediaPlayerWeakReference.get();
            if (mediaPlayer != null && mediaPlayer.isPlaying())
                mediaPlayer.stop();
            AlarmJob alarmJob = alarmJobWeakReference.get();
            if (alarmJob != null) {
                alarmJob.jobFinished(params, false);
            }
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
