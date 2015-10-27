package datapp.machat.helper;

/**
 * Created by hat on 10/26/15.
 */
public class TextIndex {
    private int start;
    private int end;
    private boolean emoji;

    public TextIndex(int start, int end, boolean emoji) {
        this.start = start;
        this.end = end;
        this.emoji = emoji;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public boolean isEmoji() {
        return emoji;
    }
}
