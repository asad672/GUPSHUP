package com.example.appgupshup.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.appgupshup.Activities.ChatActivity;
import com.example.appgupshup.Models.Users;
import com.example.appgupshup.R;
import com.example.appgupshup.databinding.RowConversationBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UsersViewHolder>{
        Context context;
        ArrayList<Users> users;
        FirebaseAuth auth;
        public UsersAdapter(Context context,ArrayList<Users> users){
            this.context=context;
            this.users=users;
        }
        @NonNull
        @Override
        public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view= LayoutInflater.from(context).inflate(R.layout.row_conversation,parent,false);
            return new UsersViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull UsersViewHolder holder, int position) {
            Users user = users.get(position);
            String senderId = FirebaseAuth.getInstance().getUid();
            String senderRoom = senderId + user.getUid();

            FirebaseDatabase.getInstance().getReference()
                    .child("chats")
                    .child(senderRoom)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                String lastMsg = snapshot.child("chatMsg").getValue(String.class);
                                String time = snapshot.child("chattime").getValue(String.class);

                                holder.binding.chattime.setText(time);

                                holder.binding.chatmsg.setText(lastMsg);
                            } else {
                                holder.binding.chatmsg.setText("Tap to Chat");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
            holder.binding.chatname.setText(user.getName());
            Glide.with(context).load(user.getProfileimage())
                    .placeholder(R.drawable.ic_baseline_account_circle_24)
                    .into(holder.binding.chatpic);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, ChatActivity.class);
                    intent.putExtra("name", user.getName());
                    intent.putExtra("uid", user.getUid());
                    context.startActivity(intent);
                }
            });
        }
        @Override
        public int getItemCount() {
            return users.size();
        }

        public class UsersViewHolder extends RecyclerView.ViewHolder {
            RowConversationBinding binding;
            public UsersViewHolder(@NonNull View itemView) {
                super(itemView);
                binding=RowConversationBinding.bind(itemView);
            }
        }
}
