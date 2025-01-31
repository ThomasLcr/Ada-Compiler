package eu.telecomnancy.application;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import eu.telecomnancy.application.exception.ExceptionLexicale;
import eu.telecomnancy.application.exception.ExceptionSemantique;
import eu.telecomnancy.application.exception.ExceptionSyntaxique;
import eu.telecomnancy.application.tds.TDS;
import eu.telecomnancy.application.tds.TDSItemFctProc;
import eu.telecomnancy.application.token.Token;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.stage.Stage;

import java.io.File;
import java.util.Scanner;

public class App extends Application {
    private static String[] args;

    public static void main(String[] arg) {
        args = arg;
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws ExceptionLexicale, ExceptionSyntaxique {

        // selection du fichier a lancer
        String dossier = "src/main/resources/eu/telecomnancy/application/";

        // creation d'un objet file pour représenter le dossier
        File repertoire = new File(dossier);

        // Vérifier si le chemin est un dossier et s'il existe
        if (repertoire.isDirectory() && repertoire.exists()) {
            // on récupère la liste des fichiers dans le dossier
            File[] fichiers = repertoire.listFiles();
            Arrays.sort(fichiers, Comparator.comparing(File::getName));

            // Afficher la liste des fichiers avec un numéro devant chaque nom
            for (int i = 0; i < fichiers.length; i++) {
                if (fichiers[i].getName().toLowerCase().endsWith(".txt")) {
                    System.out.println((i + 1) + ". " + fichiers[i].getName());
                }
            }

            // Lire l'entrée utilisateur pour sélectionner un fichier
            Scanner scanner = new Scanner(System.in);

            System.out.print("Veuillez selectionner un fichier en entrant son numero ( ou exit pour quitter ) : \n");

            String input = scanner.nextLine();

            if (input.equalsIgnoreCase("exit")) {
                System.exit(0);
            }

            // Vérifier si le choix est valide
            int choix = Integer.parseInt(input);
            if (choix > 0 && choix <= fichiers.length) {
                // Ouvrir le fichier sélectionné
                try {
                    // generation de l'arbre syntaxique avec le fichier choisi
                    String file = fichiers[choix - 1].getName();
                    primaryStage.setTitle("Arbre Syntaxique");
                    Label label = new Label("Appuyer sur echap pour quitter");
                    double treeWidth = 1520;
                    double treeHeight = 900;

                    double sceneWidth = 1520;
                    double sceneHeight = 900;

                    // ici on ajoute un nouvel argument à getTokens qui est le fihcier selectionné
                    ParserV6 parserV6 = new ParserV6(getTokens(file));
                    TreeNode root = parserV6.parse();
                    TreeView treeView = new TreeView(root, treeWidth, treeHeight, 12, 80);
                    treeView.setLayoutX((sceneWidth - treeWidth) / 2);
                    treeView.setLayoutY((sceneHeight - treeHeight) / 2);

                    ScrollPane scrollPane = new ScrollPane();
                    scrollPane.setContent(treeView);

                    Scene scene = new Scene(scrollPane, sceneWidth, sceneHeight);

                    primaryStage.setScene(scene);

                    // ! j'ai retiré le démarrage de de l'application en boucle pour pouvoir mieux
                    // debug l'ASM
                    
                    /*primaryStage.setOnCloseRequest(event -> {
                    Platform.runLater(() -> {
                    try {
                      // Redémarrer l'application en lançant une nouvelle instance de la classe principale
                      App main = new App();
                      Stage stage = new Stage();
                      main.start(stage);
                    } catch (Exception e) {
                      e.printStackTrace();
                    }
                    });
                    });*/
                    
                    primaryStage.show();

                    TDS tds = parserV6.getTableDesSymboles();
                    System.out.println("\nDébut affichage TDS");
                    tds.printTDS();
                    // affichage des erreurs
                    parserV6.DisplayErrors();
                    TDSItemFctProc main = tds.getFonctions().values().iterator().next();
                    // génération de code
                    if (!parserV6.hasErrors()) {
                        ASMGenerator asmGenerator = new ASMGenerator(main, root, file,tds);
                        asmGenerator.generate();
                        asmGenerator.execute();
                    }

                } catch (Exception e) {
                    System.out.println("Erreur lors de l'ouverture du fichier : " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.out.println("Choix invalide. Veuillez sélectionner un numéro de fichier valide.");
            }

        } else {
            System.out.println("Le chemin spécifié n'est pas un dossier existant.");
        }
    }

    private ArrayList<Token> getTokens(String file) throws ExceptionLexicale {

        System.out.println("Initialisation du compilateur miniAda");
        String filepath;
        // if (args.length == 0) { // si l'utilisateur a founi un chemin de fichier à
        // compiler
        // System.out.println("Warning: aucun fichier source specifie. Utilisation du
        // fichier de test par defaut");
        // filepath =
        // "src/main/resources/eu/telecomnancy/application/Programme_Operations.txt";
        // } else { // sinon on utilise le fichier par défaut
        // filepath = args[0];
        // }

        filepath = "src/main/resources/eu/telecomnancy/application/" + file;
        System.out.println("Fichier source: " + file);

        // Création du Lexer
        Lexer lexer = new Lexer(filepath);
        ArrayList<Token> token = lexer.getTokens();
        System.out.println(token.size());
        for (Token t : token) {
            t.printToken();
        }

        return token;
    }
}
