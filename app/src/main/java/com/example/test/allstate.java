package com.example.test;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class allstate extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private List<StateDat> stateDatList;
    private RequestQueue requestQueue;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_allstate);
        requestQueue= Volley.newRequestQueue(this);
        recyclerView=findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        stateDatList=new ArrayList<>();
        if(isNetworkConnected())
        {
            getData();
        }
        else
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(allstate.this);
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

    public boolean isNetworkConnected()
    {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo()!=null && cm.getActiveNetworkInfo().isConnected();
    }
    public void getData()
    {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading the Data! Please wait!!");
        progressDialog.show();
        String url="https://api.rootnet.in/covid19-in/stats/latest";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try{
                    progressDialog.dismiss();
                    String str =response.getString("data");
                    JSONObject obj = new JSONObject(str);
                    String str1=obj.getString("regional");
                    JSONArray arr = new JSONArray(str1);
                    for(int i=0;i<arr.length();i++)
                    {
                        JSONObject o = arr.getJSONObject(i);
                       StateDat stateDat = new StateDat(o.getString("loc"),
                                    Integer.parseInt(String.valueOf((o.getInt("confirmedCasesIndian"))-(o.getInt("discharged"))-(o.getInt("deaths")))),
                                    o.getInt("totalConfirmed"),
                                    o.getInt("deaths"),
                                    o.getInt("discharged"));
                        stateDatList.add(stateDat);
                    }
                adapter= new StateAdapter(getApplicationContext(), stateDatList);
                    recyclerView.setAdapter(adapter);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(),"Error in loading Data",Toast.LENGTH_LONG).show();
                error.printStackTrace();
            }
        });
        requestQueue.add(request);
    }

}
