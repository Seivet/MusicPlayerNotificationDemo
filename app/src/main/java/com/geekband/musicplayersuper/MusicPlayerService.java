package com.geekband.musicplayersuper;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.widget.RemoteViews;

import java.util.ArrayList;

public class MusicPlayerService extends Service{

    public static final int NOTIFY_ID = 123;
    private MyBinder mMyBinder = new MyBinder();
    private MusicPlayerDao mMusic;
    private ArrayList<MusicPlayerPojo> mSongs;
    private int i = 0;
    private MediaPlayer mMediaPlayer;
    public static final String ACTION_NOTIFICATION = "action_notification";
    public static final String BUTTON_INDEX = "button_index";
    public static final String BUTTON_PREV = "0";
    public static final String BUTTON_PLAY = "1";
    public static final String BUTTON_NEXT = "2";

    private boolean isPlaying = false;

    Messenger mMessenger = new Messenger(new IncomingHandler());
    Messenger mActivityMessenger;
    private RemoteViews mRemoteViews;
    private NotificationCompat.Builder mBuilder;
    private NotificationManager mNotificationManager;

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            mActivityMessenger = msg.replyTo;
            switch (msg.what) {
                case 0:
                    mMediaPlayer.stop();
                    i  = msg.arg1;
                    mMediaPlayer = MediaPlayer.create(MusicPlayerService.this, mSongs.get(i).getSrc());
                    if(isPlaying) {
                        mMediaPlayer.start();
                    }
                    mRemoteViews.setTextViewText(R.id.music_name, mSongs.get(i).getName());
                    mRemoteViews.setTextViewText(R.id.singer_name, mSongs.get(i).getSinger());
                    mNotificationManager.notify(NOTIFY_ID, mBuilder.build());
                    break;
                case 1:
                    if(msg.arg1==1){
                        isPlaying =false;
                        mMediaPlayer.pause();
                        mRemoteViews.setImageViewResource(R.id.btn_play, R.drawable.music_play);
                        mNotificationManager.notify(NOTIFY_ID, mBuilder.build());
                    } else {
                        isPlaying = true;
                        i = msg.arg2;
                        mMediaPlayer = MediaPlayer.create(MusicPlayerService.this, mSongs.get(i).getSrc());
                        mMediaPlayer.start();
                        mRemoteViews.setImageViewResource(R.id.btn_play, R.drawable.music_pause);
                        mNotificationManager.notify(NOTIFY_ID, mBuilder.build());
                    }
                    break;
                default:
                    break;
            }
        }
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mMusic = MusicPlayerDao.getMusicList();
        mSongs = mMusic.getList();
        mMediaPlayer = MediaPlayer.create(this, mSongs.get(i).getSrc());
        mMediaPlayer.setLooping(true);
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mMyBinder.next();
            }
        });


        mRemoteViews = new RemoteViews(getPackageName(), R.layout.layout_notification);
        mRemoteViews.setTextViewText(R.id.music_name, mSongs.get(i).getName());
        mRemoteViews.setTextViewText(R.id.singer_name, mSongs.get(i).getSinger());

        Intent intent = new Intent(this, MusicPlayerService.class);
        intent.setAction(ACTION_NOTIFICATION);


        intent.putExtra(BUTTON_INDEX, BUTTON_PLAY);
        PendingIntent  pendingIntentPrev = PendingIntent.getService(this, 2, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        mRemoteViews.setOnClickPendingIntent(R.id.btn_play, pendingIntentPrev);

        intent.putExtra(BUTTON_INDEX, BUTTON_NEXT);
        pendingIntentPrev = PendingIntent.getService(this, 3, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        mRemoteViews.setOnClickPendingIntent(R.id.btn_next, pendingIntentPrev);

        intent.putExtra(BUTTON_INDEX, BUTTON_PREV);
        pendingIntentPrev = PendingIntent.getService(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mRemoteViews.setOnClickPendingIntent(R.id.btn_prev, pendingIntentPrev);

        mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setSmallIcon(R.drawable.icon2);
        mBuilder.setContent(mRemoteViews);
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(NOTIFY_ID, mBuilder.build());

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String action = intent.getAction();
        String stringExtra = intent.getStringExtra(BUTTON_INDEX);

        if(TextUtils.equals(action, ACTION_NOTIFICATION)) {
            if (TextUtils.equals(stringExtra, BUTTON_NEXT)) {
                i = (i+1)>= mSongs.size()? 0 : i+1;
                mMediaPlayer.stop();
                mMediaPlayer = MediaPlayer.create(MusicPlayerService.this, mSongs.get(i).getSrc());
                if(isPlaying) {
                    mMediaPlayer.start();
                }

                mRemoteViews.setTextViewText(R.id.music_name, mSongs.get(i).getName());
                mRemoteViews.setTextViewText(R.id.singer_name, mSongs.get(i).getSinger());
                mNotificationManager.notify(NOTIFY_ID, mBuilder.build());

            } else if(TextUtils.equals(stringExtra, BUTTON_PLAY)) {
                if(mMediaPlayer.isPlaying()){
                    isPlaying =false;
                    mMediaPlayer.pause();

                    mRemoteViews.setImageViewResource(R.id.btn_play, R.drawable.music_play);
                    mNotificationManager.notify(NOTIFY_ID, mBuilder.build());
                } else {
                    isPlaying = true;
                    mMediaPlayer = MediaPlayer.create(MusicPlayerService.this, mSongs.get(i).getSrc());
                    mMediaPlayer.start();

                    mRemoteViews.setImageViewResource(R.id.btn_play,R.drawable.music_pause);
                    mNotificationManager.notify(NOTIFY_ID, mBuilder.build());
                }

            } else {

                i = (i-1)<0? mSongs.size()-1:i-1;
                mMediaPlayer.stop();
                mMediaPlayer = MediaPlayer.create(MusicPlayerService.this, mSongs.get(i).getSrc());
                if(isPlaying) {
                    mMediaPlayer.start();
                }

                mRemoteViews.setTextViewText(R.id.music_name, mSongs.get(i).getName());
                mRemoteViews.setTextViewText(R.id.singer_name, mSongs.get(i).getSinger());
                mNotificationManager.notify(NOTIFY_ID, mBuilder.build());
            }

            sendToActivity();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void sendToActivity() {
        Message message = Message.obtain();
        message.what = MainActivity.CODE;
        message.arg1 = i;
        message.arg2 = (isPlaying == true)?1:2;
        try {
            mActivityMessenger.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    class MyBinder extends Binder {

        public void next() {
            i = (i+1)>= mSongs.size()? 0 : i+1;
            mMediaPlayer.stop();
            mMediaPlayer = MediaPlayer.create(MusicPlayerService.this, mSongs.get(i).getSrc());
            if(isPlaying) {
                mMediaPlayer.start();
            }
        }

        public void prev() {
            i = (i-1)<0? mSongs.size()-1:i-1;
            mMediaPlayer.stop();
            mMediaPlayer = MediaPlayer.create(MusicPlayerService.this, mSongs.get(i).getSrc());
            if(isPlaying) {
                mMediaPlayer.start();
            }
        }

        public void play() {
            if(mMediaPlayer.isPlaying()){
                isPlaying =false;
                mMediaPlayer.pause();
            } else {
                isPlaying = true;
                mMediaPlayer.start();
            }
        }

        public int getMusicIndex() {
            return i;
        }
    }
}
