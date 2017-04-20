package pk.edu.itu.csalt.pquiz;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Zain on 5/12/17.
 */
public class DashboardForAndroidApp extends AppCompatActivity {

    TextView progressingTextView;
    int mm_options = 6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard);

        for (int c = 1; c <= mm_options; c++){
            final int id = c;
            TextView tv = (TextView) findViewById(getResources().getIdentifier("progress_circle_text"+c, "id", getPackageName()));
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.e(this.getClass().getSimpleName(), "On Click Pressed: "+"progressBar"+id );
                    animateMenuCircle(getResources().getIdentifier("progressBar"+id, "id", getPackageName()));
                }
            });
        }
    }

    protected void animateMenuCircle(int progress_bar_id){

        final ProgressBar myprogressBar = (ProgressBar) findViewById(progress_bar_id);
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
}