package com.example.android.miwok;

import android.net.Uri;

public class Word {
    String defaultTranslation;
    String miwokTranslation;
    int imageId;
    int media;

    public Word(String defaultTranslation,String miwokTranslation,int media){
        this.defaultTranslation=defaultTranslation;
        this.miwokTranslation=miwokTranslation;
        this.media=media;
    }

    public Word(String defaultTranslation,String miwokTranslation,int media,int imageId){
        this.defaultTranslation=defaultTranslation;
        this.miwokTranslation=miwokTranslation;
        this.media=media;
        this.imageId=imageId;
    }

    public String getDefaultTranslation() {
        return defaultTranslation;
    }

    public String getMiwokTranslation() {
        return miwokTranslation;
    }

    public int getImageId() {
        return imageId;
    }

    public int getMedia() {
        return media;
    }
}
