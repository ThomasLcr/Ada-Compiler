package eu.telecomnancy.application.token;

public class VarToken extends Token {
    private String identifier;

    public VarToken(String tag, int line, String value) {
        super(tag, line);
        this.identifier = value;
    }

    public String getValue() {
        return identifier;
    }    
}
