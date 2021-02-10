package com.example.appgupshup.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appgupshup.databinding.ActivityVerificationBinding;
import com.google.firebase.auth.FirebaseAuth;

public class Verification extends AppCompatActivity {
    ActivityVerificationBinding binding;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= ActivityVerificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth= FirebaseAuth.getInstance();

        if(auth.getCurrentUser()!=null){
            Intent intent=new Intent(Verification.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        getSupportActionBar().hide();
        binding.veriPhone.requestFocus();
        binding.veriContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(Verification.this, OTP.class);
                intent.putExtra("phoneNumber",binding.veriPhone.getText().toString());
                startActivity(intent);
            }
        });
    }
}