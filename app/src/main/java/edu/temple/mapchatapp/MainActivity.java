package edu.temple.mapchatapp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Timer;
import java.util.TimerTask;

import javax.net.ssl.HttpsURLConnection;

@RequiresApi(api = Build.VERSION_CODES.N)
public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, RecyclerViewAdapter.ItemClickListener{

    RecyclerView recyclerView;
    RecyclerViewAdapter mAdapter;
    RecyclerView.LayoutManager layoutManager;

    MapView mapView;

    LocationManager locationManager;
    LocationListener locationListener;

    Marker marker;
    GoogleMap map;
    private Timer timer;
    private int iTime = 30;
    LatLng currentLatLng= null;
    LatLng pastLatLng= null;
    boolean bUpdatePosition = true;

    boolean singlePane;

    private final String GET_LOCATION_URL = "https://kamorris.com/lab/get_locations.php";
    private final String REGISTER_URL = "https://kamorris.com/lab/register_location.php";
    PartnerComparator partnerComparator = new PartnerComparator();
    PriorityQueue<User> partnerQueue = new PriorityQueue<User>(partnerComparator);
    User me;
    MainActivity mainActivity;

    Handler usersHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            try {
                partnerQueue.clear();
                JSONArray userArray = new JSONArray((String) message.obj);
                for (int i = 0; i < userArray.length(); i++) {
                    //System.out.printf("======================== %s\n",userArray.getJSONObject(i).toString());
                    JSONObject jsonUser = new JSONObject(userArray.getJSONObject(i).toString());
                    String strName = jsonUser.getString("username");
                    String strLatitude = jsonUser.getString("latitude");
                    String strLongitude = jsonUser.getString("longitude");
                    System.out.printf("======================== %s\n",strName);

                    User user = new User(strName,Double.parseDouble(strLatitude),Double.parseDouble(strLongitude));
                    user.me = me;
                    partnerQueue.add(user);
                }

                layoutManager = new LinearLayoutManager(mainActivity);
                recyclerView.setLayoutManager(layoutManager);
                mAdapter = new RecyclerViewAdapter(mainActivity, partnerQueue);
                mAdapter.setClickListener(mainActivity);
                recyclerView.setAdapter(mAdapter);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return true;
        }
    });
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainActivity = this;
        startTimer();
        singlePane = findViewById(R.id.container_1) == null;

        if (singlePane) {
            System.out.printf("======================== single pane\n");

            recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
            recyclerView.setHasFixedSize(true);

            mapView = findViewById(R.id.mapView);
            mapView.onCreate(savedInstanceState);
            mapView.getMapAsync((OnMapReadyCallback) this);
            Loadlocation();
            /*
            findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (checkCallingOrSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                }
            });*/

        } else {
            System.out.printf("======================== two pane\n");
            UserDetailsFragment userDetailsFragment = new UserDetailsFragment();

            //String[] users;
            UserListFragment userListFragment = new UserListFragment();//.newInstance(users);
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.container_1, userListFragment)
                    .add(R.id.container_2, userDetailsFragment)
                    .commit();
        }
    }

    @Override   //recyclerView row click
    public void onItemClick(View view, int position) {
        Toast.makeText(this, "You clicked " + mAdapter.getItem(position) + " on row number " + position, Toast.LENGTH_SHORT).show();
        bUpdatePosition = false;
        LatLng latLng = mAdapter.getLatLng(position);
        map.addMarker(new MarkerOptions().position(latLng)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                .title(mAdapter.getItem(position)));
        map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    private void Loadlocation(){

        System.out.printf("======================== Loadlocation\n");
        locationManager = getSystemService(LocationManager.class);

        if (checkCallingOrSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                //CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);

                if (map != null) {
                    //map.animateCamera(cameraUpdate);
                    if (marker == null) {
                        map.addMarker(new MarkerOptions().position(latLng)
                                .title("My Current Location"));
                    } else {
                        marker.setPosition(latLng);
                    }
                }
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

    }// end load map

    public void setMapView(MapView view){
        if(singlePane)
            return;
        System.out.printf("==============main========== setMapView\n");
        mapView = view;
        mapView.getMapAsync((OnMapReadyCallback) this);
    }

    public void setRecyclerView(RecyclerView view){
        if(singlePane)
            return;
        recyclerView = view;
        recyclerView.setHasFixedSize(true);
        Loadlocation();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(singlePane) {
            System.out.printf("==============single========== onStart\n");
            if (mapView == null)
                return;
            mapView.onStart();
        }
        else{
            //mapView.onStart();
            //System.out.printf("==============land========== onStart\n");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(singlePane) {
            System.out.printf("===============single========= onResume\n");
            super.onResume();
            if (mapView == null)
                return;
            mapView.onResume();
        }else{
            //mapView.onResume();
            //System.out.printf("===============land========= onResume\n");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(singlePane) {
            System.out.printf("===============single========= onPause\n");
            mapView.onPause();

        }else{
            //System.out.printf("===============land========= onPause\n");
            //mapView.onPause();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(singlePane) {
            System.out.printf("===============single========= onStop\n");
            mapView.onStop();
        }else{
            //System.out.printf("===============land========= onStop\n");
            //mapView.onStop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(singlePane) {
            System.out.printf("===============single========= onDestroy\n");
            locationManager.removeUpdates(locationListener);
            mapView.onDestroy();
        }else{
            System.out.printf("===============land========= onDestroy\n");
            locationManager.removeUpdates(locationListener);
            //mapView.onDestroy();

        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(singlePane) {
            System.out.printf("==============single========== onSaveInstanceState\n");
            mapView.onSaveInstanceState(outState);

        }else{
            //System.out.printf("==============land========== onSaveInstanceState\n");
            //mapView.onSaveInstanceState(outState);

        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if(singlePane) {
            System.out.printf("=============single=========== onLowMemory\n");
            mapView.onLowMemory();
        }else{
            //System.out.printf("=============land=========== onLowMemory\n");
            //mapView.onLowMemory();

        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.getUiSettings().setZoomControlsEnabled(true);
        getCurrentLocation();
        //fetchPartners();
    }

    private void getCurrentLocation() {
        map.clear();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        //Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    double longitude = location.getLongitude();
                    double latitude = location.getLatitude();
                    me = new User("me",latitude,longitude);
                    System.out.printf("============= current longitude: %f, Latitude: %f\n", me.getLongitude(), me.getLatitude());

                    //===========
                    currentLatLng = new LatLng(latitude, longitude);
                    // create marker
                    MarkerOptions marker = new MarkerOptions().position(currentLatLng).title("Me");

                    // Changing marker icon
                    marker.icon(BitmapDescriptorFactory
                            .defaultMarker(BitmapDescriptorFactory.HUE_ROSE));
                    // adding marker
                    map.addMarker(marker);
                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(currentLatLng).zoom(3).build();
                    map.animateCamera(CameraUpdateFactory
                            .newCameraPosition(cameraPosition));
                    //=====
                    RegisterMyPosition(latitude, longitude);

                    fetchPartners();
                } else {
                    System.out.printf("============= get current location fail !\n");
                }
            }
        });
    }

    //===================================================

    private boolean isNetworkActive() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    private void fetchPartners() {
        new Thread() {
            @Override
            public void run() {
                if (isNetworkActive()) {

                    URL url;

                    try {
                        url = new URL(GET_LOCATION_URL);
                        BufferedReader reader = new BufferedReader(
                                new InputStreamReader(
                                        url.openStream()));

                        StringBuilder response = new StringBuilder();
                        String tmpResponse;

                        while ((tmpResponse = reader.readLine()) != null) {
                            response.append(tmpResponse);
                        }

                        Message msg = Message.obtain();

                        msg.obj = response.toString();

                        //Log.d("Users RECEIVED", response.toString());

                        usersHandler.sendMessage(msg);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {
                    Log.e("Network Error", "Cannot download users");
                }
            }
        }.start();
    } // end fetchPartners

    public void postRegister(String requestURL,
                                   HashMap<String, String> postDataParams) {
        String response = "";
        RequestPackage requestPackage = new RequestPackage();
        requestPackage.setMethod("POST");
        requestPackage.setUrl(requestURL);
        requestPackage.setParams(postDataParams);

        RegisterRequest registerRequest = new RegisterRequest(); //Instantiation of the Async task
        //that’s defined below

        registerRequest.execute(requestPackage);
    }// end performPostCall

    private class RegisterRequest extends AsyncTask<RequestPackage, String, String> {
        @Override
        protected String doInBackground(RequestPackage... params) {
            return HttpManager.getData(params[0]);
        }

        //The String that is returned in the doInBackground() method is sent to the
        // onPostExecute() method below. The String should contain JSON data.
        @Override
        protected void onPostExecute(String result) {
            //try {
                //We need to convert the string in result to a JSONObject
                //JSONObject jsonObject = new JSONObject(result);
                //String price = jsonObject.getString("ask");
                System.out.printf("============= result: %s \n",result);
            //} catch (JSONException e) {
            //    e.printStackTrace();
            //}
        }
    } // end class RegisterRequest

    private void RegisterMyPosition(double latitude, double longitude){
        //=====
        HashMap<String, String> postDataParams = new HashMap<>();
        postDataParams.put("user", "Huajing Lin");
        postDataParams.put("latitude", Double.toString(latitude));
        postDataParams.put("longitude", Double.toString(longitude));
        postRegister( REGISTER_URL, postDataParams);
    }
    public void startTimer() {
        timer = new Timer();
        TimerTask timerTask = new TimerTask() {

            @Override
            public void run() {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        //updates server with user’s name and location every 10 meters moved
                        //System.out.printf("============= result: %s \n",iTime);
                        if(currentLatLng != null)
                        {
                            GetCurrentPosition();
                            if(pastLatLng == null){
                                pastLatLng = currentLatLng;
                            }
                            else{
                                double d = GetMovedDistance();
                                if(d > 10){
                                    System.out.printf("============= updates location to server.%f\n",d);
                                    RegisterMyPosition(currentLatLng.latitude, currentLatLng.longitude);
                                    pastLatLng = currentLatLng;
                                }
                                //else
                                //    System.out.printf("============= distance: %f \n",d);
                            }
                        }
                        if (iTime > 0)
                            iTime -= 1;
                        else {
                            iTime = 30;
                            System.out.printf("============= update user list ...\n");
                            fetchPartners();
                            bUpdatePosition = true;
                        }
                    }
                });
            }
        };
        timer.scheduleAtFixedRate(timerTask, 0, 1000);
    }// end startTimer

    private void GetCurrentPosition(){
        if(!bUpdatePosition)
            return;
        if (checkCallingOrSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 10, locationListener);

        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    double longitude = location.getLongitude();
                    double latitude = location.getLatitude();
                    //pastLatLng = currentLatLng;
                    currentLatLng = new LatLng(latitude, longitude);
                    //System.out.printf("============= distance: %f , %f \n",latitude, longitude);
                } else {
                    System.out.printf("============= GetCurrentPosition fail !\n");
                }
            }
        });
    }

    private double GetMovedDistance() {
        double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;
        double lat1 = Math.toRadians(pastLatLng.latitude);
        double lon1 = Math.toRadians(pastLatLng.longitude);
        double lat2 = Math.toRadians(currentLatLng.latitude);
        double lon2 = Math.toRadians(currentLatLng.longitude);

        // great circle distance in radians, using law of cosines formula
        double angle = Math.acos(Math.sin(lat1) * Math.sin(lat2)
                + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2));

        // each degree on a great circle of Earth is 60 nautical miles
        double nauticalMiles = 60 * Math.toDegrees(angle);
        double statuteMiles = STATUTE_MILES_PER_NAUTICAL_MILE * nauticalMiles;
        return statuteMiles * 1609.34;
    }

}
