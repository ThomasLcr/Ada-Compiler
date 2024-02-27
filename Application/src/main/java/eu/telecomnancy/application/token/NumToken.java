package eu.telecomnancy.application.token;

public class NumToken extends Token{ // token pour les nombres flottants et entiers
    private Boolean isFloat;
    private int intValue;
    private float floatValue;

    public NumToken(String tag, int line, Number num) { //overload
        super(tag, line);
        if (num instanceof Float || num instanceof Double) {
            isFloat = true;
            floatValue = num.floatValue();
        } else {
            isFloat = false;
            intValue = num.intValue();
        }
    }

    public Number getValue() {
        if(isFloat){
            return floatValue;
        }else{
            return intValue;
        }
    }
    
}
