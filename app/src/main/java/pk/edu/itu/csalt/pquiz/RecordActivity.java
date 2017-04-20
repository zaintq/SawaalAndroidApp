package pk.edu.itu.csalt.pquiz;

/**
 * Created by Zain on 4/24/17.
 */
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Environment;
import android.widget.Button;
import android.media.MediaPlayer;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

public class RecordActivity extends AppCompatActivity {

    private ViewSwitcher switcher;

    private static MediaRecorder mediaRecorder;
    private static MediaPlayer mediaPlayer;

    private static String audioFilePath;
    private static ImageButton stopButton;
    private static ImageButton playButton;
    private static ImageButton recordButton;
    private static ImageButton saveButton;

    private Integer optnum = 0;
    private Integer uopt   = null;

    String urlString, saveAPIURL;

    String rec_type, rec_id, fname, uid, qid;

    private ProgressDialog dialog = null;

    private boolean isRecording = false;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onStart() {
        super.onStart();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();

        setContentView(R.layout.activity_sound_recording);

        Bundle args = getIntent().getExtras();
        uid         = args.getString("uid");
        qid         = args.getString("qid");
        rec_type    = args.getString("type");
        rec_id      = args.getString("rec_id");
        fname       = rec_id + ".wav";

        Log.w("~~ Record Activity", "Rec Type: " + rec_type);
        Log.w("~~ Record Activity", "Rec ID: " + rec_id);

        urlString     = getString(R.string.pq_base) + getString(R.string.pq_scripts);
        saveAPIURL = getString(R.string.pq_base) + getString(R.string.pq_scripts) + "save_recording.php?type="+rec_type+"&uid="+uid+"&call_id=00&pquiz=true";

        switch (rec_type){
            case "uc":{

                urlString += "app_upload_comment.php";
                saveAPIURL += ("&cid="+rec_id+"&qid="+qid+"");
                break;
            }
            case "uq":{
                urlString += "app_upload_rec.php";
                saveAPIURL += ("&record_id="+rec_id+"");
                break;
            }
            case "qo":{
                urlString += "app_upload_rec.php";
                saveAPIURL += ("&record_id="+rec_id);
                break;
            }
            case "un":{
                urlString += "app_upload_rec.php";
                break;
            }
            case "fb":{
                urlString += "app_upload_fb.php";
                break;
            }
            default:{
                Log.w("~~ Record Activity", "Invalid Rec Type: " + rec_type);
                return;
            }
        }

        Log.w("~~ Record Activity", "URL:" + urlString);

        recordButton = (ImageButton) findViewById(R.id.btnStart);
        playButton = (ImageButton) findViewById(R.id.btnPlay);
        stopButton = (ImageButton) findViewById(R.id.btnStop);
        saveButton = (ImageButton) findViewById(R.id.btnSave);

        setImageButtonEnabled(false, saveButton, R.drawable.rec_save);

        if (!hasMicrophone()) {
            setImageButtonEnabled(false, stopButton, R.drawable.rec_stop);
            setImageButtonEnabled(false, playButton, R.drawable.rec_play);
            setImageButtonEnabled(false, recordButton, R.drawable.rec_mic);
        } else {
            setImageButtonEnabled(false, stopButton, R.drawable.rec_stop);
            setImageButtonEnabled(false, playButton, R.drawable.rec_play);
        }

        audioFilePath =
                Environment.getExternalStorageDirectory().getAbsolutePath() + "/"+ fname;// + "/pq_audio.3gp";
        Log.w("~~ Record Activity Path", audioFilePath);


        setButtonsOnClick();

    }

    public void setImageButtonEnabled(boolean enabled, ImageButton item, int iconResId) {

        if (item == null){
            Log.w("~~setImageButtonEnabled", "Item is NULL!");
            return;
        }

        Log.w("~~setImageButtonEnabled", "Enabled: " + enabled);

        Context ctxt = getApplicationContext();

        item.setEnabled(enabled);
        Drawable originalIcon = ctxt.getResources().getDrawable(iconResId);
        Drawable icon = enabled ? originalIcon : convertDrawableToGrayScale(originalIcon);
        item.setImageDrawable(icon);
    }

    public static Drawable convertDrawableToGrayScale(Drawable drawable) {
        if (drawable == null)
            return null;

        Drawable res = drawable.mutate();
        res.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
        return res;
    }

    protected void setButtonsOnClick(){

        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.w("~~ Record Activity", "Clicked rec_start");
                try {
                    recordAudio();
                } catch (IOException e) {
                    Log.w("~~ Record Activity", "Exception Thrown While recording!");
                    e.printStackTrace();
                }
            }
        });

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.w("~~ Record Activity", "Clicked rec_play");
                try {
                    playAudio(audioFilePath);
                } catch (IOException e) {
                    Log.w("~~ Record Activity", "Thrown exception while playing");
                    e.printStackTrace();
                }
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.w("~~ Record Activity", "Clicked rec_stop");

                stopClicked();

            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.w("~~ Record Activity", "Clicked rec_save");
                new uploadFile().execute();
                //uploadFile(audioFilePath);
            }
        });
    }

    protected boolean hasMicrophone() {
        PackageManager pmanager = this.getPackageManager();
        return pmanager.hasSystemFeature(
                PackageManager.FEATURE_MICROPHONE);
    }

    public void recordAudio() throws IOException {

        isRecording = true;

        setImageButtonEnabled(true, stopButton, R.drawable.rec_stop);
        setImageButtonEnabled(false, playButton, R.drawable.rec_play);
        setImageButtonEnabled(false, recordButton, R.drawable.rec_mic);

        try {
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setOutputFile(audioFilePath);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mediaRecorder.prepare();
            mediaRecorder.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopClicked() {

        setImageButtonEnabled(false, stopButton, R.drawable.rec_stop);
        setImageButtonEnabled(true, playButton, R.drawable.rec_play);

        if (isRecording) {
            setImageButtonEnabled(false, recordButton, R.drawable.rec_mic);
            mediaRecorder.stop();
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;
            isRecording = false;
            setImageButtonEnabled(true, saveButton, R.drawable.rec_save);
            setImageButtonEnabled(true, recordButton, R.drawable.rec_mic);
        } else {
            mediaPlayer.release();
            mediaPlayer = null;
            setImageButtonEnabled(true, recordButton, R.drawable.rec_mic);
        }
    }

    public void playAudio(String fpath) throws IOException {

        setImageButtonEnabled(false, playButton, R.drawable.rec_play);
        setImageButtonEnabled(false, recordButton, R.drawable.rec_mic);
        setImageButtonEnabled(true, stopButton, R.drawable.rec_stop);

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            public void onCompletion(MediaPlayer mp) {

                setImageButtonEnabled(true, playButton, R.drawable.rec_play);
                setImageButtonEnabled(true, recordButton, R.drawable.rec_mic);
                setImageButtonEnabled(false, stopButton, R.drawable.rec_stop);

            }
        });
        mediaPlayer.setDataSource(fpath);
        mediaPlayer.prepare();
        mediaPlayer.start();

        setImageButtonEnabled(true, playButton, R.drawable.rec_play);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Record Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://pk.edu.itu.csalt.pquiz/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    protected void showResponse(int level){

        ImageButton error   = (ImageButton) findViewById(R.id.rec_error);
        ImageButton success = (ImageButton) findViewById(R.id.rec_success);
        ImageButton dummy = (ImageButton) findViewById(R.id.dummy);

        AVLoadingIndicatorView avi = (AVLoadingIndicatorView) findViewById(R.id.avi);

        switch (level){
            case 0:{
                dummy.setVisibility(View.VISIBLE);
                error.setVisibility(View.GONE);
                success.setVisibility(View.GONE);
                avi.setVisibility(View.GONE);
                break;
            }
            case 1:{
                dummy.setVisibility(View.GONE);
                error.setVisibility(View.GONE);
                success.setVisibility(View.VISIBLE);
                avi.setVisibility(View.GONE);
                break;
            }
            case 2:{
                dummy.setVisibility(View.GONE);
                error.setVisibility(View.VISIBLE);
                success.setVisibility(View.GONE);
                avi.setVisibility(View.GONE);
                break;
            }
            case 3:{
                dummy.setVisibility(View.GONE);
                error.setVisibility(View.GONE);
                success.setVisibility(View.GONE);
                avi.setVisibility(View.VISIBLE);
                break;
            }
        }

    }


    class uploadFile extends AsyncTask<Void, Void, Boolean>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showResponse(3);
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            return doFileUpload1(audioFilePath);
        }

        @Override
        protected void onPostExecute(Boolean result) {

            super.onPostExecute(result);

            showResponse(0);

            if (result){
                new API_SAVE_RECORDING().execute();
            }
            else {
                showResponse(2);
                Log.e("Debug uploadFile", "onPostExecute: Upload Failed!");
            }
        }

        public boolean doFileUpload1(final String selectedFilePath){

            int serverResponseCode = 0;

            HttpURLConnection connection;
            DataOutputStream dataOutputStream;
            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "*****";


            int bytesRead,bytesAvailable,bufferSize;
            byte[] buffer;
            int maxBufferSize = 1 * 1024 * 1024;
            File selectedFile = new File(selectedFilePath);


            String[] parts = selectedFilePath.split("/");
            final String fileName = parts[parts.length-1];

            if (!selectedFile.isFile()){

                Log.e("^^ Upload: No File!!", audioFilePath);
                //dialog.dismiss();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //tvFileName.setText("Source File Doesn't Exist: " + selectedFilePath);
                    }
                });
                //return 0;
                return false;
            }else{
                Log.e("^^ Upload: File Found!!", audioFilePath);
                try{
                    FileInputStream fileInputStream = new FileInputStream(selectedFile);
                    URL url = new URL(urlString);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);//Allow Inputs
                    connection.setDoOutput(true);//Allow Outputs
                    connection.setUseCaches(false);//Don't use a cached Copy
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Connection", "Keep-Alive");
                    connection.setRequestProperty("ENCTYPE", "multipart/form-data");
                    connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                    connection.setRequestProperty("uploaded_file",selectedFilePath);

                    //creating new dataoutputstream
                    dataOutputStream = new DataOutputStream(connection.getOutputStream());

                    //writing bytes to data outputstream
                    dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
                    dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                            + selectedFilePath + "\"" + lineEnd);

                    dataOutputStream.writeBytes(lineEnd);

                    //returns no. of bytes present in fileInputStream
                    bytesAvailable = fileInputStream.available();
                    //selecting the buffer size as minimum of available bytes or 1 MB
                    bufferSize = Math.min(bytesAvailable,maxBufferSize);
                    //setting the buffer as byte array of size of bufferSize
                    buffer = new byte[bufferSize];

                    //reads bytes from FileInputStream(from 0th index of buffer to buffersize)
                    bytesRead = fileInputStream.read(buffer,0,bufferSize);

                    //loop repeats till bytesRead = -1, i.e., no bytes are left to read
                    while (bytesRead > 0){
                        //write the bytes read from inputstream
                        dataOutputStream.write(buffer,0,bufferSize);
                        bytesAvailable = fileInputStream.available();
                        bufferSize = Math.min(bytesAvailable,maxBufferSize);
                        bytesRead = fileInputStream.read(buffer,0,bufferSize);
                    }

                    dataOutputStream.writeBytes(lineEnd);
                    dataOutputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                    serverResponseCode = connection.getResponseCode();
                    String serverResponseMessage = connection.getResponseMessage();

                    Log.i("^^ File Upload", "Server Response is: " + serverResponseMessage + ": " + serverResponseCode);

                    //response code of 200 indicates the server status OK
                    if(serverResponseCode == 200){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.e("^^ Upload", "Uploading finished. Returnging. ");
                                //tvFileName.setText("File Upload completed.\n\n You can see the uploaded file here: \n\n" + "http://coderefer.com/extras/uploads/"+ fileName);
                            }
                        });
                    }

                    InputStream in = connection.getInputStream();

                    byte data[] = new byte[1024];
                    int counter = -1;
                    String jsonString = "";
                    while( (counter = in.read(data)) != -1){
                        jsonString += new String(data, 0, counter);
                    }

                    Log.d("==> Record Debug", " JSON String: " + jsonString);

                    //closing the input and output streams
                    fileInputStream.close();
                    dataOutputStream.flush();
                    dataOutputStream.close();

                    if (jsonString.equals("success"))
                        return true;

                } catch (FileNotFoundException e) {
                    Log.e("^^ Upload", "File not found: " + audioFilePath);
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(RecordActivity.this,"File Not Found", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (MalformedURLException e) {
                    Log.e("^^ Upload", "URL Error: " + urlString);
                    e.printStackTrace();
                    Toast.makeText(RecordActivity.this, "URL error!", Toast.LENGTH_SHORT).show();

                } catch (IOException e) {
                    Log.e("^^ Upload", "IO Error!");
                    e.printStackTrace();
                    Toast.makeText(RecordActivity.this, "Cannot Read/Write File!", Toast.LENGTH_SHORT).show();
                }
                //dialog.dismiss();
                Log.e("^^ Upload", "Uploading finished. Returnging. ");
                //return serverResponseCode;
                return false;
            }
        }
    }

    public class API_SAVE_RECORDING extends AsyncTask<Boolean, Void, Boolean> {

        String TAG = "API_SAVE_RECORDING";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.e(TAG, "On Pre Execute!");
            showResponse(3);
        }

        protected Boolean doInBackground(Boolean... arg0) {

            Log.e(TAG, this.getClass().getSimpleName()+" -- API Url: " + saveAPIURL);

            HttpHandler sh = new HttpHandler();

            String api_response = sh.makeServiceCall(saveAPIURL);

            Log.e(TAG, this.getClass().getSimpleName()+" -- API Response: " + api_response);

            if (api_response != null) {
                try {
                    JSONObject jsonObj = new JSONObject(api_response);

                    final JSONObject result = jsonObj.getJSONObject("result");

                    Boolean error = result.getBoolean("error");

                    if (error){
                        Log.e(TAG, this.getClass().getSimpleName()+" -- API Error: True");
                        return false;
                    }else{
                        Log.e(TAG, this.getClass().getSimpleName()+" -- API Error: False");
                        return true;
                    }
                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Json parsing error: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }
            } else {
                Log.e(TAG, "Couldn't get json from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Couldn't get json from server. Check LogCat for possible errors!",
                                Toast.LENGTH_LONG).show();
                    }
                });
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {

            super.onPostExecute(result);

            if (result){
                showResponse(1);
                setImageButtonEnabled(false, playButton, R.drawable.rec_play);
                setImageButtonEnabled(false, recordButton, R.drawable.rec_mic);
                setImageButtonEnabled(false, stopButton, R.drawable.rec_stop);
                setImageButtonEnabled(false, saveButton, R.drawable.rec_save);
            }else{
                showResponse(2);
                Log.e(TAG, "Got FALSE in onPostExecute in API_SAVE_RECORDING.");
            }
        }
    }
}