package com.example.weath22;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.icu.text.SimpleDateFormat;
import android.icu.util.TimeZone;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import java.util.Date;

public class widgetWorker extends Worker {
    public widgetWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        String longitude = getInputData().getString("lon");
        String latitude = getInputData().getString("lat");
        String url = "https://api.openweathermap.org/data/2.5/weather?lat=" + latitude + "&lon=" + longitude + "&appid=72ef7ccc6fd8601a88a0d7e08e8e1181";
        StringRequest dailyRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                String url = "https://pro.openweathermap.org/data/2.5/forecast/hourly?lat="
                        + latitude + "&lon=" + longitude + "&appid=72ef7ccc6fd8601a88a0d7e08e8e1181";
                StringRequest hourlyRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String hourresponse) {
                        updateWidget(getApplicationContext(), response, hourresponse);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), "Hourly request failed!", Toast.LENGTH_SHORT).show();
                    }
                });
                RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
                requestQueue.add(hourlyRequest);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(dailyRequest);

        return Result.success();
    }
    private void updateWidget(Context context, String currResponse, String hourResponse) {
        try {
            JSONObject currjsonRes = new JSONObject(currResponse);
            JSONObject sys = currjsonRes.getJSONObject("sys");

            JSONArray arrWeather = currjsonRes.getJSONArray("weather");
            JSONObject weather = arrWeather.getJSONObject(0);
            JSONObject main = currjsonRes.getJSONObject("main");
            String icon_mum = "xx" + weather.getString("icon");
            String bump = weather.getString("description").substring(0, 1).toUpperCase() + weather.getString("description").substring(1);

            DecimalFormat nodecimal = new DecimalFormat("#");
            double temp = main.getDouble("temp") - 273.15;
            String buff = nodecimal.format(temp) + "°C";
            String dumb = currjsonRes.getString("name") + ", " + sys.getString("country");
            String zoneString;
            int localTimeZone = currjsonRes.getInt("timezone");
            if(localTimeZone >= 0){
                zoneString = "GMT+" + localTimeZone/3600;
            } else {
                zoneString = "GMT" + localTimeZone/3600;
            }
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.stat_widget);
            JSONObject hourjsonRes = new JSONObject(hourResponse);
            JSONArray hour = hourjsonRes.getJSONArray("list");

            JSONObject hour1 = hour.getJSONObject(0);
            JSONObject hourmain = hour1.getJSONObject("main");
            double temper = hourmain.getDouble("temp") - 273.15;
            views.setTextViewText(R.id.temper1, nodecimal.format(temper) + "°C");
            JSONArray hourweather = hour1.getJSONArray("weather");
            JSONObject hourweather0 = hourweather.getJSONObject(0);
            String packageName = context.getPackageName();
            int resId = context.getResources().getIdentifier("xx" + hourweather0.getString("icon"), "drawable", packageName);
            views.setImageViewResource(R.id.icon1, resId);
            views.setTextViewText(R.id.hour1, unixt_to_HHMM(hour1.getLong("dt"), zoneString));

            hour1 = hour.getJSONObject(1);
            hourmain = hour1.getJSONObject("main");
            temper = hourmain.getDouble("temp") - 273.15;
            views.setTextViewText(R.id.temper2, nodecimal.format(temper) + "°C");
            hourweather = hour1.getJSONArray("weather");
            hourweather0 = hourweather.getJSONObject(0);
            packageName = context.getPackageName();
            resId = context.getResources().getIdentifier("xx" + hourweather0.getString("icon"), "drawable", packageName);
            views.setImageViewResource(R.id.icon2, resId);
            views.setTextViewText(R.id.hour2, unixt_to_HHMM(hour1.getLong("dt"), zoneString));

            JSONObject hour2 = hour.getJSONObject(2);
            hourmain = hour2.getJSONObject("main");
            temper = hourmain.getDouble("temp") - 273.15;
            views.setTextViewText(R.id.temper3, nodecimal.format(temper) + "°C");
            hourweather = hour2.getJSONArray("weather");
            hourweather0 = hourweather.getJSONObject(0);
            packageName = context.getPackageName();
            resId = context.getResources().getIdentifier("xx" + hourweather0.getString("icon"), "drawable", packageName);
            views.setImageViewResource(R.id.icon3, resId);
            views.setTextViewText(R.id.hour3, unixt_to_HHMM(hour2.getLong("dt"), zoneString));




            views.setTextViewText(R.id.loc, dumb);
            views.setTextViewText(R.id.descr, bump);
            views.setTextViewText(R.id.tempera, buff);
            packageName = context.getPackageName();
            resId = context.getResources().getIdentifier(icon_mum, "drawable", packageName);
            views.setImageViewResource(R.id.imageView7, resId);

            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, statWidget.class));
            appWidgetManager.updateAppWidget(appWidgetIds, views);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    String unixt_to_HHMM(long unix_seconds, String zone){
        Date date = new Date(unix_seconds*1000L);
        SimpleDateFormat jdf = new SimpleDateFormat("yyyy-MM-dd; HH:mm:ss z");
        jdf.setTimeZone(TimeZone.getTimeZone(zone));
        String date_time = jdf.format(date);
        int start = date_time.indexOf(";");
        return date_time.substring(start + 2, start + 7);
    }
}
