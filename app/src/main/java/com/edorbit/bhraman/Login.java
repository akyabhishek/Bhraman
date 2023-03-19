package com.edorbit.bhraman;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Login extends AppCompatActivity {
    SignInButton googlebtn;

    Button callSignUp,btnlogin,fp;
    TextInputLayout lemail,lpassword;
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    FirebaseAuth mAuth=FirebaseAuth.getInstance();
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);
        if(FirebaseAuth.getInstance().getCurrentUser()!=null){
            sendusettonextactivity();
        }

        progressDialog = new ProgressDialog(Login.this);
        googlebtn = findViewById(R.id.sign_in_button);

        callSignUp = findViewById(R.id.call_signUp_screen);
        lemail = findViewById(R.id.email);
        lpassword = findViewById(R.id.password);
        btnlogin=findViewById(R.id.btn_login);
        fp=findViewById(R.id.fp);

        googlebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Login.this, googleSigin.class);
                startActivity(intent);
            }
        });

        callSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Login.this,SignUp.class));
            }
        });
        btnlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                perforlogin();
            }
        });
        fp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater li = LayoutInflater.from(getApplicationContext());
                MaterialAlertDialogBuilder passwordrestdialog = new MaterialAlertDialogBuilder(view.getContext());
                passwordrestdialog.setTitle("Reset Password");
                passwordrestdialog.setMessage("Enter your email to receive the reset link");
                View v=li.inflate(R.layout.forget_password,null);
                passwordrestdialog.setView(v);
                TextInputLayout resetmail=v.findViewById(R.id.resetemail);

                passwordrestdialog.setPositiveButton("Send", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        String mail = resetmail.getEditText().getText().toString().trim();
                        if (!mail.isEmpty()&&mail.matches(emailPattern)) {
                            progressDialog.setMessage(" Please Wait while processing....");
                            progressDialog.setTitle("Sending");
                            progressDialog.setCanceledOnTouchOutside(false);
                            progressDialog.show();
                            mAuth.sendPasswordResetEmail(mail).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(@NonNull Void unused) {
                                    progressDialog.dismiss();
                                    Toast.makeText(Login.this, "Reset Link Sent To Your Email", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressDialog.dismiss();
                                    Toast.makeText(Login.this, ""+e.getMessage(), Toast.LENGTH_LONG).show();;
                                }
                            });
                        }else{
                            Toast.makeText(Login.this, "Please enter correct email", Toast.LENGTH_SHORT).show();
                        }

                    }
                }).show();
            }
        });
    }

    private void perforlogin() {
        String email = lemail.getEditText().getText().toString().trim();
        String password = lpassword.getEditText().getText().toString().trim();

        if (!email.matches(emailPattern)) {
            lemail.setError("Enter correct Email");
            return;

        } else if (password.isEmpty() || password.length() < 6) {
            lpassword.setError("Enter correct Password");
            return;

        } else {
            progressDialog.setMessage(" Please Wait while Login....");
            progressDialog.setTitle("Login");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        progressDialog.dismiss();
                        finish();
                        sendusettonextactivity();
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(Login.this, "Something went wrong"+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

    }

    private void sendusettonextactivity() {
        Intent intent = new Intent(Login.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}