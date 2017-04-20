package pk.edu.itu.csalt.pquiz;

import android.media.MediaPlayer;
import android.util.Log;

import java.io.IOException;

/**
 * Created by Zain on 4/17/17.
 */
public class RecPlayer {

    String TAG = "RecPlayer";
    private MediaPlayer mediaPlayer;

    public RecPlayer(){

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.stop();
                mp.reset();
            }
        });
        mediaPlayer.setOnErrorListener( new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {

                Log.e(TAG, "Media Player Crashed. Check file path perhaps?");
                mp.stop();
                return true;
            }
        });
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            public void onPrepared(MediaPlayer mp) {
            }
        });
    }

    public boolean setURL(String url){

        Log.e(TAG, "~~ MP Test Log: URL Set => " + url);

        //stopAndKill();

        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.reset();
        }

        try {
            Log.e(TAG, "Setting datasource");
            mediaPlayer.setDataSource(url);
            Log.e(TAG, "Data source set");
            try {
                Log.e(TAG, "Preparing");
                mediaPlayer.prepare();
                Log.e(TAG, "Prepared");
            } catch (IOException e) {
                Log.e(TAG, "Unable to prepare!");
                e.printStackTrace();
                return false;
            } catch (Exception e) {
                Log.e(TAG, "~~ Media Player Crashed While Preparing!");
                Log.e(TAG, "~~ Error: "+String.valueOf(e));
                e.printStackTrace();
                return false;
            }
        } catch (IOException e) {
            Log.e(TAG, "Unable to set data source!");
            e.printStackTrace();
            return false;
        }  catch (Exception e) {
            Log.e(TAG, "~~ Media Player Crashed!");
            Log.e(TAG, "~~ Error: "+String.valueOf(e));
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void play(){
        Log.e(TAG, "~~ MP Test Log: Playing");
        mediaPlayer.start();
    }

    public void pause(){
        mediaPlayer.pause();
    }

    private void stopAndKill() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            mediaPlayer = new MediaPlayer();
        }
    }

    public void stop(){
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying())
                mediaPlayer.stop();
        }
    }

    public int getDuration(){
        return mediaPlayer.getDuration();
    }


}
