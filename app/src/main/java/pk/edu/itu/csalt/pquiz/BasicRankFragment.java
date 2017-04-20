package pk.edu.itu.csalt.pquiz;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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

import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Zain on 4/20/17.
 */
public class BasicRankFragment extends Fragment {

    String url;
    private String TAG          = BasicRankFragment.class.getSimpleName();
    protected ListView table = null;
    private RecPlayer rp        = null;
    boolean populated           = false;
    View root_view              = null;
    char type, cat;
    AVLoadingIndicatorView avi = null;

    //ArrayList<HashMap<String, String>> rankings = null;
    String[] rankings = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

        Bundle args = getArguments();

        type = args.getChar("type");
        cat  = args.getChar("cat");

        Log.e(TAG, "BasicRankFragment, type received: " + type);
        Log.e(TAG, "BasicRankFragment, cat received: " + cat);

        super.onCreate(savedInstanceState);

        url= getString(R.string.pq_base) + getString(R.string.pq_scripts);

        switch (cat){
            case 'a' :{
                switch (type){
                    case 'o':
                        url += "get_rankings.php?mode=overall";
                        break;
                    case 'd':
                        url += "get_rankings.php?mode=daily";
                        break;
                    case 'w':
                        url += "get_rankings.php?mode=weekly";
                        break;
                }
                break;
            }
            case 'q' :{
                switch (type){
                    case 'o':
                        url += "get_question_rankings.php?mode=overall";
                        break;
                    case 'd':
                        url += "get_question_rankings.php?mode=daily";
                        break;
                    case 'w':
                        url += "get_question_rankings.php?mode=weekly";
                        break;
                }
                break;
            }
        }

        if (rp == null)
            rp = new RecPlayer();

//        populated = new boolean[3];
//        populated[0] = populated[1] = populated[2] = false;
    }

    protected class RankAdapter extends ArrayAdapter<String> {
        private final Context context;
        private final String[] values;
        private String TAG = this.getClass().getName();

        public RankAdapter(Context context, String[] values) {
            super(context, -1, values);
            this.context = context;
            this.values = values;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View rowView;
            TextView uidView, scoreView;
            TextView playButton;

            if (convertView != null) {
                rowView    = (View) convertView;
                uidView   = (TextView) rowView.findViewById(R.id.uid);
                scoreView= (TextView) rowView.findViewById(R.id.score);
                playButton = (TextView) rowView.findViewById(R.id.playbutton);
            }else{
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                rowView        = inflater.inflate(R.layout.ranking_row, parent, false);
                uidView   = (TextView) rowView.findViewById(R.id.uid);
                scoreView= (TextView) rowView.findViewById(R.id.score);
                playButton = (TextView) rowView.findViewById(R.id.playbutton);
            }

            String params = values[position];
            final String uid = params.split(",")[0];
            final String score = params.split(",")[1];

            Log.w(TAG, "Inflating uid:" + uid + "  - Pos: " + position + "  - score: " + score);

            uidView.setText(uid);
            scoreView.setText(score);

            playButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String url = getString(R.string.pq_base) + getString(R.string.pq_recordings) + "U" + uid + ".wav";
                    Log.w(TAG, "URL:" + url);
                    rp.setURL(url);
                    rp.play();
                }
            });
            if (convertView != null)
                return convertView;
            return rowView;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (root_view == null){
            switch (type){
                case 'o':
                    root_view = inflater.inflate(R.layout.rankingsd, container, false);
                    break;
                case 'd':
                    root_view = inflater.inflate(R.layout.rankingsw, container, false);
                    break;
                case 'w':
                    root_view = inflater.inflate(R.layout.rankingso, container, false);
                    break;
            }
        }else
            return root_view;

        if (root_view != null && table == null){
            table = (ListView) root_view.findViewById(R.id.list);
        }

        if (root_view != null && avi == null) {
            avi = (AVLoadingIndicatorView) root_view.findViewById(R.id.avi);
        }

        new GetRankings().execute();

        return root_view;
    }

    protected void populateRank(){
        RankAdapter adapter = new RankAdapter(root_view.getContext(), rankings);
        table.setAdapter(adapter);
    }

    public class GetRankings extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.e(TAG, "On Pre Execute!");
            if (avi != null) avi.show();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();

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

                        JSONArray users = result.getJSONArray("users");

                        rankings = new String[users.length()];

                        for (int i = 0; i < users.length(); i++) {

                            JSONObject u = users.getJSONObject(i);

                            rankings[i] = u.getString("uid") + "," + u.getString("count");
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

            if (avi != null) avi.hide();

            if (rankings.length > 0)
                populateRank();
            else{
                table.setVisibility(View.INVISIBLE);
                TextView tv = (TextView) root_view.findViewById(R.id.no_rank);
                tv.setVisibility(View.VISIBLE);
            }

        }
    }
}
