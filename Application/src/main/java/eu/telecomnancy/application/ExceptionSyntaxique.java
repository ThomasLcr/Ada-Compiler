package eu.telecomnancy.application;

public class ExceptionSyntaxique extends Exception{
    
    public ExceptionSyntaxique(String message) {
        super("Exception syntaxique : " + message);
    }
}
