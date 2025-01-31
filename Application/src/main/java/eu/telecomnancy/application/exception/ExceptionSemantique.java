package eu.telecomnancy.application.exception;

import eu.telecomnancy.application.App;
import javafx.application.Platform;
import javafx.stage.Stage;

public class ExceptionSemantique extends Exception{
    
    public ExceptionSemantique(String message) {

        super("Exception sémantique : " + message);

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
