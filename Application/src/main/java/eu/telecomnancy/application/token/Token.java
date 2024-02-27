package eu.telecomnancy.application.token;

public abstract class Token {
    protected String tag;
    protected int line;


    public Token(String tag, int line) {
        this.tag = tag;
        this.line = line;
    }

    public String getTag() {
        return tag;
    }

    public int getLine() {
        return line;
    }

    public void printToken(){
        System.out.print(tag+" ");
    }
}
