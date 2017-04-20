package pk.edu.itu.csalt.pquiz;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;


/**
 * Created by Zain on 4/16/17.
 */
public class ProfileActivity  extends AppCompatActivity {

    String url;
    private String TAG          = ProfileActivity.class.getSimpleName();

    LinearLayout profile_info;
    TextView tv_attempted, tv_correct;

    String uid = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(getResources().getString(R.string.mm_user_profile));

        url= getString(R.string.pq_base) + getString(R.string.pq_scripts) + "user_record.php?uid=";

        Bundle args = getIntent().getExtras();
        uid         = args.getString("uid");

        url += uid;

        Log.e(TAG, "Got UID: " + uid);

        Log.e(TAG, "Starting API!! ");

        new API_PROFILE_STATS().execute();
    }

    public class API_PROFILE_STATS extends AsyncTask<Void, Void, HashMap<String, String>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.e(TAG, "On Pre Execute!");
            setContentView(R.layout.loading);
        }

        @Override
        protected HashMap<String, String> doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();

            Log.e(TAG, "API Url: " + url);
            String jsonStr = sh.makeServiceCall(url);

            Log.e(TAG, "Response from API: " + jsonStr);
            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    final JSONObject result = jsonObj.getJSONObject("result");

                    Boolean error = result.getBoolean("error");

                    if (error){
                        Log.e(TAG, "API Error: True");
                    }else{

                        Log.e(TAG, "API Error: False");

                        String attempted = result.getString("attempted");
                        String correct   = result.getString("correct");

                        Log.e(TAG, "API Result -> Attempted: "+attempted);
                        Log.e(TAG, "API Result -> Correct: "+correct);

                        HashMap<String, String> p_stats = new HashMap<String, String>();
                        p_stats.put("attempted", attempted);
                        p_stats.put("correct", correct);

                        return p_stats;

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
        protected void onPostExecute(final HashMap<String, String> p_stats) {

            super.onPostExecute(p_stats);

            setContentView(R.layout.profile);

            if (p_stats == null) return;

            profile_info = (LinearLayout) findViewById(R.id.profile_info_box);
            tv_attempted = (TextView) findViewById(R.id.attempted);
            tv_correct = (TextView) findViewById(R.id.correct);

            tv_attempted.setText(p_stats.get("attempted"));
            tv_correct.setText( p_stats.get("correct"));

            Button revisitQuestionsButton = (Button) findViewById(R.id.revisit_questions);
            revisitQuestionsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    goToRevisitQuestions();
                }
            });

            profile_info.setVisibility(View.VISIBLE);
            revisitQuestionsButton.setVisibility(View.VISIBLE);

        }
    }

    private void goToRevisitQuestions() {

        Intent intent = new Intent(this, PlayRevisitQuestion.class);
        Bundle args = new Bundle();
        args.putString("uid", uid);
        intent.putExtras(args);
        startActivity(intent);
    }
}
