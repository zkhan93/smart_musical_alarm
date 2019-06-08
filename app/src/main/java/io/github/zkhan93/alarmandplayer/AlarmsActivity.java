package io.github.zkhan93.alarmandplayer;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.SnapshotParser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.zkhan93.alarmandplayer.data.Alarm;
import io.github.zkhan93.alarmandplayer.viewholder.AlarmVH;

public class AlarmsActivity extends AppCompatActivity {
    public static final String TAG = AlarmsActivity.class.getSimpleName();

    @BindView(R.id.back)
    public TextView btnBack;

    @BindView(R.id.add)
    public TextView btnAdd;

    @BindView(R.id.list_alarms)
    public RecyclerView alarmList;

    private FirebaseFirestore firestore;
    private View.OnClickListener clicksListener;
    private FirestoreRecyclerAdapter adapter;
    private AlarmVH.ItemInteractionListener alarmItemInteractionListener;


    {
        clicksListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.back:
                        onBackPressed();
                        break;
                    case R.id.add:
                        btnAddAction(view);
                        break;
                }
            }
        };
        alarmItemInteractionListener = new AlarmVH.ItemInteractionListener() {
            @Override
            public void onSelect(Alarm alarm) {
                showAlarmDialog(alarm);
            }

            @Override
            public void onAlarmEnabled(Alarm alarm, boolean enabled) {
                if (alarm != null && alarm.key != null)
                    firestore.collection("alarms").document(alarm.key).update("enabled", enabled).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "update success");
                            } else {
                                Log.d(TAG, "update failed");
                            }
                        }
                    });
            }
        };
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarms);
        ButterKnife.bind(this);

        firestore = ((App) getApplication()).getFirestore();

        btnBack.setOnClickListener(clicksListener);
        btnAdd.setOnClickListener(clicksListener);

        setAlarmAdapter();
        alarmList.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        adapter.stopListening();
        super.onStop();
    }

    private void setAlarmAdapter() {
        Query query = firestore.collection("alarms");
        FirestoreRecyclerOptions<Alarm> options = new FirestoreRecyclerOptions.Builder<Alarm>()
                .setQuery(query, new SnapshotParser<Alarm>() {
                    @NonNull
                    @Override
                    public Alarm parseSnapshot(@NonNull DocumentSnapshot snapshot) {
                        Alarm alarm = snapshot.toObject(Alarm.class);
                        if (alarm != null)
                            alarm.key = snapshot.getId();
                        return alarm;
                    }
                })
                .build();
        adapter = new FirestoreRecyclerAdapter<Alarm, AlarmVH>(options) {

            @Override
            public void onBindViewHolder(AlarmVH holder, int position, Alarm alarm) {
                holder.setAlarm(alarm);
            }

            @Override
            public AlarmVH onCreateViewHolder(ViewGroup group, int i) {
                // Create a new instance of the ViewHolder, in this case we are using a custom
                // layout called R.layout.message for each item
                View view = LayoutInflater.from(group.getContext())
                        .inflate(R.layout.listitem_alarm, group, false);

                return new AlarmVH(view, alarmItemInteractionListener);
            }
        };
    }

    private void btnAddAction(View view) {
        //  show dialog to add a new alarm
        Intent intent = new Intent(getApplicationContext(), AlarmActivity.class);
        intent.setAction(AlarmActivity.ACTION_NEW);
        startActivity(intent);
    }

    private void showAlarmDialog(Alarm alarm) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("alarm", alarm);
        Intent intent = new Intent(getApplicationContext(), AlarmActivity.class);
        intent.setAction(AlarmActivity.ACTION_UPDATE);
        intent.putExtras(bundle);
        startActivity(intent);
    }
}
