package com.example.watermyplants;

import android.content.DialogInterface;
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
import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class update_plant extends AppCompatActivity {

    private static final int pic_id = 102;
    ImageView imgView;
    EditText updateTxtPlantName, updateTxtPlantDescription, updateTxtPlantTime;
    Button btnUpdatePlant, btnDelPlant;

    String name, description, id;
    Long time;
    byte[] img;
    Bitmap bitmap;

    ArrayList<byte[]> plantImages;

    DataBase db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_update_plant);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        plantImages = new ArrayList<byte[]>();

        imgView = findViewById(R.id.updatePlantImgView);
        updateTxtPlantName = findViewById(R.id.edtUpdateTxtPlantName);
        updateTxtPlantDescription = findViewById(R.id.edtUpdateTxtPlantDescription);
        updateTxtPlantTime = findViewById(R.id.edtUpdateTxtTime);
        btnUpdatePlant = findViewById(R.id.btnUpdatePlant);
        btnDelPlant = findViewById(R.id.btnDelPlant);

        Bundle bundle = new Bundle();
        bundle = getIntent().getExtras();
        id = bundle.getString("id");
        img = bundle.getByteArray("img");
        name = bundle.getString("name");
        description = bundle.getString("description");
        time = bundle.getLong("time");


        bitmap = DbBitmapUtility.getImage(img);
        plantImages.add(add_plant_to_list.DbBitmapUtility.getBytes(bitmap));
        imgView.setImageBitmap(bitmap);
        updateTxtPlantName.setText(name);
        updateTxtPlantDescription.setText(description);
        updateTxtPlantTime.setText("" + time);

        imgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(camera_intent, pic_id);
            }
        });

        btnUpdatePlant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(TextUtils.isEmpty(updateTxtPlantName.getText().toString())) {
                    updateTxtPlantName.setError("Plant name required");
                    updateTxtPlantName.requestFocus();
                } else if(TextUtils.isEmpty(updateTxtPlantDescription.getText().toString())) {
                    updateTxtPlantDescription.setError("Description required");
                    updateTxtPlantDescription.requestFocus();
                } else if(TextUtils.isEmpty(updateTxtPlantTime.getText().toString())) {
                    updateTxtPlantTime.setError("Timer is required");
                    updateTxtPlantTime.requestFocus();
                } else {
                    db = new DataBase(update_plant.this);
                    bitmap = DbBitmapUtility.getImage(img);
                    plantImages.add(add_plant_to_list.DbBitmapUtility.getBytes(bitmap));
                    name = updateTxtPlantName.getText().toString().trim();
                    description = updateTxtPlantDescription.getText().toString().trim();
                    time = TimeUnit.HOURS.toMillis(Long.parseLong(updateTxtPlantTime.getText().toString().trim()));
                    db.updatePlant(id, plantImages.get(1), name, description, time, time);
                    finish();
                }
            }
        });

        btnDelPlant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmDialog();
            }
        });

    }

    //Transform pictures to correct format to use
    public static class DbBitmapUtility {

        // convert from bitmap to byte array
        public static byte[] getBytes(Bitmap bitmap) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
            return stream.toByteArray();
        }

        // convert from byte array to bitmap
        public static Bitmap getImage(byte[] image) {
            return BitmapFactory.decodeByteArray(image, 0, image.length);
        }
    }

    void confirmDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete this plant?");
        builder.setMessage("Are you sure you want to delete this plant?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DataBase myDB = new DataBase(update_plant.this);
                myDB.removePlant(id);
                finish();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.create().show();
    }

    //Replace Imageview with picture
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == pic_id) {
            bitmap = (Bitmap) data.getExtras().get("data");
            plantImages.add(add_plant_to_list.DbBitmapUtility.getBytes(bitmap));
            imgView.setImageBitmap(bitmap);
        }
    }
}