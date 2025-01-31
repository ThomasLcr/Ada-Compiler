package eu.telecomnancy.application;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

// import org.checkerframework.checker.guieffect.qual.SafeEffect;
// import org.checkerframework.checker.units.qual.s;
// import org.checkerframework.checker.units.qual.t;

import eu.telecomnancy.application.token.Token;
import eu.telecomnancy.application.exception.ExceptionLexicale;
import eu.telecomnancy.application.exception.ExceptionSemantique;
import eu.telecomnancy.application.exception.ExceptionSyntaxique;
 
public class Parser 
{
    private ArrayList<Token> tokens;
    private int currentTokenIndex = 0;

    public Parser(ArrayList<Token> tokens) 
    {
        System.out.println("Initialisation du parser");
        System.out.println("Nombre de tokens: " + tokens.size());
        this.tokens = tokens;
        this.currentTokenIndex = 0;
    }
    
    public Token getNextToken() //Fonction permettant de récupérer le token suivant
    {
        if (currentTokenIndex < this.tokens.size()) 
        {
            return this.tokens.get(currentTokenIndex);
        } 
        else 
        {
            return null;
        }
    }
    
    public void advanceToken() //Fonction permettant d'avancer dans la liste des tokens
    {
        currentTokenIndex++;
    }

    public void decrementToken() //Fonction permettant de reculer dans la liste des tokens
    {
        currentTokenIndex--;
    }

    public String tokenTag()
    {
        if (getNextToken() == null)
        {
            return "Erreur";
        }
        else
        {
            return getNextToken().getTag();
        }
    }

    public void parse() throws ExceptionSyntaxique
    {
        //On démarre par l'analyse de l'axiome
        axiome();
    }

    public void axiome() throws ExceptionSyntaxique {
        System.out.println("axiome");
        //Pour l'axiome, on vérifie que le premier token est bien un "with"
        if (tokenTag() == "with") {
            advanceToken();
        } else {
            throw new ExceptionSyntaxique("'with' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    
        //On vérifie que le token suivant est bien une librairie
        if (tokenTag() == "LIB") {
            advanceToken();
        } else {
            throw new ExceptionSyntaxique("'Librairie' attendue");
        }

        //On vérifie que le token suivant est bien un ";"
        if (tokenTag() == ";") {
            advanceToken();
        } else {
            throw new ExceptionSyntaxique("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    
        //On vérifie que le token suivant est bien un "use"
        if (tokenTag() == "use") {
            advanceToken();
        } else {
            throw new ExceptionSyntaxique("'use' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    
        //On vérifie que le token suivant est bien une librairie
        if (tokenTag() == "LIB") {
            advanceToken();
        } else {
            throw new ExceptionSyntaxique("'Librairie' attendue");
        }
 
        //On vérifie que le token suivant est bien un ";"
        if (tokenTag() == ";") {
            advanceToken();
        } else {
            throw new ExceptionSyntaxique("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    
        //On vérifie que le token suivant est bien un début de procedure
        if (tokenTag() == "procedure") {
            advanceToken();
        } else {
            throw new ExceptionSyntaxique("'procedure' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }

        //On vérifie que le token suivant est bien l'identificateur de la procedure
        if (tokenTag() == "VARIABLE") {
            advanceToken();
        } else {
            throw new ExceptionSyntaxique("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }

        //On vérifie que le token suivant est bien un "is" et on passe dans la règle fichier2
        if (tokenTag() == "is") {
            advanceToken();
            fichier2();
        } else {
            throw new ExceptionSyntaxique("'is' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }

    public void fichier2() throws ExceptionSyntaxique {
        System.out.print("fichier2 : ");
        // Si le token suivant est un "begin", on est bien dans la première réduction de la règle fichier2 et on passe dans la règle instructionPlus
        if (tokenTag() == "begin") {
            System.out.println("r2");
            advanceToken();
            instructionPlus();
            if (tokenTag() == "end") {
                advanceToken();
                fichier3();
                return;
            } else {
                throw new ExceptionSyntaxique("'end' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        } 
        // Sinon, on est dans la deuxième réduction de la règle fichier2, le prochain token doit être un "type" ou un "procedure" ou un "function"
        // On passe donc dans la règle declarationPlus sans avancer la lecture des tokens
        else if (tokenTag() == "type" || tokenTag() == "procedure" || tokenTag() == "function") {
            System.out.println("r3");
            declarationPlus();
            if (tokenTag() == "begin") {
                advanceToken();
                instructionPlus();
                if (tokenTag() == "end") {
                    advanceToken();
                    fichier3();
                    return;
                } else {
                    throw new ExceptionSyntaxique("'end' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                }
            } else {
                throw new ExceptionSyntaxique("'begin' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        } else {
            throw new ExceptionSyntaxique("'begin', 'type', 'procedure' ou 'function' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }

    public void fichier3() throws ExceptionSyntaxique {
        System.out.print("fichier3 : ");
        // S'il n'y a plus de token, on est bien dans la première réduction de la règle fichier3, c'est la fin du fichier
        if (tokenTag()== ";") {
            advanceToken();
            System.out.println("r4");
            System.out.println("Fin de fichier");
            return;
        } 
        //Sinon, le prochain token doit être l'identificateur de la procedure et on rappelle fichier3 pour fermer la procedure principale
        else if (tokenTag() == "VARIABLE") {
            System.out.println("r5");
            advanceToken();
            if (tokenTag() == ";") {
                advanceToken();
            } else {
                throw new ExceptionSyntaxique("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        } else {
            throw new ExceptionSyntaxique("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }

    //A partir d'ici certaine méthodes ne sont pas complètes, je les fait dans l'ordre dans lesquelles je les rencontre

    public void declaration() throws ExceptionSyntaxique {
        System.out.print("declaration : ");
        if (tokenTag() == "type") {
            System.out.println("r6");
            advanceToken();
            if (tokenTag() == "VARIABLE") {
                advanceToken();
                declaration12();
                return;
            } else {
                throw new ExceptionSyntaxique("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        } else if (tokenTag() == "procedure") {
            System.out.println("r7");
            advanceToken();
            if (tokenTag() == "VARIABLE") {
                advanceToken();
                declaration21();
                return;
            } else {
                throw new ExceptionSyntaxique("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        } else if (tokenTag() == "function") {
            advanceToken();
            System.out.println("r8");
            if (tokenTag() == "VARIABLE") {
                advanceToken();
                declaration31();
            } else {
                throw new ExceptionSyntaxique("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        } else if (tokenTag() == "VARIABLE") {
            System.out.println("r9");
            identificateurVirgulePlus();
            if (tokenTag() == ":") {
                advanceToken();
                type();
                declaration13();
            } else {
                throw new ExceptionSyntaxique("':' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        } else {
            throw new ExceptionSyntaxique("'type', 'procedure', 'function' ou identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }

    public void declaration12() throws ExceptionSyntaxique {
        System.out.print("declaration12 : ");
        if (tokenTag() == ";") {
            System.out.println("r10");
            advanceToken();
            return;
        } else if (tokenTag() == "is") {
            System.out.println("r11");
            advanceToken();
            declaration14();
        } else {
            throw new ExceptionSyntaxique("'is' ou ';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }

    public void declaration13() throws ExceptionSyntaxique {
        System.out.print("declaration13 : ");
        if (tokenTag() == ";") {
            System.out.println("r12");
            advanceToken();
            return;
        } else if (tokenTag() == ":") {
            System.out.println("r13");
            advanceToken();
            if (tokenTag() == "=") {
                advanceToken();
                expression();
                if (tokenTag() == ";") {
                    advanceToken();
                    return;
                } else {
                    throw new ExceptionSyntaxique("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                }
            } else {
                throw new ExceptionSyntaxique("'=' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        } else {
            throw new ExceptionSyntaxique("':' ou ';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }
 
    public void declaration14() throws ExceptionSyntaxique {
        System.out.print("declaration14 : ");
        if (tokenTag() == "access"){
            System.out.println("r14");
            advanceToken();
            if (tokenTag() == "VARIABLE"){
                advanceToken();
            }
            else{
                throw new ExceptionSyntaxique("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
            if (tokenTag() == ";"){
                advanceToken();
                return;
            }
            else{
                throw new ExceptionSyntaxique("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        }
        else if (tokenTag() == "record"){
            System.out.println("r15");
            advanceToken();
            champsPlus();
            if (tokenTag() == "end"){
                advanceToken();
            }else{  
                throw new ExceptionSyntaxique("'end' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
            if (tokenTag() == "record"){
                advanceToken();
            } else{
                throw new ExceptionSyntaxique("'record' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
            if (tokenTag() == ";"){
                advanceToken();
                return;
            } else{
                throw new ExceptionSyntaxique("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        } else {
            throw new ExceptionSyntaxique("'access' ou 'record' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }

    public void declaration21() throws ExceptionSyntaxique {
        System.out.print("declaration21 : ");
        if (tokenTag() == "is"){
            System.out.println("r16");
            advanceToken();
            declaration22();
            return;
        }
        else if(tokenTag() == "("){
            System.out.println("r17");
            params();
            if (tokenTag() == "is"){
                advanceToken();
                declaration22();
            }
            else{
                throw new ExceptionSyntaxique("'is' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        }
        else{
            throw new ExceptionSyntaxique("'is' ou '(' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }

    public void declaration22() throws ExceptionSyntaxique {
        System.out.print("declaration22 : ");
        if (tokenTag() == "begin") {
            System.out.println("r18");
            advanceToken();
            if (tokenTag() == "VARIABLE" 
            || tokenTag() == "NUM_CONST" 
            || tokenTag() == "STRING_CONST" 
            || tokenTag() == "true" 
            || tokenTag() == "false" 
            || tokenTag() == "null" 
            || tokenTag() == "(" 
            || tokenTag() == "not" 
            || tokenTag() == "-" 
            || tokenTag() == "new" 
            || tokenTag() == "character" 
            || tokenTag() == "begin" 
            || tokenTag() == "if" 
            || tokenTag() == "for" 
            || tokenTag() == "while"
            || tokenTag() == "put") {
                instructionPlus();
                if(tokenTag() == "end"){
                    advanceToken();
                    declaration23();
                }
                else{
                    throw new ExceptionSyntaxique("'end' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                }
            } else {
                throw new ExceptionSyntaxique("identificateur ou entier ou chaine ou 'true' ou 'false' ou 'null' ou '(' ou 'not' ou '-' ou 'new' ou 'character' ou 'begin' ou 'if' ou 'for' ou 'while' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        } else if (tokenTag() == "type" || tokenTag() == "procedure" || tokenTag() == "function" || tokenTag() == "VARIABLE") {
            System.out.println("r19");
            declarationPlus();
            if (tokenTag() == "begin") {
                advanceToken();
                instructionPlus();
                if(tokenTag() == "end"){
                    advanceToken();
                    declaration23();
                }
                else{
                    throw new ExceptionSyntaxique("'end' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                }
            } else {
                throw new ExceptionSyntaxique("'begin' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        } else {
            throw new ExceptionSyntaxique("'begin', 'type', 'procedure', 'function' ou identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }

    public void declaration23() throws ExceptionSyntaxique {
        System.out.print("declaration23 : ");
        if (tokenTag() == ";") {
            System.out.println("r20");
            advanceToken();
            return;
        } else if (tokenTag() == "VARIABLE") {
            System.out.println("r21");
            advanceToken();
            if (tokenTag() == ";") {
                advanceToken();
            } else {
                throw new ExceptionSyntaxique("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        } else {
            throw new ExceptionSyntaxique("';' ou identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }

    public void declaration31() throws ExceptionSyntaxique {
        System.out.print("declaration31 : ");
        if (tokenTag() == "return") {
            System.out.println("r22");
            advanceToken();
            type();
            if (tokenTag() == "is") {
                advanceToken();
                declaration22();
            } else {
                throw new ExceptionSyntaxique("'is' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        } else if (tokenTag() == "(") {
            System.out.println("r23");
            params();
            if (tokenTag() == "return") {
                advanceToken();
                type();
                if (tokenTag() == "is") {
                    advanceToken();
                    declaration22();
                } else {
                    throw new ExceptionSyntaxique("'is' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                }
            } else {
                throw new ExceptionSyntaxique("'return' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        } else {
            throw new ExceptionSyntaxique("'return' ou '(' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }

    public void champs() throws ExceptionSyntaxique {
        System.out.print("champs : ");
        if (tokenTag() == "VARIABLE"){
            System.out.println("r24");
            identificateurVirgulePlus();
            if (tokenTag() == ":"){
                advanceToken();
            } else{
                throw new ExceptionSyntaxique("':' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                
            }
            type();
            if (tokenTag() == ";"){
                advanceToken();
                return;
            } else{
                throw new ExceptionSyntaxique("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        }
        else{
            throw new ExceptionSyntaxique("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }

    public void type() throws ExceptionSyntaxique {
        System.out.print("type : ");
        if (tokenTag() == "VARIABLE") {
            System.out.println("r25");
            advanceToken();
            return;
        } else if (tokenTag() == "access") {
            System.out.println("r26");
            advanceToken();
            if (tokenTag() == "VARIABLE") {
                advanceToken();
                return;
            } else {
                throw new ExceptionSyntaxique("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        } else {
            throw new ExceptionSyntaxique("identificateur ou 'access' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }

    public void params() throws ExceptionSyntaxique {
        System.out.print("params : ");
        if (tokenTag() == "(") {
            System.out.println("r27");
            advanceToken();
            paramPointVirgulePlus();
            if (tokenTag() == ")") {
                advanceToken();
                return;
            } else {
                throw new ExceptionSyntaxique("')' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        } else {
            throw new ExceptionSyntaxique("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }

    public void param() throws ExceptionSyntaxique {
        System.out.print("param : ");
        if (tokenTag() == "VARIABLE") {
            System.out.println("r28");
            identificateurVirgulePlus();
            if (tokenTag() == ":") {
                advanceToken();
            } else {
                throw new ExceptionSyntaxique("':' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
            if (tokenTag() == "VARIABLE" || tokenTag() == "access") {
                param2();
            } else {
                throw new ExceptionSyntaxique("identificateur ou 'access' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        }else {
            throw new ExceptionSyntaxique("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }


    public void param2() throws ExceptionSyntaxique {
        System.out.print("param2 : ");
        if (tokenTag() == "VARIABLE" || tokenTag() == "access") {
            System.out.println("r29");
            type();
            return;
        } else if (tokenTag() == "in") {
            System.out.println("r30");
            mode();
        } else {
            throw new ExceptionSyntaxique("identificateur ou 'access' ou 'in' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }

    public void mode() throws ExceptionSyntaxique {
        System.out.print("mode : ");
        if (tokenTag() == "in") {
            System.out.println("r31");
            advanceToken();
            mode1();
        } else {
            throw new ExceptionSyntaxique("'in' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }

    public void mode1() throws ExceptionSyntaxique {
        System.out.print("mode1 : ");
        if (tokenTag() == "out") {
            System.out.println("r32");
            advanceToken();
            return;
        }
        else
        {
            System.out.println("r33");
            return;
        }
    }


    public void expression() throws ExceptionSyntaxique {
        System.out.print("expression : ");
        if (tokenTag() == "VARIABLE") { //LL(2) ici
            advanceToken();
            if(tokenTag() == "("){
                System.out.println("r44");
                advanceToken();
                expressionPlusVirgule();
                if (tokenTag()== ")") {
                    advanceToken();
                    expressionRecur();
                } else {
                    throw new ExceptionSyntaxique("')' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                }
            }
            else if(tokenTag() == ";" //à vérifier
            || tokenTag() == "." 
            || tokenTag() == ":" 
            || tokenTag() == "="
            || tokenTag() == "/="
            || tokenTag() == "<"
            || tokenTag() == "<="
            || tokenTag() == ">"
            || tokenTag() == ">="
            || tokenTag() == "+"
            || tokenTag() == "-"
            || tokenTag() == "*"
            || tokenTag() == "/"
            || tokenTag() == "rem"
            || tokenTag() == ")"
            || tokenTag() == "then"
            || tokenTag() == "loop"
            || tokenTag() == ".."
            || tokenTag() == "end"){
                decrementToken();
                System.out.println("r40");
                acces();
                expressionRecur();
                return;
            }
        }
        else if (tokenTag() == "NUM_CONST") {
            System.out.println("r34");
            advanceToken();
            expressionRecur();
            return;
        }
        else if (tokenTag() == "STRING_CONST") {
            System.out.println("r35");
            advanceToken();
            expressionRecur();
            return;
        }
        else if (tokenTag() == "true") {
            System.out.println("r36");
            advanceToken();
            expressionRecur();
            return;
        }
        else if (tokenTag() == "false") {
            System.out.println("r37");
            advanceToken();
            expressionRecur();
            return;
        }
        else if (tokenTag() == "null") {
            System.out.println("r38");
            advanceToken();
            expressionRecur();
            return;
        }
        else if (tokenTag() == "(") {
            System.out.println("r39");
            advanceToken();
            expression();
            if (tokenTag() == ")") {
                advanceToken();
                expressionRecur();
                return;
            } else {
                throw new ExceptionSyntaxique("')' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        }
        else if (tokenTag() == "not") {
            System.out.println("r41");
            advanceToken();
            expression();
            expressionRecur();
            return;
        }
        else if (tokenTag() == "-") {
            System.out.println("r42");
            advanceToken();
            expression();
            expressionRecur();
            return;
        }
        else if (tokenTag() == "new") {
            System.out.println("r43");
            advanceToken();
            if (tokenTag() == "VARIABLE") {
                advanceToken();
                expressionRecur();
            } else {
                throw new ExceptionSyntaxique("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        }
        else if(tokenTag() == "character"){
            System.out.println("r45");
            advanceToken();
            if (tokenTag() == "'"){
                advanceToken();
                if (tokenTag()== "val"){
                    advanceToken();
                    if (tokenTag()== "("){
                        advanceToken();
                        expression();
                        if (tokenTag()== ")"){
                            advanceToken();
                            expressionRecur();
                        }
                        else{
                            throw new ExceptionSyntaxique(") attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                        }
                    }
                    else{
                        throw new ExceptionSyntaxique("( attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                    }
                }
                else{
                    throw new ExceptionSyntaxique("val attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                }
            }
        }
        else{
            throw new ExceptionSyntaxique("identificateur ou entier ou chaine ou 'true' ou 'false' ou 'null' ou '(' ou 'not' ou '-' ou 'new' ou 'character' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }

    public void expressionRecur() throws ExceptionSyntaxique {
        System.out.print("expressionRecur : ");
        if (tokenTag() == "="
        || tokenTag() == "/="
        || tokenTag() == "<"
        || tokenTag() == "<="
        || tokenTag() == ">"
        || tokenTag() == ">="
        || tokenTag() == "+"
        || tokenTag() == "-"
        || tokenTag() == "*"
        || tokenTag() == "/"
        || tokenTag() == "rem") {
            System.out.println("r46");
            operateur();
            expression();
            expressionRecur();
        } else {
            System.out.println("r47");
            return;
        }
    }

    public void instruction() throws ExceptionSyntaxique {
        System.out.print("instruction : ");
        if (tokenTag() == "NUM_CONST"
        || tokenTag() == "STRING_CONST"
        || tokenTag() == "true"
        || tokenTag() == "false"
        || tokenTag() == "null"
        || tokenTag() == "("
        || tokenTag() == "not"
        || tokenTag() == "-"
        || tokenTag() == "new"
        || tokenTag() == "character"){
            System.out.println("r48");
            acces();
        } 
        else if(tokenTag() == "VARIABLE" ){ //LL(2) ici
            advanceToken();
            if (tokenTag() == ";" || tokenTag() == "("){  //Problème ici à l'éxécution à cause des parenthèses
                decrementToken();
                System.out.println("r49");
                instruction1();
                return; 
            } // Ajouter en dessous la reconnaissance dans des opérateurs
            else if (tokenTag() == "." || tokenTag() == "(" || tokenTag() == ":"){
                decrementToken();
                System.out.println("r48");
                acces();
                if (tokenTag() == ":"){
                    advanceToken();
                    if (tokenTag() == "="){
                        advanceToken();
                        expression();
                        if (tokenTag() == ";"){
                            advanceToken();
                            return;
                        }
                        else{
                            throw new ExceptionSyntaxique("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                        }
                    }
                    else{
                        throw new ExceptionSyntaxique("'=' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                    }
                }else{
                    throw new ExceptionSyntaxique("':' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                }
            } else {
                throw new ExceptionSyntaxique("';' ou '.' ou '(' ou ':' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
            
        }
        else if(tokenTag() == "return"){
            System.out.println("r50");
            advanceToken();
            instruction2();
            return;
        }
        else if(tokenTag() == "begin"){
            System.out.println("r51");
            advanceToken();
            instructionPlus();
            if(tokenTag() == "end"){
                advanceToken();
                if(tokenTag() == ";"){
                    advanceToken();
                    return;
                }
                else{
                    throw new ExceptionSyntaxique("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                }
            }
            else{
                throw new ExceptionSyntaxique("'end' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        }
        else if(tokenTag() == "if"){
            System.out.println("r52");
            advanceToken();
            expression();
            if(tokenTag()=="then"){
                advanceToken();
                instructionPlus();
                instruction3();
                return;
            }
            else{
                throw new ExceptionSyntaxique("'then' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        }
        else if(tokenTag() == "for"){
            System.out.println("r53");
            advanceToken();
            if (tokenTag() == "VARIABLE") {
                advanceToken();
            } else {
                throw new ExceptionSyntaxique("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
            if (tokenTag() == "in") {
                advanceToken();
            } else {
                throw new ExceptionSyntaxique("'in' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
            instruction5();
        }
        else if(tokenTag() == "while"){
            System.out.println("r54");
            advanceToken();
            expression();
            if(tokenTag() == "loop"){
                advanceToken();
                instructionPlus();
                if(tokenTag() == "end"){
                    advanceToken();
                    if(tokenTag() == "loop"){
                        advanceToken();
                        if (tokenTag() == ";") {
                            advanceToken();
                            return;
                        } else {
                            throw new ExceptionSyntaxique("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                        }          
                    }
                    else{
                        throw new ExceptionSyntaxique("'loop' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                    }
                }
                else{
                    throw new ExceptionSyntaxique("'end' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                }
            }
            else{
                throw new ExceptionSyntaxique("'loop' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        }
        else if (tokenTag() == "put") {
            System.out.println("rPUT");
            advanceToken();
            if (tokenTag() == "(") {
                advanceToken();
                expression();
                if (tokenTag() == ")") {
                    advanceToken();
                    if (tokenTag() == ";") {
                        advanceToken();
                        return;
                    } else {
                        throw new ExceptionSyntaxique("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                    }
                } else {
                    throw new ExceptionSyntaxique("')' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                }
            } else {
                throw new ExceptionSyntaxique("'(' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        }
        else{
            throw new ExceptionSyntaxique("identificateur ou entier ou chaine ou 'true' ou 'false' ou 'null' ou '(' ou 'not' ou '-' ou 'new' ou 'character' ou 'return' ou 'begin' ou 'if' ou 'for' ou 'while' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());

        }
    }

    public void instruction1() throws ExceptionSyntaxique {
        System.out.print("instruction1 : ");
        System.out.println("token actuel "+ this.tokens.get(currentTokenIndex).getTag());
        System.out.println("ligne token actuel "+ this.tokens.get(currentTokenIndex).getLine());

        if (tokenTag() == ";"){
            System.out.println("r55");
            advanceToken();
            return;
        }
        else if (tokenTag() == "("){
            System.out.println("r56");
            advanceToken();
            expressionPlusVirgule();
            if (tokenTag() == ")"){
                advanceToken();
                if (tokenTag() == ";"){
                    advanceToken();
                    return;
                }
                else{
                    throw new ExceptionSyntaxique("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                }
            }
            else{
                throw new ExceptionSyntaxique("')' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        }
        else{
            throw new ExceptionSyntaxique("';' ou '(' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }

    public void instruction2() throws ExceptionSyntaxique {
        System.out.print("instruction2 : ");
        if (tokenTag() == ";"){
            System.out.println("r57");
            advanceToken();
            return;
        }
        else if (tokenTag() == "NUM_CONST"
        || tokenTag() == "STRING_CONST"
        || tokenTag() == "true"
        || tokenTag() == "false"
        || tokenTag() == "null"
        || tokenTag() == "("
        || tokenTag() == "not"
        || tokenTag() == "-"
        || tokenTag() == "new"
        || tokenTag() == "character"
        || tokenTag() == "VARIABLE" ){
            System.out.println("r58");
            expression();
            if (tokenTag() == ";") {
                advanceToken();
            } else {
                throw new ExceptionSyntaxique("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        }
        else{
            throw new ExceptionSyntaxique("identificateur ou entier ou chaine ou 'true' ou 'false' ou 'null' ou '(' ou 'not' ou '-' ou 'new' ou 'character' ou ';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }

    public void instruction3() throws ExceptionSyntaxique {
        System.out.print("instruction3 : ");
        //Lignes de debug
        //System.out.println("token actuel "+ this.tokens.get(currentTokenIndex).getTag());
        //System.out.println("token suivant "+ this.tokens.get(currentTokenIndex+1).getTag());
        //System.out.println("token précédent "+ this.tokens.get(currentTokenIndex-1).getTag());
        //System.out.println("token index "+ currentTokenIndex);
        if (tokenTag() == "end"){
            System.out.println("r59");
            advanceToken();
            if (tokenTag() == "if") {
                advanceToken();
            } else {
                throw new ExceptionSyntaxique("'if' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
            if (tokenTag() == ";") {
                advanceToken();
                return;
            } else {
                throw new ExceptionSyntaxique("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        }
        else if (tokenTag() == "else"){
            System.out.println("r60");
            advanceToken();
            instructionPlus();
            if (tokenTag() == "end"){
                advanceToken();
                if (tokenTag() == "if") {
                advanceToken();
                } else {
                    throw new ExceptionSyntaxique("'if' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                }
                if (tokenTag() == ";") {
                    advanceToken();
                    return;
                } else {
                    throw new ExceptionSyntaxique("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                }
            }
            else{
                throw new ExceptionSyntaxique("'end' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        } else if (tokenTag() == "elsif"){
            System.out.println("r61");
            elsifPlus();
            instruction4();
        } else{
            throw new ExceptionSyntaxique("'else' ou 'end' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
        }

    public void instruction4() throws ExceptionSyntaxique {
        System.out.print("instruction4 : ");
        if (tokenTag() == "end"){
            System.out.println("r62");
            advanceToken();
            if (tokenTag() == "if") {
                advanceToken();
            } else {
                throw new ExceptionSyntaxique("'if' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
            if (tokenTag() == ";") {
                advanceToken();
                return;
            } else {
                throw new ExceptionSyntaxique("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        }
        else if (tokenTag() == "else"){
            System.out.println("r63");
            advanceToken();
            instructionPlus();
            if (tokenTag() == "end"){
                advanceToken();
                if (tokenTag() == "if") {
                advanceToken();
                } else {
                    throw new ExceptionSyntaxique("'if' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                }
                if (tokenTag() == ";") {
                    advanceToken();
                } else {
                    throw new ExceptionSyntaxique("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                }
            }
            else{
                throw new ExceptionSyntaxique("'end' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        }
        else{
            throw new ExceptionSyntaxique("'else' ou 'end' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }

    public void instruction5() throws ExceptionSyntaxique {
        System.out.print("instruction5 : ");
        if (tokenTag() == "NUM_CONST"
        || tokenTag() == "STRING_CONST"
        || tokenTag() == "true"
        || tokenTag() == "false"
        || tokenTag() == "null"
        || tokenTag() == "("
        || tokenTag() == "not"
        || tokenTag() == "-"
        || tokenTag() == "new"
        || tokenTag() == "character"
        || tokenTag() == "VARIABLE" ){
            System.out.println("r64");
            expression();
            if (tokenTag() == "..") {
                advanceToken();
            } else {
                throw new ExceptionSyntaxique("'..' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
            expression();
            if (tokenTag() == "loop") {
                advanceToken();
            } else {
                throw new ExceptionSyntaxique("'loop' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
            instructionPlus();
            if (tokenTag() == "end") {
                advanceToken();
            } else {
                throw new ExceptionSyntaxique("'end' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
            if (tokenTag() == "loop") {
                advanceToken();
            } else {
                throw new ExceptionSyntaxique("'loop' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
            if (tokenTag() == ";") {
                advanceToken();
                return;
            } else {
                throw new ExceptionSyntaxique("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        } else if(tokenTag() == "reverse")
        {
            System.out.println("r65");
            advanceToken();
            if (tokenTag() == "NUM_CONST"
            || tokenTag() == "STRING_CONST"
            || tokenTag() == "true"
            || tokenTag() == "false"
            || tokenTag() == "null"
            || tokenTag() == "("
            || tokenTag() == "not"
            || tokenTag() == "-"
            || tokenTag() == "new"
            || tokenTag() == "character"
            || tokenTag() == "VARIABLE" ){
                expression();
                if (tokenTag() == "..") {
                    advanceToken();
                } else {
                    throw new ExceptionSyntaxique("'..' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                }
                expression();
                if (tokenTag() == "loop") {
                    advanceToken();
                } else {
                    throw new ExceptionSyntaxique("'loop' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                }
                instructionPlus();
                if (tokenTag() == "end") {
                    advanceToken();
                } else {
                    throw new ExceptionSyntaxique("'end' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                }
                if (tokenTag() == "loop") {
                    advanceToken();
                } else {
                    throw new ExceptionSyntaxique("'loop' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                }
                if (tokenTag() == ";") {
                    advanceToken();
                    return;
                } else {
                    throw new ExceptionSyntaxique("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                }
            }
            else{
                throw new ExceptionSyntaxique("identificateur ou entier ou chaine ou 'true' ou 'false' ou 'null' ou '(' ou 'not' ou '-' ou 'new' ou 'character' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        } else{
            throw new ExceptionSyntaxique("identificateur ou entier ou chaine ou 'true' ou 'false' ou 'null' ou '(' ou 'not' ou '-' ou 'new' ou 'character' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }


    public void operateur() throws ExceptionSyntaxique {
        System.out.print("operateur : ");
        if (tokenTag() == "=") {
            System.out.println("r66");
            advanceToken();
            return;
        } else if (tokenTag() == "/=") {
            System.out.println("r67");
            advanceToken();
            return;
        } else if (tokenTag() == "<") {
            System.out.println("r68");
            advanceToken();
            return;
        } else if (tokenTag() == "<=") {
            System.out.println("r69");
            advanceToken();
            return;
        } else if (tokenTag() == ">") {
            System.out.println("r70");
            advanceToken();
            return;
        } else if (tokenTag() == ">=") {
            System.out.println("r71");
            advanceToken();
            return;
        } else if (tokenTag() == "+") {
            System.out.println("r72");
            advanceToken();
            return;
        } else if (tokenTag() == "-") {
            System.out.println("r73");
            advanceToken();
            return;
        } else if (tokenTag() == "*") {
            System.out.println("r74");
            advanceToken();
            return;
        } else if (tokenTag() == "/") {
            System.out.println("r75");
            advanceToken();
            return;
        } else if (tokenTag() == "rem") {
            System.out.println("r76");
            advanceToken();
            return;
        } else {
            throw new ExceptionSyntaxique("'=', '/=', '<', '<=', '>', '>=', '+', '-', '*', '/', 'rem' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }
 
    public void acces() throws ExceptionSyntaxique {
        System.out.print("acces : ");
        if (tokenTag() == "VARIABLE")  { //LL(2) ici
            advanceToken();
            if(tokenTag() == ";" 
            || tokenTag() == "." 
            || tokenTag() == ":" 
            || tokenTag() == "="
            || tokenTag() == "/="
            || tokenTag() == "<"
            || tokenTag() == "<="
            || tokenTag() == ">"
            || tokenTag() == ">="
            || tokenTag() == "+"
            || tokenTag() == "-"
            || tokenTag() == "*"
            || tokenTag() == "/"
            || tokenTag() == "rem"
            || tokenTag() == ")"
            || tokenTag() == "then"
            || tokenTag() == "loop"
            || tokenTag() == ".."
            || tokenTag() == "end"){
                System.out.println("r77");
                accesRecur();
            }
            else if (tokenTag() == "(") {
                System.out.println("r87");
                advanceToken();
                expressionPlusVirgule();
                if (tokenTag()== ")") {
                    advanceToken();
                    expressionRecur();
                    if (tokenTag() == ".")
                    {
                        advanceToken();
                        if (tokenTag() == "VARIABLE") {
                            advanceToken();
                            accesRecur();
                        } else {
                            throw new ExceptionSyntaxique("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                        }
                    } else{
                        throw new ExceptionSyntaxique("'.' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                    }
                } else {
                    throw new ExceptionSyntaxique("')' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                }
            } else {
                throw new ExceptionSyntaxique("'.' ou '(' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        } else if (tokenTag() == "NUM_CONST") {
            System.out.println("r78");
            advanceToken();
            expressionRecur();
            if (tokenTag() == ".")
            {
                advanceToken();
                if (tokenTag() == "VARIABLE") {
                    advanceToken();
                    accesRecur();
                } else {
                    throw new ExceptionSyntaxique("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                }
            } else{
                throw new ExceptionSyntaxique("'.' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        } else if (tokenTag() == "STRING_CONST") {
            System.out.println("r79");
            advanceToken();
            expressionRecur();
            if (tokenTag() == ".")
            {
                advanceToken();
                if (tokenTag() == "VARIABLE") {
                    advanceToken();
                    accesRecur();
                } else {
                    throw new ExceptionSyntaxique("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                }
            } else{
                throw new ExceptionSyntaxique("'.' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        } else if (tokenTag() == "true") {
            System.out.println("r80");
            advanceToken();
            expressionRecur();
            if (tokenTag() == ".")
            {
                advanceToken();
                if (tokenTag() == "VARIABLE") {
                    advanceToken();
                    accesRecur();
                } else {
                    throw new ExceptionSyntaxique("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                }
            } else{
                throw new ExceptionSyntaxique("'.' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        } else if (tokenTag() == "false") {
            System.out.println("r81");
            advanceToken();
            expressionRecur();
            if (tokenTag() == ".")
            {
                advanceToken();
                if (tokenTag() == "VARIABLE") {
                    advanceToken();
                    accesRecur();
                } else {
                    throw new ExceptionSyntaxique("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                }
            } else{
                throw new ExceptionSyntaxique("'.' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        } else if (tokenTag() == "null") {
            System.out.println("r82");
            advanceToken();
            expressionRecur();
            if (tokenTag() == ".")
            {
                advanceToken();
                if (tokenTag() == "VARIABLE") {
                    advanceToken();
                    accesRecur();
                } else {
                    throw new ExceptionSyntaxique("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                }
            } else{
                throw new ExceptionSyntaxique("'.' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        } else if (tokenTag() == "(") {
            System.out.println("r83");
            advanceToken();
            expression();
            if (tokenTag() == ")") {
                advanceToken();
                expressionRecur();
                if (tokenTag() == ".")
                {
                    advanceToken();
                    if (tokenTag() == "VARIABLE") {
                        advanceToken();
                        accesRecur();
                    } else {
                        throw new ExceptionSyntaxique("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                    }
                } else{
                    throw new ExceptionSyntaxique("'.' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                }
            } else {
                throw new ExceptionSyntaxique("')' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        } else if (tokenTag() == "not") {
            System.out.println("r84");
            advanceToken();
            expression();
            expressionRecur();
            if (tokenTag() == ".")
            {
                advanceToken();
                if (tokenTag() == "VARIABLE") {
                    advanceToken();
                    accesRecur();
                } else {
                    throw new ExceptionSyntaxique("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                }
            } else{
                throw new ExceptionSyntaxique("'.' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        } else if (tokenTag() == "-") {
            System.out.println("r85");
            advanceToken();
            expression();
            expressionRecur();
            if (tokenTag() == ".")
            {
                advanceToken();
                if (tokenTag() == "VARIABLE") {
                    advanceToken();
                    accesRecur();
                } else {
                    throw new ExceptionSyntaxique("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                }
            } else{
                throw new ExceptionSyntaxique("'.' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        } else if (tokenTag() == "new") {
            System.out.println("r86");
            advanceToken();
            if (tokenTag() == "VARIABLE") {
                advanceToken();
                expressionRecur();
                if (tokenTag() == ".")
                {
                    advanceToken();
                    if (tokenTag() == "VARIABLE") {
                        advanceToken();
                        accesRecur();
                    } else {
                        throw new ExceptionSyntaxique("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                    }
                } else{
                    throw new ExceptionSyntaxique("'.' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                }
            } else {
                throw new ExceptionSyntaxique("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        } else if (tokenTag() == "character") {
            System.out.println("r88");
            advanceToken();
            if(tokenTag() == "'"){
                advanceToken();
                if (tokenTag() == "VARIABLE") {
                    advanceToken();
                    if(tokenTag() == "'"){
                        advanceToken();
                        expressionRecur();
                        if (tokenTag() == ".")
                        {
                            advanceToken();
                            if (tokenTag() == "VARIABLE") {
                                advanceToken();
                                accesRecur();
                            } else {
                                throw new ExceptionSyntaxique("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                            }
                        } else{
                            throw new ExceptionSyntaxique("'.' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                        }
                    }
                    else{
                        throw new ExceptionSyntaxique("' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                    }
                } else {
                    throw new ExceptionSyntaxique("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                }
            }
            else{
                throw new ExceptionSyntaxique("' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        } else {
            throw new ExceptionSyntaxique("identificateur ou entier ou chaine ou 'true' ou 'false' ou 'null' ou '(' ou 'not' ou '-' ou 'new' ou 'character' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }

    public void accesRecur() throws ExceptionSyntaxique {
        System.out.print("accesRecur : ");
        if (tokenTag() == ".") {
            System.out.println("r89");
            advanceToken();
            if (tokenTag() == "VARIABLE") {
                advanceToken();
                accesRecur();
            } else {
                throw new ExceptionSyntaxique("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        } else {
            System.out.println("r90");
            return;
        }
    }


        

    public void instructionPlus() throws ExceptionSyntaxique {
        System.out.print("instructionPlus : ");
        if (tokenTag() == "VARIABLE" 
        || tokenTag() == "NUM_CONST" 
        || tokenTag() == "STRING_CONST" 
        || tokenTag() == "true" 
        || tokenTag() == "false" 
        || tokenTag() == "null" 
        || tokenTag() == "(" 
        || tokenTag() == "not" 
        || tokenTag() == "-" 
        || tokenTag() == "new" 
        || tokenTag() == "character" 
        || tokenTag() == "return"
        || tokenTag() == "begin" 
        || tokenTag() == "if" 
        || tokenTag() == "for" 
        || tokenTag() == "while"
        || tokenTag() == "put") {
            System.out.println("r91");
            instruction();
            instructionPlus1();
        } else {
            throw new ExceptionSyntaxique("identificateur ou entier ou chaine ou 'true' ou 'false' ou 'null' ou '(' ou 'not' ou '-' ou 'new' ou 'character' ou 'begin' ou 'if' ou 'for' ou 'while' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }
 
    public void instructionPlus1() throws ExceptionSyntaxique {
        System.out.print("instructionPlus1 : ");
        if (tokenTag() == "VARIABLE" 
        || tokenTag() == "NUM_CONST" 
        || tokenTag() == "STRING_CONST" 
        || tokenTag() == "true" 
        || tokenTag() == "false" 
        || tokenTag() == "null" 
        || tokenTag() == "(" 
        || tokenTag() == "not" 
        || tokenTag() == "-" 
        || tokenTag() == "new" 
        || tokenTag() == "character"
        || tokenTag() == "return"
        || tokenTag() == "begin" 
        || tokenTag() == "if" 
        || tokenTag() == "for" 
        || tokenTag() == "while"
        || tokenTag() == "put") {
            System.out.println("r92");
            instructionPlus();
        } else {
            System.out.println("r93");
            return;
        }
    }

    public void declarationPlus() throws ExceptionSyntaxique {
        System.out.print("declarationPlus : ");
        if (tokenTag() == "type" || tokenTag() == "procedure" || tokenTag() == "function" || tokenTag() == "VARIABLE") {
            System.out.println("r94");
            declaration();
            declarationPlus1();
        } else {
            throw new ExceptionSyntaxique("'type', 'procedure', 'function' ou identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }

    public void declarationPlus1() throws ExceptionSyntaxique {
        System.out.print("declarationPlus1 : ");
        if (tokenTag() == "type" || tokenTag() == "procedure" || tokenTag() == "function" || tokenTag() == "VARIABLE") {
            System.out.println("r95");
            declarationPlus();
        } else {
            System.out.println("r96");
            return;
        }
    } 
    
    
    public void champsPlus() throws ExceptionSyntaxique {
        System.out.print("champsPlus : ");
        if (tokenTag() == "VARIABLE"){
            System.out.println("r97");
            champs();
            champsPlus1();
        }
        else{
            throw new ExceptionSyntaxique("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }

    public void champsPlus1() throws ExceptionSyntaxique {
        System.out.print("champsPlus1 : ");
        if (tokenTag() == "VARIABLE"){
            System.out.println("r98");
            champsPlus();
        }
        else{
            System.out.println("r99");
            return;
        }
    }



    public void identificateurVirgulePlus() throws ExceptionSyntaxique {
        System.out.print("identificateurVirgulePlus : ");
        if (tokenTag() == "VARIABLE") {
            System.out.println("r100");
            advanceToken();
            identificateurVirgulePlus1();
            return;
        } else {
            throw new ExceptionSyntaxique("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }

    public void identificateurVirgulePlus1() throws ExceptionSyntaxique {
        System.out.print("identificateurVirgulePlus1 : ");
        if (tokenTag() == ",") {
            System.out.println("r101");
            advanceToken();
            if (tokenTag() == "VARIABLE") {
                identificateurVirgulePlus();
            } else {
                throw new ExceptionSyntaxique("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        } else{
            System.out.println("r102");
            return;
        }
    }

    public void paramPointVirgulePlus() throws ExceptionSyntaxique {
        System.out.print("paramPointVirgulePlus : ");
        if (tokenTag() == "VARIABLE") {
            System.out.println("r103");
            param();
            paramPointVirgulePlus1();
        } else {
            throw new ExceptionSyntaxique("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }

    public void paramPointVirgulePlus1() throws ExceptionSyntaxique {
        System.out.print("paramPointVirgulePlus1 : ");
        if (tokenTag() == ";") {
            System.out.println("r104");
            advanceToken();
            paramPointVirgulePlus();
        } else {
            System.out.println("r105");
            return;
        }
    }  

    public void expressionPlusVirgule() throws ExceptionSyntaxique {
        System.out.print("expressionPlusVirgule : ");
        if (tokenTag() == "NUM_CONST"
        || tokenTag() == "STRING_CONST"
        || tokenTag() == "true"
        || tokenTag() == "false"
        || tokenTag() == "null"
        || tokenTag() == "("
        || tokenTag() == "not"
        || tokenTag() == "-"
        || tokenTag() == "new"
        || tokenTag() == "character"
        || tokenTag() == "VARIABLE" ){
            System.out.println("r106");
            expression();
            expressionPlusVirgule1();
        }
        else{
            throw new ExceptionSyntaxique("identificateur ou entier ou chaine ou 'true' ou 'false' ou 'null' ou '(' ou 'not' ou '-' ou 'new' ou 'character' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }

    public void expressionPlusVirgule1() throws ExceptionSyntaxique {
        System.out.print("expressionPlusVirgule1 : ");
        if (tokenTag() == ","){
            System.out.println("r107");
            advanceToken();
            expressionPlusVirgule();
        }
        else{
            System.out.println("r108");
            return;
        }
    }


    public void elsifPlus() throws ExceptionSyntaxique {
        System.out.print("elsifPlus : ");
        if (tokenTag() == "elsif"){
            System.out.println("r109");
            advanceToken();
            expression();
            if (tokenTag() == "then"){
                advanceToken();
                instructionPlus();
                elsifPlus1();
            }
            else{
                throw new ExceptionSyntaxique("'then' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        }
        else{
            throw new ExceptionSyntaxique("'elsif' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }

    public void elsifPlus1() throws ExceptionSyntaxique {
        System.out.print("elsifPlus1 : ");
        if (tokenTag() == "elsif"){
            System.out.println("r110");
            elsifPlus();
        }
        else{
            System.out.println("r111");
            return;
        }
    }
}


