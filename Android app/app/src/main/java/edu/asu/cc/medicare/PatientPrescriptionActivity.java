package edu.asu.cc.medicare;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.CalendarContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cz.msebera.android.httpclient.Header;
import io.github.pierry.progress.Progress;

public class PatientPrescriptionActivity extends AppCompatActivity {

    @BindView(R.id.rv_prescriptions)
    RecyclerView recyclerView;

    PillRecyclerViewAdapter adapter;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_TAKE_PHOTO = 1;
    MedicareApplication medicareApplication;
    Progress progress;
    String currentPhotoPath;
    List<PillPrescription> pills;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_prescription);
        ButterKnife.bind(this);

        medicareApplication = (MedicareApplication)getApplication();
        pills = new ArrayList<>();
        progress = medicareApplication.createProgressDialog(this, "Getting prescription...");

        fetchPills();
    }

    @OnClick(R.id.fab_goto_camera)
    void gotoCamera(){
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            getPillInformation();
        }
    }

    void getPillInformation() {
        progress = medicareApplication.createProgressDialog(this, "Verifying pill...");
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
                verifyPill(new String(responseBody));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                dismissProgressBar();
                medicareApplication.createToast(PatientPrescriptionActivity.this, "Failed to upload image!");
                Log.e(medicareApplication.LOGTAG,"identifyPill", error );
            }
        });
    }

    void verifyPill(String response) {
        JSONObject pillData = null;
        try{
            pillData = new JSONObject(response);

            int pillPosition = findPillInList(pillData.getString("NDC11Code"));
            dismissProgressBar();
            if(pillPosition != -1){
                animateView(recyclerView.findViewHolderForAdapterPosition(pillPosition).itemView);
                medicareApplication.createToast(this, "The pill "+pillData.getString("ProprietaryName")+" is in your prescription.");
            }
            else{
                medicareApplication.createToast(this, "The pill "+pillData.getString("ProprietaryName")+" in not your in your prescription.");
            }
        }
        catch (JSONException e){
            dismissProgressBar();
            Log.e(medicareApplication.LOGTAG, "displayIdentifiedPillInformation", e);
            medicareApplication.createToast(this, "Could not parse pill information.");
        }
    }

    void animateView(View view) {
        ObjectAnimator anim = ObjectAnimator.ofInt(view, "backgroundColor", R.color.colorBackground, Color.WHITE, R.color.colorBackground);
        anim.setDuration(1500);
        anim.setEvaluator(new ArgbEvaluator());
        anim.setRepeatMode(ValueAnimator.REVERSE);
        anim.setRepeatCount(1);
        anim.start();
    }

    int findPillInList(String pillNdc11Code) {
        for(int i=0; i< pills.size(); i++) {
            if(pills.get(i).ndc11Code.compareTo(pillNdc11Code) == 0){
                return i;
            }
        }
        return -1;
    }


    void fetchPills() {
        showProgressBar();

        medicareApplication.fetchPatientPills()
                .addOnCompleteListener(new OnCompleteListener<List<Map<String,String>>>() {
                    @Override
                    public void onComplete(@NonNull Task<List<Map<String,String>>> task) {
                        if(task.isSuccessful()) {
                            List<Map<String,String>> result = task.getResult();

                            Gson gson = new Gson();
                            for (Map<String, String> data : result) {
                                JsonElement jsonElement = gson.toJsonTree(data);
                                pills.add(gson.fromJson(jsonElement, PillPrescription.class));
                            }

                            setRecyclerView();
                        }
                        else {
                            dismissProgressBar();
                            Log.e(medicareApplication.LOGTAG, "fetchPills", task.getException());
                            medicareApplication.createToast(PatientPrescriptionActivity.this, "Failed to get prescription." );
                        }
                    }
                });
    }

    void setRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PillRecyclerViewAdapter(this, pills, new PillRecyclerViewAdapter.PillAdapterListener() {
            @Override
            public void remindOnClick(View v, int position) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd-yyyy");
                Date date;
                try{
                    date = simpleDateFormat.parse(pills.get(position).refillDate);
                }
                catch (ParseException e){
                    Log.e(medicareApplication.LOGTAG, "remindOnClick", e);
                    medicareApplication.createToast(PatientPrescriptionActivity.this, "Date is invalid.");
                    return;
                }
                Intent intent = new Intent(Intent.ACTION_EDIT);
                intent.setType("vnd.android.cursor.item/event");
                intent.putExtra(CalendarContract.Events.TITLE, "Get a refill for pill "+ pills.get(position).proprietaryName);
                intent.putExtra(CalendarContract.Events.ALL_DAY, true);
                intent.putExtra(CalendarContract.Events.HAS_ALARM, 1);
                intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, date.getTime());
                startActivity(intent);
            }
        });
        recyclerView.setAdapter(adapter);

        dismissProgressBar();
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
