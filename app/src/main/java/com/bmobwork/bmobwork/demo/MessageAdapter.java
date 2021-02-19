package com.bmobwork.bmobwork.demo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bmobwork.bmobwork.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/*
 * Created by Administrator on 2021/2/19.
 */
public class MessageAdapter extends RecyclerView.Adapter<MessageHolder> {

    private Context context;
    private List<Messagebean> messages;

    public MessageAdapter(Context context, List<Messagebean> messages) {
        this.context = context;
        this.messages = messages;
    }

    public void notifys(List<Messagebean> messages) {
        this.messages = messages;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MessageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MessageHolder(LayoutInflater.from(context).inflate(R.layout.item_message, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MessageHolder holder, int position) {
        Messagebean messagebean = messages.get(position);
        boolean receiver = messagebean.isReceiver();// 是否为接收
        String content = messagebean.getContent();// 内容
        
        holder.tv_send.setVisibility(receiver ? View.GONE : View.VISIBLE);
        holder.tv_receive.setVisibility(holder.tv_send.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        if (!receiver) {
            holder.tv_send.setText(content);
        } else {
            holder.tv_receive.setText(content);
        }
    }

    @Override
    public int getItemCount() {
        return messages != null ? messages.size() : 0;
    }
}
