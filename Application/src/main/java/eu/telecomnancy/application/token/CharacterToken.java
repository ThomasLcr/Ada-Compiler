package eu.telecomnancy.application.token;

public class CharacterToken extends Token{
    private String value;

    public CharacterToken(String tag, int line, String value) {
        super(tag, line);
        this.value = value;
    }

    public String getValue() {
        return value;
    }  
}
