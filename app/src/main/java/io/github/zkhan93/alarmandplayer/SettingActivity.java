package io.github.zkhan93.alarmandplayer;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.zkhan93.alarmandplayer.dialog.BaseDialogFragment;
import io.github.zkhan93.alarmandplayer.dialog.MediaPickerDialog;

public class SettingActivity extends AppCompatActivity {
    public static final String TAG = SettingActivity.class.getSimpleName();

    @BindView(R.id.back)
    public View back;

    @BindView(R.id.setting_ambient)
    public View settingAmbient;

    @BindView(R.id.setting_ambient_desc)
    public TextView settingAmbientDesc;

    @BindView(R.id.setting_location)
    public View settingLocation;

    @BindView(R.id.setting_location_desc)
    public TextView settingLocationDesc;

    @BindView(R.id.setting_alarm_sound)
    public View settingAlarmSound;

    @BindView(R.id.setting_alarm_sound_desc)
    public TextView settingAlarmSoundDesc;


    private SharedPreferences sharedPreferences;
    private View.OnClickListener clicksListener;
    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener;
    private final int PERMISSION_REQUEST_FOR_ALARM_SOUND = 1;

    {
        clicksListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.back:
                        onBackPressed();
                        break;
                    case R.id.setting_ambient:
                        settingAmbientClicked();
                        break;
                    case R.id.setting_location:
                        settingLocationClicked();
                        break;
                    case R.id.setting_alarm_sound:
                        settingAlarmSoundClicked();
                        break;
                }
            }
        };
        preferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                updateDescriptions();
            }
        };
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        ButterKnife.bind(this);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        sharedPreferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener);

        back.setOnClickListener(clicksListener);
        settingAmbient.setOnClickListener(clicksListener);
        settingLocation.setOnClickListener(clicksListener);
        settingAlarmSound.setOnClickListener(clicksListener);

        updateDescriptions();
    }

    private void updateDescriptions() {
        int secs =
                sharedPreferences.getInt(getString(R.string.pref_setting_ambient_key), 15000) / 1000;
        settingAmbientDesc.setText(getString(R.string.setting_ambient_desc, secs));

        settingLocationDesc.setText(sharedPreferences.getString(getString(R.string.pref_setting_location_key), "No Location specified"));

        settingAlarmSoundDesc.setText(sharedPreferences.getString(getString(R.string.pref_setting_alarmsound_key), "No Sound file Specified"));
    }

    //    click handler actions
    private void settingAmbientClicked() {
        int secs =
                sharedPreferences.getInt(getString(R.string.pref_setting_ambient_key), 15000) / 1000;
        String desc = getString(R.string.setting_ambient_desc, secs);
        DialogFragment settingFragment =
                BaseDialogFragment.getInstance(R.string.setting_ambient_title,
                        R.string.pref_setting_ambient_key, desc);
        settingFragment.show(getSupportFragmentManager(), BaseDialogFragment.TAG);
    }

    private void settingLocationClicked() {
        String location =
                sharedPreferences.getString(getString(R.string.pref_setting_location_key), "Not " +
                        "set");
        DialogFragment settingFragment =
                BaseDialogFragment.getInstance(R.string.setting_location_title,
                        R.string.pref_setting_location_key, location);
        settingFragment.show(getSupportFragmentManager(), BaseDialogFragment.TAG);
    }

    private void settingAlarmSoundClicked() {
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            showAlarmSoundDialog();
        } else {
            requestPermissions(
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_FOR_ALARM_SOUND);
        }
    }

    private void showAlarmSoundDialog() {
        MediaPickerDialog dialog = new MediaPickerDialog();
        dialog.show(getSupportFragmentManager(), MediaPickerDialog.TAG);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST_FOR_ALARM_SOUND:
                if (permissions.length != 0 && grantResults.length != 0) {
                    int i = 0;
                    for (i = 0; i < permissions.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(getApplicationContext(), R.string.toast_permission_needed_message, Toast.LENGTH_LONG).show();
                            break;
                        }
                    }
                    showAlarmSoundDialog();
                }
                break;
        }
    }
}
