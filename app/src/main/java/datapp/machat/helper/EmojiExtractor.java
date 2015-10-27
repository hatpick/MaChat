package datapp.machat.helper;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class EmojiExtractor {
    public static ArrayList<TextIndex> extract(String str) throws UnsupportedEncodingException {
        String regexPattern = "[\uD83C-\uDBFF\uDC00-\uDFFF]+";
        byte[] utf8 = str.getBytes("UTF-8");
        ArrayList<TextIndex> output = new ArrayList<>();

        String tmpStr = new String(utf8, "UTF-8");

        Pattern pattern = Pattern.compile(regexPattern);
        Matcher matcher = pattern.matcher(tmpStr);

        int i = 0;
        boolean anyEmoji = false;
        TextIndex ti;

        while (matcher.find()) {
            anyEmoji = true;
            if (matcher.start() != i) {
                ti = new TextIndex(i, matcher.start() - 1, false);
                output.add(ti);
                ti = new TextIndex(matcher.start(), matcher.end(), true);
                output.add(ti);
            } else {
                ti = new TextIndex(matcher.start(), matcher.end(), true);
                output.add(ti);
            }
            i = matcher.end() + 1;
        }

        if(!anyEmoji) {
            ti = new TextIndex(0, str.length() - 1, false);
            output.add(ti);
        } else if(i < str.length() - 1) {
            ti = new TextIndex(i, str.length() - 1, false);
        }

        return output;
    }
}
