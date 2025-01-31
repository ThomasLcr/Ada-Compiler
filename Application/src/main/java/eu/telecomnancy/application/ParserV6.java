package eu.telecomnancy.application;

import java.util.ArrayList;
import java.util.List;

import eu.telecomnancy.application.token.CharacterToken;
import eu.telecomnancy.application.token.FloatToken;
import eu.telecomnancy.application.token.IntegerToken;
import eu.telecomnancy.application.token.StringToken;
import eu.telecomnancy.application.token.Token;
import eu.telecomnancy.application.token.VarToken;
import eu.telecomnancy.application.exception.ExceptionLexicale;
import eu.telecomnancy.application.exception.ExceptionSemantique;
import eu.telecomnancy.application.exception.ExceptionSyntaxique;
import eu.telecomnancy.application.tds.TDS;
import eu.telecomnancy.application.tds.TDSItemFctProc;
import eu.telecomnancy.application.tds.TDSItemVar;
import java.util.stream.Collectors;
import java.util.Comparator;


public class ParserV6
{
    private ArrayList<Token> tokens;
    private int currentTokenIndex = 0;

    private List<String> rulesName;
    private TDS tableDesSymboles;
    private ArrayList<String> errors = new ArrayList<String>();
    private int limiteOfRecurrence = 5000;
    private int nbOfRecurrence = 0;


    public ParserV6(ArrayList<Token> tokens)
    {
        System.out.println("Initialisation du parser");
        System.out.println("Nombre de tokens: " + tokens.size());
        this.tokens = tokens;
        this.currentTokenIndex = 0;

        this.rulesName = new ArrayList<>();
        this.tableDesSymboles = new TDS();
    }

    public boolean hasErrors() {
        return errors.size() > 0;
    }
    public TDS getTableDesSymboles(){return this.tableDesSymboles;}

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

    public String tokenTag() throws ExceptionSyntaxique, ExceptionSemantique //Fonction permettant de récupérer le tag du token suivanat
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


    public void DisplayErrors() //Fonction permettant d'afficher les erreurs
    {
        if(errors.size() > 0){
        System.out.println("\u001B[31m" + "====== Errors/Warnings ======" + "\u001B[0m");
        for (String error : errors)
        {
            System.out.println(error);
        }
        System.out.println("\u001B[31m" + "=============================" + "\u001B[0m");
    }
    }


    private void printErrorAndSkip(String errorMessage, String type) throws ExceptionSyntaxique, ExceptionSemantique {
        if (type.equals("syntaxique"))
        {
            errors.add("\033[0;33m" + "Erreur Syntaxique: " + "\033[0m" + errorMessage);
            System.out.println("\u001B[31m" + "Erreur Syntaxique: " + "\u001B[0m" + errorMessage);
            skipErrors();
        }
        else if (type.equals("semantique"))
        {
            errors.add("\033[0;33m" + "Erreur Sémantique: " + "\033[0m" + errorMessage);
            System.out.println("\u001B[31m" + "Erreur Sémantique: " + "\u001B[0m" + errorMessage);
        }
    }

    private void skipErrors() throws ExceptionSyntaxique, ExceptionSemantique {
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

    //_______________Méthodes pour les controles sémantiques_______________//
    public TDSItemVar findVariableDeclaration(String identif) throws ExceptionSyntaxique, ExceptionSemantique {
        TDSItemVar result = null;
        TDS loop = this.tableDesSymboles;
        while(loop.getParent() != null){
            if (loop.getVariables().containsKey(identif)){
                result = loop.getVariables().get(identif);
                break;
            }
            loop = loop.getParent();
        }
        return result;
    }

    public TDSItemFctProc findFunctionDeclaration(String identif) throws ExceptionSyntaxique, ExceptionSemantique {
        TDSItemFctProc result = null;
        TDS loop = this.tableDesSymboles;
        while(loop.getParent() != null){
            if (loop.getFonctions().containsKey(identif)){
                result = loop.getFonctions().get(identif);
                break;
            }
            loop = loop.getParent();
        }
        return result;
    }

    public boolean isTokeninFunction(int index) throws ExceptionSyntaxique, ExceptionSemantique {
        boolean result = false;
        while (this.tokens.get(index).getTag() == ";"){
            if (this.tokens.get(index).getTag() != "(" && this.tokens.get(index-1).getTag() != "VARIABLE"){
                result = true;
                break;
            }
        }
        return result;
    }

    public boolean compareTypes(TreeNode node, String type) throws ExceptionSemantique, ExceptionSyntaxique {
        if (node.getSons().size() == 0){
            if (!type.equalsIgnoreCase("")){
                return this.findVariableDeclaration(node.getLabel()).getType().equalsIgnoreCase(type);
            }else{
                return false;
            }
        }
        TreeNode left = node.getFirstSon();
        TreeNode right = node.getLastSon();
        if (left.getSons().size() == 0 && right.getSons().size() == 0){
            String left_l = left.getLabel();
            String right_l = right.getLabel();
            Boolean left_var = true;
            Boolean right_var = true;
            if (this.findVariableDeclaration(right_l) == null){
                right_var = false;
            }
            if (this.findVariableDeclaration(left_l) == null){
                left_var = false;
            }
            if (left_var && right_var){
                String type1 = this.findVariableDeclaration(left_l).getType();
                String type2 = this.findVariableDeclaration(right_l).getType();
                return type1.equalsIgnoreCase(type2);
            }else{
                try {
                    if (left_var){
                        Integer.parseInt(right_l);
                        return this.findVariableDeclaration(left_l).getType().equalsIgnoreCase("integer");
                    } else if (right_var) {
                        Integer.parseInt(left_l);
                        return this.findVariableDeclaration(right_l).getType().equalsIgnoreCase("integer");
                    }else {
                        Integer.parseInt(right_l);
                        Integer.parseInt(left_l);
                        return true;
                    }
                }catch (NumberFormatException e1){
                    try {
                        if (left_var){
                            Float.parseFloat(right_l);
                            return this.findVariableDeclaration(left_l).getType().equalsIgnoreCase("float");
                        } else if (right_var) {
                            Float.parseFloat(left_l);
                            return this.findVariableDeclaration(right_l).getType().equalsIgnoreCase("float");
                        }else {
                            Float.parseFloat(right_l);
                            Float.parseFloat(left_l);
                            return true;
                        }
                    }catch (NumberFormatException e2){
                        if (left_l.length() == 3){
                            if (right_l.length() == 3){
                                return true;
                            }else{
                                return false;
                            }
                        }
                        else if (left_l.equalsIgnoreCase("true") || left_l.equalsIgnoreCase("false")) {
                            if (right_l.equalsIgnoreCase("true") || right_l.equalsIgnoreCase("false")) {
                                return true;
                            } else {
                                return false;
                            }
                        }
                        else{
                            return false;
                        }
                    }
                }
            }
        }
        else if (left.getSons().size() == 0){
            String type1;
            if (this.findVariableDeclaration(left.getLabel()) == null){
                String label = left.getLabel();
                try{
                    Integer.parseInt(label);
                    type1 = "integer";
                }catch (NumberFormatException e1){
                    try{
                        Float.parseFloat(label);
                        type1 = "float";
                    }catch (NumberFormatException e2){
                        if (label.length() == 3){
                            type1 = "character";
                        } else if (label.equalsIgnoreCase("true") || label.equalsIgnoreCase("false")) {
                            type1 = "boolean";
                        } else {
                            type1 = "string";
                        }
                    }
                }
            }else{
                type1 = this.findVariableDeclaration(left.getLabel()).getType();
            }
            return compareTypes(right, type1);
        }
        else if (right.getSons().size() == 0){
            String type1;
            if (this.findVariableDeclaration(right.getLabel()) == null){
                String label = right.getLabel();
                try{
                    Integer.parseInt(label);
                    type1 = "integer";
                }catch (NumberFormatException e1){
                    try{
                        Float.parseFloat(label);
                        type1 = "float";
                    }catch (NumberFormatException e2){
                        if (label.length() == 3){
                            type1 = "character";
                        } else if (label.equalsIgnoreCase("true") || label.equalsIgnoreCase("false")) {
                            type1 = "boolean";
                        } else {
                            type1 = "string";
                        }
                    }
                }
            }else{
                type1 = this.findVariableDeclaration(right.getLabel()).getType();
            }
            return compareTypes(left, type1);
        }
        else{
            return compareTypes(left, "") && compareTypes(right, "");
        }
    }

    public boolean isBoolean(TreeNode node) throws ExceptionSemantique, ExceptionSyntaxique {
        if (node.getLabel().equalsIgnoreCase("or") ||
                node.getLabel().equalsIgnoreCase("or_else") ||
                node.getLabel().equalsIgnoreCase("and") ||
                node.getLabel().equalsIgnoreCase("and_then")) {
            return isBoolean(node.getFirstSon()) && isBoolean(node.getLastSon());
        }
        if (node.getLabel().equalsIgnoreCase("=") ||
                node.getLabel().equalsIgnoreCase("/=") ||
                node.getLabel().equalsIgnoreCase(">") ||
                node.getLabel().equalsIgnoreCase(">=") ||
                node.getLabel().equalsIgnoreCase("<") ||
                node.getLabel().equalsIgnoreCase("<=")) {
            if (isBoolean(node.getFirstSon()) && isBoolean(node.getLastSon())) {
                return true;
            } else {
                if (compareTypes(node, "")) {
                    return true;
                }
            }
        } else {
            return false;
        }
        return false;
    }

    public void compatibleTypes(TDSItemVar TDSvar_membre_gauche, TreeNode right) throws ExceptionSemantique, ExceptionSyntaxique {
        //On vérifie que les types sont compatibles
        if (TDSvar_membre_gauche.getType().equalsIgnoreCase("boolean")){
            if (right.getLabel().equalsIgnoreCase("or") ||
                    right.getLabel().equalsIgnoreCase("or_else") ||
                    right.getLabel().equalsIgnoreCase("and") ||
                    right.getLabel().equalsIgnoreCase("and_then")){
                if (!(isBoolean(right.getFirstSon()) && isBoolean(right.getLastSon()))){
                    printErrorAndSkip("Vous ne comparez pas 2 booléen. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "semantique");
                }
            }
            else {
                if (right.getLabel().equalsIgnoreCase("=") ||
                        right.getLabel().equalsIgnoreCase("/=") ||
                        right.getLabel().equalsIgnoreCase(">") ||
                        right.getLabel().equalsIgnoreCase(">=")||
                        right.getLabel().equalsIgnoreCase("<") ||
                        right.getLabel().equalsIgnoreCase("<=")){
                    if (!isBoolean(right)){
                        printErrorAndSkip("Vous ne comparez pas 2 éléments du même type. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "semantique");
                    }
                }
                else {
                    printErrorAndSkip("La variable "+TDSvar_membre_gauche.getIdentif()+ " n'est pas un booléen. Dans le fichier source, ligne : " + this.tokens.get(currentTokenIndex).getLine(), "semantique");
                }
            }
        }
        else{
            if(this.tokens.get(currentTokenIndex) instanceof FloatToken ){
                if (!TDSvar_membre_gauche.getType().toLowerCase().equals("float")){
                    printErrorAndSkip("Vous avez utilisé un float alors que le membre gauche de l'expression est de type "+ TDSvar_membre_gauche.getType().toLowerCase() + ". Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "semantique");
                }
            }
            else{
                if (!TDSvar_membre_gauche.getType().toLowerCase().equals("integer")){
                    printErrorAndSkip("Vous avez utilisé un integer alors que le membre gauche de l'expression est de type "+ TDSvar_membre_gauche.getType().toLowerCase() + ". Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "semantique");
                }
            }
        }
    }


    //_______________Méthodes de l'analyseur syntaxique_______________//

    public TreeNode simplifyCalculation(TreeNode node){
        if (node.getSons().size() == 0){
            return node;
        }
        else if (node.getSons().size() == 1) {
            if (node.getLabel().equalsIgnoreCase("-")){
                TreeNode son = simplifyCalculation(node.getLastSon());
                return new TreeNode("-"+son.getLabel());
            }
        }
        else{
            try {
                int left = Integer.parseInt(simplifyCalculation(node.getFirstSon()).getLabel());
                int right = Integer.parseInt(simplifyCalculation(node.getLastSon()).getLabel());
                String calculation;
                TreeNode result = node;
                switch (node.getLabel()) {
                    case "+":
                        calculation = String.valueOf(left + right);
                        result = new TreeNode(calculation);
                        break;
                    case "-":
                        calculation = String.valueOf(left - right);
                        result = new TreeNode(calculation);
                        break;
                    case "*":
                        calculation = String.valueOf(left * right);
                        result = new TreeNode(calculation);
                        break;
                    case "/":
                        calculation = String.valueOf(left / right);
                        result = new TreeNode(calculation);
                        break;
                    case "rem":
                        calculation = String.valueOf(left % right);
                        result = new TreeNode(calculation);
                        break;
                }
                return result;
            } catch (NumberFormatException e) {
                return node;
            }
        }
        return node;
    }

    public TreeNode parse() throws ExceptionSyntaxique, ExceptionSemantique
    {
        // On crée le noeud racine de l'arbre syntaxique.
        TreeNode root = new TreeNode("ROOT");

        //On démarre par l'analyse de l'axiome
        fichier(root);

        return root;
    }

    public void fichier(TreeNode root) throws ExceptionSyntaxique, ExceptionSemantique {

        System.out.println("fichier");
        //Pour le fichier, on vérifie que le premier token est bien un "with"
        if (tokenTag() == "with") {

            advanceToken();
        } else {
            printErrorAndSkip("'with' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
        }

        //On vérifie que le token suivant est bien une librairie
        if (tokenTag() == "LIB") {
            // On ajoute un noeud.
            root.addSon(new TreeNode("DECL_LIB"));

            advanceToken();
        } else {
            printErrorAndSkip("'Librairie' attendue", "syntaxique");
        }

        //On vérifie que le token suivant est bien un ";"
        if (tokenTag() == ";") {

            advanceToken();
        } else {
            printErrorAndSkip("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
        }

        //On vérifie que le token suivant est bien un "use"
        if (tokenTag() == "use") {

            advanceToken();
        } else {
            printErrorAndSkip("'use' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
        }

        //On vérifie que le token suivant est bien une librairie
        if (tokenTag() == "LIB") {

            advanceToken();
        } else {
            printErrorAndSkip("'Librairie' attendue", "syntaxique");
        }

        //On vérifie que le token suivant est bien un ";"
        if (tokenTag() == ";") {

            advanceToken();
        } else {
            printErrorAndSkip("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
        }

        //On vérifie que le token suivant est bien un début de procedure
        if (tokenTag() == "procedure") {

            advanceToken();
        } else {
            printErrorAndSkip("'procedure' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
        }

        //On vérifie que le token suivant est bien l'identificateur de la procedure
        if (tokenTag() == "VARIABLE") {
            // On ajoute un noeud.
            root.addSon(new TreeNode(((VarToken) getNextToken()).getValue()));

            // Ajout procedure à la TDS
            TDS tableFille = new TDS();
            tableFille.setParent(tableDesSymboles);
            tableDesSymboles.addFunctionOrProcedure(root.getSons().get(1).getLabel(), 0, tableFille, null);
            tableDesSymboles = tableFille;

            advanceToken();
        } else {
            printErrorAndSkip("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
        }

        //On vérifie que le token suivant est bien un "is" et on passe dans la règle fichier2
        if (tokenTag() == "is") {

            advanceToken();
            fichier2(root);
        } else {
            printErrorAndSkip("'is' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
        }
    }

    public void fichier2(TreeNode pere) throws ExceptionSyntaxique, ExceptionSemantique {


        System.out.print("fichier2 : ");
        this.rulesName.add("fichier2");
        // Si le token suivant est un "begin", on est bien dans la première réduction de la règle fichier2 et on passe dans la règle instructionPlus
        if (tokenTag() == "begin") {

            System.out.println("r2");
            advanceToken();
            instructionPlus(pere);
            if (tokenTag() == "end") {

                // Fin de la procedure principale, on remonte la TDS
                tableDesSymboles = tableDesSymboles.getParent();
                

                advanceToken();
                fichier3(pere);
                return;
            } else {
                printErrorAndSkip("'end' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
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


                    // Fin de procedure ou fonction, on remonte la TDS
                    tableDesSymboles = tableDesSymboles.getParent();

                    advanceToken();
                    fichier3(pere);
                    return;
                } else {
                    printErrorAndSkip("'end' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
                }
            } else {
                printErrorAndSkip("'begin' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
            }
        } else {
            printErrorAndSkip("'begin', 'type', 'procedure', 'function' ou identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
        }
    }

    public void fichier3(TreeNode pere) throws ExceptionSyntaxique, ExceptionSemantique {

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
            //On vérifie que la valeur du token correspond à la valeur de l'identificateur de la procedure
            String identif_fin = ((VarToken) this.tokens.get(currentTokenIndex)).getValue();
            String identif_debut = tableDesSymboles.getFonctions().keySet().iterator().next();
            if(!identif_fin.equals(identif_debut)){
                printErrorAndSkip("L'idendificateur de fin de procédure principale ne correspond pas à celui attendu "+ identif_debut +" != "+ identif_fin + ". Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "semantique"); 
            }
            advanceToken();
            if (tokenTag() == ";") {

                advanceToken();
            } else {
                printErrorAndSkip("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
            }
            
            
        } else {
            printErrorAndSkip("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
        }
    }


    public void declaration(TreeNode pere) throws ExceptionSyntaxique, ExceptionSemantique {

        System.out.print("declaration : ");
        this.rulesName.add("declaration");
        if (tokenTag() == "type") {
            System.out.println("r6");
            // On ajoute un noeud.
            TreeNode noeud = new TreeNode(tokenTag());
            pere.addSon(noeud);
            advanceToken();
            if (tokenTag() == "VARIABLE") {
                // On ajoute une structure à la TDS
                tableDesSymboles.addItemStruct(((VarToken) getNextToken()).getValue(),0);
                // On ajoute un noeud.
                noeud.addSon(new TreeNode(((VarToken) getNextToken()).getValue()));
                advanceToken();
                declaration11(noeud);
                return;
            } else {
                printErrorAndSkip("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
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
                printErrorAndSkip("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
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
                printErrorAndSkip("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
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
                    

                    // Ajout variable à la TDS
                    TDS tableFille = new TDS();
                    tableFille.setParent(tableDesSymboles);
                    //System.out.println("Ajout de la variable : " + ((VarToken) this.tokens.get(currentTokenIndex-2)).getValue());
                    if (this.tableDesSymboles.getVariables().containsKey(((VarToken) this.tokens.get(currentTokenIndex-2)).getValue())){
                        printErrorAndSkip("La variable " + ((VarToken) this.tokens.get(currentTokenIndex-2)).getValue() + " est déjà déclarée dans la portée actuelle. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "semantique"); 
                    }else {
                        tableDesSymboles.addVariable(((VarToken)this.tokens.get(currentTokenIndex-2)).getValue(), ((VarToken) this.tokens.get(currentTokenIndex)).getValue(), 1, null, null);
                    }
                    int i=3;
                    // On ajoute les variables suivantes en cas de déclaration multiple
                    while (this.tokens.get(currentTokenIndex-i).getTag() == ","){
                        // Ajout variable à la TDS
                        //System.out.println("Ajout de la variable : " + ((VarToken) this.tokens.get(currentTokenIndex-i-1)).getValue());
                        if (this.tableDesSymboles.getVariables().containsKey(((VarToken) this.tokens.get(currentTokenIndex-i-1)).getValue())){
                            printErrorAndSkip("La variable " + ((VarToken) this.tokens.get(currentTokenIndex-i-1)).getValue() + " est déjà déclarée dans la portée actuelle. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "semantique"); 
                        }else {
                            tableDesSymboles.addVariable(((VarToken)this.tokens.get(currentTokenIndex-i-1)).getValue(), ((VarToken) this.tokens.get(currentTokenIndex)).getValue(), 1, null, null);
                        }
                        i+=2;
                    }
                    advanceToken();
                } else{
                    printErrorAndSkip("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
                }

                declaration41(noeud);
            } else {
                printErrorAndSkip("':' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
            }
        } else {
            printErrorAndSkip("'type', 'procedure', 'function' ou identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
        }
    }

    public void declaration11(TreeNode pere) throws ExceptionSyntaxique, ExceptionSemantique {

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
                    printErrorAndSkip("'end' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
                }
                if (tokenTag() == "record"){

                    advanceToken();
                } else{
                    printErrorAndSkip("'record' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
                }
                if (tokenTag() == ";"){

                    advanceToken();
                    return;
                } else{
                    printErrorAndSkip("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
                }
            } else {
                printErrorAndSkip("'record' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
            }
        } else {
            printErrorAndSkip("'is' ou ';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
        }
    }

    public void declaration21(TreeNode pere) throws ExceptionSyntaxique, ExceptionSemantique {

        System.out.print("declaration21 : ");
        this.rulesName.add("declaration21");
        if (tokenTag() == "is"){

            System.out.println("r12");
            advanceToken();

            // Ajout procedure à la TDS
            TDS tableFille = new TDS();
            tableFille.setParent(tableDesSymboles);
            tableDesSymboles.addFunctionOrProcedure(pere.getSons().get(0).getLabel(), 0, tableFille,null);
            tableDesSymboles = tableFille;

            declaration22(pere);
            return;
        }
        else if(tokenTag() == "("){

            System.out.println("r13");
            params(pere);
            if (tokenTag() == "is"){

                advanceToken();

                // Ajout procedure à la TDS

                TDS tableFille = new TDS();
                tableFille.setParent(tableDesSymboles);

                for(TreeNode son : pere.getSons().get(1).getSons()){
                    for (TreeNode s2 : son.getSons()){
                        tableFille.addVariable(s2.getLabel(), son.getLabel(),1, pere.getSons().get(0).getLabel(), null); //TODO
                    }
                }

                tableDesSymboles.addFunctionOrProcedure(pere.getSons().get(0).getLabel(), pere.getSons().get(1).getNbSons(), tableFille, null);


                tableDesSymboles = tableFille;

                declaration22(pere);
            }
            else{
                printErrorAndSkip("'is' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
            }
        }
        else{
            printErrorAndSkip("'is' ou '(' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
        }
    }

    public void declaration22(TreeNode pere) throws ExceptionSyntaxique, ExceptionSemantique {

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
                    printErrorAndSkip("'end' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
                }
            } else {
                printErrorAndSkip("identificateur ou entier ou chaine ou 'true' ou 'false' ou 'null' ou '(' ou 'not' ou '-' ou 'character val'ou 'begin' ou 'if' ou 'for' ou 'while' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
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
                    printErrorAndSkip("'end' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
                }
            } else {
                printErrorAndSkip("'begin' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
            }
        } else {
            printErrorAndSkip("'begin', 'type', 'procedure', 'function' ou identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
        }
    }

    public void declaration23(TreeNode pere) throws ExceptionSyntaxique, ExceptionSemantique {
        ///A voir où placer la remonter de TDS en cas d'erreurs syntaxiques
        System.out.print("declaration23 : ");
        this.rulesName.add("declaration23");
        if (tokenTag() == ";") {

            System.out.println("r16");
            advanceToken();
            // Fin de procedure ou fonction, on remonte la TDS
            tableDesSymboles = tableDesSymboles.getParent();

            return;
        } else if (tokenTag() == "VARIABLE") {
            System.out.println("r17");
            //On vérifie que la valeur du token correspond à la valeur de l'identificateur de la procedure
            String identif_fin = ((VarToken) this.tokens.get(currentTokenIndex)).getValue();
            //On parcourt toute la HashMap de TDSFonction et on regarde si un des éléments possède comme table fille la table actuelle
            TDS tableParent = tableDesSymboles.getParent();
            String identif_debut = "";
            for (String key : tableParent.getFonctions().keySet()){
                if (tableParent.getFonctions().get(key).tableFille.getId() == tableDesSymboles.getId()){
                    identif_debut = key;
                }
            }
            if(!identif_fin.equals(identif_debut)){
                printErrorAndSkip("L'idendificateur de fin de fonction/procedure ne correspond pas à celui attendu "+ identif_debut +" != "+ identif_fin + ". Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "semantique"); 
            }

            advanceToken();
            if (tokenTag() == ";") {
                advanceToken();
                // Fin de procedure ou fonction, on remonte la TDS
                tableDesSymboles = tableDesSymboles.getParent();

            } else {
                printErrorAndSkip("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
            }
        } else {
            printErrorAndSkip("';' ou identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
        }
    }

    public void declaration31(TreeNode pere) throws ExceptionSyntaxique, ExceptionSemantique {

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
                printErrorAndSkip("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
            }
            if (tokenTag() == "is") {

                advanceToken();

                // Ajout fonction à la TDS

                TDS tableFille = new TDS();
                tableFille.setParent(tableDesSymboles);
                tableDesSymboles.addFunctionOrProcedure(pere.getSons().get(0).getLabel(), 0, tableFille, ((VarToken) this.tokens.get(currentTokenIndex-2)).getValue());
                tableDesSymboles = tableFille;

                declaration22(pere);
                //Ici on a contruit tout le corps de la fonction, on vérifie que la fonction retourne bien une valeur
                int index = this.currentTokenIndex;
                int nbreturn = 0;
                while (this.tokens.get(index).getTag() != "function"){
                    if (this.tokens.get(index).getTag() == "return"){
                        nbreturn++;
                    }
                    index--;
                }
                if (nbreturn == 1){
                    printErrorAndSkip("La fonction ne retourne pas de valeur. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "semantique");
                }
            } else {
                printErrorAndSkip("'is' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
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
                    printErrorAndSkip("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
                }
                if (tokenTag() == "is") {

                    advanceToken();

                    // Ajout fonction à la TDS
 
                    TDS tableFille = new TDS();
                    tableFille.setParent(tableDesSymboles);

                    for(TreeNode son : pere.getSons().get(1).getSons()){
                        for (TreeNode s2 : son.getSons()){
                            tableFille.addVariable(s2.getLabel(), son.getLabel(), 1, pere.getSons().get(0).getLabel(), null); //TODO
                        }
                    }

                    tableDesSymboles.addFunctionOrProcedure(pere.getSons().get(0).getLabel(), pere.getSons().get(1).getNbSons(), tableFille, ((VarToken) this.tokens.get(currentTokenIndex-2)).getValue());
                    tableDesSymboles = tableFille;

                    declaration22(pere);
                    //Ici on a contruit tout le corps de la fonction, on vérifie que la fonction retourne bien une valeur
                    int index = this.currentTokenIndex;
                    int nbreturn = 0;
                    while (this.tokens.get(index).getTag() != "function"){
                        if (this.tokens.get(index).getTag() == "return"){
                            nbreturn++;
                        }
                        index--;
                    }
                    if (nbreturn == 1){
                        printErrorAndSkip("La fonction ne retourne pas de valeur. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "semantique");
                    }
                } else {
                    printErrorAndSkip("'is' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
                }
            } else {
                printErrorAndSkip("'return' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
            }
        } else {
            printErrorAndSkip("'return' ou '(' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
        }
    }

    public void declaration41(TreeNode pere) throws ExceptionSyntaxique, ExceptionSemantique {

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
                    printErrorAndSkip("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
                }
            } else {
                printErrorAndSkip("'=' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
            }
        } else {
            printErrorAndSkip("':' ou ';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
        }
    }

    public void champs(TreeNode pere) throws ExceptionSyntaxique, ExceptionSemantique {
        System.out.print("champs : ");

        // On crée un nouveau noeud.
        TreeNode noeud = new TreeNode("champs");
        // On l'ajoute à l'arbre.
        pere.addSon(noeud);
        this.rulesName.add("champ");

        if (tokenTag() == "VARIABLE"){
            System.out.println("r22");
            identificateurVirgulePlus(noeud);
            if (tokenTag() == ":"){

                advanceToken();
            } else{
                printErrorAndSkip("':' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");

            }
            // A vérifier pour l'affichage de l'arbre ici
            if (tokenTag() == "VARIABLE"){
                //Ici on met à jour les champs de la structure dans la TDS
                String attributType = ((VarToken) this.tokens.get(currentTokenIndex)).getValue();
                String attributName = ((VarToken) this.tokens.get(currentTokenIndex-2)).getValue();
                int index = currentTokenIndex;
                while (this.tokens.get(index).getTag() != "type"){
                    index--;
                }
                String structName = ((VarToken) this.tokens.get(index+1)).getValue();
                tableDesSymboles.addVariable(attributName, attributType, 1, null, structName);
                // On incrémente de 1 le nombre de champs de la structure
                tableDesSymboles.getStructures().get(structName).addAttribut();
                // On ajoute un noeud.
                noeud.addSon(new TreeNode(((VarToken) getNextToken()).getValue()));
                advanceToken();

            } else{
                printErrorAndSkip("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
            }
            if (tokenTag() == ";"){

                advanceToken();
                return;
            } else{
                printErrorAndSkip("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
            }
        }
        else{
            printErrorAndSkip("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
        }
    }

    public void params(TreeNode pere) throws ExceptionSyntaxique, ExceptionSemantique {
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
                printErrorAndSkip("')' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
            }
        } else {
            printErrorAndSkip("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
        }
    }

    public void param(TreeNode pere) throws ExceptionSyntaxique, ExceptionSemantique {

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
                printErrorAndSkip("':' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
            }
            if (tokenTag() == "VARIABLE" || tokenTag() == "in") {
                param2(noeud2);
                noeud.setLabel(noeud2.getLastSon().getLabel());
                for (int i=noeud2.getSons().size()-2; i>= 0; i--){
                    noeud.addSon(noeud2.getSons().get(i));
                }
                pere.addSon(noeud);

            } else {
                printErrorAndSkip("identificateur ou 'in' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
            }
        }else {
            printErrorAndSkip("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
        }
    }


    public void param2(TreeNode pere) throws ExceptionSyntaxique, ExceptionSemantique {

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
            if (tokenTag() == "VARIABLE") {
                pere.addSon(new TreeNode(((VarToken) getNextToken()).getValue()));
                advanceToken();
                return;
            } else {
                printErrorAndSkip("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
            }
        } else {
            printErrorAndSkip("identificateur ou 'in' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
        }
    }

    public void mode(TreeNode pere) throws ExceptionSyntaxique, ExceptionSemantique {

        System.out.print("mode : ");
        this.rulesName.add("mode");
        if (tokenTag() == "in") {
            // On ajoute un noeud.
            pere.addSon(new TreeNode(tokenTag()));

            System.out.println("r27");
            advanceToken();
            mode1(pere);
        } else {
            printErrorAndSkip("'in' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
        }
    }

    public void mode1(TreeNode pere) throws ExceptionSyntaxique, ExceptionSemantique {

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


    public void expression(TreeNode pere) throws ExceptionSyntaxique, ExceptionSemantique {

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
            TreeNode noeud = new TreeNode("N1");
            N1(noeud);
            disjonction(pere);
            if(pere.getSons().size() != 0 && pere.getLabel()!="ARGUMENT"){
                pere.getLastSon().addFirstSon((noeud.getLastSon()));
            }
            else if(pere.getLabel()=="ARGUMENT"){
                pere.addSon((noeud.getLastSon()));
            }
            else{
                 pere.addSon(noeud.getLastSon());
            }
        } else {
            printErrorAndSkip("identificateur ou entier ou chaine ou 'true' ou 'false' ou 'null' ou '(' ou 'not' ou '-' ou 'character val'attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
        }
    }

    public void disjonction(TreeNode pere) throws ExceptionSyntaxique, ExceptionSemantique {
        System.out.print("disjonction : ");
        this.rulesName.add("disjonction");
        if (tokenTag() == "or") {
            // On ajoute un noeud.
            TreeNode noeud = new TreeNode(tokenTag());
            pere.addSon(noeud);

            System.out.println("r31");
            advanceToken();
            expression(noeud);
            return;
        } else if (tokenTag() == "or_else") {
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
    public void N1(TreeNode pere) throws ExceptionSyntaxique, ExceptionSemantique {

        System.out.print("N1 : ");
        this.rulesName.add("N1");
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
            TreeNode noeud = new TreeNode("N1");
            N2(noeud);
            conjonction(pere);
            if(pere.getSons().size() != 0){
                pere.getLastSon().addFirstSon((noeud.getLastSon()));
            }
            else{
                pere.addSon(noeud.getLastSon());
            }  
        } else {
            printErrorAndSkip("identificateur ou entier ou chaine ou 'true' ou 'false' ou 'null' ou '(' ou 'not' ou '-' ou 'character val'attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
        }
    }

    public void conjonction(TreeNode pere) throws ExceptionSyntaxique, ExceptionSemantique {
        System.out.print("conjonction : ");
        this.rulesName.add("conjonction");
        if (tokenTag() == "and") {
            // On ajoute un noeud.
            TreeNode noeud = new TreeNode(tokenTag());
            pere.addSon(noeud);

            System.out.println("r35");
            advanceToken();
            N1(noeud);
            return;
        } else if (tokenTag() == "and_then") {
            // On ajoute un noeud.
            TreeNode noeud = new TreeNode(tokenTag());
            pere.addSon(noeud);

            System.out.println("r36");
            advanceToken();
            N1(noeud);
        } else {
            System.out.println("r37");
            return;
        }
    }

    public void N2(TreeNode pere) throws ExceptionSyntaxique, ExceptionSemantique {
        System.out.print("N2 : ");
        this.rulesName.add("N2");
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
            TreeNode noeud = new TreeNode("N2");
            N3(noeud);
            comparaison1(pere);
            if(pere.getSons().size() != 0){
                pere.getLastSon().addFirstSon((noeud.getLastSon()));
                //compareTypes(pere.getLastSon());
            }
            else{
                pere.addSon(noeud.getLastSon());
            }
        } else {
            printErrorAndSkip("identificateur ou entier ou chaine ou 'true' ou 'false' ou 'null' ou '(' ou 'not' ou '-' ou 'character val'attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
        }
    }

    public void comparaison1(TreeNode pere) throws ExceptionSyntaxique, ExceptionSemantique {
        System.out.print("comparaison1 : ");
        this.rulesName.add("comparaison1");
        if (tokenTag() == "=") {
            // On ajoute un noeud.
            TreeNode noeud = new TreeNode(tokenTag());
            pere.addSon(noeud);

            System.out.println("r39");
            advanceToken();
            N2(noeud);
            return;
        } else if (tokenTag() == "/=") {
            // On ajoute un noeud.
            TreeNode noeud = new TreeNode(tokenTag());
            pere.addSon(noeud);

            System.out.println("r40");
            advanceToken();
            N2(noeud);
            return;
        } else {
            System.out.println("r41");
            return;
        }
    }

    public void N3(TreeNode pere) throws ExceptionSyntaxique, ExceptionSemantique {
        System.out.print("N3 : ");
        this.rulesName.add("N3");
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
            System.out.println("r42");
            TreeNode noeud = new TreeNode("N3");
            N4(noeud);
            //Tant que l'on va avoir une opération de même type on va effectuer une rotation gauche
            TreeNode op1 = noeud.getLastSon();
            if (op1.getLabel() == "-" || op1.getLabel() == "+"){
                while (op1.getLabel() == "-" || op1.getLabel() == "+"){
                    if (op1.getLastSon().getLabel() == "-" || op1.getLastSon().getLabel() == "+"){
                        op1.leftRotation();
                    }
                    op1 = op1.getParent();
                    System.out.println("op1 "+op1.getLabel());
                }
            }
            comparaison2(pere);
            if(pere.getSons().size() != 0){
                pere.getLastSon().addFirstSon((noeud.getLastSon()));
                //compareTypes(pere.getLastSon());
            }
            else{
                pere.addSon(noeud.getLastSon());
            }
        } else {
            printErrorAndSkip("identificateur ou entier ou chaine ou 'true' ou 'false' ou 'null' ou '(' ou 'not' ou '-' ou 'character val'attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
        }
    }

    public void comparaison2(TreeNode pere) throws ExceptionSyntaxique, ExceptionSemantique {
        System.out.print("comparaison2 : ");
        this.rulesName.add("comparaison2");
        if (tokenTag() == "<") {
            // On ajoute un noeud.
            TreeNode noeud = new TreeNode(tokenTag());
            pere.addSon(noeud);

            System.out.println("r43");
            advanceToken();
            N3(noeud);
            return;
        } else if (tokenTag() == "<=") {
            // On ajoute un noeud.
            TreeNode noeud = new TreeNode(tokenTag());
            pere.addSon(noeud);

            System.out.println("r44");
            advanceToken();
            N3(noeud);
            return;
        } else if (tokenTag() == ">") {
            // On ajoute un noeud.
            TreeNode noeud = new TreeNode(tokenTag());
            pere.addSon(noeud);

            System.out.println("r45");
            advanceToken();
            N3(noeud);
            return;
        } else if (tokenTag() == ">=") {
            // On ajoute un noeud.
            TreeNode noeud = new TreeNode(tokenTag());
            pere.addSon(noeud);

            System.out.println("r46");
            advanceToken();
            N3(noeud);
            return;
        } else {
            System.out.println("r47");
            return;
        }
    }

    public void N4(TreeNode pere) throws ExceptionSyntaxique, ExceptionSemantique {
        System.out.print("N4 : ");
        this.rulesName.add("N4");
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
            System.out.println("r48");
            TreeNode noeud = new TreeNode("N4");
            N5(noeud);
            //Tant que l'on va avoir une opération de même type on va effectuer une rotation gauche
            TreeNode op1 = noeud.getLastSon();
            if (op1.getLabel() == "*" || op1.getLabel() == "/" || op1.getLabel() == "rem"){
                while (op1.getLabel() == "*" || op1.getLabel() == "/" || op1.getLabel() == "rem"){
                    if (op1.getLastSon().getLabel() == "*" || op1.getLastSon().getLabel() == "/" || op1.getLastSon().getLabel() == "rem"){
                        op1.leftRotation();
                    }
                    op1 = op1.getParent();
                    System.out.println("op1 "+op1.getLabel());
                }
            }
            operation1(pere);
            if(pere.getSons().size() != 0){
                pere.getLastSon().addFirstSon((noeud.getLastSon()));      
            }
            else{
                pere.addSon(noeud.getLastSon());
            }

        } else {
            printErrorAndSkip("identificateur ou entier ou chaine ou 'true' ou 'false' ou 'null' ou '(' ou 'not' ou '-' ou 'character val'attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
        }
    }

    public void operation1(TreeNode pere) throws ExceptionSyntaxique, ExceptionSemantique {
        System.out.print("operation1 : ");
        this.rulesName.add("operation1");
        if (tokenTag() == "+") {
            // On ajoute un noeud.
            TreeNode noeud = new TreeNode(tokenTag());
            pere.addSon(noeud);

            System.out.println("r49");
            advanceToken();
            N4(noeud);
            return;
        } else if (tokenTag() == "-") {
            // On ajoute un noeud.
            TreeNode noeud = new TreeNode(tokenTag());
            pere.addSon(noeud);

            System.out.println("r50");
            advanceToken();
            N4(noeud);
            return;
        } else {
            System.out.println("r51");
            return;
        }
    }

    public void N5(TreeNode pere) throws ExceptionSyntaxique, ExceptionSemantique {
        System.out.print("N5 : ");
        this.rulesName.add("N5");
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
            System.out.println("r52");
            TreeNode noeud = new TreeNode("N5");
            negation(noeud);
            operation2(pere);
            if(pere.getSons().size() != 0){
                pere.getLastSon().addFirstSon((noeud.getLastSon()));
            }
            else{
                pere.addSon(noeud.getLastSon());
            }
        } else {
            printErrorAndSkip("identificateur ou entier ou chaine ou 'true' ou 'false' ou 'null' ou '(' ou 'not' ou '-' ou 'character val'attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
        }
    }

    public void operation2(TreeNode pere) throws ExceptionSyntaxique, ExceptionSemantique {
        System.out.print("operation2 : ");
        this.rulesName.add("operation2");
        if (tokenTag() == "*") {
            // On ajoute un noeud.
            TreeNode noeud = new TreeNode(tokenTag());
            pere.addSon(noeud);

            System.out.println("r53");
            advanceToken();
            N5(noeud);
            return;
        } else if (tokenTag() == "/") {
            // On ajoute un noeud.
            TreeNode noeud = new TreeNode(tokenTag());
            TreeNode simplifiedNode = new TreeNode(tokenTag());

            System.out.println("r54");
            advanceToken();
            N5(noeud);
            TreeNode calculation = simplifyCalculation(noeud.getLastSon());
            simplifiedNode.addSon(calculation);
            pere.addSon(simplifiedNode);
            if (simplifiedNode.getLastSon().getLabel().equalsIgnoreCase("0") && noeud.getSons().size() == 1)
                printErrorAndSkip("Division par 0 à la ligne : "+this.tokens.get(currentTokenIndex).getLine(), "semantique");
            return;
        } else if (tokenTag() == "rem") {
            // On ajoute un noeud.
            TreeNode noeud = new TreeNode(tokenTag());
            pere.addSon(noeud);

            System.out.println("r55");
            advanceToken();
            N5(noeud);
            return;
        } else {
            System.out.println("r56");
            return;
        }
    }

    

    public void negation(TreeNode pere) throws ExceptionSyntaxique, ExceptionSemantique {

        System.out.print("negation : ");
        this.rulesName.add("negation");
        if (tokenTag() == "NUM_CONST"
                || tokenTag() == "STR_CONST"
                || tokenTag() == "true"
                || tokenTag() == "false"
                || tokenTag() == "null"
                || tokenTag() == "character'val"
                || tokenTag() == "("
                || tokenTag() == "VARIABLE") {
            System.out.println("r57");
            axiome(pere);
            return;
        } else if (tokenTag() == "-") {
            // On ajoute un noeud.
            TreeNode noeud = new TreeNode(tokenTag());
            pere.addSon(noeud);

            System.out.println("r58");
            advanceToken();
            if (tokenTag() == "NUM_CONST"
                    || tokenTag() == "STR_CONST"
                    || tokenTag() == "true"
                    || tokenTag() == "false"
                    || tokenTag() == "null"
                    || tokenTag() == "character'val"
                    || tokenTag() == "("
                    || tokenTag() == "VARIABLE") {
                axiome(noeud);
                return;
            } else {
                printErrorAndSkip("identificateur ou entier ou chaine ou 'true' ou 'false' ou 'null' ou '(' ou '-' ou 'character val'attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
            }
        } else if (tokenTag() == "not") {
            // On ajoute un noeud.
            TreeNode noeud = new TreeNode(tokenTag());
            pere.addSon(noeud);

            System.out.println("r59");
            advanceToken();
            if (tokenTag() == "NUM_CONST"
                    || tokenTag() == "STR_CONST"
                    || tokenTag() == "true"
                    || tokenTag() == "false"
                    || tokenTag() == "null"
                    || tokenTag() == "character'val"
                    || tokenTag() == "("
                    || tokenTag() == "VARIABLE") {
                axiome(noeud);
                return;
            } else {
                printErrorAndSkip("identificateur ou entier ou chaine ou 'true' ou 'false' ou 'null' ou '(' ou 'not' ou 'character val'attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
            }
        } else {
            printErrorAndSkip("identificateur ou entier ou chaine ou 'true' ou 'false' ou 'null' ou '(' ou 'not' ou '-' ou 'character val'attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
        }
    }

    public void axiome(TreeNode pere) throws ExceptionSyntaxique, ExceptionSemantique {

        System.out.print("axiome : ");
        this.rulesName.add("axiome");
        if (tokenTag() == "NUM_CONST"){
            System.out.println("r60");
            //_______________________________CONTROLES SEMANTIQUES ICI_____________________________//
            int index = currentTokenIndex;
            boolean decl_affect = false;
            boolean retour = false;
            boolean for_gauche = false;
            boolean for_droite = false;
            int open = 0;
            int close = 0;
            // On va regarder dans quelles conditions la variable est utilisée
            while (this.tokens.get(index).getTag() != ";"){
                if (this.tokens.get(index).getTag() == "=" && this.tokens.get(index-1).getTag() == ":"){
                    decl_affect = true;
                    break;
                }
                if (this.tokens.get(index).getTag() == "return"){
                    retour = true;
                    break;
                }
                if (this.tokens.get(index+1).getTag() == "in" || this.tokens.get(index+1).getTag() == "reverse"){
                    for_gauche = true;
                    break;
                }
                if (this.tokens.get(index).getTag() == "DOTDOT"){
                    for_droite = true;
                    break;
                }
                if (this.tokens.get(index).getTag() == "put"){
                    break;
                }
                if (this.tokens.get(index+1).getTag() == "("){
                    open+=1;
                }
                if (this.tokens.get(index+1).getTag() == ")"){
                    close+=1;
                }
                if(this.tokens.get(index).getTag() == "VARIABLE" && this.tokens.get(index+1).getTag() == "("){
                    TDSItemFctProc fct = findFunctionDeclaration(((VarToken)this.tokens.get(index)).getValue());
                    if (fct !=null && open>close){
                        break; //car notre str est paramètre de fonction et son cas sera traité postérieurement
                    }
                }
                //A voir s'il y a d'autres cas à gérer
                index--;
            }
            if (decl_affect && !isTokeninFunction(currentTokenIndex)){
                //Dans ce cas, il faut aller vérifier que le membre gauche de l'expression est bien de type integer/float
                index -= 2;
                if (((VarToken)this.tokens.get(index)).isType()){
                    index -= 2;
                }
                VarToken membre_gauche = (VarToken) this.tokens.get(index);
            }
            if (retour && !isTokeninFunction(currentTokenIndex)){
                //Dans ce cas, il faut aller vérifier que le type integer/float est bien compatible avec le type de la fonction
                //On récupère le type de retour de la fonction dont l'attribut fille est la table des symboles actuelle
                String type_retour = this.tableDesSymboles.getParent().getFonctions().values().stream()
                .filter(f -> Integer.valueOf(f.getTableFille().getId()).equals(this.tableDesSymboles.getId()))
                .map(TDSItemFctProc::getType)
                .findFirst()
                .orElse(null);
                //On vérifie que les types sont compatibles
                if (this.tokens.get(currentTokenIndex) instanceof FloatToken ){
                    if (!type_retour.equals("float")){
                        printErrorAndSkip("Vous avez utilisé un float alors que la fonction retourne un type "+ type_retour + ". Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "semantique");
                    }
                }else{
                    if (!type_retour.equals("integer")){
                        printErrorAndSkip("Vous avez utilisé un integer alors que la fonction retourne un type "+ type_retour + ". Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "semantique");
                    }
                }
            }
            if (for_gauche && !isTokeninFunction(currentTokenIndex)){
                //Dans ce cas il faut juste vérifier que l'instance de notre NUM_CONST n'est pas FloatToken
                if (this.tokens.get(currentTokenIndex) instanceof FloatToken ){
                    printErrorAndSkip("Vous avez utilisé un float alors que c'est impossible dans une expression de boucle. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "semantique");
                }
                else {
                    //Sinon il faut vérifier que le membre précédent de l'expression de la boucle est bien de type integer
                    //Première étape : on vérifie que notre integer n'est pas le premier membre de l'expression
                    index = currentTokenIndex-1;
                    if (this.tokens.get(index).getTag()!= "in" && this.tokens.get(index).getTag()!= "reverse"){
                        //Ici on passe tous ce qui est parenthèses et opérateurs
                        while (this.tokens.get(index).getTag() != "NUM_CONST" && this.tokens.get(index).getTag() != "STR_CONST" && this.tokens.get(index).getTag() != "VARIABLE" && !isTokeninFunction(index)){
                            index--;
                        }
                        if (this.tokens.get(index).getTag() == "STR_CONST"){
                            printErrorAndSkip("Vous avez utilisé un integer alors que le membre précédent de l'expression de la boucle est de type character. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "semantique");
                        }
                        else if (this.tokens.get(index).getTag() == "VARIABLE"){
                            TDSItemVar TDSvar = findVariableDeclaration(((VarToken)this.tokens.get(index)).getValue());
                            if (TDSvar != null){
                                if (TDSvar.getType().toLowerCase().equals("character")){
                                    printErrorAndSkip("Vous avez utilisé un integer alors que le membre précédent de l'expression de la boucle est de type character. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "semantique");
                                }
                            }
                        }
                    }
                }
            }
            if (for_droite && !isTokeninFunction(currentTokenIndex)){
                //Dans ce cas il faut juste vérifier que l'instance de notre NUM_CONST n'est pas FloatToken
                if (this.tokens.get(currentTokenIndex) instanceof FloatToken ){
                    printErrorAndSkip("Vous avez utilisé un float alors que c'est impossible dans une expression de boucle. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "semantique");
                }
                else {
                    //Sinon il faut vérifier que le membre précédent de l'expression de la boucle est bien de type integer
                    //Première étape : on vérifie que notre integer n'est pas le premier membre de l'expression
                    index = currentTokenIndex-1;
                    if (this.tokens.get(index).getTag()!= "DOTDOT"){
                        //Ici on passe tous ce qui est parenthèses et opérateurs
                        while (this.tokens.get(index).getTag() != "NUM_CONST" && this.tokens.get(index).getTag() != "STR_CONST" && this.tokens.get(index).getTag() != "VARIABLE" && !isTokeninFunction(index)){
                            index--;
                        }
                        if (this.tokens.get(index).getTag() == "STR_CONST"){
                            printErrorAndSkip("Vous avez utilisé un integer alors que le membre précédent de l'expression de la boucle est de type character. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "semantique");
                        }
                        else if (this.tokens.get(index).getTag() == "VARIABLE"){
                            TDSItemVar TDSvar = findVariableDeclaration(((VarToken)this.tokens.get(index)).getValue());
                            if (TDSvar != null){
                                if (TDSvar.getType().toLowerCase().equals("character")){
                                    printErrorAndSkip("Vous avez utilisé un integer alors que le membre précédent de l'expression de la boucle est de type character. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "semantique");
                                }
                            }
                        }
                    } else {
                        //Il faut aller vérifier que le dernier membre de l'expression du for de gauche est bien de type integer
                        index = currentTokenIndex-2;
                        while (this.tokens.get(index).getTag() == ")"){
                            index--;
                        }
                        if(this.tokens.get(index).getTag() == "STR_CONST"){
                            printErrorAndSkip("Vous avez utilisé un integer alors que le membre à gauche du '..' est de type character. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "semantique");
                        }
                        else if (this.tokens.get(index).getTag() == "VARIABLE"){
                            TDSItemVar TDSvar = findVariableDeclaration(((VarToken)this.tokens.get(index)).getValue());
                            TDSItemFctProc TDSfct = findFunctionDeclaration(((VarToken)this.tokens.get(index)).getValue());
                            if (TDSvar != null){
                                if (TDSvar.getType().toLowerCase().equals("character")){
                                    printErrorAndSkip("Vous avez utilisé un integer alors que le membre à gauche du '..' est de type character. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "semantique");
                                }
                            } else{
                                if (TDSfct.getType().toLowerCase().equals("character")){
                                    printErrorAndSkip("Vous avez utilisé un integer alors que le membre à gauche du '..' est de type character. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "semantique");
                                }
                            }
                        }
                    }
                }
            }
            //_____________________________________________________________________________________//
            // On ajoute un noeud.
            TreeNode noeud;
            if (getNextToken() instanceof IntegerToken){
                noeud = new TreeNode(Integer.toString(((IntegerToken) getNextToken()).getValue()));
            } else{
                noeud = new TreeNode(Float.toString(((FloatToken) getNextToken()).getValue()));
            }
            pere.addSon(noeud);

            advanceToken();
            axiomeRecur(noeud);
            return;
        }
        else if (tokenTag() == "STR_CONST"){
            System.out.println("r61");
            //_______________________________CONTROLES SEMANTIQUES ICI_____________________________//
            int index = currentTokenIndex;
            boolean decl_affect = false;
            boolean retour = false;
            boolean for_gauche = false;
            boolean for_droite = false;
            int open = 0;
            int close = 0;
            // On va regarder dans quelles conditions la string est utilisée
            while (this.tokens.get(index).getTag() != ";"){
                if (this.tokens.get(index).getTag() == "=" && this.tokens.get(index-1).getTag() == ":"){
                    decl_affect = true;
                    break;
                }
                if (this.tokens.get(index).getTag() == "return"){
                    retour = true;
                    break;
                }
                if (this.tokens.get(index+1).getTag() == "in" || this.tokens.get(index+1).getTag() == "reverse"){
                    for_gauche = true;
                    break;
                }
                if (this.tokens.get(index).getTag() == "DOTDOT"){
                    for_droite = true;
                    break;
                }
                if (this.tokens.get(index).getTag() == "put"){
                    break;
                }
                if (this.tokens.get(index+1).getTag() == "("){
                    open+=1;
                }
                if (this.tokens.get(index+1).getTag() == ")"){
                    close+=1;
                }
                if(this.tokens.get(index).getTag() == "VARIABLE" && this.tokens.get(index+1).getTag() == "("){
                    TDSItemFctProc fct = findFunctionDeclaration(((VarToken)this.tokens.get(index)).getValue());
                    if (fct !=null && open>close){
                        break; //car notre str est paramètre de fonction et son cas sera traité postérieurement
                    }
                }
                //A voir s'il y a d'autres cas à gérer
                index--;
            }
            if (decl_affect && !isTokeninFunction(currentTokenIndex)){
                //Dans ce cas, il faut aller vérifier que le membre gauche de l'expression est bien de type string
                index -= 2;
                if (((VarToken)this.tokens.get(index)).isType()){
                    index -= 2;
                }
                VarToken membre_gauche = (VarToken) this.tokens.get(index);
                //On regarde si le membre gauche est bien définie dans la TDS
                TDSItemVar TDSvar_membre_gauche = findVariableDeclaration(membre_gauche.getValue());
                if (TDSvar_membre_gauche != null){
                    //On vérifie que les types sont compatibles sauf pour les booléens où on vérifie dans les expressions
                    if (!TDSvar_membre_gauche.getType().equalsIgnoreCase("boolean")) {
                        if (this.tokens.get(currentTokenIndex) instanceof StringToken) {
                            if (!TDSvar_membre_gauche.getType().toLowerCase().equals("string")) {
                                printErrorAndSkip("Vous avez utilisé un string alors que le membre gauche de l'expression est de type " + TDSvar_membre_gauche.getType().toLowerCase() + ". Dans fichier source, ligne : " + this.tokens.get(currentTokenIndex).getLine(), "semantique");
                            }
                        } else {
                            if (!TDSvar_membre_gauche.getType().toLowerCase().equals("character")) {
                                printErrorAndSkip("Vous avez utilisé un character alors que le membre gauche de l'expression est de type " + TDSvar_membre_gauche.getType().toLowerCase() + ". Dans fichier source, ligne : " + this.tokens.get(currentTokenIndex).getLine(), "semantique");
                            }
                        }
                    }
                }
            }
            if (retour && !isTokeninFunction(currentTokenIndex)){
                //Dans ce cas, il faut aller vérifier que le string est bien compatible avec le type de la fonction
                //On récupère le type de retour de la fonction dont l'attribut fille est la table des symboles actuelle
                String type_retour = this.tableDesSymboles.getParent().getFonctions().values().stream()
                .filter(f -> Integer.valueOf(f.getTableFille().getId()).equals(this.tableDesSymboles.getId()))
                .map(TDSItemFctProc::getType)
                .findFirst()
                .orElse(null);
                //On vérifie que les types sont compatibles
                if (this.tokens.get(currentTokenIndex) instanceof StringToken ){
                    if (!type_retour.equals("string")){
                        printErrorAndSkip("Vous avez utilisé un string alors que la fonction retourne un type "+ type_retour + ". Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "semantique");
                    }
                }else{
                    if (!type_retour.equals("character")){
                        printErrorAndSkip("Vous avez utilisé un character alors que la fonction retourne un type "+ type_retour + ". Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "semantique");
                    }
                }
            }
            if (for_gauche && !isTokeninFunction(currentTokenIndex)){
                //Dans ce cas il faut juste vérifier que l'instance de notre STR_CONST n'est pas StringToken
                if (this.tokens.get(currentTokenIndex) instanceof StringToken ){
                    printErrorAndSkip("Vous avez utilisé un string alors que c'est impossible dans une expression de boucle. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "semantique");
                }
                else {
                    //Sinon il faut vérifier que le membre précédent de l'expression de la boucle est bien de type character
                    //Première étape : on vérifie que notre character n'est pas le premier membre de l'expression
                    index = currentTokenIndex-1;
                    if (this.tokens.get(index).getTag()!= "in" && this.tokens.get(index).getTag()!= "reverse"){
                        //Ici on passe tous ce qui est parenthèses et opérateurs
                        while (this.tokens.get(index).getTag() != "NUM_CONST" && this.tokens.get(index).getTag() != "STR_CONST" && this.tokens.get(index).getTag() != "VARIABLE" && !isTokeninFunction(index)){
                            index--;
                        }
                        if (this.tokens.get(index).getTag() == "NUM_CONST"){
                            printErrorAndSkip("Vous avez utilisé un character alors que le membre précédent de l'expression de la boucle est de type integer. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "semantique");
                        }
                        else if (this.tokens.get(index).getTag() == "VARIABLE"){
                            TDSItemVar TDSvar = findVariableDeclaration(((VarToken)this.tokens.get(index)).getValue());
                            if (TDSvar != null){
                                if (TDSvar.getType().toLowerCase().equals("integer")){
                                    printErrorAndSkip("Vous avez utilisé un character alors que le membre précédent de l'expression de la boucle est de type integer. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "semantique");
                                }
                            }
                        }
                    }
                }
            }
            if (for_droite && !isTokeninFunction(currentTokenIndex)){
                //Dans ce cas il faut juste vérifier que l'instance de notre STR_CONST n'est pas StringToken
                if (this.tokens.get(currentTokenIndex) instanceof StringToken ){
                    printErrorAndSkip("Vous avez utilisé un string alors que c'est impossible dans une expression de boucle. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "semantique");
                }
                else {
                    //Sinon il faut vérifier que le membre précédent de l'expression de la boucle est bien de type character
                    //Première étape : on vérifie que notre character n'est pas le premier membre de l'expression
                    index = currentTokenIndex-1;
                    if (this.tokens.get(index).getTag()!= "DOTDOT"){
                        //Ici on passe tous ce qui est parenthèses et opérateurs
                        while (this.tokens.get(index).getTag() != "NUM_CONST" && this.tokens.get(index).getTag() != "STR_CONST" && this.tokens.get(index).getTag() != "VARIABLE" && !isTokeninFunction(index)){
                            index--;
                        }
                        if (this.tokens.get(index).getTag() == "NUM_CONST"){
                            printErrorAndSkip("Vous avez utilisé un character alors que le membre précédent de l'expression de la boucle est de type integer. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "semantique");
                        }
                        else if (this.tokens.get(index).getTag() == "VARIABLE"){
                            TDSItemVar TDSvar = findVariableDeclaration(((VarToken)this.tokens.get(index)).getValue());
                            if (TDSvar != null){
                                if (TDSvar.getType().toLowerCase().equals("integer")){
                                    printErrorAndSkip("Vous avez utilisé un character alors que le membre précédent de l'expression de la boucle est de type integer. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "semantique");
                                }
                            }
                        }
                    }
                    else {
                        //Il faut aller vérifier que le dernier membre de l'expression du for de gauche est bien de type character
                        index = currentTokenIndex-2;
                        while (this.tokens.get(index).getTag() == ")"){
                            index--;
                        }
                        if(this.tokens.get(index).getTag() == "NUM_CONST"){
                            printErrorAndSkip("Vous avez utilisé un character alors que le membre à gauche du '..' est de type integer. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "semantique");
                        }
                        else if (this.tokens.get(index).getTag() == "VARIABLE"){
                            TDSItemVar TDSvar = findVariableDeclaration(((VarToken)this.tokens.get(index)).getValue());
                            TDSItemFctProc TDSfct = findFunctionDeclaration(((VarToken)this.tokens.get(index)).getValue());
                            if (TDSvar != null){
                                if (TDSvar.getType().toLowerCase().equals("integer")){
                                    printErrorAndSkip("Vous avez utilisé un character alors que le membre à gauche du '..' est de type integer. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "semantique");
                                }
                            } else{
                                if (TDSfct.getType().toLowerCase().equals("integer")){
                                    printErrorAndSkip("Vous avez utilisé un character alors que le membre à gauche du '..' est de type integer. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "semantique");
                                }
                            }
                        }
                    }
                }
            }
            
            //________________________________________________________________________________//
            // On ajoute un noeud.
            TreeNode noeud;
            if (getNextToken() instanceof StringToken){
                noeud = new TreeNode(((StringToken) getNextToken()).getValue());
            } else{
                noeud = new TreeNode(((CharacterToken) getNextToken()).getValue());
            }
            pere.addSon(noeud);

            advanceToken();
            axiomeRecur(noeud);
            return;
        }
        else if (tokenTag() == "true"){
            System.out.println("r62");
            //_______________________________CONTROLES SEMANTIQUES ICI_____________________________//
            int index = currentTokenIndex;
            boolean decl_affect = false;
            boolean retour = false;
            boolean for_gauche = false;
            boolean for_droite = false;
            int open = 0;
            int close = 0;
            // On va regarder dans quelles conditions la variable est utilisée
            while (this.tokens.get(index).getTag() != ";"){
                if (this.tokens.get(index).getTag() == "=" && this.tokens.get(index-1).getTag() == ":"){
                    decl_affect = true;
                    break;
                }
                if (this.tokens.get(index).getTag() == "return"){
                    retour = true;
                    break;
                }
                if (this.tokens.get(index+1).getTag() == "in" || this.tokens.get(index+1).getTag() == "reverse"){
                    for_gauche = true;
                    break;
                }
                if (this.tokens.get(index).getTag() == "DOTDOT"){
                    for_droite = true;
                    break;
                }
                if (this.tokens.get(index).getTag() == "put"){
                    break;
                }
                if (this.tokens.get(index+1).getTag() == "("){
                    open+=1;
                }
                if (this.tokens.get(index+1).getTag() == ")"){
                    close+=1;
                }
                if(this.tokens.get(index).getTag() == "VARIABLE" && this.tokens.get(index+1).getTag() == "("){
                    TDSItemFctProc fct = findFunctionDeclaration(((VarToken)this.tokens.get(index)).getValue());
                    if (fct !=null && open>close){
                        break; //car notre str est paramètre de fonction et son cas sera traité postérieurement
                    }
                }
                //A voir s'il y a d'autres cas à gérer
                index--;
            }
            if (decl_affect && !isTokeninFunction(currentTokenIndex)){
                //Dans ce cas, il faut aller vérifier que le membre gauche de l'expression est bien de type boolean
                index -= 2;
                if (((VarToken)this.tokens.get(index)).isType()){
                    index -= 2;
                }
                VarToken membre_gauche = (VarToken) this.tokens.get(index);
                //On regarde si le membre gauche est bien définie dans la TDS
                TDSItemVar TDSvar_membre_gauche = findVariableDeclaration(membre_gauche.getValue());
                if (TDSvar_membre_gauche != null){
                    //On vérifie que les types sont compatibles
                    if(!TDSvar_membre_gauche.getType().toLowerCase().equals("boolean")){
                        printErrorAndSkip("Vous avez utilisé un boolean alors que le membre gauche de l'expression est de type "+ TDSvar_membre_gauche.getType().toLowerCase() + ". Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "semantique");
                    }
                }
            }
            if (retour && !isTokeninFunction(currentTokenIndex)){
                //Dans ce cas, il faut aller vérifier que le type boolean est bien compatible avec le type de la fonction
                //On récupère le type de retour de la fonction dont l'attribut fille est la table des symboles actuelle
                String type_retour = this.tableDesSymboles.getParent().getFonctions().values().stream()
                .filter(f -> Integer.valueOf(f.getTableFille().getId()).equals(this.tableDesSymboles.getId()))
                .map(TDSItemFctProc::getType)
                .findFirst()
                .orElse(null);
                //On vérifie que les types sont compatibles
                if (!type_retour.equals("boolean")){
                    printErrorAndSkip("Vous avez utilisé un boolean alors que la fonction retourne un type "+ type_retour + ". Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "semantique");
                }
            }
            if (for_gauche && !isTokeninFunction(currentTokenIndex)){
                //Dans ce cas, ce n'est pas possible d'utiliser un boolean dans une expression de boucle
                printErrorAndSkip("Vous avez utilisé un boolean alors que c'est impossible dans une expression de for. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "semantique");
            }
            if (for_droite && !isTokeninFunction(currentTokenIndex)){
                //Dans ce cas, ce n'est pas possible d'utiliser un boolean dans une expression de boucle
                printErrorAndSkip("Vous avez utilisé un boolean alors que c'est impossible dans une expression de for. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "semantique");
            }
            // On ajoute un noeud.
            TreeNode noeud = new TreeNode(tokenTag());
            pere.addSon(noeud);

            advanceToken();
            axiomeRecur(noeud);
            return;
        }
        else if (tokenTag() == "false"){
            System.out.println("r63");
            //_______________________________CONTROLES SEMANTIQUES ICI_____________________________//
            int index = currentTokenIndex;
            boolean decl_affect = false;
            boolean retour = false;
            boolean for_gauche = false;
            boolean for_droite = false;
            int open = 0;
            int close = 0;
            // On va regarder dans quelles conditions la variable est utilisée
            while (this.tokens.get(index).getTag() != ";"){
                if (this.tokens.get(index).getTag() == "=" && this.tokens.get(index-1).getTag() == ":"){
                    decl_affect = true;
                    break;
                }
                if (this.tokens.get(index).getTag() == "return"){
                    retour = true;
                    break;
                }
                if (this.tokens.get(index+1).getTag() == "in" || this.tokens.get(index+1).getTag() == "reverse"){
                    for_gauche = true;
                    break;
                }
                if (this.tokens.get(index).getTag() == "DOTDOT"){
                    for_droite = true;
                    break;
                }
                if (this.tokens.get(index).getTag() == "put"){
                    break;
                }
                if (this.tokens.get(index+1).getTag() == "("){
                    open+=1;
                }
                if (this.tokens.get(index+1).getTag() == ")"){
                    close+=1;
                }
                if(this.tokens.get(index).getTag() == "VARIABLE" && this.tokens.get(index+1).getTag() == "("){
                    TDSItemFctProc fct = findFunctionDeclaration(((VarToken)this.tokens.get(index)).getValue());
                    if (fct !=null && open>close){
                        break; //car notre str est paramètre de fonction et son cas sera traité postérieurement
                    }
                }
                //A voir s'il y a d'autres cas à gérer
                index--;
            }
            if (decl_affect && !isTokeninFunction(currentTokenIndex)){
                //Dans ce cas, il faut aller vérifier que le membre gauche de l'expression est bien de type boolean
                index -= 2;
                if (((VarToken)this.tokens.get(index)).isType()){
                    index -= 2;
                }
                VarToken membre_gauche = (VarToken) this.tokens.get(index);
                //On regarde si le membre gauche est bien définie dans la TDS
                TDSItemVar TDSvar_membre_gauche = findVariableDeclaration(membre_gauche.getValue());
                if (TDSvar_membre_gauche != null){
                    //On vérifie que les types sont compatibles
                    if(!TDSvar_membre_gauche.getType().toLowerCase().equals("boolean")){
                        printErrorAndSkip("Vous avez utilisé un boolean alors que le membre gauche de l'expression est de type "+ TDSvar_membre_gauche.getType().toLowerCase() + ". Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "semantique");
                    }
                }
            }
            if (retour && !isTokeninFunction(currentTokenIndex)){
                //Dans ce cas, il faut aller vérifier que le type boolean est bien compatible avec le type de la fonction
                //On récupère le type de retour de la fonction dont l'attribut fille est la table des symboles actuelle
                String type_retour = this.tableDesSymboles.getParent().getFonctions().values().stream()
                .filter(f -> Integer.valueOf(f.getTableFille().getId()).equals(this.tableDesSymboles.getId()))
                .map(TDSItemFctProc::getType)
                .findFirst()
                .orElse(null);
                //On vérifie que les types sont compatibles
                if (!type_retour.equals("boolean")){
                    printErrorAndSkip("Vous avez utilisé un boolean alors que la fonction retourne un type "+ type_retour + ". Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "semantique");
                }
            }
            if (for_gauche && !isTokeninFunction(currentTokenIndex)){
                //Dans ce cas, ce n'est pas possible d'utiliser un boolean dans une expression de boucle
                printErrorAndSkip("Vous avez utilisé un boolean alors que c'est impossible dans une expression de for. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "semantique");
            }
            if (for_droite && !isTokeninFunction(currentTokenIndex)){
                //Dans ce cas, ce n'est pas possible d'utiliser un boolean dans une expression de boucle
                printErrorAndSkip("Vous avez utilisé un boolean alors que c'est impossible dans une expression de for. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "semantique");
            }
            // On ajoute un noeud.
            TreeNode noeud = new TreeNode(tokenTag());
            pere.addSon(noeud);

            advanceToken();
            axiomeRecur(noeud);
            return;
        }
        else if (tokenTag() == "null"){
            // On ajoute un noeud.
            TreeNode noeud = new TreeNode(tokenTag());
            pere.addSon(noeud);

            System.out.println("r64");
            advanceToken();
            axiomeRecur(noeud);
            return;
        }
        else if (tokenTag() == "character'val"){
            // On ajoute un noeud.
            TreeNode noeud = new TreeNode(tokenTag());
            pere.addSon(noeud);

            System.out.println("r65");
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
                        axiomeRecur(noeud);
                        return;
                    } else{
                        printErrorAndSkip("')' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
                    }
                } else{
                    printErrorAndSkip("expression attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
                }
            } else{
                printErrorAndSkip("'(' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
            }
        }
        else if (tokenTag() == "("){

            System.out.println("r66");
            advanceToken();
            expression(pere);
            if (tokenTag() == ")"){

                advanceToken();
                return;
            }
            else{
                printErrorAndSkip("')' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
            }
        }
        else if (tokenTag() == "VARIABLE"){
            System.out.println("r67");
            //_______________________________BEAUCOUP DE CONTROLES SEMANTIQUES ICI_____________________________//
            VarToken variable_actuelle = (VarToken) this.tokens.get(currentTokenIndex);
            //On vérifie que la variable existe (soit en tant que variable, soit en tant que fonction)
            TDSItemVar TDSvar = findVariableDeclaration(variable_actuelle.getValue());
            TDSItemFctProc TDSfct = findFunctionDeclaration(variable_actuelle.getValue());
            if (TDSvar == null && TDSfct == null){
                printErrorAndSkip("La variable "+ variable_actuelle.getValue() +" n'a pas été déclarée. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "semantique");
            }else{
                String type_actuel_var = null;
                String type_actuel_fct = null;
                if (TDSvar != null){
                    type_actuel_var = TDSvar.getType();
                }   else{
                    type_actuel_fct = TDSfct.getType();
                }
                int index = currentTokenIndex;
                boolean decl_affect = false;
                boolean retour = false;
                boolean for_gauche = false;
                boolean for_droite = false;
                int open = 0;
                int close = 0;
                // On va regarder dans quelles conditions la variable est utilisée
                while (this.tokens.get(index).getTag() != ";"){
                    if (this.tokens.get(index).getTag() == "=" && this.tokens.get(index-1).getTag() == ":"){
                        decl_affect = true;
                        break;
                    }
                    if (this.tokens.get(index).getTag() == "return"){
                        retour = true;
                        break;
                    }
                    if (this.tokens.get(index+1).getTag() == "in" || this.tokens.get(index+1).getTag() == "reverse"){
                        for_gauche = true;
                        break;
                    }
                    if (this.tokens.get(index).getTag() == "DOTDOT"){
                        for_droite = true;
                        break;
                    }
                    if (this.tokens.get(index).getTag() == "put"){
                        break;
                    }
                    if (this.tokens.get(index+1).getTag() == "("){
                        open+=1;
                    }
                    if (this.tokens.get(index+1).getTag() == ")"){
                        close+=1;
                    }
                    if(this.tokens.get(index).getTag() == "VARIABLE" && this.tokens.get(index+1).getTag() == "("){
                        TDSItemFctProc fct = findFunctionDeclaration(((VarToken)this.tokens.get(index)).getValue());
                        if (fct !=null && open>close){
                            break; //car notre str est paramètre de fonction et son cas sera traité postérieurement
                        }
                    }
                    //A voir s'il y a d'autres cas à gérer
                    index--;
                }
                if (decl_affect && !isTokeninFunction(currentTokenIndex)){
                    //Dans ce cas, il faut aller vérifier que le membre gauche de l'expression est bien fdu même type que la variable actuelle
                    index -= 2;
                    if (((VarToken)this.tokens.get(index)).isType()){
                        index -= 2;
                    }
                    VarToken membre_gauche = (VarToken) this.tokens.get(currentTokenIndex);
                    //On regarde si le membre gauche est bien définie dans la TDS
                    TDSItemVar TDSvar_membre_gauche = findVariableDeclaration(membre_gauche.getValue());
                    if (TDSvar_membre_gauche != null){
                        //On vérifie que les types sont compatibles
                        if (TDSvar != null){
                            if (!TDSvar_membre_gauche.getType().equals(type_actuel_var)){
                                printErrorAndSkip("La variable "+ variable_actuelle.getValue() +" est de type "+ type_actuel_var +" alors que le membre gauche de l'expression est de type "+ TDSvar_membre_gauche.getType() + ". Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "semantique");
                            }
                        }else{
                            if (!TDSvar_membre_gauche.getType().equals(type_actuel_fct)){
                                printErrorAndSkip("La fonction "+ variable_actuelle.getValue() +" retourne un type "+ type_actuel_fct +" alors que le membre gauche de l'expression est de type "+ TDSvar_membre_gauche.getType() + ". Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "semantique");
                            }
                        }
                    }
                }
                if (retour && !isTokeninFunction(currentTokenIndex)){
                    //Dans ce cas, il faut aller vérifier que le type de la variable est bien compatible avec le type de la fonction
                    //On récupère le type de retour de la fonction dont l'attribut fille est la table des symboles actuelle
                    String type_retour = this.tableDesSymboles.getParent().getFonctions().values().stream()
                    .filter(f -> Integer.valueOf(f.getTableFille().getId()).equals(this.tableDesSymboles.getId()))
                    .map(TDSItemFctProc::getType)
                    .findFirst()
                    .orElse(null);
                    if (TDSvar != null){
                        if (!type_actuel_var.equals(type_retour)){
                            printErrorAndSkip("La variable "+ variable_actuelle.getValue() +" est de type "+ type_actuel_var +" alors que la fonction retourne un type "+ type_retour + ". Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "semantique");
                        }
                    }else{
                        if (!type_actuel_fct.equals(type_retour)){
                            printErrorAndSkip("La fonction "+ variable_actuelle.getValue() +" retourne un type "+ type_actuel_fct +" alors que la fonction actuelle retourne un type "+ type_retour + ". Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "semantique");
                        }
                    }
                }
                if (for_gauche && !isTokeninFunction(currentTokenIndex)){
                    //Dans ce cas il faut juste vérifier que le type de notre VARIABLE est integer ou character
                    if (TDSvar!=null){
                        if (!type_actuel_var.toLowerCase().equals("integer") && !type_actuel_var.toLowerCase().equals("character")){
                            printErrorAndSkip("Vous avez utilisé une variable de type "+ type_actuel_var +" alors que c'est impossible dans une expression de boucle. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "semantique");
                        }
                        else {
                            //Sinon il faut vérifier que le membre précédent de l'expression de la boucle est bien du même type que notre variable
                            //Première étape : on vérifie que notre variable n'est pas le premier membre de l'expression
                            index = currentTokenIndex-1;
                            if (this.tokens.get(index).getTag()!= "in" && this.tokens.get(index).getTag()!= "reverse"){
                                //Ici on passe tous ce qui est parenthèses et opérateurs
                                while (this.tokens.get(index).getTag() != "NUM_CONST" && this.tokens.get(index).getTag() != "STR_CONST" && this.tokens.get(index).getTag() != "VARIABLE" && !isTokeninFunction(index)){
                                    index--;
                                }
                                if(type_actuel_var.equals("integer") && this.tokens.get(index).getTag() == "STR_CONST"){
                                    printErrorAndSkip("Vous avez utilisé une variable de type integer alors que le membre précédent de l'expression de la boucle est de type character. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "semantique");
                                }
                                else if(type_actuel_var.equals("character") && this.tokens.get(index).getTag() == "NUM_CONST"){
                                    printErrorAndSkip("Vous avez utilisé une variable de type character alors que le membre précédent de l'expression de la boucle est de type integer. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "semantique");
                                }
                                else if (this.tokens.get(index).getTag() == "VARIABLE"){
                                    TDSItemVar TDSvar2 = findVariableDeclaration(((VarToken)this.tokens.get(index)).getValue());
                                    TDSItemFctProc TDSfct2 = findFunctionDeclaration(((VarToken)this.tokens.get(index)).getValue());
                                    if (TDSvar2 != null){
                                        if (type_actuel_var.equals("integer") && TDSvar2.getType().toLowerCase().equals("character")){
                                            printErrorAndSkip("Vous avez utilisé une variable de type integer alors que le membre précédent de l'expression de la boucle est de type character. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "semantique");
                                        }
                                        else if (type_actuel_var.equals("character") && TDSvar2.getType().toLowerCase().equals("integer")){
                                            printErrorAndSkip("Vous avez utilisé une variable de type character alors que le membre précédent de l'expression de la boucle est de type integer. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "semantique");
                                        }
                                    }
                                    else {
                                        if (TDSfct2 != null){
                                            if (type_actuel_var.equals("integer") && TDSfct2.getType().toLowerCase().equals("character")){
                                                printErrorAndSkip("Vous avez utilisé une variable de type integer alors que le membre précédent de l'expression de la boucle est de type character. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "semantique");
                                            }
                                            else if (type_actuel_var.equals("character") && TDSfct2.getType().toLowerCase().equals("integer")){
                                                printErrorAndSkip("Vous avez utilisé une variable de type character alors que le membre précédent de l'expression de la boucle est de type integer. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "semantique");
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else{
                        //Dans ce cas il faut vérifier que le type de retour de notre fonction est integer ou character
                        if (!type_actuel_fct.toLowerCase().equals("integer") && !type_actuel_fct.toLowerCase().equals("character")){
                            printErrorAndSkip("Vous avez utilisé une fonction de type "+ type_actuel_fct +" alors que c'est impossible dans une expression de boucle. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "semantique");
                        }
                        else {
                            //Sinon il faut vérifier que le membre précédent de l'expression de la boucle est bien du même type que notre fonction
                            //Première étape : on vérifie que notre fonction n'est pas le premier membre de l'expression
                            index = currentTokenIndex-1;
                            if (this.tokens.get(index).getTag()!= "in" && this.tokens.get(index).getTag()!= "reverse"){
                                //Ici on passe tous ce qui est parenthèses et opérateurs
                                while (this.tokens.get(index).getTag() != "NUM_CONST" && this.tokens.get(index).getTag() != "STR_CONST" && this.tokens.get(index).getTag() != "VARIABLE" && !isTokeninFunction(index)){
                                    index--;
                                }
                                if(type_actuel_fct.equals("integer") && this.tokens.get(index).getTag() == "STR_CONST"){
                                    printErrorAndSkip("Vous avez utilisé une fonction de type integer alors que le membre précédent de l'expression de la boucle est de type character. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "semantique");
                                }
                                else if(type_actuel_fct.equals("character") && this.tokens.get(index).getTag() == "NUM_CONST"){
                                    printErrorAndSkip("Vous avez utilisé une fonction de type character alors que le membre précédent de l'expression de la boucle est de type integer. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "semantique");
                                }
                                else if (this.tokens.get(index).getTag() == "VARIABLE"){
                                    TDSItemVar TDSvar2 = findVariableDeclaration(((VarToken)this.tokens.get(index)).getValue());
                                    TDSItemFctProc TDSfct2 = findFunctionDeclaration(((VarToken)this.tokens.get(index)).getValue());
                                    if (TDSvar2 != null){
                                        if (type_actuel_fct.equals("integer") && TDSvar2.getType().toLowerCase().equals("character")){
                                            printErrorAndSkip("Vous avez utilisé une fonction de type integer alors que le membre précédent de l'expression de la boucle est de type character. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "semantique");
                                        }
                                        else if (type_actuel_fct.equals("character") && TDSvar2.getType().toLowerCase().equals("integer")){
                                            printErrorAndSkip("Vous avez utilisé une fonction de type character alors que le membre précédent de l'expression de la boucle est de type integer. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "semantique");
                                        }
                                    }
                                    else {
                                        if (TDSfct2 != null){
                                            if (type_actuel_fct.equals("integer") && TDSfct2.getType().toLowerCase().equals("character")){
                                                printErrorAndSkip("Vous avez utilisé une fonction de type integer alors que le membre précédent de l'expression de la boucle est de type character. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "semantique");
                                            }
                                            else if (type_actuel_fct.equals("character") && TDSfct2.getType().toLowerCase().equals("integer")){
                                                printErrorAndSkip("Vous avez utilisé une fonction de type character alors que le membre précédent de l'expression de la boucle est de type integer. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "semantique");
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (for_droite && !isTokeninFunction(currentTokenIndex)){
                    //Dans ce cas il faut juste vérifier que le type de notre VARIABLE est integer ou character
                    if (TDSvar!=null){
                        if (!type_actuel_var.toLowerCase().equals("integer") && !type_actuel_var.toLowerCase().equals("character")){
                            printErrorAndSkip("Vous avez utilisé une variable de type "+ type_actuel_var +" alors que c'est impossible dans une expression de boucle. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "semantique");
                        }
                        else {
                            //Sinon il faut vérifier que le membre précédent de l'expression de la boucle est bien du même type que notre variable
                            //Première étape : on vérifie que notre variable n'est pas le premier membre de l'expression
                            index = currentTokenIndex-1;
                            if (this.tokens.get(index).getTag()!= "DOTDOT"){
                                //Ici on passe tous ce qui est parenthèses et opérateurs
                                while (this.tokens.get(index).getTag() != "NUM_CONST" && this.tokens.get(index).getTag() != "STR_CONST" && this.tokens.get(index).getTag() != "VARIABLE" && !isTokeninFunction(index)){
                                    index--;
                                }
                                if(type_actuel_var.equals("integer") && this.tokens.get(index).getTag() == "STR_CONST"){
                                    printErrorAndSkip("Vous avez utilisé une variable de type integer alors que le membre précédent de l'expression de la boucle est de type character. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "semantique");
                                }
                                else if(type_actuel_var.equals("character") && this.tokens.get(index).getTag() == "NUM_CONST"){
                                    printErrorAndSkip("Vous avez utilisé une variable de type character alors que le membre précédent de l'expression de la boucle est de type integer. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "semantique");
                                }
                                else if (this.tokens.get(index).getTag() == "VARIABLE"){
                                    TDSItemVar TDSvar2 = findVariableDeclaration(((VarToken)this.tokens.get(index)).getValue());
                                    TDSItemFctProc TDSfct2 = findFunctionDeclaration(((VarToken)this.tokens.get(index)).getValue());
                                    if (TDSvar2 != null){
                                        if (type_actuel_var.equals("integer") && TDSvar2.getType().toLowerCase().equals("character")){
                                            printErrorAndSkip("Vous avez utilisé une variable de type integer alors que le membre précédent de l'expression de la boucle est de type character. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "semantique");
                                        }
                                        else if (type_actuel_var.equals("character") && TDSvar2.getType().toLowerCase().equals("integer")){
                                            printErrorAndSkip("Vous avez utilisé une variable de type character alors que le membre précédent de l'expression de la boucle est de type integer. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "semantique");
                                        }
                                    }
                                    else {
                                        if (TDSfct2 != null){
                                            if (type_actuel_var.equals("integer") && TDSfct2.getType().toLowerCase().equals("character")){
                                                printErrorAndSkip("Vous avez utilisé une variable de type integer alors que le membre précédent de l'expression de la boucle est de type character. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "semantique");
                                            }
                                            else if (type_actuel_var.equals("character") && TDSfct2.getType().toLowerCase().equals("integer")){
                                                printErrorAndSkip("Vous avez utilisé une variable de type character alors que le membre précédent de l'expression de la boucle est de type integer. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "semantique");
                                            }
                                        }
                                    }
                                }
                            } else {
                                //Il faut aller vérifier que le dernier membre de l'expression du for de gauche est bien du même type que notre variable
                                index = currentTokenIndex-2;
                                while (this.tokens.get(index).getTag() == ")"){
                                    index--;
                                }
                                if(this.tokens.get(index).getTag() == "NUM_CONST" && type_actuel_var.equals("character")){
                                    printErrorAndSkip("Vous avez utilisé une variable de type character alors que le membre à gauche du '..' est de type integer. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "semantique");
                                }
                                else if (this.tokens.get(index).getTag() == "STR_CONST" && type_actuel_var.equals("integer")){
                                    printErrorAndSkip("Vous avez utilisé une variable de type integer alors que le membre à gauche du '..' est de type character. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "semantique");
                                }
                                else if (this.tokens.get(index).getTag() == "VARIABLE"){
                                    TDSItemVar TDSvar2 = findVariableDeclaration(((VarToken)this.tokens.get(index)).getValue());
                                    TDSItemFctProc TDSfct2 = findFunctionDeclaration(((VarToken)this.tokens.get(index)).getValue());
                                    if (TDSvar2 != null){
                                        if (type_actuel_var.equals("character") && TDSvar2.getType().toLowerCase().equals("integer")){
                                            printErrorAndSkip("Vous avez utilisé une variable de type character alors que le membre à gauche du '..' est de type integer. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "semantique");
                                        }
                                    } else{
                                        if (TDSfct2.getType().toLowerCase().equals("integer")){
                                            printErrorAndSkip("Vous avez utilisé une variable de type character alors que le membre à gauche du '..' est de type integer. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "semantique");
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        //Dans ce cas il faut vérifier que le type de retour de notre fonction est integer ou character
                        if (!type_actuel_fct.toLowerCase().equals("integer") && !type_actuel_fct.toLowerCase().equals("character")){
                            printErrorAndSkip("Vous avez utilisé une fonction de type "+ type_actuel_fct +" alors que c'est impossible dans une expression de boucle. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "semantique");
                        }
                        else {
                            //Sinon il faut vérifier que le membre précédent de l'expression de la boucle est bien du même type que notre fonction
                            //Première étape : on vérifie que notre fonction n'est pas le premier membre de l'expression
                            index = currentTokenIndex-1;
                            if (this.tokens.get(index).getTag()!= "DOTDOT"){
                                //Ici on passe tous ce qui est parenthèses et opérateurs
                                while (this.tokens.get(index).getTag() != "NUM_CONST" && this.tokens.get(index).getTag() != "STR_CONST" && this.tokens.get(index).getTag() != "VARIABLE" && !isTokeninFunction(index)){
                                    index--;
                                }
                                if(type_actuel_fct.equals("integer") && this.tokens.get(index).getTag() == "STR_CONST"){
                                    printErrorAndSkip("Vous avez utilisé une fonction de type integer alors que le membre précédent de l'expression de la boucle est de type character. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "semantique");
                                }
                                else if(type_actuel_fct.equals("character") && this.tokens.get(index).getTag() == "NUM_CONST"){
                                    printErrorAndSkip("Vous avez utilisé une fonction de type character alors que le membre précédent de l'expression de la boucle est de type integer. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "semantique");
                                }
                                else if (this.tokens.get(index).getTag() == "VARIABLE"){
                                    TDSItemVar TDSvar2 = findVariableDeclaration(((VarToken)this.tokens.get(index)).getValue());
                                    TDSItemFctProc TDSfct2 = findFunctionDeclaration(((VarToken)this.tokens.get(index)).getValue());
                                    if (TDSvar2 != null){
                                        if (type_actuel_fct.equals("integer") && TDSvar2.getType().toLowerCase().equals("character")){
                                            printErrorAndSkip("Vous avez utilisé une fonction de type integer alors que le membre précédent de l'expression de la boucle est de type character. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "semantique");
                                        }
                                        else if (type_actuel_fct.equals("character") && TDSvar2.getType().toLowerCase().equals("integer")){
                                            printErrorAndSkip("Vous avez utilisé une fonction de type character alors que le membre précédent de l'expression de la boucle est de type integer. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "semantique");
                                        }
                                    }
                                    else {
                                        if (TDSfct2 != null){
                                            if (type_actuel_fct.equals("integer") && TDSfct2.getType().toLowerCase().equals("character")){
                                                printErrorAndSkip("Vous avez utilisé une fonction de type integer alors que le membre précédent de l'expression de la boucle est de type character. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "semantique");
                                            }
                                            else if (type_actuel_fct.equals("character") && TDSfct2.getType().toLowerCase().equals("integer")){
                                                printErrorAndSkip("Vous avez utilisé une fonction de type character alors que le membre précédent de l'expression de la boucle est de type integer. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "semantique");
                                            }
                                        }
                                    }
                                }
                            } else {
                                //Il faut aller vérifier que le dernier membre de l'expression du for de gauche est bien du même type que notre variable
                                index = currentTokenIndex-2;
                                while (this.tokens.get(index).getTag() == ")"){
                                    index--;
                                }
                                if(this.tokens.get(index).getTag() == "NUM_CONST" && type_actuel_fct == "character"){
                                    printErrorAndSkip("Vous avez utilisé une fonction de type character alors que le membre à gauche du '..' est de type integer. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "semantique");
                                }
                                else if (this.tokens.get(index).getTag() == "STR_CONST" && type_actuel_fct == "integer"){
                                    printErrorAndSkip("Vous avez utilisé une fonction de type integer alors que le membre à gauche du '..' est de type character. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "semantique");
                                }
                                else if (this.tokens.get(index).getTag() == "VARIABLE"){
                                    TDSItemVar TDSvar2 = findVariableDeclaration(((VarToken)this.tokens.get(index)).getValue());
                                    TDSItemFctProc TDSfct2 = findFunctionDeclaration(((VarToken)this.tokens.get(index)).getValue());
                                    if (TDSvar2 != null){
                                        if (type_actuel_fct.equals("character") && TDSvar2.getType().toLowerCase().equals("integer")){
                                            printErrorAndSkip("Vous avez utilisé une fonction de type character alors que le membre à gauche du '..' est de type integer. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "semantique");
                                        }
                                    } else{
                                        if (TDSfct2.getType().toLowerCase().equals("integer")){
                                            printErrorAndSkip("Vous avez utilisé une fonction de type character alors que le membre à gauche du '..' est de type integer. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "semantique");
                                        }
                                    }
                                }
                            }
                        }
                    }
                }          
            }

            //On ajoute un noeud.
            TreeNode noeud = new TreeNode(((VarToken) getNextToken()).getValue());
            pere.addSon(noeud);

            advanceToken();
            axiome1(noeud);

            //System.out.println(this.tableDesSymboles.getId());
            //Ici, toute l'expression a été analysée, on peut vérifier dans le cas d'une fonction que le nombre d'arguments est correct et que les types sont compatibles
            if (TDSfct != null){
                //On vérifie que le nombre d'arguments est correct
                int nb_args_lus = 0;
                int i = 1; 
                while (this.tokens.get(currentTokenIndex-i).getTag() != "("){
                    nb_args_lus++;
                    int j=1;
                    while (this.tokens.get(currentTokenIndex-i-j).getTag() != "," && this.tokens.get(currentTokenIndex-i-j).getTag() != "("){
                        j++;
                    }
                    i+=j;
                }
                if (TDSfct.getNbArgs() != nb_args_lus){
                    printErrorAndSkip(variable_actuelle.getValue()+ " a reçu "+ nb_args_lus + " arguments alors qu'il en attendait "+ TDSfct.getNbArgs() + ". Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "semantique");
                } else{
                    List<TDSItemVar> liste = new ArrayList<>();
                    //System.out.println(Integer.toString(nb_args_lus) + " arguments lus");
                    //System.out.println("Liste des types des arguments de la fonction "+ variable_actuelle.getValue());
                    //On récupère l'ensemble des variables qui sont des paramètres de la fonction ayant comme identificateur variable_actuelle.getValue()
                    this.tableDesSymboles.printTDS();
                    TDS loop = this.tableDesSymboles;
                    while (loop.getParent() != null){
                        for (TDSItemFctProc elem : loop.getFonctions().values()){
                            if (elem.getIdentif().equals(variable_actuelle.getValue())){
                                //On récupère les variables de la fonction actuelle
                                TDS fct = elem.getTableFille();
                                for (TDSItemVar item : fct.getVariables().values()){
                                    //System.out.println(item.getIdentif());
                                    List<TDSItemVar> tempList = fct.getVariables().values().stream()
                                    .filter(x -> x.getIsParam() != null && x.getIsParam().equals(variable_actuelle.getValue()))
                                    .sorted(Comparator.comparing(TDSItemVar::getAdresse)) // trier par ordre croissant d'adresse
                                    .collect(Collectors.toList());
                                    liste.addAll(tempList);
                                }
                            }
                        }
                        loop = loop.getParent();
                    }
                    
                    loop = this.tableDesSymboles;
                    while(loop.getParent() != null){
                        //System.out.println(loop.getVariables().values());
                        List<TDSItemVar> tempList = loop.getVariables().values().stream()
                        .filter(x -> x.getIsParam() != null && x.getIsParam().equals(variable_actuelle.getValue()))
                        .sorted(Comparator.comparing(TDSItemVar::getAdresse)) // trier par ordre croissant d'adresse
                        .collect(Collectors.toList());
                        liste.addAll(tempList);
                        loop = loop.getParent();
                    }

                    //On récupère le type de chaque argument, en commencant par le dernier
                    String param_actuel_type = liste.get(nb_args_lus-1).getType();
                    i=1;
                    int k=1;
                    
                    String nom_fonction = "";
                    while (this.tokens.get(currentTokenIndex-i).getTag() != "(" && !nom_fonction.equals(TDSfct.getIdentif())){
                        Token param = this.tokens.get(currentTokenIndex-i);
                        if(param.getTag() == ","){
                            k++;
                            i++;
                            param_actuel_type = liste.get(nb_args_lus-k).getType();
                            if (this.tokens.get(currentTokenIndex-i) instanceof VarToken) {
                                nom_fonction =(((VarToken)this.tokens.get(currentTokenIndex-i)).getValue());
                            }
                            continue;
                        }
                        else if(param.getTag() == "(" 
                            || param.getTag() == ")" 
                            || param.getTag() == "+" 
                            || param.getTag() == "-" 
                            || param.getTag() == "*" 
                            || param.getTag() == "/" 
                            || param.getTag() == "rem" 
                            || param.getTag() == "not" 
                            || param.getTag() == "and" 
                            || param.getTag() == "or" 
                            || param.getTag() == "xor" 
                            || param.getTag() == "<" 
                            || param.getTag() == "<=" 
                            || param.getTag() == ">" 
                            || param.getTag() == ">=" 
                            || param.getTag() == "=" 
                            || param.getTag() == "/="){
                                i++;
                                if (this.tokens.get(currentTokenIndex-i) instanceof VarToken) {
                                    nom_fonction =(((VarToken)this.tokens.get(currentTokenIndex-i)).getValue());
                                }
                                continue;
                        }
                        else {
                            //System.out.println(param.getTag());
                            if (param.getTag() == "NUM_CONST"){
                                if (param instanceof FloatToken){
                                    if (!param_actuel_type.equals("float")){
                                        printErrorAndSkip("L'argument "+ liste.get(nb_args_lus-k).getIdentif() +" doit être de type "+ param_actuel_type +" alors que la valeur passée contient un type float. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "semantique");
                                    }
                                }
                                else{
                                    if (!param_actuel_type.equals("integer")){
                                        printErrorAndSkip("L'argument "+ liste.get(nb_args_lus-k).getIdentif() +" doit être de type "+ param_actuel_type +" alors que la valeur passée contient un type integer. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "semantique");
                                    }
                                }
                            }
                            else if (param.getTag() == "STR_CONST"){
                                if (param instanceof StringToken){
                                    if (!param_actuel_type.equals("string")){
                                        printErrorAndSkip("L'argument "+ liste.get(nb_args_lus-k).getIdentif() +" doit être de type "+ param_actuel_type +" alors que la valeur passée contient un type string. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "semantique");
                                    }
                                }
                                else{
                                    if (!param_actuel_type.equals("character")){
                                        printErrorAndSkip("L'argument "+ liste.get(nb_args_lus-k).getIdentif() +" doit être de type "+ param_actuel_type +" alors que la valeur passée contient un type character. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "semantique");
                                    }
                                }
                            }
                            else if (param.getTag() == "true" || param.getTag() == "false"){
                                if (!param_actuel_type.equals("boolean")){
                                    printErrorAndSkip("L'argument "+ liste.get(nb_args_lus-k).getIdentif() +" doit être de type "+ param_actuel_type +" alors que la valeur passée contient un type boolean. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "semantique");
                                }
                            }
                            else if (param.getTag() == "VARIABLE"){
                                //System.out.println(param.getTag());
                                TDSItemVar TDSvar2 = findVariableDeclaration(((VarToken) param).getValue());
                                TDSItemFctProc TDSfct2 = findFunctionDeclaration(((VarToken) param).getValue());
                                if (TDSvar2 != null){
                                    if (!TDSvar2.getType().equals(param_actuel_type)){
                                        printErrorAndSkip("L'argument "+ liste.get(nb_args_lus-k).getIdentif() +" doit être de type "+ param_actuel_type +" alors que la valeur passée contient un type "+ TDSvar2.getType() + ". Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "semantique");
                                    }
                                } else if (TDSfct2 != null){
                                    if (!TDSfct2.getType().equals(param_actuel_type)){
                                        printErrorAndSkip("L'argument "+ liste.get(nb_args_lus-k).getIdentif() +" doit être de type "+ param_actuel_type +" alors que la valeur passée contient un type "+ TDSfct2.getType() + ". Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "semantique");
                                    }
                                }
                            }         
                        }
                        if (this.tokens.get(currentTokenIndex-i) instanceof VarToken) {
                            nom_fonction =(((VarToken)this.tokens.get(currentTokenIndex-i)).getValue());
                        }
                        i++;
                    }
                }
            }
            return;    
        } else{
            printErrorAndSkip("identificateur ou entier ou chaine ou 'true' ou 'false' ou 'null' ou '(' ou 'not' ou '-' ou 'character val'attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
        }
    }

    public void axiomeRecur(TreeNode pere) throws ExceptionSyntaxique, ExceptionSemantique {
        System.out.print("axiomeRecur : ");
        //Lignes de debug
        //System.out.println("token actuel "+ this.tokens.get(currentTokenIndex).getTag());
        //System.out.println("token suivant "+ this.tokens.get(currentTokenIndex+1).getTag());
        //System.out.println("token précédent "+ this.tokens.get(currentTokenIndex-1).getTag());
        //System.out.println("token index "+ currentTokenIndex);
        this.rulesName.add("axiomeRecur");
        if (tokenTag() == "DOT"){
            //LL(3) ici

            advanceToken();
            if (tokenTag() == "VARIABLE"){

                advanceToken();
                if(tokenTag() == ":"){
                    System.out.println("r68");
                    decrementToken();
                    decrementToken();
                    return;
                }
                else{

                    // On ajoute un noeud.
                    TreeNode noeud = new TreeNode("VARIABLE");
                    pere.addSon(noeud);

                    System.out.println("r69");
                    axiomeRecur(noeud);
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
                || tokenTag() == "or"
                || tokenTag() == "or_else"
                || tokenTag() == "and"
                || tokenTag() == "and_then"
                || tokenTag() == ";"
                || tokenTag() == ")"
                || tokenTag() == "loop"
                || tokenTag() == "then"
                || tokenTag() == "DOTDOT"
                || tokenTag() == ","){
            System.out.println("r69");
            return;
        }
        else{
            printErrorAndSkip("')' ou '.' ou '=' ou '/=' ou '<' ou '<=' ou '>' ou '>=' ou '+' ou '-' ou '*' ou '/' ou 'rem' ou '+' ou '-' ou ';' ou ')' ou 'loop' ou 'then' ou '..' ou ',' ou 'or' ou 'or_else' ou 'and' ou 'and_then' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
        }

    }

    public void axiome1(TreeNode pere) throws ExceptionSyntaxique, ExceptionSemantique {

        System.out.print("axiome1 : ");
        this.rulesName.add("axiome1");
        //System.out.println(tokenTag());
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
                || tokenTag() == "or"
                || tokenTag() == "or_else"
                || tokenTag() == "and"
                || tokenTag() == "and_then"
                || tokenTag() == ";"
                || tokenTag() == ")"
                || tokenTag() == "loop"
                || tokenTag() == "then"
                || tokenTag() == "DOTDOT"
                || tokenTag() == ","){
            System.out.println("r70");
            axiomeRecur(pere);
            return;

        }
        else if (tokenTag() == "("){

            TreeNode arg = new TreeNode("ARGUMENT");
            pere.addSon(arg);

            System.out.println("r71");
            advanceToken();
            expressionPlusVirgule(arg);
            if (tokenTag() == ")"){

                advanceToken();
                axiomeRecur(pere);
                return;
            }
            else{
                printErrorAndSkip("')' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
            }
        } else{
            printErrorAndSkip("')' ou '.' ou '=' ou '/=' ou '<' ou '<=' ou '>' ou '>=' ou '+' ou '-' ou '*' ou '/' ou 'rem' ou '+' ou '-' ou ';' ou ')' ou 'loop' ou 'then' ou '..' ou ',' ou 'or' ou 'or_else' ou 'and' ou 'and_then' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
        }
    }

    public void instruction(TreeNode pere) throws ExceptionSyntaxique, ExceptionSemantique {

        TreeNode affect = new TreeNode("AFFECT");

        System.out.print("instruction : ");
        this.rulesName.add("instruction");
        if (tokenTag() == "VARIABLE"){
            System.out.println("r72");
            VarToken membre_gauche = (VarToken) this.tokens.get(currentTokenIndex);
            //On regarde si le membre gauche est bien définie dans la TDS
            TDSItemVar TDSvar_membre_gauche = findVariableDeclaration(membre_gauche.getValue());
            TDSItemFctProc TDSfct = findFunctionDeclaration(membre_gauche.getValue());
            if (TDSvar_membre_gauche == null && TDSfct == null){
                printErrorAndSkip("La variable "+ membre_gauche.getValue() +" n'a pas été déclarée. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "semantique");
            }
            // On ajoute un noeud.
            TreeNode noeud_bis = new TreeNode(((VarToken) getNextToken()).getValue());
            TreeNode noeud2 = new TreeNode("");

            
            advanceToken();
            instruction1(noeud2);

            noeud2.addFirstSon(noeud_bis);
            pere.addSon(noeud2);

            return;
        }
        else if (tokenTag() == "-"){
            System.out.println("r73");
            pere.addSon(affect);
            // On ajoute un noeud.
            TreeNode minus = new TreeNode(tokenTag());
            TreeNode left1 = new TreeNode("");
            TreeNode left2 = new TreeNode("");
            TreeNode left3 = new TreeNode("");
            TreeNode left4 = new TreeNode("");
            TreeNode left5 = new TreeNode("");
            TreeNode left6 = new TreeNode("");
            affect.addSon(minus);

            advanceToken();
            axiome(minus);
            operation2(left1);
            operation1(left2);
            comparaison2(left3);
            comparaison1(left4);
            conjonction(left5);
            disjonction(left6);

            left1.getSons().get(0).addFirstSon(minus);
            left2.getSons().get(0).addFirstSon(left1.getSons().get(0));
            left3.getSons().get(0).addFirstSon(left2.getSons().get(0));
            left4.getSons().get(0).addFirstSon(left3.getSons().get(0));
            left5.getSons().get(0).addFirstSon(left4.getSons().get(0));
            left6.getSons().get(0).addFirstSon(left5.getSons().get(0));
            affect.addSon(left6.getSons().get(0));


            if (tokenTag() == "DOT"){

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
                                printErrorAndSkip("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
                            }
                        } else{
                            printErrorAndSkip("'=' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
                        }
                    } else{
                        printErrorAndSkip("':' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
                    }
                } else{
                    printErrorAndSkip("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
                }
            } else{
                printErrorAndSkip("'.' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
            }
        }
        else if (tokenTag() == "not"){
            System.out.println("r74");
            // On ajoute un noeud.
            pere.addSon(affect);
            // On ajoute un noeud.
            TreeNode nott = new TreeNode(tokenTag());
            TreeNode left1 = new TreeNode("");
            TreeNode left2 = new TreeNode("");
            TreeNode left3 = new TreeNode("");
            TreeNode left4 = new TreeNode("");
            TreeNode left5 = new TreeNode("");
            TreeNode left6 = new TreeNode("");
            affect.addSon(nott);

            advanceToken();
            axiome(nott);
            operation2(left1);
            operation1(left2);
            comparaison2(left3);
            comparaison1(left4);
            conjonction(left5);
            disjonction(left6);

            left1.getSons().get(0).addFirstSon(nott);
            left2.getSons().get(0).addFirstSon(left1.getSons().get(0));
            left3.getSons().get(0).addFirstSon(left2.getSons().get(0));
            left4.getSons().get(0).addFirstSon(left3.getSons().get(0));
            left5.getSons().get(0).addFirstSon(left4.getSons().get(0));
            left6.getSons().get(0).addFirstSon(left5.getSons().get(0));
            affect.addSon(left6.getSons().get(0));
            if (tokenTag() == "DOT"){

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
                                printErrorAndSkip("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
                            }
                        } else{
                            printErrorAndSkip("'=' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
                        }
                    } else{
                        printErrorAndSkip("':' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
                    }
                } else{
                    printErrorAndSkip("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
                }
            } else{
                printErrorAndSkip("'.' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
            }
        }
        else if (tokenTag() == "NUM_CONST"){
            System.out.println("r75");
            // On ajoute un noeud.
            pere.addSon(affect);

            TreeNode num;
            if (getNextToken() instanceof IntegerToken){
                num = new TreeNode(Integer.toString(((IntegerToken) getNextToken()).getValue()));
            } else{
                num = new TreeNode(Float.toString(((FloatToken) getNextToken()).getValue()));
            }
            TreeNode left1 = new TreeNode("");
            TreeNode left2 = new TreeNode("");
            TreeNode left3 = new TreeNode("");
            TreeNode left4 = new TreeNode("");
            TreeNode left5 = new TreeNode("");
            TreeNode left6 = new TreeNode("");
            TreeNode left7 = new TreeNode("");

            advanceToken();
            axiomeRecur(left1);
            operation2(left2);
            operation1(left3);
            comparaison2(left4);
            comparaison1(left5);
            conjonction(left6);
            disjonction(left7);

            left1.getSons().get(0).addFirstSon(num);
            left2.getSons().get(0).addFirstSon(left1.getSons().get(0));
            left3.getSons().get(0).addFirstSon(left2.getSons().get(0));
            left4.getSons().get(0).addFirstSon(left3.getSons().get(0));
            left5.getSons().get(0).addFirstSon(left4.getSons().get(0));
            left6.getSons().get(0).addFirstSon(left5.getSons().get(0));
            left7.getSons().get(0).addFirstSon(left6.getSons().get(0));
            affect.addSon(left7.getSons().get(0));

            if(tokenTag() == "DOT"){
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
                                printErrorAndSkip("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
                            }
                        } else{
                            printErrorAndSkip("'=' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
                        }
                    } else{
                        printErrorAndSkip("':' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
                    }
                } else{
                    printErrorAndSkip("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
                }
            } else{
                printErrorAndSkip("'.' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
            }
        }
        else if(tokenTag() == "STR_CONST"){
            System.out.println("r76");
            // On ajoute un noeud.
            pere.addSon(affect);

            TreeNode character;
            if (getNextToken() instanceof IntegerToken){
                character = new TreeNode(Integer.toString(((IntegerToken) getNextToken()).getValue()));
            } else{
                character = new TreeNode(Float.toString(((FloatToken) getNextToken()).getValue()));
            }
            TreeNode left1 = new TreeNode("");
            TreeNode left2 = new TreeNode("");
            TreeNode left3 = new TreeNode("");
            TreeNode left4 = new TreeNode("");
            TreeNode left5 = new TreeNode("");
            TreeNode left6 = new TreeNode("");
            TreeNode left7 = new TreeNode("");

            advanceToken();
            axiomeRecur(left1);
            operation2(left2);
            operation1(left3);
            comparaison2(left4);
            comparaison1(left5);
            conjonction(left6);
            disjonction(left7);

            left1.getSons().get(0).addFirstSon(character);
            left2.getSons().get(0).addFirstSon(left1.getSons().get(0));
            left3.getSons().get(0).addFirstSon(left2.getSons().get(0));
            left4.getSons().get(0).addFirstSon(left3.getSons().get(0));
            left5.getSons().get(0).addFirstSon(left4.getSons().get(0));
            left6.getSons().get(0).addFirstSon(left5.getSons().get(0));
            left7.getSons().get(0).addFirstSon(left6.getSons().get(0));
            affect.addSon(left7.getSons().get(0));

            if(tokenTag() == "DOT"){
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
                                printErrorAndSkip("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
                            }
                        } else{
                            printErrorAndSkip("'=' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
                        }
                    } else{
                        printErrorAndSkip("':' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
                    }
                } else{
                    printErrorAndSkip("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
                }
            } else{
                printErrorAndSkip("'.' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
            }
        }
        else if(tokenTag() == "true"){
            System.out.println("r77");
            // On ajoute un noeud.
            pere.addSon(affect);

            TreeNode tru = new TreeNode(tokenTag());
            TreeNode left1 = new TreeNode("");
            TreeNode left2 = new TreeNode("");
            TreeNode left3 = new TreeNode("");
            TreeNode left4 = new TreeNode("");
            TreeNode left5 = new TreeNode("");
            TreeNode left6 = new TreeNode("");
            TreeNode left7 = new TreeNode("");

            advanceToken();
            axiomeRecur(left1);
            operation2(left2);
            operation1(left3);
            comparaison2(left4);
            comparaison1(left5);
            conjonction(left6);
            disjonction(left7);

            left1.getSons().get(0).addFirstSon(tru);
            left2.getSons().get(0).addFirstSon(left1.getSons().get(0));
            left3.getSons().get(0).addFirstSon(left2.getSons().get(0));
            left4.getSons().get(0).addFirstSon(left3.getSons().get(0));
            left5.getSons().get(0).addFirstSon(left4.getSons().get(0));
            left6.getSons().get(0).addFirstSon(left5.getSons().get(0));
            left7.getSons().get(0).addFirstSon(left6.getSons().get(0));
            affect.addSon(left7.getSons().get(0));

            if(tokenTag() == "DOT"){
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
                                printErrorAndSkip("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
                            }
                        } else{
                            printErrorAndSkip("'=' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
                        }
                    } else{
                        printErrorAndSkip("':' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
                    }
                } else{
                    printErrorAndSkip("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
                }
            } else{
                printErrorAndSkip("'.' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
            }
        }
        else if(tokenTag() == "false"){
            System.out.println("r78");
            // On ajoute un noeud.
            pere.addSon(affect);

            TreeNode fals = new TreeNode(tokenTag());
            TreeNode left1 = new TreeNode("");
            TreeNode left2 = new TreeNode("");
            TreeNode left3 = new TreeNode("");
            TreeNode left4 = new TreeNode("");
            TreeNode left5 = new TreeNode("");
            TreeNode left6 = new TreeNode("");
            TreeNode left7 = new TreeNode("");

            advanceToken();
            axiomeRecur(left1);
            operation2(left2);
            operation1(left3);
            comparaison2(left4);
            comparaison1(left5);
            conjonction(left6);
            disjonction(left7);

            left1.getSons().get(0).addFirstSon(fals);
            left2.getSons().get(0).addFirstSon(left1.getSons().get(0));
            left3.getSons().get(0).addFirstSon(left2.getSons().get(0));
            left4.getSons().get(0).addFirstSon(left3.getSons().get(0));
            left5.getSons().get(0).addFirstSon(left4.getSons().get(0));
            left6.getSons().get(0).addFirstSon(left5.getSons().get(0));
            left7.getSons().get(0).addFirstSon(left6.getSons().get(0));
            affect.addSon(left7.getSons().get(0));

            if(tokenTag() == "DOT"){
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
                                printErrorAndSkip("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
                            }
                        } else{
                            printErrorAndSkip("'=' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
                        }
                    } else{
                        printErrorAndSkip("':' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
                    }
                } else{
                    printErrorAndSkip("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
                }
            } else{
                printErrorAndSkip("'.' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
            }
        }
        else if(tokenTag() == "null"){
            System.out.println("r79");
            // On ajoute un noeud.
            pere.addSon(affect);

            TreeNode nul = new TreeNode(tokenTag());
            TreeNode left1 = new TreeNode("");
            TreeNode left2 = new TreeNode("");
            TreeNode left3 = new TreeNode("");
            TreeNode left4 = new TreeNode("");
            TreeNode left5 = new TreeNode("");
            TreeNode left6 = new TreeNode("");
            TreeNode left7 = new TreeNode("");

            advanceToken();
            axiomeRecur(left1);
            operation2(left2);
            operation1(left3);
            comparaison2(left4);
            comparaison1(left5);
            conjonction(left6);
            disjonction(left7);

            left1.getSons().get(0).addFirstSon(nul);
            left2.getSons().get(0).addFirstSon(left1.getSons().get(0));
            left3.getSons().get(0).addFirstSon(left2.getSons().get(0));
            left4.getSons().get(0).addFirstSon(left3.getSons().get(0));
            left5.getSons().get(0).addFirstSon(left4.getSons().get(0));
            left6.getSons().get(0).addFirstSon(left5.getSons().get(0));
            left7.getSons().get(0).addFirstSon(left6.getSons().get(0));
            affect.addSon(left7.getSons().get(0));

            if(tokenTag() == "DOT"){
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
                                printErrorAndSkip("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
                            }
                        } else{
                            printErrorAndSkip("'=' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
                        }
                    } else{
                        printErrorAndSkip("':' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
                    }
                } else{
                    printErrorAndSkip("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
                }
            } else{
                printErrorAndSkip("'.' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
            }
        }
        else if (tokenTag() == "character'val"){
            System.out.println("r80");
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
                    TreeNode left5 = new TreeNode("");
                    TreeNode left6 = new TreeNode("");
                    TreeNode left7 = new TreeNode("");

                    advanceToken();
                    axiomeRecur(left1);
                    operation2(left2);
                    operation1(left3);
                    comparaison2(left4);
                    comparaison1(left5);
                    conjonction(left6);
                    disjonction(left7);

                    left1.getSons().get(0).addFirstSon(characterVAL);
                    left2.getSons().get(0).addFirstSon(left1.getSons().get(0));
                    left3.getSons().get(0).addFirstSon(left2.getSons().get(0));
                    left4.getSons().get(0).addFirstSon(left3.getSons().get(0));
                    left5.getSons().get(0).addFirstSon(left4.getSons().get(0));
                    left6.getSons().get(0).addFirstSon(left5.getSons().get(0));
                    left7.getSons().get(0).addFirstSon(left6.getSons().get(0));
                    affect.addSon(left7.getSons().get(0));
                    if(tokenTag() == "DOT"){
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
                                        printErrorAndSkip("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
                                    }
                                } else{
                                    printErrorAndSkip("'=' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
                                }
                            } else{
                                printErrorAndSkip("':' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
                            }
                        } else{
                            printErrorAndSkip("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
                        }
                    } else{
                        printErrorAndSkip("'.' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
                    }
                } else{
                    printErrorAndSkip("')' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
                }
            } else{
                printErrorAndSkip("'(' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
            }
        }
        else if (tokenTag() == "("){
            System.out.println("r81");
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
                TreeNode left5 = new TreeNode("");
                TreeNode left6 = new TreeNode("");
                TreeNode left7 = new TreeNode("");

                advanceToken();
                axiomeRecur(left1);
                operation2(left2);
                operation1(left3);
                comparaison2(left4);
                comparaison1(left5);
                conjonction(left6);
                disjonction(left7);

                left1.getSons().get(0).addFirstSon(prio);
                left2.getSons().get(0).addFirstSon(left1.getSons().get(0));
                left3.getSons().get(0).addFirstSon(left2.getSons().get(0));
                left4.getSons().get(0).addFirstSon(left3.getSons().get(0));
                left5.getSons().get(0).addFirstSon(left4.getSons().get(0));
                left6.getSons().get(0).addFirstSon(left5.getSons().get(0));
                left7.getSons().get(0).addFirstSon(left6.getSons().get(0));
                affect.addSon(left7.getSons().get(0));

                if(tokenTag() == "DOT"){
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
                                    printErrorAndSkip("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
                                }
                            } else{
                                printErrorAndSkip("'=' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
                            }
                        } else{
                            printErrorAndSkip("':' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
                        }
                    } else{
                        printErrorAndSkip("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
                    }
                } else{
                    printErrorAndSkip("'.' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
                }
            } else{
                printErrorAndSkip("')' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
            }
        }
        else if(tokenTag() == "return"){
            // On ajoute un noeud.
            TreeNode ret = new TreeNode(tokenTag());
            pere.addSon(ret);

            System.out.println("r82");
            advanceToken();
            instruction2(ret);
            return;
        }
        else if(tokenTag() == "begin"){

            System.out.println("r83");
            advanceToken();
            instructionPlus(pere);
            if(tokenTag() == "end"){
                advanceToken();
                if(tokenTag() == ";"){
                    advanceToken();
                    return;
                }
                else{
                    printErrorAndSkip("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
                }
            }
            else{
                printErrorAndSkip("'end' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
            }
        }
        else if(tokenTag() == "if"){
            // On ajoute un noeud.
            TreeNode nodeIf = new TreeNode(tokenTag());
            pere.addSon(nodeIf);

            System.out.println("r84");
            advanceToken();
            expression(nodeIf);
            
            if(tokenTag()=="then"){

                advanceToken();
                instructionPlus(nodeIf);
                instruction3(nodeIf);
                return;
            }
            else{
                printErrorAndSkip("'then' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
            }
        }
        else if(tokenTag() == "for"){
            // On ajoute un noeud.
            TreeNode nodeFor = new TreeNode(tokenTag());
            pere.addSon(nodeFor);

            System.out.println("r85");
            advanceToken();
            if (tokenTag() == "VARIABLE") {
                // On ajoute un noeud.
                nodeFor.addSon(new TreeNode(((VarToken) getNextToken()).getValue()));
                tableDesSymboles.addVariable(((VarToken) getNextToken()).getValue(), "integer", 16,"false", "false");
                advanceToken();
            } else {
                printErrorAndSkip("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
            }
            if (tokenTag() == "in") {

                advanceToken();
            } else {
                printErrorAndSkip("'in' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
            }
            instruction4(nodeFor);
        }
        else if(tokenTag() == "while"){
            // On ajoute un noeud.
            TreeNode nodeWhile = new TreeNode(tokenTag());
            pere.addSon(nodeWhile);

            System.out.println("r86");
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
                            printErrorAndSkip("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
                        }
                    }
                    else{
                        printErrorAndSkip("'loop' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
                    }
                }
                else{
                    printErrorAndSkip("'end' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
                }
            }
            else{
                printErrorAndSkip("'loop' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
            }
        }
        else if (tokenTag() == "put") {
            // On ajoute un noeud.
            TreeNode nodePut = new TreeNode(tokenTag());
            pere.addSon(nodePut);

            System.out.println("r87");
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
                        printErrorAndSkip("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
                    }
                } else {
                    printErrorAndSkip("')' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
                }
            } else {
                printErrorAndSkip("'(' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
            }
        }
        else{
            printErrorAndSkip("identificateur ou entier ou chaine ou 'true' ou 'false' ou 'null' ou '(' ou 'not' ou '-' ou 'character'val' ou 'return' ou 'begin' ou 'if' ou 'for' ou 'while' ou 'put' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
        }
    }

    public void instruction1(TreeNode pere) throws ExceptionSyntaxique, ExceptionSemantique {
        System.out.print("instruction1 : ");
        if (tokenTag() == ":"){
            pere.setLabel("AFFECT");

            System.out.println("r88");
            advanceToken();
            if (tokenTag() == "="){

                advanceToken();
                expression(pere);
                if (tokenTag() == ";"){

                    advanceToken();
                    return;
                } else{
                    printErrorAndSkip("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
                }
            } else{
                printErrorAndSkip("'=' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
            }
        }
        else if (tokenTag() == ";"){

            pere.setLabel("APPEL");

            System.out.println("r89");
            advanceToken();
            return;
        }
        else if (tokenTag() == "("){

            pere.setLabel("APPEL");

            System.out.println("r90");
            advanceToken();
            expressionPlusVirgule(pere);
            if (tokenTag() == ")"){

                advanceToken();
                instruction11(pere);
                return;
            } else{
                printErrorAndSkip("')' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
            }
        }
        else if (tokenTag() == "DOT"){
            //Ici on est sur du LL(2)
            advanceToken();
            if(tokenTag() == "VARIABLE"){
                advanceToken();
                if(tokenTag() == ":"){
                    decrementToken();
                    decrementToken();
                    pere.setLabel("AFFECT");
                    System.out.println("r92");
                    if (tokenTag() == "DOT"){
                        advanceToken();
                        if (tokenTag() == "VARIABLE"){
                            // On regarde si la variable est bien déclarée comme attribut
                            TDSItemVar TDSattr = findVariableDeclaration(((VarToken) this.tokens.get(currentTokenIndex)).getValue());
                            String structName = ((VarToken) this.tokens.get(currentTokenIndex-2)).getValue();
                            String structType = findVariableDeclaration(structName).getType();
                            if (TDSattr == null || !TDSattr.getIsAttribut().equals(structType)){
                                printErrorAndSkip("La variable "+ ((VarToken) this.tokens.get(currentTokenIndex)).getValue() +" n'a pas été déclarée comme attribut de la structure : "+structType+". Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "semantique");
                            }
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
                                        printErrorAndSkip("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
                                    }
                                } else{
                                    printErrorAndSkip("'=' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
                                }
                            } else{
                                printErrorAndSkip("':' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
                            }
            
                        } else{
                            printErrorAndSkip("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
                        }
                    } else{
                        printErrorAndSkip("'.' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
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
                || tokenTag() == "DOT"){
                    decrementToken();
                    decrementToken();
                    pere.setLabel("AFFECT");
                    System.out.println("r91");
                    if (tokenTag() == "DOT"){
                        pere.setLabel("APPEL");
                        advanceToken();
                        if (tokenTag() == "VARIABLE"){
                            // On ajoute un noeud.
                            pere.addSon(new TreeNode(((VarToken) getNextToken()).getValue()));

                            advanceToken();
                            operation2(pere);
                            operation1(pere);
                            comparaison2(pere);
                            comparaison1(pere);
                            conjonction(pere);
                            disjonction(pere);
                            if (tokenTag() == "DOT"){

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
                                                printErrorAndSkip("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
                                            }
                                        } else{
                                            printErrorAndSkip("'=' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
                                        }
                                    } else{
                                        printErrorAndSkip("':' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
                                    }
                                } else{
                                    printErrorAndSkip("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
                                }
                            } else{
                                printErrorAndSkip("'.' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
                            }
                        } else{
                            printErrorAndSkip("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
                        }
                    } else {
                        printErrorAndSkip("'.' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
                    }
                } else {
                    printErrorAndSkip("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
                }
            } else {
                printErrorAndSkip("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
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
            operation2(pere);
            operation1(pere);
            comparaison2(pere);
            comparaison1(pere);
            conjonction(pere);
            disjonction(pere);
            if (tokenTag() == "DOT"){

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
                                printErrorAndSkip("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
                            }
                        } else{
                            printErrorAndSkip("'=' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
                        }
                    } else{
                        printErrorAndSkip("':' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
                    }
                } else{
                    printErrorAndSkip("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
                }
            } else{
                printErrorAndSkip("'.' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
            }
        } else {
            printErrorAndSkip("';' ou '.' ou '=' ou '/=' ou '<' ou '<=' ou '>' ou '>=' ou '+' ou '-' ou '*' ou '/' ou 'rem' ou '+' ou '-' ou ';' ou ')' ou 'loop' ou 'then' ou '..' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
        }
    }

    public void instruction11(TreeNode pere) throws ExceptionSyntaxique, ExceptionSemantique {

        System.out.print("instruction11 : ");
        this.rulesName.add("instruction11");
        if (tokenTag() == ";"){
            System.out.println("r93");
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
            System.out.println("r94");
            TreeNode affect = new TreeNode("AFFECT");
            TreeNode left1 = new TreeNode("");
            TreeNode left2 = new TreeNode("");
            TreeNode left3 = new TreeNode("");
            TreeNode left4 = new TreeNode("");
            TreeNode left5 = new TreeNode("");
            TreeNode left6 = new TreeNode("");
            TreeNode left7 = new TreeNode("");
            axiomeRecur(left1);
            operation2(left2);
            operation1(left3);
            comparaison2(left4);
            comparaison1(left5);
            conjonction(left6);
            disjonction(left7);

            left1.getSons().get(0).addFirstSon(affect);
            left2.getSons().get(0).addFirstSon(left1.getSons().get(0));
            left3.getSons().get(0).addFirstSon(left2.getSons().get(0));
            left4.getSons().get(0).addFirstSon(left3.getSons().get(0));
            left5.getSons().get(0).addFirstSon(left4.getSons().get(0));
            left6.getSons().get(0).addFirstSon(left5.getSons().get(0));
            left7.getSons().get(0).addFirstSon(left6.getSons().get(0));
            affect.addSon(left7.getSons().get(0));
            pere.addSon(affect);
 
            left1.getSons().get(0).addFirstSon(affect);
            left2.getSons().get(0).addFirstSon(left1.getSons().get(0));
            left3.getSons().get(0).addFirstSon(left2.getSons().get(0));
            left4.getSons().get(0).addFirstSon(left3.getSons().get(0));
            affect.addSon(left4.getSons().get(0));

            if (tokenTag() == "DOT"){
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
                                printErrorAndSkip("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
                            }
                        } else{
                            printErrorAndSkip("'=' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
                        }
                    } else{
                        printErrorAndSkip("':' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
                    }
                } else{
                    printErrorAndSkip("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
                }
            } else{
                printErrorAndSkip("'.' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
            }
        } else {
            printErrorAndSkip("';' ou '.' ou '=' ou '/=' ou '<' ou '<=' ou '>' ou '>=' ou '+' ou '-' ou '*' ou '/' ou 'rem' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
        }
    }

    public void instruction2(TreeNode pere) throws ExceptionSyntaxique, ExceptionSemantique {
        System.out.print("instruction2 : ");
        this.rulesName.add("instruction2");
        if (tokenTag() == ";"){
            System.out.println("r96");
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
            System.out.println("r95");
            expression(pere);
            if (tokenTag() == ";") {
                advanceToken();
            } else {
                printErrorAndSkip("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
            }
        }
        else{
            printErrorAndSkip("identificateur ou entier ou chaine ou 'true' ou 'false' ou 'null' ou '(' ou 'not' ou '-' ou 'new' ou 'character val'ou ';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
        }
    }

    public void instruction3(TreeNode pere) throws ExceptionSyntaxique, ExceptionSemantique {
        System.out.print("instruction3 : ");
        this.rulesName.add("instruction3");
        if (tokenTag() == "end"){
            System.out.println("r97");
            advanceToken();
            if (tokenTag() == "if") {
                advanceToken();
            } else {
                printErrorAndSkip("'if' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
            }
            if (tokenTag() == ";") {
                advanceToken();
                return;
            } else {
                printErrorAndSkip("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
            }
        }
        else if (tokenTag() == "else"){
            // On ajoute un noeud.
            TreeNode elsse = new TreeNode(tokenTag());
            pere.addSon(elsse);

            System.out.println("r98");
            advanceToken();
            instructionPlus(elsse);
            if (tokenTag() == "end"){
                advanceToken();
                if (tokenTag() == "if") {
                    advanceToken();
                } else {
                    printErrorAndSkip("'if' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
                }
                if (tokenTag() == ";") {
                    advanceToken();
                    return;
                } else {
                    printErrorAndSkip("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
                }
            }
            else{
                printErrorAndSkip("'end' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
            }
        } else if (tokenTag() == "elsif"){
            System.out.println("r99");
            elsifPlus(pere);
            instruction3(pere);
        } else{
            printErrorAndSkip("'else' ou 'end' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
        }
    }

    public void instruction4(TreeNode pere) throws ExceptionSyntaxique, ExceptionSemantique {
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
            System.out.println("r100");

            // On ajoute un noeud.
            TreeNode range1 = new TreeNode("range1");
            TreeNode range2 = new TreeNode("range2");
            expression(range1);
            //On ajoute l'expression à son père

            if (tokenTag() == "DOTDOT") {
                advanceToken();
            } else {
                printErrorAndSkip("'..' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
            }
            expression(range2);
            TreeNode range = new TreeNode("range");
            range.addSon(range1.getLastSon());
            range.addSon(range2.getLastSon());
            pere.addSon(range);

            if (tokenTag() == "loop") {
                advanceToken();
            } else {
                printErrorAndSkip("'loop' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
            }
            instructionPlus(pere);
            if (tokenTag() == "end") {
                advanceToken();
            } else {
                printErrorAndSkip("'end' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
            }
            if (tokenTag() == "loop") {
                advanceToken();
            } else {
                printErrorAndSkip("'loop' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
            }
            if (tokenTag() == ";") {
                advanceToken();
                return;
            } else {
                printErrorAndSkip("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
            }
        } else if(tokenTag() == "reverse")
        {
            // On ajoute un noeud.
            TreeNode reverserange = new TreeNode("reversed_range");

            System.out.println("r101");
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
                    printErrorAndSkip("'..' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
                }
                expression(reverserange);
                if (tokenTag() == "loop") {
                    advanceToken();
                } else {
                    printErrorAndSkip("'loop' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
                }
                instructionPlus(pere);
                if (tokenTag() == "end") {
                    advanceToken();
                } else {
                    printErrorAndSkip("'end' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
                }
                if (tokenTag() == "loop") {
                    advanceToken();
                } else {
                    printErrorAndSkip("'loop' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
                }
                if (tokenTag() == ";") {
                    advanceToken();
                    return;
                } else {
                    printErrorAndSkip("';' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
                }
            }
            else{
                printErrorAndSkip("identificateur ou entier ou chaine ou 'true' ou 'false' ou 'null' ou '(' ou 'not' ou '-' ou 'new' ou 'character val'attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
            }
        } else{
            printErrorAndSkip("identificateur ou entier ou chaine ou 'true' ou 'false' ou 'null' ou '(' ou 'not' ou '-' ou 'new' ou 'character val'attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
        }
    }

    public void instructionPlus(TreeNode pere) throws ExceptionSyntaxique, ExceptionSemantique {
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
            System.out.println("r102");
            instruction(pere);
            instructionPlus1(pere);
        } else {
            printErrorAndSkip("identificateur ou entier ou chaine ou 'true' ou 'false' ou 'null' ou '(' ou 'not' ou '-' ou 'new' ou 'character val'ou 'begin' ou 'if' ou 'for' ou 'while' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
        }
    }

    public void instructionPlus1(TreeNode pere) throws ExceptionSyntaxique, ExceptionSemantique {
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
            System.out.println("r103");
            instructionPlus(pere);
        } else {
            System.out.println("r104");
            return;
        }
    }

    public void declarationPlus(TreeNode pere) throws ExceptionSyntaxique, ExceptionSemantique {
        System.out.print("declarationPlus : ");
        this.rulesName.add("declarationPlus");
        if (tokenTag() == "type" || tokenTag() == "procedure" || tokenTag() == "function" || tokenTag() == "VARIABLE") {
            System.out.println("r105");
            declaration(pere);

            if (pere.getLastSon().getSons().size() == 2){
                //On regarde si le membre gauche est bien définie dans la TDS
                TDSItemVar TDSvar_membre_gauche = findVariableDeclaration(pere.getLastSon().getFirstSon().getLabel());
                TreeNode right = pere.getLastSon().getLastSon().getLastSon();
                if (TDSvar_membre_gauche != null){
                    //compatibleTypes(TDSvar_membre_gauche, right);
                }
            }

            declarationPlus1(pere);
        } else {
            printErrorAndSkip("'type', 'procedure', 'function' ou identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
        }
    }

    public void declarationPlus1(TreeNode pere) throws ExceptionSyntaxique, ExceptionSemantique {
        System.out.print("declarationPlus1 : ");
        this.rulesName.add("declarationPlus1");
        if (tokenTag() == "type" || tokenTag() == "procedure" || tokenTag() == "function" || tokenTag() == "VARIABLE") {
            System.out.println("r106");
            declarationPlus(pere);
        } else {
            System.out.println("r107");
            return;
        }
   
    }


    public void champsPlus(TreeNode pere) throws ExceptionSyntaxique, ExceptionSemantique {
        System.out.print("champsPlus : ");
        this.rulesName.add("champsPlus");
        if (tokenTag() == "VARIABLE"){
            System.out.println("r108");
            champs(pere);
            champsPlus1(pere);
        }
        else{
            printErrorAndSkip("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
        }
    }

    public void champsPlus1(TreeNode pere) throws ExceptionSyntaxique, ExceptionSemantique {
        System.out.print("champsPlus1 : ");
        this.rulesName.add("champsPlus1");
        if (tokenTag() == "VARIABLE"){
            System.out.println("r109");
            champsPlus(pere);
        }
        else{
            System.out.println("r110");
            return;
        }
    }



    public void identificateurVirgulePlus(TreeNode pere) throws ExceptionSyntaxique, ExceptionSemantique {
        System.out.print("identificateurVirgulePlus : ");
        this.rulesName.add("identificateurVirgulePlus");
        if (tokenTag() == "VARIABLE") {
            System.out.println("r111");
            // On ajoute un noeud.
            pere.addSon(new TreeNode(((VarToken) getNextToken()).getValue()));
            //On avance le curseur
            advanceToken();
            identificateurVirgulePlus1(pere);
            return;
        } else {
            printErrorAndSkip("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
        }
    }

    public void identificateurVirgulePlus1(TreeNode pere) throws ExceptionSyntaxique, ExceptionSemantique {
        System.out.print("identificateurVirgulePlus1 : ");
        this.rulesName.add("identificateurVirgulePlus1");
        if (tokenTag() == ",") {
            System.out.println("r112");
            advanceToken();
            if (tokenTag() == "VARIABLE") {
                identificateurVirgulePlus(pere);
            } else {
                printErrorAndSkip("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
            }
        } else{
            System.out.println("r113");
            return;
        }
    }

    public void paramPointVirgulePlus(TreeNode pere) throws ExceptionSyntaxique, ExceptionSemantique {
        System.out.print("paramPointVirgulePlus : ");
        this.rulesName.add("paramPointVirgulePlus");
        if (tokenTag() == "VARIABLE") {
            System.out.println("r114");
            param(pere);
            paramPointVirgulePlus1(pere);
        } else {
            printErrorAndSkip("identificateur attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
        }
    }

    public void paramPointVirgulePlus1(TreeNode pere) throws ExceptionSyntaxique, ExceptionSemantique {
        System.out.print("paramPointVirgulePlus1 : ");
        this.rulesName.add("paramPointVirgulePlus1");
        if (tokenTag() == ";") {
            System.out.println("r115");
            advanceToken();
            paramPointVirgulePlus(pere);
        } else {
            System.out.println("r116");
            return;
        }
    }

    public void expressionPlusVirgule(TreeNode pere) throws ExceptionSyntaxique, ExceptionSemantique {
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
            System.out.println("r117");
            expression(pere);
            expressionPlusVirgule1(pere);
        }
        else{
            printErrorAndSkip("identificateur ou entier ou chaine ou 'true' ou 'false' ou 'null' ou '(' ou 'not' ou '-' ou 'new' ou 'character val'attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
        }
    }

    public void expressionPlusVirgule1(TreeNode pere) throws ExceptionSyntaxique, ExceptionSemantique {
        System.out.print("expressionPlusVirgule1 : ");
        this.rulesName.add("expressionPlusVirgule1");
        if (tokenTag() == ","){
            System.out.println("r118");
            advanceToken();
            expressionPlusVirgule(pere);
        }
        else{
            System.out.println("r119");
            return;
        }
    }


    public void elsifPlus(TreeNode pere) throws ExceptionSyntaxique, ExceptionSemantique {
        System.out.print("elsifPlus : ");
        this.rulesName.add("elsifPlus");
        if (tokenTag() == "elsif"){
            // On ajoute un noeud.
            TreeNode elseIfNode = new TreeNode(tokenTag());
            pere.addSon(elseIfNode);

            System.out.println("r120");
            advanceToken();
            expression(elseIfNode);
            if (tokenTag() == "then"){
                advanceToken();
                instructionPlus(elseIfNode);
                elsifPlus1(pere);
            }
            else{
                printErrorAndSkip("'then' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
            }
        }
        else{
            printErrorAndSkip("'elsif' attendu. Dans fichier source, ligne : "  + this.tokens.get(currentTokenIndex).getLine(), "syntaxique");
        }
    }

    public void elsifPlus1(TreeNode pere) throws ExceptionSyntaxique, ExceptionSemantique {
        System.out.print("elsifPlus1 : ");
        this.rulesName.add("elsifPlus1");
        if (tokenTag() == "elsif"){
            System.out.println("r121");
            elsifPlus(pere);
        }
        else{
            System.out.println("r122");
            return;
        }
    }
}
