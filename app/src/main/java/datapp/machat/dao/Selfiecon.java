package datapp.machat.dao;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by hat on 7/10/15.
 */
public class Selfiecon implements Parcelable {
    private String id;
    private String gifUrl;
    private String thumbnailUrl;

    public Selfiecon(String id, String gifUrl, String thumbnailUrl) {
        this.id = id;
        this.gifUrl = gifUrl;
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getGifUrl() {
        return gifUrl;
    }

    public void setGifUrl(String gifUrl) {
        this.gifUrl = gifUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    protected Selfiecon(Parcel in) {
        id = in.readString();
        gifUrl = in.readString();
        thumbnailUrl = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(gifUrl);
        dest.writeString(thumbnailUrl);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Selfiecon> CREATOR = new Parcelable.Creator<Selfiecon>() {
        @Override
        public Selfiecon createFromParcel(Parcel in) {
            return new Selfiecon(in);
        }

        @Override
        public Selfiecon[] newArray(int size) {
            return new Selfiecon[size];
        }
    };
}