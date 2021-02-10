package com.example.appgupshup.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.appgupshup.databinding.ActivityOTPBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.mukesh.OnOtpCompletionListener;

import java.util.concurrent.TimeUnit;

public class OTP extends AppCompatActivity {
    ActivityOTPBinding binding;
    FirebaseAuth auth;
    String verificationId;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityOTPBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().hide();
        binding.otpview.requestFocus();
        progressDialog=new ProgressDialog(this);
        progressDialog.setMessage("Sending OTP ....");
        progressDialog.setCancelable(false);
        progressDialog.show();
        auth=FirebaseAuth.getInstance();

        String phonenumber=getIntent().getStringExtra("phoneNumber");
        binding.OTPphone.setText("Verify "+ phonenumber);

        //        VerificationCodeToUser(phonenumber);

        PhoneAuthOptions options=PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phonenumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(OTP.this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {

                    }

                    @Override
                    public void onCodeSent(@NonNull String verifyId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        super.onCodeSent(verifyId, forceResendingToken);
                        progressDialog.dismiss();
                        verificationId=verifyId;
                    }
                }).build();
        PhoneAuthProvider.verifyPhoneNumber(options);

        binding.otpview.setOtpCompletionListener(new OnOtpCompletionListener() {
            @Override
            public void onOtpCompleted(String otp) {
                PhoneAuthCredential credential=PhoneAuthProvider.getCredential(verificationId,otp);
                auth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Intent intent=new Intent(OTP.this, SetupProfile.class);
                            startActivity(intent);
                            finishAffinity();
                        }
                        else{
                            Toast.makeText(OTP.this, "Error", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }


//        private void VerificationCodeToUser(String phonenumber) {
//                PhoneAuthOptions options =
//                        PhoneAuthOptions.newBuilder(auth)
//                                .setPhoneNumber(phonenumber)       // Phone number to verify
//                                .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
//                                .setActivity(this)                 // Activity (for callback binding)
//                                .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
//                                .build();
//        PhoneAuthProvider.verifyPhoneNumber(options);
//    }
//    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
//        @Override
//        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
//            super.onCodeSent(s, forceResendingToken);
//
//            verificationId=s;
//        }
//
//        @Override
//        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
//           String code = phoneAuthCredential.getSmsCode();
//           if(code!=null){
//               //progressDialog.show();
//               verifyCode(code);
//           }
//        }
//
//        @Override
//        public void onVerificationFailed(@NonNull FirebaseException e) {
//            Toast.makeText(OTP.this, e.getMessage(), Toast.LENGTH_SHORT).show();
//        }
//    };
//
//    private void verifyCode(String codeByUser){
//        PhoneAuthCredential credential= PhoneAuthProvider.getCredential(verificationId,codeByUser);
//        SignInUser(credential);
//    }
//
//    private void SignInUser(PhoneAuthCredential credential) {
//        auth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
//            @Override
//            public void onComplete(@NonNull Task<AuthResult> task) {
//                if(task.isSuccessful()){
//                    Intent intent=new Intent(OTP.this, SetupProfile.class);
//                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                    startActivity(intent);
//                }
//                else{
//                    Toast.makeText(OTP.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
//    }
}