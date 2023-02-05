package com.teneasy.sdk.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.teneasy.sdk.R
import com.teneasy.sdk.TimeUtil
import com.teneasy.sdk.ui.MessageListAdapter.MsgViewHolder
import java.util.*

class MessageListAdapter : RecyclerView.Adapter<MsgViewHolder>() {
    private var list: ArrayList<MessageItem>? = null
    fun getList(): ArrayList<MessageItem>? {
        return list
    }

    fun setList(list: ArrayList<MessageItem>?) {
        this.list = ArrayList(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MsgViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
        return MsgViewHolder(view)
    }

    override fun onBindViewHolder(holder: MsgViewHolder, position: Int) {
        if (list == null) {
            return
        }
        val item = list!![position]
        val msgDate =  Date(item.cMsg!!.msgTime.seconds * 1000L)
        val localTime = TimeUtil.getTimeStringAutoShort2(msgDate, true)
        if (item.isSend) {

            holder.tvLeftTime.text = localTime
            holder.tvLeftMsg.text = item.cMsg!!.content.data
            holder.tvLeftTime.visibility = View.VISIBLE
            holder.tvLeftMsg.visibility = View.VISIBLE
            holder.tvRightTime.visibility = View.GONE
            holder.tvRightMsg.visibility = View.GONE
        } else {
            holder.tvRightTime.text = localTime
            holder.tvRightMsg.text = item.cMsg!!.content.data
            holder.tvRightTime.visibility = View.VISIBLE
            holder.tvRightMsg.visibility = View.VISIBLE
            holder.tvLeftTime.visibility = View.GONE
            holder.tvLeftMsg.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return if (list == null) 0 else list!!.size
    }

    inner class MsgViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvLeftTime: TextView
        var tvLeftMsg: TextView
        var tvRightTime: TextView
        var tvRightMsg: TextView

        init {
            tvLeftTime = itemView.findViewById(R.id.tv_left_time)
            tvLeftMsg = itemView.findViewById(R.id.tv_left_msg)
            tvRightTime = itemView.findViewById(R.id.tv_right_time)
            tvRightMsg = itemView.findViewById(R.id.tv_right_msg)
        }
    }
}