package hmi.hmiprojekt;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.Format;
import java.text.SimpleDateFormat;

import hmi.hmiprojekt.TripComponents.Trip;

public class TripAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static Format dateFormat = new SimpleDateFormat("dd.MM.yyyy");
    private static ClickListener clickListener;
    private int position;

    // ViewHolder of an Object in the RecyclerView
    public static class ViewHolderTrip extends RecyclerView.ViewHolder implements View.OnClickListener
            , View.OnCreateContextMenuListener {

        TextView nameView;
        TextView dateView;
        Context ctx;
        // creates an empty ViewHolder
        ViewHolderTrip(View itemView, Context ctx) {
            super(itemView);
            itemView.setOnClickListener(this);
            nameView = itemView.findViewById(R.id.textView_trip_name);
            dateView = itemView.findViewById(R.id.textView_trip_date);
            this.ctx = ctx;
            itemView.setOnCreateContextMenuListener(this);
        }

        // fills it with the Tripdata
        public void fill(Trip t) {
            nameView.setText(t.getName());
            dateView.setText(dateFormat.format(t.getStart()));
        }

        @Override
        public void onClick(View view) {
            clickListener.onItemClick(getAdapterPosition(), view);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            int menuRes = R.menu.delete_send_menu;
            new MenuInflater(ctx).inflate(menuRes, menu);
        }


    }

    public void setOnItemClickListener(ClickListener clickListener) {
        TripAdapter.clickListener = clickListener;
    }

    public interface ClickListener {
        void onItemClick(int position, View v);
    }

    // Array of Trips that is going to be displayed
    private Trip[] trips;

    TripAdapter(Trip[] trips) {
        this.trips = trips;
    }

    public Trip getTrip(int position){
        return trips[position];
    }

    // called when creating a ViewHolder
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = R.layout.layout_card_trip;
        View v = LayoutInflater
                    .from(parent.getContext())
                    .inflate(layout, parent, false);
        return new ViewHolderTrip(v, parent.getContext());
    }

    // after ViewHolder is created this is called to fill it with data
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Trip trip = trips[position];
        ((ViewHolderTrip) holder).fill(trip);

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick (View v) {
                setPosition(holder.getPosition());
                return false;
            }
        });
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        holder.itemView.setOnLongClickListener(null);
        super.onViewRecycled(holder);
    }

    @Override
    public int getItemCount() {
        return trips.length;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }
}
