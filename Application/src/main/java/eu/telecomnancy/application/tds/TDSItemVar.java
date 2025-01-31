package eu.telecomnancy.application.tds;


public class TDSItemVar { // Classe représentant une entrée dans la table des symboles pour une variable ou un paramètre
    private String identif;      // Nom de la variable
    private String type;         // Type de la variable
    private int taille;           // Taille mémoire occupée par la variable
    private String isParam;     // Indique s'il s'agit d'une variable (null) ou d'un paramètre (nom de la fonction ou procédure à laquelle il appartient)
    private String isAttribut;  // Indique s'il s'agit d'un attribut (nom de la structure à laquelle il appartient)
    private int adresse;         // Adresse relative de la variable
    private int deplacement;     // Déplacement de la variable dans la pile

    // Constructeur
    public TDSItemVar(String identif, String type, int taille, TDS portee, String isParam, String isAttribut, int adresse, int deplacement) {
        this.identif = identif;
        this.type = type;
        this.taille = taille;
        this.isParam = isParam;
        this.isAttribut = isAttribut;
        this.adresse = adresse;
        this.deplacement = deplacement;
    }

    public int getDeplacement() {
        return this.deplacement;
    }
    /*
     * Get the name of the variable
     */
    public String getIdentif() {
        return this.identif;
    }
    /*
     * Get the type of the variable
     */
    public String getType() {
        return this.type;
    }
    /*
     * Get the size of the variable
     */
    public int getTaille() {
        return this.taille;
    }
    /*
     * Get the name of the function or procedure to which the variable belongs
     */
    public String getIsParam() {
        if(this.isParam == null)
            return "null";
        return this.isParam;
    }
    /*
     * Get the name of the structure to which the variable belongs
     */
    public String getIsAttribut() {
        return this.isAttribut;
    }
    /*
     * Get the adresse of the variable
     */
    public int getAdresse(){
        return this.adresse;
    }
}