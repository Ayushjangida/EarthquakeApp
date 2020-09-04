package com.example.earthquakeapp.Activities;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.earthquakeapp.R;
import com.example.earthquakeapp.model.EarthQuake;
import com.example.earthquakeapp.ui.CustomInfoWindow;
import com.example.earthquakeapp.util.Constants;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener,
        GoogleMap.OnMarkerClickListener {
    private LocationManager locationManager;
    private LocationListener locationListener;
    private RequestQueue queue;
    private JsonObjectRequest jsonObjectRequest;
    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;
    private Button showListBtn;


    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        queue = Volley.newRequestQueue(this);
        getEarthQuake();
        showListBtn = findViewById(R.id.showListButton);
        showListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MapsActivity.this, QuakesListActivity.class);
                startActivityForResult(intent, 0);
                finish();
            }
        });
        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
        if (Build.VERSION.SDK_INT < 23) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        } else {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }else   {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
//                mMap.addMarker(new MarkerOptions()
//                    .position(latLng)
//                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
//                    .title("My Location"));
//                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,7));
            }
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void getEarthQuake() {
        final EarthQuake earthQuake = new EarthQuake();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, Constants.URL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            JSONArray features = response.getJSONArray("features");
                            for (int i = 0; i < features.length(); i++) {
                                JSONObject properties = features.getJSONObject(i).getJSONObject("properties");
                                Log.d("properties", "onResponse: "+ i + " " + properties.getString("place"));

                                //Get geomety object
                                JSONObject geometry = features.getJSONObject(i).getJSONObject("geometry");

                                //get coordinatea array
                                JSONArray coordinates = geometry.getJSONArray("coordinates");
                                double lon = coordinates.getDouble(0);
                                double lat = coordinates.getDouble(1);

                                Log.d("Quake", lon + " " + lat);
                                earthQuake.setPlace(properties.getString("place"));
                                earthQuake.setType(properties.getString("type"));
                                earthQuake.setLatitude(lat);
                                earthQuake.setLongitude(lon);
                                earthQuake.setMagnitude(properties.getDouble("mag"));
                                earthQuake.setDetailLink(properties.getString("detail"));

                                java.text.DateFormat dateFormat = java.text.DateFormat.getDateInstance();
                                String formattedDate = dateFormat.format(new Date(Long.valueOf(properties.getLong("time"))).getTime());

                                //add circles
                                if(earthQuake.getMagnitude() >= 3)  {
                                    CircleOptions circleOptions = new CircleOptions();
                                    circleOptions.center(new LatLng(lat,lon));
                                    circleOptions.radius(30000);
                                    circleOptions.strokeWidth(3.6f);
                                    circleOptions.fillColor(Color.RED);
                                    mMap.addCircle(circleOptions);

                                }

                                MarkerOptions markerOptions = new MarkerOptions();
                                if (earthQuake.getMagnitude() < 1)  {
                                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                                }
                                else if(earthQuake.getMagnitude() >= 1 && earthQuake.getMagnitude() < 2) {
                                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                                }
                                else if(earthQuake.getMagnitude() >= 2 && earthQuake.getMagnitude() < 3)    {
                                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
                                }
                                else if(earthQuake.getMagnitude() >=3 && earthQuake.getMagnitude() < 4) {
                                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
                                }
                                else    {
                                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                                }
                                markerOptions.title(earthQuake.getPlace());
                                markerOptions.position(new LatLng(lat, lon));
                                markerOptions.snippet("Magnitude: " + earthQuake.getMagnitude() + "\n" + "Date: " + formattedDate);
                                Log.d("map", "onResponse: " + markerOptions.getTitle());

                                Marker marker = mMap.addMarker(markerOptions);
                                marker.setTag(earthQuake.getDetailLink());
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon), 1));

                                //Add circle to markers that have magnitude > x

//                                //Add circle to markers that have mag > x
//                                if (earthQuake.getMagnitude() >= 2.0 ) {
//                                    CircleOptions circleOptions = new CircleOptions();
//                                    circleOptions.center(new LatLng(earthQuake.getLat(),
//                                            earthQuake.getLon()));
//                                    circleOptions.radius(30000);
//                                    circleOptions.strokeWidth(2.5f);
//                                    circleOptions.fillColor(Color.RED);
//
//                                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
//
//                                    mMap.addCircle(circleOptions);
//
//                                }
//
//                                Marker marker = mMap.addMarker(markerOptions);
//                                marker.setTag(earthQuake.getDetailLink());
//
//                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon), 1));

                            }

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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)  {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            }
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnInfoWindowClickListener(this);
        mMap.setOnMarkerClickListener(this);
        mMap.setInfoWindowAdapter(new CustomInfoWindow(getApplicationContext()));

        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
//        Toast.makeText(getApplicationContext(),marker.getTag().toString(), Toast.LENGTH_SHORT).show();
        getQuakeDetails(marker.getTag().toString());

    }

    private void getQuakeDetails(String url) {

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                String detailsUrl = "";

                try {
                    JSONObject properties = response.getJSONObject("properties");
                    JSONObject products = properties.getJSONObject("products");
                    JSONArray geoserve = products.getJSONArray("geoserve");

                    for (int i = 0; i < geoserve.length(); i++) {
                        JSONObject geoserveObj = geoserve.getJSONObject(i);

                        JSONObject contentObj = geoserveObj.getJSONObject("contents");
                        JSONObject geoJsonObj = contentObj.getJSONObject("geoserve.json");

                        detailsUrl = geoJsonObj.getString("url");


                    }
                    Log.d("URL: ", detailsUrl);

                    getMoreDetails(detailsUrl);


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

    public void getMoreDetails(String url)  {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        dialogBuilder = new AlertDialog.Builder(MapsActivity.this);

                        View view = getLayoutInflater().inflate(R.layout.popup, null);

                        Button dismissButton = view.findViewById(R.id.dismissPop);
                        Button dismissButtonTop = view.findViewById(R.id.dismissPopTop);
                        TextView popList = view.findViewById(R.id.poplist);
                        WebView htmlPop = view.findViewById(R.id.HtmlWebView);

                        StringBuilder stringBuilder = new StringBuilder();
                        try {
                            if(response.has("tectonicSummary") && response.getString("tectonicSummary") != null)    {
                                JSONObject techtonic = response.getJSONObject("tectonicSummary");
                                if(techtonic.has("text") && techtonic.getString("text") != null)    {
                                    String text = techtonic.getString("text");
                                    htmlPop.loadDataWithBaseURL(null, text, "text/html", "UTF-8", null);
                                }
                            }
                            JSONArray cities = response.getJSONArray("cities");
                            for(int i = 0; i < cities.length(); i++)    {
                                JSONObject citiesObj = cities.getJSONObject(i);
                                stringBuilder.append("City: " + citiesObj.get("name") + "\n"
                                                    + "Distance: " + citiesObj.get("distance") + "\n"
                                                    + "Population: " + citiesObj.get("population"));
                                stringBuilder.append("\n \n");

                            }
                            popList.setText(stringBuilder);

                            dismissButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    dialog.dismiss();
                                }
                            });

                            dismissButtonTop.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    dialog.dismiss();
                                }
                            });

                            dialogBuilder.setView(view);
                            dialog = dialogBuilder.create();
                            dialog.show();
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

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }
}
