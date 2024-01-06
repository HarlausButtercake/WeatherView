package com.example.weath22;

import static java.lang.Integer.parseInt;
import static java.lang.Math.abs;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.icu.text.SimpleDateFormat;
import android.icu.util.TimeZone;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainInfoActivity extends AppCompatActivity {

    DecimalFormat onedecimal = new DecimalFormat("#.#");
    DecimalFormat nodecimal = new DecimalFormat("#");
    TextView location_country;
    TextView temp_celcius, max_minTemp;
    double temp;
    RecyclerView hourly_list, daily_list;
    HourlyCustomAdapter hourlyAdapter;
    DailyCustomAdapter dailyAdapter;
    ImageView iconview, search_icon, config_icon;
    TextView weath_desc, rain_desc;
    TextView cloud_det, humid_det, wind_det, visib_det;
    TextView aqi_gene, aqi_gene_add;
    String dominant_poll, ref_info, logoStr;
    String appidExtra = "72ef7ccc6fd8601a88a0d7e08e8e1181";
    String aqiToken = "29f258f9a7b2aa7b8e76536c1cc85f7cd1a25320";
    Integer pm10, pm25, no2, so2, o3, co, aqi_index;
//    Integer localTimeZone;
    String IconUrl;
    String lat, lon, zoneString;
    long sunrise, sunset;
    Button aqi_button;
    SwipeRefreshLayout swipe_refresh_layout;
    File configFile;
    JSONObject settingsObject;
    Integer metric, time, zone;
    Integer resumeyes = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_info);


        boolean isInternetAvailable = isInternetAvailable(getApplicationContext());
        if (isInternetAvailable) {

        } else {
            Toast.makeText(getApplicationContext(), "Check your internet connection and try again!", Toast.LENGTH_SHORT).show();
        }
        Locale.setDefault(Locale.forLanguageTag("en"));
        configFile = new File(getFilesDir(), "config.json");
        settingsObject = loadSettings();
        if (settingsObject != null) {
            try {
//                temper = settingsObject.getInt("Temperature");

                metric = settingsObject.getInt("Metric");

                time = settingsObject.getInt("Time");

                zone = settingsObject.getInt("Zone");
                if(zone != 0){
                    TimeZone timeZone = TimeZone.getDefault();
                    int rawOffsetInMillis = timeZone.getRawOffset();
                    int offsetHours = rawOffsetInMillis / (60 * 60 * 1000);
                    String dum = (offsetHours >= 0) ? "+" : "-";
                    offsetHours = abs(offsetHours);
                    zoneString = "GMT" + dum + offsetHours;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            settingsObject = new JSONObject();
        }


        location_country = findViewById(R.id.location_country);
        temp_celcius = findViewById(R.id.temp_celsius);
        max_minTemp = findViewById(R.id.max_minTemp);
        weath_desc = findViewById(R.id.weath_desc);
        rain_desc = findViewById(R.id.rain_desc);
        iconview = findViewById(R.id.iconview);
        search_icon = findViewById(R.id.search_icon);
        config_icon = findViewById(R.id.config_icon);

        visib_det = findViewById(R.id.visib_det);
        cloud_det = findViewById(R.id.cloud_det);
        humid_det = findViewById(R.id.humid_det);
        wind_det = findViewById(R.id.wind_det);

        aqi_gene = findViewById(R.id.aqi_gene);
        aqi_gene_add = findViewById(R.id.aqi_gene_add);
        aqi_button = findViewById(R.id.aqi_button);

        swipe_refresh_layout = findViewById(R.id.swipe_refresh_layout);

        hourly_list = findViewById(R.id.hourly_list);
        hourlyAdapter = new HourlyCustomAdapter(this);
        LinearLayoutManager horizontalLayoutManagaer = new LinearLayoutManager(MainInfoActivity.this, LinearLayoutManager.HORIZONTAL, false);
        hourly_list.setLayoutManager(horizontalLayoutManagaer);
        hourly_list.setAdapter(hourlyAdapter);
        hourly_list.setVisibility(View.VISIBLE);

        daily_list = findViewById(R.id.daily_list);
        BottomSheetDialog dayDetail = new BottomSheetDialog(this);
        dayDetail.setContentView(R.layout.day_detail);

        dailyAdapter = new DailyCustomAdapter(this);
        LinearLayoutManager dailyLayout;
        dailyLayout = new LinearLayoutManager(this) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        daily_list.setLayoutManager(dailyLayout);
        daily_list.setAdapter(dailyAdapter);
        daily_list.setVisibility(View.VISIBLE);


            lat = getIntent().getStringExtra("latitude");
            lon = getIntent().getStringExtra("longtitude");

        resumeyes = 0;
        Log.d("gatee", lat+lon);
        getMainInfo();
        aqi_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainInfoActivity.this, aqi_info.class);
                intent.putExtra("ref_info", ref_info);
                intent.putExtra("logoStr", logoStr);
                intent.putExtra("dominant_poll", dominant_poll);
                intent.putExtra("aqi_index", aqi_index);
                intent.putExtra("pm10", pm10);
                intent.putExtra("pm25", pm25);
                intent.putExtra("so2", so2);
                intent.putExtra("no2", no2);
                intent.putExtra("o3", o3);
                intent.putExtra("co", co);
                startActivity(intent);
            }
        });
        search_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainInfoActivity.this, showSearch.class);
                startActivity(intent);
            }
        });
        config_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainInfoActivity.this, config_user.class);
                intent.putExtra("latitude", lat);
                intent.putExtra("longtitude", lon);
                startActivity(intent);

            }
        });

        swipe_refresh_layout.setDistanceToTriggerSync(300);
        swipe_refresh_layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                recreateActivity();
            }
        });
    }
    JSONObject loadSettings() {
        JSONObject jsonObject = null;
        try {
            if (configFile.exists()) {
                BufferedReader br = new BufferedReader(new FileReader(configFile));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                br.close();
                jsonObject = new JSONObject(sb.toString());
            } else {
                JSONObject blank = new JSONObject();
                BufferedWriter bw = new BufferedWriter(new FileWriter(configFile, false));
                bw.write(blank.toString());
                bw.close();
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }
    void getMainInfo(){
        String url = "https://api.openweathermap.org/data/2.5/weather?lat=" + lat + "&lon=" + lon + "&appid=" + appidExtra;
//        Log.d("currentlyyyy", url);
        StringRequest weather_request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonRes = new JSONObject(response);
                    JSONObject sys = jsonRes.getJSONObject("sys");

                    JSONArray arrWeather = jsonRes.getJSONArray("weather");
                    JSONObject weather = arrWeather.getJSONObject(0);
                    JSONObject main = jsonRes.getJSONObject("main");
                    JSONObject wind = jsonRes.getJSONObject("wind");
                    JSONObject clouds = jsonRes.getJSONObject("clouds");

                    IconUrl = "https://openweathermap.org/img/wn/" + weather.getString("icon") + "@4x.png";
                    Glide.with(MainInfoActivity.this).load(IconUrl).into(iconview);

                    cloud_det.setText("Cloudiness: " + clouds.getInt("all") + "%");
                    humid_det.setText("Humidity: " + main.getInt("humidity") + "%");
                    if(jsonRes.has("rain")){
                        rain_desc.setVisibility(View.VISIBLE);
                        rain_desc.setText(jsonRes.getJSONObject("rain").getString("1h") + "mm/h of precipitation in the last hour");
                    }
                    if(jsonRes.has("snow")){
                        rain_desc.setVisibility(View.VISIBLE);
                        rain_desc.setText(jsonRes.getJSONObject("snow").getString("1h") + "mm/h of precipitation in the last hour");
                    }
                    weath_desc.setText(weather.getString("description").substring(0, 1).toUpperCase() + weather.getString("description").substring(1));


                    temp = main.getDouble("temp") - 273.15;
                    if(metric == 1){
                        temp_celcius.setText(nodecimal.format(temp) + "°C");
                        visib_det.setText("Visibility: " + jsonRes.getInt("visibility")/1000 + "km");
                        wind_det.setText("Wind: " + onedecimal.format(wind.getDouble("speed")) + "m/s " + windDegreeToDir(wind.getInt("deg")));
                    } else {
                        temp_celcius.setText(nodecimal.format(temp*1.8+32) + "°F");
                        visib_det.setText("Visibility: " + onedecimal.format(jsonRes.getInt("visibility")/1000*0.6213) + "mi");
                        wind_det.setText("Wind: " + onedecimal.format(wind.getDouble("speed")*2.237) + "mph " + windDegreeToDir(wind.getInt("deg")));
                    }
                    Locale loc = new Locale("", sys.getString("country"));
                    location_country.setText(jsonRes.getString("name") + ",\n" + loc.getDisplayCountry());

                    sunrise = sys.getLong("sunrise");
                    sunset = sys.getLong("sunset");
                    if(zone == 0){
                        int localTimeZone = jsonRes.getInt("timezone");
                        if(localTimeZone >= 0){
                            zoneString = "GMT+" + localTimeZone/3600;
                        } else {
                            zoneString = "GMT" + localTimeZone/3600;
                        }
//                        Log.d("zoneeeeee", zoneString);
                    }
                    getTimeRelated();
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Current weather request failed!", Toast.LENGTH_SHORT).show();
            }
        });
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(weather_request);
        url = "https://api.waqi.info/feed/geo:" + lat + ";" + lon + "/?token=" + aqiToken;
//        Log.d("aqiiii", url);
        StringRequest aqiRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonRes = new JSONObject(response);
                    JSONObject data = jsonRes.getJSONObject("data");
                    aqi_index = data.getInt("aqi");
                    ref_info = data.getJSONObject("city").getString("name");
                    JSONObject attributions0 = data.getJSONArray("attributions").getJSONObject(0);
                    if(attributions0.has("logo")){
                        logoStr = attributions0.getString("logo");
                    }
                    else{
                        logoStr = "logoNull";
                    }

                    if(data.has("dominentpol")){
                        dominant_poll = data.getString("dominentpol");
                    }
                    else{
                        dominant_poll = "333333";
                    }
                    JSONObject iaqi = data.getJSONObject("iaqi");
                    if(iaqi.has("pm10")){
                        pm10 = iaqi.getJSONObject("pm10").getInt("v");
                    }
                    else{
                        pm10 = 333333;
                    }
                    if(iaqi.has("pm25")){
                        pm25 = iaqi.getJSONObject("pm25").getInt("v");
                    }
                    else{
                        pm25 = 333333;
                    }
                    if(iaqi.has("no2")){
                        no2 = iaqi.getJSONObject("no2").getInt("v");
                    }
                    else{
                        no2 = 333333;
                    }
                    if(iaqi.has("so2")){
                        so2 = iaqi.getJSONObject("so2").getInt("v");
                    }
                    else{
                        so2 = 333333;
                    }
                    if(iaqi.has("o3")){
                        o3 = iaqi.getJSONObject("o3").getInt("v");
                    }
                    else{
                        o3 = 333333;
                    }
                    if(iaqi.has("co")){
                        co = iaqi.getJSONObject("co").getInt("v");
                    }
                    else{
                        co = 333333;
                    }
//                    Log.d("indexxxxxx", aqi_index.toString());
                    if(aqi_index <= 50){
                        aqi_gene.setText("Good");
                        aqi_gene_add.setVisibility(View.GONE);
                        aqi_button.setBackgroundColor(Color.parseColor("#32CD32"));
                    } else if (aqi_index > 50 && aqi_index <= 100) {
                        aqi_gene.setText("Moderate");
                        aqi_gene_add.setVisibility(View.GONE);
                        aqi_button.setBackgroundColor(Color.parseColor("#e5c700"));
                    } else if (aqi_index > 100 && aqi_index <= 150) {
                        aqi_gene.setText("Unhealthy");
                        aqi_gene_add.setVisibility(View.VISIBLE);
                        aqi_gene_add.setText("for sensitive groups");
                        aqi_button.setBackgroundColor(Color.parseColor("#FFAC1C"));
                    } else if (aqi_index > 150 && aqi_index <= 200) {
                        aqi_gene.setText("Unhealthy");
                        aqi_gene_add.setVisibility(View.GONE);
                        aqi_button.setBackgroundColor(Color.parseColor("#FF0000"));
                    } else if (aqi_index > 200 && aqi_index <= 300) {
                        aqi_gene.setText("Very unhealthy");
                        aqi_gene_add.setVisibility(View.GONE);
                        aqi_button.setBackgroundColor(Color.parseColor("#800080"));
                    } else if (aqi_index > 300 && aqi_index <= 500) {
                        aqi_gene.setText("Hazardous");
                        aqi_gene_add.setVisibility(View.GONE);
                        aqi_button.setBackgroundColor(Color.parseColor("#800000"));
                    } else {
                        aqi_gene.setText("Very Hazardous");
                        aqi_gene_add.setVisibility(View.GONE);
                        aqi_button.setBackgroundColor(Color.parseColor("#400000"));
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
//                Log.d("AQI failed", error.toString());
                Toast.makeText(getApplicationContext(), "AQI request failed!", Toast.LENGTH_SHORT).show();
            }
        });
        requestQueue.add(aqiRequest);

    }
    void getTimeRelated(){
        String url = "https://pro.openweathermap.org/data/2.5/forecast/hourly?lat="
                + lat + "&lon=" + lon + "&appid=" + appidExtra;
        StringRequest hourlyRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONObject jsonRes = new JSONObject(response);
                    JSONArray list = jsonRes.getJSONArray("list");
                    List<String> results = new ArrayList<>();
                    String buff, sunrise_string, sunset_string;
                    sunset_string = unixt_to_HHMM(sunset, zoneString);
                    Integer sunset_ex = parseInt(sunset_string.substring(0, 2));
                    sunrise_string = unixt_to_HHMM(sunrise, zoneString);
//                    Log.d("riseeee", sunrise_string);
//                    Log.d("setttttt", sunset_string);
                    Integer sunrise_ex = parseInt(sunrise_string.substring(0, 2));
                    if(time == 0){

                        sunrise_string = hrs24to12(sunrise_string);
                        sunset_string = hrs24to12(sunset_string);
                    }


                    Integer buffer;
                    JSONObject hour;
                    JSONArray weather;
                    for(int i = 0;i<24;i++){
                        //  20:00; 04n, 21°C.
                        hour = list.getJSONObject(i);
                        buff = unixt_to_HHMM(hour.getLong("dt"), zoneString);

                        buffer = parseInt(buff.substring(0, 2));
                        if(buffer - sunrise_ex == 1 || buffer - sunrise_ex == -23){
                            results.add(sunrise_string + "; sunrise, Sunrise");
                        }
                        if (buffer - sunset_ex == 1 || buffer - sunset_ex == -23) {
                            results.add(sunset_string + "; sunset, Sunset");
                        }
                        weather = hour.getJSONArray("weather");
                        JSONObject dumb = weather.getJSONObject(0);

                        if(time == 0){
                            buff = hrs24to12(buff);
                        }
                        buff = buff + "; " + dumb.getString("icon");
                        dumb = hour.getJSONObject("main");
                        temp = dumb.getDouble("temp") - 273.15;
                        if(metric == 1){
                            buff = buff + ", " + nodecimal.format(temp) + "°C";
                        } else {
                            buff = buff + ", " + nodecimal.format(temp*1.8+32) + "°F";
                        }
                        results.add(buff);
                    }
                    hourlyAdapter.updateData(results);

                } catch (JSONException e){
                    throw new RuntimeException(e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Hourly request failed!", Toast.LENGTH_SHORT).show();
            }
        });
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(hourlyRequest);

        url = "https://api.openweathermap.org/data/2.5/forecast/daily?lat=" + lat + "&lon=" + lon + "&cnt=8&appid=" + appidExtra;
//        Log.d("dailyyyy", url);
        StringRequest dailyRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonRes = new JSONObject(response);
                    JSONArray list = jsonRes.getJSONArray("list");
                    List<String> dailyFore = new ArrayList<>();
                    String buff;
                    long day_time;
                    JSONObject day_info, temp;
                    for(int i = 0;i < 8;i++){
                        day_info = list.getJSONObject(i);
                        temp = day_info.getJSONObject("temp");
                        if(i != 0){
                            buff = i + ": ";
                            day_time = day_info.getLong("dt");
                            buff = buff + unixt_to_EEMD(day_time, zoneString);
                            buff = buff + "; " + day_info.getJSONArray("weather").getJSONObject(0).getString("icon");
                            if(metric == 1){
                                buff = buff + " " + nodecimal.format(temp.getDouble("max") - 273.15) + "°C/" + nodecimal.format(temp.getDouble("min") - 273.15) + "°C";
                            } else {
                                buff = buff + " " + nodecimal.format((temp.getDouble("max") - 273.15)*1.8+32) + "°F/" + nodecimal.format((temp.getDouble("min") - 273.15)*1.8+32) + "°F";
                            }

                            dailyFore.add(buff);
                        } else {
                            if(metric == 1){
                                max_minTemp.setText(nodecimal.format(temp.getDouble("max") - 273.15) + "°C/" + nodecimal.format(temp.getDouble("min") - 273.15) + "°C");
                            } else {
                                max_minTemp.setText(nodecimal.format((temp.getDouble("max") - 273.15)*1.8+32) + "°F/" + nodecimal.format((temp.getDouble("min") - 273.15)*1.8+32) + "°F");
                            }

                        }
                    }
                    dailyAdapter.updateData(dailyFore);
                    dailyAdapter.setOnItemClickListener(new DailyCustomAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(String item) {
                            int index = parseInt(item.substring(0, 1));
                            try {
                                JSONObject base = list.getJSONObject(index);
                                showBottomSheetDialog(base);
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Daily weather request failed!", Toast.LENGTH_SHORT).show();
            }
        });
//        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(dailyRequest);
    }
    public static boolean isInternetAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }
    void recreateActivity() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
        swipe_refresh_layout.setRefreshing(false);
    }
    String hrs24to12(String base){
        int start = base.indexOf(":");
        String buff = base.substring(start);
        int bluff = parseInt(base.substring(0, start));
        if(bluff > 12){
            if(bluff - 12 >= 10){
                return buff = (bluff-12) + buff + " pm";
            } else {
                return buff = "0" + (bluff-12) + buff + " pm";
            }

        } else if (bluff < 12) {
            if(bluff == 0){
                return buff = "12" + buff + " am";
            }
            return base + " am";
        } else {
            return base + " pm";
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
    String unixt_to_EEMD(long unix_seconds, String zone){
        Date date = new Date(unix_seconds*1000L);
        SimpleDateFormat jdf = new SimpleDateFormat("EEE, MMM d");
        jdf.setTimeZone(TimeZone.getTimeZone(zone));
        return jdf.format(date);
    }
    String windDegreeToDir(Integer dir) {
        Integer val = (int) ((dir/22.5)+.5);
        String[] arr ={"N","NNE","NE","ENE","E","ESE", "SE", "SSE","S","SSW","SW","WSW","W","WNW","NW","NNW"};
        return arr[(val % 16)];
    }
    public void showBottomSheetDialog(JSONObject res) {

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);

        View bottomSheetView = LayoutInflater.from(this).inflate(R.layout.day_detail, null);

        TextView daily_temp_min_max = bottomSheetView.findViewById(R.id.temper);
        ImageView daily_iconview = bottomSheetView.findViewById(R.id.icon);
        TextView humid_day = bottomSheetView.findViewById(R.id.humid_day);
        TextView wind_day = bottomSheetView.findViewById(R.id.wind_day);
        TextView cloud_day = bottomSheetView.findViewById(R.id.cloud_day);
        TextView precip_day = bottomSheetView.findViewById(R.id.precip_day);
        TextView condi_day = bottomSheetView.findViewById(R.id.condi_day);
        TextView sunset_day = bottomSheetView.findViewById(R.id.sunset_day);
        TextView sunrise_day = bottomSheetView.findViewById(R.id.sunrise_day);
        TextView chancePrecip = bottomSheetView.findViewById(R.id.chancePrecip);
        BarChart temperChart = bottomSheetView.findViewById(R.id.temperChart);



        try {
            JSONObject temp = res.getJSONObject("temp");
            DecimalFormat nodecimal = new DecimalFormat("#");

            cloud_day.setText("Cloudiness: " + res.getInt("clouds") + "%");
            humid_day.setText("Humidity: " + res.getInt("humidity") + "%");
            if(res.has("snow")){
                chancePrecip.setVisibility(View.VISIBLE);
                chancePrecip.setText(nodecimal.format(res.getDouble("pop")*10) + "% chance of precipitation");
                precip_day.setText("Precipitation: " + res.getString("snow") + "mm/h");
            }else if(res.has("rain")){
                chancePrecip.setVisibility(View.VISIBLE);
                chancePrecip.setText(nodecimal.format(res.getDouble("pop")*10) + "% chance of precipitation");
                precip_day.setText("Precipitation: " + res.getString("rain") + "mm/h");
            } else {
                precip_day.setText("No precipitation");
            }
//

            double max = temp.getDouble("max") - 273.15;
            double min = temp.getDouble("min") - 273.15;
            double dday = temp.getDouble("day") - 273.15;
            double dnight = temp.getDouble("night") - 273.15;
            double deve = temp.getDouble("eve") - 273.15;
            double dmorn = temp.getDouble("morn") - 273.15;
            String day, night, eve, morn;
            if(metric == 1){
                daily_temp_min_max.setText(nodecimal.format(max) + "°C/" + nodecimal.format(min) + "°C");
                day = nodecimal.format(dday);
                night = nodecimal.format(dnight);
                eve = nodecimal.format(deve);
                morn = nodecimal.format(dmorn);
            } else {
                day = nodecimal.format(dday*1.8+32);
                night = nodecimal.format(dnight*1.8+32);
                eve = nodecimal.format(deve*1.8+32);
                morn = nodecimal.format(dmorn*1.8+32);
                daily_temp_min_max.setText(nodecimal.format(max*1.8+32) + "°F/" + nodecimal.format(min*1.8+32) + "°F");
            }
            if(metric == 1){
//                visib_det.setText("Visibility: " + jsonRes.getInt("visibility")/1000 + "km");
                wind_day.setText("Wind: " + onedecimal.format(res.getDouble("speed")) + "m/s " + windDegreeToDir(res.getInt("deg")));
            }
            else {
                wind_day.setText("Wind: " + onedecimal.format(res.getDouble("speed")*2.237) + "mph " + windDegreeToDir(res.getInt("deg")));
            }
            String sunset_string = unixt_to_HHMM(res.getLong("sunset"), zoneString);
            String sunrise_string = unixt_to_HHMM(res.getLong("sunrise"), zoneString);
            if(time == 0){
                sunrise_string = hrs24to12(sunrise_string);
                sunset_string = hrs24to12(sunset_string);
            }
            sunrise_day.setText(sunrise_string);
            sunset_day.setText(sunset_string);

            JSONObject weather0 = res.getJSONArray("weather").getJSONObject(0);
            String iconString = weather0.getString("icon");
            int resource = this.getResources().getIdentifier("xx" + iconString, "drawable", this.getPackageName());
            daily_iconview.setImageResource(resource);
            condi_day.setText(weather0.getString("description").substring(0, 1).toUpperCase() + weather0.getString("description").substring(1));


            int ymin = Math.min(Math.min(Math.min(parseInt(day), parseInt(morn)), parseInt(eve)), parseInt(night));
            int ymax = Math.max(Math.max(Math.max(parseInt(day), parseInt(morn)), parseInt(eve)), parseInt(night));

            day = (parseInt(day) + 2*abs(ymin)) + "";
            night = (parseInt(night) + 2*abs(ymin)) + "";
            eve = (parseInt(eve) + 2*abs(ymin)) + "";
            morn = (parseInt(morn) + 2*abs(ymin)) + "";
            ArrayList<BarEntry> entries = new ArrayList<>();
            entries.add(new BarEntry(0, parseInt(night)));
            entries.add(new BarEntry(1, parseInt(morn)));
            entries.add(new BarEntry(2, parseInt(day)));
            entries.add(new BarEntry(3, parseInt(eve)));

            Typeface customTypeface = ResourcesCompat.getFont(MainInfoActivity.this, R.font.bebasneue);
            BarDataSet barDataSet = new BarDataSet(entries, "");
            barDataSet.setColor(Color.rgb(255, 172, 28));
            barDataSet.setValueTextColor(Color.BLACK);
            barDataSet.setValueTextSize(40f);
            barDataSet.setValueTypeface(customTypeface);



            BarData barData = new BarData(barDataSet);
            barData.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    if(metric == 1){
                        return String.valueOf((int) (value - 2*abs(ymin))) + "°C";
                    } else {
                        return String.valueOf((int) (value - 2*abs(ymin))) + "°F";
                    }
                }
            });
            temperChart.setData(barData);


            Description description = new Description();
            description.setEnabled(false);
            temperChart.setDescription(description);
            temperChart.getAxisRight().setEnabled(false);
            temperChart.getAxisLeft().setDrawGridLines(false);
            temperChart.getXAxis().setDrawGridLines(false);

            YAxis yAxis = temperChart.getAxisLeft();
            if(metric == 1){
//                temperChart.getAxisLeft().setAxisMinimum(ymin - 20);
//                temperChart.getAxisRight().setAxisMinimum(ymin - 20);
                yAxis.setAxisMinimum((float) (ymin + 2*abs(ymin) - 0.3*abs(ymin)));
                yAxis.setAxisMaximum((float) (ymax + 2*abs(ymin) + 0.1*abs(ymax)));
            } else {
//                temperChart.getAxisLeft().setAxisMinimum(ymin - 68);
//                temperChart.getAxisRight().setAxisMinimum(ymin - 68);
                yAxis.setAxisMinimum((float) (ymin + 2*abs(ymin) - 0.3*abs(ymin))); // Minimum value on the Y-axis
                yAxis.setAxisMaximum((float) (ymax + 2*abs(ymin) + 0.1*abs(ymax)));
            }

            yAxis.setTextSize(12f); // Set text size for Y-axis labels

            // Custom labels for X-axis (bottom)
            final List<String> labels = new ArrayList<>();
            labels.add("Early");
            labels.add("Morning");
            labels.add("Afternoon");
            labels.add("Evening");

            XAxis xAxis = temperChart.getXAxis();
            xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setLabelCount(entries.size()); // Set label count to match the number of entries
            xAxis.setTextSize(22f); // Set text size for X-axis labels
            xAxis.setTypeface(customTypeface);

            temperChart.setExtraTopOffset(30f);
            temperChart.setExtraBottomOffset(8f);

//            temperChart.getAxisLeft().setStartAtZero(false);
            temperChart.getLegend().setEnabled(false);
            temperChart.getAxisLeft().setEnabled(false);
            temperChart.setTouchEnabled(false); // Disable touch gestures
            temperChart.setDragEnabled(false); // Disable dragging
            temperChart.setScaleEnabled(false); // Disable scaling
            temperChart.setPinchZoom(false);
            temperChart.invalidate();


        } catch (JSONException e) {
            throw new RuntimeException(e);
        }



        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);
        Log.d("Saveeeeee", "Saveeeeee");
        outState.putString("lat", lat);
        outState.putString("lon", lon);
        try {
            File configFile = new File(getFilesDir(), "config.json");
            JSONObject jsonObject = null;
            if (configFile.exists()) {
                BufferedReader br = new BufferedReader(new FileReader(configFile));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                br.close();
                jsonObject = new JSONObject(sb.toString());
            } else {
                saveSettings(new JSONObject());
            }
            jsonObject.put("pre_lat", lat);
            jsonObject.put("pre_lon", lon);
            saveSettings(jsonObject);
        } catch (IOException | JSONException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d("Restoreeeeeeee", "Restoreeeeeeee");
        lat = savedInstanceState.getString("lat");
        lon = savedInstanceState.getString("lon");
    }
    void saveSettings(JSONObject jsonObject) {
        try {
            File configFile = new File(getFilesDir(), "config.json");
            BufferedWriter bw = new BufferedWriter(new FileWriter(configFile, false));
            bw.write(jsonObject.toString());
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    protected void onPause(){
        super.onPause();
        try {
            File configFile = new File(getFilesDir(), "config.json");
            JSONObject jsonObject = null;
            if (configFile.exists()) {
                BufferedReader br = new BufferedReader(new FileReader(configFile));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                br.close();
                jsonObject = new JSONObject(sb.toString());
            } else {
                saveSettings(new JSONObject());
            }
            jsonObject.put("pre_lat", lat);
            jsonObject.put("pre_lon", lon);
            saveSettings(jsonObject);
        } catch (IOException | JSONException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    protected void onResume(){
        super.onResume();
        resumeyes = 1;
        settingsObject = loadSettings();
//        Log.d("REsumeeeeee", "REsumeeeeee");
//        settingsObject = loadSettings();
//        try {
//            lat = settingsObject.getString("pre_lat");
//            lon = settingsObject.getString("pre_lon");
//        } catch (JSONException e) {
//            throw new RuntimeException(e);
//        }
    }
//    @Override
//    protected void onRestart(){
//        super.onRestart();
//        Log.d("REStarttttt", "REStarttttt");
//        settingsObject = loadSettings();
//        try {
//            lat = settingsObject.getString("pre_lat");
//            lon = settingsObject.getString("pre_lon");
//        } catch (JSONException e) {
//            throw new RuntimeException(e);
//        }
//
//    }
//    @Override
//    protected void onStart(){
//        super.onStart();
//        Log.d("Starttttt", "Starttttt");
//        settingsObject = loadSettings();
//        try {
//            lat = settingsObject.getString("pre_lat");
//            lon = settingsObject.getString("pre_lon");
//        } catch (JSONException e) {
//            throw new RuntimeException(e);
//        }
//    }
    public static class HourlyCustomAdapter extends RecyclerView.Adapter<MainInfoActivity.HourlyCustomAdapter.ViewHolder> {

        private final List<String> dataList;
        private final Context context;
        private MainInfoActivity.HourlyCustomAdapter.OnItemClickListener listener;
        public interface OnItemClickListener {
            void onItemClick(String item);
        }
        public void setOnItemClickListener(MainInfoActivity.HourlyCustomAdapter.OnItemClickListener listener) {
            this.listener = listener;
        }

        public HourlyCustomAdapter(Context context) {
            this.context = context;
            dataList = new ArrayList<>();
        }

        public void updateData(List<String> newData) {
            dataList.clear();
            dataList.addAll(newData);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public MainInfoActivity.HourlyCustomAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.hourly_info, parent, false);
            return new MainInfoActivity.HourlyCustomAdapter.ViewHolder(view);
        }


        @Override
        public void onBindViewHolder(@NonNull MainInfoActivity.HourlyCustomAdapter.ViewHolder holder, int position) {
            String item = dataList.get(position);
            holder.bind(item);
        }
        @Override
        public int getItemCount() {
            return dataList.size();
        }
        public class ViewHolder extends RecyclerView.ViewHolder {
            private final TextView hour;
            private final ImageView icon;
            private final TextView temper;
            public ViewHolder(View itemView) {
                super(itemView);
                hour = itemView.findViewById(R.id.hour);
                icon = itemView.findViewById(R.id.icon2);
                temper = itemView.findViewById(R.id.temper1);
            }
            public void bind(String item){
                int start = item.indexOf(";");
                // 13:00; 04d, 25°C
                hour.setText(item.substring(0, start));
                start = item.indexOf(";");
                String iconString = item.substring(start + 2, item.indexOf(","));
                if(iconString.equals("sunrise")){
                    icon.setImageResource(R.drawable.sunrise);
                } else if (iconString.equals("sunset")) {
                    icon.setImageResource(R.drawable.sunset);
                } else {
                    int resource = context.getResources().getIdentifier("xx" + iconString, "drawable", context.getPackageName());
                    icon.setImageResource(resource);
                }
                start = item.indexOf(",");
                temper.setText(item.substring(start + 2));
            }
        }
    }
    public static class DailyCustomAdapter extends RecyclerView.Adapter<MainInfoActivity.DailyCustomAdapter.ViewHolder> {

        private final List<String> dataList;
        private final Context context;
        private MainInfoActivity.DailyCustomAdapter.OnItemClickListener listener;
        public interface OnItemClickListener {
            void onItemClick(String item);
        }
        public void setOnItemClickListener(MainInfoActivity.DailyCustomAdapter.OnItemClickListener listener) {
            this.listener = listener;
        }

        public DailyCustomAdapter(Context context) {
            this.context = context;
            dataList = new ArrayList<>();
        }

        public void updateData(List<String> newData) {
            dataList.clear();
            dataList.addAll(newData);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public MainInfoActivity.DailyCustomAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.daily_info, parent, false);
            return new MainInfoActivity.DailyCustomAdapter.ViewHolder(view);
        }


        @Override
        public void onBindViewHolder(@NonNull MainInfoActivity.DailyCustomAdapter.ViewHolder holder, int position) {
            String item = dataList.get(position);
            holder.bind(item);
        }
        @Override
        public int getItemCount() {
            return dataList.size();
        }
        public class ViewHolder extends RecyclerView.ViewHolder {
            private final TextView date;
            private final ImageView date_icon;
            private final TextView date_temp;
            public ViewHolder(View itemView) {
                super(itemView);
                date = itemView.findViewById(R.id.date);
                date_icon = itemView.findViewById(R.id.date_icon);
                date_temp = itemView.findViewById(R.id.date_temp);
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION && listener != null) {
                            String item = dataList.get(position);
                            listener.onItemClick(item);
                        }
                    }
                });
            }
            public void bind(String item){
                int start = item.indexOf(";");
                // Wed, Nov 22; 04d 18-25°C
                date.setText(item.substring(item.indexOf(":")+2, start));
                String iconString = item.substring(start + 2, start + 5);
                int resource = context.getResources().getIdentifier("xx" + iconString, "drawable", context.getPackageName());
                date_icon.setImageResource(resource);
                date_temp.setText(item.substring(start + 5));
            }
        }

    }
}