package io.github.zkhan93.alarmandplayer.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.zkhan93.alarmandplayer.R;

public class SimpleSettingDialogFragment extends DialogFragment {
    public static final String TAG = SimpleSettingDialogFragment.class.getSimpleName();

    private int title;
    private int preferenceKey;
    private String desc;

    private DialogInterface.OnClickListener positiveListener;
    private DialogInterface.OnClickListener negativeListener;
    private SharedPreferences sharedPreferences;

    @BindView(R.id.setting_desc)
    public TextView settingDesc;

    @BindView(R.id.setting_value)
    public EditText settingValue;

    {
        positiveListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                updateSetting();
            }
        };

        negativeListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismiss();
            }
        };
    }

    public static SimpleSettingDialogFragment getInstance(int title, int preferenceKey, String desc) {
        SimpleSettingDialogFragment frag = new SimpleSettingDialogFragment();
        Bundle args = new Bundle();
        args.putInt("title", title);
        args.putInt("preferenceKey", preferenceKey);
        args.putString("desc", desc);
        frag.setArguments(args);
        return frag;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        Bundle bundle = getArguments();
        if (bundle != null) {
            title = bundle.getInt("title");
            preferenceKey = bundle.getInt("preferenceKey");
            desc = bundle.getString("desc");
        }

        View view = LayoutInflater.from(getContext()).inflate(R.layout.setting_dialog, null);
        ButterKnife.bind(this, view);

        settingDesc.setText(desc);
        String hint = getHint();
        if (hint != null)
            settingValue.setHint(hint);
        return new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.Dialog))
                .setTitle(title)
                .setView(view)
                .setPositiveButton("ok", positiveListener)
                .setNegativeButton("cancel", negativeListener)
                .create();
    }

    private void updateSetting() {
        String value = settingValue.getText().toString();
        switch (preferenceKey) {
            case R.string.pref_setting_ambient_key:
                sharedPreferences.edit().putInt(getString(preferenceKey),
                        Integer.parseInt(value) * 1000).apply();
                break;
            case R.string.pref_setting_location_key:
                sharedPreferences.edit().putString(getString(preferenceKey),
                        value).apply();
                break;
            case R.string.pref_setting_alarmsound_key:
                sharedPreferences.edit().putString(getString(preferenceKey),
                        value).apply();
                break;
        }
    }
    private String getHint(){
        switch (preferenceKey){
            case R.string.pref_setting_ambient_key:
                int secs = sharedPreferences.getInt(getString(R.string.pref_setting_ambient_key), 15000)/1000;
                return String.valueOf(secs);
            case R.string.pref_setting_location_key:
                return sharedPreferences.getString(getString(R.string.pref_setting_location_key), "Bangalore, India");
            default:
                return null;
        }
    }
}
