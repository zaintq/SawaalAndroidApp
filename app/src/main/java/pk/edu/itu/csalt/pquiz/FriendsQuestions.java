package pk.edu.itu.csalt.pquiz;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.IntegerRes;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.app.ActionBar.LayoutParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import  java.util.Arrays;

import wseemann.media.FFmpegMediaMetadataRetriever;


/**
 * Created by Zain on 4/16/17.
 */
public class FriendsQuestions  extends AppCompatActivity {

    LinearLayout qbuttons;
    //private SoundPool sp;
    private String TAG;

    HttpHandler sh = null;
    String uid = null;
    Boolean fetchingQuestions = false;
    protected ListView lv = null;

    private TextView title;
    private int noOfBtns;
    private Button[] btns;
    private int NUM_ITEMS_PAGE = 10;

    protected ArrayList<HashMap<String, String>> questions, questions_ss;

    RQuestionAdapter adapter = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setTitle(getResources().getString(R.string.forwarded_questions_title));

        TAG  = this.getLocalClassName();

        Intent intent = getIntent();
        Bundle args   = intent.getExtras();
        uid           = args.getString("uid");
        sh            = new HttpHandler();

        Log.e(TAG, "Got UID: " + uid);

        questions    = new ArrayList<HashMap<String, String>>();
        questions_ss = new ArrayList<HashMap<String, String>>();

        //sp = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);

        fetchingQuestions = true;
        new API_FORWARDED_QUESTIONS().execute();
    }

    protected void initNewQuestions(){

        setContentView(R.layout.rquestion_page);

        lv = (ListView) findViewById(R.id.list);

        adapter = new RQuestionAdapter(this, questions_ss);

        lv.setAdapter(adapter);

        populateQuestions(0);
        Btnfooter();
        CheckBtnBackGroud(0);

    }

    public void playAudio(String fpath) throws IOException {

        final MediaPlayer mediaPlayer = new MediaPlayer();

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            public void onCompletion(MediaPlayer mp) {
                mediaPlayer.stop();
                mediaPlayer.reset();
                mediaPlayer.release();
            }
        });

        mediaPlayer.setDataSource(fpath);
        mediaPlayer.prepare();
        mediaPlayer.start();

    }
    
    protected void forwardQuestion(String qid){

        if (qid != null) {

            Intent intent = new Intent(this, ForwardQuestion.class);
            intent.putExtra("uid", uid);
            intent.putExtra("qid", qid);
            startActivity(intent);
        }
    }

    void populateQuestions(int number){

        Log.e(TAG, "POPULATING RQ");

        adapter.clear();

        int start = number * NUM_ITEMS_PAGE;
        int total = start + NUM_ITEMS_PAGE;

        Log.e(TAG, "POPULATING RQ: start = " + start + " -- total = " + questions.size() +" -- end = " + total);

        for(int i = start; i < total ; i++) {

            if(i < questions.size()) {
                Log.e(TAG, "Adding Question: "+ i + "  -- qid:" + questions.get(i).get("qid"));
                adapter.add(questions.get(i));
            }
            else {
                break;
            }
        }
        adapter.notifyDataSetChanged();
        Log.e(TAG, "POPULATING RQ: Done");
    }

    protected class RQuestionAdapter extends ArrayAdapter<HashMap<String, String>> {

        private final Context context;
        private final ArrayList<HashMap<String, String>> values;
        private String TAG = this.getClass().getName();

        public RQuestionAdapter(Context context, ArrayList<HashMap<String, String>> values) {
            super(context, 0, values);
            this.context = context;
            this.values = values;
        }

        @Override
        public HashMap<String, String> getItem(int position) {
            return values.get(position);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                //convertView = LayoutInflater.from(getContext()).inflate(R.layout.rquestion_item, parent, false);
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView             = inflater.inflate(R.layout.fquestion_item, parent, false);
            }

            final HashMap<String, String> rq = getItem(position);

            if (rq != null){

                TextView playButton    = (TextView) convertView.findViewById(R.id.qplaybutton);
                TextView fnum          = (TextView)   convertView.findViewById(R.id.fnum);

                fnum.setText(rq.get("fnum"));

                Log.w(TAG, "** Inflating Question:" + rq.get("qid") + "  - Pos: " + position);

                final String url = getString(R.string.pq_base) + getString(R.string.pq_recordings) + rq.get("qid") + ".wav";
                Log.w(TAG, "URL:" + url);

                //final int pqid = sp.load(url, position);
                final int pqid = 0;

                playButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            playAudio(url);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        //sp.autoPause();
                        //sp.play(pqid, 10, 10, 1, 0, 1f);
                    }});

                for (int i = 0; i < 4 ; i++){

                    final int opt = i + 1;

                    String buttonID = "opt" + opt + "rec";

                    // Log.w(TAG, "button id: " + buttonID);

                    int resID = getResources().getIdentifier(buttonID, "id", getPackageName());

                    // Log.w(TAG, "button - resID: " + resID);
                    // Log.w(TAG, "button - resID - Org: " + R.id.opt1rec);

                    TextView aplay = (TextView) convertView.findViewById(resID);

                    if (Integer.valueOf(opt).toString().equals(rq.get("ans"))){
                        aplay.setTypeface(null, Typeface.BOLD_ITALIC);
                        aplay.setHighlightColor(Color.YELLOW);
                    }

                    final String ourl = getString(R.string.pq_base) + getString(R.string.pq_recordings) + rq.get("qid") + "_" + opt + ".wav";
                    Log.w(TAG, "Loading:" + ourl);
                    //final int oid = sp.load(ourl, position+opt);
                    final int oid = 0;
                    Log.w(TAG, "Loaded.");

                    aplay.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try {
                                playAudio(url);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            //sp.autoPause();
                            //sp.play(oid, 10, 10, 1, 0, 1f);
                        }
                    });
                }
            }else{
                Log.w(TAG, "** rqid is null!");
            }
            return convertView;
        }

    }

    private void CheckBtnBackGroud(int index)
    {
        for(int i=0;i<noOfBtns;i++)
        {
            if(i==index)
            {
                //btns[index].setBackgroundDrawable(getResources().getDrawable(R.drawable.rq_current_page));
                btns[i].setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                btns[i].setTextColor(getResources().getColor(android.R.color.white));
            }
            else
            {
                btns[i].setBackgroundColor(getResources().getColor(android.R.color.transparent));
                btns[i].setTextColor(getResources().getColor(android.R.color.black));
            }
        }

    }

    private void Btnfooter() {

        int val = questions.size() % NUM_ITEMS_PAGE;
        val     = val == 0 ? 0 : 1;

        noOfBtns = questions.size() / NUM_ITEMS_PAGE + val;

        if (noOfBtns <= 1){
            HorizontalScrollView pagination = (HorizontalScrollView) findViewById(R.id.pagination);
            pagination.setVisibility(View.GONE);
        }

        LinearLayout ll = (LinearLayout)findViewById(R.id.btnLay);

        btns    =new Button[noOfBtns];

        for(int i = 0; i < noOfBtns; i++) {

            btns[i] =   new Button(this);
            btns[i].setBackgroundColor(getResources().getColor(android.R.color.transparent));
            btns[i].setText(""+(i+1));

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            ll.addView(btns[i], lp);

            final int j = i;
            btns[j].setOnClickListener(new View.OnClickListener() {

                public void onClick(View v)
                {
                    populateQuestions(j);
                    CheckBtnBackGroud(j);
                }
            });
        }

    }

    protected void postComment(String rec_id, String qid){

        if (qid != null) {

            Intent intent = new Intent(this, RecordActivity.class);
            intent.putExtra("uid", uid);
            intent.putExtra("qid", qid);
            intent.putExtra("type", "uc");
            intent.putExtra("rec_id", rec_id);
            startActivity(intent);
        }
    }

    protected void listenComments(String qid){

        if (qid != null) {

            Intent intent = new Intent(this, ListenComments.class);
            Bundle args = new Bundle();
            args.putString("qid", qid);
            intent.putExtras(args);
            startActivity(intent);
        }
    }

    public class API_CREATE_COMMENT extends AsyncTask<String, Void, Integer> {

        String qid = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.e(TAG, "On Pre Execute!");
            setContentView(R.layout.loading);
        }

        protected Integer doInBackground(String... arg0) {

            qid = arg0[0];

            if (qid == null) {
                Log.e(TAG, "No Current QID!!");
                return null;
            }

            String api_url = getString(R.string.pq_base)+getString(R.string.pq_scripts)+"new_comment.php?uid="+uid+"&qid="+qid+"&call_id=00";

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

            if (result != null && qid != null){
                postComment(qid, result.toString());
            }
        }
    }

    public class API_FORWARDED_QUESTIONS extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.e(TAG, "On Pre Execute!");
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            HttpHandler sh = new HttpHandler();

            String url = getString(R.string.pq_base)+getString(R.string.pq_scripts)+"answers_of_friends.php?uid="+ uid;

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

                        JSONArray questions_ja = result.getJSONArray("questions");

                        for (int i = 0; i < questions_ja.length(); i++){

                            HashMap<String, String> q_hm = new HashMap<String, String>();

                            JSONObject q_jo = questions_ja.getJSONObject(i);

                            q_hm.put("qid", q_jo.getString("qid"));
                            q_hm.put("qfile", q_jo.getString("qfile"));
                            q_hm.put("fuid", q_jo.getString("friend_uid"));
                            q_hm.put("ans", q_jo.getString("opt"));
                            q_hm.put("afile", q_jo.getString("afile"));

                            String keyToPhURL = getString(R.string.key2ph_url)+uid;

                            Log.e(TAG, "KeyToPh API Url: " + keyToPhURL);

                            String keyToPhData = sh.makeServiceCall(keyToPhURL);
                            //String keyToPhData = "03239754007";

                            Log.e(TAG, "Response from KeyToPh API: " + keyToPhData);

                            q_hm.put("fnum", keyToPhData);

                            Log.e(TAG, "Data # " + i + ": " + q_hm.toString());

                            questions.add(q_hm);
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

            initNewQuestions();
        }
    }

}