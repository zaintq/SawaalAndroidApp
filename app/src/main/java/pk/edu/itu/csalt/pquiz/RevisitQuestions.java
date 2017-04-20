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
public class RevisitQuestions  extends AppCompatActivity {

    HttpHandler sh              = null;
    String uid                  = null;
    private String TAG          = FriendsQuestions.class.getSimpleName();
    private RecPlayer rp        = null;
    protected TableLayout table = null;
    protected ArrayList<HashMap<String, String>> questions;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (rp == null)
            rp = new RecPlayer();

        Bundle args = getIntent().getExtras();
        uid         = args.getString("uid");
        sh          = new HttpHandler();

        Log.e(TAG, "Got UID: " + uid);

        questions = new ArrayList<HashMap<String, String>>();

        new API_REVISIT_QUESTIONS().execute();
    }

    public class API_REVISIT_QUESTIONS extends AsyncTask<Integer, Void, Integer> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.e(TAG, "On Pre Execute!");
            setContentView(R.layout.loading);
        }

        @Override
        protected Integer doInBackground(Integer... arg0) {

            String url = getString(R.string.pq_base)+getString(R.string.pq_scripts)+"attempted_questions.php?uid="+uid;

            Log.e(TAG, "RQuestions API Url: " + url);
            String jsonStr = sh.makeServiceCall(url);

            Log.e(TAG, "Response from RQuestions API: " + jsonStr);

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
        protected void onPostExecute(Integer result) {

            super.onPostExecute(result);

            setContentView(R.layout.question_list);

            table = (TableLayout) findViewById(R.id.table);

            populateQuestions();
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

        Intent intent = new Intent(this, PlayRevisitQuestion.class);
        Bundle args = new Bundle();
        args.putString("uid", uid);
        intent.putExtra("qObj", qObj);
        intent.putExtras(args);
        startActivity(intent);
    }

    protected void forwardQuestion(String qid){

        Intent intent = new Intent(this, ForwardQuestion.class);
        intent.putExtra("uid", uid);
        intent.putExtra("qid", qid);
        startActivity(intent);
    }

    protected void postComment(String rec_id, String qid){

        Intent intent = new Intent(this, RecordActivity.class);
        intent.putExtra("uid", uid);
        intent.putExtra("qid", qid);
        intent.putExtra("type", "uc");
        intent.putExtra("rec_id", rec_id);
        startActivity(intent);
    }

    protected void listenComments(String qid){

        Intent intent = new Intent(this, ListenComments.class);
        Bundle args = new Bundle();
        args.putString("qid", qid);
        intent.putExtras(args);
        startActivity(intent);
    }
}
