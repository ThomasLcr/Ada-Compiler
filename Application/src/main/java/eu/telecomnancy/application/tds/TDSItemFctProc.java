package eu.telecomnancy.application.tds;


public class TDSItemFctProc { // Classe représentant une entrée dans la table des symboles pour une fonction ou une procédure
    String identif;       // Nom de la fonction
    int nbArgs;           // Nombre d'arguments de la fonction
    TDS table;             // Table des symboles locale à la fonction

    public TDSItemFctProc(String identif, int nbArgs, TDS table) {
        this.identif = identif;
        this.nbArgs = nbArgs;
        this.table = table;
    }
}   