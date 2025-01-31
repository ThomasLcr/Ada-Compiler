package eu.telecomnancy.application.tds;

import java.util.List;

public class TDSItemFctProc { // Classe représentant une entrée dans la table des symboles pour une fonction ou une procédure
    String identif;       // Nom de la fonction
    int nbArgs;           // Nombre d'arguments de la fonction
    public TDS tableFille;// Table des symboles locale à la fonction
    String type;          // Type de retour de la fonction ou nul si procédure

    public TDSItemFctProc(String identif, int nbArgs, TDS table, String type) {
        this.identif = identif;
        this.nbArgs = nbArgs;
        this.tableFille = table;
        this.type = type;
    }
    
    public TDSItemVar getVariable(String identif) {
        return tableFille.getVariable(identif);
    }
    /*
     * Get the name of the function
     */
    public String getIdentif() {
        return this.identif;
    }
    /*
     * Get the number of arguments of the function
     */
    public int getNbArgs() {
        return this.nbArgs;
    }
    /*
     * Get the local TDS of the function
     */
    public TDS getTableFille() {
        return this.tableFille;
    }
    /*
     * Get the return type of the function
     */
    public String getType() {
        return this.type;
    }
}   