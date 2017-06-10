package com.example.stefa.mycustommaps;

import android.content.Intent;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddSeedActivity extends AppCompatActivity {

    private DatabaseReference firebaseDB;
    static final int REQUEST_IMAGE_CAPTURE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_seed);
        this.firebaseDB = FirebaseDatabase.getInstance().getReference();
    }

    public void takePhoto(View v) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    public void saveSeed(View v) {
        EditText seedTitle = (EditText) findViewById(R.id.seedTitle);
        if (seedTitle.getText().toString().length() == 0){
            Toast.makeText(this, "No title registered", Toast.LENGTH_SHORT).show();
            return;
        }

        EditText seedDes = (EditText) findViewById(R.id.seedDescription);
        if (seedDes.getText().toString().length() == 0){
            Toast.makeText(this, "No description registered", Toast.LENGTH_SHORT).show();
            return;
        }

        String latitude = getIntent().getStringExtra("latitude");
        String longitude = getIntent().getStringExtra("longitude");
        Seed seed = new Seed(seedTitle.getText().toString(), seedDes.getText().toString(), Double.valueOf(latitude), Double.valueOf(longitude));

        firebaseDB.child(seed.title).setValue(seed);
        Toast.makeText(this, seed.toString(), Toast.LENGTH_SHORT).show();
        finish();
    }
}
