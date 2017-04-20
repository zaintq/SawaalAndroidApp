package pk.edu.itu.csalt.pquiz;

/**
 * Created by Zain on 4/24/17.
 */
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
import java.util.HashMap;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Environment;
import android.widget.Button;
import android.media.MediaPlayer;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONException;
import org.json.JSONObject;

public class RecordNewQuestion extends AppCompatActivity {

    private static MediaRecorder mediaRecorder;
    private static MediaPlayer mediaPlayer;

    HashMap<String, ImageButton> question;
    ArrayList<HashMap<String, ImageButton>> options;

    static Button finalSubmit;
    String urlString, uid, qid, basePath, saveAPIBase;

    private boolean isRecording      = false;
    private Integer optnum = 0, uopt = null;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    protected void onStart() {

        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.

        client.connect();

        setContentView(R.layout.record_new_question);

        setupRadioButtonsMX();

        Bundle args = getIntent().getExtras();
        uid         = args.getString("uid");
        qid         = args.getString("qid");

        urlString   = getString(R.string.pq_base) + getString(R.string.pq_scripts) + "app_upload_rec.php";
        saveAPIBase = getString(R.string.pq_base) + getString(R.string.pq_scripts) + "save_recording.php?type=%s&uid="+uid+"&call_id=00&pquiz=true" + "&record_id=%s";
        basePath    = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";//+ fname;// + "/pq_audio.3gp";

        finalSubmit = (Button) findViewById(R.id.submit);

        question    = new HashMap<>();

        question.put("start", (ImageButton) findViewById(R.id.qrecstart));
        question.put("play", (ImageButton) findViewById(R.id.qrecplay));
        question.put("stop", (ImageButton) findViewById(R.id.qrecstop));
        question.put("pause", (ImageButton) findViewById(R.id.qrecpause));
        question.put("save", (ImageButton) findViewById(R.id.qrecsave));

        options = new ArrayList<>();

        for (int i = 1; i <= 4; i++){

            final int recstart = getResources().getIdentifier("opt"+i+"recstart", "id", getPackageName());
            final int recplay  = getResources().getIdentifier("opt"+i+"recplay", "id", getPackageName());
            final int recstop  = getResources().getIdentifier("opt"+i+"recstop", "id", getPackageName());
            final int recpause = getResources().getIdentifier("opt"+i+"recpause", "id", getPackageName());
            final int recsave  = getResources().getIdentifier("opt"+i+"recsave", "id", getPackageName());

            options.add(new HashMap<String, ImageButton>(){{
                put("start", (ImageButton) findViewById(recstart));
                put("play", (ImageButton) findViewById(recplay));
                put("stop", (ImageButton) findViewById(recstop));
                put("pause", (ImageButton) findViewById(recpause));
                put("save", (ImageButton) findViewById(recsave));
            }});
        }

        finalSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.w("~~ RecNewQ", "Clicked rec_save");
                if (uopt != null)
                    new API_SAVE_CORRECT_OPT().execute();
            }
        });

        setMainButtonsOnClick(question, qid);
        setMainButtonsOnClick(options.get(0), qid+"_1");
        setMainButtonsOnClick(options.get(1), qid+"_2");
        setMainButtonsOnClick(options.get(2), qid+"_3");
        setMainButtonsOnClick(options.get(3), qid+"_4");

        updateViewAccess();
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

    protected void setFinalSubmitEnabled(boolean enabled){

        if (enabled){
            finalSubmit.setEnabled(true);
            finalSubmit.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
        }else{
            finalSubmit.setEnabled(false);
            finalSubmit.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.opt_grey_dark));
        }
    }

    protected void setMainButtonsEnabled(boolean enabled, HashMap<String, ImageButton> hm){

        setImageButtonEnabled(enabled, hm.get("start"), R.drawable.rec_mic);
        setImageButtonEnabled(enabled, hm.get("stop"), R.drawable.rec_stop);
        setImageButtonEnabled(enabled, hm.get("play"), R.drawable.rec_play);
        setImageButtonEnabled(enabled, hm.get("pause"), R.drawable.rec_pause);
        setImageButtonEnabled(enabled, hm.get("save"), R.drawable.rec_save);
    }

    protected void setCOptClickable(boolean enabled){

        for (int i = 1; i <= 4; i++){
            RadioButton radioButton = (RadioButton) findViewById(getResources().getIdentifier("opt"+i, "id", getPackageName()));
            radioButton.setClickable(enabled);
        }
    }

    protected void setupRadioButtonsMX(){

        final ArrayList<RadioButton> buttons = new ArrayList<>();

        buttons.add((RadioButton) findViewById(R.id.opt1));
        buttons.add((RadioButton) findViewById(R.id.opt2));
        buttons.add((RadioButton) findViewById(R.id.opt3));
        buttons.add((RadioButton) findViewById(R.id.opt4));

        for (int i = 0; i < 4; i++){

            final int opt = i + 1;

            buttons.get(i).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    switch (opt){
                        case 1:{
                            buttons.get(0).setChecked(true);
                            buttons.get(1).setChecked(false);
                            buttons.get(2).setChecked(false);
                            buttons.get(3).setChecked(false);
                            uopt = 1;
                            break;
                        }
                        case 2:{
                            buttons.get(0).setChecked(false);
                            buttons.get(1).setChecked(true);
                            buttons.get(2).setChecked(false);
                            buttons.get(3).setChecked(false);
                            uopt = 2;
                            break;
                        }
                        case 3:{
                            buttons.get(0).setChecked(false);
                            buttons.get(1).setChecked(false);
                            buttons.get(2).setChecked(true);
                            buttons.get(3).setChecked(false);
                            uopt = 3;
                            break;
                        }
                        case 4:{
                            buttons.get(0).setChecked(false);
                            buttons.get(1).setChecked(false);
                            buttons.get(2).setChecked(false);
                            buttons.get(3).setChecked(true);
                            uopt = 4;
                            break;
                        }
                    }
                }
            });
        }

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

    protected void setMainButtonsOnClick(final HashMap<String, ImageButton> hm, final String rec_id){

        final String fpath = basePath + rec_id + ".wav";

        hm.get("start").setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.w("~~ RecNewQ", "Clicked rec_start");
                try {
                    recordAudio(hm, fpath);
                } catch (IOException e) {
                    Log.w("~~ RecNewQ", "Exception Thrown While recording!");
                    e.printStackTrace();
                }
            }
        });

        hm.get("play").setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.w("~~ RecNewQ", "Clicked rec_play");
                try {
                    playAudio(hm, fpath);
                } catch (IOException e) {
                    Log.w("~~ RecNewQ", "Thrown exception while playing");
                    e.printStackTrace();
                }
            }
        });

        hm.get("stop").setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.w("~~ RecNewQ", "Clicked rec_stop");
                stopClicked(hm);
            }
        });

        hm.get("save").setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.w("~~ RecNewQ", "Clicked rec_save");
                new API_UPLOAD_FILE().execute(rec_id);
            }
        });

        hm.get("pause").setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.w("~~ RecNewQ", "Clicked rec_pause");

            }
        });
    }

    protected boolean hasMicrophone() {
        PackageManager pmanager = this.getPackageManager();
        return pmanager.hasSystemFeature(
                PackageManager.FEATURE_MICROPHONE);
    }

    public void recordAudio(HashMap<String, ImageButton> hm, String audioFilePath) throws IOException {

        isRecording = true;

        setMainButtonsEnabled(false, hm);
        setImageButtonEnabled(true, hm.get("stop"), R.drawable.rec_stop);
        setImageButtonEnabled(false, hm.get("start"), R.drawable.rec_mic_active);

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
            isRecording = false;
        }
    }

    public void stopClicked(HashMap<String, ImageButton> hm) {

        setImageButtonEnabled(false, hm.get("stop"), R.drawable.rec_stop);
        setImageButtonEnabled(true, hm.get("play"), R.drawable.rec_play);

        if (isRecording) {
            setImageButtonEnabled(false, hm.get("start"), R.drawable.rec_mic);
            mediaRecorder.stop();
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;
            isRecording = false;
            setImageButtonEnabled(true, hm.get("save"), R.drawable.rec_save);
            setImageButtonEnabled(true, hm.get("start"), R.drawable.rec_mic);
        } else {
            mediaPlayer.release();
            mediaPlayer = null;
            setImageButtonEnabled(true, hm.get("start"), R.drawable.rec_mic);
        }
    }

    public void playAudio(final HashMap<String, ImageButton> hm, String fpath) throws IOException {

        setImageButtonEnabled(false, hm.get("play"), R.drawable.rec_play);
        setImageButtonEnabled(false, hm.get("start"), R.drawable.rec_mic);
        setImageButtonEnabled(true, hm.get("stop"), R.drawable.rec_stop);

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            public void onCompletion(MediaPlayer mp) {

                setImageButtonEnabled(true, hm.get("play"), R.drawable.rec_play);
                if (optnum >= 1 && optnum <=4)
                    setImageButtonEnabled(true, hm.get("start"), R.drawable.rec_mic);
                setImageButtonEnabled(false, hm.get("stop"), R.drawable.rec_stop);
            }
        });
        mediaPlayer.setDataSource(fpath);
        mediaPlayer.prepare();
        mediaPlayer.start();

        setImageButtonEnabled(true, hm.get("play"), R.drawable.rec_play);
    }
    
    private void updateViewAccess(){

        switch (optnum){
            case 0:{

                setMainButtonsEnabled(false, question);

                setMainButtonsEnabled(false, options.get(0));
                setMainButtonsEnabled(false, options.get(1));
                setMainButtonsEnabled(false, options.get(2));
                setMainButtonsEnabled(false, options.get(3));
                setFinalSubmitEnabled(false);

                setCOptClickable(false);

                if (hasMicrophone()) {
                    setImageButtonEnabled(true, question.get("start"), R.drawable.rec_mic);
                }

                break;
            }
            case 1:{

                setMainButtonsEnabled(false, question);

                setMainButtonsEnabled(false, options.get(0));
                setMainButtonsEnabled(false, options.get(1));
                setMainButtonsEnabled(false, options.get(2));
                setMainButtonsEnabled(false, options.get(3));
                setFinalSubmitEnabled(false);

                setCOptClickable(false);

                if (hasMicrophone()) {
                    setImageButtonEnabled(true, options.get(0).get("start"), R.drawable.rec_mic);
                }

                break;
            }
            case 2:{

                setMainButtonsEnabled(false, question);

                setMainButtonsEnabled(false, options.get(0));
                setMainButtonsEnabled(false, options.get(1));
                setMainButtonsEnabled(false, options.get(2));
                setMainButtonsEnabled(false, options.get(3));
                setFinalSubmitEnabled(false);

                setCOptClickable(false);

                if (hasMicrophone()) {
                    setImageButtonEnabled(true, options.get(1).get("start"), R.drawable.rec_mic);
                }

                break;
            }
            case 3:{

                setMainButtonsEnabled(false, question);

                setMainButtonsEnabled(false, options.get(0));
                setMainButtonsEnabled(false, options.get(1));
                setMainButtonsEnabled(false, options.get(2));
                setMainButtonsEnabled(false, options.get(3));
                setFinalSubmitEnabled(false);

                setCOptClickable(false);

                if (hasMicrophone()) {
                    setImageButtonEnabled(true, options.get(2).get("start"), R.drawable.rec_mic);
                }

                break;
            }
            case 4:{

                setMainButtonsEnabled(false, question);

                setMainButtonsEnabled(false, options.get(0));
                setMainButtonsEnabled(false, options.get(1));
                setMainButtonsEnabled(false, options.get(2));
                setMainButtonsEnabled(false, options.get(3));
                setFinalSubmitEnabled(false);

                setCOptClickable(false);

                if (hasMicrophone()) {
                    setImageButtonEnabled(true, options.get(3).get("start"), R.drawable.rec_mic);
                }

                break;
            }
            case 5:{

                setMainButtonsEnabled(false, question);

                setMainButtonsEnabled(false, options.get(0));
                setMainButtonsEnabled(false, options.get(1));
                setMainButtonsEnabled(false, options.get(2));
                setMainButtonsEnabled(false, options.get(3));

                setImageButtonEnabled(true, options.get(0).get("play"), R.drawable.rec_play);
                setImageButtonEnabled(true, options.get(1).get("play"), R.drawable.rec_play);
                setImageButtonEnabled(true, options.get(2).get("play"), R.drawable.rec_play);
                setImageButtonEnabled(true, options.get(3).get("play"), R.drawable.rec_play);

                setCOptClickable(true);
                setFinalSubmitEnabled(true);

                break;
            }
        }

    }

    protected void showResponse(int level){

        ImageButton error   = (ImageButton) findViewById(R.id.rec_error);
        ImageButton success = (ImageButton) findViewById(R.id.rec_success);

        AVLoadingIndicatorView avi = (AVLoadingIndicatorView) findViewById(R.id.avi);

        switch (level){
            case 0:{
                error.setVisibility(View.GONE);
                success.setVisibility(View.GONE);
                avi.setVisibility(View.GONE);
                break;
            }
            case 1:{
                error.setVisibility(View.GONE);
                success.setVisibility(View.VISIBLE);
                avi.setVisibility(View.GONE);
                break;
            }
            case 2:{
                error.setVisibility(View.VISIBLE);
                success.setVisibility(View.GONE);
                avi.setVisibility(View.GONE);
                break;
            }
            case 3:{
                error.setVisibility(View.GONE);
                success.setVisibility(View.GONE);
                avi.setVisibility(View.VISIBLE);
                break;
            }
        }

    }

    class API_UPLOAD_FILE extends AsyncTask<String, Void, Boolean>{

        String rec_id, fpath;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showResponse(3);
        }

        @Override
        protected Boolean doInBackground(String... params) {

            rec_id = params[0];
            fpath  = basePath + rec_id + ".wav";

            Log.e("^^ Upload rec_id: ", rec_id);
            Log.e("^^ Upload path: ", fpath);

            return doFileUpload(fpath);
        }

        @Override
        protected void onPostExecute(Boolean result) {

            super.onPostExecute(result);

            if (result){
                showResponse(0);
                new API_SAVE_RECORDING().execute(rec_id);
            }
            else {
                showResponse(2);
                Log.e("Debug API_UPLOAD_FILE", "onPostExecute: Upload Failed!");
            }
        }

        public boolean doFileUpload(final String selectedFilePath){

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

                Log.e("^^ Upload: No File!!", fpath);
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
                Log.e("^^ Upload: File Found!!", fpath);
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
                    Log.e("^^ Upload", "File not found: " + fpath);
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(RecordNewQuestion.this,"File Not Found", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (MalformedURLException e) {
                    Log.e("^^ Upload", "URL Error: " + urlString);
                    e.printStackTrace();
                    Toast.makeText(RecordNewQuestion.this, "URL error!", Toast.LENGTH_SHORT).show();

                } catch (IOException e) {
                    Log.e("^^ Upload", "IO Error!");
                    e.printStackTrace();
                    Toast.makeText(RecordNewQuestion.this, "Cannot Read/Write File!", Toast.LENGTH_SHORT).show();
                }
                //dialog.dismiss();
                Log.e("^^ Upload", "Uploading finished. Returnging. ");
                //return serverResponseCode;
                return false;
            }
        }
    }

    public class API_SAVE_CORRECT_OPT extends AsyncTask<Void, Void, Boolean> {

        String TAG = "API_SAVE_CORRECT_OPT";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.e(TAG, "On Pre Execute!");
            showResponse(3);
        }

        protected Boolean doInBackground(Void... args) {

            if (uopt < 1 || uopt > 4 ){
                Log.e(TAG, this.getClass().getSimpleName()+" -- C OPT Recieved: " + uopt);
                return null;
            }

            String api_url = getString(R.string.pq_base) + getString(R.string.pq_scripts) + "save_correct_answer.php?qid="+qid+"&uid="+uid+"&ans="+uopt+"&call_id=00";

            Log.e(TAG, this.getClass().getSimpleName()+" -- API Url: " + api_url);

            HttpHandler sh = new HttpHandler();

            String api_response = sh.makeServiceCall(api_url);

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
                setFinalSubmitEnabled(false);
                showResponse(1);
            }else {
                showResponse(2);
            }
        }
    }

    public class API_SAVE_RECORDING extends AsyncTask<String, Void, Boolean> {

        String TAG = "API_SAVE_RECORDING";
        String rec_id, saveAPIURL;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.e(TAG, "On Pre Execute!");
            showResponse(3);
        }

        protected Boolean doInBackground(String... arg0) {

            rec_id = arg0[0];

            if (optnum == 0)
                saveAPIURL  = String.format(saveAPIBase, "uq", rec_id);
            else
                saveAPIURL  = String.format(saveAPIBase, "qo", rec_id);

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

            showResponse(0);

            if (result){
                optnum++;
                if (optnum >= 1 && optnum <= 4)
                    saveAPIURL  = String.format(saveAPIBase, "qo", qid+"_"+optnum);
                updateViewAccess();

            }else{
                Log.e(TAG, "Got FALSE in onPostExecute in API_SAVE_RECORDING.");
            }
        }
    }
}