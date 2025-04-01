package com.example.watermyplants;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import java.util.ArrayList;

public class CountdownReceiver extends BroadcastReceiver {
    private ArrayList<Long> timeList;
    private PlantListAdapter adapter; // Reference to RecyclerView adapter

    public CountdownReceiver(ArrayList<Long> timeList, PlantListAdapter adapter) {
        this.timeList = timeList;
        this.adapter = adapter;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals(CountdownService.COUNTDOWN_BROADCAST)) {
            long timeLeft = intent.getLongExtra("TIME_LEFT", 0);
            int index = intent.getIntExtra("CURRENT_INDEX", 0);

            if (index < timeList.size()) {
                timeList.set(index, timeLeft); // Update the list with the remaining time
                adapter.notifyItemChanged(index); // Notify adapter to refresh that item
            }
        }
    }
}


