package com.edorbit.bhraman;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class SignUp extends AppCompatActivity {

    TextInputLayout regName, regEmail, regPassword;
    Button regBtn, regToLoginBtn;
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    FirebaseAuth mAuth=FirebaseAuth.getInstance();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        regName = findViewById(R.id.reg_name);
        regEmail = findViewById(R.id.reg_email);
        regPassword = findViewById(R.id.reg_password);
        regBtn = findViewById(R.id.reg_btn);
        regToLoginBtn = findViewById(R.id.reg_login_btn);
        progressDialog = new ProgressDialog(SignUp.this);

        //Save data in FireBase on button click
        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = regName.getEditText().getText().toString();
                String email = regEmail.getEditText().getText().toString();
                String password = regPassword.getEditText().getText().toString();

                if(name.isEmpty()){
                    regName.setError("Name is required");
                    regName.requestFocus();
                    return;
                }else {
                    regName.setError(null);
                }
                if (!email.matches(emailPattern)) {
                    regEmail.setError("Enter correct Email");
                    regEmail.requestFocus();
                    return;
                } else {
                    regEmail.setError(null);
                }
                if(password.length()<6){
                    regPassword.setError("Must have 6 characters");
                    regPassword.requestFocus();
                    return;
                }else{
                    regPassword.setError(null);
                }
                progressDialog.setMessage("Please Wait while Registration....");
                progressDialog.setTitle("Registration");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();
                mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){

                            UserHelperClass userClass = new UserHelperClass(name, email);
                            database.getReference("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .setValue(userClass).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                FirebaseAuth.getInstance().getCurrentUser().sendEmailVerification();
                                                mAuth.signInWithEmailAndPassword(email,password)
                                                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<AuthResult> task) {
                                                                if(task.isSuccessful()){
                                                                    progressDialog.dismiss();
                                                                    Toast.makeText(SignUp.this, "Login Successfully", Toast.LENGTH_SHORT).show();
                                                                }
                                                                else{
                                                                    progressDialog.dismiss();
                                                                    Toast.makeText(SignUp.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        });

                                            }
                                            else{
                                                progressDialog.dismiss();
                                                Toast.makeText(SignUp.this, ""+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            }

                                        }
                                    });


                        }
                        else{
                            progressDialog.dismiss();
                            Toast.makeText(SignUp.this, ""+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                //Get all the values
            }
        });//Register Button method end
    }//onCreate Method End

}