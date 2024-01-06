package com.example.weath22;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.icu.util.Calendar;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

public class notiWorker extends Worker {

    public notiWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        String lon = getInputData().getString("lon");
        String lat = getInputData().getString("lat");
        String temp_mode = getInputData().getString("temp_mode");
        String timer = getInputData().getString("timer");
        createNotification(getApplicationContext(), lat, lon, Integer.parseInt(temp_mode));

        Data queueData = new Data.Builder()
                .putString("lat", lat)
                .putString("lon", lon)
                .putString("temp_mode", temp_mode)
                .putString("timer", timer)
                .build();
        scheduleNotificationForTomorrow(getApplicationContext(), timer, queueData);
        return Result.success();
    }
    private void scheduleNotificationForTomorrow(Context context, String timer, Data data) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 1); // Set to tomorrow
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timer.substring(0, timer.indexOf(":"))));
        calendar.set(Calendar.MINUTE, Integer.parseInt(timer.substring(timer.indexOf(":") + 1)));
        calendar.set(Calendar.SECOND, 0);
        long delay = calendar.getTimeInMillis() - System.currentTimeMillis();

        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(notiWorker.class)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .build();

        WorkManager.getInstance(context).enqueue(workRequest);
    }
    private static final String CHANNEL_ID = "your_channel_id";
    private static final int NOTIFICATION_ID = 1;
    private static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "channel_name";
            String description = "channel_desc";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    public static void createNotification(Context context, String lat, String lon, int temp_mode) {
        createNotificationChannel(context);
        String title = "An error occurred";
        String text = "Failed to receive data. Make sure to have internet connection enabled!";
        String url = "https://api.openweathermap.org/data/2.5/forecast/daily?lat=" + lat + "&lon=" + lon + "&cnt=1&appid=" + "72ef7ccc6fd8601a88a0d7e08e8e1181";
        StringRequest dailyRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    String buff, bump;
                    DecimalFormat nodecimal = new DecimalFormat("#");
                    JSONObject jsonRes = new JSONObject(response);
                    JSONArray list = jsonRes.getJSONArray("list");
                    JSONObject list0 = list.getJSONObject(0);
                    JSONObject temp = list0.getJSONObject("temp");
                    if(temp_mode == 1){
                        buff = nodecimal.format(temp.getDouble("max") - 273.15) + "°C/" + nodecimal.format(temp.getDouble("min") - 273.15) + "°C";
                    } else {
                        buff = nodecimal.format((temp.getDouble("max") - 273.15)*1.8+32) + "°F/" + nodecimal.format((temp.getDouble("min") - 273.15)*1.8+32) + "°F";
                    }

                    buff = buff + " in " + jsonRes.getJSONObject("city").getString("name") + ", " + jsonRes.getJSONObject("city").getString("country");
                    JSONArray weather = list0.getJSONArray("weather");
                    JSONObject weather0 = weather.getJSONObject(0);

                    bump = weather0.getString("description").substring(0, 1).toUpperCase() + weather0.getString("description").substring(1);
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                            .setSmallIcon(R.drawable.sunrise)
                            .setContentTitle(buff)
                            .setContentText(bump)
                            .setSound(null)
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                            .setAutoCancel(true);

                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.VIBRATE) != PackageManager.PERMISSION_GRANTED) {
                        notificationManager.notify(NOTIFICATION_ID, builder.build());

                        return;
                    }
                    notificationManager.notify(NOTIFICATION_ID, builder.build());
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(dailyRequest);

    }
}