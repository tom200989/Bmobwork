package com.bmobwork.bmobwork.demo;

import android.view.View;
import android.widget.TextView;

import com.bmobwork.bmobwork.R;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/*
 * Created by Administrator on 2021/2/19.
 */
public class MessageHolder extends RecyclerView.ViewHolder {

    public final TextView tv_send;
    public final TextView tv_receive;

    public MessageHolder(@NonNull View itemView) {
        super(itemView);
        tv_send = itemView.findViewById(R.id.tv_msg_send);
        tv_receive = itemView.findViewById(R.id.tv_msg_receive);
    }
}
