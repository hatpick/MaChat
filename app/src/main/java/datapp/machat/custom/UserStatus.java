package datapp.machat.custom;

import com.parse.ParseUser;

/**
 * Created by hat on 7/17/15.
 */
public class UserStatus {
    private static ParseUser parseUser = ParseUser.getCurrentUser();

    public static void setUserOnline() {
        if(parseUser != null) {
            ParseUser.getCurrentUser().put("inApp", true);
            ParseUser.getCurrentUser().saveEventually();
        }
    }

    public static void setUserOffline() {
        if(parseUser != null) {
            ParseUser.getCurrentUser().put("inApp", false);
            ParseUser.getCurrentUser().saveEventually();
        }
    }
}
