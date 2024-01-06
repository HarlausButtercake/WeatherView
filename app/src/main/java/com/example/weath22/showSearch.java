package com.example.weath22;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


public class showSearch extends AppCompatActivity {

    SearchView location_search;
    RecyclerView result_list;
    CustomAdapter adapter;
    private final String appidExtra = "72ef7ccc6fd8601a88a0d7e08e8e1181";
    //private final String aqiToken = "29f258f9a7b2aa7b8e76536c1cc85f7cd1a25320";
    Button button_auto2, saved_button;
    private FusedLocationProviderClient fusedLocationClient;
    String lat, lon;
//    ProgressBar loading_anim;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_search);

        button_auto2 = findViewById(R.id.button_auto2);
        saved_button = findViewById(R.id.saved_button);

        location_search = findViewById(R.id.location_search);
        location_search.clearFocus();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        result_list = findViewById(R.id.result_list);

//        loading_anim = findViewById(R.id.loading_anim);
//        loading_anim.setVisibility(View.INVISIBLE);

        adapter = new CustomAdapter(this);


        result_list.setLayoutManager(new LinearLayoutManager(this));
        result_list.setAdapter(adapter);
        result_list.setVisibility(View.VISIBLE);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        button_auto2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!checkLocationPermission()) {
                    ActivityCompat.requestPermissions(showSearch.this,
                            new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                            1);
                } else {
                    fusedLocationClient.getLastLocation().addOnSuccessListener(showSearch.this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                String lat = String.valueOf(location.getLatitude());
                                String lon = String.valueOf(location.getLongitude());
                                location = null;
                                coord_request(lat, lon);
                            } else {
                                Toast.makeText(getApplicationContext(), "Location not available!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
        saved_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(showSearch.this, SavedLocationList.class);
                startActivity(intent);
            }
        });
        location_search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
//                loading_anim.setVisibility(View.VISIBLE);
                String search_url = "https://api.openweathermap.org/data/2.5/find?q=" + s + "&type=like&appid=" + appidExtra;
                Log.d("searchhhhhh", search_url);
                StringRequest searchRequest = new StringRequest(Request.Method.POST, search_url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject searchRes = new JSONObject(response);
                            Integer num = searchRes.getInt("count");
                            Integer cod = searchRes.getInt("cod");

                            if (cod != 200) {
                                //prompt.setText("Error " + cod.toString() + "!");
                            } else {
                                if (num == 0) {
                                    Toast.makeText(getApplicationContext(), "No results found!", Toast.LENGTH_SHORT).show();
                                } else {

                                    List<String> results = new ArrayList<>();
                                    JSONArray list = searchRes.getJSONArray("list");
                                    JSONObject location;
                                    String buff;
                                    for(int i = 0;i < num;i++){
                                        location = list.getJSONObject(i);
                                        buff = location.getString("name") + ", " + location.getJSONObject("sys").getString("country")
                                        + " (" + location.getJSONObject("coord").getString("lat") + "; "
                                                + location.getJSONObject("coord").getString("lon") + ")";
                                        // "Ulster, US (40.7128; -74.0060)"

                                        results.add(buff);
                                    }
                                    adapter.updateData(results);
                                    adapter.setOnItemClickListener(new CustomAdapter.OnItemClickListener() {
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
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), "Bad query: No results could be found!", Toast.LENGTH_SHORT).show();
                    }
                });
//                loading_anim.setVisibility(View.VISIBLE);
                RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
                requestQueue.add(searchRequest);
//                loading_anim.setVisibility(View.INVISIBLE);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
    }
    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, get last known location
                fusedLocationClient.getLastLocation().addOnSuccessListener(showSearch.this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            String lat = String.valueOf(location.getLatitude());
                            String lon = String.valueOf(location.getLongitude());
                            coord_request(lat, lon);
                        } else {
                            Toast.makeText(getApplicationContext(), "Location not available!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } else {
                // Permission denied by the user, show a message or handle accordingly
                Toast.makeText(getApplicationContext(), "Location permission denied. Enable it in settings to use this feature.", Toast.LENGTH_SHORT).show();
            }
        }
    }
    void coord_request(String lat, String lon) {
        Intent intent = new Intent(showSearch.this, MainInfoActivity.class);
        intent.putExtra("latitude", lat);
        intent.putExtra("longtitude", lon);
        startActivity(intent);
//        finish();
    }


    public static class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {

        private final List<String> dataList;
        private static Set<String> favoriteItems;
        private final Context context;
        private OnItemClickListener listener;
        public interface OnItemClickListener {
            void onItemClick(String item);
        }
        public void setOnItemClickListener(OnItemClickListener listener) {
            this.listener = listener;
        }

        public CustomAdapter(Context context) {
            this.context = context;
            dataList = new ArrayList<>();
            favoriteItems = readFavoritesFromFile(context, "favorites.txt");
        }

        public void updateData(List<String> newData) {
            dataList.clear();
            dataList.addAll(newData);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.result_list_layout, parent, false);
            return new ViewHolder(view);
        }

        private static Set<String> readFavoritesFromFile(Context context, String filename) {
            Set<String> favorites = new LinkedHashSet<>();
            try {
                FileInputStream fis = context.openFileInput(filename);
                ObjectInputStream objectInputStream = new ObjectInputStream(fis);
                favorites = (Set<String>) objectInputStream.readObject();
                objectInputStream.close();
                fis.close();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            return favorites;
        }
        private void writeFavoritesToFile(Set<String> favorites, String filename) {
            try {
                FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(fos);
                objectOutputStream.writeObject(favorites);
                objectOutputStream.close();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String item = dataList.get(position);
            holder.bind(item);

            if (favoriteItems.contains(item)) {
                holder.favoriteImageView.setImageResource(R.drawable.star_filled);
            } else {
                holder.favoriteImageView.setImageResource(R.drawable.star_unfilled);
            }
            holder.favoriteImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (favoriteItems.contains(item)) {
                        favoriteItems.remove(item);
                        holder.favoriteImageView.setImageResource(R.drawable.star_unfilled);
                    } else {
                        favoriteItems.add(item);
                        holder.favoriteImageView.setImageResource(R.drawable.star_filled);
                    }
                    writeFavoritesToFile(favoriteItems, "favorites.txt");
                    notifyDataSetChanged();
                }
            });
        }
        @Override
        public int getItemCount() {
            return dataList.size();
        }
        public static Set<String> getSetBuffer() {
            return favoriteItems;
        }
        public class ViewHolder extends RecyclerView.ViewHolder {
            private final TextView resultTextView;
            private final ImageView flagImageView;
            private final ImageView favoriteImageView;
            public ViewHolder(View itemView) {
                super(itemView);
                resultTextView = itemView.findViewById(R.id.date);
                flagImageView = itemView.findViewById(R.id.flagImageView);
                favoriteImageView = itemView.findViewById(R.id.favoriteImageView);

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
                int start = item.indexOf("(");
                resultTextView.setText(item.substring(0, start - 1));
                // "Ulster, US (40.7128; -74.0060)"
                String countrycode = item.substring(start - 3, start - 1);
                String flagUrl = "https://flagcdn.com/w80/" + countrycode.toLowerCase() + ".png";
//                Log.d("flaggggggg", flagUrl);
                Glide.with(itemView.getContext()).load(flagUrl).into(flagImageView);
            }
        }
    }
}