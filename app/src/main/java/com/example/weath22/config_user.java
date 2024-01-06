package com.example.weath22;

import static java.lang.Integer.parseInt;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.util.Calendar;
import android.icu.util.TimeZone;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class config_user extends AppCompatActivity {
    Switch noti_switch, time_switch, zone_switch, dist_switch;
    String lat, lon;
    File configFile;
    JSONObject settingsObject;
    TextView currZone;
    ImageView back_image;
    LinearLayout setTime, layout_denug;
    TextView timeShow;
    Button saved_button2, widget_button;
    String timeDest;

    private static final int PERMISSION_REQUEST_CODE = 200;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config_user);

        dist_switch = findViewById(R.id.dist_switch);
        time_switch = findViewById(R.id.time_switch);
        noti_switch = findViewById(R.id.noti_switch);
        zone_switch = findViewById(R.id.zone_switch);
        back_image = findViewById(R.id.back_image);
        saved_button2 = findViewById(R.id.saved_button2);
        setTime = findViewById(R.id.setTime);
        timeShow = findViewById(R.id.timeShow);
        layout_denug = findViewById(R.id.layout_denug);
        widget_button = findViewById(R.id.widget_button);

        lat = getIntent().getStringExtra("latitude");
        lon = getIntent().getStringExtra("longtitude");

//        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        currZone = findViewById(R.id.currZone);
        TimeZone timeZone = TimeZone.getDefault();
        int rawOffsetInMillis = timeZone.getRawOffset();
        int offsetHours = rawOffsetInMillis / (60 * 60 * 1000);
        String gmtOffsetSign = (offsetHours >= 0) ? "+" : "-";
        offsetHours = Math.abs(offsetHours);
        currZone.setText("Your current time zone is GMT" + gmtOffsetSign + offsetHours);

        configFile = new File(getFilesDir(), "config.json");
        settingsObject = loadSettings();


        if (settingsObject != null) {
            try {
                timeDest = settingsObject.getString("Notify_time");
                String loc = settingsObject.getString("Notify_loc");
                int fore = loc.indexOf(":");
                String loclat = loc.substring(0, fore);
                String loclon = loc.substring(fore+1);
                loc = settingsObject.getString("Widget_loc");
                fore = loc.indexOf(":");
                String widgetloclat = loc.substring(0, fore);
                String widgetloclon = loc.substring(fore+1);
                Log.d("coordadsa", loclat + " and "+ loclon);
                if(!loclat.equals(lat) || !loclon.equals(lon)){
                    saved_button2.setText("Notify me about this location");
                    saved_button2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            try {
                                settingsObject.put("Notify_loc", lat +":"+ lon);
                                saveSettings(settingsObject);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            saved_button2.setText("Receiving notification for this location");
                            disableNotificationWorker();
                            resetNotiSw();
                        }
                    });
                } else {
                    saved_button2.setText("Receiving notification for this location");
                }
                if(!widgetloclat.equals(lat) || !widgetloclon.equals(lon)){
                    widget_button.setText("Assign next widget to showcase this location");
                    widget_button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            try {
                                settingsObject.put("Widget_loc", lat +":"+ lon);
                                saveSettings(settingsObject);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            widget_button.setText("Next widget created will showcase this location");

                        }
                    });
                } else {
                    saved_button2.setText("Next widget created will showcase this location");
                }

                int dist = settingsObject.getInt("Metric");
                dist_switch.setChecked(toBool(dist));

                int time = settingsObject.getInt("Time");
                time_switch.setChecked(toBool(time));
                setTimeShown(timeDest, time);

                int zone = settingsObject.getInt("Zone");
                zone_switch.setChecked(toBool(zone));

                int noti = settingsObject.getInt("Notify");
                noti_switch.setChecked(toBool(noti));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            settingsObject = new JSONObject();
        }
        setTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JSONObject dumb = new JSONObject();
                dumb = loadSettings();
                try {
                    Integer timemode = dumb.getInt("Time");
                    String timeinfo = dumb.getString("Notify_time");
                    showTimeSet(timemode, timeinfo);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });

        dist_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateSetting("Metric", isChecked ? 1 : 0);
            }
        });
        time_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateSetting("Time", isChecked ? 1 : 0);
            }
        });
        zone_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateSetting("Zone", isChecked ? 1 : 0);
            }
        });
        back_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(config_user.this, MainInfoActivity.class);
                intent.putExtra("latitude", lat);
                intent.putExtra("longtitude", lon);
                startActivity(intent);
                finish();
            }
        });

        noti_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateSetting("Notify", isChecked ? 1 : 0);
                if (isChecked) {
                    enableNotificationWorker();
                } else {
                    disableNotificationWorker();
                }
            }
        });

    }


    private void enableNotificationWorker() {
        if (hasNotificationPermission()) {
            scheduleNotificationWorker();
        } else {
            requestNotificationPermission();
        }
    }
    private void disableNotificationWorker() {
        WorkManager.getInstance(this).cancelAllWork();
        Toast.makeText(this, "Notification canceled", Toast.LENGTH_SHORT).show();
    }

    private boolean hasNotificationPermission() {
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestNotificationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                scheduleNotificationWorker();
            } else {
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show();
                noti_switch.setChecked(false);
            }
        }
    }
    private void scheduleNotificationWorker() {
        File sett = new File(getFilesDir(), "config.json");
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
        Calendar calendar = Calendar.getInstance();
        String coord;
        Data inputData;
        try {
            String timetoSet = jsonObject.getString("Notify_time");
            coord = jsonObject.getString("Notify_loc");
            inputData = new Data.Builder()
                    .putString("lat", coord.substring(0, coord.indexOf(":")))
                    .putString("lon", coord.substring(coord.indexOf(":") + 1))
                    .putString("temp_mode", jsonObject.getString("Metric"))
                    .putString("timer", jsonObject.getString("Notify_time"))
                    .build();
            calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timetoSet.substring(0, timetoSet.indexOf(":"))));
            calendar.set(Calendar.MINUTE, Integer.parseInt(timetoSet.substring(timetoSet.indexOf(":") + 1)));
            calendar.set(Calendar.SECOND, 0);
            if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                calendar.add(Calendar.DATE, 1);
            }

            long initialDelay = calendar.getTimeInMillis() - System.currentTimeMillis();
            // Create OneTimeWorkRequest with initial delay


            OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(notiWorker.class)
                    .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                    .setInputData(inputData)
                    .build();
            WorkManager.getInstance(getApplicationContext()).enqueue(workRequest);
            Toast.makeText(this, "Notification set at " + timetoSet, Toast.LENGTH_SHORT).show();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }




//////////////////////////////////
    //////////////////////////////////
    //////////////////////////


    void resetNotiSw(){
        noti_switch.setChecked(false);
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
    void setTimeShown(String timeStr, int timeset) {
        if(timeset == 0) {
            timeShow.setText(" " + hrs24to12(timeStr) + " ");
        } else {
            timeShow.setText(" " + timeStr + " ");
        }
    }
    public void showTimeSet(int timeMode, String timeinfo) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View bottomSheetView = LayoutInflater.from(this).inflate(R.layout.noti_time, null);
        bottomSheetDialog.setOnShowListener(dialog -> {
            View timePickerView = bottomSheetDialog.findViewById(R.id.timingSetter); // Replace with your TimePicker's ID

            if (timePickerView instanceof TimePicker) {
                TimePicker timePicker = (TimePicker) timePickerView;
                Button button = bottomSheetView.findViewById(R.id.button);
                timePicker.setHour(Integer.parseInt(timeinfo.substring(0, timeinfo.indexOf(":"))));
                timePicker.setMinute(Integer.parseInt(timeinfo.substring(timeinfo.indexOf(":") + 1)));
                if (timeMode == 0)   {
                    timePicker.setIs24HourView(false);
                } else {
                    timePicker.setIs24HourView(true);
                }
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String formattedTime = String.format("%02d:%02d", timePicker.getHour(), timePicker.getMinute());
                        try {
                            settingsObject.put("Notify_time", formattedTime);
                            settingsObject.put("Notify", 0);
                            saveSettings(settingsObject);
                            resetNotiSw();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        setTimeShown(formattedTime, timeMode);
                        bottomSheetDialog.dismiss();
                    }
                });
            } else {

            }
        });
        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }
    boolean toBool(int a){
        return a != 0;
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
                saveSettings(new JSONObject());
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }
    void saveSettings(JSONObject jsonObject) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(configFile, false));
            bw.write(jsonObject.toString());
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    void updateSetting(String key, int value) {
        try {
            settingsObject.put(key, value);
            saveSettings(settingsObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}