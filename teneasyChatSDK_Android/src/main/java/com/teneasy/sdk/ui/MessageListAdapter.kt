package com.teneasy.sdk.ui

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.teneasy.sdk.R
import com.teneasy.sdk.TimeUtil
import com.teneasy.sdk.ui.MessageListAdapter.MsgViewHolder
import java.util.*

class MessageListAdapter (myContext: Context) : RecyclerView.Adapter<MsgViewHolder>() {
    private var list: ArrayList<MessageItem>? = null
    var TYPE_Text : Int = 0
    val TYPE_Image : Int = 1
    val act: Context = myContext
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
            holder.tvRightTime.text = localTime
            holder.tvRightTime.visibility = View.VISIBLE
            holder.tvRightMsg.visibility = View.VISIBLE

            holder.tvLeftTime.visibility = View.GONE
            holder.ivLeftImg.visibility = View.GONE
            holder.tvLeftMsg.visibility = View.GONE

            if (getItemViewType(position) == TYPE_Text){
                holder.tvRightMsg.visibility = View.VISIBLE
               holder.ivRightImg.visibility = View.GONE
                holder.tvRightMsg.text = item.cMsg!!.content.data
            }else{
                holder.tvRightMsg.visibility = View.GONE
                holder.ivRightImg.visibility = View.VISIBLE
                Glide.with(act).load(item.cMsg!!.image.uri).dontAnimate()
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .into(holder.ivRightImg)
            }
        } else {
            holder.tvLeftTime.text = localTime
            holder.tvLeftTime.visibility = View.VISIBLE
            holder.tvLeftMsg.visibility = View.VISIBLE
            holder.tvRightTime.visibility = View.GONE
            holder.tvRightMsg.visibility = View.GONE
            holder.ivRightImg.visibility = View.GONE
            holder.tvRightMsg.visibility = View.GONE

            if (getItemViewType(position) == TYPE_Text){
                holder.tvLeftMsg.visibility = View.VISIBLE
                holder.ivLeftImg.visibility = View.GONE
                holder.tvLeftMsg.text = item.cMsg!!.content.data
            }else{
                holder.tvLeftMsg.visibility = View.GONE
                holder.ivLeftImg.visibility = View.VISIBLE
                Glide.with(act).load(item.cMsg!!.image.uri).dontAnimate()
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .into(holder.ivLeftImg)
            }
        }
    }

    override
    fun getItemViewType(position: Int) : Int{
        if (list == null) {
            return TYPE_Text
        }
        val obj = list!![position]
        obj.cMsg?.apply {
            return if (this.hasImage()){
                return TYPE_Image
            } else {
                return TYPE_Text
            }
        }
        return TYPE_Text
    }

    override fun getItemCount(): Int {
        return if (list == null) 0 else list!!.size
    }

    inner class MsgViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvLeftTime: TextView
        var tvLeftMsg: TextView
        var ivLeftImg: AppCompatImageView

        var tvRightTime: TextView
        var tvRightMsg: TextView
        var ivRightImg: AppCompatImageView

        init {
            tvLeftTime = itemView.findViewById(R.id.tv_left_time)
            tvLeftMsg = itemView.findViewById(R.id.tv_left_msg)
            ivLeftImg = itemView.findViewById(R.id.iv_left_image)

            ivRightImg = itemView.findViewById(R.id.iv_right_image)
            tvRightTime = itemView.findViewById(R.id.tv_right_time)
            tvRightMsg = itemView.findViewById(R.id.tv_right_msg)
        }
    }
}