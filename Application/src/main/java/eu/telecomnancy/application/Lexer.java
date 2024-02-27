package eu.telecomnancy.application;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;

import eu.telecomnancy.application.token.KeywordsEnum;
import eu.telecomnancy.application.token.KeywordsToken;
import eu.telecomnancy.application.token.LibraryToken;
import eu.telecomnancy.application.token.NumToken;
import eu.telecomnancy.application.token.OperatorEnum;
import eu.telecomnancy.application.token.OperatorToken;
import eu.telecomnancy.application.token.StringToken;
import eu.telecomnancy.application.token.Token;
import eu.telecomnancy.application.token.VarToken;

public class Lexer {
    private String sourceFile; // chemin du fichier source (à compiler)
    private static String currentLine; // contenu de la ligne courante
    private static int currentCharIndex; // index du prochaine charactère à lire dans la ligne courante
    private static int currentLineIndex; // index de la ligne courante
    private static BufferedReader reader; // lecteur du fichier source
    
    public Lexer(String filepath) {
        this.sourceFile = filepath;
        System.out.println("Creation du Lexer");
        Lexer.currentLineIndex=0; 
        Lexer.currentLine = "";
        Lexer.currentCharIndex = -1;
        try { // vérifie que le fichier source existe
            Lexer.reader = new BufferedReader(new FileReader(filepath));
            System.out.println("Hi");
            if (Lexer.reader == null){
                System.out.println("Iciiiiiiiii");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static char getNextChar() { //retourne le prochain charactère à lire dans le fichier source
        currentCharIndex++;
        if(currentLine == null){ // on a terminé la lecture du fichier
            return '\0'; //fin de la ligne
        }
        if(currentLine.length() == currentCharIndex || currentCharIndex==-1){// on a terminé la lecture de la ligne
            try {
                currentLine = reader.readLine();
                if(currentLine != null){ // on retire les commentaires
                    currentLine = currentLine.split("--")[0];
                }
                currentLineIndex ++;
                currentCharIndex = -1;
                return getNextChar();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return currentLine.charAt(currentCharIndex);
    }

    public static String peekNextChar(){
        if(currentCharIndex+1 == currentLine.length()){
            return "\n";
        }else{
            return String.valueOf(currentLine.charAt(currentCharIndex+1));
        }
    }

    public ArrayList<Token> getTokens() throws ExceptionLexicale{
        String chaine = "";
        System.out.println("Recuperation des tokens");
        char c = getNextChar();
        ArrayList<Token> resultToken = new ArrayList<>();
        String[] wordBreak = {"\n", " ", ";","(",")","+","-","*","/","=",",","<",">","{","}",":"};
        while(c != '\0'){
            chaine = chaine + c;
            //debut du switch case pour chaque mot cle
            switch(chaine){

                case " ","\n":
                    //on fait quelque chose
                    chaine = "";
                break;

                case "access" : 
                    if(Arrays.asList(wordBreak).contains(peekNextChar())){
                        resultToken.add(new KeywordsToken("access", currentLineIndex, KeywordsEnum.ACCESS));
                        chaine = "";
                    }
                break;

                case "begin" : 
                    if(Arrays.asList(wordBreak).contains(peekNextChar())){
                        resultToken.add(new KeywordsToken("begin", currentLineIndex, KeywordsEnum.BEGIN));
                        chaine = "";
                    }
                break;

                case "else" : 
                    if(Arrays.asList(wordBreak).contains(peekNextChar())){
                        resultToken.add(new KeywordsToken("else", currentLineIndex, KeywordsEnum.ELSE));
                        chaine = "";
                    }
                break;

                case "elsif" : 
                    if(Arrays.asList(wordBreak).contains(peekNextChar())){
                        resultToken.add(new KeywordsToken("elsif", currentLineIndex, KeywordsEnum.ELSIF));
                        chaine = "";
                    }
                break;
                
                case "end" : 
                    if(Arrays.asList(wordBreak).contains(peekNextChar())){
                        resultToken.add(new KeywordsToken("end", currentLineIndex, KeywordsEnum.END));
                        chaine = "";
                    }
                break;

                case "false" : 
                    if(Arrays.asList(wordBreak).contains(peekNextChar())){
                        resultToken.add(new KeywordsToken("false", currentLineIndex, KeywordsEnum.FALSE));
                        chaine = "";
                    }
                break;

                case "for" : 
                    if(Arrays.asList(wordBreak).contains(peekNextChar())){
                        resultToken.add(new KeywordsToken("for", currentLineIndex, KeywordsEnum.FOR));
                        chaine = "";
                    }
                break;

                case "function" : 
                    if(Arrays.asList(wordBreak).contains(peekNextChar())){
                        resultToken.add(new KeywordsToken("function", currentLineIndex, KeywordsEnum.FUNCTION));
                        chaine = "";
                    }
                break;

                case "if" : 
                    if(Arrays.asList(wordBreak).contains(peekNextChar())){
                        resultToken.add(new KeywordsToken("if", currentLineIndex, KeywordsEnum.IF));
                        chaine = "";
                    }
                break;

                case "in" : 
                    if(Arrays.asList(wordBreak).contains(peekNextChar())){
                        resultToken.add(new KeywordsToken("in", currentLineIndex, KeywordsEnum.IN));
                        chaine = "";
                    }
                break;

                case "is" : 
                    if(Arrays.asList(wordBreak).contains(peekNextChar())){
                        resultToken.add(new KeywordsToken("is", currentLineIndex, KeywordsEnum.IS));
                        chaine = "";
                    }
                break;

                case "loop" : 
                    if(Arrays.asList(wordBreak).contains(peekNextChar())){
                        resultToken.add(new KeywordsToken("loop", currentLineIndex, KeywordsEnum.LOOP));
                        chaine = "";
                    }
                break;

                case "new" : 
                    if(Arrays.asList(wordBreak).contains(peekNextChar())){
                        resultToken.add(new KeywordsToken("new", currentLineIndex, KeywordsEnum.NEW));
                        chaine = "";
                    }
                break;

                case "null" : 
                    if(Arrays.asList(wordBreak).contains(peekNextChar())){
                        resultToken.add(new KeywordsToken("null", currentLineIndex, KeywordsEnum.NULL));
                        chaine = "";
                    }
                break;

                case "out" : 
                    if(Arrays.asList(wordBreak).contains(peekNextChar())){
                        resultToken.add(new KeywordsToken("out", currentLineIndex, KeywordsEnum.OUT));
                        chaine = "";
                    }
                break;

                case "procedure" : 
                    if(Arrays.asList(wordBreak).contains(peekNextChar())){
                        resultToken.add(new KeywordsToken("procedure", currentLineIndex, KeywordsEnum.PROCEDURE));
                        chaine = "";
                    }
                break;
                
                case "Put" : 
                    if(Arrays.asList(wordBreak).contains(peekNextChar())){
                        resultToken.add(new KeywordsToken("put", currentLineIndex, KeywordsEnum.PUT));
                        chaine = "";
                    }
                break;

                case "Put_Line" : 
                    if(Arrays.asList(wordBreak).contains(peekNextChar())){
                        resultToken.add(new KeywordsToken("put_line", currentLineIndex, KeywordsEnum.PUT));
                        chaine = "";
                    }
                break;
                
                case "record" : 
                    if(Arrays.asList(wordBreak).contains(peekNextChar())){
                        resultToken.add(new KeywordsToken("record", currentLineIndex, KeywordsEnum.RECORD));
                        chaine = "";
                    }
                break;
                
                case "rem" : 
                    if(Arrays.asList(wordBreak).contains(peekNextChar())){
                        resultToken.add(new OperatorToken("rem", currentLineIndex, OperatorEnum.REM));
                        chaine = "";
                    }
                break;

                case "return" : 
                    if(Arrays.asList(wordBreak).contains(peekNextChar())){
                        resultToken.add(new KeywordsToken("return", currentLineIndex, KeywordsEnum.RETURN));
                        chaine = "";
                    }
                break;
                
                case "reverse" : 
                    if(Arrays.asList(wordBreak).contains(peekNextChar())){
                        resultToken.add(new KeywordsToken("reverse", currentLineIndex, KeywordsEnum.REVERSE));
                        chaine = "";
                    }
                break;

                case "then" : 
                    if(Arrays.asList(wordBreak).contains(peekNextChar())){
                        resultToken.add(new KeywordsToken("then", currentLineIndex, KeywordsEnum.THEN));
                        chaine = "";
                    }
                break;
                
                case "true" : 
                    if(Arrays.asList(wordBreak).contains(peekNextChar())){
                        resultToken.add(new KeywordsToken("true", currentLineIndex, KeywordsEnum.TRUE));
                        chaine = "";
                    }
                break;
                
                case "type" : 
                    if(Arrays.asList(wordBreak).contains(peekNextChar())){
                        resultToken.add(new KeywordsToken("type", currentLineIndex, KeywordsEnum.TYPE));
                        chaine = "";
                    }
                break;

                case "use" : 
                    if(Arrays.asList(wordBreak).contains(peekNextChar())){
                        resultToken.add(new KeywordsToken("use", currentLineIndex, KeywordsEnum.USE));
                        chaine = "";
                    }
                break;
                
                case "while" : 
                    if(Arrays.asList(wordBreak).contains(peekNextChar())){
                        resultToken.add(new KeywordsToken("while", currentLineIndex, KeywordsEnum.WHILE));
                        chaine = "";
                    }
                break;
                
                case "with" : 
                    if(Arrays.asList(wordBreak).contains(peekNextChar())){
                        resultToken.add(new KeywordsToken("with", currentLineIndex, KeywordsEnum.WITH));
                        chaine = "";
                    }
                break;

                case "character'val" : 
                    if(Arrays.asList(wordBreak).contains(peekNextChar())){
                        resultToken.add(new KeywordsToken("character'val", currentLineIndex, KeywordsEnum.CHARACTER));
                        chaine = "";
                    }
                break;

                case "(":
                            resultToken.add(new KeywordsToken("(", currentLineIndex, KeywordsEnum.OPENING_BRACKET));
                            chaine = "";
                    break;

                case ")":
                            resultToken.add(new KeywordsToken(")", currentLineIndex, KeywordsEnum.CLOSING_BRACKET));
                            chaine = "";
                    break;
                case ":":
                            resultToken.add(new OperatorToken(":", currentLineIndex, OperatorEnum.TYPE_SPECIFIER));
                            chaine = "";
                    break;

                case "=":
                            resultToken.add(new OperatorToken("=", currentLineIndex, OperatorEnum.EQUAL));
                            chaine = "";
                    break;
                case "+":
                            resultToken.add(new OperatorToken("+", currentLineIndex, OperatorEnum.PLUS));
                            chaine = "";
                    break;
                case "-":
                            resultToken.add(new OperatorToken("-", currentLineIndex, OperatorEnum.MINUS));
                            chaine = "";
                    break;
                
                case "*":
                            resultToken.add(new OperatorToken("*", currentLineIndex, OperatorEnum.MULT));
                            chaine = "";
                    break;
                
                case "/":
                    if(peekNextChar().equals("=")){
                        resultToken.add(new OperatorToken("/=", currentLineIndex, OperatorEnum.NOT_EQUAL));
                        chaine = "";
                        c = getNextChar();
                    }else{
                            resultToken.add(new OperatorToken("/", currentLineIndex, OperatorEnum.DIV));
                            chaine = "";
                    }
                    break;
                case "or":
                    if(Arrays.asList(wordBreak).contains(peekNextChar())){
                        resultToken.add(new OperatorToken("or", currentLineIndex, OperatorEnum.OR));
                        chaine = "";
                    }
                    break;
                case "and":
                    if(Arrays.asList(wordBreak).contains(peekNextChar())){
                        resultToken.add(new OperatorToken("and", currentLineIndex, OperatorEnum.AND));
                        chaine = "";
                    }
                    break;
                case "not":
                    if(Arrays.asList(wordBreak).contains(peekNextChar())){
                        resultToken.add(new OperatorToken("not", currentLineIndex, OperatorEnum.NOT));
                        chaine = "";
                    }
                    break;

                case "<":
                    if(peekNextChar().equals("=")){
                        resultToken.add(new OperatorToken("<=", currentLineIndex, OperatorEnum.INF));
                        chaine = "";
                        c=getNextChar();
                    }else{
                        resultToken.add(new OperatorToken("<", currentLineIndex, OperatorEnum.STRICT_INF));
                        chaine = "";
                    }
                    break;
                case ">":
                    if(peekNextChar().equals("=")){
                        resultToken.add(new OperatorToken(">=", currentLineIndex, OperatorEnum.SUP));
                        chaine = "";
                        c=getNextChar();
                    }else{
                        resultToken.add(new OperatorToken(">", currentLineIndex, OperatorEnum.STRICT_SUP));
                        chaine = "";
                    }
                    break;
                case "..":
                            resultToken.add(new KeywordsToken("DOTDOT", currentLineIndex, KeywordsEnum.DOTDOT));
                            chaine = "";
                    break;
                case ",":
                            resultToken.add(new KeywordsToken(",", currentLineIndex, KeywordsEnum.COMA));
                            chaine = "";
                    break;
                case ";":
                            resultToken.add(new KeywordsToken(";", currentLineIndex, KeywordsEnum.SEMICOLON));
                            chaine = "";
                    break;
                case ".":
                            resultToken.add(new KeywordsToken(".", currentLineIndex, KeywordsEnum.DOT));
                            chaine = "";
                    break;
                default:
                if (chaine.matches("[a-z|A-Z]\\w*")) {
                    if(Arrays.asList(wordBreak).contains(peekNextChar())){ // le nom de variable est terminé
                        resultToken.add(new VarToken("VARIABLE",currentLineIndex,chaine));
                        chaine = "";
                    }
                
                } else if(chaine.matches("[0-9]+(\\.[0-9]*)?")){
                    if(Arrays.asList(wordBreak).contains(peekNextChar())){ // le nombre est terminé
                        resultToken.add(new NumToken("NUM_CONST", currentLineIndex, Double.parseDouble(chaine)));
                        chaine = "";
                    }
                }else if(chaine.matches("[0-9]+\\.\\.[0-9]*")){
                    if(Arrays.asList(wordBreak).contains(peekNextChar())){ // la range est terminée
                        resultToken.add(new NumToken("NUM_CONST", currentLineIndex, Double.parseDouble(chaine.split("\\.\\.")[0])));
                        resultToken.add(new KeywordsToken("DOTDOT", currentLineIndex, KeywordsEnum.DOTDOT));
                        resultToken.add(new NumToken("NUM_CONST", currentLineIndex, Double.parseDouble(chaine.split("\\.\\.")[1])));
                        chaine = "";
                        
                    }
                    
                } else if(chaine.matches("\".*\"")){
                        resultToken.add(new StringToken("STR_CONST", currentLineIndex, chaine));
                        chaine = "";
                
                } else if (chaine.matches("Ada.*")){
                    if(Arrays.asList(wordBreak).contains(peekNextChar())){
                        resultToken.add(new LibraryToken("LIB", currentLineIndex, chaine));
                        chaine = "";
                    }
                
                }else if(chaine.matches("([a-z|A-Z]+[0-9|_]*)*\\.([a-z|A-Z]+[0-9|_]*)*")){ //chaines du type Nom.Nom
                    if(Arrays.asList(wordBreak).contains(peekNextChar())){
                        resultToken.add(new VarToken("VARIABLE",currentLineIndex,chaine.split("\\.")[0]));
                        resultToken.add(new KeywordsToken("DOT", currentLineIndex, KeywordsEnum.DOT));
                        resultToken.add(new VarToken("VARIABLE",currentLineIndex,chaine.split("\\.")[1]));
                        chaine = "";
                    }
                    
                }else{
                    if(Arrays.asList(wordBreak).contains(peekNextChar()) && !chaine.contains("\"")){
                        throw new ExceptionLexicale("Mot inconnu ligne "+currentLineIndex+" : "+chaine);
                    }
                }
                    ;         
            } 
            c = getNextChar();
        }
        return resultToken;
    }
}