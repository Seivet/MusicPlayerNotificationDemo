package com.geekband.musicplayersuper;

import java.util.ArrayList;


public class MusicPlayerDao {

    private volatile static MusicPlayerDao sMusicList;
    private final ArrayList<MusicPlayerPojo> mMusicDatas;

    private MusicPlayerDao(){
        mMusicDatas = new ArrayList<>();
        mMusicDatas.add(new MusicPlayerPojo(R.raw.song_00,"beautiful", "oh sunshine"));
        mMusicDatas.add(new MusicPlayerPojo(R.raw.song_01, "梦追人", "KOKIA"));
        mMusicDatas.add(new MusicPlayerPojo(R.raw.song_02, "prisoner of love", "宇多田光"));
    }

    public static MusicPlayerDao getMusicList() {
        if (sMusicList == null) {
            synchronized (MusicPlayerDao.class) {
                if (sMusicList == null) {
                    sMusicList = new MusicPlayerDao();
                }
            }
        }
        return sMusicList;
    }

    public ArrayList<MusicPlayerPojo> getList() {
        return mMusicDatas;
    }
    public MusicPlayerPojo getMusicData(int i) {
        return mMusicDatas.get(i);
    }
}
