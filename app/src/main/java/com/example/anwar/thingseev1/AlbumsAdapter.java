package com.example.anwar.thingseev1;

/**
 * Created by  on 04/05/2018.
 */

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import android.content.Intent;

import com.bumptech.glide.Glide;

import java.util.List;


public class AlbumsAdapter extends RecyclerView.Adapter<AlbumsAdapter.MyViewHolder> {

    private Context mContext;
    private List<Album> albumList;
    private AlbumsAdapterListener listener;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title, count;
        public ImageView thumbnail, overflow;
        public CardView cardView;

        public MyViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.title);
            count = view.findViewById(R.id.count);
            thumbnail = view.findViewById(R.id.thumbnail);
            overflow = view.findViewById(R.id.overflow);
            cardView = view.findViewById(R.id.card_view);
        }
    }


    public AlbumsAdapter(Context mContext, List<Album> albumList, AlbumsAdapterListener listener) {
        this.mContext = mContext;
        this.albumList = albumList;
        this.listener = listener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.album_card, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        Album album = albumList.get(position);
        holder.title.setText(album.getName());
        holder.count.setText(album.getNumOfSongs() + " Years old");

        /*loading album cover using Glide library*/
        Glide.with(mContext).load(album.getThumbnail()).into(holder.thumbnail);

        holder.overflow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

               showPopupMenu(holder.overflow, position);
            }
        });



        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Write code here what to do when Clicked on card

                StartDashBoardActivity(holder.cardView,position);

                //listener.onCardSelected(position, holder.thumbnail);

                //Pass position and thumbnail to .onCardSelected method.
                //where position and thaumbnail can be pass to DashBoard activity using intent.
                //that DashBoard activity can use these values
            }
        });

        holder.thumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                StartDashBoardActivity(holder.cardView,position);

                //listener.onCardSelected(position, holder.thumbnail);
            }
        });
    }

    /**
     * Showing popup menu when tapping on 3 dots
     */
    private void showPopupMenu(View view, int position) {

        PopupMenu popup = new PopupMenu(mContext, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_album, popup.getMenu());
        popup.setOnMenuItemClickListener(new MyMenuItemClickListener(position));
        popup.show();
    }

    /**
     * Click listener for popup menu items
     */
    class MyMenuItemClickListener implements PopupMenu.OnMenuItemClickListener {

        int position;

        public MyMenuItemClickListener(int position) {
            this.position = position;
        }

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.action_add_favourite:
                    //listener.onAddToFavoriteSelected(position);
                    Toast.makeText(mContext, "Picture Changed", Toast.LENGTH_LONG).show();
                    return true;
                case R.id.action_play_next:
                    //listener.onPlayNextSelected(position);
                    Toast.makeText(mContext, "Pet Removed", Toast.LENGTH_LONG).show();
                    return true;
                default:
            }
            return false;
        }
    }

    @Override
    public int getItemCount() {
        return albumList.size();
    }

    public interface AlbumsAdapterListener {
        void onAddToFavoriteSelected(int position);

        void onPlayNextSelected(int position);

        void onCardSelected(int position, ImageView thumbnail);
    }

    private void StartDashBoardActivity(View view, int position)
    {

        if (position == 0)
        {
            //view.getContext().startActivity(new Intent(view.getContext(), PetDashBoard.class));
            Intent PetDashBoardActivity = new Intent(view.getContext(), com.example.anwar.thingseev1.PetDashBoard.class);
            view.getContext().startActivity(PetDashBoardActivity);
        }

    }


}