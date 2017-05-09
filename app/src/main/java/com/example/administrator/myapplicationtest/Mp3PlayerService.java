package com.example.administrator.myapplicationtest;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaDataSource;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;

/**
 * Created by Administrator on 2017/4/3 0003.
 */

public class Mp3PlayerService extends Service {
    private IntentFilter intentFilter;
    private BroadcastReceiver broadcastReceiver;
    private MediaPlayer mediaPlayer;
    public static String PLAY_MEDIA=
    "com.example.administrator.myapplicationtest.Mp3PlayerService.PLAY_MEDIA";
    public static String PAUSE_MEDIA=
            "com.example.administrator.myapplicationtest.Mp3PlayerService.PAUSE_MEDIA";
    public static String STOP_MEDIA=
            "com.example.administrator.myapplicationtest.Mp3PlayerService.STOP_MEDIA";
    public static String MEDIA_URL=
            "com.example.administrator.myapplicationtest.Mp3PlayerService.MEDIA_URL";
    @Override
    public IBinder onBind(Intent arg0){
        Log.d("MP3 Service","Service has been bind.");
        return null;
    }

    @Override
    public void onCreate(){
        super.onCreate();
        Log.d("MP3 Service","Service has been created.");
        addIntentFilterMp3();
        broadcastReceiver = new Mp3Receiver();
        registerReceiver(broadcastReceiver,intentFilter);
        mediaPlayer = new MediaPlayer();
        //可以在对mediaplayer的各项回调函数进行复写。
        mediaPlayer.setOnCompletionListener(
                new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        Log.d("MP3 Service","complete");
                        try{
                            mp.seekTo(0);
                            mp.start();
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
        );
    }

    @Override
    public int onStartCommand(Intent intent,int flags,int startId){
        Log.d("MP3 Service","Service is started.");
        return START_STICKY;
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
        Log.d("MP3 Service","Service has been destroyed.");
    }

    public class Mp3Receiver extends BroadcastReceiver{
        @Override
        public void onReceive(final Context context,Intent intent){
            //here we do something.
            //play one of the media
            //stop it,play it,switch.
            mediaPlayer.reset();
            if (intent.getAction().equals(Mp3PlayerService.PLAY_MEDIA)){
                Log.d("media","play media command----->" + intent.getStringExtra(Mp3PlayerService.MEDIA_URL));
                try{
//                    mediaPlayer.setDataSource(Mp3PlayerService.this, Uri.parse(intent.getStringExtra(Mp3PlayerService.MEDIA_URL)));
                    mediaPlayer.setDataSource(Mp3PlayerService.this,Uri.fromFile(new File(intent.getStringExtra(Mp3PlayerService.MEDIA_URL))));
                    mediaPlayer.prepare();
                    mediaPlayer.start();

//                    Intent mIntent = new Intent();
//                    mIntent.setAction(android.content.Intent.ACTION_VIEW);
//                    Uri uri = Uri.fromFile(new File(intent.getStringExtra(Mp3PlayerService.MEDIA_URL)));
//                    mIntent.setDataAndType(uri , "audio/x-wav");
//                    startActivity(mIntent);

                }catch (Exception e){
                    e.printStackTrace();
                }

                //play media at here.
            } else if (intent.getAction().equals(Mp3PlayerService.STOP_MEDIA)){
                Log.d("media","stop media command----->" );
                if (mediaPlayer.isPlaying())
                    mediaPlayer.stop();
            } else if (intent.getAction().equals(Mp3PlayerService.PAUSE_MEDIA)){
                Log.d("media","pause media command----->");
            }
        }
    }

    public void addIntentFilterMp3(){
        intentFilter = new IntentFilter();
        intentFilter.addAction(Mp3PlayerService.PLAY_MEDIA);
        intentFilter.addAction(Mp3PlayerService.STOP_MEDIA);
        intentFilter.addAction(Mp3PlayerService.PAUSE_MEDIA);
    }
}
