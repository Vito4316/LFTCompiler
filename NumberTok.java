public class NumberTok extends Token {
    int num;
    public NumberTok(int tag, String num) {
        super(tag);
        this.num = Integer.parseInt(num);
    }

    public NumberTok(int tag, int num) {
        super(tag);
        this.num = num;
    }

    @Override
    public String toString() {return "<" + tag + ", " + num + ">";}
}