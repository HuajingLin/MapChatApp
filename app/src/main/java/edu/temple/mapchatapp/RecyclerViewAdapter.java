package edu.temple.mapchatapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.model.LatLng;

import java.util.Iterator;
import java.util.PriorityQueue;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    //private String[] mDataset;
    PriorityQueue<User> partnerQueue;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    public RecyclerViewAdapter(Context context, PriorityQueue<User> data) {
        this.mInflater = LayoutInflater.from(context);
        partnerQueue = data;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recyclerview_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Object[] arr = partnerQueue.toArray();

        for (int j = 0; j < arr.length; j++) {
            if(position == j) {

                holder.myTextView.setText(((User)arr[j]).getUser());
                break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return partnerQueue.size();
        //return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView myTextView;

        ViewHolder(View itemView) {
            super(itemView);
            myTextView = itemView.findViewById(R.id.tvUserName);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null)
                mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    public String getItem(int id) {

        String userName = "";
        Object[] arr = partnerQueue.toArray();

        for (int j = 0; j < arr.length; j++) {
            if(id == j) {

                userName = ((User)arr[j]).getUser();
                break;
            }
        }
        return userName;
    }

    public LatLng getLatLng(int id){
        LatLng latLng = null;
        Object[] arr = partnerQueue.toArray();

        for (int j = 0; j < arr.length; j++) {
            if(id == j) {

                double lat = ((User)arr[j]).getLatitude();
                double lon = ((User)arr[j]).getLongitude();
                latLng = new LatLng(lat, lon);
                break;
            }
        }
        return latLng;
    }

    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}
