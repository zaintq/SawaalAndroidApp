package pk.edu.itu.csalt.pquiz;

import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.SoundPool;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import wseemann.media.FFmpegMediaMetadataRetriever;


/**
 * Created by Zain on 4/16/17.
 */
public class PlayQuestion  extends AppCompatActivity {

    HttpHandler sh              = null;
    String icount               = null;
    String uid                  = null;
    String uans                 = null;
    // HashMap<String, String> qObj= null;
    LinearLayout qbuttons;
    Button submit_answer   = null;
    private String TAG;

    private SoundPool sp;
    private ArrayList<RecPlayer> rp = null;
    private HashMap<String, Button> buttons;
    ArrayList<RadioButton> rbopts;
    protected ArrayList<HashMap<String, String>> questions;
    Boolean updateInProgress = false;
    Boolean fetchingQuestions = false;
    Integer current_qid = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        TAG  = this.getLocalClassName();

        Intent intent = getIntent();
        // qObj = (HashMap<String, String>) intent.getSerializableExtra("qObj");
        Bundle args   = intent.getExtras();
        uid           = args.getString("uid");

        sh            = new HttpHandler();

        Log.e(TAG, "Got UID: " + uid);

        setContentView(R.layout.question_page);

        submit_answer = (Button) findViewById(R.id.submit_answer);
        questions = new ArrayList<HashMap<String, String>>();

        buttons = new HashMap<String, Button>();
        buttons.put("next", (Button) findViewById(R.id.next));
        buttons.put("forward", (Button) findViewById(R.id.forward));
        buttons.put("submit", (Button) findViewById(R.id.opt1));
        buttons.put("comment", (Button) findViewById(R.id.comment));
        buttons.put("report", (Button) findViewById(R.id.report));
        buttons.put("like", (Button) findViewById(R.id.like));
        buttons.put("dislike", (Button) findViewById(R.id.dislike));
        buttons.put("listen_comments", (Button) findViewById(R.id.user_comments));
        //buttons.put("answer", (ImageButton) findViewById(R.id.submit_answer));
        //buttons.put("level", ));

        rbopts = new ArrayList<RadioButton>();
        rbopts.add((RadioButton) findViewById(R.id.opt1));
        rbopts.add((RadioButton) findViewById(R.id.opt2));
        rbopts.add((RadioButton) findViewById(R.id.opt3));
        rbopts.add((RadioButton) findViewById(R.id.opt4));

        qbuttons = (LinearLayout) findViewById(R.id.qbuttons);

        submit_answer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAnswer();
            }});

        initNewQuestions();

    }

    protected void initNewQuestions(){

        fetchingQuestions = true;
        new API_NEW_QUESTIONS().execute();

    }

    protected void clearView(){

        toggleMenuButtons(true);
        togglePrefButtons(true);

        rbopts.get(Integer.parseInt(uans)-1).setChecked(false);
        rbopts.get(Integer.parseInt(questions.get(current_qid-1).get("copt"))-1).setBackgroundColor(Color.WHITE);
        rbopts.get(Integer.parseInt(questions.get(current_qid-1).get("copt"))-1).setHighlightColor(Color.WHITE);

        submit_answer.setEnabled(true);
        submit_answer.setTextColor(getResources().getColor(R.color.colorPrimary));

        qbuttons.setVisibility(View.INVISIBLE);
//        setContentView(R.layout.question_page);
    }

    protected void setMenuButtonClickEvents(){

        buttons.get("next").setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.w(TAG, "==> Clicked: Next");
                nextQuestion();
            }
        });
        buttons.get("forward").setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                forwardQuestion();
            }
        });
        buttons.get("comment").setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new API_CREATE_COMMENT().execute();
            }
        });
        buttons.get("report").setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new API_SET_PREFERENCE().execute("report");
            }
        });
        buttons.get("like").setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new API_SET_PREFERENCE().execute("like");
            }
        });
        buttons.get("dislike").setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new API_SET_PREFERENCE().execute("dislike");
            }
        });
        buttons.get("listen_comments").setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listenComments();
            }
        });
    }

    protected void toggleMenuButtons(Boolean flag){

        buttons.get("next").setEnabled(flag);
        buttons.get("forward").setEnabled(flag);
        buttons.get("comment").setEnabled(flag);
        buttons.get("listen_comments").setEnabled(flag);

        if (flag){
            buttons.get("next").setTextColor(getResources().getColor(R.color.white));
            buttons.get("forward").setTextColor(getResources().getColor(R.color.colorPrimary));
            buttons.get("comment").setTextColor(getResources().getColor(R.color.colorPrimary));
            buttons.get("listen_comments").setTextColor(getResources().getColor(R.color.colorPrimary));
        }else{
            buttons.get("next").setTextColor(getResources().getColor(R.color.black));
            buttons.get("forward").setTextColor(getResources().getColor(R.color.black));
            buttons.get("comment").setTextColor(getResources().getColor(R.color.black));
            buttons.get("listen_comments").setTextColor(getResources().getColor(R.color.black));
        }
    }

    protected void togglePrefButtons(Boolean flag){

        buttons.get("dislike").setEnabled(flag);
        buttons.get("like").setEnabled(flag);
        buttons.get("report").setEnabled(flag);

        if (flag){
            buttons.get("dislike").setTextColor(getResources().getColor(R.color.white));
            buttons.get("like").setTextColor(getResources().getColor(R.color.white));
            buttons.get("report").setTextColor(getResources().getColor(R.color.white));
        }else{
            buttons.get("dislike").setTextColor(getResources().getColor(R.color.black));
            buttons.get("like").setTextColor(getResources().getColor(R.color.black));
            buttons.get("report").setTextColor(getResources().getColor(R.color.black));
        }
    }

    protected void displayQuestion(final HashMap<String, String> qObj){

        if(sp != null) sp.release();

        sp = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);

        TableRow qr = new TableRow(getApplicationContext());

        TextView qplay= (TextView) findViewById(R.id.qplaybutton);
        final String url = getString(R.string.pq_base) + getString(R.string.pq_recordings) + qObj.get("qfile");

        Log.w(TAG, "URL:" + url);
        final int pqid = sp.load(url, 0);
//        rp.get(0).setURL(url);
        qplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sp.autoPause();
                sp.play(pqid, 10, 10, 1, 0, 1f);
//                rp.get(0).play();
            }
        });

        String pathStr = getString(R.string.pq_base)+getString(R.string.pq_recordings)+qObj.get("qid")+".wav";
        Uri uri = Uri.parse(pathStr);
        FFmpegMediaMetadataRetriever mmr = new FFmpegMediaMetadataRetriever();
        mmr.setDataSource(pathStr);
        Float durationStr = Float.valueOf(mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_DURATION)) / 1000;
        mmr.release();

//        TextView qlen= (TextView) findViewById(R.id.qplaylength);
//        qlen.setText(durationStr.toString());

        TableRow qRadioButtons = (TableRow) findViewById(R.id.qoptions);
        TableRow qPlayButtons  = (TableRow) findViewById(R.id.qOptPlayButtons);

        for (int i = 0; i < Integer.parseInt(qObj.get("topt")); i++){

            final int opt = i + 1;

            RadioButton rb = (RadioButton) qRadioButtons.getChildAt(i);
            TextView aplay = (TextView) qPlayButtons.getChildAt(i);

            final String ourl = getString(R.string.pq_base) + getString(R.string.pq_recordings) + qObj.get("qid") + "_" + opt + ".wav";
            Log.w(TAG, "URL:" + ourl);
            final int oid = sp.load(ourl, opt);
//            rp.get(opt).setURL(ourl);

            aplay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sp.autoPause();
                    sp.play(oid, 10, 10, 1, 0, 1f);
//                    rp.get(opt).play();
                }
            });
            rb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    switch (opt){
                        case 1:{
                            rbopts.get(0).setChecked(true);
                            rbopts.get(1).setChecked(false);
                            rbopts.get(2).setChecked(false);
                            rbopts.get(3).setChecked(false);
                            uans = "1";
                            break;
                        }
                        case 2:{
                            rbopts.get(0).setChecked(false);
                            rbopts.get(1).setChecked(true);
                            rbopts.get(2).setChecked(false);
                            rbopts.get(3).setChecked(false);
                            uans = "2";
                            break;
                        }
                        case 3:{
                            rbopts.get(0).setChecked(false);
                            rbopts.get(1).setChecked(false);
                            rbopts.get(2).setChecked(true);
                            rbopts.get(3).setChecked(false);
                            uans = "3";
                            break;
                        }
                        case 4:{
                            rbopts.get(0).setChecked(false);
                            rbopts.get(1).setChecked(false);
                            rbopts.get(2).setChecked(false);
                            rbopts.get(3).setChecked(true);
                            uans = "4";
                            break;
                        }
                    }
                }
            });
        }
    }

    protected void saveAnswer(){

        if(sp != null)  sp.autoPause();

        if (uans == null) return;

        submit_answer.setEnabled(false);
        Log.w(TAG, "Option Selected:" + uans);
        new API_SAVE_ANSWER().execute();
    }

    protected void nextQuestion(){

        if(sp != null) sp.autoPause();

        current_qid++;
        clearView();

        if (current_qid < questions.size()) {
            displayQuestion(questions.get(current_qid));
        }
        else{
            initNewQuestions();
        }
//        Log.e(TAG, "==> nextQuestion in PlayQuestion: Result OK: " + RESULT_OK);
//        Intent returnIntent = new Intent();
//        returnIntent.putExtra("qid",qObj.get("qid"));
//        returnIntent.putExtra("next", true);
//        returnIntent.putExtra("ans", true);
//        setResult(RESULT_OK,returnIntent);
//        finish();
    }

    protected void goBack(){
        if(sp != null) sp.autoPause();
        Log.e(TAG, "==> goBack in PlayQuestion, Result canceled!");
        Intent returnIntent = new Intent();
        setResult(RESULT_CANCELED, returnIntent);
        finish();
    }

//    @Override
//    public void onBackPressed() {
//
//        super.onBackPressed();
//
//        Log.e(TAG, "==> onBackPressed in PlayQuestion!");
//
//        if (rp != null){
//            rp.stop();
//        }
//
//        Intent returnIntent = new Intent();
//        returnIntent.putExtra("qid",qObj.get("qid"));
//        returnIntent.putExtra("next", false);
//        if (uans == null) {
//            returnIntent.putExtra("ans", false);
//            setResult(RESULT_CANCELED,returnIntent);
//            Log.e(TAG, "==> onBackPressed in PlayQuestion: Result Canceled: " + RESULT_CANCELED);
//        }
//        else{
//            returnIntent.putExtra("ans", true);
//            setResult(RESULT_OK,returnIntent);
//            Log.e(TAG, "==> onBackPressed in PlayQuestion: Result OK: " + RESULT_OK);
//        }
//        finish();
//    }

    protected void forwardQuestion(){

        if(sp != null)  sp.autoPause();

        if (current_qid != null) {

            Intent intent = new Intent(this, ForwardQuestion.class);
            intent.putExtra("uid", uid);
            intent.putExtra("qid", questions.get(current_qid).get("qid"));
            startActivity(intent);
        }
    }

    protected void postComment(String rec_id){

        if(sp != null) sp.autoPause();

        if (current_qid != null) {

            Intent intent = new Intent(this, RecordActivity.class);
            intent.putExtra("uid", uid);
            intent.putExtra("qid", questions.get(current_qid).get("qid"));
            intent.putExtra("type", "uc");
            intent.putExtra("rec_id", rec_id);
            startActivity(intent);
        }
    }

    protected void listenComments(){

        if(sp != null) sp.autoPause();

        if (current_qid != null) {

            Intent intent = new Intent(this, ListenComments.class);
            Bundle args = new Bundle();
            args.putString("qid", questions.get(current_qid).get("qid"));
            intent.putExtras(args);
            startActivity(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        toggleMenuButtons(true);
    }

    public class API_SAVE_ANSWER extends AsyncTask<Boolean, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.e(TAG, "On Pre Execute!");
        }

        protected Boolean doInBackground(Boolean... arg0) {

            if (current_qid == null || questions == null){
                Log.e(TAG, this.getClass().getSimpleName()+" -- current_qid: " + current_qid.toString() + " OR questions : " + questions.toString());
                return false;
            }

            HashMap<String, String> qObj = questions.get(current_qid);

            Log.e(TAG, this.getClass().getSimpleName()+" -- UAns: " + uans + " == COpt: " + qObj.get("copt"));

            questions.get(current_qid);

            Log.e(TAG, "On Pre Execute!");

            String api_url = getString(R.string.pq_base) + getString(R.string.pq_scripts) +
                    "user_answer.php?uid="+uid+"&qid="+qObj.get("qid")+"&opt="+uans+"&call_id=00";

            Log.e(TAG, this.getClass().getSimpleName()+" -- UAns: " + uans + " == COpt: " + qObj.get("copt"));

            if (uans.equals(qObj.get("copt")))
                api_url += "&correct=1";
            else
                api_url += "&correct=0";

            Log.e(TAG, this.getClass().getSimpleName()+" -- API Url: " + api_url);

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
                //submit_answer.setVisibility(View.INVISIBLE);
                submit_answer.setEnabled(false);
                submit_answer.setTextColor(getResources().getColor(R.color.black));
                qbuttons.setVisibility(View.VISIBLE);
                //rbopts.get(Integer.parseInt(qObj.get("copt"))-1).setChecked(true);
                rbopts.get(Integer.parseInt(questions.get(current_qid).get("copt"))-1).setBackgroundColor(Color.GREEN);
                rbopts.get(Integer.parseInt(questions.get(current_qid).get("copt"))-1).setHighlightColor(Color.GREEN);

            }else
                submit_answer.setEnabled(true);
        }
    }

    public class API_CREATE_COMMENT extends AsyncTask<String, Void, Integer> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.e(TAG, "On Pre Execute!");
            toggleMenuButtons(false);
        }

        protected Integer doInBackground(String... arg0) {

            if (current_qid == null) {
                Log.e(TAG, "No Current QID!!");
                return null;
            }

            String api_url = getString(R.string.pq_base)+getString(R.string.pq_scripts)+"new_comment.php?uid="+uid+"&qid="+questions.get(current_qid).get("qid")+"&call_id=00";

            Log.e(TAG, this.getClass().getSimpleName()+" -- API Url: " + api_url);

            String api_response = sh.makeServiceCall(api_url);

            Log.e(TAG, this.getClass().getSimpleName()+" -- API Response: " + api_response);

            if (api_response != null) {
                try {
                    JSONObject jsonObj = new JSONObject(api_response);

                    final JSONObject result = jsonObj.getJSONObject("result");

                    Boolean error = result.getBoolean("error");

                    if (error){
                        Log.e(TAG, this.getClass().getSimpleName()+" -- API Error: True");
                    }else{
                        Log.e(TAG, this.getClass().getSimpleName()+" -- API Error: False");
                        return result.getInt("id");
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

            return null;
        }

        @Override
        protected void onPostExecute(Integer result) {

            super.onPostExecute(result);

            if (result != null){
                postComment(result.toString());
            }
            toggleMenuButtons(true);
        }
    }

    public class API_SET_PREFERENCE extends AsyncTask<String, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.e(TAG, "On Pre Execute!");
        }

        protected Boolean doInBackground(String... arg0) {

            if (current_qid == null) {
                Log.e(TAG, "No Current QID!!");
                return false;
            }

            String pref = arg0[0];
            Log.e(TAG, this.getClass().getSimpleName()+" -- Pref: " + pref);

            String api_url = getString(R.string.pq_base)+getString(R.string.pq_scripts)+"user_like.php?uid="+uid+"&qid="+questions.get(current_qid).get("qid")+"&preference="+pref+"&call_id=00";

            Log.e(TAG, this.getClass().getSimpleName()+" -- API Url: " + api_url);

            String api_response = sh.makeServiceCall(api_url);

            Log.e(TAG, this.getClass().getSimpleName()+" -- API Response: " + api_response);

            if (api_response != null) {
                try {
                    JSONObject jsonObj = new JSONObject(api_response);

                    final JSONObject result = jsonObj.getJSONObject("result");

                    Boolean error = result.getBoolean("error");

                    if (error){
                        Log.e(TAG, this.getClass().getSimpleName()+" -- API Error: True");
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
                togglePrefButtons(false);
            }
        }
    }

    public class API_NEW_QUESTIONS extends AsyncTask<Integer, Void, Integer> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            fetchingQuestions = true;
            questions.clear();
            current_qid = null;
            Log.e(TAG, "On Pre Execute!");
        }

        protected boolean getIteration(){

            String getIterURL = String.format("%s%sget_iteration.php?callid=0&uid=%s", getString(R.string.pq_base), getString(R.string.pq_scripts), uid);

            Log.e(TAG, "get iteration API Url: " + getIterURL);

            String iterResponse = sh.makeServiceCall(getIterURL);

            Log.e(TAG, "Response from get iteration API: " + iterResponse);

            if (iterResponse != null) {
                try {
                    JSONObject jsonObj = new JSONObject(iterResponse);

                    final JSONObject result = jsonObj.getJSONObject("result");

                    Boolean error = result.getBoolean("error");

                    if (error){
                        Log.e(TAG, "API Error: True");
                    }else{

                        Log.e(TAG, "API Error: False");

                        icount = result.getString("icount");

                        Log.e(TAG, "==> icount: "+icount);

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

        protected int getNewQuestions(){

            String url = getString(R.string.pq_base)+getString(R.string.pq_scripts)+"new_questions.php?uid="+uid+"&callid=00&icount="+ icount;

            Log.e(TAG, "New Questions API Url: " + url);
            String jsonStr = sh.makeServiceCall(url);

            Log.e(TAG, "Response from New Questions API: " + jsonStr);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    final JSONObject result = jsonObj.getJSONObject("result");

                    Boolean error = result.getBoolean("error");

                    if (error){
                        Log.e(TAG, "API Error: True");
                    }else{

                        Log.e(TAG, "API Error: False");

                        JSONArray questions_ja = result.getJSONArray("questions");

                        for (int i = 0; i < questions_ja.length(); i++){

                            JSONObject q_jo = questions_ja.getJSONObject(i);

                            HashMap<String, String> q_hm = new HashMap<String, String>();

                            q_hm.put("qid", q_jo.getString("qid"));
                            q_hm.put("qfile", q_jo.getString("q_filename"));
                            q_hm.put("copt", q_jo.getString("correct_option"));
                            q_hm.put("cfile", q_jo.getString("correct_filename"));
                            q_hm.put("topt", q_jo.getString("total_options"));
                            q_hm.put("qtype", q_jo.getString("type"));
                            q_hm.put("qfile", q_jo.getString("q_filename"));

                            Log.e(TAG, "Data # " + i + ": " + q_hm.toString());

                            questions.add(q_hm);
                        }

                        return questions_ja.length();
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

            return 0;
        }

        @Override
        protected Integer doInBackground(Integer... arg0) {

            if (getIteration())
                return getNewQuestions();
            return null;
        }

        @Override
        protected void onPostExecute(Integer result) {

            super.onPostExecute(result);

            if (result == null){
                Log.e(TAG, "Couldn't get json from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Couldn't get json from server. Check LogCat for possible errors!",
                                Toast.LENGTH_LONG).show();
                    }
                });
                return;
            }

            if (result <= 0){
                updateInProgress = true;
                new API_UPDATE_ITER().execute();
            }
            else {
                while (updateInProgress){}
                if (questions.size() > 0){
                    current_qid = 0;
                    displayQuestion(questions.get(current_qid));
                    setMenuButtonClickEvents();
                }else{
                    new API_UPDATE_ITER().execute();
                    Log.w(TAG, "==> No questions in on create!");
                }

            }

            fetchingQuestions = false;
        }
    }

    public class API_UPDATE_ITER extends AsyncTask<Boolean, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.e(TAG, "On Pre Execute!");
        }

        protected Boolean doInBackground(Boolean... arg0) {

            String api_url = getString(R.string.pq_base)+getString(R.string.pq_scripts)+"update_iteration.php?uid="+uid+"&icount="+icount+"&callid=00";

            Log.e(TAG, this.getClass().getSimpleName()+" -- API Url: " + api_url);

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
            Log.e(TAG, "Update done iter.");
            Log.e(TAG, "Gettign new qqqqs.");
            new API_NEW_QUESTIONS().execute();
            updateInProgress = false;
            Log.e(TAG, "update and get done.");

        }
    }

    public class API_AUDIO_LENGTH extends AsyncTask<String, Void, Float> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.e(TAG, "On Pre Execute!");
        }

        protected Float doInBackground(String... arg0) {

            String qid = arg0[0];

            String api_url = getString(R.string.pq_base)+getString(R.string.pq_scripts)+"audio_length.php?path=D:\\xampp\\htdocs\\PQuiz\\Recordings\\"+qid+".wav";

            Log.e(TAG, this.getClass().getSimpleName()+" -- API Url: " + api_url);

            String api_response = sh.makeServiceCall(api_url);

            Log.e(TAG, this.getClass().getSimpleName()+" -- API Response: " + api_response);

            if (api_response != null) {
                try {
                    JSONObject jsonObj = new JSONObject(api_response);

                    final JSONObject result = jsonObj.getJSONObject("result");

                    Boolean error = result.getBoolean("error");

                    if (error){
                        Log.e(TAG, this.getClass().getSimpleName()+" -- API Error: True");
                        return null;
                    }else{
                        Log.e(TAG, this.getClass().getSimpleName()+" -- API Error: False");
                        return (float) result.getDouble("length");
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

            return null;
        }

        @Override
        protected void onPostExecute(Float result) {

            super.onPostExecute(result);

        }
    }


}
