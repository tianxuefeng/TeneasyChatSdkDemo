package com.example.teneasychatsdkdemo.ui.main.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MassageListAdapter extends RecyclerView.Adapter<MassageListAdapter.MsgViewHolder> {

    @NonNull
    @Override
    public MsgViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull MsgViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class MsgViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvMsg;

        public MsgViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
