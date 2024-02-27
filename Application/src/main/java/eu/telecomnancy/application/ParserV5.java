package eu.telecomnancy.application;

import java.util.ArrayList;
import java.util.List;

import eu.telecomnancy.application.token.NumToken;
import eu.telecomnancy.application.token.StringToken;
import eu.telecomnancy.application.token.Token;
import eu.telecomnancy.application.token.VarToken;

import eu.telecomnancy.application.tds.TDS;


public class ParserV5
{
    private ArrayList<Token> tokens;
    private int currentTokenIndex = 0;

    private List<String> rulesName;
    private TDS tableDesSymboles;

    public ParserV5(ArrayList<Token> tokens)
    {
        System.out.println("Initialisation du parser");
        System.out.println("Nombre de tokens: " + tokens.size());
        this.tokens = tokens;
        this.currentTokenIndex = 0;

        this.rulesName = new ArrayList<>();
        this.tableDesSymboles = new TDS();
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

    //_______________Méthodes de gestion des erreurs_______________//

    public String tokenTag() throws ExceptionSyntaxique //Fonction permettant de récupérer le tag du token suivanat
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

    //_______________Méthodes de remplissage de la table des symboles_______________//
    public void addVariableToSymbolTable(TreeNode pere) {
        System.out.println(" ");
        for (String nom: this.rulesName) {
            System.out.print(nom + ", ");
        }
        System.out.println(" ");
        System.out.println("addVariableToSymbolTable" + pere.getLabel());
        //this.tableDesSymboles.addVariable(identif, taille, isParam);
    }

    public void addFunctionToSymbolTable(String identif, int nbArgs, TDS table) {
        this.tableDesSymboles.addFunctionOrProcedure(identif, nbArgs, table);
    }


    //_______________Méthodes de l'analyseur syntaxique_______________//

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

            advanceToken();
        } else {
            printErrorAndSkip("'with' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }

        //On vérifie que le token suivant est bien une librairie
        if (tokenTag() == "LIB") {
            // On ajoute un noeud.
            root.addSon(new TreeNode("DECL_LIB"));

            advanceToken();
        } else {
            printErrorAndSkip("'Librairie' attendue");
        }

        //On vérifie que le token suivant est bien un ";"
        if (tokenTag() == ";") {

            advanceToken();
        } else {
            printErrorAndSkip("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }

        //On vérifie que le token suivant est bien un "use"
        if (tokenTag() == "use") {

            advanceToken();
        } else {
            printErrorAndSkip("'use' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }

        //On vérifie que le token suivant est bien une librairie
        if (tokenTag() == "LIB") {

            advanceToken();
        } else {
            printErrorAndSkip("'Librairie' attendue");
        }

        //On vérifie que le token suivant est bien un ";"
        if (tokenTag() == ";") {

            advanceToken();
        } else {
            printErrorAndSkip("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }

        //On vérifie que le token suivant est bien un début de procedure
        if (tokenTag() == "procedure") {

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

            advanceToken();
            fichier2(root);
        } else {
            printErrorAndSkip("'is' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }

    public void fichier2(TreeNode pere) throws ExceptionSyntaxique {


        System.out.print("fichier2 : ");
        this.rulesName.add("fichier2");
        // Si le token suivant est un "begin", on est bien dans la première réduction de la règle fichier2 et on passe dans la règle instructionPlus
        if (tokenTag() == "begin") {

            System.out.println("r2");
            advanceToken();
            instructionPlus(pere);
            if (tokenTag() == "end") {

                advanceToken();
                fichier3(pere);
                return;
            } else {
                printErrorAndSkip("'end' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        }
        // Sinon, on est dans la deuxième réduction de la règle fichier2, le prochain token doit être un "type" ou un "procedure" ou un "function"
        // On passe donc dans la règle declarationPlus sans avancer la lecture des tokens
        else if (tokenTag() == "type" || tokenTag() == "procedure" || tokenTag() == "function" || tokenTag() == "VARIABLE") {
            System.out.println("r3");
            TreeNode noeud = new TreeNode("DECL");
            declarationPlus(noeud);
            pere.addSon(noeud);
            if (tokenTag() == "begin") {

                advanceToken();
                instructionPlus(pere);
                if (tokenTag() == "end") {

                    advanceToken();
                    fichier3(pere);
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

        System.out.print("fichier3 : ");
        this.rulesName.add("fichier3");
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
                printErrorAndSkip("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        } else {
            printErrorAndSkip("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }


    public void declaration(TreeNode pere) throws ExceptionSyntaxique {

        System.out.print("declaration : ");
        this.rulesName.add("declaration");
        if (tokenTag() == "type") {
            // On ajoute un noeud.
            TreeNode noeud = new TreeNode(tokenTag());
            pere.addSon(noeud);

            System.out.println("r6");
            advanceToken();
            if (tokenTag() == "VARIABLE") {
                // On ajoute un noeud.
                noeud.addSon(new TreeNode(((VarToken) getNextToken()).getValue()));

                advanceToken();
                declaration11(noeud);
                return;
            } else {
                printErrorAndSkip("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        } else if (tokenTag() == "procedure") {
            // On ajoute un noeud.
            TreeNode noeud = new TreeNode(tokenTag());
            pere.addSon(noeud);

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
            TreeNode noeud = new TreeNode(tokenTag());
            pere.addSon(noeud);

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

            TreeNode noeud = new TreeNode("");
            TreeNode noeud2 = new TreeNode("");

            System.out.println("r9");
            identificateurVirgulePlus(noeud2);
            if (tokenTag() == ":") {
                advanceToken();
                if (tokenTag() == "VARIABLE"){
                    noeud.addAllSons(noeud2.getSons());
                    noeud.setLabel(((VarToken) getNextToken()).getValue());
                    pere.addSon(noeud);
                    advanceToken();
                } else{
                    printErrorAndSkip("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                }

                declaration41(noeud);
            } else {
                printErrorAndSkip("':' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        } else {
            printErrorAndSkip("'type', 'procedure', 'function' ou identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }

    public void declaration11(TreeNode pere) throws ExceptionSyntaxique {

        System.out.print("declaration11 : ");
        this.rulesName.add("declaration11");
        if (tokenTag() == ";") {

            System.out.println("r10");
            advanceToken();
            return;
        } else if (tokenTag() == "is") {

            System.out.println("r11");
            advanceToken();
            if (tokenTag() == "record"){
                advanceToken();
                champsPlus(pere);
                if (tokenTag() == "end"){

                    advanceToken();
                }else{
                    printErrorAndSkip("'end' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                }
                if (tokenTag() == "record"){

                    advanceToken();
                } else{
                    printErrorAndSkip("'record' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                }
                if (tokenTag() == ";"){

                    advanceToken();
                    return;
                } else{
                    printErrorAndSkip("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                }
            } else {
                printErrorAndSkip("'record' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        } else {
            printErrorAndSkip("'is' ou ';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }

    public void declaration21(TreeNode pere) throws ExceptionSyntaxique {

        System.out.print("declaration21 : ");
        this.rulesName.add("declaration21");
        if (tokenTag() == "is"){

            System.out.println("r12");
            advanceToken();
            declaration22(pere);
            return;
        }
        else if(tokenTag() == "("){

            System.out.println("r13");
            params(pere);
            if (tokenTag() == "is"){

                advanceToken();
                declaration22(pere);
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

        System.out.print("declaration22 : ");
        this.rulesName.add("declaration22");
        if (tokenTag() == "begin") {

            System.out.println("r14");
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
                    || tokenTag() == "character'val"
                    || tokenTag() == "begin"
                    || tokenTag() == "if"
                    || tokenTag() == "for"
                    || tokenTag() == "while"
                    || tokenTag() == "put") {
                instructionPlus(pere);
                if(tokenTag() == "end"){

                    advanceToken();
                    declaration23(pere);
                }
                else{
                    printErrorAndSkip("'end' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                }
            } else {
                printErrorAndSkip("identificateur ou entier ou chaine ou 'true' ou 'false' ou 'null' ou '(' ou 'not' ou '-' ou 'character val'ou 'begin' ou 'if' ou 'for' ou 'while' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        } else if (tokenTag() == "type" || tokenTag() == "procedure" || tokenTag() == "function" || tokenTag() == "VARIABLE") {
            System.out.println("r15");
            TreeNode noeud = new TreeNode("DECL");
            declarationPlus(noeud);
            pere.addSon(noeud);
            if (tokenTag() == "begin") {
                advanceToken();
                instructionPlus(pere);
                if(tokenTag() == "end"){

                    advanceToken();
                    declaration23(pere);
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

        System.out.print("declaration23 : ");
        this.rulesName.add("declaration23");
        if (tokenTag() == ";") {

            System.out.println("r16");
            advanceToken();
            return;
        } else if (tokenTag() == "VARIABLE") {

            System.out.println("r17");
            advanceToken();
            if (tokenTag() == ";") {

                advanceToken();
            } else {
                printErrorAndSkip("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        } else {
            printErrorAndSkip("';' ou identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }

    public void declaration31(TreeNode pere) throws ExceptionSyntaxique {

        System.out.print("declaration31 : ");
        this.rulesName.add("declaration31");
        if (tokenTag() == "return") {
            // On ajoute un noeud.
            TreeNode noeud = new TreeNode(tokenTag());
            pere.addSon(noeud);

            System.out.println("r18");
            advanceToken();
            if (tokenTag() == "VARIABLE") {
                // On ajoute un noeud.
                noeud.addSon(new TreeNode(((VarToken) getNextToken()).getValue()));
                advanceToken();
            } else {
                printErrorAndSkip("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
            if (tokenTag() == "is") {

                advanceToken();
                declaration22(pere);
            } else {
                printErrorAndSkip("'is' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        } else if (tokenTag() == "(") {
            System.out.println("r19");
            params(pere);
            if (tokenTag() == "return") {
                // On ajoute un noeud.
                TreeNode noeud = new TreeNode(tokenTag());
                pere.addSon(noeud);
                advanceToken();
                if (tokenTag() == "VARIABLE") {
                    // On ajoute un noeud.
                    noeud.addSon(new TreeNode(((VarToken) getNextToken()).getValue()));
                    advanceToken();
                } else {
                    printErrorAndSkip("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                }
                if (tokenTag() == "is") {

                    advanceToken();
                    declaration22(pere);
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

    public void declaration41(TreeNode pere) throws ExceptionSyntaxique {

        System.out.print("declaration41 : ");
        this.rulesName.add("declaration41");
        if (tokenTag() == ";") {

            System.out.println("r20");
            advanceToken();
            return;
        } else if (tokenTag() == ":") {

            System.out.println("r21");
            advanceToken();
            if (tokenTag() == "=") {

                advanceToken();
                TreeNode noeud = new TreeNode("VAL");
                expression(noeud);
                pere.addSon(noeud);
                if (tokenTag() == ";") {

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

    public void champs(TreeNode pere) throws ExceptionSyntaxique {

        System.out.print("champs : ");
        this.rulesName.add("champs");
        if (tokenTag() == "VARIABLE"){
            System.out.println("r22");

            TreeNode noeud = new TreeNode("");

            identificateurVirgulePlus(noeud);
            if (tokenTag() == ":"){

                advanceToken();
            } else{
                printErrorAndSkip("':' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());

            }
            // A vérifier pour l'affichage de l'arbre ici
            if (tokenTag() == "VARIABLE"){
                advanceToken();
                noeud.addSon(new TreeNode(((VarToken) getNextToken()).getValue()));

            } else{
                printErrorAndSkip("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
            if (tokenTag() == ";"){

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

    public void params(TreeNode pere) throws ExceptionSyntaxique {
        // On crée un nouveau noeud.
        TreeNode noeud = new TreeNode("params");

        // On l'ajoute à l'arbre.
        pere.addSon(noeud);

        System.out.print("params : ");
        this.rulesName.add("params");
        if (tokenTag() == "(") {

            System.out.println("r23");
            advanceToken();
            paramPointVirgulePlus(noeud);
            if (tokenTag() == ")") {
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

        System.out.print("param : ");
        this.rulesName.add("param");
        if (tokenTag() == "VARIABLE") {

            TreeNode noeud = new TreeNode("");
            TreeNode noeud2 = new TreeNode("");

            System.out.println("r24");
            identificateurVirgulePlus(noeud);
            if (tokenTag() == ":") {

                advanceToken();
            } else {
                printErrorAndSkip("':' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
            if (tokenTag() == "VARIABLE") {
                param2(noeud2);
                noeud.setLabel(noeud2.getLastSon().getLabel());
                for (int i=noeud2.getSons().size()-2; i>= 0; i--){
                    noeud.addSon(noeud2.getSons().get(i));
                }
                pere.addSon(noeud);

            } else {
                printErrorAndSkip("identificateur ou 'access' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        }else {
            printErrorAndSkip("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }


    public void param2(TreeNode pere) throws ExceptionSyntaxique {

        System.out.print("param2 : ");
        this.rulesName.add("param2");
        if (tokenTag() == "VARIABLE") {
            System.out.println("r25");
            pere.addSon(new TreeNode(((VarToken) getNextToken()).getValue()));
            advanceToken();
            return;
        } else if (tokenTag() == "in") {
            System.out.println("r26");
            mode(pere);
        } else {
            printErrorAndSkip("identificateur ou 'access' ou 'in' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }

    public void mode(TreeNode pere) throws ExceptionSyntaxique {

        System.out.print("mode : ");
        this.rulesName.add("mode");
        if (tokenTag() == "in") {
            // On ajoute un noeud.
            pere.addSon(new TreeNode(tokenTag()));

            System.out.println("r27");
            advanceToken();
            mode1(pere);
        } else {
            printErrorAndSkip("'in' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }

    public void mode1(TreeNode pere) throws ExceptionSyntaxique {

        System.out.print("mode1 : ");
        this.rulesName.add("mode1");
        if (tokenTag() == "out") {
            // On ajoute un noeud.
            pere.addSon(new TreeNode(tokenTag()));

            System.out.println("r28");
            advanceToken();
            return;
        }
        else
        {
            System.out.println("r29");
            return;
        }
    }


    public void expression(TreeNode pere) throws ExceptionSyntaxique {

        System.out.print("expression : ");
        this.rulesName.add("expression");
        if (tokenTag() == "-"
                || tokenTag() == "not"
                || tokenTag() == "NUM_CONST"
                || tokenTag() == "STR_CONST"
                || tokenTag() == "true"
                || tokenTag() == "false"
                || tokenTag() == "null"
                || tokenTag() == "character'val"
                || tokenTag() == "("
                || tokenTag() == "VARIABLE") {
            System.out.println("r30");
            TreeNode noeud = new TreeNode("T");
            T(noeud);
            expression1(pere);
            if(pere.getSons().size() != 0)
                pere.getLastSon().addFirstSon((noeud.getLastSon()));
            else
                pere.addSon(noeud.getLastSon());
        } else {
            printErrorAndSkip("identificateur ou entier ou chaine ou 'true' ou 'false' ou 'null' ou '(' ou 'not' ou '-' ou 'character val'attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }

    public void expression1(TreeNode pere) throws ExceptionSyntaxique {

        System.out.print("expression1 : ");
        this.rulesName.add("expression1");
        if (tokenTag() == "+") {
            // On ajoute un noeud.
            TreeNode noeud = new TreeNode(tokenTag());
            pere.addSon(noeud);

            System.out.println("r31");
            advanceToken();
            expression(noeud);
            return;
        } else if (tokenTag() == "-") {
            // On ajoute un noeud.
            TreeNode noeud = new TreeNode(tokenTag());
            pere.addSon(noeud);

            System.out.println("r32");
            advanceToken();
            expression(noeud);
        } else {
            System.out.println("r33");
            return;
        }
    }

    public void T(TreeNode pere) throws ExceptionSyntaxique {

        System.out.print("T : ");
        this.rulesName.add("T");
        if (tokenTag() == "-"
                || tokenTag() == "not"
                || tokenTag() == "NUM_CONST"
                || tokenTag() == "STR_CONST"
                || tokenTag() == "true"
                || tokenTag() == "false"
                || tokenTag() == "null"
                || tokenTag() == "character'val"
                || tokenTag() == "("
                || tokenTag() == "VARIABLE") {
            System.out.println("r34");
            TreeNode noeud = new TreeNode("T");
            I(noeud);
            T1(pere);
            if(pere.getSons().size() != 0)
                pere.getLastSon().addFirstSon((noeud.getLastSon()));
            else
                pere.addSon(noeud.getLastSon());
        } else {
            printErrorAndSkip("identificateur ou entier ou chaine ou 'true' ou 'false' ou 'null' ou '(' ou 'not' ou '-' ou 'character val'attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }

    public void T1(TreeNode pere) throws ExceptionSyntaxique {

        System.out.print("T1 : ");
        this.rulesName.add("T1");
        if (tokenTag() == "*") {
            // On ajoute un noeud.
            TreeNode noeud = new TreeNode(tokenTag());
            pere.addSon(noeud);

            System.out.println("r35");
            advanceToken();
            T(noeud);
            return;
        } else if (tokenTag() == "/") {
            // On ajoute un noeud.
            TreeNode noeud = new TreeNode(tokenTag());
            pere.addSon(noeud);

            System.out.println("r36");
            advanceToken();
            T(noeud);
            return;
        } else {
            System.out.println("r37");
            return;
        }
    }

    public void I(TreeNode pere) throws ExceptionSyntaxique {

        System.out.print("I : ");
        this.rulesName.add("I");
        if (tokenTag() == "-"
                || tokenTag() == "not"
                || tokenTag() == "NUM_CONST"
                || tokenTag() == "STR_CONST"
                || tokenTag() == "true"
                || tokenTag() == "false"
                || tokenTag() == "null"
                || tokenTag() == "character'val"
                || tokenTag() == "("
                || tokenTag() == "VARIABLE") {
            System.out.println("r38");
            TreeNode noeud = new TreeNode("I");
            F(noeud);
            I1(pere);
            if(pere.getSons().size() != 0)
                pere.getLastSon().addFirstSon((noeud.getLastSon()));
            else
                pere.addSon(noeud.getLastSon());
        } else {
            printErrorAndSkip("identificateur ou entier ou chaine ou 'true' ou 'false' ou 'null' ou '(' ou 'not' ou '-' ou 'character val'attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }

    public void I1(TreeNode pere) throws ExceptionSyntaxique {

        System.out.print("I1 : ");
        this.rulesName.add("I1");
        if (tokenTag() == "=") {
            // On ajoute un noeud.
            TreeNode noeud = new TreeNode(tokenTag());
            pere.addSon(noeud);

            System.out.println("r39");
            advanceToken();
            T(noeud);
            return;
        } else if (tokenTag() == "/=") {
            // On ajoute un noeud.
            TreeNode noeud = new TreeNode(tokenTag());
            pere.addSon(noeud);

            System.out.println("r40");
            advanceToken();
            T(noeud);
            return;
        } else if (tokenTag() == "<") {
            // On ajoute un noeud.
            TreeNode noeud = new TreeNode(tokenTag());
            pere.addSon(noeud);

            System.out.println("r41");
            advanceToken();
            T(noeud);
            return;
        } else if (tokenTag() == "<=") {
            // On ajoute un noeud.
            TreeNode noeud = new TreeNode(tokenTag());
            pere.addSon(noeud);

            System.out.println("r42");
            advanceToken();
            T(noeud);
            return;
        } else if (tokenTag() == ">") {
            // On ajoute un noeud.
            TreeNode noeud = new TreeNode(tokenTag());
            pere.addSon(noeud);

            System.out.println("r43");
            advanceToken();
            T(noeud);
            return;
        } else if (tokenTag() == ">=") {
            // On ajoute un noeud.
            TreeNode noeud = new TreeNode(tokenTag());
            pere.addSon(noeud);

            System.out.println("r44");
            advanceToken();
            T(noeud);
            return;
        } else if (tokenTag() == "rem") {
            // On ajoute un noeud.
            TreeNode noeud = new TreeNode(tokenTag());
            pere.addSon(noeud);

            System.out.println("r45");
            advanceToken();
            T(noeud);
            return;
        } else {
            System.out.println("r46");
            return;
        }
    }

    public void F(TreeNode pere) throws ExceptionSyntaxique {

        System.out.print("F : ");
        this.rulesName.add("F");
        if (tokenTag() == "NUM_CONST"
                || tokenTag() == "STR_CONST"
                || tokenTag() == "true"
                || tokenTag() == "false"
                || tokenTag() == "null"
                || tokenTag() == "character'val"
                || tokenTag() == "("
                || tokenTag() == "VARIABLE") {
            System.out.println("r47");
            P(pere);
            return;
        } else if (tokenTag() == "-") {
            // On ajoute un noeud.
            TreeNode noeud = new TreeNode(tokenTag());
            pere.addSon(noeud);

            System.out.println("r48");
            advanceToken();
            if (tokenTag() == "NUM_CONST"
                    || tokenTag() == "STR_CONST"
                    || tokenTag() == "true"
                    || tokenTag() == "false"
                    || tokenTag() == "null"
                    || tokenTag() == "character'val"
                    || tokenTag() == "("
                    || tokenTag() == "VARIABLE") {
                P(noeud);
                return;
            } else {
                printErrorAndSkip("identificateur ou entier ou chaine ou 'true' ou 'false' ou 'null' ou '(' ou '-' ou 'character val'attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        } else if (tokenTag() == "not") {
            // On ajoute un noeud.
            TreeNode noeud = new TreeNode(tokenTag());
            pere.addSon(noeud);

            System.out.println("r49");
            advanceToken();
            if (tokenTag() == "NUM_CONST"
                    || tokenTag() == "STR_CONST"
                    || tokenTag() == "true"
                    || tokenTag() == "false"
                    || tokenTag() == "null"
                    || tokenTag() == "character'val"
                    || tokenTag() == "("
                    || tokenTag() == "VARIABLE") {
                P(noeud);
                return;
            } else {
                printErrorAndSkip("identificateur ou entier ou chaine ou 'true' ou 'false' ou 'null' ou '(' ou 'not' ou 'character val'attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        } else {
            printErrorAndSkip("identificateur ou entier ou chaine ou 'true' ou 'false' ou 'null' ou '(' ou 'not' ou '-' ou 'character val'attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }

    public void P(TreeNode pere) throws ExceptionSyntaxique {

        System.out.print("P : ");
        this.rulesName.add("P");
        if (tokenTag() == "NUM_CONST"){
            // On ajoute un noeud.
            TreeNode noeud = new TreeNode(((NumToken) getNextToken()).getValue().toString());
            pere.addSon(noeud);

            System.out.println("r50");
            advanceToken();
            Precur(noeud);
            return;
        }
        else if (tokenTag() == "STR_CONST"){
            // On ajoute un noeud.
            TreeNode noeud = new TreeNode(((StringToken) getNextToken()).getValue());
            pere.addSon(noeud);

            System.out.println("r51");
            advanceToken();
            Precur(noeud);
            return;
        }
        else if (tokenTag() == "true"){
            // On ajoute un noeud.
            TreeNode noeud = new TreeNode(tokenTag());
            pere.addSon(noeud);

            System.out.println("r52");
            advanceToken();
            Precur(noeud);
            return;
        }
        else if (tokenTag() == "false"){
            // On ajoute un noeud.
            TreeNode noeud = new TreeNode(tokenTag());
            pere.addSon(noeud);

            System.out.println("r53");
            advanceToken();
            Precur(noeud);
            return;
        }
        else if (tokenTag() == "null"){
            // On ajoute un noeud.
            TreeNode noeud = new TreeNode(tokenTag());
            pere.addSon(noeud);

            System.out.println("r54");
            advanceToken();
            Precur(noeud);
            return;
        }
        else if (tokenTag() == "character'val"){
            // On ajoute un noeud.
            TreeNode noeud = new TreeNode(tokenTag());
            pere.addSon(noeud);

            System.out.println("r55");
            advanceToken();
            if (tokenTag() == "("){

                advanceToken();
                if (tokenTag() == "-" ||
                tokenTag() == "not" ||
                tokenTag() == "NUM_CONST" ||
                tokenTag() == "STR_CONST" ||
                tokenTag() == "true" ||
                tokenTag() == "false" ||
                tokenTag() == "null" ||
                tokenTag() == "new" ||
                tokenTag() == "character'val" ||
                tokenTag() == "(" ||
                tokenTag() == "VARIABLE"){
                    expression(noeud);
                    if (tokenTag() == ")"){
                        advanceToken();
                        Precur(noeud);
                        return;
                    } else{
                        printErrorAndSkip("')' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                    }
                } else{
                    printErrorAndSkip("expression attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                }
            } else{
                printErrorAndSkip("'(' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        }
        else if (tokenTag() == "("){

            System.out.println("r56");
            advanceToken();
            expression(pere);
            if (tokenTag() == ")"){

                advanceToken();
                return;
            }
            else{
                printErrorAndSkip("')' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        }
        else if (tokenTag() == "VARIABLE"){
            // On ajoute un noeud.
            TreeNode noeud = new TreeNode(((VarToken) getNextToken()).getValue());
            pere.addSon(noeud);

            System.out.println("r57");
            advanceToken();
            P1(noeud);
            return;
        }
        else{
            printErrorAndSkip("identificateur ou entier ou chaine ou 'true' ou 'false' ou 'null' ou '(' ou 'not' ou '-' ou 'character val'attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }

    public void Precur(TreeNode pere) throws ExceptionSyntaxique {

        System.out.print("Precur : ");
        this.rulesName.add("Precur");
        if (tokenTag() == "."){
            //LL(3) ici

            advanceToken();
            if (tokenTag() == "VARIABLE"){

                advanceToken();
                if(tokenTag() == ":"){
                    System.out.println("r59");
                    decrementToken();
                    decrementToken();
                    return;
                }
                else{

                    // On ajoute un noeud.
                    TreeNode noeud = new TreeNode("VARIABLE");
                    pere.addSon(noeud);

                    System.out.println("r58");
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
            System.out.println("r59");
            return;
        }
        else{
            printErrorAndSkip("')' ou '.' ou '=' ou '/=' ou '<' ou '<=' ou '>' ou '>=' ou '+' ou '-' ou '*' ou '/' ou 'rem' ou '+' ou '-' ou ';' ou ')' ou 'loop' ou 'then' ou '..' ou ',' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }

    }

    public void P1(TreeNode pere) throws ExceptionSyntaxique {

        System.out.print("P1 : ");
        this.rulesName.add("P1");
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
            System.out.println("r60");
            Precur(pere);
            return;

        }
        else if (tokenTag() == "("){

            TreeNode arg = new TreeNode("ARGUMENT");
            pere.addSon(arg);

            System.out.println("r61");
            advanceToken();
            expressionPlusVirgule(arg);
            if (tokenTag() == ")"){

                advanceToken();
                Precur(pere);
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

        TreeNode affect = new TreeNode("AFFECT");

        System.out.print("instruction : ");
        this.rulesName.add("instruction");
        if (tokenTag() == "VARIABLE"){
            // On ajoute un noeud.
            TreeNode noeud_bis = new TreeNode(((VarToken) getNextToken()).getValue());
            TreeNode noeud2 = new TreeNode("");

            System.out.println("r62");
            advanceToken();
            instruction1(noeud2);

            noeud2.addFirstSon(noeud_bis);
            pere.addSon(noeud2);

            return;
        }
        else if (tokenTag() == "-"){
            System.out.println("r63");
            pere.addSon(affect);
            // On ajoute un noeud.
            TreeNode minus = new TreeNode(tokenTag());
            TreeNode left1 = new TreeNode("");
            TreeNode left2 = new TreeNode("");
            TreeNode left3 = new TreeNode("");
            affect.addSon(minus);

            advanceToken();
            P(minus);
            I1(left1);
            T1(left2);
            expression1(left3);


            // TODO
            // A tester, pcq sus
            left1.getSons().get(0).addFirstSon(minus);
            left2.getSons().get(0).addFirstSon(left1.getSons().get(0));
            left3.getSons().get(0).addFirstSon(left2.getSons().get(0));
            affect.addSon(left3.getSons().get(0));


            if (tokenTag() == "."){

                advanceToken();
                if (tokenTag() == "VARIABLE"){
                    // On ajoute un noeud.
                    affect.addSon(new TreeNode(((VarToken) getNextToken()).getValue()));

                    advanceToken();
                    if (tokenTag() == ":"){

                        advanceToken();
                        if (tokenTag() == "="){

                            advanceToken();
                            expression(affect);
                            if (tokenTag() == ";"){

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
            System.out.println("r64");
            // On ajoute un noeud.
            pere.addSon(affect);
            // On ajoute un noeud.
            TreeNode nott = new TreeNode(tokenTag());
            TreeNode left1 = new TreeNode("");
            TreeNode left2 = new TreeNode("");
            TreeNode left3 = new TreeNode("");
            affect.addSon(nott);

            advanceToken();
            P(nott);
            I1(left1);
            T1(left2);
            expression1(left3);


            // TODO
            // A tester, pcq sus
            left1.getSons().get(0).addFirstSon(nott);
            left2.getSons().get(0).addFirstSon(left1.getSons().get(0));
            left3.getSons().get(0).addFirstSon(left2.getSons().get(0));
            affect.addSon(left3.getSons().get(0));
            if (tokenTag() == "."){

                advanceToken();
                if (tokenTag() == "VARIABLE"){
                    // On ajoute un noeud.
                    affect.addSon(new TreeNode(((VarToken) getNextToken()).getValue()));

                    advanceToken();
                    if (tokenTag() == ":"){

                        advanceToken();
                        if (tokenTag() == "="){

                            advanceToken();
                            expression(affect);
                            if (tokenTag() == ";"){

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
            System.out.println("r65");
            // On ajoute un noeud.
            pere.addSon(affect);

            TreeNode entier = new TreeNode(((NumToken) getNextToken()).getValue().toString());
            TreeNode left1 = new TreeNode("");
            TreeNode left2 = new TreeNode("");
            TreeNode left3 = new TreeNode("");
            TreeNode left4 = new TreeNode("");


            advanceToken();
            Precur(left1);
            I1(left2);
            T1(left3);
            expression1(left4);

            // TODO
            // A tester, pcq sus
            left1.getSons().get(0).addFirstSon(entier);
            left2.getSons().get(0).addFirstSon(left1.getSons().get(0));
            left3.getSons().get(0).addFirstSon(left2.getSons().get(0));
            left4.getSons().get(0).addFirstSon(left3.getSons().get(0));
            affect.addSon(left4.getSons().get(0));

            if(tokenTag() == "."){
                advanceToken();
                if (tokenTag() == "VARIABLE"){
                    // On ajoute un noeud.
                    affect.addSon(new TreeNode(((VarToken) getNextToken()).getValue()));

                    advanceToken();
                    if (tokenTag() == ":"){

                        advanceToken();
                        if (tokenTag() == "="){

                            advanceToken();
                            expression(affect);
                            if (tokenTag() == ";"){
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
            System.out.println("r66");
            // On ajoute un noeud.
            pere.addSon(affect);

            TreeNode character = new TreeNode(((NumToken) getNextToken()).getValue().toString());
            TreeNode left1 = new TreeNode("");
            TreeNode left2 = new TreeNode("");
            TreeNode left3 = new TreeNode("");
            TreeNode left4 = new TreeNode("");

            advanceToken();
            Precur(left1);
            I1(left2);
            T1(left3);
            expression1(left4);

            // TODO
            // A tester, pcq sus
            left1.getSons().get(0).addFirstSon(character);
            left2.getSons().get(0).addFirstSon(left1.getSons().get(0));
            left3.getSons().get(0).addFirstSon(left2.getSons().get(0));
            left4.getSons().get(0).addFirstSon(left3.getSons().get(0));
            affect.addSon(left4.getSons().get(0));
            if(tokenTag() == "."){
                advanceToken();
                if (tokenTag() == "VARIABLE"){
                    // On ajoute un noeud.
                    affect.addSon(new TreeNode(((VarToken) getNextToken()).getValue()));
                    advanceToken();
                    if (tokenTag() == ":"){
                        advanceToken();
                        if (tokenTag() == "="){
                            advanceToken();
                            expression(affect);
                            if (tokenTag() == ";"){
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
            System.out.println("r67");
            // On ajoute un noeud.
            pere.addSon(affect);

            TreeNode tru = new TreeNode(tokenTag());
            TreeNode left1 = new TreeNode("");
            TreeNode left2 = new TreeNode("");
            TreeNode left3 = new TreeNode("");
            TreeNode left4 = new TreeNode("");

            advanceToken();
            Precur(left1);
            I1(left2);
            T1(left3);
            expression1(left4);

            // TODO
            // A tester, pcq sus
            left1.getSons().get(0).addFirstSon(tru);
            left2.getSons().get(0).addFirstSon(left1.getSons().get(0));
            left3.getSons().get(0).addFirstSon(left2.getSons().get(0));
            left4.getSons().get(0).addFirstSon(left3.getSons().get(0));
            affect.addSon(left4.getSons().get(0));

            if(tokenTag() == "."){
                advanceToken();
                if (tokenTag() == "VARIABLE"){
                    // On ajoute un noeud.
                    affect.addSon(new TreeNode(((VarToken) getNextToken()).getValue()));
                    advanceToken();
                    if (tokenTag() == ":"){
                        advanceToken();
                        if (tokenTag() == "="){
                            advanceToken();
                            expression(affect);
                            if (tokenTag() == ";"){
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
            System.out.println("r68");
            // On ajoute un noeud.
            pere.addSon(affect);

            TreeNode fals = new TreeNode(tokenTag());
            TreeNode left1 = new TreeNode("");
            TreeNode left2 = new TreeNode("");
            TreeNode left3 = new TreeNode("");
            TreeNode left4 = new TreeNode("");
            
            advanceToken();
            Precur(left1);
            I1(left2);
            T1(left3);
            expression1(left4);

            left1.getSons().get(0).addFirstSon(fals);
            left2.getSons().get(0).addFirstSon(left1.getSons().get(0));
            left3.getSons().get(0).addFirstSon(left2.getSons().get(0));
            left4.getSons().get(0).addFirstSon(left3.getSons().get(0));
            affect.addSon(left4.getSons().get(0));

            if(tokenTag() == "."){
                advanceToken();
                if (tokenTag() == "VARIABLE"){
                    // On ajoute un noeud.
                    affect.addSon(new TreeNode(((VarToken) getNextToken()).getValue()));

                    advanceToken();
                    if (tokenTag() == ":"){
                        advanceToken();
                        if (tokenTag() == "="){
                            advanceToken();
                            expression(affect);
                            if (tokenTag() == ";"){
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
            System.out.println("r69");
            // On ajoute un noeud.
            pere.addSon(affect);

            TreeNode nul = new TreeNode(tokenTag());
            TreeNode left1 = new TreeNode("");
            TreeNode left2 = new TreeNode("");
            TreeNode left3 = new TreeNode("");
            TreeNode left4 = new TreeNode("");

            advanceToken();
            Precur(left1);
            I1(left2);
            T1(left3);
            expression1(left4);
            if(tokenTag() == "."){
                advanceToken();
                if (tokenTag() == "VARIABLE"){
                    // On ajoute un noeud.
                    affect.addSon(new TreeNode(((VarToken) getNextToken()).getValue()));

                    advanceToken();
                    if (tokenTag() == ":"){
                        advanceToken();
                        if (tokenTag() == "="){
                            advanceToken();
                            expression(affect);
                            if (tokenTag() == ";"){
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
        else if (tokenTag() == "character'val"){
            System.out.println("r70");
            // On ajoute un noeud.
            pere.addSon(affect);
            advanceToken();
            if (tokenTag() == "("){
                advanceToken();
                TreeNode characterVAL = new TreeNode("character'val");
                expression(characterVAL);
                if (tokenTag() == ")"){
                    TreeNode left1 = new TreeNode("");
                    TreeNode left2 = new TreeNode("");
                    TreeNode left3 = new TreeNode("");
                    TreeNode left4 = new TreeNode("");
                    advanceToken();
                    Precur(left1);
                    I1(left2);
                    T1(left3);
                    expression1(left4);

                    left1.getSons().get(0).addFirstSon(characterVAL);
                    left2.getSons().get(0).addFirstSon(left1.getSons().get(0));
                    left3.getSons().get(0).addFirstSon(left2.getSons().get(0));
                    left4.getSons().get(0).addFirstSon(left3.getSons().get(0));
                    affect.addSon(left4.getSons().get(0));
                    if(tokenTag() == "."){
                        advanceToken();
                        if (tokenTag() == "VARIABLE"){
                            // On ajoute un noeud.
                            affect.addSon(new TreeNode(((VarToken) getNextToken()).getValue()));

                            advanceToken();
                            if (tokenTag() == ":"){
                                advanceToken();
                                if (tokenTag() == "="){
                                    advanceToken();
                                    expression(affect);
                                    if (tokenTag() == ";"){
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
            System.out.println("r71");
            // On ajoute un noeud.
            pere.addSon(affect);

            TreeNode prio = new TreeNode("");
            advanceToken();
            expression(prio);
            if (tokenTag() == ")"){
                advanceToken();

                TreeNode left1 = new TreeNode("");
                TreeNode left2 = new TreeNode("");
                TreeNode left3 = new TreeNode("");
                TreeNode left4 = new TreeNode("");
                Precur(left1);
                I1(left2);
                T1(left3);
                expression1(left4);

                left1.getSons().get(0).addFirstSon(prio);
                left2.getSons().get(0).addFirstSon(left1.getSons().get(0));
                left3.getSons().get(0).addFirstSon(left2.getSons().get(0));
                left4.getSons().get(0).addFirstSon(left3.getSons().get(0));
                affect.addSon(left4.getSons().get(0));

                if(tokenTag() == "."){
                    advanceToken();
                    if (tokenTag() == "VARIABLE"){
                        // On ajoute un noeud.
                        affect.addSon(new TreeNode(((VarToken) getNextToken()).getValue()));

                        advanceToken();
                        if (tokenTag() == ":"){
                            advanceToken();
                            if (tokenTag() == "="){
                                advanceToken();
                                expression(affect);
                                if (tokenTag() == ";"){
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
            TreeNode ret = new TreeNode(tokenTag());
            pere.addSon(ret);

            System.out.println("r72");
            advanceToken();
            instruction2(ret);
            return;
        }
        else if(tokenTag() == "begin"){

            System.out.println("r73");
            advanceToken();
            instructionPlus(pere);
            if(tokenTag() == "end"){
                advanceToken();
                if(tokenTag() == ";"){
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
            TreeNode nodeIf = new TreeNode(tokenTag());
            pere.addSon(nodeIf);

            System.out.println("r74");
            advanceToken();
            expression(nodeIf);
            if(tokenTag()=="then"){

                advanceToken();
                instructionPlus(nodeIf);
                instruction3(nodeIf);
                return;
            }
            else{
                printErrorAndSkip("'then' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        }
        else if(tokenTag() == "for"){
            // On ajoute un noeud.
            TreeNode nodeFor = new TreeNode(tokenTag());
            pere.addSon(nodeFor);

            System.out.println("r75");
            advanceToken();
            if (tokenTag() == "VARIABLE") {
                // On ajoute un noeud.
                nodeFor.addSon(new TreeNode(((VarToken) getNextToken()).getValue()));

                advanceToken();
            } else {
                printErrorAndSkip("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
            if (tokenTag() == "in") {

                advanceToken();
            } else {
                printErrorAndSkip("'in' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
            instruction4(nodeFor);
        }
        else if(tokenTag() == "while"){
            // On ajoute un noeud.
            TreeNode nodeWhile = new TreeNode(tokenTag());
            pere.addSon(nodeWhile);

            System.out.println("r76");
            advanceToken();
            expression(nodeWhile);
            if(tokenTag() == "loop"){

                advanceToken();
                instructionPlus(nodeWhile);
                if(tokenTag() == "end"){

                    advanceToken();
                    if(tokenTag() == "loop"){

                        advanceToken();
                        if (tokenTag() == ";") {

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
            TreeNode nodePut = new TreeNode(tokenTag());
            pere.addSon(nodePut);

            System.out.println("r77");
            advanceToken();
            if (tokenTag() == "(") {

                advanceToken();
                expression(nodePut);
                if (tokenTag() == ")") {

                    advanceToken();
                    if (tokenTag() == ";") {

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
            printErrorAndSkip("identificateur ou entier ou chaine ou 'true' ou 'false' ou 'null' ou '(' ou 'not' ou '-' ou 'character'val' ou 'return' ou 'begin' ou 'if' ou 'for' ou 'while' ou 'put' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }

    public void instruction1(TreeNode pere) throws ExceptionSyntaxique {

        System.out.print("instruction1 : ");
        this.rulesName.add("instruction1");
        if (tokenTag() == ":"){
            pere.setLabel("AFFECT");

            System.out.println("r78");
            advanceToken();
            if (tokenTag() == "="){

                advanceToken();
                expression(pere);
                if (tokenTag() == ";"){

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

            pere.setLabel("APPEL");

            System.out.println("r79");
            advanceToken();
            return;
        }
        else if (tokenTag() == "("){

            pere.setLabel("APPEL");

            System.out.println("r80");
            advanceToken();
            expressionPlusVirgule(pere);
            if (tokenTag() == ")"){

                advanceToken();
                instruction11(pere);
                return;
            } else{
                printErrorAndSkip("')' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        }
        else if (tokenTag() == "."){

            pere.setLabel("APPEL");

            System.out.println("r81");
            advanceToken();
            if (tokenTag() == "VARIABLE"){
                // On ajoute un noeud.
                pere.addSon(new TreeNode(((VarToken) getNextToken()).getValue()));

                advanceToken();
                I1(pere);
                T1(pere);
                expression1(pere);
                if (tokenTag() == "."){

                    advanceToken();
                    if (tokenTag() == "VARIABLE"){
                        // On ajoute un noeud.
                        pere.addSon(new TreeNode(((VarToken) getNextToken()).getValue()));

                        advanceToken();
                        if (tokenTag() == ":"){

                            advanceToken();
                            if (tokenTag() == "="){

                                advanceToken();
                                expression(pere);
                                if (tokenTag() == ";"){

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
            System.out.println("r82");
            I1(pere);
            T1(pere);
            expression1(pere);
            if (tokenTag() == "."){

                advanceToken();
                if (tokenTag() == "VARIABLE"){
                    // On ajoute un noeud.
                    pere.addSon(new TreeNode(((VarToken) getNextToken()).getValue()));

                    advanceToken();
                    if (tokenTag() == ":"){
                        advanceToken();
                        if (tokenTag() == "="){

                            advanceToken();
                            expression(pere);
                            if (tokenTag() == ";"){

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

        System.out.print("instruction11 : ");
        this.rulesName.add("instruction11");
        if (tokenTag() == ";"){
            System.out.println("r83");
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
            System.out.println("r84");
            TreeNode left1 = new TreeNode("");
            TreeNode left2 = new TreeNode("");
            TreeNode left3 = new TreeNode("");
            TreeNode left4 = new TreeNode("");
            TreeNode affect = new TreeNode("AFFECT");
            Precur(left1);
            I1(left2);
            T1(left3);
            expression1(left4);
            pere.addSon(affect);
            // TODO
            // A tester, pcq sus
            left1.getSons().get(0).addFirstSon(affect);
            left2.getSons().get(0).addFirstSon(left1.getSons().get(0));
            left3.getSons().get(0).addFirstSon(left2.getSons().get(0));
            left4.getSons().get(0).addFirstSon(left3.getSons().get(0));
            affect.addSon(left4.getSons().get(0));

            if (tokenTag() == "."){
                advanceToken();
                if (tokenTag() == "VARIABLE"){
                    // On ajoute un noeud.
                    affect.addSon(new TreeNode(((VarToken) getNextToken()).getValue()));

                    advanceToken();
                    if (tokenTag() == ":"){
                        advanceToken();
                        if (tokenTag() == "="){
                            advanceToken();
                            expression(affect);
                            if (tokenTag() == ";"){
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
        System.out.print("instruction2 : ");
        this.rulesName.add("instruction2");
        if (tokenTag() == ";"){
            System.out.println("r86");
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
            System.out.println("r85");
            expression(pere);
            if (tokenTag() == ";") {
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
        System.out.print("instruction3 : ");
        this.rulesName.add("instruction3");
        if (tokenTag() == "end"){
            System.out.println("r87");
            advanceToken();
            if (tokenTag() == "if") {
                advanceToken();
            } else {
                printErrorAndSkip("'if' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
            if (tokenTag() == ";") {
                advanceToken();
                return;
            } else {
                printErrorAndSkip("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        }
        else if (tokenTag() == "else"){
            // On ajoute un noeud.
            TreeNode elsse = new TreeNode(tokenTag());
            pere.addSon(elsse);

            System.out.println("r88");
            advanceToken();
            instructionPlus(elsse);
            if (tokenTag() == "end"){
                advanceToken();
                if (tokenTag() == "if") {
                    advanceToken();
                } else {
                    printErrorAndSkip("'if' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                }
                if (tokenTag() == ";") {
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
            System.out.println("r89");
            elsifPlus(pere);
            instruction3(pere);
        } else{
            printErrorAndSkip("'else' ou 'end' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }

    public void instruction4(TreeNode pere) throws ExceptionSyntaxique {
        System.out.print("instruction4 : ");
        this.rulesName.add("instruction4");
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
            System.out.println("r90");
            TreeNode range = new TreeNode("range");
            expression(range);
            if (tokenTag() == "DOTDOT") {
                advanceToken();
            } else {
                printErrorAndSkip("'..' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
            expression(range);
            pere.addSon(range);
            if (tokenTag() == "loop") {
                advanceToken();
            } else {
                printErrorAndSkip("'loop' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
            instructionPlus(pere);
            if (tokenTag() == "end") {
                advanceToken();
            } else {
                printErrorAndSkip("'end' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
            if (tokenTag() == "loop") {
                advanceToken();
            } else {
                printErrorAndSkip("'loop' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
            if (tokenTag() == ";") {
                advanceToken();
                return;
            } else {
                printErrorAndSkip("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        } else if(tokenTag() == "reverse")
        {
            // On ajoute un noeud.
            TreeNode reverserange = new TreeNode("reversed_range");

            System.out.println("r91");
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
                expression(reverserange);
                if (tokenTag() == "DOTDOT") {
                    advanceToken();
                } else {
                    printErrorAndSkip("'..' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                }
                expression(reverserange);
                if (tokenTag() == "loop") {
                    advanceToken();
                } else {
                    printErrorAndSkip("'loop' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                }
                instructionPlus(pere);
                if (tokenTag() == "end") {
                    advanceToken();
                } else {
                    printErrorAndSkip("'end' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                }
                if (tokenTag() == "loop") {
                    advanceToken();
                } else {
                    printErrorAndSkip("'loop' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
                }
                if (tokenTag() == ";") {
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
        //Lignes de debug
        //System.out.println("token actuel "+ this.tokens.get(currentTokenIndex).getTag());
        //System.out.println("token suivant "+ this.tokens.get(currentTokenIndex+1).getTag());
        //System.out.println("token précédent "+ this.tokens.get(currentTokenIndex-1).getTag());
        //System.out.println("token index "+ currentTokenIndex);
        System.out.print("instructionPlus : ");
        this.rulesName.add("instructionPlus");
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
            System.out.println("r92");
            instruction(pere);
            instructionPlus1(pere);
        } else {
            printErrorAndSkip("identificateur ou entier ou chaine ou 'true' ou 'false' ou 'null' ou '(' ou 'not' ou '-' ou 'new' ou 'character val'ou 'begin' ou 'if' ou 'for' ou 'while' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }

    public void instructionPlus1(TreeNode pere) throws ExceptionSyntaxique {
        System.out.print("instructionPlus1 : ");
        this.rulesName.add("instructionPlus1");
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
            System.out.println("r93");
            instructionPlus(pere);
        } else {
            System.out.println("r94");
            return;
        }
    }

    public void declarationPlus(TreeNode pere) throws ExceptionSyntaxique {
        System.out.print("declarationPlus : ");
        this.rulesName.add("declarationPlus");
        if (tokenTag() == "type" || tokenTag() == "procedure" || tokenTag() == "function" || tokenTag() == "VARIABLE") {
            System.out.println("r95");
            declaration(pere);
            declarationPlus1(pere);
        } else {
            printErrorAndSkip("'type', 'procedure', 'function' ou identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }

    public void declarationPlus1(TreeNode pere) throws ExceptionSyntaxique {
        System.out.print("declarationPlus1 : ");
        this.rulesName.add("declarationPlus1");
        if (tokenTag() == "type" || tokenTag() == "procedure" || tokenTag() == "function" || tokenTag() == "VARIABLE") {
            System.out.println("r96");
            declarationPlus(pere);
        } else {
            System.out.println("r97");
            return;
        }
    }


    public void champsPlus(TreeNode pere) throws ExceptionSyntaxique {
        System.out.print("champsPlus : ");
        this.rulesName.add("champsPlus");
        if (tokenTag() == "VARIABLE"){
            System.out.println("r98");
            champs(pere);
            champsPlus1(pere);
        }
        else{
            printErrorAndSkip("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }

    public void champsPlus1(TreeNode pere) throws ExceptionSyntaxique {
        System.out.print("champsPlus1 : ");
        this.rulesName.add("champsPlus1");
        if (tokenTag() == "VARIABLE"){
            System.out.println("r99");
            champsPlus(pere);
        }
        else{
            System.out.println("r100");
            return;
        }
    }



    public void identificateurVirgulePlus(TreeNode pere) throws ExceptionSyntaxique {
        System.out.print("identificateurVirgulePlus : ");
        this.rulesName.add("identificateurVirgulePlus");
        if (tokenTag() == "VARIABLE") {
            System.out.println("r101");
            // On ajoute un noeud.
            pere.addSon(new TreeNode(((VarToken) getNextToken()).getValue()));
            //On ajoute une entrée dans la table des symboles
            addVariableToSymbolTable(pere);
            //On avance le curseur
            advanceToken();
            identificateurVirgulePlus1(pere);
            return;
        } else {
            printErrorAndSkip("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }

    public void identificateurVirgulePlus1(TreeNode pere) throws ExceptionSyntaxique {
        System.out.print("identificateurVirgulePlus1 : ");
        this.rulesName.add("identificateurVirgulePlus1");
        if (tokenTag() == ",") {
            System.out.println("r102");
            advanceToken();
            if (tokenTag() == "VARIABLE") {
                identificateurVirgulePlus(pere);
            } else {
                printErrorAndSkip("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
            }
        } else{
            System.out.println("r103");
            return;
        }
    }

    public void paramPointVirgulePlus(TreeNode pere) throws ExceptionSyntaxique {
        System.out.print("paramPointVirgulePlus : ");
        this.rulesName.add("paramPointVirgulePlus");
        if (tokenTag() == "VARIABLE") {
            System.out.println("r104");
            param(pere);
            paramPointVirgulePlus1(pere);
        } else {
            printErrorAndSkip("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }

    public void paramPointVirgulePlus1(TreeNode pere) throws ExceptionSyntaxique {
        System.out.print("paramPointVirgulePlus1 : ");
        this.rulesName.add("paramPointVirgulePlus1");
        if (tokenTag() == ";") {
            System.out.println("r105");
            advanceToken();
            paramPointVirgulePlus(pere);
        } else {
            System.out.println("r106");
            return;
        }
    }

    public void expressionPlusVirgule(TreeNode pere) throws ExceptionSyntaxique {
        System.out.print("expressionPlusVirgule : ");
        this.rulesName.add("expressionPlusVirgule");
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
            System.out.println("r107");
            expression(pere);
            expressionPlusVirgule1(pere);
        }
        else{
            printErrorAndSkip("identificateur ou entier ou chaine ou 'true' ou 'false' ou 'null' ou '(' ou 'not' ou '-' ou 'new' ou 'character val'attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine());
        }
    }

    public void expressionPlusVirgule1(TreeNode pere) throws ExceptionSyntaxique {
        System.out.print("expressionPlusVirgule1 : ");
        this.rulesName.add("expressionPlusVirgule1");
        if (tokenTag() == ","){
            System.out.println("r108");
            advanceToken();
            expressionPlusVirgule(pere);
        }
        else{
            System.out.println("r109");
            return;
        }
    }


    public void elsifPlus(TreeNode pere) throws ExceptionSyntaxique {
        System.out.print("elsifPlus : ");
        this.rulesName.add("elsifPlus");
        if (tokenTag() == "elsif"){
            // On ajoute un noeud.
            TreeNode elseIfNode = new TreeNode(tokenTag());
            pere.addSon(elseIfNode);

            System.out.println("r110");
            advanceToken();
            expression(elseIfNode);
            if (tokenTag() == "then"){
                advanceToken();
                instructionPlus(elseIfNode);
                elsifPlus1(pere);
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
        System.out.print("elsifPlus1 : ");
        this.rulesName.add("elsifPlus1");
        if (tokenTag() == "elsif"){
            System.out.println("r111");
            elsifPlus(pere);
        }
        else{
            System.out.println("r112");
            return;
        }
    }
}
