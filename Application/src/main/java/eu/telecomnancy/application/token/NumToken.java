package eu.telecomnancy.application.token;

public class NumToken extends Token{ // token pour les nombres flottants et entiers
    private Boolean isFloat;
    private int intValue;
    private float floatValue;

    public NumToken(String tag, int line, String num) { //overload
        super(tag, line);
        if(num.contains(".")){
            this.floatValue = Float.parseFloat(num);
            this.isFloat = true;
        }else{
            this.intValue = Integer.parseInt(num);
            this.isFloat = false;
        }
    }

    public Number getValue() {
        if(isFloat){
            return floatValue;
        }else{
            return intValue;
        }
    }

    public boolean isFloat(){
        return isFloat;
    }
    
}
