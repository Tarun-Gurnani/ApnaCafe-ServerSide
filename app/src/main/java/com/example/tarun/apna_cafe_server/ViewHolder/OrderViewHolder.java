package com.example.tarun.apna_cafe_server.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.View;
import android.widget.TextView;

import com.example.tarun.apna_cafe_server.Interface.ItemClickListener;
import com.example.tarun.apna_cafe_server.R;

public class OrderViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,View.OnCreateContextMenuListener{


    public TextView txtOrderId,txtOrderStatus,txtOrderPhone,txtOrderAddress;

    private ItemClickListener itemClickListener;


    public OrderViewHolder ( View itemView ) {
        super ( itemView );

        txtOrderId = (TextView)itemView.findViewById ( R.id.order_id);
        txtOrderPhone = (TextView)itemView.findViewById ( R.id.order_phone);
        txtOrderStatus = (TextView)itemView.findViewById ( R.id.order_status);
        txtOrderAddress = (TextView)itemView.findViewById ( R.id.order_address);

        itemView.setOnClickListener ( this );
        itemView.setOnCreateContextMenuListener ( this );

    }

    public void setItemClickListener ( ItemClickListener itemClickListener ) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick ( View v ) {

        itemClickListener.onClick ( v,getAdapterPosition (),false );

    }

    @Override
    public void onCreateContextMenu ( ContextMenu menu , View v , ContextMenu.ContextMenuInfo menuInfo ) {

        menu.setHeaderTitle ( "Select The Action" );
        menu.add ( 0,0,getAdapterPosition(),"Update" );
        menu.add ( 0,1,getAdapterPosition(),"Delete" );
    }
}
