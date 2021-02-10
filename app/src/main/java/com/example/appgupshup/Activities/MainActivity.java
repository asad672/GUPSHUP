package com.example.appgupshup.Activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appgupshup.Adapters.TopStatusAdapter;
import com.example.appgupshup.Adapters.UsersAdapter;
import com.example.appgupshup.Models.Status;
import com.example.appgupshup.Models.UserStatus;
import com.example.appgupshup.Models.Users;
import com.example.appgupshup.R;
import com.example.appgupshup.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    FirebaseDatabase database;
    FirebaseAuth auth;
    ArrayList<Users> users;
    ArrayList<UserStatus> userStatuses;
    UsersAdapter usersAdapter;
    ProgressDialog progressDialog;
    ProgressDialog Dialog;
    TopStatusAdapter statusAdapter;
    Users user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if(isConnected()) {

            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Loading your chat ...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }
        else{
            Toast.makeText(this, "No internet!", Toast.LENGTH_LONG).show();
        }
        Dialog = new ProgressDialog(this);
        Dialog.setMessage("Uploading Image ...");
        Dialog.setCancelable(false);

        database=FirebaseDatabase.getInstance();
        auth=FirebaseAuth.getInstance();
        users=new ArrayList<>();
        userStatuses=new ArrayList<>();

        database.getReference().child("users").child(FirebaseAuth.getInstance().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        user = snapshot.getValue(Users.class);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        usersAdapter=new UsersAdapter(this,users);
        binding.recyclerview.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerview.setAdapter(usersAdapter);

        statusAdapter=new TopStatusAdapter(this,userStatuses);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.HORIZONTAL);
        binding.satuslist.setLayoutManager(layoutManager);
        binding.satuslist.setAdapter(statusAdapter);

        database.getReference().child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                users.clear();
                progressDialog.dismiss();
                for(DataSnapshot snapshot1 : snapshot.getChildren()){
                    Users user = snapshot1.getValue(Users.class);
                    if(user.getNumber().equals(auth.getCurrentUser().getPhoneNumber())) {
                        Toast.makeText(MainActivity.this, "Welcome " + user.getName(), Toast.LENGTH_SHORT).show();
                    }
                    else{
                        users.add(user);
                    }
                }
                usersAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        database.getReference().child("stories").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    userStatuses.clear();
                    for(DataSnapshot storySnapshot : snapshot.getChildren()) {
                        UserStatus status = new UserStatus();
                        status.setName(storySnapshot.child("name").getValue(String.class));
                        status.setProfileImage(storySnapshot.child("profileImage").getValue(String.class));
                        status.setLastUpdated(storySnapshot.child("lastUpdated").getValue(String.class));

                        ArrayList<Status> statuses = new ArrayList<>();

                        for(DataSnapshot statusSnapshot : storySnapshot.child("statuses").getChildren()) {
                            Status sampleStatus = statusSnapshot.getValue(Status.class);
                            statuses.add(sampleStatus);
                        }
                        status.setStatuses(statuses);
                        userStatuses.add(status);
                    }
                    statusAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        binding.bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.status:
                        Intent intent1=new Intent();
                        intent1.setType("image/*");
                        intent1.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(intent1,75);

                        break;
                }

                return false;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data!=null){
            if (data != null) {
                Dialog.show();
                Calendar calendar= Calendar.getInstance();
                SimpleDateFormat simpleDateFormat=new SimpleDateFormat("hh:mm a");
                String dateTime = simpleDateFormat.format(calendar.getTime());
                FirebaseStorage storage=FirebaseStorage.getInstance();
                StorageReference reference=storage.getReference().child("status").child(dateTime + "");

                reference.putFile(data.getData()).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    UserStatus userStatus=new UserStatus();
                                    userStatus.setName(user.getName());
                                    userStatus.setProfileImage(user.getProfileimage());
                                    userStatus.setLastUpdated(dateTime);

                                    HashMap<String,Object> obj=new HashMap<>();
                                    obj.put("name", userStatus.getName());
                                    obj.put("profileImage", userStatus.getProfileImage());
                                    obj.put("lastUpdated", userStatus.getLastUpdated());

                                    String imageUrl = uri.toString();
                                    Status status = new Status(imageUrl,userStatus.getLastUpdated());

                                    database.getReference()
                                            .child("stories")
                                            .child(FirebaseAuth.getInstance().getUid())
                                            .updateChildren(obj);

                                    database.getReference().child("stories")
                                            .child(FirebaseAuth.getInstance().getUid())
                                            .child("statuses")
                                            .push()
                                            .setValue(status);
                                    Dialog.dismiss();
                                }
                            });
                        }

                    }
                });

            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.topmenu,menu);
//        MenuItem item=menu.findItem(R.id.search);
//
//        SearchView searchView=(SearchView) MenuItemCompat.getActionView(item);
//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                SearchChat(query);
//                return false;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String newText) {
//                SearchChat(newText);
//                return false;
//            }
//        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){

            case R.id.search:
                Toast.makeText(this, "Search Clicked", Toast.LENGTH_SHORT).show();
                break;
            case R.id.setting:
                Toast.makeText(this, "Setting Clicked", Toast.LENGTH_SHORT).show();
                Intent in=new Intent(this,SetupProfile.class);
                startActivity(in);
                break;
            case R.id.logout:
                auth.signOut();
                Intent intent = new Intent(MainActivity.this, Verification.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    public boolean isConnected() {
        boolean connected = false;
        try {
            ConnectivityManager cm = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo nInfo = cm.getActiveNetworkInfo();
            connected = nInfo != null && nInfo.isAvailable() && nInfo.isConnected();
            return connected;
        } catch (Exception e) {
            Log.e("Connectivity Exception", e.getMessage());
        }
        return connected;
    }

//    private void SearchChat(String searchText) {
//        String quary = searchText.toLowerCase();
//        Query query = database.getReference().orderByChild("name")
//                .startAt(quary)
//                .endAt(quary + "\uf8ff");
//
//        query.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//            if(snapshot.hasChildren()){
//               users.clear();
//               for(DataSnapshot dss:snapshot.getChildren()){
//                   final Users us=dss.getValue(Users.class);
//                   users.add(us);
//               }
//                usersAdapter.notifyDataSetChanged();
//            }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });

//    }
}