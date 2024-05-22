package admin;

import java.util.Arrays;
import java.util.List;
import java.time.LocalDate;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import receipt.Receipt;

public class SalesController extends main.AppPanels{

    @FXML
    private TableView<Receipt> tableView;

    @FXML
    private TableColumn<Receipt, Integer> idColumn;

    @FXML
    private TableColumn<Receipt, String> dateColumn;

    @FXML
    private TableColumn<Receipt, String> timeColumn;

    @FXML
    private TableColumn<Receipt, String> buyerColumn;

    @FXML
    private TableColumn<Receipt, String> paymentColumn;

    @FXML
    private TableColumn<Receipt, String> deliveryColumn;

    @FXML
    private TableColumn<Receipt, String> productsColumn;

    @FXML
    private TableColumn<Receipt, String> quantityColumn;

    @FXML
    private TableColumn<Receipt, String> individualPriceColumn;

    @FXML
    private TableColumn<Receipt, String> totalCostColumn;

    @FXML
    private TableColumn<Receipt, Double> totalAmountColumn;

    @FXML
    private Button closeWindow;

    @FXML
    private Button refreshButton;

    @FXML
    private Label totalOrdersLabel;

    @FXML
    private Label productsSoldLabel;

    @FXML
    private Label earningsTodayLabel;

    @FXML
    private Label earningsLabel;

    @FXML
    private ImageView panel1;

    @FXML
    private ImageView panel2;

    @FXML
    private ImageView panel3;

    @FXML
    private ImageView panel4;

    private static ObservableList<Receipt> receiptList = FXCollections.observableArrayList();
    private List<ImageView> panels;

    @FXML
    private void initialize(){
        receiptList = getReceipts();

        // Bind the columns to the data properties
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));
        buyerColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        paymentColumn.setCellValueFactory(new PropertyValueFactory<>("paymentMethod"));
        deliveryColumn.setCellValueFactory(new PropertyValueFactory<>("deliveryMethod"));
        productsColumn.setCellValueFactory(new PropertyValueFactory<>("products"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        individualPriceColumn.setCellValueFactory(new PropertyValueFactory<>("individual_price"));
        totalCostColumn.setCellValueFactory(new PropertyValueFactory<>("total_cost"));
        totalAmountColumn.setCellValueFactory(new PropertyValueFactory<>("total_amount"));

        tableView.setItems(receiptList);

        panels = Arrays.asList(panel1, panel2, panel3, panel4);
        for(ImageView panel : panels){
            displayImages("/resources/panel.png", panel);
        }

        calculateTotalOrders();
        calculateProductsSold();
        calculateEarningsToday();
        calculateTotalEarnings();
    
    }

    private void calculateTotalOrders(){
        int size = receiptList.size();
        totalOrdersLabel.setText(Integer.toString(size));
    }

    private void calculateProductsSold(){
        double sum = 0;

        for (int i = 0; i < receiptList.size(); i++) {
            String quantityString = receiptList.get(i).getQuantity();
            String[] quantity = quantityString.split("\\r?\\n");

            for (String price : quantity) {
                sum += Double.parseDouble(price);
            }
        }

        productsSoldLabel.setText(Double.toString(sum));
    }

    private void calculateEarningsToday(){
        LocalDate currentDate = LocalDate.now();
        Double sum = 0.0;

        for (int i = 0; i < receiptList.size(); i++) {
            if(currentDate.equals(receiptList.get(i).getDate().toLocalDate())){
                sum += receiptList.get(i).getTotal_amount();
            }
        }

        earningsTodayLabel.setText(Double.toString(sum));
    }

    private void calculateTotalEarnings(){
        Double sum = 0.0;

        for (int i = 0; i < receiptList.size(); i++) {
            sum += receiptList.get(i).getTotal_amount();
        }

        earningsLabel.setText(Double.toString(sum));
    }

    @FXML
    private void handleCloseWindow() {
        closeWindow(closeWindow);
    }

    @FXML
    private void handleRefresh() {
        initialize();
    }
}
