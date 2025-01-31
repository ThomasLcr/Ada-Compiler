package eu.telecomnancy.application.token;

public class BooleanToken extends Token {
    private KeywordsEnum value;

    public BooleanToken(String tag, int line, KeywordsEnum value) {
        super(tag, line);
        this.value = value;
    }

    public KeywordsEnum getValue() {
        return value;
    }
}
