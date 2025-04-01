package com.example.watermyplants;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    //Create Variables

    private CountdownReceiver countdownReceiver;

    FloatingActionButton btnAdd;

    ArrayList<Long> waterTimers, water_start_timer;

    DataBase db;

    RecyclerView recyclerView;
    PlantListAdapter plantAdapter;

    ArrayList<byte[]> plantImages;
    ArrayList<String> plantId, plantNames, plantDescriptions;

    public static NotificationChannel channel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }

        //For notification
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = new NotificationChannel("myCh", "My Channel", NotificationManager.IMPORTANCE_DEFAULT);

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        //Initialize variables
        recyclerView = findViewById(R.id.recyclerView);

        db = new DataBase(MainActivity.this);

        plantId = new ArrayList<String>();
        plantImages = new ArrayList<byte[]>();
        plantNames = new ArrayList<String>();
        plantDescriptions = new ArrayList<String>();
        waterTimers = new ArrayList<Long>();
        water_start_timer = new ArrayList<Long>();

        storeDataInArrays();

        //Set up recyclerview
        plantAdapter = new PlantListAdapter(MainActivity.this, MainActivity.this, plantId, plantImages, plantNames, plantDescriptions, waterTimers, water_start_timer);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(plantAdapter);

        btnAdd = findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, add_plant_to_list.class);
                startActivityForResult(intent, 1);
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            if (!pm.isIgnoringBatteryOptimizations(getPackageName())) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                        Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }
        }

        // Start the countdown service with the list of times
        Intent serviceIntent = new Intent(this, CountdownService.class);
        serviceIntent.putExtra("TIME_LIST", waterTimers);
        startService(serviceIntent);

        // Register the broadcast receiver
        countdownReceiver = new CountdownReceiver(waterTimers, plantAdapter);
        IntentFilter filter = new IntentFilter(CountdownService.COUNTDOWN_BROADCAST);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // API 33 (Android 13+)
            registerReceiver(countdownReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(countdownReceiver, filter);
        }
    }

    //To refresh recyclerview after adding a new item
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1) {
            recreate();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.deleteAll) {
            confirmDialog();
        } else {
            return super.onOptionsItemSelected(item);
        }

        return true;

    }

    void confirmDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete all Plants?");
        builder.setMessage("Are you sure you want to delete all Plants?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                db.deleteTable();
                recreate();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.create().show();
    }

    void storeDataInArrays() {
        //Pass all table data to cursor
        Cursor cursor = db.readAllData();
        if(cursor.getCount() == 0) {
            Toast.makeText(this, "No data.", Toast.LENGTH_SHORT).show();
        } else {
            while (cursor.moveToNext()) {
                plantId.add(cursor.getString(0));
                plantImages.add(cursor.getBlob(1));
                plantNames.add(cursor.getString(2));
                plantDescriptions.add(cursor.getString(3));
                waterTimers.add(cursor.getLong(4));
                water_start_timer.add(cursor.getLong(5));
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, you can now send notifications
            } else {
                // Permission denied, show a message if needed
            }
        }
    }
}