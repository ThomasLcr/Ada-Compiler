package eu.telecomnancy.application.tds;


public class TDSItemVar { // Classe représentant une entrée dans la table des symboles pour une variable ou un paramètre
    String identif;      // Nom de la variable
    int taille;          // Taille mémoire occupée par la variable
    TDS portee;           // Portée de la variable
    boolean isParam;     // Indique s'il s'agit d'une variable ou d'un paramètre
    int adresse;         // Adresse relative de la variable

    // Constructeur
    public TDSItemVar(String identif, int taille, TDS portee, boolean isParam, int adresse) {
        this.identif = identif;
        this.taille = taille;
        this.portee = portee;
        this.isParam = isParam;
        this.adresse = adresse;
    }
}