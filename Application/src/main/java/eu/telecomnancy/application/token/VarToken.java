package eu.telecomnancy.application.token;

public class VarToken extends Token {
    private String identifier;

    public VarToken(String tag, int line, String value) {
        super(tag, line);
        this.identifier = value;
        if (isType()) {
            //System.out.println("C'est un type");
        }
    }

    public String getValue() {
        return identifier;
    }

    public boolean isType(){
        String id = this.identifier.toLowerCase();
        if(id.equals("integer") || id.equals("float") || id.equals("string") || id.equals("character") || id.equals("boolean")){
            this.identifier = id;
            return true;
        }
        return false;
    }
}
