package com.example.watermyplants;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class add_plant_to_list extends AppCompatActivity {

    //Create Variables

    private static final int pic_id = 101;
    ImageView addPlantImgView;
    EditText edtTxtPlantName, edtTxtPlantDescription, editTextTime;

    DataBase db;
    Button btnAddPlant;

    Bitmap bitmap;

    ArrayList<byte[]> plantImages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_plant_to_list);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Initialize Variables
        db = new DataBase(add_plant_to_list.this);

        plantImages = new ArrayList<byte[]>();

        edtTxtPlantName = findViewById(R.id.edtTxtPlantName);
        edtTxtPlantDescription = findViewById(R.id.edtTxtPlantDescription);
        editTextTime = findViewById(R.id.editTextTime);

        addPlantImgView = findViewById(R.id.addPlantImgView);
        addPlantImgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(camera_intent, pic_id);
            }
        });


        btnAddPlant = findViewById(R.id.btnAddPlant);
        btnAddPlant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bitmap == null) {
                    Toast.makeText(add_plant_to_list.this, "Add image", Toast.LENGTH_SHORT).show();
                } else if(TextUtils.isEmpty(edtTxtPlantName.getText().toString())) {
                    edtTxtPlantName.setError("Plant name required");
                    edtTxtPlantName.requestFocus();
                } else if(TextUtils.isEmpty(edtTxtPlantDescription.getText().toString())) {
                    edtTxtPlantDescription.setError("Description required");
                    edtTxtPlantDescription.requestFocus();
                } else if(TextUtils.isEmpty(editTextTime.getText().toString())) {
                    editTextTime.setError("Timer is required");
                    editTextTime.requestFocus();
                } else {
                    db.addPlant(plantImages.get(0), edtTxtPlantName.getText().toString().trim(),
                            edtTxtPlantDescription.getText().toString().trim(), TimeUnit.HOURS.toMillis(Long.parseLong(editTextTime.getText().toString().trim())), TimeUnit.HOURS.toMillis(Long.parseLong(editTextTime.getText().toString().trim())));
                    finish();
                }
            }
        });


    }
    //Replace Imageview with picture
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == pic_id) {
            bitmap = (Bitmap) data.getExtras().get("data");
            plantImages.add(DbBitmapUtility.getBytes(bitmap));
            addPlantImgView.setImageBitmap(bitmap);
        }
    }

    //Transform picture in correct formats
    public static class DbBitmapUtility {

        // convert from bitmap to byte array
        public static byte[] getBytes(Bitmap bitmap) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
            return stream.toByteArray();
        }

        // convert from byte array to bitmap
        public static byte[] getImage(byte[] image) {
            return BitmapFactory.decodeByteArray(image, 0, image.length).getNinePatchChunk();
        }
    }

}
