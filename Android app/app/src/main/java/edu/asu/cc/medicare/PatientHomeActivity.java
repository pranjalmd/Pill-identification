package edu.asu.cc.medicare;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.database.CursorIndexOutOfBoundsException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.loopj.android.http.*;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.github.pierry.progress.Progress;

public class PatientHomeActivity extends AppCompatActivity {

    @BindView(R.id.tv_patient_welcome)
    TextView tv_patient_welcome;

    @BindView(R.id.tv_patient_name)
    TextView tv_patient_name;

    @BindView(R.id.tv_patient_email)
    TextView tv_patient_email;

    @BindView(R.id.tv_patient_age)
    TextView tv_patient_age;

    @BindView(R.id.iv_pill_image)
    ImageView iv_pill_view;

    @BindView(R.id.ll_pill_details_section)
    LinearLayout ll_pill_details_section;

    @BindView(R.id.tv_pill_proprietary_name)
    TextView tv_pill_proprietary_name;

    @BindView(R.id.tv_pill_non_proprietary_name)
    TextView tv_pill_non_proprietary_name;

    @BindView(R.id.tv_pill_ndc11_code)
    TextView tv_pill_ndc11_code;

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_TAKE_PHOTO = 1;
    MedicareApplication medicareApplication;
    Progress progress;
    String currentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_home);

        ButterKnife.bind(this);
        medicareApplication = (MedicareApplication)getApplication();
        progress = medicareApplication.createProgressDialog(this, "Loading...");
        fetchPatientData();
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @OnClick(R.id.bt_goto_camera)
    void gotoCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try{
                photoFile = createImageFile();
            }
            catch (IOException e){
                Log.e(medicareApplication.LOGTAG, "gotoCamera", e);
            }
            if(photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        medicareApplication.PACKAGE_NAME,
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    @OnClick(R.id.bt_reset_identify_pill)
    void resetIdentifiedPill() {
        iv_pill_view.setImageDrawable(null);
        ll_pill_details_section.setVisibility(View.GONE);
    }

    @OnClick(R.id.bt_patient_prescription)
    void gotoPrescription() {
        startActivity(new Intent(this, PatientPrescriptionActivity.class));
    }

    @OnClick(R.id.bt_patient_sign_out)
    void signOut(){
        medicareApplication.getFirebaseAuth().signOut();
        startActivity(new Intent(this, SignInActivity.class));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            identifyPill();
        }
    }

    void fetchPatientData() {
        showProgressBar();
        medicareApplication.fetchPatientData()
                .addOnCompleteListener(new OnCompleteListener<Map<String, String>>() {
                    @Override
                    public void onComplete(@NonNull Task<Map<String, String>> task) {
                        if(task.isSuccessful()){
                            Map<String, String> data = task.getResult();
                            Log.i(medicareApplication.LOGTAG, "Fetched Patient data!");
                            medicareApplication.createToast(PatientHomeActivity.this, "Welcome!");

                            Gson gson = new Gson();
                            JsonElement jsonElement = gson.toJsonTree(data);
                            MedicarePatient patient = gson.fromJson(jsonElement, MedicarePatient.class);
                            displayPatientData(patient);
                        }
                        else{
                            dismissProgressBar();
                            Log.e(medicareApplication.LOGTAG, "fetchPatientData", task.getException());
                            medicareApplication.createToast(PatientHomeActivity.this, "Error getting patient data.");
                        }
                    }
                });
    }

    void displayPatientData(MedicarePatient patient) {
        tv_patient_welcome.setText("Welcome "+ patient.firstName);
        tv_patient_name.setText(patient.firstName +" "+patient.lastName);
        tv_patient_email.setText(patient.emailAddress);
        tv_patient_age.setText(patient.age);
        dismissProgressBar();
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    void identifyPill() {
        progress = medicareApplication.createProgressDialog(this, "Detecting pill...");
        showProgressBar();

        AsyncHttpClient client = new AsyncHttpClient();
        //Append the parameters in the Service URL
        RequestParams rp = new RequestParams();
        String SERVICE_URL_REG = getString(R.string.gcm_server_address);

        try{
            rp.put("image",new File(currentPhotoPath));
        }
        catch (Exception e){}

        client.post(SERVICE_URL_REG, rp, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.d(medicareApplication.LOGTAG, new String(responseBody));
                displayIdentifiedPillInformation(new String(responseBody));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                dismissProgressBar();
                medicareApplication.createToast(PatientHomeActivity.this, "Failed to upload image!");
                Log.e(medicareApplication.LOGTAG,"identifyPill", error );
            }
        });

    }

    void displayIdentifiedPillInformation(String response) {

        Bitmap bitmapFactory =  BitmapFactory.decodeFile(currentPhotoPath);
        iv_pill_view.setImageBitmap(bitmapFactory);
        JSONObject pillData;
        try{
            pillData = new JSONObject(response);
            tv_pill_ndc11_code.setText(pillData.getString("NDC11Code"));
            tv_pill_proprietary_name.setText(pillData.getString("ProprietaryName"));
            tv_pill_non_proprietary_name.setText(pillData.getString("NonProprietaryName"));
            ll_pill_details_section.setVisibility(View.VISIBLE);
        }
        catch (JSONException e){
            Log.e(medicareApplication.LOGTAG, "displayIdentifiedPillInformation", e);
            medicareApplication.createToast(this, "Could not parse pill information.");
         }
        dismissProgressBar();
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
