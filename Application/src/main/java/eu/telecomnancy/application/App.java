package eu.telecomnancy.application;

import java.util.ArrayList;

import eu.telecomnancy.application.token.Token;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.stage.Stage;

public class App extends Application {
    private static String[] args;

    public static void main(String[] arg) {
        args = arg;
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws ExceptionLexicale, ExceptionSyntaxique {
        primaryStage.setTitle("Arbre Syntaxique");

        double treeWidth = 1800;
        double treeHeight = 900;

        double sceneWidth = 1700;
        double sceneHeight = 900;

        TreeNode root = getSyntaxTree(getTokens());
        //root.cleanTree();
        TreeView treeView = new TreeView(root, treeWidth, treeHeight, 12, 80);
        treeView.setLayoutX((sceneWidth - treeWidth) / 2);
        treeView.setLayoutY((sceneHeight - treeHeight) / 2);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(treeView);

        Scene scene = new Scene(scrollPane, sceneWidth, sceneHeight);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private ArrayList<Token> getTokens() throws ExceptionLexicale {

        System.out.println("Initialisation du compilateur miniAda");
        String filepath;
        if (args.length == 0) { // si l'utilisateur a founi un chemin de fichier à compiler
            System.out.println("Warning: aucun fichier source specifie. Utilisation du fichier de test par defaut");
            filepath = "src/main/resources/eu/telecomnancy/application/Programme_Loop.txt";
        } else { // sinon on utilise le fichier par défaut
            filepath = args[0];
        }
        System.out.println("Fichier source: " + filepath);

        // Création du Lexer
        Lexer lexer = new Lexer(filepath);
        ArrayList<Token> token = lexer.getTokens();
        System.out.println(token.size());
        for (Token t : token) {
            t.printToken();
        }

        return token;
    }

    private TreeNode getSyntaxTree(ArrayList<Token> token) throws ExceptionSyntaxique {

        // Création du Parser
        ParserV4 parser = new ParserV4(token);

        return parser.parse();
    }
}