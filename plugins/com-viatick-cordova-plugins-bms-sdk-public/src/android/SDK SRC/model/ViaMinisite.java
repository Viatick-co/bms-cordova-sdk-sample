package com.viatick.bmsandroidsdk.model;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by zanyk on 27/4/18.
 */

public class ViaMinisite implements Parcelable {
    private String title;
    private String description;
    private String coverUrl;
    private String url;
    private String type;
    private boolean isVisited;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ViaMinisite (String title, String description, String coverUrl, String url, String type) {
        this.title = title;
        this.description = description;
        this.coverUrl = coverUrl;
        this.url = url;
        this.type = type;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        Bundle b = new Bundle();
        b.putString("title",title);
        b.putString("text",description);
        b.putString("type",type);
        b.putString("coverUrl",coverUrl);
        b.putString("url",url.toString());
        b.putBoolean("isVisited",isVisited);
        dest.writeBundle(b);
    }

    protected ViaMinisite(Parcel in) {
        Bundle b = in.readBundle();
        title = b.getString("title");
        description = b.getString("text");
        coverUrl = b.getString("coverUrl");
        url = b.getString("url");
        isVisited = b.getBoolean("isVisited");
        type = b.getString("type");
    }

    public static final Creator<ViaMinisite> CREATOR = new Creator<ViaMinisite>() {
        @Override
        public ViaMinisite createFromParcel(Parcel in) {
            return new ViaMinisite(in);
        }

        @Override
        public ViaMinisite[] newArray(int size) {
            return new ViaMinisite[size];
        }
    };
}
