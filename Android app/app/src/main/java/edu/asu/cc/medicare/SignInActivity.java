package edu.asu.cc.medicare;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.WindowManager;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.github.pierry.progress.Progress;

public class SignInActivity extends AppCompatActivity {

    @BindView(R.id.et_email)
    EditText et_email;

    @BindView(R.id.et_password)
    EditText et_password;


    MedicareApplication medicareApplication;
    Progress progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        ButterKnife.bind(this);

        medicareApplication = (MedicareApplication)getApplication();
        progress = medicareApplication.createProgressDialog(this, "Signing you in...");
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = medicareApplication.getFirebaseAuth().getCurrentUser();
        if(currentUser != null){
            showProgressBar();
            gotoHomeActivity();
        }
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @OnClick(R.id.bt_sign_in)
    void signIn(){
        if(!isValidEmail(et_email.getText().toString())){
            medicareApplication.createToast(this, "Invalid email address!");
        }
        else if(!isValidPassword(et_password.getText().toString())){
            medicareApplication.createToast(this, "Password is empty!");
        }
        else{
            showProgressBar();
            medicareApplication.getFirebaseAuth().signInWithEmailAndPassword(et_email.getText().toString(), et_password.getText().toString())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(medicareApplication.LOGTAG, "signInWithEmail:success");
                                fetchAccountType();
                            } else {
                                // If sign in fails, display a message to the user.
                                dismissProgressBar();
                                Log.w(medicareApplication.LOGTAG, "signInWithEmail:failure", task.getException());
                                medicareApplication.createToast(SignInActivity.this, task.getException().getLocalizedMessage());
                            }
                        }
                    });
        }
    }

    @OnClick(R.id.bt_sign_up_page)
    void signUp() {
        Intent signUpIntent = new Intent(this, SignUpActivity.class);
        startActivity(signUpIntent);
    }


    private void fetchAccountType() {
        medicareApplication.fetchUserType()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.e(medicareApplication.LOGTAG, "fetchAccountType", task.getException());
                            dismissProgressBar();
                            medicareApplication.createToast(SignInActivity.this, "Something went wrong!");
                        }
                        else {
                            Log.i(medicareApplication.LOGTAG, "fetchAccountType, Accounttype fetched to sign in.");
                            String type = task.getResult();
                            saveAccountType(AccountType.fromString(type));
                            gotoHomeActivity();
                        }
                    }
                });
    }

    private void saveAccountType(AccountType accountType) {
        medicareApplication.setAccountType(this, accountType);
    }

    private void gotoHomeActivity() {
        Intent homeIntent;
        if(medicareApplication.getAccountType(this) == AccountType.PATIENT){
            homeIntent = new Intent(this, PatientHomeActivity.class);
            dismissProgressBar();
            startActivity(homeIntent);
        }
        else if(medicareApplication.getAccountType(this) == AccountType.PHARMACIST) {
            homeIntent = new Intent(this, PharmacistHomeActivity.class);
            dismissProgressBar();
            startActivity(homeIntent);
        }
        else{
            dismissProgressBar();
            medicareApplication.createToast(this, "AccountType is neither MedicarePatient or Pharmacist!");
        }
    }

    private boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }

    private boolean isValidPassword(CharSequence text1) {
        return !TextUtils.isEmpty(text1);
    }

    private void showProgressBar(){
        progress.show();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

    }

    private void dismissProgressBar(){
        progress.dismiss();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }
}
