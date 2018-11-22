package hmi.hmiprojekt;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.Format;
import java.text.SimpleDateFormat;

import hmi.hmiprojekt.TripComponents.Trip;

public class TripAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static Format dateFormat = new SimpleDateFormat("dd.MM.yyyy");

    // ViewHolder of an Object in the RecyclerView
    public class ViewHolderTrip extends RecyclerView.ViewHolder {

        TextView nameView;
        TextView dateView;
        // creates an empty ViewHolder
        ViewHolderTrip(View itemView) {
            super(itemView);
            nameView = itemView.findViewById(R.id.textView_trip_name);
            dateView = itemView.findViewById(R.id.textView_trip_date);
        }

        // fills it with the Tripdata
        public void fill(Trip t) {
            nameView.setText(t.getName());
            dateView.setText(dateFormat.format(t.getStart()));
        }
    }

    // Array of Trips that is going to be displayed
    private Trip[] trips;

    TripAdapter(Trip[] trips) {
        this.trips = trips;
    }

    // called when creating a ViewHolder
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = R.layout.layout_card_trip;
        View v = LayoutInflater
                    .from(parent.getContext())
                    .inflate(layout, parent, false);
        return new ViewHolderTrip(v);
    }

    // after ViewHolder is created this is called to fill it with data
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Trip trip = trips[position];
        ((ViewHolderTrip) holder).fill(trip);
    }

    @Override
    public int getItemCount() {
        return trips.length;
    }
}
