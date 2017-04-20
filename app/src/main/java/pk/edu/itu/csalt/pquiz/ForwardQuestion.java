package pk.edu.itu.csalt.pquiz;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.Toast;

import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Zain on 4/23/17.
 */
public class ForwardQuestion extends AppCompatActivity{

    String uid = null;
    String qid = null;
    HttpHandler sh = null;
    private String TAG = ForwardQuestion.class.getSimpleName();
    AVLoadingIndicatorView avi;
    ImageButton success_msg, error_msg;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setTitle(getResources().getString(R.string.send_to_friend));

        Intent intent = getIntent();
        Bundle args   = intent.getExtras();
        uid           = args.getString("uid");
        qid           = args.getString("uid");

        sh            = new HttpHandler();

        Log.e(TAG, "Got UID: " + uid);

        setContentView(R.layout.forward);

        Button submit = (Button) findViewById(R.id.fwd_submit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new API_FORWARD().execute();
            }
        });

        avi         = (AVLoadingIndicatorView) findViewById(R.id.avi);
        success_msg = (ImageButton) findViewById(R.id.fwd_success);
        error_msg   = (ImageButton) findViewById(R.id.fwd_error);
    }

    public class API_FORWARD extends AsyncTask<Void, Void, Boolean> {

        String ph       = null;
        String fuid     = null;
        Boolean valid   = false;
        Integer dreq_id = null;

        @Override
        protected void onPreExecute() {

            super.onPreExecute();
            Log.e(TAG, "On Pre Execute, fetching number!");

            success_msg.setVisibility(View.GONE);
            error_msg.setVisibility(View.GONE);
            avi.setVisibility(View.VISIBLE);

            final EditText ET = (EditText) findViewById(R.id.fwd_number);

            ph = ET.getText().toString();

            if(ph.matches("") || ph.length() < 11)
            {
                Toast.makeText(ForwardQuestion.this, "Type a valid number!", Toast.LENGTH_SHORT).show();
                //do something if it is "BYE"
            } else {
                valid = true;
            }

            Log.e(TAG, this.getClass().getSimpleName()+" -- PhValid: " + valid);
        }

        protected void phToKeyAndStore(){

            if (!valid) return;

            String api_url = getString(R.string.dbdir) + "insertNewPh.php?sender="+uid+"&ph="+ph;
            Log.e(TAG, this.getClass().getSimpleName()+" -- API Url: " + api_url);

            fuid = sh.makeServiceCall(api_url);
            fuid = fuid.replaceAll("\\D+","");
            Log.e(TAG, this.getClass().getSimpleName()+" -- API Response: " + fuid);
        }

        protected void PQCreateDeliveryRequest(){

            if (!valid) return;

            String api_url = getString(R.string.pq_base)+getString(R.string.pq_scripts) + "new_delivery_request.php?uid="+uid+"&qid="+qid+"&call_id=00&fuid="+fuid;
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
                        if (result.getBoolean("exists")){
                            Log.e(TAG, this.getClass().getSimpleName()+" -- Delivery already exists: True");
                        }else
                            dreq_id = result.getInt("id");

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
        }

        protected Boolean makeNewReq(){

            if (dreq_id == null){
                Log.e(TAG, this.getClass().getSimpleName()+" -- dreq id null: True");
                return false;
            }

            String api_url = getString(R.string.dbdir) + "New_Req.php?recid="+dreq_id+"&effect=0&callid=00&reqtype=PQDelivery&from="+uid+"&phno="+fuid+"&status=Pending&syslang=Urdu&msglang=Urdu&testcall=FALSE&ch=WateenE1";
            Log.e(TAG, this.getClass().getSimpleName()+" -- API Url: " + api_url);

            String api_response = sh.makeServiceCall(api_url);

            Log.e(TAG, this.getClass().getSimpleName()+" -- API Response: " + api_response);

            if (api_response != null) {
                Log.e(TAG, "Request created!");
                return true;
            } else {
                Log.e(TAG, "Couldn't get response from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Couldn't get response from server. Check LogCat for possible errors!",
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
            return false;
        }

        protected Boolean doInBackground(Void... arg0) {

            Log.e(TAG, this.getClass().getSimpleName()+" -- PhValid: " + valid);

            if (!valid) return false;

            phToKeyAndStore();

            PQCreateDeliveryRequest();

            return makeNewReq();

        }

        @Override
        protected void onPostExecute(Boolean result) {

            super.onPostExecute(result);

            avi.setVisibility(View.GONE);

            if (result)
                success_msg.setVisibility(View.VISIBLE);
                //finish();
            else{
                error_msg.setVisibility(View.VISIBLE);
                Log.e(TAG, this.getClass().getSimpleName()+" -- Not sent!");
            }

        }
    }
}
