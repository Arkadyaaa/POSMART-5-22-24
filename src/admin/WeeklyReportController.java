package admin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import main.Product;
import receipt.Receipt;

public class WeeklyReportController extends main.AppPanels{

    @FXML
    private TableView<Report> tableView;

    @FXML
    private TableColumn<Report, Integer> idColumn;

    @FXML
    private TableColumn<Report, String> dateColumn;

    @FXML
    private TableColumn<Report, String> receiptsColumn;

    @FXML
    private TableColumn<Report, String> buyersColumn;

    @FXML
    private TableColumn<Report, String> unitsSoldColumn;

    @FXML
    private TableColumn<Report, String> customersColumn;

    @FXML
    private TableColumn<Report, String> productColumn;

    @FXML
    private TableColumn<Report, String> categoryColumn;

    @FXML
    private Button closeWindow;

    @FXML
    private Button refreshButton;

    @FXML
    private Label totalOrdersLabel;

    @FXML
    private Label popProductsLabel;

    @FXML
    private Label popCategoryLabel;

    @FXML
    private Label popProduct;

    @FXML
    private Label popCategory;

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

    private static ObservableList<String> receiptIdList = FXCollections.observableArrayList();
    private static ObservableList<Report> reportList = FXCollections.observableArrayList();
    private List<ImageView> panels;
    private List<Product> allProducts = new ArrayList<>();

    @FXML
    private void initialize(){
        popProduct.setText("Most Popular\n Product");
        popCategory.setText("Most Popular\n Category");
    }

    public void passValues(List<Product> allProducts){
        this.allProducts = allProducts;
    }

    public void displayReport(){
        
        System.out.println("All products print");
        for(Product product : allProducts){
            System.out.println(product.getName());
        }

        reportList = getReports();

        // Bind the columns to the data properties
        idColumn.setCellValueFactory(new PropertyValueFactory<>("report_id"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("report_date"));
        receiptsColumn.setCellValueFactory(new PropertyValueFactory<>("receipt_ids"));
        buyersColumn.setCellValueFactory(new PropertyValueFactory<>("total_sales"));
        unitsSoldColumn.setCellValueFactory(new PropertyValueFactory<>("units_sold"));
        customersColumn.setCellValueFactory(new PropertyValueFactory<>("no_of_customers"));
        productColumn.setCellValueFactory(new PropertyValueFactory<>("most_popular_product"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("most_popular_category"));

        tableView.setItems(reportList);

        panels = Arrays.asList(panel1, panel2, panel3, panel4);
        for(ImageView panel : panels){
            displayImages("/resources/panel.png", panel);
        }

        calculateTotalSales();
        calculatePopProduct();
        calculatePopCategory();
        calculateTotalEarnings();
    }

    private void calculateTotalSales(){
        for(Report report : reportList){
            List<String> current_receipt_ids = Arrays.asList(report.getReceipt_ids().split("\n"));
            for(String id : current_receipt_ids){
                receiptIdList.add(id.trim());
            }
        }

        totalOrdersLabel.setText(receiptIdList.size() + "");
    }

    private void calculatePopProduct(){
        Map<String, Integer> productCount = new HashMap<>();
        
        //Iterate over each report in the list
        for (Report report : reportList) {
            if(report.getReceipt_ids() == null || report.getReceipt_ids().equals("")){
                continue;
            }

            List<String> receiptIds = Arrays.asList(report.getReceipt_ids().split("\n"));
            
            for (String receiptId : receiptIds) {
                Receipt receipt = getReceiptById(receiptId);
                List<String> products = Arrays.asList(receipt.getProducts().split("\n"));
                
                //Count product occurrences
                for (String product : products) {
                    productCount.put(product, productCount.getOrDefault(product, 0) + 1);
                }
            }
        }
        
        //Find the most sold product
        String mostSoldProduct = "";
        int maxCount = 0;
        for (Map.Entry<String, Integer> entry : productCount.entrySet()) {
            if (entry.getValue() > maxCount) {
                mostSoldProduct = entry.getKey();
                maxCount = entry.getValue();
            }
        }
        
        popProductsLabel.setText(mostSoldProduct);
    }

    private void calculatePopCategory(){
        Map<String, Integer> categoryCount = new HashMap<>();
        
        //Iterate over each report in the list
        for (Report report : reportList) {
            if(report.getReceipt_ids() == null || report.getReceipt_ids().equals("")){
                continue;
            }

            List<String> receiptIds = Arrays.asList(report.getReceipt_ids().split("\n"));
            
            for (String receiptId : receiptIds) {
                Receipt receipt = getReceiptById(receiptId);
                List<String> products = Arrays.asList(receipt.getProducts().split("\n"));
                String categoryName = null;
                
                //Count category occurrences
                for (String productName : products) {
                    for(Product currentProduct : allProducts){
                        if(currentProduct.getName().equals(productName)){
                            categoryName = currentProduct.getCategory();
                            break;
                        }

                    }
                    
                    //Skip if product doesnt exist in the list
                    if (categoryName == null) {
                        System.err.println("Product not found: " + productName);
                        continue;
                    }

                    categoryCount.put(categoryName, categoryCount.getOrDefault(productName, 0) + 1);
                }
            }
        }
        
        //Find the most sold category
        String mostSoldCategory = "";
        int maxCount = 0;
        for (Map.Entry<String, Integer> entry : categoryCount.entrySet()) {
            if (entry.getValue() > maxCount) {
                mostSoldCategory = entry.getKey();
                maxCount = entry.getValue();
            }
        }
        
        popCategoryLabel.setText(mostSoldCategory);
    }

    private void calculateTotalEarnings(){
        Double sum = 0.0;

        for(Report i : reportList){
            sum += i.getTotal_sales();
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

    private Receipt getReceiptById(String inputId){
        ObservableList<Receipt> receiptList = getReceipts();
    
        if (inputId == null || inputId.trim().isEmpty()) {
            return null;
        }
    
        try {
            int receiptId = Integer.parseInt(inputId.trim());
            for (Receipt i : receiptList) {
                if (i.getId() == receiptId) {
                    return i;
                }
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    
        return null;
    }
}
