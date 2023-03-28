package com.example.yepnotes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;

import java.util.Objects;

public class NotesDetailActivity extends AppCompatActivity {

    EditText titleEditText,contentEditText;
    ImageButton saveNoteBtn;

    TextView pageTitleTextView;

    String title,content,docId;

    boolean isEditMode=false;

    MaterialButton deleteButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes_detail);

        titleEditText = findViewById(R.id.notes_title_text);
        contentEditText=findViewById(R.id.notes_content_text);
        saveNoteBtn = findViewById(R.id.note_save_btn);
        pageTitleTextView = findViewById(R.id.page_title);
        deleteButton = findViewById(R.id.delete_btn);

        saveNoteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveNote();
            }
        });


        //receive the data
        title = getIntent().getStringExtra("title");
        content = getIntent().getStringExtra("content");
        docId = getIntent().getStringExtra("docId");

        if(docId!=null && !docId.isEmpty())
        {
            isEditMode=true;
        }
        if(isEditMode){
            pageTitleTextView.setText("Edit your note");
            titleEditText.setText(title);
            contentEditText.setText(content);
            deleteButton.setVisibility(View.VISIBLE);
        }

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteNote();
            }
        });


    }

    private void deleteNote() {

        DocumentReference documentReference = Utility.getCollectionReferenceForNotes().document(docId);
        documentReference.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                  Toast.makeText(NotesDetailActivity.this,"Deleted successfully!",Toast.LENGTH_SHORT).show();
                  startActivity(new Intent(NotesDetailActivity.this,MainActivity.class));
                  finish();
                }
                else {
                    Toast.makeText(NotesDetailActivity.this,"Error in deletion!",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void saveNote() {
        String noteTitle = titleEditText.getText().toString();
        String noteContent = contentEditText.getText().toString();

        if(noteTitle.isEmpty())
        {
            titleEditText.setError("Title is required!");
        }
        Note note = new Note();
        note.setTitle(noteTitle);
        note.setContent(noteContent);
        note.setTimestamp(Timestamp.now());
        if(internetIsConnected()) {
            saveNoteToFirebase(note);
        }
        else {
            Toast.makeText(NotesDetailActivity.this,"You are not connected to internet!",Toast.LENGTH_SHORT).show();
        }
    }

    private void saveNoteToFirebase(Note note){
        DocumentReference documentReference;

        if(isEditMode){
            //update existing note
            documentReference = Objects.requireNonNull(Utility.getCollectionReferenceForNotes()).document(docId);
        }
        else{
            //add new note
            documentReference = Objects.requireNonNull(Utility.getCollectionReferenceForNotes()).document();
        }


        documentReference.set(note).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    if(!isEditMode) {
                        Toast.makeText(NotesDetailActivity.this, "Successfully uploaded!", Toast.LENGTH_SHORT).show();
                    }
                    else
                        Toast.makeText(NotesDetailActivity.this, "Saved!", Toast.LENGTH_SHORT).show();

                }
                else{
                    Toast.makeText(NotesDetailActivity.this, "error",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public boolean internetIsConnected() {
        try {
            String command = "ping -c 1 google.com";
            return (Runtime.getRuntime().exec(command).waitFor() == 0);
        } catch (Exception e) {
            return false;
        }
    }
}