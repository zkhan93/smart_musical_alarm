package io.github.zkhan93.alarmandplayer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.zkhan93.alarmandplayer.data.Alarm;
import io.github.zkhan93.alarmandplayer.viewholder.AlarmVH;

public class AlarmActivity extends AppCompatActivity {
    public static final String TAG = AlarmActivity.class.getSimpleName();

    @BindView(R.id.back)
    public TextView btnBack;

    @BindView(R.id.add)
    public TextView btnAdd;

    @BindView(R.id.list_alarms)
    public RecyclerView alarmList;

    private FirebaseFirestore firestore;
    private View.OnClickListener clicksListener;
    private FirestoreRecyclerAdapter adapter;


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
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);
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
                .setQuery(query, Alarm.class)
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

                return new AlarmVH(view);
            }
        };
    }
    private void btnAddAction(View view){
        //  show dialog to add a new alarm
    }
}
