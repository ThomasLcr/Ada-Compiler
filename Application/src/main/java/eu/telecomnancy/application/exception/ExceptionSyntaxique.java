package eu.telecomnancy.application.exception;

import eu.telecomnancy.application.App;
import javafx.application.Platform;
import javafx.stage.Stage;

public class ExceptionSyntaxique extends Exception{
    
    public ExceptionSyntaxique(String message) {

        super("Exception syntaxique : " + message);

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
    }
}
