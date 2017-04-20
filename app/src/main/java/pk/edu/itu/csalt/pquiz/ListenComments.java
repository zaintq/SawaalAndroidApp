package pk.edu.itu.csalt.pquiz;

import android.app.ListActivity;
import android.content.Context;
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
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
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
 * Created by Zain on 4/20/17.
 */

public class ListenComments extends AppCompatActivity {

    private String TAG          = BasicRankFragment.class.getSimpleName();
    protected ListView lv       = null;
    private RecPlayer rp        = null;
    String qid                  = null;
    char type, cat;

    ArrayList<HashMap<String, String>> comments = null;
    String[] comments_str = null;

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if (rp != null){
            rp.stop();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setTitle(getResources().getString(R.string.user_comments));

        Bundle args = getIntent().getExtras();
        qid  = args.getString("qid");

        setContentView(R.layout.comments_list);

        lv = (ListView) findViewById(R.id.list);

        Log.e(TAG, "Listen Comments, qid received: " + qid);

        if (rp == null)
            rp = new RecPlayer();

        if (comments == null){
            comments     = new ArrayList<>();
            new API_GET_COMMENTS().execute();
        }
    }

    void populateComments(){

        Log.e(TAG, "POPULATING COMMENTS");

        CommentAdapter adapter = new CommentAdapter(this, comments_str);
        lv.setAdapter(adapter);

        Log.e(TAG, "POPULATING COMMENTS: Done");

    }

    public class API_GET_COMMENTS extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.e(TAG, "On Pre Execute!");
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            if (qid == null){
                Log.e(TAG, "QID is null!");
                return null;
            }
            
            HttpHandler sh = new HttpHandler();

            String url = getString(R.string.pq_base) + getString(R.string.pq_scripts) + "get_comments.php?qid="+qid;

            Log.e(TAG, "Listen Comments, get comments url: " + url);

            Log.e(TAG, "API Url: " + url);
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

                        JSONArray comments_json = result.getJSONArray("comments");

                        comments_str = new String[comments_json.length()];

                        for (int i = 0; i < comments_json.length(); i++) {

                            JSONObject comment_json = comments_json.getJSONObject(i);

                            HashMap<String, String> comment = new HashMap<>();

                            comment.put("uid", comment_json.getString("uid"));
                            comment.put("cid", comment_json.getString("cid"));

                            Log.e(TAG, "UID: " + comment_json.getString("uid"));
                            Log.e(TAG, "COUNT: " + comment_json.getString("cid"));

                            comments.add(comment);
                            comments_str[i] = comment_json.getString("cid");
                        }
                    }
                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                }

            } else {
                Log.e(TAG, "Couldn't get json from server.");
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            super.onPostExecute(result);

            if (comments.size() > 0)
                populateComments();
            else{
                lv.setVisibility(View.INVISIBLE);
                TextView tv = (TextView) findViewById(R.id.no_comments);
                tv.setVisibility(View.VISIBLE);
            }
        }
    }

    protected class CommentAdapter extends ArrayAdapter<String> {
        private final Context context;
        private final String[] values;
        private String TAG = this.getClass().getName();

        public CommentAdapter(Context context, String[] values) {
            super(context, -1, values);
            this.context = context;
            this.values = values;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View rowView           ;
            TextView textView      ;
            TextView playButton    ;

            if (convertView != null) {
                rowView    = (View) convertView;
                textView   = (TextView) rowView.findViewById(R.id.cid);
                playButton = (TextView) rowView.findViewById(R.id.cplaybutton);
            }else{
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                rowView        = inflater.inflate(R.layout.comment_item, parent, false);
                textView       = (TextView) rowView.findViewById(R.id.cid);
                playButton     = (TextView) rowView.findViewById(R.id.cplaybutton);
            }

            textView.setText(String.valueOf(position + 1));
            final String cid = values[position];

            Log.w(TAG, "Inflating Comment:" + cid + "  - Pos: " + position);

            playButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String url = context.getString(R.string.pq_base) + context.getString(R.string.pq_comments) + cid + ".wav";
                    Log.w(TAG, "URL:" + url);
                    rp.setURL(url);
                    rp.play();
                }});
            if (convertView != null)
                return convertView;
            return rowView;
        }
    }

}
