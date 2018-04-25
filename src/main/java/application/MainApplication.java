package application;

import core.ui.UIController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainApplication extends Application {
    private static final Logger logger = LoggerFactory.getLogger(MainApplication.class);

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("pull request client");
        primaryStage.setWidth(1000);
        primaryStage.setHeight(500);
        primaryStage.centerOnScreen();

        UIController uiController = new UIController(primaryStage);
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/pullRequestViewer.fxml"));
        fxmlLoader.setControllerFactory(param -> param.isInstance(uiController) ? uiController : null);
        primaryStage.setScene(new Scene(fxmlLoader.load()));
        primaryStage.show();
        primaryStage.toFront();
    }

    public static void main(String[] args) {
        logger.debug("launching application");
        Application.launch(MainApplication.class, args);
    }
}
