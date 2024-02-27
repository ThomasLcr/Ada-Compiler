package eu.telecomnancy.application.token;

public class OperatorToken extends Token{
    private OperatorEnum operator;
    public OperatorToken(String value, int line, OperatorEnum operator) {
        super(value, line);
        this.operator = operator;
    }

}
