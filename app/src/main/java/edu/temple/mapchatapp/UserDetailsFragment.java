package edu.temple.mapchatapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class UserDetailsFragment extends Fragment {
    private static final String USERS_KEY = "user";

    String userName;    //alphanumeric username

    MapView mapView;
    //private GoogleMap googleMap;

    public UserDetailsFragment() {
        // Required empty public constructor
    }

    public static UserDetailsFragment newInstance(String userName) {
        UserDetailsFragment fragment = new UserDetailsFragment();
        Bundle args = new Bundle();
        args.putString(USERS_KEY, userName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            userName = getArguments().getString(USERS_KEY);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_detail, container, false);

        mapView = (MapView) v.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        //mapView.getMapAsync((OnMapReadyCallback) this);
        //mapView.onResume();// needed to get the map to display immediately
        System.out.printf("======================== create de_fragment.\n");
        ((MainActivity)getActivity()).setMapView(mapView);
/*
        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }*/


        // Perform any camera updates here
        return v;
    }


    @Override
    public void onStart() {
        super.onStart();

        //mapView = ((MapView)getView());
        //((MainActivity)getActivity()).setMapView(mapView);
        mapView.onStart();
        System.out.printf("==============de_fragment========== onStart\n");
    }

    @Override
    public void onResume() {
        super.onResume();

        mapView.onResume();
        System.out.printf("==============de_fragment========== onResume\n");
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
        System.out.printf("===============de_fragment========= onPause\n");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        System.out.printf("===============de_fragment========= onDestroy\n");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        System.out.printf("==============de_fragment========== onSaveInstanceState\n");
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
/*
    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;

        // latitude and longitude
        double latitude = 17.385044;
        double longitude = 78.486671;

        // create marker
        MarkerOptions marker = new MarkerOptions().position(
                new LatLng(latitude, longitude)).title("Hello Maps");

        // Changing marker icon
        marker.icon(BitmapDescriptorFactory
                .defaultMarker(BitmapDescriptorFactory.HUE_ROSE));
        // adding marker
        googleMap.addMarker(marker);
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(17.385044, 78.486671)).zoom(12).build();
        googleMap.animateCamera(CameraUpdateFactory
                .newCameraPosition(cameraPosition));
    }*/
}
