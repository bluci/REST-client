package core.ui;

import core.entities.PullRequest;
import core.rest.RestClient;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.IllegalCharsetNameException;
import java.util.List;

public class UIController {
    private final Stage primaryStage;
    private final Logger logger = LoggerFactory.getLogger(UIController.class);
    private final RestClient restClient;
    private ObservableList<PullRequest> pullRequestTableData;
    private final String PATH_TO_PROJECT = "/Microsoft/TypeScript";

    @FXML
    public TableView<PullRequest> pullRequestTableView;
    @FXML
    public TableColumn<PullRequest, Integer> pullRequestNrCol;
    @FXML
    public TableColumn<PullRequest, String> pullRequestTitleCol;

    public UIController(final RestClient restClient, final Stage primaryStage) {
        this.restClient = restClient;
        this.primaryStage = primaryStage;
    }

    public void initialize() {
        pullRequestTitleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        pullRequestTableData = FXCollections.observableArrayList();
    }

    public void onRefreshBtnClicked(ActionEvent actionEvent) {
        updateTable();
    }

    private void updateTable() {
        List<PullRequest> pullRequests;
        try {
            pullRequests = restClient.getAllOpenPullRequests(PATH_TO_PROJECT);
            setPullRequestTableData(pullRequests);
            pullRequestTableView.refresh();
        } catch (IOException e) {
            logger.error("error getting pull requests from restClient");
            e.printStackTrace();
        }
    }

    public void setPullRequestTableData(List<PullRequest> pullRequestList) {
        pullRequestTableData.clear();
        this.pullRequestTableData.addAll(pullRequestList);
        pullRequestTableView.setItems(pullRequestTableData);
    }
}
