package com.viatick.bmsandroidsdk.model;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import com.viatick.bmsandroidsdk.model.ViaBmsUtil.MinisiteType;

/**
 * Created by zanyk on 27/4/18.
 */

public class ViaMinisite implements Parcelable {
    private int id;
    private String title;
    private String description;
    private String coverUrl;
    private String url;
    private MinisiteType type;
    private String deepLink;
    private String notificationImg;
    private int beacon;
    private Integer logId;
    private boolean isVisited;

    public ViaMinisite(int id, String title, String description, String coverUrl, String url,
                       MinisiteType type, String deepLink, String notificationImg, int beacon) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.coverUrl = coverUrl;
        this.url = url;
        this.type = type;
        this.deepLink = deepLink;
        this.notificationImg = notificationImg;
        this.beacon = beacon;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

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

    public MinisiteType getType() {
        return type;
    }

    public void setType(MinisiteType type) {
        this.type = type;
    }

    public String getDeepLink() {
        return deepLink;
    }

    public void setDeepLink(String deepLink) {
        this.deepLink = deepLink;
    }

    public String getNotificationImg() {
        return notificationImg;
    }

    public void setNotificationImg(String notificationImg) {
        this.notificationImg = notificationImg;
    }

    public int getBeacon() {
        return beacon;
    }

    public void setBeacon(int beacon) {
        this.beacon = beacon;
    }

    public Integer getLogId() {
        return logId;
    }

    public void setLogId(Integer logId) {
        this.logId = logId;
    }

    public boolean isVisited() {
        return isVisited;
    }

    public void setVisited(boolean visited) {
        isVisited = visited;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof  ViaMinisite) {
            ViaMinisite minisite =  (ViaMinisite) obj;

            return minisite.getId() == this.id;
        }

        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + this.id;
        return hash;
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
        b.putString("type", type.toString());
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
        type = MinisiteType.valueOf(b.getString("type"));
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
