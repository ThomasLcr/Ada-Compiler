package eu.telecomnancy.application.token;

public class LibraryToken extends Token{
    private String value;

    public LibraryToken(String tag, int line, String value) {
        super(tag, line);
        this.value = value;
    }

    public String getValue() {
        return value;
    }
    
}