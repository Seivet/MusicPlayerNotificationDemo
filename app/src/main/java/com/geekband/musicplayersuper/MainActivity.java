package com.geekband.musicplayersuper;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private TextView mMusicName;
    private TextView mSingerName;
    private ImageButton mNextBtn;
    private ImageButton mPlayBtn;
    private ImageButton mPrevBtn;
    private MusicPlayerDao mMusicPlayerDao;
    private ArrayList<MusicPlayerPojo> mSongs;
    private boolean isPlaying = false;
    private int mIndex = 0;
    private Messenger mMessenger;

    public static final int CODE = 1;

    private  Messenger mActivityMessenger = new Messenger(new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case CODE:
                    mIndex = msg.arg1;
                    setNames(mIndex);
                    if(msg.arg2 == 1) {
                        mPlayBtn.setBackgroundResource(R.drawable.music_pause);
                    } else {
                        mPlayBtn.setBackgroundResource(R.drawable.music_play);
                    }
            }
        }
    });


    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            mMessenger = new Messenger(service);

            Message message = Message.obtain();
            message.replyTo = mActivityMessenger;
            try {
                mMessenger.send(message);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = new Intent(this, MusicPlayerService.class);

        bindService(intent, mServiceConnection, BIND_AUTO_CREATE);

        mMusicPlayerDao = MusicPlayerDao.getMusicList();
        mSongs = mMusicPlayerDao.getList();

        mMusicName = (TextView) findViewById(R.id.music_name);
        mSingerName = (TextView) findViewById(R.id.singer_name);
        mNextBtn = (ImageButton) findViewById(R.id.btn_next);
        mPlayBtn = (ImageButton) findViewById(R.id.btn_play);
        mPrevBtn = (ImageButton) findViewById(R.id.btn_prev);

        setNames(mIndex);

        if(mMessenger != null) {
            Message message = Message.obtain(null, 0);
            try {
                mMessenger.send(message);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        mPlayBtn.setOnClickListener(this);
        mPrevBtn.setOnClickListener(this);
        mNextBtn.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_play:
                if(isPlaying) {
                    if(mMessenger != null) {
                        Message message = Message.obtain(null, 1);
                        message.arg1 = 1;
                        message.arg2 = mIndex;
                        message.replyTo = mActivityMessenger;
                        try {
                            mMessenger.send(message);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                    mPlayBtn.setBackgroundResource(R.drawable.music_play);
                    isPlaying = false;
                } else {
                    if(mMessenger != null) {
                        Message message = Message.obtain(null, 1);
                        message.arg1 = 0;
                        message.arg2 = mIndex;
                        message.replyTo = mActivityMessenger;
                        try {
                            mMessenger.send(message);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                    mPlayBtn.setBackgroundResource(R.drawable.music_pause);
                    isPlaying = true;
                }
                break;

            case R.id.btn_prev:
                mIndex = (mIndex-1)<0? mSongs.size()-1:mIndex-1;
                setNames(mIndex);
                if(mMessenger != null) {
                    Message message = Message.obtain(null, 0);
                    message.arg1 =mIndex;
                    message.replyTo = mActivityMessenger;
                    try {
                        mMessenger.send(message);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                break;

            case R.id.btn_next:
                mIndex = (mIndex+1)>= mSongs.size()? 0 : mIndex+1;
                setNames(mIndex);
                if(mMessenger != null) {
                    Message message = Message.obtain(null, 0);
                    message.arg1 = mIndex;
                    message.replyTo = mActivityMessenger;
                    try {
                        mMessenger.send(message);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }

    }

    private void setNames(int index) {
        mMusicName.setText(mSongs.get(index).getName());
        mSingerName.setText(mSongs.get(index).getSinger());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
    }
}
