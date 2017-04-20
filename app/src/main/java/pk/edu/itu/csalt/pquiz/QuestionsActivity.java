package pk.edu.itu.csalt.pquiz;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by Zain on 4/16/17.
 */
public class QuestionsActivity extends AppCompatActivity{

    private String TAG = QuestionsActivity.class.getSimpleName();
    protected TableLayout table;
    private RecPlayer rp;
    ArrayList<HashMap<String, String>> arankings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.rankingsw);

        rp          = new RecPlayer();
        arankings   = new ArrayList<>();
        table       = (TableLayout)  findViewById(R.id.list);

        new GetQuestions().execute();
    }

    public class GetQuestions extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Toast.makeText(MainActivity.this,"Json Data is downloading",Toast.LENGTH_LONG).show();
            Log.e(TAG, "On Pre Execute!");
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();
            // Making a request to url and getting response
            //String url = "http://api.androidhive.info/contacts/";
            String url = "http://58.27.220.110:201/PQuiz/Scripts/get_rankings.php?mode=overall";
            //String url  = "http://localhost/PQuiz/Scripts/get_rankings.php?mode=overall";
            String jsonStr = sh.makeServiceCall(url);

            Log.e(TAG, "Response from API: " + jsonStr);
            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    JSONObject result = jsonObj.getJSONObject("result");

                    Boolean error = result.getBoolean("error");

                    if (error){
                        Log.e(TAG, "API Error: True");
                    }else{

                        Log.e(TAG, "API Error: False");

                        JSONArray users = result.getJSONArray("users");

                        for (int i = 0; i < users.length(); i++) {

                            JSONObject u = users.getJSONObject(i);

                            HashMap<String, String> user = new HashMap<>();

                            user.put("uid", u.getString("uid"));
                            user.put("score", u.getString("count"));

                            Log.e(TAG, "UID: " + u.getString("uid"));
                            Log.e(TAG, "COUNT: " + u.getString("count"));

                            arankings.add(user);
                        }
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
        protected void onPostExecute(Void result) {

            super.onPostExecute(result);

            for (int i = 0; i < arankings.size(); i++) {

                TableRow tr = new TableRow(getApplicationContext());

                final String uid   = arankings.get(i).get("uid");
                final String score = arankings.get(i).get("score");

                TextView uid_v = new TextView(getApplicationContext());

                uid_v.setText(uid);
                uid_v.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
                        LayoutParams.WRAP_CONTENT));
                uid_v.setPadding(5, 5, 5, 5);
                uid_v.setTextColor(Color.BLACK);
                // uid_v.setBackgroundColor(Color.GRAY);

                tr.addView(uid_v);

                TextView score_v = new TextView(getApplicationContext());

                score_v.setText(score);
                score_v.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
                        LayoutParams.WRAP_CONTENT));
                score_v.setPadding(5, 5, 5, 5);
                uid_v.setTextColor(Color.BLACK);
                //score_v.setBackgroundColor(Color.GRAY);

                tr.addView(score_v);

                ImageButton play_button = new ImageButton(getApplicationContext());
                play_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String url = getString(R.string.pq_base) + getString(R.string.pq_recordings) + "U" + uid + ".wav";
                        Log.w(TAG, "URL:" + url);
                        rp.setURL(url);
                        rp.play();
                    }});

                tr.addView(play_button);

                table.addView(tr, new TableLayout.LayoutParams(
                        LayoutParams.MATCH_PARENT,
                        LayoutParams.WRAP_CONTENT));

            }
        }
    }

    protected void playRank(String uid, String score){

        String url = getString(R.string.pq_base) + getString(R.string.pq_recordings) + "U" + uid + ".wav";
        Log.e(TAG, "Rank URL: "+url);

        if (rp.setURL(url))
            rp.play();
        else{
            Toast.makeText(QuestionsActivity.this,"Can't play this ranking!",Toast.LENGTH_LONG).show();
            Log.e(TAG, "Can't play URL: "+url);
        }
    }

}
