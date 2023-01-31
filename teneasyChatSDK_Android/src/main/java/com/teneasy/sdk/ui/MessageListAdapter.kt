package com.teneasy.sdk.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.teneasy.sdk.R;

import java.util.ArrayList;
import java.util.List;

public class MessageListAdapter extends RecyclerView.Adapter<MessageListAdapter.MsgViewHolder> {

    private List<MessageItem> list;

    public List<MessageItem> getList() {
        return list;
    }

    public void setList(List<MessageItem> list) {
        this.list = new ArrayList<>(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MsgViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        return new MsgViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MsgViewHolder holder, int position) {
        if(list == null) {
            return ;
        }
        MessageItem item = list.get(position);
        if(item.isSend()) {
            holder.tvLeftTime.setText(item.getTime());
            holder.tvLeftMsg.setText(item.getMsg());

            holder.tvLeftTime.setVisibility(View.VISIBLE);
            holder.tvLeftMsg.setVisibility(View.VISIBLE);

            holder.tvRightTime.setVisibility(View.GONE);
            holder.tvRightMsg.setVisibility(View.GONE);
        } else {
            holder.tvRightTime.setText(item.getTime());
            holder.tvRightMsg.setText(item.getMsg());

            holder.tvRightTime.setVisibility(View.VISIBLE);
            holder.tvRightMsg.setVisibility(View.VISIBLE);

            holder.tvLeftTime.setVisibility(View.GONE);
            holder.tvLeftMsg.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    public class MsgViewHolder extends RecyclerView.ViewHolder {
        TextView tvLeftTime;
        TextView tvLeftMsg;

        TextView tvRightTime;
        TextView tvRightMsg;

        public MsgViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLeftTime = itemView.findViewById(R.id.tv_left_time);
            tvLeftMsg = itemView.findViewById(R.id.tv_left_msg);

            tvRightTime = itemView.findViewById(R.id.tv_right_time);
            tvRightMsg = itemView.findViewById(R.id.tv_right_msg);
        }
    }
}
