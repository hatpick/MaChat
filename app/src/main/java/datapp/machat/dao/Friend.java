package datapp.machat.dao;

/**
 * Created by hat on 7/6/15.
 */
public class Friend {
    private String name;
    private String fbId;

    public Friend(String name, String fbId) {
        this.name = name;
        this.fbId = fbId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFbId() {
        return fbId;
    }

    public void setFbId(String fbId) {
        this.fbId = fbId;
    }
}
