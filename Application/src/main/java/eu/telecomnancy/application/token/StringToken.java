package eu.telecomnancy.application.token;

public class StringToken extends Token{
    private String value;

    public StringToken(String tag, int line, String value) {
        super(tag, line);
        this.value = value;
    }

    public String getValue() {
        return value;
    }   
}
