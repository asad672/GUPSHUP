package com.example.appgupshup.Adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appgupshup.Models.Message;
import com.example.appgupshup.R;
import com.example.appgupshup.databinding.ItemRecieveBinding;
import com.example.appgupshup.databinding.ItemSendBinding;
import com.github.pgreze.reactions.ReactionPopup;
import com.github.pgreze.reactions.ReactionsConfig;
import com.github.pgreze.reactions.ReactionsConfigBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class MessagesAdapter extends RecyclerView.Adapter {

    Context context;
    ArrayList<Message> messages;
    final int ITEM_RECIEVE = 1;
    final int ITEM_SEND = 2;

    String senderRoom;
    String receiverRoom;

    public MessagesAdapter(Context context, ArrayList<Message> messages, String senderRoom, String receiverRoom) {
        this.context = context;
        this.messages = messages;
        this.senderRoom = senderRoom;
        this.receiverRoom = receiverRoom;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ITEM_SEND) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_send, parent, false);
            return new SendViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_recieve, parent, false);
            return new RecieverViewHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        if (FirebaseAuth.getInstance().getUid().equals(message.getSenderId())) {
            return ITEM_SEND;
        } else {
            return ITEM_RECIEVE;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);

        int reactions[] = new int[]{
                R.drawable.ic_fb_like,
                R.drawable.ic_fb_love,
                R.drawable.ic_fb_laugh,
                R.drawable.ic_fb_wow,
                R.drawable.ic_fb_sad,
                R.drawable.ic_fb_angry
        };

        ReactionsConfig config = new ReactionsConfigBuilder(context)
                .withReactions(reactions)
                .build();

        ReactionPopup popup = new ReactionPopup(context, config, (pos) -> {
            if (pos > 0) {
                if (holder.getClass() == SendViewHolder.class) {
                    SendViewHolder viewHolder = (SendViewHolder) holder;
                    viewHolder.binding.feeling.setImageResource(reactions[pos]);
                    viewHolder.binding.feeling.setVisibility(View.VISIBLE);

                } else {
                    RecieverViewHolder viewHolder = (RecieverViewHolder) holder;
                    viewHolder.binding.feeling.setImageResource(reactions[pos]);
                    viewHolder.binding.feeling.setVisibility(View.VISIBLE);
                }
            } else {
                Toast.makeText(context, "Nothing is selected", Toast.LENGTH_SHORT).show();
            }

            message.setFeeling(pos);
            FirebaseDatabase.getInstance().getReference()
                    .child("chats")
                    .child(senderRoom)
                    .child("messages")
                    .child(message.getMessageId()).setValue(message);
            FirebaseDatabase.getInstance().getReference()
                    .child("chats")
                    .child(receiverRoom)
                    .child("messages")
                    .child(message.getMessageId()).setValue(message);
            return true; // true is closing popup, false is requesting a new selection
        });
        if (message.getType().equals("text")) {
            if (holder.getClass() == SendViewHolder.class) {
                SendViewHolder viewHolder = (SendViewHolder) holder;
                viewHolder.setIsRecyclable(false);
                viewHolder.binding.message.setVisibility(View.VISIBLE);
                viewHolder.binding.message.setText(message.getMessage());

                if (message.getFeeling() >= 0) {
                    viewHolder.binding.feeling.setImageResource(reactions[message.getFeeling()]);
                    viewHolder.binding.feeling.setVisibility(View.VISIBLE);
                } else {
                    viewHolder.binding.feeling.setVisibility(View.GONE);
                }

                viewHolder.binding.message.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        popup.onTouch(v, event);

                        return false;
                    }
                });

            } else if (holder.getClass() == RecieverViewHolder.class) {
                RecieverViewHolder viewHolder = (RecieverViewHolder) holder;
                viewHolder.setIsRecyclable(false);
                viewHolder.binding.message.setText(message.getMessage());

                if (message.getFeeling() >= 0) {
                    viewHolder.binding.feeling.setImageResource(reactions[message.getFeeling()]);
                    viewHolder.binding.feeling.setVisibility(View.VISIBLE);
                } else {
                    viewHolder.binding.feeling.setVisibility(View.GONE);
                }

                viewHolder.binding.message.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        popup.onTouch(v, event);

                        return false;
                    }
                });
            }
        }
        else if (message.getType().equals("image")) {
            if (holder.getClass() == SendViewHolder.class) {
                SendViewHolder viewHolder = (SendViewHolder) holder;
                viewHolder.setIsRecyclable(false);
               viewHolder.binding.message.setVisibility(View.GONE);
                viewHolder.binding.picmsg.setVisibility(View.VISIBLE);
                Picasso.get().load(message.getMessage()).into((viewHolder.binding.picmsg));

            }
            else{
                RecieverViewHolder viewHolder = (RecieverViewHolder) holder;
                viewHolder.setIsRecyclable(false);
                viewHolder.binding.message.setVisibility(View.GONE);
                viewHolder.binding.picmsg.setVisibility(View.VISIBLE);
                Picasso.get().load(message.getMessage()).into((viewHolder.binding.picmsg));
            }
        }
        else if (message.getType().equals("pdf") || message.getType().equals("docx")){
            if (holder.getClass() == SendViewHolder.class) {
                SendViewHolder viewHolder = (SendViewHolder) holder;
                viewHolder.setIsRecyclable(false);
                viewHolder.binding.message.setVisibility(View.GONE);
                viewHolder.binding.picmsg.setVisibility(View.VISIBLE);
                if(message.getType().equals("pdf")) {
                    viewHolder.binding.picmsg.setBackgroundResource(R.drawable.ic_pdf);
                }
                else if(message.getType().equals("docx")){
                    viewHolder.binding.picmsg.setBackgroundResource(R.drawable.ic_word);
                }
                viewHolder.binding.picmsg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse(messages.get(position).getMessage()));
                        viewHolder.binding.picmsg.getContext().startActivity(intent);
                    }
                });

            }
            else{
                RecieverViewHolder viewHolder = (RecieverViewHolder) holder;
                viewHolder.setIsRecyclable(false);
                viewHolder.binding.message.setVisibility(View.GONE);
                viewHolder.binding.picmsg.setVisibility(View.VISIBLE);
                if(message.getType().equals("pdf")) {
                    viewHolder.binding.picmsg.setBackgroundResource(R.drawable.ic_pdf);
                }
                else if(message.getType().equals("docx")){
                    viewHolder.binding.picmsg.setBackgroundResource(R.drawable.ic_word);
                }
                viewHolder.binding.picmsg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse(messages.get(position).getMessage()));
                        viewHolder.binding.picmsg.getContext().startActivity(intent);
                    }
                });
            }
        }
    }
    @Override
    public int getItemCount () {
        return messages.size();
    }

    public class SendViewHolder extends RecyclerView.ViewHolder {
        ItemSendBinding binding;

        public SendViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemSendBinding.bind(itemView);
        }
    }

    public class RecieverViewHolder extends RecyclerView.ViewHolder {
        ItemRecieveBinding binding;

        public RecieverViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemRecieveBinding.bind(itemView);
        }
    }
}
