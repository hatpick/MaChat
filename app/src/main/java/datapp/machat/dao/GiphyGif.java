package datapp.machat.dao;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by hat on 7/15/15.
 */
public class GiphyGIF implements Parcelable {
    private String id;
    private int width;
    private int height;
    private String rating;
    private String smallSizedUrl;

    public GiphyGIF(String id, int width, int height, String rating, String smallSizedUrl) {
        this.id = id;
        this.width = width;
        this.height = height;
        this.rating = rating;
        this.smallSizedUrl = smallSizedUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getSmallSizedUrl() {
        return smallSizedUrl;
    }

    public void setSmallSizedUrl(String smallSizedUrl) {
        this.smallSizedUrl = smallSizedUrl;
    }

    protected GiphyGIF(Parcel in) {
        id = in.readString();
        width = in.readInt();
        height = in.readInt();
        rating = in.readString();
        smallSizedUrl = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeInt(width);
        dest.writeInt(height);
        dest.writeString(rating);
        dest.writeString(smallSizedUrl);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<GiphyGIF> CREATOR = new Parcelable.Creator<GiphyGIF>() {
        @Override
        public GiphyGIF createFromParcel(Parcel in) {
            return new GiphyGIF(in);
        }

        @Override
        public GiphyGIF[] newArray(int size) {
            return new GiphyGIF[size];
        }
    };
}
