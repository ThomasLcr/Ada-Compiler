package eu.telecomnancy.application.token;

public class FloatToken extends Token{ 
    
    private float value;

    public FloatToken(String tag, int line, String value) {
        super(tag, line);
        this.value = Float.parseFloat(value);
    }

    public float getValue() {
        return value;
    }   
}
