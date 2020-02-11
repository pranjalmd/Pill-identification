package edu.asu.cc.medicare;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.List;
import java.util.Map;
import java.util.Random;

import io.github.pierry.progress.Progress;

public class MedicareApplication  extends Application {
    private FirebaseAuth mFirebaseAuth;
    private FirebaseFunctions mFirebaseFunctions;
    String LOGTAG;
    String PACKAGE_NAME;
    String ACCOUNT_TYPE_KEY;


    @Override
    public void onCreate() {
        super.onCreate();
        LOGTAG = getString(R.string.application_name) + "Log";
        ACCOUNT_TYPE_KEY = getString(R.string.account_type_key);
        PACKAGE_NAME = getPackageName();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseFunctions = FirebaseFunctions.getInstance();
    }

    public FirebaseAuth getFirebaseAuth() {
        return this.mFirebaseAuth;
    }

    public AccountType getAccountType(Context context){
        SharedPreferences mSharedPreferences = context.getSharedPreferences(PACKAGE_NAME, Context.MODE_PRIVATE);
        int accountTypeValue = mSharedPreferences.getInt(ACCOUNT_TYPE_KEY, -1);
        if(accountTypeValue == -1){
            return null;
        }
        else{
            return AccountType.values()[accountTypeValue];
        }
    }

    public void setAccountType(Context context, AccountType accountType){
        SharedPreferences mSharedPreferences = context.getSharedPreferences(PACKAGE_NAME, Context.MODE_PRIVATE);
        mSharedPreferences.edit().putInt(ACCOUNT_TYPE_KEY, accountType.getIntValue()).apply();
    }

    public void createToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    public Progress createProgressDialog(Context context, String message) {
        return new Progress(context)
                .setBackgroundColor(getColor(R.color.colorAccent))
                .setMessage(message)
                .setMessageColor(getColor(R.color.pp_white))
                .setProgressColor(getColor(R.color.pp_white));
    }


    public Task<Boolean> uploadNewUserData(MedicareUser user) {
        Map<String, String> data = user.getMap();
        data.put("age", String.valueOf(new Random().nextInt(70) +10));
        return mFirebaseFunctions
                .getHttpsCallable("uploadNewUserData")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, Boolean>() {
                    @Override
                    public Boolean then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        boolean result = (boolean) task.getResult().getData();
                        return result;
                    }
                });
    }

    public Task<String> fetchUserType() {
        return mFirebaseFunctions.getHttpsCallable("getUserType")
                .call()
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        String result = (String) task.getResult().getData();
                        return result;
                    }
                });

    }

    public Task<Map<String, String>> fetchPatientData() {
        return mFirebaseFunctions.getHttpsCallable("getPatientData")
                .call()
                .continueWith(new Continuation<HttpsCallableResult, Map<String, String>>() {
                    @Override
                    public Map<String, String> then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        Map<String, String>  result = (Map) task.getResult().getData();
                        return result;
                    }
                });
    }

    public Task<List<Map<String,String>>> fetchPatientPills() {
        return mFirebaseFunctions.getHttpsCallable("getPatientPills")
                .call()
                .continueWith(new Continuation<HttpsCallableResult, List<Map<String,String>>>() {
                    @Override
                    public List<Map<String,String>> then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        List<Map<String,String>> result = (List) task.getResult().getData();
                        return result;
                    }
                });
    }
}