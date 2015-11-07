package datapp.machat.dao;

/**
 * Created by hat on 11/5/15.
 */
public class MaChatTheme {
    private String name;
    private int id;
    private int color1;
    private int color;
    private int color2;
    private int refColor;

    public MaChatTheme(String name, int id, int color1, int color2, int color, int refColor) {
        this.name = name;
        this.id = id;
        this.color1 = color1;
        this.color2 = color2;
        this.color = color;
        this.refColor = refColor;
    }

    public int getColor2() {
        return color2;
    }

    public int getColor1() {
        return color1;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getColor() {
        return color;
    }

    public int getRefColor() {
        return refColor;
    }
}
