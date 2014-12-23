/**
 * Created by danakatz on 11/20/14.
 */
public class Symbol {
    private String type;
    private String kind;
    private int index;

    public Symbol(String t, String k, int i) {
        this.type = t;
        this.kind = k;
        this.index = i;
    }

    public String getType() {
        return this.type;
    }

    public String getKind() {
        return this.kind;
    }

    public int getIndex() {
        return this.index;
    }

}
