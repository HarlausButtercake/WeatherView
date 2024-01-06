package com.example.weath22;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;

import androidx.work.Data;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of App Widget functionality.
 */
public class statWidget extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
    {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        File sett = new File(context.getFilesDir(), "config.json");
        JSONObject jsonObject = null;
        try {
            BufferedReader br = new BufferedReader(new FileReader(sett));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            br.close();
            jsonObject = new JSONObject(sb.toString());
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        try {
            String coord = jsonObject.getString("Widget_loc");
            Data queuedata = new Data.Builder()
                    .putString("lat", coord.substring(0, coord.indexOf(":")))
                    .putString("lon", coord.substring(coord.indexOf(":") + 1))
                    .build();
            PeriodicWorkRequest widgetUpdateRequest =
                    new PeriodicWorkRequest.Builder(widgetWorker.class, 30, TimeUnit.MINUTES)
                            .setInputData(queuedata)
                            .build();
            WorkManager.getInstance(context).enqueue(widgetUpdateRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
