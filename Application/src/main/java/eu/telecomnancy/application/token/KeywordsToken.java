package eu.telecomnancy.application.token;

public class KeywordsToken extends Token {
    private KeywordsEnum value;

    public KeywordsToken(String tag, int line, KeywordsEnum value) {
        super(tag, line);
        this.value = value;
    }

    public KeywordsEnum getValue() {
        return value;
    }

}
