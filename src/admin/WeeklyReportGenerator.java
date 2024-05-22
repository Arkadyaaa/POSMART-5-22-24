package admin;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import main.Product;
import receipt.Receipt;

public class WeeklyReportGenerator extends main.AppPanels {

    private static ObservableList<Receipt> receiptList = FXCollections.observableArrayList();
    private static List<Receipt> weeklyReceipts = new ArrayList<Receipt>();
    private List<Product> allProducts = new ArrayList<>();

    LocalDate lastSunday = LocalDate.now().minusDays(7);
    LocalDate today = LocalDate.now();

    public void passValues(List<Product> allProducts){
        receiptList = getReceipts();
        this.allProducts = allProducts;

        weeklyReceipts = getReceiptsBetweenDates(lastSunday, today);
    }

    public void generateReport(int reportId) {
        String receipt_ids = getReceiptIds();
        Double total_sales = getTotalSales();
        Double units_sold = getUnitsSold();
        int no_of_customers = getCustomers();
        String popProduct = getPopProduct();
        String popCategory = getPopCategory();

        Report report = new Report(reportId, Date.valueOf(today), receipt_ids, total_sales, 
                                    units_sold, no_of_customers, popProduct, popCategory);
        
        saveToDatabase(report);
    }
    

    // Method to retrieve receipts between two dates
    public List<Receipt> getReceiptsBetweenDates(LocalDate startDate, LocalDate endDate) {
        List<Receipt> receiptsInRange = new ArrayList<>();
        for (Receipt receipt : receiptList) {
            LocalDate receiptDate = receipt.getDate().toLocalDate();
            if (!receiptDate.isBefore(startDate) && !receiptDate.isAfter(endDate)) {
                receiptsInRange.add(receipt);
            }
        }
        return receiptsInRange;
    }

    private String getReceiptIds(){

        StringBuilder receipt_ids = new StringBuilder();
        // Process each receipt
        for (Receipt receipt : weeklyReceipts) {
            receipt_ids.append(receipt.getId() + "\n");
        }

        // Removes the last \n
        if (receipt_ids.length() > 0 && receipt_ids.charAt(receipt_ids.length() - 1) == '\n') {
            receipt_ids.deleteCharAt(receipt_ids.length() - 1);
        }

        return receipt_ids.toString();

    }

    private Double getTotalSales(){

        Double sum = 0d;

        for(Receipt receipt : weeklyReceipts){
            sum += receipt.getTotal_amount();
        }

        return sum;
    }

    private Double getUnitsSold(){

        Double sum = 0d;

        for(Receipt receipt : weeklyReceipts){
            //Separates the quantity by new lines
            List<String> quantityStrings = Arrays.asList(receipt.getQuantity().split("\n"));

            for(String q : quantityStrings){
                try{
                    Double quantityInt = Double.parseDouble(q.trim());
                    sum += quantityInt;
                } catch(NumberFormatException e){
                    System.err.println("parse int error");
                }
            }
        }

        return sum;
    }

    private int getCustomers(){
        HashSet<String> buyers = new HashSet<>();
        
        //Count
        for(Receipt receipt : weeklyReceipts){
            if(!buyers.contains(receipt.getName())){
                buyers.add(receipt.getName());
            }
        }


        return buyers.size();
    }

    private String getPopProduct(){
        Map<String, Integer> productCount = new HashMap<>();
        
        //Iterate over each report in the list
        for (Receipt receipt : weeklyReceipts) {

            List<String> productsList = Arrays.asList(receipt.getProducts().split("\n"));
            
            for (String product : productsList) {
                product.trim();
                productCount.put(product, productCount.getOrDefault(product, 0) + 1);
            }
        }
        
        //Find the most sold product
        String popProduct = "";
        int maxCount = 0;
        for (Map.Entry<String, Integer> entry : productCount.entrySet()) {
            if (entry.getValue() > maxCount) {
                popProduct = entry.getKey();
                maxCount = entry.getValue();
            }
        }

        return popProduct;
    }

    private String getPopCategory(){
        Map<String, Integer> categoryCount = new HashMap<>();
        
        //Iterate over each report in the list
        for (Receipt receipt : weeklyReceipts) {

            List<String> productsList = Arrays.asList(receipt.getProducts().split("\n"));
            String categoryName = null;
                
            //Count category occurrences
            for (String productName : productsList) {
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
        
        //Find the most sold product
        String popCategory = "";
        int maxCount = 0;
        for (Map.Entry<String, Integer> entry : categoryCount.entrySet()) {
            if (entry.getValue() > maxCount) {
                popCategory = entry.getKey();
                maxCount = entry.getValue();
            }
        }

        return popCategory;
    }

    private void saveToDatabase(Report report){
        String url = "jdbc:mysql://localhost:3306/pos";
        String user = "root";
        String password = "";
    
        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            String sql = "INSERT INTO weekly_report (report_date, receipt_ids, total_sales, units_sold, no_of_customers, most_popular_product, most_popular_category)"
                    + "VALUES (?, ?, ?, ?, ?, ?, ?)";
    
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setDate(1, report.getReport_date());
                preparedStatement.setString(2, report.getReceipt_ids());
                preparedStatement.setDouble(3, report.getTotal_sales());
                preparedStatement.setDouble(4, report.getUnits_sold());
                preparedStatement.setInt(5, report.getNo_of_customers());
                preparedStatement.setString(6, report.getMost_popular_product());
                preparedStatement.setString(7, report.getMost_popular_category());
    
                // Execute the insert statement
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
