package eu.telecomnancy.application;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Array;
import java.util.ArrayList;

import eu.telecomnancy.application.App;
import eu.telecomnancy.application.token.KeywordsEnum;
import eu.telecomnancy.application.token.KeywordsToken;
import eu.telecomnancy.application.token.LibraryToken;
import eu.telecomnancy.application.token.StringToken;
import eu.telecomnancy.application.token.Token;
import eu.telecomnancy.application.token.VarToken;

class AppTest {
    @Test void test1Lexer() throws ExceptionLexicale {
        Lexer lexer = new Lexer("src/main/resources/eu/telecomnancy/application/Empty_file.txt");
        assertEquals(0, lexer.getTokens().size()); // le fichier étant vide (0 tokens), les commentaires sont bien retirés
    }

    @Test void test2Lexer() throws ExceptionLexicale {
        Lexer lexer = new Lexer("src/main/resources/eu/telecomnancy/application/Programme_Operations.txt");
        assertEquals(142, lexer.getTokens().size()); // le fichier contient 142 tokens
    }

    @Test void test3Lexer() throws ExceptionLexicale{
        Lexer lexer = new Lexer("src/main/resources/eu/telecomnancy/application/Programme_Sujet.txt");
        ArrayList<Token> tokens = lexer.getTokens();
        assertEquals(129, tokens.size()); // le fichier contient 129 tokens
    }

    @Test void test4Lexer() throws ExceptionLexicale{
        Lexer lexer = new Lexer("src/main/resources/eu/telecomnancy/application/Procedures_sample.txt");
        ArrayList<Token> tokens = lexer.getTokens();
        assertEquals(58, tokens.size()); // le fichier contient 129 tokens
    }
    @Test void test_lexer_tokens() throws ExceptionLexicale{ // ce test vise à valider la nature des tokens (pas uniquement leur nombre) reconnus sur le fichier hello world 
        Lexer lexer = new Lexer("src/main/resources/eu/telecomnancy/application/Hello_World.txt");
        ArrayList<Token> tokens = new ArrayList<Token>();
        tokens.add(new KeywordsToken("with", 1, KeywordsEnum.WITH));
        tokens.add(new LibraryToken("LIB", 1, "Ada.Text_IO"));
        tokens.add(new KeywordsToken(";", 1, KeywordsEnum.SEMICOLON));
        tokens.add(new KeywordsToken("use", 1, KeywordsEnum.USE));
        tokens.add(new LibraryToken("LIB", 1, "Ada.Text_IO"));
        tokens.add(new KeywordsToken(";", 1, KeywordsEnum.SEMICOLON));
        tokens.add(new KeywordsToken("procedure", 3, KeywordsEnum.PROCEDURE));
        tokens.add(new VarToken("VARIABLE", 3, "Hello_World"));
        tokens.add(new KeywordsToken("is", 3, KeywordsEnum.IS));
        tokens.add(new KeywordsToken("begin", 4, KeywordsEnum.BEGIN));
        tokens.add(new KeywordsToken("PUT", 5, KeywordsEnum.PUT));
        tokens.add(new KeywordsToken("(", 5, KeywordsEnum.OPENING_BRACKET));
        tokens.add(new StringToken("ST_CONST", 5, "Hello World"));
        tokens.add(new KeywordsToken(")", 5, KeywordsEnum.CLOSING_BRACKET));
        tokens.add(new KeywordsToken(";", 5, KeywordsEnum.SEMICOLON));
        tokens.add(new KeywordsToken("end", 6, KeywordsEnum.END));
        tokens.add(new VarToken("VARIABLE", 6, "Hello_World"));
        tokens.add(new KeywordsToken(";", 6, KeywordsEnum.SEMICOLON));

        ArrayList<Token> tokens_lexer = lexer.getTokens();
        assertEquals(tokens.size(), tokens_lexer.size()); // le fichier contient 18 tokens
        //comparaison des tokens
        for(int i=0; i<tokens.size(); i++){
            assertEquals(tokens.get(i).getTag(), tokens_lexer.get(i).getTag());
            assertEquals(tokens.get(i).getLine(), tokens_lexer.get(i).getLine());
            if(tokens.get(i) instanceof KeywordsToken){
                assertEquals(((KeywordsToken)tokens.get(i)).getValue(), ((KeywordsToken)tokens_lexer.get(i)).getValue());
            }
            else if(tokens.get(i) instanceof LibraryToken){
                assertEquals(((LibraryToken)tokens.get(i)).getValue(), ((LibraryToken)tokens_lexer.get(i)).getValue());
            }
            else if(tokens.get(i) instanceof StringToken){
                assertEquals(((StringToken)tokens.get(i)).getValue(), ((StringToken)tokens_lexer.get(i)).getValue());
            }
            else if(tokens.get(i) instanceof VarToken){
                assertEquals(((VarToken)tokens.get(i)).getValue(), ((VarToken)tokens_lexer.get(i)).getValue());
            }
        }
    }
}
