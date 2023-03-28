package com.example.yepnotes;

import android.annotation.SuppressLint;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;

public class Utility {

    public static CollectionReference getCollectionReferenceForNotes(){
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            return FirebaseFirestore.getInstance().collection("notes")
                    .document(currentUser.getUid()).collection("my_notes");
        }
        return null;
    }
    @SuppressLint("SimpleDateFormat")
    static String timestampToString(Timestamp timestamp){
        return new SimpleDateFormat("dd/MM/yyyy, hh:mm a").format(timestamp.toDate());
    }
}
