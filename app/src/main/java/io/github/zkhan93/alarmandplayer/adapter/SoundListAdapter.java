package io.github.zkhan93.alarmandplayer.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import io.github.zkhan93.alarmandplayer.R;
import io.github.zkhan93.alarmandplayer.adapter.model.Sound;
import io.github.zkhan93.alarmandplayer.viewholder.SoundVH;

public class SoundListAdapter extends RecyclerView.Adapter<SoundVH> {
    private List<Sound> names;
    private View.OnClickListener clickListener;
    private SoundVH.ItemInteractionListener itemInteractionListener;

    {
        clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        };
    }

    public SoundListAdapter(List<Sound> names,
                            SoundVH.ItemInteractionListener itemInteractionListener) {
        this.names = names;
        if (this.names == null) {
            this.names = new ArrayList<>();
        }
        this.itemInteractionListener = itemInteractionListener;
    }

    @NonNull
    @Override
    public SoundVH onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.listitem_sound,
                viewGroup, false);
        return new SoundVH(view, itemInteractionListener);
    }

    @Override
    public void onBindViewHolder(@NonNull SoundVH soundVH, int i) {
        soundVH.setSound(names.get(i));
        Sound sound = names.get(i);
        if (sound != null) {
            if (sound.name != null)
                soundVH.name.setText(sound.name);
            if (sound.path != null)
                soundVH.path.setText(sound.path);
        }
    }

    @Override
    public int getItemCount() {
        return names.size();
    }
}
