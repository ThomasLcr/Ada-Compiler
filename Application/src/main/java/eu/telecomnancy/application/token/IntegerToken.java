package eu.telecomnancy.application.token;

public class IntegerToken extends Token{ 

    private int value;

    public IntegerToken(String tag, int line, String value) {
        super(tag, line);
        this.value = Integer.parseInt(value);
    }

    public int getValue() {
        return value;
    }   
    
}
