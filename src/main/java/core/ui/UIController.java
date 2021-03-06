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
import javafx.stage.WindowEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class UIController {
    private final Logger logger = LoggerFactory.getLogger(UIController.class);
    private final RestClient restClient;
    private final Stage primaryStage;
    private ObservableList<PullRequest> pullRequestTableData;
    private List<PullRequest> allPullRequests = new ArrayList<>();

    @FXML
    public TableView<PullRequest> pullRequestTableView;
    @FXML
    public TableColumn<PullRequest, Integer> pullRequestNrCol;
    @FXML
    public TableColumn<PullRequest, String> pullRequestTitleCol;
    @FXML
    public TextField filterStringInput;


    public UIController(final RestClient restClient, final Stage primaryStage) {
        this.restClient = restClient;
        this.primaryStage = primaryStage;
    }

    public void initialize() {
        pullRequestNrCol.setCellValueFactory(new PropertyValueFactory<>("number"));
        pullRequestTitleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        pullRequestTableData = FXCollections.observableArrayList();
        this.primaryStage.addEventHandler(WindowEvent.WINDOW_SHOWING, (event -> {
            new Thread(this::updateTable).start();
        }));
    }

    @FXML
    public void onRefreshBtnClicked(ActionEvent actionEvent) {
        updateTable();
        pullRequestTableView.refresh();
    }

    private void updateTable() {
        try {
            allPullRequests.addAll(restClient.getAllOpenPullRequests());
            logger.debug("number of pullRequests:" + allPullRequests.size());
            setPullRequestTableData(allPullRequests);
        } catch (IOException | RestException e) {
            logger.error("error getting pull requests from restClient");
            e.printStackTrace();
            showWarningAlert("\n" + e.getMessage());
        }
    }

    private void setPullRequestTableData(final List<PullRequest> pullRequestList) {
        pullRequestTableData.clear();
        pullRequestTableData.addAll(pullRequestList);
        pullRequestTableView.setItems(pullRequestTableData);
    }

    @FXML
    public void onApplyFilterBtnClicked(ActionEvent actionEvent) {
        String filterString = filterStringInput.getText();
        if (!isValidFilterString(filterString)) {
            showWarningAlert("invalid filter string");
            return;
        }

        List<PullRequest> filteredPullRequests = pullRequestTableData.stream()
                .filter(p -> p.getTitle().matches(".*" + filterString + ".*"))
                .collect(Collectors.toList());
        setPullRequestTableData(filteredPullRequests);
        pullRequestTableView.refresh();
    }

    private boolean isValidFilterString(final String filterString) {
        boolean result = true;
        final String validRegex = "([A-Z]*[a-z]*)*";
        if (filterString == null) {
            return false;
        }
        if (filterString.isEmpty()) {
            result = false;
        }
        if (!filterString.matches(validRegex)) {
            result = false;
        }
        return result;
    }

    @FXML
    public void onRemoveFilterBtnClicked(ActionEvent actionEvent) {
        setPullRequestTableData(allPullRequests);
        filterStringInput.setText("");
        pullRequestTableView.refresh();
    }

    private void showWarningAlert(final String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText("warning");
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
