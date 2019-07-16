package com.example.weatherapplication;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import Connection.HttpManager;

public class MainActivity extends Activity {

    ArrayList<String> listItems=new ArrayList<String>();

    ArrayAdapter adapter;
    Context context;
    Button confirm;
    EditText editText;
    ArrayList<String> mobileArray = new ArrayList<>();
    ListView listView;
    Button addBtn;
    double temperature;

    final static String BASE_URL = "http://api.openweathermap.org/data/2.5/weather?q=";
    final static String API_ID = "&appid=46823a6d3e5a5aea39bd72cf8a706516";
    private String location;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;

        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        1);
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        1);
            }
        } else {
            LocationManager locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
            Location location1 = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            try {
                location = hereLocation(location1.getLatitude(), location1.getLongitude(), context);

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(context, "Not found!", Toast.LENGTH_SHORT).show();
            }
        }

        if(!mobileArray.contains(location)){
            mobileArray.add(location);
        }


        adapter = new ArrayAdapter<String>(this,
                R.layout.activity_listview, mobileArray);

        listView = (ListView) findViewById(R.id.mobile_list);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Object item = listView.getItemAtPosition(i);
                String location = item.toString();
                final String COMPLETE_URL = BASE_URL + location.toLowerCase() + API_ID;
                requestData(COMPLETE_URL);

            }
        });
        addBtn = (Button) findViewById(R.id.addBtn);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setContentView(R.layout.add_item);
                confirm = (Button) findViewById(R.id.confirm);
                editText = (EditText) findViewById(R.id.editText);

                confirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(!mobileArray.contains(editText.getText().toString())) {
                            mobileArray.add(editText.getText().toString());
                        }
                        onCreate(savedInstanceState);
                    }
                });
            }
        });


    }

    public String hereLocation(double lat, double lon, Context context) {
        String ourCity = "";

        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> addressList;

        try {
            addressList = geocoder.getFromLocation(lat, lon, 1);
            if (addressList.size() > 0) {
                ourCity = addressList.get(0).getLocality();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return ourCity;
    }

    public void requestData(String s){
        //System.out.println(s + " XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXx");
        FetchData task = new FetchData();
        task.execute(s);
    }

    public class FetchData extends AsyncTask<String, String, String> {


        @Override
        protected String doInBackground(String... strings) {
            System.out.println(strings[0] + " yyyyyyy");
            return HttpManager.getData(strings[0]);
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                System.out.println("onPost");
                System.out.println(s);
                JSONObject obj = new JSONObject(s);
                JSONObject myObj = obj.getJSONObject("main");
                String cityName = obj.getString("name");

                Double cityTemp = myObj.getDouble("temp");
                temperature = (cityTemp - 273);
                String finalTemp = String.valueOf(Math.round(temperature));
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("Temperature: " + finalTemp + "°C" )
                        .setTitle("Location: "  + cityName);

                AlertDialog alert =builder.create();
                alert.show();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            super.onPostExecute(s);
        }
    }
}