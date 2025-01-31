package eu.telecomnancy.application;

import java.util.ArrayList;

import eu.telecomnancy.application.exception.ExceptionSyntaxique;
import eu.telecomnancy.application.token.IntegerToken;
import eu.telecomnancy.application.token.Token;
import eu.telecomnancy.application.token.VarToken;


public class ParserV3
{
    private ArrayList<Token> tokens;
    private int currentTokenIndex = 0;

    public ParserV3(ArrayList<Token> tokens)
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

    public String tokenTag() throws ExceptionSyntaxique //Fonction permettant de récupérer le tag du token suivant
    {
        if (getNextToken() == null)
        {
            throw new ExceptionSyntaxique("Erreur : fin de fichier inattendue");
        }
        else
        {
            return getNextToken().getTag();
        }
    }

    private void printErrorAndSkip(String errorMessage) throws ExceptionSyntaxique {
        System.out.println("Erreur syntaxique : " + errorMessage);
        skipErrors();
    }

    private void skipErrors() throws ExceptionSyntaxique {
        while (currentTokenIndex < this.tokens.size()) {
            // Avancer dans la liste des tokens
            //System.out.println("Token actuel : " + tokenTag());
            currentTokenIndex++;

            // On arrête le saut si on trouve un point où la reprise est possible (par exemple, un point-virgule)
            if (tokenTag() == ";") {
                advanceToken();
                break;
            }
        }
    }

    public TreeNode parse() throws ExceptionSyntaxique
    {
        // On crée le noeud racine de l'arbre syntaxique.
        TreeNode root = new TreeNode("ROOT");

        //On démarre par l'analyse de l'axiome
        axiome(root);

        return root;
    }

    public void axiome(TreeNode root) throws ExceptionSyntaxique {

        System.out.println("axiome");
        //Pour l'axiome, on vérifie que le premier token est bien un "with"
        if (tokenTag() == "with") {
            // On ajoute un noeud.
            root.addSon(new TreeNode(tokenTag()));

            advanceToken();
        } else {
            printErrorAndSkip("'with' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }

        //On vérifie que le token suivant est bien une librairie
        if (tokenTag() == "LIB") {
            // On ajoute un noeud.
            root.addSon(new TreeNode(tokenTag()));

            advanceToken();
        } else {
            printErrorAndSkip("'Librairie' attendue");
        }

        //On vérifie que le token suivant est bien un ";"
        if (tokenTag() == ";") {
            // On ajoute un noeud.
            root.addSon(new TreeNode(tokenTag()));

            advanceToken();
        } else {
            printErrorAndSkip("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }

        //On vérifie que le token suivant est bien un "use"
        if (tokenTag() == "use") {
            // On ajoute un noeud.
            root.addSon(new TreeNode(tokenTag()));

            advanceToken();
        } else {
            printErrorAndSkip("'use' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }

        //On vérifie que le token suivant est bien une librairie
        if (tokenTag() == "LIB") {
            // On ajoute un noeud.
            root.addSon(new TreeNode(tokenTag()));

            advanceToken();
        } else {
            printErrorAndSkip("'Librairie' attendue");
        }

        //On vérifie que le token suivant est bien un ";"
        if (tokenTag() == ";") {
            // On ajoute un noeud.
            root.addSon(new TreeNode(tokenTag()));

            advanceToken();
        } else {
            printErrorAndSkip("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }

        //On vérifie que le token suivant est bien un début de procedure
        if (tokenTag() == "procedure") {
            // On ajoute un noeud.
            root.addSon(new TreeNode(tokenTag()));

            advanceToken();
        } else {
            printErrorAndSkip("'procedure' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }

        //On vérifie que le token suivant est bien l'identificateur de la procedure
        if (tokenTag() == "VARIABLE") {
            // On ajoute un noeud.
            root.addSon(new TreeNode(((VarToken) getNextToken()).getValue()));

            advanceToken();
        } else {
            printErrorAndSkip("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }

        //On vérifie que le token suivant est bien un "is" et on passe dans la règle fichier2
        if (tokenTag() == "is") {
            // On ajoute un noeud.
            root.addSon(new TreeNode(tokenTag()));

            advanceToken();
            fichier2(root);
        } else {
            printErrorAndSkip("'is' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }

    public void fichier2(TreeNode pere) throws ExceptionSyntaxique {
        // On crée un nouveau noeud.
        TreeNode noeud = new TreeNode("fich2");

        // On l'ajoute à l'arbre.
        pere.addSon(noeud);


        System.out.print("fichier2 : ");
        // Si le token suivant est un "begin", on est bien dans la première réduction de la règle fichier2 et on passe dans la règle instructionPlus
        if (tokenTag() == "begin") {
            // On ajoute un noeud.
            noeud.addSon(new TreeNode(tokenTag()));

            System.out.println("r2");
            advanceToken();
            instructionPlus(noeud);
            if (tokenTag() == "end") {
                // On ajoute un noeud.
                noeud.addSon(new TreeNode(tokenTag()));

                advanceToken();
                fichier3(noeud);
                return;
            } else {
                printErrorAndSkip("'end' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        }
        // Sinon, on est dans la deuxième réduction de la règle fichier2, le prochain token doit être un "type" ou un "procedure" ou un "function"
        // On passe donc dans la règle declarationPlus sans avancer la lecture des tokens
        else if (tokenTag() == "type" || tokenTag() == "procedure" || tokenTag() == "function" || tokenTag() == "VARIABLE") {
            System.out.println("r3");
            declarationPlus(noeud);
            if (tokenTag() == "begin") {
                // On ajoute un noeud.
                noeud.addSon(new TreeNode(tokenTag()));

                advanceToken();
                instructionPlus(noeud);
                if (tokenTag() == "end") {
                    // On ajoute un noeud.
                    noeud.addSon(new TreeNode(tokenTag()));

                    advanceToken();
                    fichier3(noeud);
                    return;
                } else {
                    printErrorAndSkip("'end' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                }
            } else {
                printErrorAndSkip("'begin' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        } else {
            printErrorAndSkip("'begin', 'type', 'procedure', 'function' ou identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }

    public void fichier3(TreeNode pere) throws ExceptionSyntaxique {
        // On crée un nouveau noeud.
        TreeNode noeud = new TreeNode("fich3");

        // On l'ajoute à l'arbre.
        pere.addSon(noeud);

        System.out.print("fichier3 : ");
        // S'il n'y a plus de token, on est bien dans la première réduction de la règle fichier3, c'est la fin du fichier
        if (tokenTag()== ";") {
            // On ajoute un noeud.
            noeud.addSon(new TreeNode(tokenTag()));

            advanceToken();
            System.out.println("r4");
            System.out.println("Fin de fichier");
            return;
        }
        //Sinon, le prochain token doit être l'identificateur de la procedure et on rappelle fichier3 pour fermer la procedure principale
        else if (tokenTag() == "VARIABLE") {
            // On ajoute un noeud.
            noeud.addSon(new TreeNode(((VarToken) getNextToken()).getValue()));

            System.out.println("r5");
            advanceToken();
            if (tokenTag() == ";") {
                // On ajoute un noeud.
                noeud.addSon(new TreeNode(tokenTag()));

                advanceToken();
            } else {
                printErrorAndSkip("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        } else {
            printErrorAndSkip("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }


    public void declaration(TreeNode pere) throws ExceptionSyntaxique {
        // On crée un nouveau noeud.
        TreeNode noeud = new TreeNode("decl");

        // On l'ajoute à l'arbre.
        pere.addSon(noeud);

        System.out.print("declaration : ");
        if (tokenTag() == "type") {
            // On ajoute un noeud.
            noeud.addSon(new TreeNode(tokenTag()));

            System.out.println("r6");
            advanceToken();
            if (tokenTag() == "VARIABLE") {
                // On ajoute un noeud.
                noeud.addSon(new TreeNode(((VarToken) getNextToken()).getValue()));

                advanceToken();
                declaration12(noeud);
                return;
            } else {
                printErrorAndSkip("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        } else if (tokenTag() == "procedure") {
            // On ajoute un noeud.
            noeud.addSon(new TreeNode(tokenTag()));

            System.out.println("r7");
            advanceToken();
            if (tokenTag() == "VARIABLE") {
                // On ajoute un noeud.
                noeud.addSon(new TreeNode(((VarToken) getNextToken()).getValue()));

                advanceToken();
                declaration21(noeud);
                return;
            } else {
                printErrorAndSkip("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        } else if (tokenTag() == "function") {
            // On ajoute un noeud.
            noeud.addSon(new TreeNode(tokenTag()));

            advanceToken();
            System.out.println("r8");
            if (tokenTag() == "VARIABLE") {
                // On ajoute un noeud.
                noeud.addSon(new TreeNode(((VarToken) getNextToken()).getValue()));

                advanceToken();
                declaration31(noeud);
            } else {
                printErrorAndSkip("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        } else if (tokenTag() == "VARIABLE") {
            System.out.println("r9");
            identificateurVirgulePlus(noeud);
            if (tokenTag() == ":") {
                // On ajoute un noeud.
                noeud.addSon(new TreeNode(tokenTag()));

                advanceToken();
                type(noeud);
                declaration13(noeud);
            } else {
                printErrorAndSkip("':' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        } else {
            printErrorAndSkip("'type', 'procedure', 'function' ou identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }

    public void declaration12(TreeNode pere) throws ExceptionSyntaxique {
        // On crée un nouveau noeud.
        TreeNode noeud = new TreeNode("decl12");

        // On l'ajoute à l'arbre.
        pere.addSon(noeud);

        System.out.print("declaration12 : ");
        if (tokenTag() == ";") {
            // On ajoute un noeud.
            noeud.addSon(new TreeNode(tokenTag()));

            System.out.println("r10");
            advanceToken();
            return;
        } else if (tokenTag() == "is") {
            // On ajoute un noeud.
            noeud.addSon(new TreeNode(tokenTag()));

            System.out.println("r11");
            advanceToken();
            declaration14(noeud);
        } else {
            printErrorAndSkip("'is' ou ';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }

    public void declaration13(TreeNode pere) throws ExceptionSyntaxique {
        // On crée un nouveau noeud.
        TreeNode noeud = new TreeNode("decl13");

        // On l'ajoute à l'arbre.
        pere.addSon(noeud);

        System.out.print("declaration13 : ");
        if (tokenTag() == ";") {
            // On ajoute un noeud.
            noeud.addSon(new TreeNode(tokenTag()));

            System.out.println("r12");
            advanceToken();
            return;
        } else if (tokenTag() == ":") {
            // On ajoute un noeud.
            noeud.addSon(new TreeNode(tokenTag()));

            System.out.println("r13");
            advanceToken();
            if (tokenTag() == "=") {
                // On ajoute un noeud.
                noeud.addSon(new TreeNode(tokenTag()));

                advanceToken();
                expression(noeud);
                if (tokenTag() == ";") {
                    // On ajoute un noeud.
                    noeud.addSon(new TreeNode(tokenTag()));

                    advanceToken();
                    return;
                } else {
                    printErrorAndSkip("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                }
            } else {
                printErrorAndSkip("'=' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        } else {
            printErrorAndSkip("':' ou ';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }

    public void declaration14(TreeNode pere) throws ExceptionSyntaxique {
        // On crée un nouveau noeud.
        TreeNode noeud = new TreeNode("decl14");

        // On l'ajoute à l'arbre.
        pere.addSon(noeud);

        System.out.print("declaration14 : ");
        if (tokenTag() == "access"){
            // On ajoute un noeud.
            noeud.addSon(new TreeNode(tokenTag()));

            System.out.println("r14");
            advanceToken();
            if (tokenTag() == "VARIABLE"){
                // On ajoute un noeud.
                noeud.addSon(new TreeNode(((VarToken) getNextToken()).getValue()));

                advanceToken();
            }
            else{
                printErrorAndSkip("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
            if (tokenTag() == ";"){
                // On ajoute un noeud.
                noeud.addSon(new TreeNode(tokenTag()));

                advanceToken();
                return;
            }
            else{
                printErrorAndSkip("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        }
        else if (tokenTag() == "record"){
            // On ajoute un noeud.
            noeud.addSon(new TreeNode(tokenTag()));

            System.out.println("r15");
            advanceToken();
            champsPlus(noeud);
            if (tokenTag() == "end"){
                // On ajoute un noeud.
                noeud.addSon(new TreeNode(tokenTag()));

                advanceToken();
            }else{
                printErrorAndSkip("'end' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
            if (tokenTag() == "record"){
                // On ajoute un noeud.
                noeud.addSon(new TreeNode(tokenTag()));

                advanceToken();
            } else{
                printErrorAndSkip("'record' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
            if (tokenTag() == ";"){
                // On ajoute un noeud.
                noeud.addSon(new TreeNode(tokenTag()));

                advanceToken();
                return;
            } else{
                printErrorAndSkip("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        } else {
            printErrorAndSkip("'access' ou 'record' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }

    public void declaration21(TreeNode pere) throws ExceptionSyntaxique {
        // On crée un nouveau noeud.
        TreeNode noeud = new TreeNode("decl21");

        // On l'ajoute à l'arbre.
        pere.addSon(noeud);

        System.out.print("declaration21 : ");
        if (tokenTag() == "is"){
            // On ajoute un noeud.
            noeud.addSon(new TreeNode(tokenTag()));

            System.out.println("r16");
            advanceToken();
            declaration22(noeud);
            return;
        }
        else if(tokenTag() == "("){
            // On ajoute un noeud.
            noeud.addSon(new TreeNode(tokenTag()));

            System.out.println("r17");
            params(noeud);
            if (tokenTag() == "is"){
                // On ajoute un noeud.
                noeud.addSon(new TreeNode(tokenTag()));

                advanceToken();
                declaration22(noeud);
            }
            else{
                printErrorAndSkip("'is' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        }
        else{
            printErrorAndSkip("'is' ou '(' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }

    public void declaration22(TreeNode pere) throws ExceptionSyntaxique {
        // On crée un nouveau noeud.
        TreeNode noeud = new TreeNode("decl22");

        // On l'ajoute à l'arbre.
        pere.addSon(noeud);

        System.out.print("declaration22 : ");
        if (tokenTag() == "begin") {
            // On ajoute un noeud.
            noeud.addSon(new TreeNode(tokenTag()));

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
                    || tokenTag() == "character'val"
                    || tokenTag() == "begin"
                    || tokenTag() == "if"
                    || tokenTag() == "for"
                    || tokenTag() == "while"
                    || tokenTag() == "put") {
                instructionPlus(noeud);
                if(tokenTag() == "end"){
                    // On ajoute un noeud.
                    noeud.addSon(new TreeNode(tokenTag()));

                    advanceToken();
                    declaration23(noeud);
                }
                else{
                    printErrorAndSkip("'end' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                }
            } else {
                printErrorAndSkip("identificateur ou entier ou chaine ou 'true' ou 'false' ou 'null' ou '(' ou 'not' ou '-' ou 'new' ou 'character val'ou 'begin' ou 'if' ou 'for' ou 'while' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        } else if (tokenTag() == "type" || tokenTag() == "procedure" || tokenTag() == "function" || tokenTag() == "VARIABLE") {
            System.out.println("r19");
            declarationPlus(noeud);
            if (tokenTag() == "begin") {
                // On ajoute un noeud.
                noeud.addSon(new TreeNode(tokenTag()));

                advanceToken();
                instructionPlus(noeud);
                if(tokenTag() == "end"){
                    // On ajoute un noeud.
                    noeud.addSon(new TreeNode(tokenTag()));

                    advanceToken();
                    declaration23(noeud);
                }
                else{
                    printErrorAndSkip("'end' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                }
            } else {
                printErrorAndSkip("'begin' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        } else {
            printErrorAndSkip("'begin', 'type', 'procedure', 'function' ou identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }

    public void declaration23(TreeNode pere) throws ExceptionSyntaxique {
        // On crée un nouveau noeud.
        TreeNode noeud = new TreeNode("decl23");

        // On l'ajoute à l'arbre.
        pere.addSon(noeud);

        System.out.print("declaration23 : ");
        if (tokenTag() == ";") {
            // On ajoute un noeud.
            noeud.addSon(new TreeNode(tokenTag()));

            System.out.println("r20");
            advanceToken();
            return;
        } else if (tokenTag() == "VARIABLE") {
            // On ajoute un noeud.
            noeud.addSon(new TreeNode(((VarToken) getNextToken()).getValue()));

            System.out.println("r21");
            advanceToken();
            if (tokenTag() == ";") {
                // On ajoute un noeud.
                noeud.addSon(new TreeNode(tokenTag()));

                advanceToken();
            } else {
                printErrorAndSkip("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        } else {
            printErrorAndSkip("';' ou identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }

    public void declaration31(TreeNode pere) throws ExceptionSyntaxique {
        // On crée un nouveau noeud.
        TreeNode noeud = new TreeNode("decl31");

        // On l'ajoute à l'arbre.
        pere.addSon(noeud);

        System.out.print("declaration31 : ");
        if (tokenTag() == "return") {
            // On ajoute un noeud.
            noeud.addSon(new TreeNode(tokenTag()));

            System.out.println("r22");
            advanceToken();
            type(noeud);
            if (tokenTag() == "is") {
                // On ajoute un noeud.
                noeud.addSon(new TreeNode(tokenTag()));

                advanceToken();
                declaration22(noeud);
            } else {
                printErrorAndSkip("'is' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        } else if (tokenTag() == "(") {
            System.out.println("r23");
            params(noeud);
            if (tokenTag() == "return") {
                // On ajoute un noeud.
                noeud.addSon(new TreeNode(tokenTag()));

                advanceToken();
                type(noeud);
                if (tokenTag() == "is") {
                    // On ajoute un noeud.
                    noeud.addSon(new TreeNode(tokenTag()));

                    advanceToken();
                    declaration22(noeud);
                } else {
                    printErrorAndSkip("'is' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                }
            } else {
                printErrorAndSkip("'return' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        } else {
            printErrorAndSkip("'return' ou '(' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }

    public void champs(TreeNode pere) throws ExceptionSyntaxique {
        // On crée un nouveau noeud.
        TreeNode noeud = new TreeNode("champs");

        // On l'ajoute à l'arbre.
        pere.addSon(noeud);

        System.out.print("champs : ");
        if (tokenTag() == "VARIABLE"){
            System.out.println("r24");
            identificateurVirgulePlus(noeud);
            if (tokenTag() == ":"){
                // On ajoute un noeud.
                noeud.addSon(new TreeNode(tokenTag()));

                advanceToken();
            } else{
                printErrorAndSkip("':' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());

            }
            type(noeud);
            if (tokenTag() == ";"){
                // On ajoute un noeud.
                noeud.addSon(new TreeNode(tokenTag()));

                advanceToken();
                return;
            } else{
                printErrorAndSkip("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        }
        else{
            printErrorAndSkip("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }

    public void type(TreeNode pere) throws ExceptionSyntaxique {
        // On crée un nouveau noeud.
        TreeNode noeud = new TreeNode("type");

        // On l'ajoute à l'arbre.
        pere.addSon(noeud);

        System.out.print("type : ");
        if (tokenTag() == "VARIABLE") {
            // On ajoute un noeud.
            noeud.addSon(new TreeNode(((VarToken) getNextToken()).getValue()));

            System.out.println("r25");
            advanceToken();
            return;
        } else if (tokenTag() == "access") {
            // On ajoute un noeud.
            noeud.addSon(new TreeNode(tokenTag()));

            System.out.println("r26");
            advanceToken();
            if (tokenTag() == "VARIABLE") {
                // On ajoute un noeud.
                noeud.addSon(new TreeNode(((VarToken) getNextToken()).getValue()));

                advanceToken();
                return;
            } else {
                printErrorAndSkip("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        } else {
            printErrorAndSkip("identificateur ou 'access' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }

    public void params(TreeNode pere) throws ExceptionSyntaxique {
        // On crée un nouveau noeud.
        TreeNode noeud = new TreeNode("params");

        // On l'ajoute à l'arbre.
        pere.addSon(noeud);

        System.out.print("params : ");
        if (tokenTag() == "(") {
            // On ajoute un noeud.
            noeud.addSon(new TreeNode(tokenTag()));

            System.out.println("r27");
            advanceToken();
            paramPointVirgulePlus(noeud);
            if (tokenTag() == ")") {
                // On ajoute un noeud.
                noeud.addSon(new TreeNode(tokenTag()));

                advanceToken();
                return;
            } else {
                printErrorAndSkip("')' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        } else {
            printErrorAndSkip("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }

    public void param(TreeNode pere) throws ExceptionSyntaxique {
        // On crée un nouveau noeud.
        TreeNode noeud = new TreeNode("param");

        // On l'ajoute à l'arbre.
        pere.addSon(noeud);

        System.out.print("param : ");
        if (tokenTag() == "VARIABLE") {
            System.out.println("r28");
            identificateurVirgulePlus(noeud);
            if (tokenTag() == ":") {
                // On ajoute un noeud.
                noeud.addSon(new TreeNode(tokenTag()));

                advanceToken();
            } else {
                printErrorAndSkip("':' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
            if (tokenTag() == "VARIABLE" || tokenTag() == "access") {
                param2(noeud);
            } else {
                printErrorAndSkip("identificateur ou 'access' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        }else {
            printErrorAndSkip("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }


    public void param2(TreeNode pere) throws ExceptionSyntaxique {
        // On crée un nouveau noeud.
        TreeNode noeud = new TreeNode("param2");

        // On l'ajoute à l'arbre.
        pere.addSon(noeud);

        System.out.print("param2 : ");
        if (tokenTag() == "VARIABLE" || tokenTag() == "access") {
            System.out.println("r29");
            type(noeud);
            return;
        } else if (tokenTag() == "in") {
            System.out.println("r30");
            mode(noeud);
        } else {
            printErrorAndSkip("identificateur ou 'access' ou 'in' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }

    public void mode(TreeNode pere) throws ExceptionSyntaxique {
        // On crée un nouveau noeud.
        TreeNode noeud = new TreeNode("mode");

        // On l'ajoute à l'arbre.
        pere.addSon(noeud);

        System.out.print("mode : ");
        if (tokenTag() == "in") {
            // On ajoute un noeud.
            noeud.addSon(new TreeNode(tokenTag()));

            System.out.println("r31");
            advanceToken();
            mode1(noeud);
        } else {
            printErrorAndSkip("'in' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }

    public void mode1(TreeNode pere) throws ExceptionSyntaxique {
        // On crée un nouveau noeud.
        TreeNode noeud = new TreeNode("mode1");

        // On l'ajoute à l'arbre.
        pere.addSon(noeud);

        System.out.print("mode1 : ");
        if (tokenTag() == "out") {
            // On ajoute un noeud.
            noeud.addSon(new TreeNode(tokenTag()));

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


    public void expression(TreeNode pere) throws ExceptionSyntaxique {
        // On crée un nouveau noeud.
        TreeNode noeud = new TreeNode("expr");

        // On l'ajoute à l'arbre.
        pere.addSon(noeud);

        System.out.print("expression : ");
        if (tokenTag() == "-"
                || tokenTag() == "not"
                || tokenTag() == "NUM_CONST"
                || tokenTag() == "STR_CONST"
                || tokenTag() == "true"
                || tokenTag() == "false"
                || tokenTag() == "null"
                || tokenTag() == "new"
                || tokenTag() == "character'val"
                || tokenTag() == "("
                || tokenTag() == "VARIABLE") {
            System.out.println("r34");
            T(noeud);
            expression1(noeud);
        } else {
            printErrorAndSkip("identificateur ou entier ou chaine ou 'true' ou 'false' ou 'null' ou '(' ou 'not' ou '-' ou 'new' ou 'character val'attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }

    public void expression1(TreeNode pere) throws ExceptionSyntaxique {
        // On crée un nouveau noeud.
        TreeNode noeud = new TreeNode("expr1");

        // On l'ajoute à l'arbre.
        pere.addSon(noeud);

        System.out.print("expression1 : ");
        if (tokenTag() == "+") {
            // On ajoute un noeud.
            noeud.addSon(new TreeNode(tokenTag()));

            System.out.println("r35");
            advanceToken();
            expression(noeud);
            return;
        } else if (tokenTag() == "-") {
            // On ajoute un noeud.
            noeud.addSon(new TreeNode(tokenTag()));

            System.out.println("r36");
            advanceToken();
            expression(noeud);
        } else {
            System.out.println("r37");
            return;
        }
    }

    public void T(TreeNode pere) throws ExceptionSyntaxique {
        // On crée un nouveau noeud.
        TreeNode noeud = new TreeNode("T");

        // On l'ajoute à l'arbre.
        pere.addSon(noeud);

        System.out.print("T : ");
        if (tokenTag() == "-"
                || tokenTag() == "not"
                || tokenTag() == "NUM_CONST"
                || tokenTag() == "STR_CONST"
                || tokenTag() == "true"
                || tokenTag() == "false"
                || tokenTag() == "null"
                || tokenTag() == "new"
                || tokenTag() == "character'val"
                || tokenTag() == "("
                || tokenTag() == "VARIABLE") {
            System.out.println("r38");
            I(noeud);
            T1(noeud);
        } else {
            printErrorAndSkip("identificateur ou entier ou chaine ou 'true' ou 'false' ou 'null' ou '(' ou 'not' ou '-' ou 'new' ou 'character val'attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }

    public void T1(TreeNode pere) throws ExceptionSyntaxique {
        // On crée un nouveau noeud.
        TreeNode noeud = new TreeNode("T1");

        // On l'ajoute à l'arbre.
        pere.addSon(noeud);

        System.out.print("T1 : ");
        if (tokenTag() == "*") {
            // On ajoute un noeud.
            noeud.addSon(new TreeNode(tokenTag()));

            System.out.println("r39");
            advanceToken();
            T(noeud);
            return;
        } else if (tokenTag() == "/") {
            // On ajoute un noeud.
            noeud.addSon(new TreeNode(tokenTag()));

            System.out.println("r40");
            advanceToken();
            T(noeud);
            return;
        } else {
            System.out.println("r41");
            return;
        }
    }

    public void I(TreeNode pere) throws ExceptionSyntaxique {
        // On crée un nouveau noeud.
        TreeNode noeud = new TreeNode("I");

        // On l'ajoute à l'arbre.
        pere.addSon(noeud);

        System.out.print("I : ");
        if (tokenTag() == "-"
                || tokenTag() == "not"
                || tokenTag() == "NUM_CONST"
                || tokenTag() == "STR_CONST"
                || tokenTag() == "true"
                || tokenTag() == "false"
                || tokenTag() == "null"
                || tokenTag() == "new"
                || tokenTag() == "character'val"
                || tokenTag() == "("
                || tokenTag() == "VARIABLE") {
            System.out.println("r42");
            F(noeud);
            I1(noeud);
        } else {
            printErrorAndSkip("identificateur ou entier ou chaine ou 'true' ou 'false' ou 'null' ou '(' ou 'not' ou '-' ou 'new' ou 'character val'attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }

    public void I1(TreeNode pere) throws ExceptionSyntaxique {
        // On crée un nouveau noeud.
        TreeNode noeud = new TreeNode("I1");

        // On l'ajoute à l'arbre.
        pere.addSon(noeud);

        System.out.print("I1 : ");
        if (tokenTag() == "=") {
            // On ajoute un noeud.
            noeud.addSon(new TreeNode(tokenTag()));

            System.out.println("r43");
            advanceToken();
            T(noeud);
            return;
        } else if (tokenTag() == "/=") {
            // On ajoute un noeud.
            noeud.addSon(new TreeNode(tokenTag()));

            System.out.println("r44");
            advanceToken();
            T(noeud);
            return;
        } else if (tokenTag() == "<") {
            // On ajoute un noeud.
            noeud.addSon(new TreeNode(tokenTag()));

            System.out.println("r45");
            advanceToken();
            T(noeud);
            return;
        } else if (tokenTag() == "<=") {
            // On ajoute un noeud.
            noeud.addSon(new TreeNode(tokenTag()));

            System.out.println("r46");
            advanceToken();
            T(noeud);
            return;
        } else if (tokenTag() == ">") {
            // On ajoute un noeud.
            noeud.addSon(new TreeNode(tokenTag()));

            System.out.println("r47");
            advanceToken();
            T(noeud);
            return;
        } else if (tokenTag() == ">=") {
            // On ajoute un noeud.
            noeud.addSon(new TreeNode(tokenTag()));

            System.out.println("r48");
            advanceToken();
            T(noeud);
            return;
        } else if (tokenTag() == "rem") {
            // On ajoute un noeud.
            noeud.addSon(new TreeNode(tokenTag()));

            System.out.println("r49");
            advanceToken();
            T(noeud);
            return;
        } else {
            System.out.println("r50");
            return;
        }
    }

    public void F(TreeNode pere) throws ExceptionSyntaxique {
        // On crée un nouveau noeud.
        TreeNode noeud = new TreeNode("F");

        // On l'ajoute à l'arbre.
        pere.addSon(noeud);

        System.out.print("F : ");
        if (tokenTag() == "NUM_CONST"
                || tokenTag() == "STR_CONST"
                || tokenTag() == "true"
                || tokenTag() == "false"
                || tokenTag() == "null"
                || tokenTag() == "new"
                || tokenTag() == "character'val"
                || tokenTag() == "("
                || tokenTag() == "VARIABLE") {
            System.out.println("r51");
            P(noeud);
            return;
        } else if (tokenTag() == "-") {
            // On ajoute un noeud.
            noeud.addSon(new TreeNode(tokenTag()));

            System.out.println("r52");
            advanceToken();
            if (tokenTag() == "NUM_CONST"
                    || tokenTag() == "STR_CONST"
                    || tokenTag() == "true"
                    || tokenTag() == "false"
                    || tokenTag() == "null"
                    || tokenTag() == "new"
                    || tokenTag() == "character'val"
                    || tokenTag() == "("
                    || tokenTag() == "VARIABLE") {
                P(noeud);
                return;
            } else {
                printErrorAndSkip("identificateur ou entier ou chaine ou 'true' ou 'false' ou 'null' ou '(' ou '-' ou 'new' ou 'character val'attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        } else if (tokenTag() == "not") {
            // On ajoute un noeud.
            noeud.addSon(new TreeNode(tokenTag()));

            System.out.println("r53");
            advanceToken();
            if (tokenTag() == "NUM_CONST"
                    || tokenTag() == "STR_CONST"
                    || tokenTag() == "true"
                    || tokenTag() == "false"
                    || tokenTag() == "null"
                    || tokenTag() == "new"
                    || tokenTag() == "character'val"
                    || tokenTag() == "("
                    || tokenTag() == "VARIABLE") {
                P(noeud);
                return;
            } else {
                printErrorAndSkip("identificateur ou entier ou chaine ou 'true' ou 'false' ou 'null' ou '(' ou 'not' ou 'new' ou 'character val'attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        } else {
            printErrorAndSkip("identificateur ou entier ou chaine ou 'true' ou 'false' ou 'null' ou '(' ou 'not' ou '-' ou 'new' ou 'character val'attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }

    public void P(TreeNode pere) throws ExceptionSyntaxique {
        // On crée un nouveau noeud.
        TreeNode noeud = new TreeNode("P");

        // On l'ajoute à l'arbre.
        pere.addSon(noeud);

        System.out.print("P : ");
        if (tokenTag() == "NUM_CONST"){
            // On ajoute un noeud.
            noeud.addSon(new TreeNode(Integer.toString(((IntegerToken) getNextToken()).getValue())));

            System.out.println("r54");
            advanceToken();
            Precur(noeud);
            return;
        }
        else if (tokenTag() == "STR_CONST"){
            // On ajoute un noeud.
            noeud.addSon(new TreeNode(tokenTag()));

            System.out.println("r55");
            advanceToken();
            Precur(noeud);
            return;
        }
        else if (tokenTag() == "true"){
            // On ajoute un noeud.
            noeud.addSon(new TreeNode(tokenTag()));

            System.out.println("r56");
            advanceToken();
            Precur(noeud);
            return;
        }
        else if (tokenTag() == "false"){
            // On ajoute un noeud.
            noeud.addSon(new TreeNode(tokenTag()));

            System.out.println("r57");
            advanceToken();
            Precur(noeud);
            return;
        }
        else if (tokenTag() == "null"){
            // On ajoute un noeud.
            noeud.addSon(new TreeNode(tokenTag()));

            System.out.println("r58");
            advanceToken();
            Precur(noeud);
            return;
        }
        else if (tokenTag() == "new"){
            // On ajoute un noeud.
            noeud.addSon(new TreeNode(tokenTag()));

            System.out.println("r59");
            advanceToken();
            if (tokenTag() == "VARIABLE"){
                // On ajoute un noeud.
                noeud.addSon(new TreeNode(((VarToken) getNextToken()).getValue()));

                advanceToken();
                Precur(noeud);
                return;
            }
            else{
                printErrorAndSkip("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        }
        else if (tokenTag() == "character'val"){
            // On ajoute un noeud.
            noeud.addSon(new TreeNode(tokenTag()));
            System.out.println("r60");
            advanceToken();
            if (tokenTag() == "("){
                // On ajoute un noeud.
                noeud.addSon(new TreeNode(tokenTag()));

                advanceToken();
                if (tokenTag() == "NUM_CONST"){
                    // On ajoute un noeud.
                    noeud.addSon(new TreeNode(Integer.toString(((IntegerToken) getNextToken()).getValue())));

                    advanceToken();
                    if (tokenTag() == ")"){
                        // On ajoute un noeud.
                        noeud.addSon(new TreeNode(tokenTag()));

                        advanceToken();
                        Precur(noeud);
                        return;
                    } else{
                        printErrorAndSkip("')' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                    }
                } else{
                    printErrorAndSkip("entier attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                }
            } else{
                printErrorAndSkip("'(' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        }
        else if (tokenTag() == "("){
            // On ajoute un noeud.
            noeud.addSon(new TreeNode(tokenTag()));

            System.out.println("r61");
            advanceToken();
            expression(noeud);
            if (tokenTag() == ")"){
                // On ajoute un noeud.
                noeud.addSon(new TreeNode(tokenTag()));

                advanceToken();
                return;
            }
            else{
                printErrorAndSkip("')' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        }
        else if (tokenTag() == "VARIABLE"){
            // On ajoute un noeud.
            noeud.addSon(new TreeNode(((VarToken) getNextToken()).getValue()));

            System.out.println("r62");
            advanceToken();
            P1(noeud);
            return;
        }
        else{
            printErrorAndSkip("identificateur ou entier ou chaine ou 'true' ou 'false' ou 'null' ou '(' ou 'not' ou '-' ou 'new' ou 'character val'attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }

    public void Precur(TreeNode pere) throws ExceptionSyntaxique {
        // On crée un nouveau noeud.
        TreeNode noeud = new TreeNode("Pr");

        // On l'ajoute à l'arbre.
        pere.addSon(noeud);

        System.out.print("Precur : ");
        if (tokenTag() == "."){
            //LL(3) ici à voir si fonctionne correctement

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
                    // On ajoute un noeud.
                    noeud.addSon(new TreeNode("."));

                    // On ajoute un noeud.
                    noeud.addSon(new TreeNode("VARIABLE"));

                    System.out.println("r63");
                    Precur(noeud);
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
            printErrorAndSkip("')' ou '.' ou '=' ou '/=' ou '<' ou '<=' ou '>' ou '>=' ou '+' ou '-' ou '*' ou '/' ou 'rem' ou '+' ou '-' ou ';' ou ')' ou 'loop' ou 'then' ou '..' ou ',' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }

    }

    public void P1(TreeNode pere) throws ExceptionSyntaxique {
        // On crée un nouveau noeud.
        TreeNode noeud = new TreeNode("P1");

        // On l'ajoute à l'arbre.
        pere.addSon(noeud);

        System.out.print("P1 : ");
        //System.out.println(tokenTag());
        if (tokenTag() == "."
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
            Precur(noeud);
            return;

        }
        else if (tokenTag() == "("){
            // On ajoute un noeud.
            noeud.addSon(new TreeNode(tokenTag()));

            System.out.println("r66");
            advanceToken();
            expressionPlusVirgule(noeud);
            if (tokenTag() == ")"){
                // On ajoute un noeud.
                noeud.addSon(new TreeNode(tokenTag()));

                advanceToken();
                Precur(noeud);
                return;
            }
            else{
                printErrorAndSkip("')' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        } else{
            printErrorAndSkip("')' ou '.' ou '=' ou '/=' ou '<' ou '<=' ou '>' ou '>=' ou '+' ou '-' ou '*' ou '/' ou 'rem' ou '+' ou '-' ou ';' ou ')' ou 'loop' ou 'then' ou '..' ou ',' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }

    public void instruction(TreeNode pere) throws ExceptionSyntaxique {
        // On crée un nouveau noeud.
        TreeNode noeud = new TreeNode("instr");

        // On l'ajoute à l'arbre.
        pere.addSon(noeud);

        System.out.print("instruction : ");
        if (tokenTag() == "VARIABLE"){
            // On ajoute un noeud.
            noeud.addSon(new TreeNode(((VarToken) getNextToken()).getValue()));

            System.out.println("r67");
            advanceToken();
            instruction1(noeud);
            return;
        }
        else if (tokenTag() == "-"){
            // On ajoute un noeud.
            noeud.addSon(new TreeNode(tokenTag()));

            advanceToken();
            P(noeud);
            I1(noeud);
            T1(noeud);
            expression1(noeud);
            if (tokenTag() == "."){
                // On ajoute un noeud.
                noeud.addSon(new TreeNode(tokenTag()));

                advanceToken();
                if (tokenTag() == "VARIABLE"){
                    // On ajoute un noeud.
                    noeud.addSon(new TreeNode(((VarToken) getNextToken()).getValue()));

                    advanceToken();
                    if (tokenTag() == ":"){
                        // On ajoute un noeud.
                        noeud.addSon(new TreeNode(tokenTag()));

                        advanceToken();
                        if (tokenTag() == "="){
                            // On ajoute un noeud.
                            noeud.addSon(new TreeNode(tokenTag()));

                            advanceToken();
                            expression(noeud);
                            if (tokenTag() == ";"){
                                // On ajoute un noeud.
                                noeud.addSon(new TreeNode(tokenTag()));

                                advanceToken();
                                return;
                            } else{
                                printErrorAndSkip("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                            }
                        } else{
                            printErrorAndSkip("'=' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                        }
                    } else{
                        printErrorAndSkip("':' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                    }
                } else{
                    printErrorAndSkip("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                }
            } else{
                printErrorAndSkip("'.' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        }
        else if (tokenTag() == "not"){
            // On ajoute un noeud.
            noeud.addSon(new TreeNode(tokenTag()));

            advanceToken();
            P(noeud);
            I1(noeud);
            T1(noeud);
            expression1(noeud);
            if (tokenTag() == "."){
                // On ajoute un noeud.
                noeud.addSon(new TreeNode(tokenTag()));

                advanceToken();
                if (tokenTag() == "VARIABLE"){
                    // On ajoute un noeud.
                    noeud.addSon(new TreeNode(((VarToken) getNextToken()).getValue()));

                    advanceToken();
                    if (tokenTag() == ":"){
                        // On ajoute un noeud.
                        noeud.addSon(new TreeNode(tokenTag()));

                        advanceToken();
                        if (tokenTag() == "="){
                            // On ajoute un noeud.
                            noeud.addSon(new TreeNode(tokenTag()));

                            advanceToken();
                            expression(noeud);
                            if (tokenTag() == ";"){
                                // On ajoute un noeud.
                                noeud.addSon(new TreeNode(tokenTag()));

                                advanceToken();
                                return;
                            } else{
                                printErrorAndSkip("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                            }
                        } else{
                            printErrorAndSkip("'=' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                        }
                    } else{
                        printErrorAndSkip("':' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                    }
                } else{
                    printErrorAndSkip("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                }
            } else{
                printErrorAndSkip("'.' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        }
        else if (tokenTag() == "NUM_CONST"){
            // On ajoute un noeud.
            noeud.addSon(new TreeNode(Integer.toString(((IntegerToken) getNextToken()).getValue())));

            advanceToken();
            Precur(noeud);
            I1(noeud);
            T1(noeud);
            expression1(noeud);
            if(tokenTag() == "."){
                // On ajoute un noeud.
                noeud.addSon(new TreeNode(tokenTag()));

                advanceToken();
                if (tokenTag() == "VARIABLE"){
                    // On ajoute un noeud.
                    noeud.addSon(new TreeNode(((VarToken) getNextToken()).getValue()));

                    advanceToken();
                    if (tokenTag() == ":"){
                        // On ajoute un noeud.
                        noeud.addSon(new TreeNode(tokenTag()));

                        advanceToken();
                        if (tokenTag() == "="){
                            // On ajoute un noeud.
                            noeud.addSon(new TreeNode(tokenTag()));

                            advanceToken();
                            expression(noeud);
                            if (tokenTag() == ";"){
                                // On ajoute un noeud.
                                noeud.addSon(new TreeNode(tokenTag()));

                                advanceToken();
                                return;
                            } else{
                                printErrorAndSkip("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                            }
                        } else{
                            printErrorAndSkip("'=' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                        }
                    } else{
                        printErrorAndSkip("':' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                    }
                } else{
                    printErrorAndSkip("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                }
            } else{
                printErrorAndSkip("'.' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        }
        else if(tokenTag() == "STR_CONST"){
            // On ajoute un noeud.
            noeud.addSon(new TreeNode(tokenTag()));

            advanceToken();
            Precur(noeud);
            I1(noeud);
            T1(noeud);
            expression1(noeud);
            if(tokenTag() == "."){
                // On ajoute un noeud.
                noeud.addSon(new TreeNode(tokenTag()));

                advanceToken();
                if (tokenTag() == "VARIABLE"){
                    // On ajoute un noeud.
                    noeud.addSon(new TreeNode(((VarToken) getNextToken()).getValue()));

                    advanceToken();
                    if (tokenTag() == ":"){
                        // On ajoute un noeud.
                        noeud.addSon(new TreeNode(tokenTag()));

                        advanceToken();
                        if (tokenTag() == "="){
                            // On ajoute un noeud.
                            noeud.addSon(new TreeNode(tokenTag()));

                            advanceToken();
                            expression(noeud);
                            if (tokenTag() == ";"){
                                // On ajoute un noeud.
                                noeud.addSon(new TreeNode(tokenTag()));

                                advanceToken();
                                return;
                            } else{
                                printErrorAndSkip("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                            }
                        } else{
                            printErrorAndSkip("'=' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                        }
                    } else{
                        printErrorAndSkip("':' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                    }
                } else{
                    printErrorAndSkip("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                }
            } else{
                printErrorAndSkip("'.' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        }
        else if(tokenTag() == "true"){
            // On ajoute un noeud.
            noeud.addSon(new TreeNode(tokenTag()));

            advanceToken();
            Precur(noeud);
            I1(noeud);
            T1(noeud);
            expression1(noeud);
            if(tokenTag() == "."){
                // On ajoute un noeud.
                noeud.addSon(new TreeNode(tokenTag()));

                advanceToken();
                if (tokenTag() == "VARIABLE"){
                    // On ajoute un noeud.
                    noeud.addSon(new TreeNode(((VarToken) getNextToken()).getValue()));

                    advanceToken();
                    if (tokenTag() == ":"){
                        // On ajoute un noeud.
                        noeud.addSon(new TreeNode(tokenTag()));

                        advanceToken();
                        if (tokenTag() == "="){
                            // On ajoute un noeud.
                            noeud.addSon(new TreeNode(tokenTag()));

                            advanceToken();
                            expression(noeud);
                            if (tokenTag() == ";"){
                                // On ajoute un noeud.
                                noeud.addSon(new TreeNode(tokenTag()));

                                advanceToken();
                                return;
                            } else{
                                printErrorAndSkip("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                            }
                        } else{
                            printErrorAndSkip("'=' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                        }
                    } else{
                        printErrorAndSkip("':' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                    }
                } else{
                    printErrorAndSkip("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                }
            } else{
                printErrorAndSkip("'.' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        }
        else if(tokenTag() == "false"){
            // On ajoute un noeud.
            noeud.addSon(new TreeNode(tokenTag()));

            advanceToken();
            Precur(noeud);
            I1(noeud);
            T1(noeud);
            expression1(noeud);
            if(tokenTag() == "."){
                // On ajoute un noeud.
                noeud.addSon(new TreeNode(tokenTag()));

                advanceToken();
                if (tokenTag() == "VARIABLE"){
                    // On ajoute un noeud.
                    noeud.addSon(new TreeNode(((VarToken) getNextToken()).getValue()));

                    advanceToken();
                    if (tokenTag() == ":"){
                        // On ajoute un noeud.
                        noeud.addSon(new TreeNode(tokenTag()));

                        advanceToken();
                        if (tokenTag() == "="){
                            // On ajoute un noeud.
                            noeud.addSon(new TreeNode(tokenTag()));

                            advanceToken();
                            expression(noeud);
                            if (tokenTag() == ";"){
                                // On ajoute un noeud.
                                noeud.addSon(new TreeNode(tokenTag()));

                                advanceToken();
                                return;
                            } else{
                                printErrorAndSkip("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                            }
                        } else{
                            printErrorAndSkip("'=' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                        }
                    } else{
                        printErrorAndSkip("':' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                    }
                } else{
                    printErrorAndSkip("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                }
            } else{
                printErrorAndSkip("'.' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        }
        else if(tokenTag() == "null"){
            // On ajoute un noeud.
            noeud.addSon(new TreeNode(tokenTag()));

            advanceToken();
            Precur(noeud);
            I1(noeud);
            T1(noeud);
            expression1(noeud);
            if(tokenTag() == "."){
                // On ajoute un noeud.
                noeud.addSon(new TreeNode(tokenTag()));

                advanceToken();
                if (tokenTag() == "VARIABLE"){
                    // On ajoute un noeud.
                    noeud.addSon(new TreeNode(((VarToken) getNextToken()).getValue()));

                    advanceToken();
                    if (tokenTag() == ":"){
                        // On ajoute un noeud.
                        noeud.addSon(new TreeNode(tokenTag()));

                        advanceToken();
                        if (tokenTag() == "="){
                            // On ajoute un noeud.
                            noeud.addSon(new TreeNode(tokenTag()));

                            advanceToken();
                            expression(noeud);
                            if (tokenTag() == ";"){
                                // On ajoute un noeud.
                                noeud.addSon(new TreeNode(tokenTag()));

                                advanceToken();
                                return;
                            } else{
                                printErrorAndSkip("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                            }
                        } else{
                            printErrorAndSkip("'=' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                        }
                    } else{
                        printErrorAndSkip("':' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                    }
                } else{
                    printErrorAndSkip("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                }
            } else{
                printErrorAndSkip("'.' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        }
        else if (tokenTag() == "new"){
            // On ajoute un noeud.
            noeud.addSon(new TreeNode(tokenTag()));

            advanceToken();
            if (tokenTag() == "VARIABLE"){
                // On ajoute un noeud.
                noeud.addSon(new TreeNode(((VarToken) getNextToken()).getValue()));

                advanceToken();
                if (tokenTag() == "("){
                    // On ajoute un noeud.
                    noeud.addSon(new TreeNode(tokenTag()));
                    advanceToken();
                    if (tokenTag() == "NUM_CONST")
                    {
                        // On ajoute un noeud.
                        noeud.addSon(new TreeNode(Integer.toString(((IntegerToken) getNextToken()).getValue())));
                        advanceToken();
                        if (tokenTag() == ")"){
                            // On ajoute un noeud.
                            noeud.addSon(new TreeNode(tokenTag()));

                            advanceToken();
                            Precur(noeud);
                            I1(noeud);
                            T1(noeud);
                            expression1(noeud);
                            if(tokenTag() == "."){
                                // On ajoute un noeud.
                                noeud.addSon(new TreeNode(tokenTag()));

                                advanceToken();
                                if (tokenTag() == "VARIABLE"){
                                    // On ajoute un noeud.
                                    noeud.addSon(new TreeNode(((VarToken) getNextToken()).getValue()));

                                    advanceToken();
                                    if (tokenTag() == ":"){
                                        // On ajoute un noeud.
                                        noeud.addSon(new TreeNode(tokenTag()));

                                        advanceToken();
                                        if (tokenTag() == "="){
                                            // On ajoute un noeud.
                                            noeud.addSon(new TreeNode(tokenTag()));

                                            advanceToken();
                                            expression(noeud);
                                            if (tokenTag() == ";"){
                                                // On ajoute un noeud.
                                                noeud.addSon(new TreeNode(tokenTag()));

                                                advanceToken();
                                                return;
                                            } else{
                                                printErrorAndSkip("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                                            }
                                        } else{
                                            printErrorAndSkip("'=' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                                        }
                                    } else{
                                        printErrorAndSkip("':' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                                    }
                                } else{
                                    printErrorAndSkip("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                                }
                            } else{
                                printErrorAndSkip("'.' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                            }
                        } else{
                            printErrorAndSkip("')' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                        }
                    } else {
                        printErrorAndSkip("entier attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                    }
                } else{
                    printErrorAndSkip("'(' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                }
            } else{
                printErrorAndSkip("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        }
        else if (tokenTag() == "character'val"){
            // On ajoute un noeud.
            noeud.addSon(new TreeNode(tokenTag()));
            advanceToken();
            if (tokenTag() == "("){
                // On ajoute un noeud.
                noeud.addSon(new TreeNode(tokenTag()));
                advanceToken();
                expression(noeud);
                if (tokenTag() == ")"){
                    // On ajoute un noeud.
                    noeud.addSon(new TreeNode(tokenTag()));

                    advanceToken();
                    Precur(noeud);
                    I1(noeud);
                    T1(noeud);
                    expression1(noeud);
                    if(tokenTag() == "."){
                        // On ajoute un noeud.
                        noeud.addSon(new TreeNode(tokenTag()));
                        advanceToken();
                        if (tokenTag() == "VARIABLE"){
                            // On ajoute un noeud.
                            noeud.addSon(new TreeNode(((VarToken) getNextToken()).getValue()));

                            advanceToken();
                            if (tokenTag() == ":"){
                                // On ajoute un noeud.
                                noeud.addSon(new TreeNode(tokenTag()));

                                advanceToken();
                                if (tokenTag() == "="){
                                    // On ajoute un noeud.
                                    noeud.addSon(new TreeNode(tokenTag()));

                                    advanceToken();
                                    expression(noeud);
                                    if (tokenTag() == ";"){
                                        // On ajoute un noeud.
                                        noeud.addSon(new TreeNode(tokenTag()));

                                        advanceToken();
                                        return;
                                    } else{
                                        printErrorAndSkip("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                                    }
                                } else{
                                    printErrorAndSkip("'=' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                                }
                            } else{
                                printErrorAndSkip("':' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                            }
                        } else{
                            printErrorAndSkip("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                        }
                    } else{
                        printErrorAndSkip("'.' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                    }
                } else{
                    printErrorAndSkip("')' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                }
            } else{
                printErrorAndSkip("'(' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }     
        }
        else if (tokenTag() == "("){
            // On ajoute un noeud.
            noeud.addSon(new TreeNode(tokenTag()));

            advanceToken();
            expression(noeud);
            if (tokenTag() == ")"){
                // On ajoute un noeud.
                noeud.addSon(new TreeNode(tokenTag()));

                advanceToken();
                Precur(noeud);
                I1(noeud);
                T1(noeud);
                expression1(noeud);
                if(tokenTag() == "."){
                    // On ajoute un noeud.
                    noeud.addSon(new TreeNode(tokenTag()));

                    advanceToken();
                    if (tokenTag() == "VARIABLE"){
                        // On ajoute un noeud.
                        noeud.addSon(new TreeNode(((VarToken) getNextToken()).getValue()));

                        advanceToken();
                        if (tokenTag() == ":"){
                            // On ajoute un noeud.
                            noeud.addSon(new TreeNode(tokenTag()));

                            advanceToken();
                            if (tokenTag() == "="){
                                // On ajoute un noeud.
                                noeud.addSon(new TreeNode(tokenTag()));

                                advanceToken();
                                expression(noeud);
                                if (tokenTag() == ";"){
                                    // On ajoute un noeud.
                                    noeud.addSon(new TreeNode(tokenTag()));

                                    advanceToken();
                                    return;
                                } else{
                                    printErrorAndSkip("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                                }
                            } else{
                                printErrorAndSkip("'=' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                            }
                        } else{
                            printErrorAndSkip("':' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                        }
                    } else{
                        printErrorAndSkip("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                    }
                } else{
                    printErrorAndSkip("'.' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                }
            } else{
                printErrorAndSkip("')' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        }
        else if(tokenTag() == "return"){
            // On ajoute un noeud.
            noeud.addSon(new TreeNode(tokenTag()));

            System.out.println("r78");
            advanceToken();
            instruction2(noeud);
            return;
        }
        else if(tokenTag() == "begin"){
            // On ajoute un noeud.
            noeud.addSon(new TreeNode(tokenTag()));

            System.out.println("r79");
            advanceToken();
            instructionPlus(noeud);
            if(tokenTag() == "end"){
                // On ajoute un noeud.
                noeud.addSon(new TreeNode(tokenTag()));

                advanceToken();
                if(tokenTag() == ";"){
                    // On ajoute un noeud.
                    noeud.addSon(new TreeNode(tokenTag()));

                    advanceToken();
                    return;
                }
                else{
                    printErrorAndSkip("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                }
            }
            else{
                printErrorAndSkip("'end' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        }
        else if(tokenTag() == "if"){
            // On ajoute un noeud.
            noeud.addSon(new TreeNode(tokenTag()));

            System.out.println("r80");
            advanceToken();
            expression(noeud);
            if(tokenTag()=="then"){
                // On ajoute un noeud.
                noeud.addSon(new TreeNode(tokenTag()));

                advanceToken();
                instructionPlus(noeud);
                instruction3(noeud);
                return;
            }
            else{
                printErrorAndSkip("'then' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        }
        else if(tokenTag() == "for"){
            // On ajoute un noeud.
            noeud.addSon(new TreeNode(tokenTag()));

            System.out.println("r81");
            advanceToken();
            if (tokenTag() == "VARIABLE") {
                // On ajoute un noeud.
                noeud.addSon(new TreeNode(((VarToken) getNextToken()).getValue()));

                advanceToken();
            } else {
                printErrorAndSkip("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
            if (tokenTag() == "in") {
                // On ajoute un noeud.
                noeud.addSon(new TreeNode(tokenTag()));

                advanceToken();
            } else {
                printErrorAndSkip("'in' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
            instruction4(noeud);
        }
        else if(tokenTag() == "while"){
            // On ajoute un noeud.
            noeud.addSon(new TreeNode(tokenTag()));

            System.out.println("r82");
            advanceToken();
            expression(noeud);
            if(tokenTag() == "loop"){
                // On ajoute un noeud.
                noeud.addSon(new TreeNode(tokenTag()));

                advanceToken();
                instructionPlus(noeud);
                if(tokenTag() == "end"){
                    // On ajoute un noeud.
                    noeud.addSon(new TreeNode(tokenTag()));

                    advanceToken();
                    if(tokenTag() == "loop"){
                        // On ajoute un noeud.
                        noeud.addSon(new TreeNode(tokenTag()));

                        advanceToken();
                        if (tokenTag() == ";") {
                            // On ajoute un noeud.
                            noeud.addSon(new TreeNode(tokenTag()));

                            advanceToken();
                            return;
                        } else {
                            printErrorAndSkip("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                        }
                    }
                    else{
                        printErrorAndSkip("'loop' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                    }
                }
                else{
                    printErrorAndSkip("'end' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                }
            }
            else{
                printErrorAndSkip("'loop' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        }
        else if (tokenTag() == "put") {
            // On ajoute un noeud.
            noeud.addSon(new TreeNode(tokenTag()));

            System.out.println("rPUT");
            advanceToken();
            if (tokenTag() == "(") {
                // On ajoute un noeud.
                noeud.addSon(new TreeNode(tokenTag()));

                advanceToken();
                expression(noeud);
                if (tokenTag() == ")") {
                    // On ajoute un noeud.
                    noeud.addSon(new TreeNode(tokenTag()));

                    advanceToken();
                    if (tokenTag() == ";") {
                        // On ajoute un noeud.
                        noeud.addSon(new TreeNode(tokenTag()));

                        advanceToken();
                        return;
                    } else {
                        printErrorAndSkip("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                    }
                } else {
                    printErrorAndSkip("')' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                }
            } else {
                printErrorAndSkip("'(' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        }
        else{
            printErrorAndSkip("identificateur ou entier ou chaine ou 'true' ou 'false' ou 'null' ou '(' ou 'not' ou '-' ou 'new' ou 'character'val' ou 'return' ou 'begin' ou 'if' ou 'for' ou 'while' ou 'put' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }

    public void instruction1(TreeNode pere) throws ExceptionSyntaxique {
        // On crée un nouveau noeud.
        TreeNode noeud = new TreeNode("instr1");

        // On l'ajoute à l'arbre.
        pere.addSon(noeud);

        System.out.print("instruction1 : ");
        if (tokenTag() == ":"){
            // On ajoute un noeud.
            noeud.addSon(new TreeNode(tokenTag()));

            System.out.println("r83");
            advanceToken();
            if (tokenTag() == "="){
                // On ajoute un noeud.
                noeud.addSon(new TreeNode(tokenTag()));

                advanceToken();
                expression(noeud);
                if (tokenTag() == ";"){
                    // On ajoute un noeud.
                    noeud.addSon(new TreeNode(tokenTag()));

                    advanceToken();
                    return;
                } else{
                    printErrorAndSkip("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                }
            } else{
                printErrorAndSkip("'=' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        }
        else if (tokenTag() == ";"){
            // On ajoute un noeud.
            noeud.addSon(new TreeNode(tokenTag()));

            System.out.println("r84");
            advanceToken();
            return;
        }
        else if (tokenTag() == "("){
            // On ajoute un noeud.
            noeud.addSon(new TreeNode(tokenTag()));

            System.out.println("r85");
            advanceToken();
            expressionPlusVirgule(noeud);
            if (tokenTag() == ")"){
                // On ajoute un noeud.
                noeud.addSon(new TreeNode(tokenTag()));

                advanceToken();
                instruction11(noeud);
                return;
            } else{
                printErrorAndSkip("')' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        }
        else if (tokenTag() == "."){
            // On ajoute un noeud.
            noeud.addSon(new TreeNode(tokenTag()));

            System.out.println("r86");
            advanceToken();
            if (tokenTag() == "VARIABLE"){
                // On ajoute un noeud.
                noeud.addSon(new TreeNode(((VarToken) getNextToken()).getValue()));

                advanceToken();
                I1(noeud);
                T1(noeud);
                expression1(noeud);
                if (tokenTag() == "."){
                    // On ajoute un noeud.
                    noeud.addSon(new TreeNode(tokenTag()));

                    advanceToken();
                    if (tokenTag() == "VARIABLE"){
                        // On ajoute un noeud.
                        noeud.addSon(new TreeNode(((VarToken) getNextToken()).getValue()));

                        advanceToken();
                        if (tokenTag() == ":"){
                            // On ajoute un noeud.
                            noeud.addSon(new TreeNode(tokenTag()));

                            advanceToken();
                            if (tokenTag() == "="){
                                // On ajoute un noeud.
                                noeud.addSon(new TreeNode(tokenTag()));

                                advanceToken();
                                expression(noeud);
                                if (tokenTag() == ";"){
                                    // On ajoute un noeud.
                                    noeud.addSon(new TreeNode(tokenTag()));

                                    advanceToken();
                                    return;
                                } else{
                                    printErrorAndSkip("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                                }
                            } else{
                                printErrorAndSkip("'=' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                            }
                        } else{
                            printErrorAndSkip("':' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                        }
                    } else{
                        printErrorAndSkip("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                    }
                } else{
                    printErrorAndSkip("'.' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                }
            } else{
                printErrorAndSkip("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        }
        else if (tokenTag() == "="
                || tokenTag() == "/="
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
            I1(noeud);
            T1(noeud);
            expression1(noeud);
            if (tokenTag() == "."){
                // On ajoute un noeud.
                noeud.addSon(new TreeNode(tokenTag()));

                advanceToken();
                if (tokenTag() == "VARIABLE"){
                    // On ajoute un noeud.
                    noeud.addSon(new TreeNode(((VarToken) getNextToken()).getValue()));

                    advanceToken();
                    if (tokenTag() == ":"){
                        // On ajoute un noeud.
                        noeud.addSon(new TreeNode(tokenTag()));

                        advanceToken();
                        if (tokenTag() == "="){
                            // On ajoute un noeud.
                            noeud.addSon(new TreeNode(tokenTag()));

                            advanceToken();
                            expression(noeud);
                            if (tokenTag() == ";"){
                                // On ajoute un noeud.
                                noeud.addSon(new TreeNode(tokenTag()));

                                advanceToken();
                                return;
                            } else{
                                printErrorAndSkip("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                            }
                        } else{
                            printErrorAndSkip("'=' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                        }
                    } else{
                        printErrorAndSkip("':' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                    }
                } else{
                    printErrorAndSkip("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                }
            } else{
                printErrorAndSkip("'.' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        } else {
            printErrorAndSkip("';' ou '.' ou '=' ou '/=' ou '<' ou '<=' ou '>' ou '>=' ou '+' ou '-' ou '*' ou '/' ou 'rem' ou '+' ou '-' ou ';' ou ')' ou 'loop' ou 'then' ou '..' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }

    public void instruction11(TreeNode pere) throws ExceptionSyntaxique {
        // On crée un nouveau noeud.
        TreeNode noeud = new TreeNode("instr11");

        // On l'ajoute à l'arbre.
        pere.addSon(noeud);

        System.out.print("instruction11 : ");
        if (tokenTag() == ";"){
            // On ajoute un noeud.
            noeud.addSon(new TreeNode(tokenTag()));

            System.out.println("r88");
            advanceToken();
            return;
        }
        else if (tokenTag() == "."
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
            Precur(noeud);
            I1(noeud);
            T1(noeud);
            expression1(noeud);
            if (tokenTag() == "."){
                // On ajoute un noeud.
                noeud.addSon(new TreeNode(tokenTag()));

                advanceToken();
                if (tokenTag() == "VARIABLE"){
                    // On ajoute un noeud.
                    noeud.addSon(new TreeNode(((VarToken) getNextToken()).getValue()));

                    advanceToken();
                    if (tokenTag() == ":"){
                        // On ajoute un noeud.
                        noeud.addSon(new TreeNode(tokenTag()));

                        advanceToken();
                        if (tokenTag() == "="){
                            // On ajoute un noeud.
                            noeud.addSon(new TreeNode(tokenTag()));

                            advanceToken();
                            expression(noeud);
                            if (tokenTag() == ";"){
                                // On ajoute un noeud.
                                noeud.addSon(new TreeNode(tokenTag()));

                                advanceToken();
                                return;
                            } else{
                                printErrorAndSkip("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                            }
                        } else{
                            printErrorAndSkip("'=' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                        }
                    } else{
                        printErrorAndSkip("':' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                    }
                } else{
                    printErrorAndSkip("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                }
            } else{
                printErrorAndSkip("'.' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        } else {
            printErrorAndSkip("';' ou '.' ou '=' ou '/=' ou '<' ou '<=' ou '>' ou '>=' ou '+' ou '-' ou '*' ou '/' ou 'rem' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }

    public void instruction2(TreeNode pere) throws ExceptionSyntaxique {

        // On crée un nouveau noeud.
        TreeNode noeud = new TreeNode("instr2");

        // On l'ajoute à l'arbre.
        pere.addSon(noeud);

        System.out.print("instruction2 : ");
        if (tokenTag() == ";"){
            // On ajoute un noeud.
            noeud.addSon(new TreeNode(tokenTag()));

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
                || tokenTag() == "character'val"
                || tokenTag() == "VARIABLE" ){
            System.out.println("r90");
            expression(noeud);
            if (tokenTag() == ";") {
                // On ajoute un noeud.
                noeud.addSon(new TreeNode(tokenTag()));

                advanceToken();
            } else {
                printErrorAndSkip("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        }
        else{
            printErrorAndSkip("identificateur ou entier ou chaine ou 'true' ou 'false' ou 'null' ou '(' ou 'not' ou '-' ou 'new' ou 'character val'ou ';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }

    public void instruction3(TreeNode pere) throws ExceptionSyntaxique {
        // On crée un nouveau noeud.
        TreeNode noeud = new TreeNode("instr3");

        // On l'ajoute à l'arbre.
        pere.addSon(noeud);

        System.out.print("instruction3 : ");
        if (tokenTag() == "end"){
            // On ajoute un noeud.
            noeud.addSon(new TreeNode(tokenTag()));

            System.out.println("r92");
            advanceToken();
            if (tokenTag() == "if") {
                // On ajoute un noeud.
                noeud.addSon(new TreeNode(tokenTag()));

                advanceToken();
            } else {
                printErrorAndSkip("'if' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
            if (tokenTag() == ";") {
                // On ajoute un noeud.
                noeud.addSon(new TreeNode(tokenTag()));

                advanceToken();
                return;
            } else {
                printErrorAndSkip("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        }
        else if (tokenTag() == "else"){
            // On ajoute un noeud.
            noeud.addSon(new TreeNode(tokenTag()));

            System.out.println("r93");
            advanceToken();
            instructionPlus(noeud);
            if (tokenTag() == "end"){
                // On ajoute un noeud.
                noeud.addSon(new TreeNode(tokenTag()));

                advanceToken();
                if (tokenTag() == "if") {
                    // On ajoute un noeud.
                    noeud.addSon(new TreeNode(tokenTag()));

                    advanceToken();
                } else {
                    printErrorAndSkip("'if' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                }
                if (tokenTag() == ";") {
                    // On ajoute un noeud.
                    noeud.addSon(new TreeNode(tokenTag()));

                    advanceToken();
                    return;
                } else {
                    printErrorAndSkip("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                }
            }
            else{
                printErrorAndSkip("'end' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        } else if (tokenTag() == "elsif"){
            System.out.println("r94");
            elsifPlus(noeud);
            instruction3(noeud);
        } else{
            printErrorAndSkip("'else' ou 'end' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }

    public void instruction4(TreeNode pere) throws ExceptionSyntaxique {
        // On crée un nouveau noeud.
        TreeNode noeud = new TreeNode("instr4");

        // On l'ajoute à l'arbre.
        pere.addSon(noeud);

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
                || tokenTag() == "character'val"
                || tokenTag() == "VARIABLE" ){
            System.out.println("r95");
            expression(noeud);
            if (tokenTag() == "DOTDOT") {
                // On ajoute un noeud.
                noeud.addSon(new TreeNode(tokenTag()));

                advanceToken();
            } else {
                printErrorAndSkip("'..' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
            expression(noeud);
            if (tokenTag() == "loop") {
                // On ajoute un noeud.
                noeud.addSon(new TreeNode(tokenTag()));

                advanceToken();
            } else {
                printErrorAndSkip("'loop' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
            instructionPlus(noeud);
            if (tokenTag() == "end") {
                // On ajoute un noeud.
                noeud.addSon(new TreeNode(tokenTag()));

                advanceToken();
            } else {
                printErrorAndSkip("'end' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
            if (tokenTag() == "loop") {
                // On ajoute un noeud.
                noeud.addSon(new TreeNode(tokenTag()));

                advanceToken();
            } else {
                printErrorAndSkip("'loop' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
            if (tokenTag() == ";") {
                // On ajoute un noeud.
                noeud.addSon(new TreeNode(tokenTag()));

                advanceToken();
                return;
            } else {
                printErrorAndSkip("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        } else if(tokenTag() == "reverse")
        {
            // On ajoute un noeud.
            noeud.addSon(new TreeNode(tokenTag()));

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
                    || tokenTag() == "character'val"
                    || tokenTag() == "VARIABLE" ){
                expression(noeud);
                if (tokenTag() == "DOTDOT") {
                    // On ajoute un noeud.
                    noeud.addSon(new TreeNode(tokenTag()));

                    advanceToken();
                } else {
                    printErrorAndSkip("'..' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                }
                expression(noeud);
                if (tokenTag() == "loop") {
                    // On ajoute un noeud.
                    noeud.addSon(new TreeNode(tokenTag()));

                    advanceToken();
                } else {
                    printErrorAndSkip("'loop' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                }
                instructionPlus(noeud);
                if (tokenTag() == "end") {
                    // On ajoute un noeud.
                    noeud.addSon(new TreeNode(tokenTag()));

                    advanceToken();
                } else {
                    printErrorAndSkip("'end' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                }
                if (tokenTag() == "loop") {
                    // On ajoute un noeud.
                    noeud.addSon(new TreeNode(tokenTag()));

                    advanceToken();
                } else {
                    printErrorAndSkip("'loop' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                }
                if (tokenTag() == ";") {
                    // On ajoute un noeud.
                    noeud.addSon(new TreeNode(tokenTag()));

                    advanceToken();
                    return;
                } else {
                    printErrorAndSkip("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                }
            }
            else{
                printErrorAndSkip("identificateur ou entier ou chaine ou 'true' ou 'false' ou 'null' ou '(' ou 'not' ou '-' ou 'new' ou 'character val'attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        } else{
            printErrorAndSkip("identificateur ou entier ou chaine ou 'true' ou 'false' ou 'null' ou '(' ou 'not' ou '-' ou 'new' ou 'character val'attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }

    public void instructionPlus(TreeNode pere) throws ExceptionSyntaxique {
        // On crée un nouveau noeud.
        TreeNode noeud = new TreeNode("instr+");

        // On l'ajoute à l'arbre.
        pere.addSon(noeud);
        //Lignes de debug
        System.out.println("token actuel "+ this.tokens.get(currentTokenIndex).getTag());
        System.out.println("token suivant "+ this.tokens.get(currentTokenIndex+1).getTag());
        System.out.println("token précédent "+ this.tokens.get(currentTokenIndex-1).getTag());
        System.out.println("token index "+ currentTokenIndex);
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
            || tokenTag() == "character'val"
            || tokenTag() == "return"
            || tokenTag() == "begin"
            || tokenTag() == "if"
            || tokenTag() == "for"
            || tokenTag() == "while"
            || tokenTag() == "put") {
            System.out.println("r97");
            instruction(noeud);
            instructionPlus1(noeud);
        } else {
            printErrorAndSkip("identificateur ou entier ou chaine ou 'true' ou 'false' ou 'null' ou '(' ou 'not' ou '-' ou 'new' ou 'character val'ou 'begin' ou 'if' ou 'for' ou 'while' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }

    public void instructionPlus1(TreeNode pere) throws ExceptionSyntaxique {
        // On crée un nouveau noeud.
        TreeNode noeud = new TreeNode("instr+1");

        // On l'ajoute à l'arbre.
        pere.addSon(noeud);

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
                || tokenTag() == "character'val"
                || tokenTag() == "return"
                || tokenTag() == "begin"
                || tokenTag() == "if"
                || tokenTag() == "for"
                || tokenTag() == "while"
                || tokenTag() == "put") {
            System.out.println("r98");
            instructionPlus(noeud);
        } else {
            System.out.println("r99");
            return;
        }
    }

    public void declarationPlus(TreeNode pere) throws ExceptionSyntaxique {
        // On crée un nouveau noeud.
        TreeNode noeud = new TreeNode("decl+");

        // On l'ajoute à l'arbre.
        pere.addSon(noeud);

        System.out.print("declarationPlus : ");
        if (tokenTag() == "type" || tokenTag() == "procedure" || tokenTag() == "function" || tokenTag() == "VARIABLE") {
            System.out.println("r100");
            declaration(noeud);
            declarationPlus1(noeud);
        } else {
            printErrorAndSkip("'type', 'procedure', 'function' ou identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }

    public void declarationPlus1(TreeNode pere) throws ExceptionSyntaxique {
        // On crée un nouveau noeud.
        TreeNode noeud = new TreeNode("decl+1");

        // On l'ajoute à l'arbre.
        pere.addSon(noeud);

        System.out.print("declarationPlus1 : ");
        if (tokenTag() == "type" || tokenTag() == "procedure" || tokenTag() == "function" || tokenTag() == "VARIABLE") {
            System.out.println("r101");
            declarationPlus(noeud);
        } else {
            System.out.println("r102");
            return;
        }
    }


    public void champsPlus(TreeNode pere) throws ExceptionSyntaxique {
        // On crée un nouveau noeud.
        TreeNode noeud = new TreeNode("champs+");

        // On l'ajoute à l'arbre.
        pere.addSon(noeud);

        System.out.print("champsPlus : ");
        if (tokenTag() == "VARIABLE"){
            System.out.println("r103");
            champs(noeud);
            champsPlus1(noeud);
        }
        else{
            printErrorAndSkip("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }

    public void champsPlus1(TreeNode pere) throws ExceptionSyntaxique {
        // On crée un nouveau noeud.
        TreeNode noeud = new TreeNode("champs+1");

        // On l'ajoute à l'arbre.
        pere.addSon(noeud);

        System.out.print("champsPlus1 : ");
        if (tokenTag() == "VARIABLE"){
            System.out.println("r104");
            champsPlus(noeud);
        }
        else{
            System.out.println("r105");
            return;
        }
    }



    public void identificateurVirgulePlus(TreeNode pere) throws ExceptionSyntaxique {
        // On crée un nouveau noeud.
        TreeNode noeud = new TreeNode("id,+");

        // On l'ajoute à l'arbre.
        pere.addSon(noeud);

        System.out.print("identificateurVirgulePlus : ");
        if (tokenTag() == "VARIABLE") {
            // On ajoute un noeud.
            noeud.addSon(new TreeNode(((VarToken) getNextToken()).getValue()));

            System.out.println("r106");
            advanceToken();
            identificateurVirgulePlus1(noeud);
            return;
        } else {
            printErrorAndSkip("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }

    public void identificateurVirgulePlus1(TreeNode pere) throws ExceptionSyntaxique {
        // On crée un nouveau noeud.
        TreeNode noeud = new TreeNode("id,+1");

        // On l'ajoute à l'arbre.
        pere.addSon(noeud);

        System.out.print("identificateurVirgulePlus1 : ");
        if (tokenTag() == ",") {
            // On ajoute un noeud.
            noeud.addSon(new TreeNode(tokenTag()));

            System.out.println("r107");
            advanceToken();
            if (tokenTag() == "VARIABLE") {
                identificateurVirgulePlus(noeud);
            } else {
                printErrorAndSkip("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        } else{
            System.out.println("r108");
            return;
        }
    }

    public void paramPointVirgulePlus(TreeNode pere) throws ExceptionSyntaxique {
        // On crée un nouveau noeud.
        TreeNode noeud = new TreeNode("param;+");

        // On l'ajoute à l'arbre.
        pere.addSon(noeud);

        System.out.print("paramPointVirgulePlus : ");
        if (tokenTag() == "VARIABLE") {
            System.out.println("r109");
            param(noeud);
            paramPointVirgulePlus1(noeud);
        } else {
            printErrorAndSkip("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }

    public void paramPointVirgulePlus1(TreeNode pere) throws ExceptionSyntaxique {
        // On crée un nouveau noeud.
        TreeNode noeud = new TreeNode("param;+1");

        // On l'ajoute à l'arbre.
        pere.addSon(noeud);

        System.out.print("paramPointVirgulePlus1 : ");
        if (tokenTag() == ";") {
            // On ajoute un noeud.
            noeud.addSon(new TreeNode(tokenTag()));

            System.out.println("r110");
            advanceToken();
            paramPointVirgulePlus(noeud);
        } else {
            System.out.println("r111");
            return;
        }
    }

    public void expressionPlusVirgule(TreeNode pere) throws ExceptionSyntaxique {
        // On crée un nouveau noeud.
        TreeNode noeud = new TreeNode("expr+,");

        // On l'ajoute à l'arbre.
        pere.addSon(noeud);

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
                || tokenTag() == "character'val"
                || tokenTag() == "VARIABLE" ){
            System.out.println("r112");
            expression(noeud);
            expressionPlusVirgule1(noeud);
        }
        else{
            printErrorAndSkip("identificateur ou entier ou chaine ou 'true' ou 'false' ou 'null' ou '(' ou 'not' ou '-' ou 'new' ou 'character val'attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }

    public void expressionPlusVirgule1(TreeNode pere) throws ExceptionSyntaxique {
        // On crée un nouveau noeud.
        TreeNode noeud = new TreeNode("expr+,1");

        // On l'ajoute à l'arbre.
        pere.addSon(noeud);

        System.out.print("expressionPlusVirgule1 : ");
        if (tokenTag() == ","){
            // On ajoute un noeud.
            noeud.addSon(new TreeNode(tokenTag()));

            System.out.println("r113");
            advanceToken();
            expressionPlusVirgule(noeud);
        }
        else{
            System.out.println("r114");
            return;
        }
    }


    public void elsifPlus(TreeNode pere) throws ExceptionSyntaxique {
        // On crée un nouveau noeud.
        TreeNode noeud = new TreeNode("elsif+");

        // On l'ajoute à l'arbre.
        pere.addSon(noeud);

        System.out.print("elsifPlus : ");
        if (tokenTag() == "elsif"){
            // On ajoute un noeud.
            noeud.addSon(new TreeNode(tokenTag()));

            System.out.println("r115");
            advanceToken();
            expression(noeud);
            if (tokenTag() == "then"){
                // On ajoute un noeud.
                noeud.addSon(new TreeNode(tokenTag()));

                advanceToken();
                instructionPlus(noeud);
                elsifPlus1(noeud);
            }
            else{
                printErrorAndSkip("'then' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        }
        else{
            printErrorAndSkip("'elsif' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }

    public void elsifPlus1(TreeNode pere) throws ExceptionSyntaxique {
        // On crée un nouveau noeud.
        TreeNode noeud = new TreeNode("elsif+1");

        // On l'ajoute à l'arbre.
        pere.addSon(noeud);

        System.out.print("elsifPlus1 : ");
        if (tokenTag() == "elsif"){
            System.out.println("r116");
            elsifPlus(noeud);
        }
        else{
            System.out.println("r117");
            return;
        }
    }
}