package edu.temple.mapchatapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.MapView;

public class UserListFragment extends Fragment {

    RecyclerView recyclerView;
    public UserListFragment(){

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_list, container, false);

        recyclerView = (RecyclerView) v.findViewById(R.id.recyclerView);
        if(recyclerView == null)
            System.out.printf("==============main========== recyclerView is null.\n");
        ((MainActivity)getActivity()).setRecyclerView(recyclerView);

        return v;
    }
}
