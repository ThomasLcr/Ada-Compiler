package eu.telecomnancy.application.tds;
import java.util.ArrayList;
import java.util.HashMap;



public class TDS { // Classe représentant la table des symboles
    static int count = 0;
    int id; // Identifiant de la table des symboles
    HashMap<String, TDSItemVar> variables;  // Table de hachage pour les variables
    HashMap<String, TDSItemFctProc> fonctions;  // Table de hachage pour les fonctions ou procédures
    HashMap<String, TDSItemStruct> structures;  // Table de hachage pour les structures
    private int adrVarCourante;                    // Taille occupée par les variables locales
    private int adrArgCourant;                     // Taille occupée par les paramètres de la fonction
    TDS parent;                           // Table des symboles parente
    private int hautPile;                     // Hauteur de la pile

    // Constructeur
    public TDS() {
        this.id = count;
        count += 1;
        this.variables = new HashMap<>();
        this.fonctions = new HashMap<>();
        this.structures = new HashMap<>();
        this.adrVarCourante = 0;
        this.adrArgCourant = 0;
        this.parent = null;
        this.hautPile = 0;
    }

    public TDSItemVar getVariable(String identif) {
        if(variables.containsKey(identif)) {
            return variables.get(identif);
        }else{
            if(parent != null) {
                return parent.getVariable(identif);
            }
        }
        return null;
    }
    /*
     * Get the id of the current TDS
     */
    public int getId() {
        return this.id;
    }
    /*
     * Get the variables of the current TDS
     */
    public HashMap<String, TDSItemVar> getVariables() {
        return this.variables;
    }
    /*
     * Get the functions of the current TDS
     */
    public HashMap<String, TDSItemFctProc> getFonctions() {
        return this.fonctions;
    }
    /*
     * Get the structures of the current TDS
     */
    public HashMap<String, TDSItemStruct> getStructures() {
        return this.structures;
    }

    public TDSItemVar getArgument(int index) {
        ArrayList<TDSItemVar> args = new ArrayList<>();
        for (TDSItemVar var : variables.values()) {
            if (!var.getIsParam().equals("null")) {
                args.add(var);
            }
        }
        return args.get(index);
    }
    /*
     * Set the parent node of the current TDS
     * @param parent the parent node
     */
    public void setParent(TDS parent) {
        this.parent = parent;
    }

    public TDS getParent() {
        return this.parent;
    }

    public void incrementHautPile(int taille) {
        this.hautPile += taille;
    }
    public int getHautPile() {
        return this.hautPile;
    }
    // Méthode pour ajouter une variable à la table des symboles
    public void addVariable(String identif, String type, int taille, String isParam, String isAttribut) {
        type = type.toLowerCase();
        if (isAttribut != null)
                isAttribut = isAttribut.toLowerCase();
        Integer deplacement = 0;
        switch (type) {
            case "integer":
                deplacement = hautPile;
                taille = 16;
                break;
            case "string":
                deplacement = hautPile;
                taille = 256;
                break;
            case "float":
                deplacement = hautPile;
                taille = 16;
                break;
            case "boolean":
                deplacement = hautPile;
                taille = 16;
                break;
            case "character":
                deplacement = hautPile;
                taille = 16;
                break;
            default:
                break;
        }
        incrementHautPile(taille);
        TDSItemVar variable = new TDSItemVar(identif, type, taille, this, isParam, isAttribut, adrVarCourante,deplacement);
        variables.put(identif, variable);
        adrVarCourante += 1;
    }

    // Méthode pour ajouter une fonction à la table des symboles
    public void addFunctionOrProcedure(String identif, int nbArgs, TDS table, String type) {
        TDSItemFctProc fonction = new TDSItemFctProc(identif, nbArgs, table, type);
        fonctions.put(identif, fonction);
    }

    // Méthode pour ajouter une structure à la table des symboles
    public void addItemStruct(String identif, int nbAttributs) {
        TDSItemStruct structure = new TDSItemStruct(identif.toLowerCase(), nbAttributs);
        structures.put(identif, structure);
    }

    //Méthode pour afficher la table des symboles
    public void printTDS() {
        System.out.println("\nDébut table "+ this.id);
        System.out.println("Variables :");
        for (String key : variables.keySet()) {
            TDSItemVar var = variables.get(key);
            System.out.println("Nom : " + var.getIdentif() + " | Type : " + var.getType() + " | Taille : " + var.getTaille() + " | Param : " + var.getIsParam() + " | Attribut : " + var.getIsAttribut() + " | Adresse : " + var.getAdresse() + " | Deplacement : " + var.getDeplacement());
        }
        System.out.println("Fonctions ou Procedures:");
        for (String key : fonctions.keySet()) {
            TDSItemFctProc fct = fonctions.get(key);
            String table_parente = "";
            if (this.parent != null){
                table_parente = " | Table Parente : " + this.parent.id;
            }
            else {
                table_parente = " | RACINE ";
            }
            if (this.parent != null){
                System.out.println("Fonction  ||  Nom : " + fct.identif + " | Nombre d'arguments: " + fct.nbArgs + " | Type de retour: " + fct.type + table_parente + "| Table Fille: " + fct.tableFille.id);
            } else {
                System.out.println("Procedure  ||  Nom : " + fct.identif + " | Nombre d'arguments : " + fct.nbArgs + table_parente + " | Table Fille : " + fct.tableFille.id);
            }
        }
        for (String key : fonctions.keySet()) {
            TDSItemFctProc fct = fonctions.get(key);
            fct.tableFille.printTDS();
        }

        for (String key : structures.keySet()) {
            TDSItemStruct struct = structures.get(key);
            System.out.println("Structure  ||  Nom : " + struct.identif + " | Nombre d'attributs : " + struct.nbAttributs);
        }
        System.out.println("Fin table "+this.id + "\n");
    }
}