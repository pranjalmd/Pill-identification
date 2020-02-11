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
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.functions.FirebaseFunctionsException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.github.pierry.progress.Progress;

public class SignUpActivity extends AppCompatActivity {

    @BindView(R.id.et_fname_sign_up)
    EditText et_fname;

    @BindView(R.id.et_lname_sign_up)
    EditText et_lname;

    @BindView(R.id.et_email_sign_up)
    EditText et_email;

    @BindView(R.id.et_password_sign_up)
    EditText et_password;

    @BindView(R.id.et_confirm_password_sign_up)
    EditText et_confirm_password;

    @BindView(R.id.rg_user_type)
    RadioGroup rg_user_type;

    MedicareApplication medicareApplication;
    MedicareUser medicareUser;
    Progress progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        ButterKnife.bind(this);

        medicareApplication = (MedicareApplication)getApplication();
        progress = medicareApplication.createProgressDialog(this, "Creating new account...");
    }

    @OnClick(R.id.bt_sign_up)
    void signUp() {
        if(TextUtils.isEmpty(et_fname.getText().toString())) {
            medicareApplication.createToast(this, "First name is empty!");
        }
        else if(TextUtils.isEmpty(et_lname.getText().toString())){
            medicareApplication.createToast(this, "Last name is empty!");
        }
        else if(!isValidEmail(et_email.getText().toString())){
            medicareApplication.createToast(this, "Invalid email address!");
        }
        else if(!isValidPassword(et_password.getText().toString(), et_confirm_password.getText().toString())){
            medicareApplication.createToast(this, "Password is either empty or doesn't match!");
        }
        else if(rg_user_type.getCheckedRadioButtonId() == -1){
            medicareApplication.createToast(this, "User type is not selected!");
        }
        else{
            showProgressBar();
            if(rg_user_type.getCheckedRadioButtonId() == R.id.rb_patient){
                medicareUser = new MedicarePatient(et_fname.getText().toString(), et_lname.getText().toString(), et_email.getText().toString());
            }
            else{
                medicareUser = new MedicarePharmacist(et_fname.getText().toString(), et_lname.getText().toString(), et_email.getText().toString());
            }

            medicareApplication.getFirebaseAuth().createUserWithEmailAndPassword(et_email.getText().toString(), et_password.getText().toString())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(medicareApplication.LOGTAG, "createUserWithEmail:success");
                                setAccountInitialData();
                            } else {
                                // If sign in fails, display a message to the user.
                                dismissProgressBar();
                                Log.w(medicareApplication.LOGTAG, "createUserWithEmail:failure", task.getException());
                                medicareApplication.createToast(SignUpActivity.this, task.getException().getLocalizedMessage());
                            }
                        }
                    });
        }
    }

    private void setAccountInitialData(){
        medicareApplication.uploadNewUserData(medicareUser)
                .addOnCompleteListener(new OnCompleteListener<Boolean>() {
                    @Override
                    public void onComplete(@NonNull Task<Boolean> task) {
                        if (!task.isSuccessful()) {
                            Log.e(medicareApplication.LOGTAG, "setAccountInitialData", task.getException());
                        }
                        else {
                            Log.i(medicareApplication.LOGTAG, "setAccountInitialData, Account data successfully stored in db.");
                            saveAccountType();
                            gotoHomeActivity();
                        }
                    }
                });
    }

    private void saveAccountType() {
        medicareApplication.setAccountType(this, this.medicareUser.type);
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

    private boolean isValidPassword(CharSequence text1, CharSequence text2) {

        return (!TextUtils.isEmpty(text1) && et_password.getText().toString().compareTo(et_confirm_password.getText().toString()) == 0);
    }

    private void showProgressBar(){
        progress.show();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private void dismissProgressBar(){
        progress.dismiss();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }
}
