package eu.telecomnancy.application.tds;
import java.util.HashMap;



public class TDS { // Classe représentant la table des symboles
    HashMap<String, TDSItemVar> variables;  // Table de hachage pour les variables
    HashMap<String, TDSItemFctProc> fonctions;  // Table de hachage pour les fonctions ou procédures
    int adrVarCourante;                    // Taille occupée par les variables locales
    int adrArgCourant;                     // Taille occupée par les paramètres de la fonction

    // Constructeur
    public TDS() {
        this.variables = new HashMap<>();
        this.fonctions = new HashMap<>();
        this.adrVarCourante = 0;
        this.adrArgCourant = 0;
    }

    // Méthode pour ajouter une variable à la table des symboles
    public void addVariable(String identif, int taille, boolean isParam) {
        TDSItemVar variable = new TDSItemVar(identif, taille, this, isParam, adrVarCourante);
        variables.put(identif, variable);
        adrVarCourante += taille;
    }

    // Méthode pour ajouter une fonction à la table des symboles
    public void addFunctionOrProcedure(String identif, int nbArgs, TDS table) {
        TDSItemFctProc fonction = new TDSItemFctProc(identif, nbArgs, table);
        fonctions.put(identif, fonction);
    }
}