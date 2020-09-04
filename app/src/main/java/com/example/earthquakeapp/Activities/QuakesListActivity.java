package com.example.earthquakeapp.Activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.earthquakeapp.R;
import com.example.earthquakeapp.model.EarthQuake;
import com.example.earthquakeapp.util.Constants;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class QuakesListActivity extends AppCompatActivity {
    private ArrayList<String> arrayList;
    private ListView listView;
    private RequestQueue queue;
    private ArrayAdapter<String> arrayAdapter;
    private EarthQuake earthQuake;
   private FloatingActionButton cancelButton;
    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;

    private List<EarthQuake> quakeList;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quake_list);
        earthQuake = new EarthQuake();

        quakeList = new ArrayList<>();
        listView = findViewById(R.id.listview);
        cancelButton = findViewById(R.id.floatingActionButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(QuakesListActivity.this, MapsActivity.class);
                startActivityForResult(intent, 0);
                finish();
            }
        });


        queue = Volley.newRequestQueue(this);
        arrayList = new ArrayList<>();

        getAllQuakes(Constants.URL);
    }

    public void getAllQuakes(String url)    {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray features = response.getJSONArray("features");
                            for (int i = 0; i < features.length(); i++) {
                                JSONObject properties = features.getJSONObject(i).getJSONObject("properties");
                                earthQuake.setPlace(properties.getString("place"));
                                earthQuake.setDetailLink(properties.getString("detail"));
                                earthQuake.setMagnitude(properties.getDouble("mag"));
                                JSONObject geometry = features.getJSONObject(i).getJSONObject("geometry");
                                JSONArray coordinates = geometry.getJSONArray("coordinates");
                                double lon = coordinates.getDouble(0);
                                double lat = coordinates.getDouble(1);

                                java.text.DateFormat dateFormat = java.text.DateFormat.getDateInstance();
                                String formattedDate = dateFormat.format(new Date(Long.valueOf(properties.getLong("time"))).getTime());


                                arrayList.add(earthQuake.getPlace());
                            }
                            arrayAdapter = new ArrayAdapter<>(QuakesListActivity.this, android.R.layout.simple_list_item_1,
                                    android.R.id.text1, arrayList);
                            listView.setAdapter(arrayAdapter);
                            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                    //Toast.makeText(QuakesListActivity.this,"Clicked " + i, Toast.LENGTH_SHORT).show();

                                }
                            });
                            arrayAdapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        queue.add(jsonObjectRequest);
    }
}
