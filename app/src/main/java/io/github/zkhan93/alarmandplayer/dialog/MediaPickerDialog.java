package io.github.zkhan93.alarmandplayer.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.zkhan93.alarmandplayer.R;
import io.github.zkhan93.alarmandplayer.adapter.SoundListAdapter;
import io.github.zkhan93.alarmandplayer.adapter.model.Sound;
import io.github.zkhan93.alarmandplayer.viewholder.SoundVH;

public class MediaPickerDialog extends DialogFragment {
    public static final String TAG = MediaPickerDialog.class.getSimpleName();

    @BindView(R.id.sound_list)
    public RecyclerView soundList;

    private SharedPreferences sharedPreferences;
    private List<Sound> mediaNameList;
    private SoundVH.ItemInteractionListener itemInteractionListener;

    {
        itemInteractionListener = new SoundVH.ItemInteractionListener() {
            @Override
            public void onSelect(Sound sound) {
                if (sound != null && sound.path != null)
                    sharedPreferences.edit().putString(getString(R.string.pref_setting_alarmsound_key), sound.path).apply();
                dismiss();
            }
        };
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        View view = LayoutInflater.from(getContext()).inflate(R.layout.media_picker_dialog, null);
        ButterKnife.bind(this, view);

        collectMediaList();

        soundList.setAdapter(new SoundListAdapter(mediaNameList, itemInteractionListener));
        return new AlertDialog.Builder(new ContextThemeWrapper(getActivity(),
                R.style.Dialog_FullWidth))
                .setView(view)
                .create();
    }

    private void collectMediaList() {
        ContentResolver cr = getActivity().getContentResolver();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        Cursor cur = cr.query(uri, null, selection, null, sortOrder);
        int count = 0;
        if (mediaNameList == null)
            mediaNameList = new ArrayList<>();
        else
            mediaNameList.clear();
        if (cur != null) {
            count = cur.getCount();
            int titleColumn = cur.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int pathColumn = cur.getColumnIndex(MediaStore.Audio.Media.DATA);
            if (count > 0) {
                while (cur.moveToNext()) {
                    String data = cur.getString(titleColumn);
                    String path = cur.getString(pathColumn);
                    mediaNameList.add(new Sound(data, path));
                    // Add code to get more column here
                    Log.d(TAG, data);
                    // Save to your list here
                }
            }
            cur.close();
        }

    }
}
