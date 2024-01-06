package com.example.weath22;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class SavedLocationList extends AppCompatActivity {
    ImageView back_image, reload_image;
    String lat,lon;
    RecyclerView saved_list;

    showSearch.CustomAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_location_list);

        saved_list = findViewById(R.id.saved_list);
        adapter = new showSearch.CustomAdapter(this);
        saved_list.setLayoutManager(new LinearLayoutManager(this));
        saved_list.setAdapter(adapter);
        saved_list.setVisibility(View.VISIBLE);
        back_image = findViewById(R.id.back_image);
        reload_image = findViewById(R.id.reload_image);
        back_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SavedLocationList.this, showSearch.class);
                startActivity(intent);
                finish();
            }
        });
//        reload_image.setVisibility(View.GONE);
        reload_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = getIntent();;
                startActivity(intent);
                finish();
            }
        });
        Set<String> favorites = new LinkedHashSet<>();
        try {
            FileInputStream fis = this.openFileInput("favorites.txt");
            ObjectInputStream objectInputStream = new ObjectInputStream(fis);
            favorites = (Set<String>) objectInputStream.readObject();
            objectInputStream.close();
            fis.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        List<String> results = new ArrayList<>(favorites);
        adapter.updateData(results);
        adapter.setOnItemClickListener(new showSearch.CustomAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String item) {
                int start = item.indexOf("(");
                int end = item.indexOf(";");
                lat = item.substring(start + 1, end);

                start = item.indexOf(";");
                end = item.indexOf(")");
                lon = item.substring(start + 2, end);
                Log.d("reportttttttt", lat+", "+lon);
                coord_request(lat, lon);
            }
        });


    }
    void coord_request(String lat, String lon) {
        Intent intent = new Intent(SavedLocationList.this, MainInfoActivity.class);
        intent.putExtra("latitude", lat);
        intent.putExtra("longtitude", lon);
        startActivity(intent);
    }
}