package com.teneasy.chatuisdk.ui.main;

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

import com.teneasy.sdk.TimeUtil
import com.teneasy.sdk.ui.MessageItem
import java.util.*
import com.teneasy.chatuisdk.R
import com.teneasy.sdk.ui.MessageSendState

class MessageListAdapter (myContext: Context) : RecyclerView.Adapter<MessageListAdapter.MsgViewHolder>() {
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
        if (!item.isLeft) {
            holder.tvRightTime.text = localTime
            holder.tvRightTime.visibility = View.VISIBLE
            holder.tvRightMsg.visibility = View.VISIBLE
            holder.lySend.visibility = View.VISIBLE

            holder.tvLeftTime.visibility = View.GONE
            holder.ivLeftImg.visibility = View.GONE
            holder.tvLeftMsg.visibility = View.GONE

            if(item.sendStatus != MessageSendState.发送成功) {
                holder.ivSendStatus.visibility = View.VISIBLE
            } else
                holder.ivSendStatus.visibility = View.GONE

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
            holder.lySend.visibility = View.GONE

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
        // 因为要处理接收和自己发送的消息，所以单纯的判断msg是不够的。需要直接判断imgPath是否为空
//        if(obj.isSend) {
//            if(obj.imgPath != null && obj.imgPath.isNotEmpty()) {
//                return TYPE_Image
//            }
//        } else {
            obj.cMsg?.apply {
                return if (this.hasImage()){
                    return TYPE_Image
                } else {
                    return TYPE_Text
                }
            }
//        }
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
        var ivSendStatus: ImageView
        var lySend: View

        init {
            tvLeftTime = itemView.findViewById(R.id.tv_left_time)
            tvLeftMsg = itemView.findViewById(R.id.tv_left_msg)
            ivLeftImg = itemView.findViewById(R.id.iv_left_image)

            ivRightImg = itemView.findViewById(R.id.iv_right_image)
            tvRightTime = itemView.findViewById(R.id.tv_right_time)
            tvRightMsg = itemView.findViewById(R.id.tv_right_msg)
            ivSendStatus = itemView.findViewById(R.id.iv_send_status)
            lySend = itemView.findViewById(R.id.layout_send)
        }
    }
}