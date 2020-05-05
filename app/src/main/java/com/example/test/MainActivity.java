package com.example.test;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Looper;
import android.telecom.CallScreeningService;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    MaterialTextView new_confirmed,total_confirmed,new_deaths,total_deaths,new_recovered,total_recovered,loc_txt,
                     gnew_confirmed,gtotal_confirmed,gnew_deaths,gtotal_deaths,gnew_recovered,gtotal_recovered,
                     city_active,city_confirmed,city_deaths,city_recovered,state_active,state_confirmed,state_deaths,state_recovered,currentCity,currentState;
    SwipeRefreshLayout swipeRefreshLayout;
    ImageView btn_loc_refresh;
    int onc=0,otc=0,ond=0,otd=0,onr=0,otr=0;
    LinearLayout linearLayout;
    MaterialButton btn;
    private RequestQueue requestQueue;
    private static final int REQUEST_CODE_LOCATION_PERMISSION=1;
    double latitude,longitude;
    String CityName="",StateName="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        swipeRefreshLayout=findViewById(R.id.swipe_container);
        new_confirmed=findViewById(R.id.new_confirmed);
        total_confirmed=findViewById(R.id.total_confirmed);
        new_deaths=findViewById(R.id.new_deaths);
        total_deaths=findViewById(R.id.total_deaths);
        new_recovered=findViewById(R.id.new_recovered);
        total_recovered=findViewById(R.id.total_recovered);
        linearLayout=findViewById(R.id.india_container);
        loc_txt=findViewById(R.id.location_txt);
        btn_loc_refresh=findViewById(R.id.loc_refresh);
        gnew_confirmed=findViewById(R.id.gnew_confirmed);
        gtotal_confirmed=findViewById(R.id.gtotal_confirmed);
        gnew_deaths=findViewById(R.id.gnew_deaths);
        gtotal_deaths=findViewById(R.id.gtotal_deaths);
        gnew_recovered=findViewById(R.id.gnew_recovered);
        gtotal_recovered=findViewById(R.id.gtotal_recovered);
        city_active=findViewById(R.id.city_active);
        city_confirmed=findViewById(R.id.city_confirmed);
        city_deaths=findViewById(R.id.city_deaths);
        city_recovered=findViewById(R.id.city_recovered);
        state_active=findViewById(R.id.state_active);
        state_confirmed=findViewById(R.id.state_confirmed);
        state_deaths=findViewById(R.id.state_deaths);
        state_recovered=findViewById(R.id.state_recovered);
        btn=findViewById(R.id.btn);
        currentCity=findViewById(R.id.currentCity);
        currentState=findViewById(R.id.currentState);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent allstat = new Intent(MainActivity.this,allstate.class);
                startActivity(allstat);
            }
        });

        getLocation();
        if(isNetworkConnected())
        {
            requestQueue= Volley.newRequestQueue(this);
            loadData();
            getIndiaData();
            compareIndiaData();
            getGlobalData();
            compareGlobalData();
            getCurrentCityData();
            compareCityData();
            getCurrentStateData();
            compareStateData();
        }
        else
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("No Internet Connection! \nPlease turn on your Internet")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(isNetworkConnected())
                {
                    loadData();
                    getIndiaData();
                    compareIndiaData();
                    getGlobalData();
                    compareGlobalData();
                    getCurrentCityData();
                    compareCityData();
                    getCurrentStateData();
                    compareStateData();
                    saveData();
                    swipeRefreshLayout.setRefreshing(false);
                }
                else
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setMessage("No Internet Connection! \nPlease turn on your Internet")
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }



            }
        });
        btn_loc_refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLocation();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        saveData();
    }

    public boolean isNetworkConnected()
    {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo()!=null && cm.getActiveNetworkInfo().isConnected();
    }

    public void getIndiaData()
    {
        String url="https://api.covid19api.com/summary";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray jsonArray = response.getJSONArray("Countries");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject country = jsonArray.getJSONObject(i);
                        if (country.getString("Country").equals("India")) {
                           new_confirmed.setText(String.valueOf(country.getInt("NewConfirmed")));
                            total_confirmed.setText(String.valueOf(country.getInt("TotalConfirmed")));
                            new_deaths.setText(String.valueOf(country.getInt("NewDeaths")));
                            total_deaths.setText(String.valueOf(country.getInt("TotalDeaths")));
                            new_recovered.setText(String.valueOf(country.getInt("NewRecovered")));
                            total_recovered.setText(String.valueOf(country.getInt("TotalRecovered")));
                        }
                    }
                } catch(JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            error.printStackTrace();
            }
        });
        requestQueue.add(request);
    }

    public void getGlobalData()
    {
        String url="https://api.covid19api.com/summary";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject gjson = (JSONObject) new JSONTokener(response.toString()).nextValue();
                    JSONObject gjson1 = gjson.getJSONObject("Global");
                    gnew_confirmed.setText(gjson1.getString("NewConfirmed"));
                    gtotal_confirmed.setText(gjson1.getString("TotalConfirmed"));
                    gnew_deaths.setText(gjson1.getString("NewDeaths"));
                    gtotal_deaths.setText(gjson1.getString("TotalDeaths"));
                    gnew_recovered.setText(gjson1.getString("NewRecovered"));
                    gtotal_recovered.setText(gjson1.getString("TotalRecovered"));
                } catch(JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(),"Error fetching global data",Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        requestQueue.add(request);
    }

    public void getLocation()
    {
        if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_CODE_LOCATION_PERMISSION);
        }
        else
        {
            final LocationRequest locationRequest = new LocationRequest();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            LocationServices.getFusedLocationProviderClient(MainActivity.this).requestLocationUpdates(locationRequest,new LocationCallback(){
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    super.onLocationResult(locationResult);
                    LocationServices.getFusedLocationProviderClient(MainActivity.this).removeLocationUpdates(this);
                    if(locationResult!=null && locationResult.getLocations().size()>0){
                        int latestlocationindex = locationResult.getLocations().size()-1;
                        latitude=locationResult.getLocations().get(latestlocationindex).getLatitude();
                        longitude=locationResult.getLocations().get(latestlocationindex).getLongitude();
                    }
                    Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                    List<Address> addresses = null;
                    try {
                        addresses = geocoder.getFromLocation (latitude, longitude, 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    CityName=addresses.get(0).getSubAdminArea();
                    StateName=addresses.get(0).getAdminArea();
                    if(CityName.equals("Bagalkot"))
                    {
                        CityName="Bagalkote";
                    }
                    Toast.makeText(getApplicationContext(),"Your Current Location is : "+CityName+","+StateName,Toast.LENGTH_LONG).show();
                    loc_txt.setText(String.format("%s,%s", CityName, StateName));
                    currentCity.setText(" "+CityName);
                    currentState.setText(" "+StateName);
                }
            }, Looper.getMainLooper());
        }
    }

    public void saveData()
    {
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("com.example.test.ashishvz.prefrence_key",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor =sharedPreferences.edit();
        editor.putString("new_confirmed",new_confirmed.getText().toString());
        editor.putString("total_confirmed",total_confirmed.getText().toString());
        editor.putString("new_deaths",new_deaths.getText().toString());
        editor.putString("total_deaths",total_deaths.getText().toString());
        editor.putString("new_recovered",new_recovered.getText().toString());
        editor.putString("total_recovered",total_recovered.getText().toString());

        ////////////////////////////////////////////////////////////////////////////

        editor.putString("gnew_confirmed",gnew_confirmed.getText().toString());
        editor.putString("gtotal_confirmed",gtotal_confirmed.getText().toString());
        editor.putString("gnew_deaths",gnew_deaths.getText().toString());
        editor.putString("gtotal_deaths",gtotal_deaths.getText().toString());
        editor.putString("gnew_recovered",gnew_recovered.getText().toString());
        editor.putString("gtotal_recovered",gtotal_recovered.getText().toString());

        ///////////////////////////////////////////////////////////////////////////////

        editor.putString("city_active",city_active.getText().toString());
        editor.putString("city_confirmed",city_confirmed.getText().toString());
        editor.putString("city_deaths",city_deaths.getText().toString());
        editor.putString("city_recovered",city_recovered.getText().toString());

        ////////////////////////////////////////////////////////////////////////////////

        editor.putString("state_active",state_active.getText().toString());
        editor.putString("state_confirmed",state_confirmed.getText().toString());
        editor.putString("state_deaths",state_deaths.getText().toString());
        editor.putString("state_recovered",state_recovered.getText().toString());

        editor.apply();
    }

    public void loadData()
    {
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("com.example.test.ashishvz.prefrence_key",Context.MODE_PRIVATE);
        new_confirmed.setText(sharedPreferences.getString("new_confirmed","00"));
        onc=Integer.parseInt(sharedPreferences.getString("new_confirmed","00"));
        total_confirmed.setText(sharedPreferences.getString("total_confirmed","00"));
        otc=Integer.parseInt(sharedPreferences.getString("total_confirmed","00"));
        new_deaths.setText(sharedPreferences.getString("new_deaths","00"));
        ond=Integer.parseInt(sharedPreferences.getString("new_deaths","00"));
        total_deaths.setText(sharedPreferences.getString("total_deaths","00"));
        otd=Integer.parseInt(sharedPreferences.getString("total_deaths","00"));
        new_recovered.setText(sharedPreferences.getString("new_recovered","00"));
        onr=Integer.parseInt(sharedPreferences.getString("new_recovered","00"));
        total_recovered.setText(sharedPreferences.getString("total_recovered","00"));
        otr=Integer.parseInt(sharedPreferences.getString("total_recovered","00"));

        //India Data

        gnew_confirmed.setText(sharedPreferences.getString("gnew_confirmed","00"));
        gtotal_confirmed.setText(sharedPreferences.getString("gtotal_confirmed","00"));
        gnew_deaths.setText(sharedPreferences.getString("gnew_deaths","00"));
        gtotal_deaths.setText(sharedPreferences.getString("gtotal_deaths","00"));
        gnew_recovered.setText(sharedPreferences.getString("gnew_recovered","00"));
        gtotal_recovered.setText(sharedPreferences.getString("gtotal_recovered","00"));

        //current city Data

        city_active.setText(sharedPreferences.getString("city_active","00"));
        city_confirmed.setText(sharedPreferences.getString("city_confirmed","00"));
        city_deaths.setText(sharedPreferences.getString("city_deaths","00"));
        city_recovered.setText(sharedPreferences.getString("city_recovered","00"));

        //current state Data
        state_active.setText(sharedPreferences.getString("state_active","00"));
        state_confirmed.setText(sharedPreferences.getString("state_confirmed","00"));
        state_deaths.setText(sharedPreferences.getString("state_deaths","00"));
        state_recovered.setText(sharedPreferences.getString("state_recovered","00"));


    }

    public void getCurrentCityData()
    {
        String url="https://api.covid19india.org/state_district_wise.json";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try{
                    String str =response.getString(StateName);
                    JSONObject ka_obj = new JSONObject(str);
                    String str1 = ka_obj.getString("districtData");
                    JSONObject dis_obj = new JSONObject(str1);
                    String dis = dis_obj.getString(CityName);
                    if(!dis.isEmpty()) {
                        JSONObject ob = new JSONObject(dis);
                        city_active.setText(String.valueOf(ob.getInt("active")));
                        city_confirmed.setText(String.valueOf(ob.getInt("confirmed")));
                        city_deaths.setText(String.valueOf(ob.getInt("deceased")));
                        city_recovered.setText(String.valueOf(ob.getInt("recovered")));
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(),"No Data Found for Current City",Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        requestQueue.add(request);
    }

    public void getCurrentStateData()
    {
        String url="https://api.rootnet.in/covid19-in/stats/latest";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try{
                    String str =response.getString("data");
                    JSONObject obj = new JSONObject(str);
                    String str1=obj.getString("regional");
                    JSONArray arr = new JSONArray(str1);
                    for(int i=0;i<arr.length();i++)
                    {
                        JSONObject o = arr.getJSONObject(i);
                        String loc = o.getString("loc");
                        if(loc.equals(StateName))
                        {
                            state_active.setText(String.valueOf((o.getInt("confirmedCasesIndian"))-(o.getInt("discharged"))-(o.getInt("deaths"))));
                            state_recovered.setText(String.valueOf(o.getInt("discharged")));
                            state_confirmed.setText(String.valueOf(o.getInt("totalConfirmed")));
                            state_deaths.setText(String.valueOf(o.getInt("deaths")));
                            break;
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        requestQueue.add(request);
    }

    public void compareIndiaData()
    {
        if(onc<Integer.parseInt(new_confirmed.getText().toString()))
        {
            new_confirmed.setTextColor(Color.RED);
        }
        else if(onc==Integer.parseInt(new_confirmed.getText().toString()))
        {
            new_confirmed.setTextColor(Color.YELLOW);
        }
        else
        {
            new_confirmed.setTextColor(Color.GREEN);
        }



        if(otc<Integer.parseInt(total_confirmed.getText().toString()))
        {
            total_confirmed.setTextColor(Color.RED);
        }
        else if(otc==Integer.parseInt(total_confirmed.getText().toString()))
        {
            total_confirmed.setTextColor(Color.YELLOW);
        }
        else
        {
            total_confirmed.setTextColor(Color.GREEN);
        }



        if(ond<Integer.parseInt(new_deaths.getText().toString()))
        {
            new_deaths.setTextColor(Color.RED);
        }
        else if(ond==Integer.parseInt(new_deaths.getText().toString()))
        {
            new_deaths.setTextColor(Color.YELLOW);
        }
        else
        {
            new_deaths.setTextColor(Color.GREEN);
        }



        if(otd<Integer.parseInt(total_deaths.getText().toString()))
        {
            total_deaths.setTextColor(Color.RED);
        }
        else if(otd==Integer.parseInt(total_deaths.getText().toString()))
        {
            total_deaths.setTextColor(Color.YELLOW);
        }
        else
        {
            total_deaths.setTextColor(Color.GREEN);
        }



        if(onr<Integer.parseInt(new_recovered.getText().toString()))
        {
            new_recovered.setTextColor(Color.RED);
        }
        else if(onr==Integer.parseInt(new_recovered.getText().toString()))
        {
            new_recovered.setTextColor(Color.YELLOW);
        }
        else
        {
            new_recovered.setTextColor(Color.GREEN);
        }


        if(otr<Integer.parseInt(total_recovered.getText().toString()))
        {
            total_recovered.setTextColor(Color.RED);
        }
        else if(otr==Integer.parseInt(total_recovered.getText().toString()))
        {
            total_recovered.setTextColor(Color.YELLOW);
        }
        else
        {
            total_recovered.setTextColor(Color.GREEN);
        }

    }
    ///////////////////////////////////////
    public void compareGlobalData()
    {
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("com.example.test.ashishvz.prefrence_key",Context.MODE_PRIVATE);
        if(Integer.parseInt(sharedPreferences.getString("gnew_confirmed","00"))<Integer.parseInt(gnew_confirmed.getText().toString()))
        {
            gnew_confirmed.setTextColor(Color.RED);
        }
        else if(Integer.parseInt(sharedPreferences.getString("gnew_confirmed","00"))==Integer.parseInt(gnew_confirmed.getText().toString()))
        {
            gnew_confirmed.setTextColor(Color.YELLOW);
        }
        else
        {
            gnew_confirmed.setTextColor(Color.GREEN);
        }



        if(Integer.parseInt(sharedPreferences.getString("gtotal_confirmed","00"))<Integer.parseInt(gtotal_confirmed.getText().toString()))
        {
            gtotal_confirmed.setTextColor(Color.RED);
        }
        else if(Integer.parseInt(sharedPreferences.getString("gtotal_confirmed","00"))==Integer.parseInt(gtotal_confirmed.getText().toString()))
        {
            gtotal_confirmed.setTextColor(Color.YELLOW);
        }
        else
        {
            gtotal_confirmed.setTextColor(Color.GREEN);
        }



        if(Integer.parseInt(sharedPreferences.getString("gnew_deaths","00"))<Integer.parseInt(gnew_deaths.getText().toString()))
        {
            gnew_deaths.setTextColor(Color.RED);
        }
        else if(Integer.parseInt(sharedPreferences.getString("gnew_deaths","00"))==Integer.parseInt(gnew_deaths.getText().toString()))
        {
            gnew_deaths.setTextColor(Color.YELLOW);
        }
        else
        {
            gnew_deaths.setTextColor(Color.GREEN);
        }



        if(Integer.parseInt(sharedPreferences.getString("gtotal_deaths","00"))<Integer.parseInt(gtotal_deaths.getText().toString()))
        {
            gtotal_deaths.setTextColor(Color.RED);
        }
        else if(Integer.parseInt(sharedPreferences.getString("gtotal_deaths","00"))==Integer.parseInt(gtotal_deaths.getText().toString()))
        {
            gtotal_deaths.setTextColor(Color.YELLOW);
        }
        else
        {
            gtotal_deaths.setTextColor(Color.GREEN);
        }



        if(Integer.parseInt(sharedPreferences.getString("gnew_recovered","00"))<Integer.parseInt(gnew_recovered.getText().toString()))
        {
            gnew_recovered.setTextColor(Color.RED);
        }
        else if(Integer.parseInt(sharedPreferences.getString("gnew_recovered","00"))==Integer.parseInt(gnew_recovered.getText().toString()))
        {
            gnew_recovered.setTextColor(Color.YELLOW);
        }
        else
        {
            gnew_recovered.setTextColor(Color.GREEN);
        }


        if(Integer.parseInt(sharedPreferences.getString("gtotal_recovered","00"))<Integer.parseInt(gtotal_recovered.getText().toString()))
        {
            gtotal_recovered.setTextColor(Color.RED);
        }
        else if(otr==Integer.parseInt(total_recovered.getText().toString()))
        {
            gtotal_recovered.setTextColor(Color.YELLOW);
        }
        else
        {
            gtotal_recovered.setTextColor(Color.GREEN);
        }
    }

    public void compareCityData()
    {
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("com.example.test.ashishvz.prefrence_key",Context.MODE_PRIVATE);
        if(Integer.parseInt(sharedPreferences.getString("city_active","00"))<Integer.parseInt(city_active.getText().toString()))
        {
            city_active.setTextColor(Color.RED);
        }
        else if(Integer.parseInt(sharedPreferences.getString("city_active","00"))==Integer.parseInt(city_active.getText().toString()))
        {
            city_active.setTextColor(Color.YELLOW);
        }
        else
        {
            city_active.setTextColor(Color.GREEN);
        }

        if(Integer.parseInt(sharedPreferences.getString("city_recovered","00"))<Integer.parseInt(city_recovered.getText().toString()))
        {
            city_recovered.setTextColor(Color.RED);
        }
        else if(Integer.parseInt(sharedPreferences.getString("city_recovered","00"))==Integer.parseInt(city_recovered.getText().toString()))
        {
            city_recovered.setTextColor(Color.YELLOW);
        }
        else
        {
            city_recovered.setTextColor(Color.GREEN);
        }

        if(Integer.parseInt(sharedPreferences.getString("city_confirmed","00"))<Integer.parseInt(city_confirmed.getText().toString()))
        {
            city_confirmed.setTextColor(Color.RED);
        }
        else if(Integer.parseInt(sharedPreferences.getString("city_confirmed","00"))==Integer.parseInt(city_confirmed.getText().toString()))
        {
            city_confirmed.setTextColor(Color.YELLOW);
        }
        else
        {
            city_confirmed.setTextColor(Color.GREEN);
        }

        if(Integer.parseInt(sharedPreferences.getString("city_deaths","00"))<Integer.parseInt(city_deaths.getText().toString()))
        {
            city_deaths.setTextColor(Color.RED);
        }
        else if(Integer.parseInt(sharedPreferences.getString("city_deaths","00"))==Integer.parseInt(city_deaths.getText().toString()))
        {
            city_deaths.setTextColor(Color.YELLOW);
        }
        else
        {
            city_deaths.setTextColor(Color.GREEN);
        }

    }
    public void compareStateData()
    {
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("com.example.test.ashishvz.prefrence_key",Context.MODE_PRIVATE);
        if(Integer.parseInt(sharedPreferences.getString("state_active","00"))<Integer.parseInt(state_active.getText().toString()))
        {
            state_active.setTextColor(Color.RED);
        }
        else if(Integer.parseInt(sharedPreferences.getString("state_active","00"))==Integer.parseInt(state_active.getText().toString()))
        {
            state_active.setTextColor(Color.YELLOW);
        }
        else
        {
            state_active.setTextColor(Color.GREEN);
        }

        if(Integer.parseInt(sharedPreferences.getString("state_recovered","00"))<Integer.parseInt(state_recovered.getText().toString()))
        {
            state_recovered.setTextColor(Color.RED);
        }
        else if(Integer.parseInt(sharedPreferences.getString("state_recovered","00"))==Integer.parseInt(state_recovered.getText().toString()))
        {
            state_recovered.setTextColor(Color.YELLOW);
        }
        else
        {
            state_recovered.setTextColor(Color.GREEN);
        }

        if(Integer.parseInt(sharedPreferences.getString("state_confirmed","00"))<Integer.parseInt(state_confirmed.getText().toString()))
        {
            state_confirmed.setTextColor(Color.RED);
        }
        else if(Integer.parseInt(sharedPreferences.getString("state_confirmed","00"))==Integer.parseInt(state_confirmed.getText().toString()))
        {
            state_confirmed.setTextColor(Color.YELLOW);
        }
        else
        {
            state_confirmed.setTextColor(Color.GREEN);
        }

        if(Integer.parseInt(sharedPreferences.getString("state_deaths","00"))<Integer.parseInt(state_deaths.getText().toString()))
        {
            state_deaths.setTextColor(Color.RED);
        }
        else if(Integer.parseInt(sharedPreferences.getString("state_deaths","00"))==Integer.parseInt(state_deaths.getText().toString()))
        {
            state_deaths.setTextColor(Color.YELLOW);
        }
        else
        {
            state_deaths.setTextColor(Color.GREEN);
        }

    }
}
