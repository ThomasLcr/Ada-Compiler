package eu.telecomnancy.application;

import java.util.ArrayList;

import eu.telecomnancy.application.exception.ExceptionLexicale;
import eu.telecomnancy.application.exception.ExceptionSemantique;
import eu.telecomnancy.application.exception.ExceptionSyntaxique;
import eu.telecomnancy.application.token.Token;

 
public class ParserV2 
{
    private ArrayList<Token> tokens;
    private int currentTokenIndex = 0;

    public ParserV2(ArrayList<Token> tokens) 
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
            || tokenTag() == "STR_CONST" 
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
        //Lignes de debug
        System.out.println("token actuel "+ this.tokens.get(currentTokenIndex).getTag());
        System.out.println("token suivant "+ this.tokens.get(currentTokenIndex+1).getTag());
        System.out.println("token précédent "+ this.tokens.get(currentTokenIndex-1).getTag());
        System.out.print("expression : ");
         if (tokenTag() == "-"
        || tokenTag() == "not"
        || tokenTag() == "NUM_CONST"
        || tokenTag() == "STR_CONST"
        || tokenTag() == "true"
        || tokenTag() == "false"
        || tokenTag() == "null"
        || tokenTag() == "new"
        || tokenTag() == "character"
        || tokenTag() == "("
        || tokenTag() == "VARIABLE") {
            System.out.println("r34");
            T();
            expression1();
        } else {
            throw new ExceptionSyntaxique("identificateur ou entier ou chaine ou 'true' ou 'false' ou 'null' ou '(' ou 'not' ou '-' ou 'new' ou 'character' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }

    public void expression1() throws ExceptionSyntaxique {
        System.out.print("expression1 : ");
        if (tokenTag() == "+") {
            System.out.println("r35");
            advanceToken();
            expression();
            return;
        } else if (tokenTag() == "-") {
            System.out.println("r36");
            advanceToken();
            expression();
        } else {
            System.out.println("r37");
            return;
        }
    }

    public void T() throws ExceptionSyntaxique {
        System.out.print("T : ");
        if (tokenTag() == "-"
        || tokenTag() == "not"
        || tokenTag() == "NUM_CONST"
        || tokenTag() == "STR_CONST"
        || tokenTag() == "true"
        || tokenTag() == "false"
        || tokenTag() == "null"
        || tokenTag() == "new"
        || tokenTag() == "character"
        || tokenTag() == "("
        || tokenTag() == "VARIABLE") {
            System.out.println("r38");
            I();
            T1();
        } else {
            throw new ExceptionSyntaxique("identificateur ou entier ou chaine ou 'true' ou 'false' ou 'null' ou '(' ou 'not' ou '-' ou 'new' ou 'character' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }

    public void T1() throws ExceptionSyntaxique {
        System.out.print("T1 : ");
        if (tokenTag() == "*") {
            System.out.println("r39");
            advanceToken();
            T();
            return;
        } else if (tokenTag() == "/") {
            System.out.println("r40");
            advanceToken();
            T();
            return;
        } else {
            System.out.println("r41");
            return;
        }
    }

    public void I() throws ExceptionSyntaxique {
        System.out.print("I : ");
        if (tokenTag() == "-"
        || tokenTag() == "not"
        || tokenTag() == "NUM_CONST"
        || tokenTag() == "STR_CONST"
        || tokenTag() == "true"
        || tokenTag() == "false"
        || tokenTag() == "null"
        || tokenTag() == "new"
        || tokenTag() == "character"
        || tokenTag() == "("
        || tokenTag() == "VARIABLE") {
            System.out.println("r42");
            F();
            I1();
        } else {
            throw new ExceptionSyntaxique("identificateur ou entier ou chaine ou 'true' ou 'false' ou 'null' ou '(' ou 'not' ou '-' ou 'new' ou 'character' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }

    public void I1() throws ExceptionSyntaxique {
        System.out.print("I1 : ");
        if (tokenTag() == "=") {
            System.out.println("r43");
            advanceToken();
            T();
            return;
        } else if (tokenTag() == "=/") {
            System.out.println("r44");
            advanceToken();
            T();
            return;
        } else if (tokenTag() == "<") {
            System.out.println("r45");
            advanceToken();
            T();
            return;
        } else if (tokenTag() == "<=") {
            System.out.println("r46");
            advanceToken();
            T();
            return;
        } else if (tokenTag() == ">") {
            System.out.println("r47");
            advanceToken();
            T();
            return;
        } else if (tokenTag() == ">=") {
            System.out.println("r48");
            advanceToken();
            T();
            return;
        } else if (tokenTag() == "rem") {
            System.out.println("r49");
            advanceToken();
            T();
            return;
        } else {
            System.out.println("r50");
            return;
        }
    }

    public void F() throws ExceptionSyntaxique {
        System.out.print("F : ");
        if (tokenTag() == "NUM_CONST"
        || tokenTag() == "STR_CONST"
        || tokenTag() == "true"
        || tokenTag() == "false"
        || tokenTag() == "null"
        || tokenTag() == "new"
        || tokenTag() == "character"
        || tokenTag() == "("
        || tokenTag() == "VARIABLE") {
            System.out.println("r51");
            P();
            return;
        } else if (tokenTag() == "-") {
            System.out.println("r52");
            advanceToken();
            if (tokenTag() == "NUM_CONST"
            || tokenTag() == "STR_CONST"
            || tokenTag() == "true"
            || tokenTag() == "false"
            || tokenTag() == "null"
            || tokenTag() == "new"
            || tokenTag() == "character"
            || tokenTag() == "("
            || tokenTag() == "VARIABLE") {
                P();
                return;
            } else {
                throw new ExceptionSyntaxique("identificateur ou entier ou chaine ou 'true' ou 'false' ou 'null' ou '(' ou '-' ou 'new' ou 'character' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        } else if (tokenTag() == "not") {
            System.out.println("r53");
            advanceToken();
            if (tokenTag() == "NUM_CONST"
            || tokenTag() == "STR_CONST"
            || tokenTag() == "true"
            || tokenTag() == "false"
            || tokenTag() == "null"
            || tokenTag() == "new"
            || tokenTag() == "character"
            || tokenTag() == "("
            || tokenTag() == "VARIABLE") {
                P();
                return;
            } else {
                throw new ExceptionSyntaxique("identificateur ou entier ou chaine ou 'true' ou 'false' ou 'null' ou '(' ou 'not' ou 'new' ou 'character' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        } else {
            throw new ExceptionSyntaxique("identificateur ou entier ou chaine ou 'true' ou 'false' ou 'null' ou '(' ou 'not' ou '-' ou 'new' ou 'character' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }

    public void P() throws ExceptionSyntaxique {
        System.out.print("P : ");
        if (tokenTag() == "NUM_CONST"){
            System.out.println("r54");
            advanceToken();
            Precur();
            return;
        }
        else if (tokenTag() == "STR_CONST"){
            System.out.println("r55");
            advanceToken();
            Precur();
            return;
        }
        else if (tokenTag() == "true"){
            System.out.println("r56");
            advanceToken();
            Precur();
            return;
        }
        else if (tokenTag() == "false"){
            System.out.println("r57");
            advanceToken();
            Precur();
            return;
        }
        else if (tokenTag() == "null"){
            System.out.println("r58");
            advanceToken();
            Precur();
            return;
        }
        else if (tokenTag() == "new"){
            System.out.println("r59");
            advanceToken();
            if (tokenTag() == "VARIABLE"){
                advanceToken();
                Precur();
                return;
            }
            else{
                throw new ExceptionSyntaxique("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        }
        else if (tokenTag() == "character"){
            System.out.println("r60");
            advanceToken();
            if (tokenTag() == "'"){
                advanceToken();
                if (tokenTag() == "val"){
                    advanceToken();
                    if (tokenTag() == "("){
                        advanceToken();
                        expression();
                        if (tokenTag() == ")"){
                            advanceToken();
                            Precur();
                            return;
                        } else{
                            throw new ExceptionSyntaxique("')' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                        }
                    } else{
                        throw new ExceptionSyntaxique("'(' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                    }
                } else{
                    throw new ExceptionSyntaxique("val attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                }
            } else {
                throw new ExceptionSyntaxique("' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        } 
        else if (tokenTag() == "("){
            System.out.println("r61");
            advanceToken();
            expression();
            if (tokenTag() == ")"){
                advanceToken();
                return;
            }
            else{
                throw new ExceptionSyntaxique("')' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        }
        else if (tokenTag() == "VARIABLE"){
            System.out.println("r62");
            advanceToken();
            P1();
            return;
        }
        else{
            throw new ExceptionSyntaxique("identificateur ou entier ou chaine ou 'true' ou 'false' ou 'null' ou '(' ou 'not' ou '-' ou 'new' ou 'character' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }   

    public void Precur() throws ExceptionSyntaxique {
        System.out.print("Precur : ");
        if (tokenTag() == "DOT"){ //LL(3) ici à voir si fonctionne correctement
            advanceToken();
            if (tokenTag() == "VARIABLE"){
                advanceToken();
                if(tokenTag() == ":"){
                    System.out.println("r64");
                    decrementToken();
                    decrementToken();
                    return;
                }
                else{
                    System.out.println("r63");
                    Precur();
                    return; 
                }
            }
        } 
        else if (tokenTag() == "="
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
        || tokenTag() == ";"
        || tokenTag() == ")"
        || tokenTag() == "loop"
        || tokenTag() == "then"
        || tokenTag() == "DOTDOT"
        || tokenTag() == ","){
            System.out.println("r64");
            return;
        }
        else{
            throw new ExceptionSyntaxique("')' ou '.' ou '=' ou '/=' ou '<' ou '<=' ou '>' ou '>=' ou '+' ou '-' ou '*' ou '/' ou 'rem' ou '+' ou '-' ou ';' ou ')' ou 'loop' ou 'then' ou '..' ou ',' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }

    }

    public void P1() throws ExceptionSyntaxique {
        System.out.print("P1 : ");
        System.out.println("token index "+ currentTokenIndex);
        if (tokenTag() == "DOT"
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
        || tokenTag() == "+"
        || tokenTag() == "-"
        || tokenTag() == ";"
        || tokenTag() == ")"
        || tokenTag() == "loop"
        || tokenTag() == "then"
        || tokenTag() == "DOTDOT"
        || tokenTag() == ","){
            System.out.println("r65");
            Precur();
            return;

        }
        else if (tokenTag() == "("){
            System.out.println("r66");
            advanceToken();
            expressionPlusVirgule();
            if (tokenTag() == ")"){
                advanceToken();
                Precur();
                return;
            }
            else{
                throw new ExceptionSyntaxique("')' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        } else{
            throw new ExceptionSyntaxique("')' ou '.' ou '=' ou '/=' ou '<' ou '<=' ou '>' ou '>=' ou '+' ou '-' ou '*' ou '/' ou 'rem' ou '+' ou '-' ou ';' ou ')' ou 'loop' ou 'then' ou '..' ou ',' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }

    public void instruction() throws ExceptionSyntaxique {
        System.out.print("instruction : ");
        if (tokenTag() == "VARIABLE"){
            System.out.println("r67");
            advanceToken();
            instruction1();
            return;
        }
        else if (tokenTag() == "-"){
            advanceToken();
            P();
            I1();
            T1();
            expression1();
            if (tokenTag() == "DOT"){
                advanceToken();
                if (tokenTag() == "VARIABLE"){
                    advanceToken();
                    if (tokenTag() == ":"){
                        advanceToken();
                        if (tokenTag() == "="){
                            advanceToken();
                            expression();
                            if (tokenTag() == ";"){
                                advanceToken();
                                return;
                            } else{
                                throw new ExceptionSyntaxique("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                            }
                        } else{
                            throw new ExceptionSyntaxique("'=' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                        }
                    } else{
                        throw new ExceptionSyntaxique("':' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                    }
                } else{
                    throw new ExceptionSyntaxique("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                }
            } else{
                throw new ExceptionSyntaxique("'.' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        }
        else if (tokenTag() == "not"){
            advanceToken();
            P();
            I1();
            T1();
            expression1();
            if (tokenTag() == "DOT"){
                advanceToken();
                if (tokenTag() == "VARIABLE"){
                    advanceToken();
                    if (tokenTag() == ":"){
                        advanceToken();
                        if (tokenTag() == "="){
                            advanceToken();
                            expression();
                            if (tokenTag() == ";"){
                                advanceToken();
                                return;
                            } else{
                                throw new ExceptionSyntaxique("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                            }
                        } else{
                            throw new ExceptionSyntaxique("'=' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                        }
                    } else{
                        throw new ExceptionSyntaxique("':' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                    }
                } else{
                    throw new ExceptionSyntaxique("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                }
            } else{
                throw new ExceptionSyntaxique("'.' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        }
        else if (tokenTag() == "NUM_CONST"){
            advanceToken();
            Precur();
            I1();
            T1();
            expression1();
            if(tokenTag() == "DOT"){
                advanceToken();
                if (tokenTag() == "VARIABLE"){
                    advanceToken();
                    if (tokenTag() == ":"){
                        advanceToken();
                        if (tokenTag() == "="){
                            advanceToken();
                            expression();
                            if (tokenTag() == ";"){
                                advanceToken();
                                return;
                            } else{
                                throw new ExceptionSyntaxique("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                            }
                        } else{
                            throw new ExceptionSyntaxique("'=' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                        }
                    } else{
                        throw new ExceptionSyntaxique("':' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                    }
                } else{
                    throw new ExceptionSyntaxique("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                }
            } else{
                throw new ExceptionSyntaxique("'.' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        }
        else if(tokenTag() == "STR_CONST"){
            advanceToken();
            Precur();
            I1();
            T1();
            expression1();
            if(tokenTag() == "DOT"){
                advanceToken();
                if (tokenTag() == "VARIABLE"){
                    advanceToken();
                    if (tokenTag() == ":"){
                        advanceToken();
                        if (tokenTag() == "="){
                            advanceToken();
                            expression();
                            if (tokenTag() == ";"){
                                advanceToken();
                                return;
                            } else{
                                throw new ExceptionSyntaxique("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                            }
                        } else{
                            throw new ExceptionSyntaxique("'=' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                        }
                    } else{
                        throw new ExceptionSyntaxique("':' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                    }
                } else{
                    throw new ExceptionSyntaxique("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                }
            } else{
                throw new ExceptionSyntaxique("'.' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        }
        else if(tokenTag() == "true"){
            advanceToken();
            Precur();
            I1();
            T1();
            expression1();
            if(tokenTag() == "DOT"){
                advanceToken();
                if (tokenTag() == "VARIABLE"){
                    advanceToken();
                    if (tokenTag() == ":"){
                        advanceToken();
                        if (tokenTag() == "="){
                            advanceToken();
                            expression();
                            if (tokenTag() == ";"){
                                advanceToken();
                                return;
                            } else{
                                throw new ExceptionSyntaxique("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                            }
                        } else{
                            throw new ExceptionSyntaxique("'=' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                        }
                    } else{
                        throw new ExceptionSyntaxique("':' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                    }
                } else{
                    throw new ExceptionSyntaxique("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                }
            } else{
                throw new ExceptionSyntaxique("'.' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        }
        else if(tokenTag() == "false"){
            advanceToken();
            Precur();
            I1();
            T1();
            expression1();
            if(tokenTag() == "DOT"){
                advanceToken();
                if (tokenTag() == "VARIABLE"){
                    advanceToken();
                    if (tokenTag() == ":"){
                        advanceToken();
                        if (tokenTag() == "="){
                            advanceToken();
                            expression();
                            if (tokenTag() == ";"){
                                advanceToken();
                                return;
                            } else{
                                throw new ExceptionSyntaxique("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                            }
                        } else{
                            throw new ExceptionSyntaxique("'=' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                        }
                    } else{
                        throw new ExceptionSyntaxique("':' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                    }
                } else{
                    throw new ExceptionSyntaxique("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                }
            } else{
                throw new ExceptionSyntaxique("'.' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        }
        else if(tokenTag() == "null"){
            advanceToken();
            Precur();
            I1();
            T1();
            expression1();
            if(tokenTag() == "DOT"){
                advanceToken();
                if (tokenTag() == "VARIABLE"){
                    advanceToken();
                    if (tokenTag() == ":"){
                        advanceToken();
                        if (tokenTag() == "="){
                            advanceToken();
                            expression();
                            if (tokenTag() == ";"){
                                advanceToken();
                                return;
                            } else{
                                throw new ExceptionSyntaxique("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                            }
                        } else{
                            throw new ExceptionSyntaxique("'=' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                        }
                    } else{
                        throw new ExceptionSyntaxique("':' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                    }
                } else{
                    throw new ExceptionSyntaxique("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                }
            } else{
                throw new ExceptionSyntaxique("'.' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        }
        else if (tokenTag() == "new"){
            advanceToken();
            if (tokenTag() == "VARIABLE"){
                advanceToken();
                if (tokenTag() == "("){
                    advanceToken();
                    if (tokenTag() == ")"){
                        advanceToken();
                        Precur();
                        I1();
                        T1();
                        expression1();
                        if(tokenTag() == "DOT"){
                            advanceToken();
                            if (tokenTag() == "VARIABLE"){
                                advanceToken();
                                if (tokenTag() == ":"){
                                    advanceToken();
                                    if (tokenTag() == "="){
                                        advanceToken();
                                        expression();
                                        if (tokenTag() == ";"){
                                            advanceToken();
                                            return;
                                        } else{
                                            throw new ExceptionSyntaxique("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                                        }
                                    } else{
                                        throw new ExceptionSyntaxique("'=' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                                    }
                                } else{
                                    throw new ExceptionSyntaxique("':' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                                }
                            } else{
                                throw new ExceptionSyntaxique("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                            }
                        } else{
                            throw new ExceptionSyntaxique("'.' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                        }
                    } else{
                        throw new ExceptionSyntaxique("')' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                    }
                } else{
                    throw new ExceptionSyntaxique("'(' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                }
            } else{
                throw new ExceptionSyntaxique("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        }
        else if (tokenTag() == "character"){
            advanceToken();
            if (tokenTag() == "'"){
                advanceToken();
                if (tokenTag() == "val"){
                    advanceToken();
                    if (tokenTag() == "("){
                        advanceToken();
                        expression();
                        if (tokenTag() == ")"){
                            advanceToken();
                            Precur();
                            I1();
                            T1();
                            expression1();
                            if(tokenTag() == "DOT"){
                                advanceToken();
                                if (tokenTag() == "VARIABLE"){
                                    advanceToken();
                                    if (tokenTag() == ":"){
                                        advanceToken();
                                        if (tokenTag() == "="){
                                            advanceToken();
                                            expression();
                                            if (tokenTag() == ";"){
                                                advanceToken();
                                                return;
                                            } else{
                                                throw new ExceptionSyntaxique("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                                            }
                                        } else{
                                            throw new ExceptionSyntaxique("'=' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                                        }
                                    } else{
                                        throw new ExceptionSyntaxique("':' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                                    }
                                } else{
                                    throw new ExceptionSyntaxique("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                                }
                            } else{
                                throw new ExceptionSyntaxique("'.' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                            }
                        } else{
                            throw new ExceptionSyntaxique("')' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                        }
                    } else{
                        throw new ExceptionSyntaxique("'(' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                    }
                } else{
                    throw new ExceptionSyntaxique("val attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                }
            } else {
                throw new ExceptionSyntaxique("' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        }
        else if (tokenTag() == "("){
            advanceToken();
            expression();
            if (tokenTag() == ")"){
                advanceToken();
                Precur();
                I1();
                T1();
                expression1();
                if(tokenTag() == "DOT"){
                    advanceToken();
                    if (tokenTag() == "VARIABLE"){
                        advanceToken();
                        if (tokenTag() == ":"){
                            advanceToken();
                            if (tokenTag() == "="){
                                advanceToken();
                                expression();
                                if (tokenTag() == ";"){
                                    advanceToken();
                                    return;
                                } else{
                                    throw new ExceptionSyntaxique("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                                }
                            } else{
                                throw new ExceptionSyntaxique("'=' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                            }
                        } else{
                            throw new ExceptionSyntaxique("':' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                        }
                    } else{
                        throw new ExceptionSyntaxique("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                    }
                } else{
                    throw new ExceptionSyntaxique("'.' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                }
            } else{
                throw new ExceptionSyntaxique("')' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        }
        else if(tokenTag() == "return"){
            System.out.println("r78");
            advanceToken();
            instruction2();
            return;
        }
        else if(tokenTag() == "begin"){
            System.out.println("r79");
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
            System.out.println("r80");
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
            System.out.println("r81");
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
            instruction4();
        }
        else if(tokenTag() == "while"){
            System.out.println("r82");
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
            throw new ExceptionSyntaxique("identificateur ou entier ou chaine ou 'true' ou 'false' ou 'null' ou '(' ou 'not' ou '-' ou 'new' ou 'character' ou 'return' ou 'begin' ou 'if' ou 'for' ou 'while' ou 'put' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }

    public void instruction1() throws ExceptionSyntaxique {
        System.out.print("instruction1 : ");
        if (tokenTag() == ":"){
            System.out.println("r83");
            advanceToken();
            if (tokenTag() == "="){
                advanceToken();
                expression();
                if (tokenTag() == ";"){
                    advanceToken();
                    return;
                } else{
                    throw new ExceptionSyntaxique("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                }
            } else{
                throw new ExceptionSyntaxique("'=' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        }
        else if (tokenTag() == ";"){
            System.out.println("r84");
            advanceToken();
            return;
        }
        else if (tokenTag() == "("){
            System.out.println("r85");
            advanceToken();
            expressionPlusVirgule();
            if (tokenTag() == ")"){
                advanceToken();
                instruction11();
                return;
            } else{
                throw new ExceptionSyntaxique("')' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        }
        else if (tokenTag() == "DOT"){
            System.out.println("r86");
            advanceToken();
            if (tokenTag() == "VARIABLE"){
                advanceToken();
                I1();
                T1();
                expression1();
                if (tokenTag() == "DOT"){
                    advanceToken();
                    if (tokenTag() == "VARIABLE"){
                        advanceToken();
                        if (tokenTag() == ":"){
                            advanceToken();
                            if (tokenTag() == "="){
                                advanceToken();
                                expression();
                                if (tokenTag() == ";"){
                                    advanceToken();
                                    return;
                                } else{
                                    throw new ExceptionSyntaxique("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                                }
                            } else{
                                throw new ExceptionSyntaxique("'=' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                            }
                        } else{
                            throw new ExceptionSyntaxique("':' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                        }
                    } else{
                        throw new ExceptionSyntaxique("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                    }
                } else{
                    throw new ExceptionSyntaxique("'.' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                }
            } else{
                throw new ExceptionSyntaxique("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        }
        else if (tokenTag() == "="
        || tokenTag() == "=/"
        || tokenTag() == "<"
        || tokenTag() == "<="
        || tokenTag() == ">"
        || tokenTag() == ">="
        || tokenTag() == "rem"
        || tokenTag() == "+"
        || tokenTag() == "-"
        || tokenTag() == "*"
        || tokenTag() == "/"
        || tokenTag() == ";"
        || tokenTag() == ")"
        || tokenTag() == "loop"
        || tokenTag() == "then"
        || tokenTag() == "DOTDOT"){
            System.out.println("r87");
            I1();
            T1();
            expression1();
            if (tokenTag() == "DOT"){
                advanceToken();
                if (tokenTag() == "VARIABLE"){
                    advanceToken();
                    if (tokenTag() == ":"){
                        advanceToken();
                        if (tokenTag() == "="){
                            advanceToken();
                            expression();
                            if (tokenTag() == ";"){
                                advanceToken();
                                return;
                            } else{
                                throw new ExceptionSyntaxique("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                            }
                        } else{
                            throw new ExceptionSyntaxique("'=' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                        }
                    } else{
                        throw new ExceptionSyntaxique("':' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                    }
                } else{
                    throw new ExceptionSyntaxique("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                }
            } else{
                throw new ExceptionSyntaxique("'.' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        } else {
            throw new ExceptionSyntaxique("';' ou '.' ou '=' ou '/=' ou '<' ou '<=' ou '>' ou '>=' ou '+' ou '-' ou '*' ou '/' ou 'rem' ou '+' ou '-' ou ';' ou ')' ou 'loop' ou 'then' ou '..' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }

    public void instruction11() throws ExceptionSyntaxique {
        System.out.print("instruction11 : ");
        if (tokenTag() == ";"){
            System.out.println("r88");
            advanceToken();
            return;
        }
        else if (tokenTag() == "DOT"
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
        || tokenTag() == "rem"){
            System.out.println("r89");
            Precur();
            I1();
            T1();
            expression1();
            if (tokenTag() == "DOT"){
                advanceToken();
                if (tokenTag() == "VARIABLE"){
                    advanceToken();
                    if (tokenTag() == ":"){
                        advanceToken();
                        if (tokenTag() == "="){
                            advanceToken();
                            expression();
                            if (tokenTag() == ";"){
                                advanceToken();
                                return;
                            } else{
                                throw new ExceptionSyntaxique("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                            }
                        } else{
                            throw new ExceptionSyntaxique("'=' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                        }
                    } else{
                        throw new ExceptionSyntaxique("':' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                    }
                } else{
                    throw new ExceptionSyntaxique("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                }
            } else{
                throw new ExceptionSyntaxique("'.' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        } else {
            throw new ExceptionSyntaxique("';' ou '.' ou '=' ou '/=' ou '<' ou '<=' ou '>' ou '>=' ou '+' ou '-' ou '*' ou '/' ou 'rem' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }

    public void instruction2() throws ExceptionSyntaxique {
        System.out.print("instruction2 : ");
        if (tokenTag() == ";"){
            System.out.println("r91");
            advanceToken();
            return;
        }
        else if (tokenTag() == "NUM_CONST"
        || tokenTag() == "STR_CONST"
        || tokenTag() == "true"
        || tokenTag() == "false"
        || tokenTag() == "null"
        || tokenTag() == "("
        || tokenTag() == "not"
        || tokenTag() == "-"
        || tokenTag() == "new"
        || tokenTag() == "character"
        || tokenTag() == "VARIABLE" ){
            System.out.println("r90");
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
        if (tokenTag() == "end"){
            System.out.println("r92");
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
            System.out.println("r93");
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
            System.out.println("r94");
            elsifPlus();
            instruction3();
        } else{
            throw new ExceptionSyntaxique("'else' ou 'end' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }

    public void instruction4() throws ExceptionSyntaxique {
        System.out.print("instruction4 : ");
        if (tokenTag() == "NUM_CONST"
        || tokenTag() == "STR_CONST"
        || tokenTag() == "true"
        || tokenTag() == "false"
        || tokenTag() == "null"
        || tokenTag() == "("
        || tokenTag() == "not"
        || tokenTag() == "-"
        || tokenTag() == "new"
        || tokenTag() == "character"
        || tokenTag() == "VARIABLE" ){
            System.out.println("r95");
            expression();
            if (tokenTag() == "DOTDOT") {
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
            System.out.println("r96");
            advanceToken();
            if (tokenTag() == "NUM_CONST"
            || tokenTag() == "STR_CONST"
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
                if (tokenTag() == "DOTDOT") {
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

    public void instructionPlus() throws ExceptionSyntaxique {
        System.out.print("instructionPlus : ");
        if (tokenTag() == "VARIABLE" 
        || tokenTag() == "NUM_CONST" 
        || tokenTag() == "STR_CONST" 
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
            System.out.println("r97");
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
        || tokenTag() == "STR_CONST" 
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
            System.out.println("r98");
            instructionPlus();
        } else {
            System.out.println("r99");
            return;
        }
    }

    public void declarationPlus() throws ExceptionSyntaxique {
        System.out.print("declarationPlus : ");
        if (tokenTag() == "type" || tokenTag() == "procedure" || tokenTag() == "function" || tokenTag() == "VARIABLE") {
            System.out.println("r100");
            declaration();
            declarationPlus1();
        } else {
            throw new ExceptionSyntaxique("'type', 'procedure', 'function' ou identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }

    public void declarationPlus1() throws ExceptionSyntaxique {
        System.out.print("declarationPlus1 : ");
        if (tokenTag() == "type" || tokenTag() == "procedure" || tokenTag() == "function" || tokenTag() == "VARIABLE") {
            System.out.println("r101");
            declarationPlus();
        } else {
            System.out.println("r102");
            return;
        }
    } 
    
    
    public void champsPlus() throws ExceptionSyntaxique {
        System.out.print("champsPlus : ");
        if (tokenTag() == "VARIABLE"){
            System.out.println("r103");
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
            System.out.println("r104");
            champsPlus();
        }
        else{
            System.out.println("r105");
            return;
        }
    }



    public void identificateurVirgulePlus() throws ExceptionSyntaxique {
        System.out.print("identificateurVirgulePlus : ");
        if (tokenTag() == "VARIABLE") {
            System.out.println("r106");
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
            System.out.println("r107");
            advanceToken();
            if (tokenTag() == "VARIABLE") {
                identificateurVirgulePlus();
            } else {
                throw new ExceptionSyntaxique("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        } else{
            System.out.println("r108");
            return;
        }
    }

    public void paramPointVirgulePlus() throws ExceptionSyntaxique {
        System.out.print("paramPointVirgulePlus : ");
        if (tokenTag() == "VARIABLE") {
            System.out.println("r109");
            param();
            paramPointVirgulePlus1();
        } else {
            throw new ExceptionSyntaxique("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }

    public void paramPointVirgulePlus1() throws ExceptionSyntaxique {
        System.out.print("paramPointVirgulePlus1 : ");
        if (tokenTag() == ";") {
            System.out.println("r110");
            advanceToken();
            paramPointVirgulePlus();
        } else {
            System.out.println("r111");
            return;
        }
    }  

    public void expressionPlusVirgule() throws ExceptionSyntaxique {
        System.out.print("expressionPlusVirgule : ");
        if (tokenTag() == "NUM_CONST"
        || tokenTag() == "STR_CONST"
        || tokenTag() == "true"
        || tokenTag() == "false"
        || tokenTag() == "null"
        || tokenTag() == "("
        || tokenTag() == "not"
        || tokenTag() == "-"
        || tokenTag() == "new"
        || tokenTag() == "character"
        || tokenTag() == "VARIABLE" ){
            System.out.println("r112");
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
            System.out.println("r113");
            advanceToken();
            expressionPlusVirgule();
        }
        else{
            System.out.println("r114");
            return;
        }
    }


    public void elsifPlus() throws ExceptionSyntaxique {
        System.out.print("elsifPlus : ");
        if (tokenTag() == "elsif"){
            System.out.println("r115");
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
            System.out.println("r116");
            elsifPlus();
        }
        else{
            System.out.println("r117");
            return;
        }
    }
}


