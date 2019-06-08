package io.github.zkhan93.alarmandplayer.viewholder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.zkhan93.alarmandplayer.R;
import io.github.zkhan93.alarmandplayer.adapter.model.Sound;

public class SoundVH extends RecyclerView.ViewHolder implements View.OnClickListener {
    @BindView(R.id.name)
    public TextView name;

    @BindView(R.id.path)
    public TextView path;

    private Sound sound;
    private ItemInteractionListener itemInteractionListener;

    public SoundVH(@NonNull View itemView, ItemInteractionListener itemInteractionListener) {
        super(itemView);
        this.itemInteractionListener = itemInteractionListener;
        itemView.setOnClickListener(this);
        ButterKnife.bind(this, itemView);
    }

    public void setSound(Sound sound) {
        this.sound = sound;
        if (this.sound != null)
            this.name.setText(this.sound.name);
    }

    @Override
    public void onClick(View v) {
        itemInteractionListener.onSelect(sound);
    }

    public interface ItemInteractionListener {
        void onSelect(Sound sound);
    }
}
