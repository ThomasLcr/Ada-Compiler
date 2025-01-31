package eu.telecomnancy.application.tds;

public class TDSItemStruct {
    String identif;      // Nom de la structure
    int nbAttributs;     // Nombre d'attributs de la structure

    public TDSItemStruct(String identif, int nbAttributs) {
        this.identif = identif;
        this.nbAttributs = nbAttributs;
    }

    /*
     * Get the name of the structure
     */
    public String getIdentif() {
        return this.identif;
    }

    /*
     * Get the number of attributes of the structure
     */
    public int getNbAttributs() {
        return this.nbAttributs;
    }
    /*
     * Add a new attribute to the structure
     */
    public void addAttribut() {
        this.nbAttributs++;
    }
}
