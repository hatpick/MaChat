package datapp.machat.helper;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by hat on 7/12/15.
 */
public class NotificationId {
    private final static AtomicInteger c = new AtomicInteger(0);
    public static int getID() {
        return c.incrementAndGet();
    }
}
