package pk.edu.itu.csalt.pquiz;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private String TAG = MainActivity.class.getSimpleName();
    HttpHandler sh;

    private Button aRankingsButton, qRankingsButton, profileButton, newQuestionsButton;
    private Button revisitQuestionsButton, fwdQuestionsButton, postQuestionButton, testButton;
    private String phno = null;
    private String uid = null;
    private String newq= null;

    static final int PostQuestionRequestCode     = 9754;
    static final int PostQuestionOpt1RequestCode = 97541;
    static final int PostQuestionOpt2RequestCode = 97542;
    static final int PostQuestionOpt3RequestCode = 97543;
    static final int PostQuestionOpt4RequestCode = 97544;

    int mm_options = 6;

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu)
//    {
//        MenuInflater menuInflater = getMenuInflater();
//        menuInflater.inflate(R.menu.main_menu, menu);
//        return true;
//    }

    public class API_CREATE_QUESTION extends AsyncTask<String, Void, Integer> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.e(TAG, "On Pre Execute!");
        }

        protected Integer doInBackground(String... arg0) {

            String api_url = getString(R.string.pq_base)+getString(R.string.pq_scripts)+"create_new_question.php?uid="+uid+"&call_id=00";

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
                newq = result.toString();
                goToPostQuestion(newq, "uq");
            }
        }
    }

    public class API_GET_UID extends AsyncTask<String, Void, String> {

        private String TAG = "** API Log **";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {

            String url = params[0];

            Log.e(TAG, "Passed URL: " + url);

            String jsonStr = sh.makeServiceCall(url);

            Log.e(TAG, "Response from API: " + jsonStr);

            return jsonStr;
        }

        @Override
        protected void onPostExecute(String result) {

            super.onPostExecute(result);

            uid = result.replaceAll("[^\\d.]", "");

            if (uid == null){
                Log.e(TAG, "Can't set UID.");
                setContentView(R.layout.raw_error);
            }else {
                Log.e(TAG, "UID set to: " + result);
            }

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);


        sh = new HttpHandler();

        getUID();

        //setContentView(R.layout.activity_main);

        setContentView(R.layout.dashboard);

        for (int c = 1; c <= mm_options; c++){
            final int id = c;
            TextView tv = (TextView) findViewById(getResources().getIdentifier("progress_circle_text"+c, "id", getPackageName()));
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.e(this.getClass().getSimpleName(), "On Click Pressed: "+"progressBar"+id );
                    animateMenuCircle(getResources().getIdentifier("progressBar"+id, "id", getPackageName()), false);
                    switch (id){
                        case 1:{
                            goToNewQuestions();
                            break;
                        }
                        case 2:{
                            new API_CREATE_QUESTION().execute();
                            break;
                        }
                        case 3:{
                            goToRankings('a');
                            break;
                        }
                        case 4:{
                            goToRankings('q');
                            break;
                        }
                        case 5:{
                            goToProfile();
                            break;
                        }
                        case 6:{
                            gotToForwardedQuestions();
                            break;
                        }
                        default:{
                            Log.e(this.getClass().getSimpleName(), "No case defined for: " + id );
                        }
                    }
                }
            });
        }

        // setMenuOnClickEvents();
    }

    private void getUID(){

        TelephonyManager tMgr = (TelephonyManager) getSystemService(getApplicationContext().TELEPHONY_SERVICE);

        phno = tMgr.getLine1Number();
        Log.e(TAG, "Phone number recieved: " + phno);

        phno = "03239754007";
        Log.e(TAG, "Phone number changed to: " + phno);

        String api_uid_url = getString(R.string.ph2key_url)+phno;

        Log.e(TAG, "URL: " + api_uid_url);

        //uid = "3566";

        new API_GET_UID().execute(api_uid_url);
    }

    private void setMenuOnClickEvents(){

        aRankingsButton = (Button) findViewById(R.id.answer_rankings);
        aRankingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToRankings('a');
            }
        });

        qRankingsButton = (Button) findViewById(R.id.question_rankings);
        qRankingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToRankings('q');
            }
        });

        profileButton = (Button) findViewById(R.id.profile);
        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToProfile();
            }
        });

        newQuestionsButton = (Button) findViewById(R.id.new_questions);
        newQuestionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToNewQuestions();
            }
        });

        fwdQuestionsButton = (Button) findViewById(R.id.friend_questions);
        fwdQuestionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotToForwardedQuestions();
            }
        });

        postQuestionButton = (Button) findViewById(R.id.post_question);
        postQuestionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new API_CREATE_QUESTION().execute();
            }
        });
    }

    private void goToRankings(char cat) {

        Intent intent = new Intent(this, TabbedRankings.class);
        Bundle args = new Bundle();
        args.putChar("cat", cat);
        intent.putExtras(args);
        startActivity(intent);
    }

    private void goToProfile() {

        Intent intent = new Intent(this, ProfileActivity.class);
        Bundle args = new Bundle();
        args.putString("uid", uid);
        intent.putExtras(args);
        startActivity(intent);
    }

    private void goToNewQuestions() {

//        Intent intent = new Intent(this, NewQuestions.class);
        Intent intent = new Intent(this, PlayQuestion.class);
        Bundle args = new Bundle();
        args.putString("uid", uid);
        intent.putExtras(args);
        startActivity(intent);
    }

    private void gotToForwardedQuestions() {

        Intent intent = new Intent(this, FriendsQuestions.class);
        Bundle args = new Bundle();
        args.putString("uid", uid);
        intent.putExtras(args);
        startActivity(intent);
    }

    private void goToPostQuestion(String rec_id, String type) {

        Intent intent = new Intent(this, RecordNewQuestion.class);
        intent.putExtra("uid", uid);
        intent.putExtra("type", type);
        intent.putExtra("qid", rec_id);
        startActivity(intent);
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//
//        Log.e(TAG, "==> On Activity Result - Request Code:" + requestCode);
//        Log.e(TAG, "==> On Activity Result - NewQuestionRequestCode " + PostQuestionRequestCode);
//        Log.e(TAG, "==> On Activity Result - Result Code:"  + resultCode);
//        Log.e(TAG, "==> On Activity Result - Result_OK:"  + RESULT_OK);
//
//        if (requestCode == PostQuestionRequestCode) {
//
//            Log.e(TAG, "==> On Activity Result - In (requestCode == NewQuestionRequestCode) ");
//            Log.e(TAG, "==> On Activity Result - result code " + resultCode);
//            Log.e(TAG, "==> On Activity Result - result ok " + RESULT_OK);
//
//            if (resultCode == RESULT_OK) {
//
//                Log.e(TAG, "==> On Activity Result - In (resultCode == RESULT_OK) ");
//                goToPostQuestion(newq+"_1", "option");
//            }
//
//            if (resultCode == RESULT_CANCELED) {
//            }
//        }
//    }

    @Override
    protected void onResume() {
        super.onResume();
        for (int c = 1; c <= mm_options; c++){
            animateMenuCircle(getResources().getIdentifier("progressBar"+c, "id", getPackageName()), true);
        }

    }

    protected void animateMenuCircle(int progress_bar_id, boolean wipe){

        final ProgressBar myprogressBar = (ProgressBar) findViewById(progress_bar_id);

        if (wipe){
            myprogressBar.setProgress(0);
            return;
        }

        final Handler progressHandler   = new Handler();

        new Thread(new Runnable() {
            int i = 0;
            public void run() {
                while (i < 100) {
                    i += 10;
                    progressHandler.post(new Runnable() {
                        public void run() {
                            myprogressBar.setProgress(i);
                        }
                    });
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void goToTest() {

        Intent intent = new Intent(this, RecordActivity.class);
        Bundle args = new Bundle();
        args.putString("uid", uid);
        intent.putExtras(args);
        startActivity(intent);
    }
}
