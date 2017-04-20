package pk.edu.itu.csalt.pquiz;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * Created by Zain on 4/16/17.
 */
public class NewQuestions  extends AppCompatActivity {

    HttpHandler sh              = null;
    String icount               = null;
    String uid                  = null;
    private String TAG          = FriendsQuestions.class.getSimpleName();
    private RecPlayer rp        = null;
    protected TableLayout table = null;
    protected ArrayList<HashMap<String, String>> questions;
    Boolean updateInProgress = false;

    static final int NewQuestionRequestCode = 1234;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(getResources().getString(R.string.mm_new_questions));

        if (rp == null)
            rp = new RecPlayer();

        Bundle args = getIntent().getExtras();
        uid         = args.getString("uid");
        sh          = new HttpHandler();

        Log.e(TAG, "Got UID: " + uid);

        setContentView(R.layout.question_list);

        table = (TableLayout) findViewById(R.id.table);
        questions = new ArrayList<HashMap<String, String>>();

        new API_NEW_QUESTIONS().execute();
    }

//    @Override
//    public void onResume() {
//        super.onResume();
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.e(TAG, "==> On Activity Result Called In NewQuestions!!!");
//        Log.e(TAG, "==> On Activity Result - Request Code:" + requestCode);
//        Log.e(TAG, "==> On Activity Result - Result Code:"  + resultCode);
//        Log.e(TAG, "==> On Activity Result - Data Code: " + data.getExtras().toString() );
//        Log.e(TAG, "==> On Activity Result - Data Code String: " + data.getDataString() );

        Log.e(TAG, "==> On Activity Result - requestCode " + requestCode);
        Log.e(TAG, "==> On Activity Result - NewQuestionRequestCode " + NewQuestionRequestCode);

        if (requestCode == NewQuestionRequestCode) {

            Log.e(TAG, "==> On Activity Result - In (requestCode == NewQuestionRequestCode) ");
            Log.e(TAG, "==> On Activity Result - result code " + resultCode);
            Log.e(TAG, "==> On Activity Result - result ok " + RESULT_OK);

            if (resultCode == RESULT_OK) {

                Log.e(TAG, "==> On Activity Result - In (resultCode == RESULT_OK) ");

                String qid   = data.getStringExtra("qid");
                Boolean next = data.getBooleanExtra("next", false);
                Boolean ans  = data.getBooleanExtra("ans", false);


                Log.e(TAG, "==> On Activity Result - qid: " + qid);
                Log.e(TAG, "==> On Activity Result - next: " + next);
                Log.e(TAG, "==> On Activity Result - ans: " + ans);
                Log.e(TAG, "==> On Activity Result - qsize: " + questions.size());

                if (ans){

                    for (int i = 0; i < questions.size(); i++) {
                        Log.e(TAG, "==> Checking  " + qid + " == " + questions.get(i).get("qid"));

                        if (qid.equals(questions.get(i).get("qid"))){
                            Log.e(TAG, "==> On Activity Result - removing question: " + i);
                            questions.remove(i);
                        }
                    }

                    Log.e(TAG, "==> On Activity Result - updated qsize: " + questions.size());

                    if (questions.size() <= 0) {
                        new API_UPDATE_ITER().execute();
                    }else {
                        populateQuestions();
                    }

                    if (next){
                        while (updateInProgress){}
                        if (questions.size() > 0)
                            goToQuestion(questions.get(0));
                    }
                }
            }

            if (resultCode == RESULT_CANCELED) {
            }
        }
    }

    public class API_NEW_QUESTIONS extends AsyncTask<Integer, Void, Integer> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.e(TAG, "On Pre Execute!");
        }

        protected boolean getIteration(){

            String getIterURL = getString(R.string.pq_base)+getString(R.string.pq_scripts)+"get_iteration.php?uid="+uid+"&callid=00";

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

            if (result <= 0){
                updateInProgress = true;
                new API_UPDATE_ITER().execute();
            }
            else {
                while (updateInProgress){}
                populateQuestions();
            }
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

    protected void populateQuestions(){

        Log.e(TAG, "POPULATING QUESTIONS");

        table.removeAllViews();
        while(table.getChildCount() > 0){
            Log.e(TAG, "REMOVING VIEW");
            table.removeViewAt(0);
        }

        for (int i = 0; i < questions.size(); i++) {

            final HashMap<String, String> qObj = questions.get(i);

            final String qfile = qObj.get("qfile");

            TableRow qr    = new TableRow(getApplicationContext());

            TextView ind_v = new TextView(getApplicationContext());

            ind_v.setText(String.valueOf(i+1));
            ind_v.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT));
            ind_v.setPadding(5, 5, 5, 5);
            ind_v.setTextColor(Color.BLACK);


            ImageButton qplay= new ImageButton(getApplicationContext());
            qplay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.e(TAG, "qplay => " + qObj.toString());
                    String url = getString(R.string.pq_base) + getString(R.string.pq_recordings) + qfile;
                    Log.w(TAG, "URL:" + url);
                    rp.setURL(url);
                    rp.play();
                }});

            ImageButton qbutton= new ImageButton(getApplicationContext());
            qbutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.e(TAG, "goToQuestion => " + qObj.toString());
                    goToQuestion(qObj);
                }});

            qr.addView(ind_v);
            qr.addView(qplay);
            qr.addView(qbutton);

            table.addView(qr, new TableLayout.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT
            ));
        }

        Log.e(TAG, "POPULATING QUESTIONS: Done");

    }

    protected void goToQuestion(HashMap<String, String> qObj){

        Intent intent = new Intent(this, PlayQuestion.class);
        Bundle args = new Bundle();
        args.putString("uid", uid);
        intent.putExtra("qObj", qObj);
        intent.putExtras(args);
        startActivityForResult(intent,NewQuestionRequestCode);
        //startActivity(intent);
    }
}
