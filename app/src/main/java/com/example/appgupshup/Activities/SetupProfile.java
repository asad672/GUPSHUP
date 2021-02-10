package com.example.appgupshup.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.appgupshup.Models.Users;
import com.example.appgupshup.databinding.ActivitySetupProfileBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class SetupProfile extends AppCompatActivity {
    ActivitySetupProfileBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database;
    FirebaseStorage storage;
    Uri selectedImage;
    ProgressDialog progressDialog;
    Users us;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivitySetupProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().hide();

        auth=FirebaseAuth.getInstance();
        database=FirebaseDatabase.getInstance();
        storage=FirebaseStorage.getInstance();

        progressDialog=new ProgressDialog(this);
        progressDialog.setMessage("Updating User Profile");
        progressDialog.setCancelable(false);


        binding.propic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,45);
            }
        });


            binding.prosaveinfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String name=binding.proname.getText().toString();
                    if(name.isEmpty()){
                        binding.proname.setError("Please Enter Name");
                    }
                    else {
                        progressDialog.show();
                        if (selectedImage != null) {
                            StorageReference reference = storage.getReference().child("Profiles").child(auth.getUid());
                            reference.putFile(selectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                                String ImageUrl = uri.toString();

                                                String uid = auth.getUid();
                                                String phone = auth.getCurrentUser().getPhoneNumber();
                                                String name = binding.proname.getText().toString();

                                                Users user = new Users(uid, name, phone, ImageUrl);


                                                database.getReference()
                                                        .child("users")
                                                        .child(uid)
                                                        .setValue(user)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                progressDialog.dismiss();
                                                                Intent intent = new Intent(SetupProfile.this, MainActivity.class);
                                                                startActivity(intent);
                                                                finish();
                                                            }
                                                    });
                                            }
                                        });
                                    }
                                }
                            });
                        }
                    else {
                            String uid = auth.getUid();
                            String phone = auth.getCurrentUser().getPhoneNumber();

                            Users user = new Users(uid, name, phone, "No Image");


                            database.getReference()
                                    .child("users")
                                    .child(uid)
                                    .setValue(user)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            progressDialog.dismiss();
                                            Intent intent = new Intent(SetupProfile.this, MainActivity.class);
                                            startActivity(intent);
                                            finish();
                                        }
                               });
                        }
                    }
                }
            });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data!=null){
            if(data.getData()!=null){
                binding.propic.setImageURI(data.getData());
                selectedImage=data.getData();
            }
        }
    }
}