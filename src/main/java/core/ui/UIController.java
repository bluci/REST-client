package core.ui;

import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UIController {
    private final Stage primaryStage;
    private final Logger logger = LoggerFactory.getLogger(UIController.class);

    public UIController(final Stage primaryStage) {

        this.primaryStage = primaryStage;
    }

    public void initialize() {
        logger.debug("UIController");
    }

}
