package com.example.watermyplants;


import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class PlantListAdapter extends RecyclerView.Adapter<PlantListAdapter.Viewholder> {

    //Create Variables

    ArrayList<Long> waterTimers, water_start_timer;
    ArrayList<byte[]> plantImages;
    ArrayList<String> plantId, plantNames, plantDescriptions;
    LayoutInflater inflater;
    DataBase db;
    Context context;
    Activity activity;

    NotificationManagerCompat notificationManagerCompat;
    Notification notification;


    //Constructor
    public PlantListAdapter(Activity activity, Context context, ArrayList<String> plantId, ArrayList<byte[]> plantImages, ArrayList<String> plantNames, ArrayList<String> plantDescriptions, ArrayList<Long> waterTimers, ArrayList<Long> water_start_timer) {
        this.plantId = plantId;
        this.plantImages = plantImages;
        this.plantNames = plantNames;
        this.plantDescriptions = plantDescriptions;
        this.waterTimers = waterTimers;
        this.water_start_timer = water_start_timer;
        this.context = context;
        db = new DataBase(context);
        inflater = LayoutInflater.from(context);
        this.activity = activity;
    }

    //To inflate right item layout
    @NonNull
    @Override
    public PlantListAdapter.Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.plant_item, parent, false);
        return new Viewholder(view);
    }

    //Insert data in layout
    @Override
    public void onBindViewHolder(@NonNull PlantListAdapter.Viewholder holder, int position) {
        CountDownTimer countDownTimer = new CountDownTimer(waterTimers.get(position), 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                holder.txtItemCountDown.setText(String.format("%d H, %02d min, %02d sec",
                        TimeUnit.MILLISECONDS.toHours(millisUntilFinished),
                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) -
                                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millisUntilFinished)),
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))
                ));
                db.updatePlant(plantId.get(position), plantImages.get(position), plantNames.get(position), plantDescriptions.get(position), waterTimers.set(position, millisUntilFinished), water_start_timer.get(position));
            }

            @Override
            public void onFinish() {
                holder.txtItemCountDown.setText("Finished");

                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "myCh")
                        .setSmallIcon(android.R.drawable.stat_notify_sync)
                        .setContentTitle(plantNames.get(position))
                        .setContentText("This Plant is ready for water");

                PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                        new Intent(context, MainActivity.class), PendingIntent.FLAG_IMMUTABLE);

                builder.setContentIntent(contentIntent);

                notification = builder.build();
                notificationManagerCompat = NotificationManagerCompat.from(context);

                notificationManagerCompat.notify(1, notification);
            }
        }.start();


        holder.itemImgView.setImageBitmap(DbBitmapUtility.getImage(plantImages.get(position)));
        holder.txtItemName.setText(plantNames.get(position));

        holder.itemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if((holder.txtItemCountDown.getText().toString()).equalsIgnoreCase("Finished")) {
                    waterTimers.set(position, water_start_timer.get(position));
                    notificationManagerCompat.cancel(1);
                    CountDownTimer countDownTimer = new CountDownTimer(waterTimers.get(position), 1000) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                            holder.txtItemCountDown.setText(String.format("%d H, %02d min, %02d sec",
                                    TimeUnit.MILLISECONDS.toHours(millisUntilFinished),
                                    TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) -
                                            TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millisUntilFinished)),
                                    TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))
                            ));
                            db.updatePlant(plantId.get(position), plantImages.get(position), plantNames.get(position), plantDescriptions.get(position), waterTimers.set(position, millisUntilFinished), water_start_timer.get(position));
                        }

                        @Override
                        public void onFinish() {

                        }
                    }.start();
                }
            }
        });

        holder.itemImgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, update_plant.class);
                intent.putExtra("id", plantId.get(position));
                intent.putExtra("img", plantImages.get(position));
                intent.putExtra("name", plantNames.get(position));
                intent.putExtra("description", plantDescriptions.get(position));
                intent.putExtra("time", TimeUnit.MILLISECONDS.toHours(water_start_timer.get(position)));
                activity.startActivityForResult(intent, 1);
            }
        });
    }

    //Number of items needed
    @Override
    public int getItemCount() {
        return plantId.size();
    }

    //Find variables from view
    public class Viewholder extends RecyclerView.ViewHolder {

        ImageView itemImgView;
        TextView txtItemName, txtItemCountDown;
        LinearLayout itemLayout;
        public Viewholder(@NonNull View itemView) {
            super(itemView);
            itemImgView = itemView.findViewById(R.id.itemImgView);
            txtItemName = itemView.findViewById(R.id.txtItemName);
            txtItemCountDown = itemView.findViewById(R.id.txtItemCountDown);
            itemLayout = itemView.findViewById(R.id.itemLayout);
        }
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

}
