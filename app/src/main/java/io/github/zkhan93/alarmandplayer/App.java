package io.github.zkhan93.alarmandplayer;

import android.app.Application;

import com.google.firebase.firestore.FirebaseFirestore;

public class App extends Application {
    private FirebaseFirestore firestore;

    public FirebaseFirestore getFirestore() {
        if (firestore == null)
            firestore = FirebaseFirestore.getInstance();
        return firestore;
    }
}
