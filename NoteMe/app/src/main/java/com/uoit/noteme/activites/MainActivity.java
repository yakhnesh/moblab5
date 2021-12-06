package com.uoit.noteme.activites;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.os.Environment;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.core.app.NotificationCompat;

import com.uoit.noteme.R;
import com.uoit.noteme.adapters.NotesAdapter;
import com.uoit.noteme.database.NotesDatabase;
import com.uoit.noteme.entities.Note;
import com.uoit.noteme.listeners.NotesListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements  NotesListener{

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private static List<Note> noteList;
    private static final String TAG = MainActivity.class.getSimpleName();

    public static final int REQUEST_CODE_ADD_NOTE = 1;
    public static final int REQUEST_CODE_UPDATE_NOTE = 2;
    public static final int REQUEST_CODE_SHOW_NOTES = 3;

    private RecyclerView notesRecyclerView;
    private List<Note> notesList;
    private NotesAdapter notesAdapter;
    private int noteClickedPosition = -1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView imageAddNoteMain = findViewById(R.id.imageAddNoteMain);
        imageAddNoteMain.setOnClickListener(v -> startActivityForResult(new Intent(
                getApplicationContext(), CreateNoteActivity.class), REQUEST_CODE_ADD_NOTE)
        );

        notesRecyclerView = findViewById(R.id.notesRecyclerView);
        notesRecyclerView.setLayoutManager(
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));

        noteList = new ArrayList<>();
        notesAdapter = new NotesAdapter(noteList, this);
        notesRecyclerView.setAdapter(notesAdapter);

        getNotes(REQUEST_CODE_SHOW_NOTES, false);

        EditText inputSearch = findViewById(R.id.inputSearch);
        inputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                notesAdapter.cancelTimer();
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (noteList.size() != 0) {
                    notesAdapter.searchNotes(s.toString());
                }
            }
        });


        findViewById(R.id.imageExportJson).setOnClickListener(view -> {
                final int REQUEST_EXTERNAL_STORAGE = 1;
                try{
                    int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    if (permission != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(
                                this,
                                PERMISSIONS_STORAGE,
                                REQUEST_EXTERNAL_STORAGE
                        );
                    }
                    File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                            "notes_data.json");
//                    if (!root.exists()) {
//                        root.mkdirs();
//                    }
//                    File file = new File(root, "notes_data.json");
                    FileWriter writer = new FileWriter(file);
                    writer.append(getJsonNotesList().toString());  //call method to get json data here
                    writer.flush();
                    writer.close();

                    Toast.makeText(this, "Json file exported Successfully", Toast.LENGTH_SHORT).show();
                    showDownloadNotification();
                }catch(IOException e){
                    e.printStackTrace();
                }

        });
    }

    public void showDownloadNotification(){
        String id = "my_channel_01";
        int importance = NotificationManager.IMPORTANCE_LOW;
        CharSequence name = "noteme_channel";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(getApplicationContext().NOTIFICATION_SERVICE);
            NotificationChannel mChannel = new NotificationChannel(id, name,importance);
            mChannel.enableLights(true);
            mNotificationManager.createNotificationChannel(mChannel);
        }

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, id)
                .setSmallIcon(R.drawable.ic_done) // notification icon
                .setContentTitle("Download status") // title
                .setContentText("JSON file export completed") // body message
                .setAutoCancel(true); // clear notification when clicked


        Intent intent = new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);
        mBuilder.setContentIntent(pi);

        NotificationManager mNotificationManager = (NotificationManager)getSystemService(getApplicationContext().NOTIFICATION_SERVICE);
        mNotificationManager.notify(0, mBuilder.build());

//        NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this, NotificationChannel.DEFAULT_CHANNEL_ID);
//        builder.setContentTitle("NoteMe App export");
//        builder.setContentText("File exported successfully");
//        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
    }

    public JSONArray getJsonNotesList(){
        JSONArray jsonList = new JSONArray();
        try{
            //List<Note> noteList = NotesDatabase.getNotesDatabase(getApplicationContext()).noteDao().getAllNotes();
            for(Note note : notesList){
                JSONObject jsonNote = new JSONObject();

                jsonNote.put("id", note.getId());
                jsonNote.put("title", note.getTitle());
                jsonNote.put("subtitle", note.getSubtitle());
                jsonNote.put("time", note.getDateTime());
                jsonNote.put("text", note.getNoteText());
                jsonNote.put("color", note.getColor());
                jsonNote.put("Image path", note.getImagePath());
                jsonList.put(jsonNote);
            }
        }catch(JSONException e){
            e.printStackTrace();
        }
        return jsonList;
    }

    @Override
    public void onNoteClicked(Note note, int position) {
        noteClickedPosition = position;
        Intent intent = new Intent(getApplicationContext(), CreateNoteActivity.class);
        intent.putExtra("isViewOrUpdate", true);
        intent.putExtra("note", note);
        startActivityForResult(intent, REQUEST_CODE_UPDATE_NOTE);

    }

    private void getNotes(final int requestCode, final boolean isNoteDeleted) {

        @SuppressLint("StaticFieldLeak")
        class GetNoteTask extends AsyncTask<Void, Void, List<Note>> {

            @Override
            protected List<Note> doInBackground(Void... voids) {
                notesList = NotesDatabase.getNotesDatabase(getApplicationContext())
                        .noteDao().getAllNotes();
                return NotesDatabase.getNotesDatabase(getApplicationContext())
                        .noteDao().getAllNotes();
            }

            @Override
            protected void onPostExecute(List<Note> notes) {
                super.onPostExecute(notes);
                if (requestCode == REQUEST_CODE_SHOW_NOTES){
                    noteList.addAll(notes);
                    notesAdapter.notifyDataSetChanged();
                }else if(requestCode == REQUEST_CODE_ADD_NOTE){
                    noteList.add(0,notes.get(0));
                    notesAdapter.notifyItemInserted(0);
                    notesRecyclerView.smoothScrollToPosition(0);
                }else if(requestCode == REQUEST_CODE_UPDATE_NOTE){
                    noteList.remove(noteClickedPosition);


                    if(isNoteDeleted){
                        notesAdapter.notifyItemRemoved(noteClickedPosition);
                    }
                    else {
                        noteList.add(noteClickedPosition, notes.get(noteClickedPosition));
                        notesAdapter.notifyItemChanged(noteClickedPosition);
                    }
                }

//                Log.d(TAG, "onPostExecute: " + notes.toString());
//                if (noteList.size() == 0) {
//                    noteList.addAll(notes);
//                    notesAdapter.notifyDataSetChanged();
//                } else {
//                    noteList.add(0, notes.get(0));
//                    notesAdapter.notifyItemInserted(0);
//                }
                notesRecyclerView.smoothScrollToPosition(0);
            }
        }

        new GetNoteTask().execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD_NOTE && resultCode == RESULT_OK) {
            getNotes(REQUEST_CODE_ADD_NOTE, false);
        } else if (requestCode == REQUEST_CODE_UPDATE_NOTE && resultCode == RESULT_OK){
            if(data != null){
                getNotes(REQUEST_CODE_UPDATE_NOTE, data.getBooleanExtra("isNotDeleted", false));
            }
        }
    }
}