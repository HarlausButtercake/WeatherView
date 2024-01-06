package com.example.weath22;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;


public class MainActivity extends AppCompatActivity {
    Button button_auto;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        Toast.makeText(getApplicationContext(), "weather v1.0 Beta First", Toast.LENGTH_LONG).show();

//        button = findViewById(R.id.button);
        button_auto = findViewById(R.id.button_auto);
//        prompt = findViewById(R.id.prompt);

        File configFile = new File(getFilesDir(), "config.json");
        if (!configFile.exists()) {
            JSONObject defaultConfig = new JSONObject();
            try {
                defaultConfig.put("Metric", 0);
                defaultConfig.put("Time", 0);
                defaultConfig.put("Zone", 0);
                defaultConfig.put("Notify", 0);
                defaultConfig.put("Notify_time", "00:00");
                defaultConfig.put("Notify_loc", "21:105");
                defaultConfig.put("Widget_loc", "21:105");
                defaultConfig.put("pre_lat", 333333);
                defaultConfig.put("pre_lon", 333333);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                FileOutputStream outputStream = openFileOutput("config.json", Context.MODE_PRIVATE);
                outputStream.write(defaultConfig.toString().getBytes());
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (configFile.exists()) {
            StringBuilder content = new StringBuilder();
            try {
                BufferedReader reader = new BufferedReader(new FileReader(configFile));
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line);
                }
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (content.toString().trim().equals("{}")) {
                JSONObject defaultConfig = new JSONObject();
                try {
                    defaultConfig.put("Metric", 0);
                    defaultConfig.put("Time", 0);
                    defaultConfig.put("Zone", 0);
                    defaultConfig.put("Notify", 0);
                    defaultConfig.put("Notify_time", "00:00");
                    defaultConfig.put("Notify_loc", "21:105");
                    defaultConfig.put("Widget_loc", "21:105");
                    defaultConfig.put("pre_lat", 333333);
                    defaultConfig.put("pre_lon", 333333);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    FileOutputStream outputStream = openFileOutput("config.json", Context.MODE_PRIVATE);
                    outputStream.write(defaultConfig.toString().getBytes());
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        button_auto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, showSearch.class);
                startActivity(intent);
            }
        });
        JSONObject jsonObject = null;
        try {
            BufferedReader br = new BufferedReader(new FileReader(configFile));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            br.close();
            jsonObject = new JSONObject(sb.toString());
            String lat = jsonObject.getString("pre_lat");
            String lon = jsonObject.getString("pre_lon");
            if(!lat.equals("333333")){
                if(!lon.equals("333333")){
                    Intent intent = new Intent(MainActivity.this, MainInfoActivity.class);
                    intent.putExtra("latitude", lat);
                    intent.putExtra("longtitude", lon);
                    startActivity(intent);
                    finish();
                }
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

}
