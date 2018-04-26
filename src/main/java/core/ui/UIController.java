package core.ui;

import core.entities.PullRequest;
import core.exception.RestException;
import core.rest.RestClient;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class UIController {
    private final Logger logger = LoggerFactory.getLogger(UIController.class);
    private final RestClient restClient;
    private ObservableList<PullRequest> pullRequestTableData;
    private List<PullRequest> allPullRequests;
    private final String PATH_TO_PROJECT = "/Microsoft/TypeScript";

    @FXML
    public TableView<PullRequest> pullRequestTableView;
    @FXML
    public TableColumn<PullRequest, Integer> pullRequestNrCol;
    @FXML
    public TableColumn<PullRequest, String> pullRequestTitleCol;
    @FXML
    public TextField filterStringInput;


    public UIController(final RestClient restClient) {
        this.restClient = restClient;
    }

    public void initialize() {
        pullRequestNrCol.setCellValueFactory(new PropertyValueFactory<>("number"));
        pullRequestTitleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        pullRequestTableData = FXCollections.observableArrayList();
    }

    public void onRefreshBtnClicked(ActionEvent actionEvent) {
        updateTable();
        pullRequestTableView.refresh();
    }

    private void updateTable() {
        try {
            allPullRequests = restClient.getAllOpenPullRequests(PATH_TO_PROJECT);
            setPullRequestTableData(allPullRequests);
        } catch (IOException | RestException e) {
            logger.error("error getting pull requests from restClient");
            e.printStackTrace();
            showWarningAltert("\n" + e.getMessage());
        }
    }

    private void setPullRequestTableData(List<PullRequest> pullRequestList) {
        pullRequestTableData.clear();
        pullRequestTableData.addAll(pullRequestList);
        pullRequestTableView.setItems(pullRequestTableData);
    }

    public void onApplyFilterBtnClicked(ActionEvent actionEvent) {
        String filterString = filterStringInput.getText();
        if (filterString.isEmpty()) {
            logger.error("missing filter string");
            showWarningAltert("missing filter string");
            return;
        }

        List<PullRequest> filteredPullRequests = pullRequestTableData.stream()
                .filter(p -> p.getTitle().matches(".*" + filterString + ".*"))
                .collect(Collectors.toList());
        setPullRequestTableData(filteredPullRequests);
        pullRequestTableView.refresh();
    }

    public void onRemoveFilterBtnClicked(ActionEvent actionEvent) {
        setPullRequestTableData(allPullRequests);
        pullRequestTableView.refresh();
    }

    private void showWarningAltert(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText("warning");
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
