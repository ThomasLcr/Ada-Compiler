package eu.telecomnancy.application;

public class ExceptionLexicale extends Exception{
    
    public ExceptionLexicale(String message) {
        super("Exception lexicale : " + message);
    }
}
