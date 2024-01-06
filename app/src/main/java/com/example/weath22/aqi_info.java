package com.example.weath22;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class aqi_info extends AppCompatActivity {
    ImageView logo;
    String ref_info, dominant_poll, logoStr;
    TextView pm10_ind, pm25_ind, no2_ind, so2_ind, o3_ind, co_ind;
    TextView ref_textview;
    Integer pm10_index, pm25_index, no2_index, so2_index, o3_index, co_index, aqi_index;
    TextView aqi_gene;
    LinearLayout aqi_layout;
    LinearLayout pm10_indi, pm25_indi, no2_indi, so2_indi, o3_indi, co_indi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aqi_info);

        logo = findViewById(R.id.logo);
        aqi_layout = findViewById(R.id.aqi_layout);
        aqi_gene = findViewById(R.id.aqi_gene);


        ref_textview = findViewById(R.id.ref_textview);

        pm10_ind = findViewById(R.id.pm10_ind);
        pm25_ind = findViewById(R.id.pm25_ind);
        no2_ind = findViewById(R.id.no2_ind);
        so2_ind = findViewById(R.id.so2_ind);
        o3_ind = findViewById(R.id.o3_ind);
        co_ind = findViewById(R.id.co_ind);

        pm10_indi = findViewById(R.id.pm10_indi);
        pm25_indi = findViewById(R.id.pm25_indi);
        no2_indi = findViewById(R.id.no2_indi);
        so2_indi = findViewById(R.id.so2_indi);
        o3_indi = findViewById(R.id.o3_indi);
        co_indi = findViewById(R.id.co_indi);

        ref_info = getIntent().getStringExtra("ref_info");
        logoStr = getIntent().getStringExtra("logoStr");
        dominant_poll = getIntent().getStringExtra("dominant_poll");
        aqi_index = getIntent().getIntExtra("aqi_index", 333333);
        pm10_index = getIntent().getIntExtra("pm10", 333333);
        pm25_index = getIntent().getIntExtra("pm25", 333333);
        no2_index = getIntent().getIntExtra("no2", 333333);
        so2_index = getIntent().getIntExtra("so2", 333333);
        o3_index = getIntent().getIntExtra("o3", 333333);
        co_index = getIntent().getIntExtra("co", 333333);

//        Log.d("domiiiii", dominant_poll);
        index_layout_set();
        ref_textview.setText("*Data sources from  \"" + ref_info + "\"");
        if(logoStr.equals("logoNull")){
            logo.setImageResource(R.drawable.logonull);
        } else {
            String url = "https://aqicn.org/air/images/feeds/" + logoStr;
            Log.d("logoooooo", url);
            Glide.with(aqi_info.this).load(url).into(logo);
        }
    }

    private void index_layout_set(){
        if(aqi_index <= 50){
            //aqi_gene.setText("Good");
            aqi_layout.setBackgroundResource(R.drawable.background_green);
        } else if (aqi_index > 50 && aqi_index <= 100) {
            aqi_layout.setBackgroundResource(R.drawable.background_yellow);
        } else if (aqi_index > 100 && aqi_index <= 150) {
            aqi_layout.setBackgroundResource(R.drawable.background_orange);
        } else if (aqi_index > 150 && aqi_index <= 200) {
            aqi_layout.setBackgroundResource(R.drawable.background_red);
        } else if (aqi_index > 200 && aqi_index <= 300) {
            aqi_layout.setBackgroundResource(R.drawable.background_purple);
        } else if (aqi_index > 300 && aqi_index <= 500) {
            aqi_layout.setBackgroundResource(R.drawable.background_maroon);
        } else {
            aqi_layout.setBackgroundResource(R.drawable.background_brown);
        }
        if(dominant_poll.equals("pm25")){
            aqi_gene.setText("PM2.5");
        } else if (dominant_poll.equals("333333")) {
            aqi_gene.setText("N/A");
        } else {
            aqi_gene.setText(dominant_poll.toUpperCase());
        }


        if(pm10_index <= 50){
            pm10_indi.setBackgroundResource(R.drawable.background_green);
        } else if (pm10_index > 50 && pm10_index <= 100) {
            pm10_indi.setBackgroundResource(R.drawable.background_yellow);
        } else if (pm10_index > 100 && pm10_index <= 150) {
            pm10_indi.setBackgroundResource(R.drawable.background_orange);
        } else if (pm10_index > 150 && pm10_index <= 200) {
            pm10_indi.setBackgroundResource(R.drawable.background_red);
        } else if (pm10_index > 200 && pm10_index <= 300) {
            pm10_indi.setBackgroundResource(R.drawable.background_purple);
        } else if (pm10_index > 300 && pm10_index <= 500) {
            pm10_indi.setBackgroundResource(R.drawable.background_maroon);
        } else if (pm10_index == 333333) {
            pm10_indi.setBackgroundResource(R.drawable.background_grey);
        }else {
            pm10_indi.setBackgroundResource(R.drawable.background_brown);
        }

        if(pm25_index <= 50){
            pm25_indi.setBackgroundResource(R.drawable.background_green);
        } else if (pm25_index > 50 && pm25_index <= 100) {
            pm25_indi.setBackgroundResource(R.drawable.background_yellow);
        } else if (pm25_index > 100 && pm25_index <= 150) {
            pm25_indi.setBackgroundResource(R.drawable.background_orange);
        } else if (pm25_index > 150 && pm25_index <= 200) {
            pm25_indi.setBackgroundResource(R.drawable.background_red);
        } else if (pm25_index > 200 && pm25_index <= 300) {
            pm25_indi.setBackgroundResource(R.drawable.background_purple);
        } else if (pm25_index > 300 && pm25_index <= 500) {
            pm25_indi.setBackgroundResource(R.drawable.background_maroon);
        } else if (pm25_index == 333333) {
            pm25_indi.setBackgroundResource(R.drawable.background_grey);
        }else {
            pm25_indi.setBackgroundResource(R.drawable.background_brown);
        }

        if(no2_index <= 50){
            no2_indi.setBackgroundResource(R.drawable.background_green);
        } else if (no2_index > 50 && no2_index <= 100) {
            no2_indi.setBackgroundResource(R.drawable.background_yellow);
        } else if (no2_index > 100 && no2_index <= 150) {
            no2_indi.setBackgroundResource(R.drawable.background_orange);
        } else if (no2_index > 150 && no2_index <= 200) {
            no2_indi.setBackgroundResource(R.drawable.background_red);
        } else if (no2_index > 200 && no2_index <= 300) {
            no2_indi.setBackgroundResource(R.drawable.background_purple);
        } else if (no2_index > 300 && no2_index <= 500) {
            no2_indi.setBackgroundResource(R.drawable.background_maroon);
        }else if (no2_index == 333333) {
            no2_indi.setBackgroundResource(R.drawable.background_grey);
        } else {
            no2_indi.setBackgroundResource(R.drawable.background_brown);
        }

        if(so2_index <= 50){
            so2_indi.setBackgroundResource(R.drawable.background_green);
        } else if (so2_index > 50 && so2_index <= 100) {
            so2_indi.setBackgroundResource(R.drawable.background_yellow);
        } else if (so2_index > 100 && so2_index <= 150) {
            so2_indi.setBackgroundResource(R.drawable.background_orange);
        } else if (so2_index > 150 && so2_index <= 200) {
            so2_indi.setBackgroundResource(R.drawable.background_red);
        } else if (so2_index > 200 && so2_index <= 300) {
            so2_indi.setBackgroundResource(R.drawable.background_purple);
        } else if (so2_index > 300 && so2_index <= 500) {
            so2_indi.setBackgroundResource(R.drawable.background_maroon);
        } else if (so2_index == 333333) {
            so2_indi.setBackgroundResource(R.drawable.background_grey);
        }else {
            so2_indi.setBackgroundResource(R.drawable.background_brown);
        }

        if(o3_index <= 50){
            o3_indi.setBackgroundResource(R.drawable.background_green);
        } else if (o3_index > 50 && o3_index <= 100) {
            o3_indi.setBackgroundResource(R.drawable.background_yellow);
        } else if (o3_index > 100 && o3_index <= 150) {
            o3_indi.setBackgroundResource(R.drawable.background_orange);
        } else if (o3_index > 150 && o3_index <= 200) {
            o3_indi.setBackgroundResource(R.drawable.background_red);
        } else if (o3_index > 200 && o3_index <= 300) {
            o3_indi.setBackgroundResource(R.drawable.background_purple);
        } else if (o3_index > 300 && o3_index <= 500) {
            o3_indi.setBackgroundResource(R.drawable.background_maroon);
        } else if (o3_index == 333333) {
            o3_indi.setBackgroundResource(R.drawable.background_grey);
        }else {
            o3_indi.setBackgroundResource(R.drawable.background_brown);
        }

        if(co_index <= 50){
            co_indi.setBackgroundResource(R.drawable.background_green);
        } else if (co_index > 50 && co_index <= 100) {
            co_indi.setBackgroundResource(R.drawable.background_yellow);
        } else if (co_index > 100 && co_index <= 150) {
            co_indi.setBackgroundResource(R.drawable.background_orange);
        } else if (co_index > 150 && co_index <= 200) {
            co_indi.setBackgroundResource(R.drawable.background_red);
        } else if (co_index > 200 && co_index <= 300) {
            co_indi.setBackgroundResource(R.drawable.background_purple);
        } else if (co_index > 300 && co_index <= 500) {
            co_indi.setBackgroundResource(R.drawable.background_maroon);
        } else if (co_index == 333333) {
            co_indi.setBackgroundResource(R.drawable.background_grey);
        } else {
            co_indi.setBackgroundResource(R.drawable.background_brown);
        }

        if(pm10_index == 333333){
            pm10_ind.setText("N/A");
        } else {
            pm10_ind.setText(pm10_index.toString());
        }
        if(pm25_index == 333333){
            pm25_ind.setText("N/A");
        } else {
            pm25_ind.setText(pm25_index.toString());
        }
        if(so2_index == 333333){
            so2_ind.setText("N/A");
        } else {
            so2_ind.setText(so2_index.toString());
        }
        if(no2_index == 333333){
            no2_ind.setText("N/A");
        } else {
            no2_ind.setText(no2_index.toString());
        }
        if(o3_index == 333333){
            o3_ind.setText("N/A");
        } else {
            o3_ind.setText(o3_index.toString());
        }
        if(co_index == 333333){
            co_ind.setText("N/A");
        } else {
            co_ind.setText(co_index.toString());
        }
    }

}

