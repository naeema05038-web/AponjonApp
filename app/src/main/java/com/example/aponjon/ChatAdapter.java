package com.example.aponjon;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private List<ChatActivity.ChatMessage> messages;

    public ChatAdapter(List<ChatActivity.ChatMessage> messages) {
        this.messages = messages;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_message, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatActivity.ChatMessage message = messages.get(position);

        if (message.isUser()) {
            holder.userLayout.setVisibility(View.VISIBLE);
            holder.botLayout.setVisibility(View.GONE);
            holder.tvUserMessage.setText(message.getMessage());
            holder.tvUserTime.setText(message.getTime());
        } else {
            holder.userLayout.setVisibility(View.GONE);
            holder.botLayout.setVisibility(View.VISIBLE);
            holder.tvBotMessage.setText(message.getMessage());
            holder.tvBotTime.setText(message.getTime());
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserMessage, tvUserTime, tvBotMessage, tvBotTime;
        View userLayout, botLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserMessage = itemView.findViewById(R.id.tvUserMessage);
            tvUserTime = itemView.findViewById(R.id.tvUserTime);
            tvBotMessage = itemView.findViewById(R.id.tvBotMessage);
            tvBotTime = itemView.findViewById(R.id.tvBotTime);
            userLayout = itemView.findViewById(R.id.userMessageLayout);
            botLayout = itemView.findViewById(R.id.botMessageLayout);
        }
    }
}