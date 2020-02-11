package edu.asu.cc.medicare;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PharmacistHomeActivity extends AppCompatActivity {

    MedicareApplication medicareApplication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pharmacist_home);
        ButterKnife.bind(this);

        medicareApplication = (MedicareApplication)getApplication();
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @OnClick(R.id.bt_pharmacist_sign_out)
    void signOut(){
        medicareApplication.getFirebaseAuth().signOut();
        startActivity(new Intent(this, SignInActivity.class));
    }
}
