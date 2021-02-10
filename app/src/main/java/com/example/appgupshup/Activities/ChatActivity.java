package com.example.appgupshup.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.appgupshup.Adapters.MessagesAdapter;
import com.example.appgupshup.Models.Message;
import com.example.appgupshup.R;
import com.example.appgupshup.databinding.ActivityChatBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.jitsi.meet.sdk.JitsiMeet;
import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class ChatActivity extends AppCompatActivity {
    ActivityChatBinding binding;
    MessagesAdapter messagesAdapter;
    ArrayList<Message> messages;
    String senderRoom, receiverRoom;
    FirebaseAuth auth;
    FirebaseDatabase database;
    FirebaseStorage storage;
    String receiverUid, senderUid;
    String checker;
    Uri fileUri;
    URL serverURL;
    boolean flag=true;
    String callerid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        messages = new ArrayList<>();

        String name = getIntent().getStringExtra("name");
        receiverUid = getIntent().getStringExtra("uid");
        senderUid = auth.getUid();

        senderRoom = senderUid + receiverUid;
        receiverRoom = receiverUid + senderUid;

        messagesAdapter = new MessagesAdapter(this, messages, senderRoom, receiverRoom);
        binding.recyclerview.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerview.setAdapter(messagesAdapter);


        database.getReference().child("chats")
                .child(senderRoom)
                .child("messages")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messages.clear();
                        for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                            Message message = snapshot1.getValue(Message.class);
                            message.setMessageId(snapshot1.getKey());
                            messages.add(message);
                        }
                        messagesAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });



        binding.sendmsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
             SendTextMsg();
            }
        });

        binding.attachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              SendAttachment();
            }
        });
        getSupportActionBar().setTitle(name);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }



   public void SendTextMsg(){
       checker="text";
       messages.clear();
       String messageText = binding.msgbox.getText().toString();
       Calendar calendar = Calendar.getInstance();
       SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm a");
       String dateTime = simpleDateFormat.format(calendar.getTime());
       String randomKey = database.getReference().push().getKey();
       Message message = new Message(messageText, senderUid, dateTime,checker,randomKey);
       binding.msgbox.setText("");



       HashMap<String, Object> lastMsgObj = new HashMap<>();
       lastMsgObj.put("chatMsg", message.getMessage());
       lastMsgObj.put("chattime", message.getTimestamp());

       database.getReference().child("chats").child(senderRoom).updateChildren(lastMsgObj);
       database.getReference().child("chats").child(receiverRoom).updateChildren(lastMsgObj);

       database.getReference().child("chats")
               .child(senderRoom)
               .child("messages")
               .child(randomKey)
               .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
           @Override
           public void onSuccess(Void aVoid) {
               database.getReference().child("chats")
                       .child(receiverRoom)
                       .child("messages")
                       .child(randomKey)
                       .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                   @Override
                   public void onSuccess(Void aVoid) {

                   }
               });

           }
       });
    }

    public void SendAttachment(){
        CharSequence options[] = new CharSequence[]
                {
                        "Images",
                        "PDF Files",
                        "Ms Word Files"
                };
        AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
        builder.setTitle("Select File");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    checker = "image";
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    startActivityForResult(intent, 82);
                    Toast.makeText(ChatActivity.this, checker, Toast.LENGTH_SHORT).show();
                }
                if (which == 1) {
                    checker = "pdf";
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    intent.setType("application/pdf");
                    startActivityForResult(intent, 82);
                    Toast.makeText(ChatActivity.this, checker, Toast.LENGTH_SHORT).show();
                }
                if (which == 2) {
                    checker = "docx";
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    intent.setType("application/msword");
                    startActivityForResult(intent, 82);
                    Toast.makeText(ChatActivity.this, checker, Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.show();
    }


    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        messages.clear();
        if (data != null) {
            if (data.getData() != null) {

                if (!checker.equals("image")) {
                    fileUri = data.getData();
                    if (fileUri != null) {
                        StorageReference reference = storage.getReference().child("pdfFile").child(data.getData() + "");
                        reference.putFile(fileUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if (task.isSuccessful()) {
                                    reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            messages.clear();
                                            String pdfwordfile = uri.toString();
                                            Calendar calendar = Calendar.getInstance();
                                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm a");
                                            String dateTime = simpleDateFormat.format(calendar.getTime());
                                            String randomKey = database.getReference().push().getKey();
                                            Message message = new Message(pdfwordfile, senderUid, dateTime, checker,randomKey);

                                            HashMap<String, Object> Obj1 = new HashMap<>();
                                            Obj1.put("chatMsg", message.getMessage());
                                            Obj1.put("chattime", message.getTimestamp());

                                            database.getReference().child("chats").child(senderRoom).updateChildren(Obj1);
                                            database.getReference().child("chats").child(receiverRoom).updateChildren(Obj1);

                                            database.getReference().child("chats")
                                                    .child(senderRoom)
                                                    .child("messages")
                                                    .child(randomKey)
                                                    .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Toast.makeText(ChatActivity.this, "File Sent Successfully", Toast.LENGTH_SHORT).show();
                                                    database.getReference().child("chats")
                                                            .child(receiverRoom)
                                                            .child("messages")
                                                            .child(randomKey)
                                                            .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {

                                                        }
                                                    });
                                                }
                                            });
                                        }
                                    });
                                }
                            }
                        });
                    }
                }

                else if (checker.equals("image")) {
                    fileUri=data.getData();
                    if (fileUri != null) {
                        StorageReference reference = storage.getReference().child("MsgImages").child(data.getData()+"");
                        reference.putFile(fileUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if (task.isSuccessful()) {
                                    reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            messages.clear();
                                            String imageUrl = uri.toString();
                                            Calendar calendar = Calendar.getInstance();
                                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm a");
                                            String dateTime = simpleDateFormat.format(calendar.getTime());
                                            String randomKey = database.getReference().push().getKey();
                                            Message message = new Message(imageUrl, senderUid, dateTime,checker,randomKey);

                                            HashMap<String, Object> Obj1 = new HashMap<>();
                                            Obj1.put("chatMsg", message.getMessage());
                                            Obj1.put("chattime", message.getTimestamp());

                                            database.getReference().child("chats").child(senderRoom).updateChildren(Obj1);
                                            database.getReference().child("chats").child(receiverRoom).updateChildren(Obj1);

                                            database.getReference().child("chats")
                                                    .child(senderRoom)
                                                    .child("messages")
                                                    .child(randomKey)
                                                    .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Toast.makeText(ChatActivity.this, "Photo Sent Successfully", Toast.LENGTH_SHORT).show();
                                                    database.getReference().child("chats")
                                                            .child(receiverRoom)
                                                            .child("messages")
                                                            .child(randomKey)
                                                            .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {

                                                        }
                                                    });
                                                }
                                            });
                                        }
                                    });
                                }
                            }
                        });
                    }
                }
            }
            else {
                    Toast.makeText(this, "Nothing Selected!", Toast.LENGTH_SHORT).show();
                }
            }
        }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.call,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            case R.id.voice:

                try {
                    serverURL = new URL("https://meet.jit.si");
                    JitsiMeetConferenceOptions ConferenceOptions =
                            new JitsiMeetConferenceOptions.Builder()
                                    .setServerURL(serverURL)
                                    .setWelcomePageEnabled(false)
                                    .build();
                    JitsiMeet.setDefaultConferenceOptions(ConferenceOptions);

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                database.getReference().child("chats")
                        .child(senderRoom)
                        .child("messages")
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                messages.clear();
                                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                                    Message message = snapshot1.getValue(Message.class);
                                    message.setMessageId(snapshot1.getKey());
                                    messages.add(message);
                                    JitsiMeetConferenceOptions optionsss = new JitsiMeetConferenceOptions.Builder()
                                            .setRoom(message.getCallerid())
                                            .setWelcomePageEnabled(false)
                                            .setAudioOnly(true)
                                            .build();
                                    JitsiMeetActivity.launch(ChatActivity.this, optionsss);
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                break;

            case R.id.video:

                try {
                    serverURL = new URL("https://meet.jit.si");
                    JitsiMeetConferenceOptions jitsiMeetConferenceOptions=
                            new JitsiMeetConferenceOptions.Builder()
                                    .setServerURL(serverURL)
                                    .setWelcomePageEnabled(false)
                                    .build();
                    JitsiMeet.setDefaultConferenceOptions(jitsiMeetConferenceOptions);

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                database.getReference().child("chats")
                        .child(senderRoom)
                        .child("messages")
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                messages.clear();
                                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                                    Message message = snapshot1.getValue(Message.class);
                                    message.setMessageId(snapshot1.getKey());
                                    messages.add(message);
                                    JitsiMeetConferenceOptions options = new JitsiMeetConferenceOptions.Builder()
                                            .setRoom(message.getCallerid())
                                            .setWelcomePageEnabled(false)
                                            .build();
                                    JitsiMeetActivity.launch(ChatActivity.this, options);
                                }

                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                break;
        }
        return super.onOptionsItemSelected(item);
     }
    }