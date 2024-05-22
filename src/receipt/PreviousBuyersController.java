package receipt;

import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;

public class PreviousBuyersController extends main.AppPanels {

    @FXML
    private TableView<Buyer> tableView;

    @FXML
    private TableColumn<Buyer, String> nameColumn;

    @FXML
    private TableColumn<Buyer, String> addressColumn;

    @FXML
    private TableColumn<Buyer, String> contactColumn;

    @FXML
    private TableColumn<Buyer, String> emailColumn;

    @FXML
    private Button selectButton;

    @FXML
    private Button closeWindow;

    private ObservableList<Buyer> previousBuyers = FXCollections.observableArrayList();
    FXMLDocumentController fxmlDocumentController = new FXMLDocumentController();

    @FXML
    private void initialize(){

        // Set up cell value factories for each column
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        contactColumn.setCellValueFactory(new PropertyValueFactory<>("contactNo"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));

        tableView.setItems(previousBuyers);

    }

    public void passVariables(List<Buyer> previousBuyers, FXMLDocumentController fxmlDocumentController){
        this.fxmlDocumentController = fxmlDocumentController;
        this.previousBuyers.addAll(previousBuyers);
        
        if(previousBuyers.isEmpty()){
            showAlert("Error", "No previous buyers");
            handleCloseWindow();
        }
    }

    @FXML
    private void handleSelect(MouseEvent event) {
        Buyer selectedBuyer = tableView.getSelectionModel().getSelectedItem();
        if (selectedBuyer == null) {
            showAlert("Error", "Make sure to select a user");
            return;
        }

        System.out.println("Selected Buyer: " + selectedBuyer.getName());
        fxmlDocumentController.setBuyerFields(selectedBuyer);

        handleCloseWindow();
    }

    @FXML
    private void handleCloseWindow() {
        closeWindow(closeWindow);
    }
}
