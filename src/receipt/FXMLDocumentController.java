/*
* Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
* Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXML2.java to edit this template
*/
package receipt;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import main.Product;
import main.AddToGrid;
import java.util.HashSet;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

public class FXMLDocumentController extends main.AppPanels {

    @FXML
    private AnchorPane mainAnchorPane;

    @FXML
    private DatePicker dateField;

    @FXML
    private ComboBox<String> timeComboBox;

    @FXML
    private ComboBox<String> paymentMethodComboBox;

    @FXML
    private ComboBox<String> deliveryMethodComboBox;

    @FXML
    private ComboBox<String> nameComboBox;

    @FXML
    private TextField address;

    @FXML
    private TextField contactNo;

    @FXML
    private TextField email;

    private ListView<String> myCart;

    private Double totalAmount;
    
    private List<Product> allProducts = new ArrayList<>();
    private HashSet<String> cartSet = new HashSet<>();
    private List<Buyer> previousBuyers = new ArrayList<>();

    private AddToGrid addToGrid;

    private int currentId;

    public void passValues(HashSet<String> cartSet, ListView<String> myCart, double totalAmount, List<Product> allProducts, AddToGrid addToGrid) {
        this.myCart = myCart;
        this.totalAmount = totalAmount;
        this.allProducts = allProducts;
        this.addToGrid = addToGrid;
        this.cartSet = cartSet;
    }

    public void initialize() {
        // Populate combo boxes
        timeComboBox.getItems().addAll("6:00 AM", "7:00 AM", "8:00 AM", "9:00 AM", "10:00 AM", "11:00 AM", "12:00 AM",
                "1:00 PM", "2:00 PM", "3:00 PM", "4:00 PM", "5:00 PM", "6:00 PM");
        paymentMethodComboBox.getItems().addAll("Gcash", "PayPal", "Cash");
        deliveryMethodComboBox.getItems().addAll("Standard Delivery", "Pick-Up");

        currentId = getNextId();
        System.out.println("Last inserted receipt_id: " + (currentId - 1));
        initializePreviousBuyers();

        for (Buyer buyer : previousBuyers){
            nameComboBox.getItems().add(buyer.getName());
        }

    }

    @FXML
    private void onNameSelect(ActionEvent event) {
        String selectedItem = nameComboBox.getSelectionModel().getSelectedItem();

        //Select buyer
        if (selectedItem != null) {
            for (Buyer buyer : previousBuyers){
                if(selectedItem.equals(buyer.getName())){
                    setBuyerFields(buyer);
                    break;
                }
            }
        }
    }

    public void setBuyerFields(Buyer selectedBuyer){

        nameComboBox.setValue(selectedBuyer.getName());
        address.setText(selectedBuyer.getAddress());
        contactNo.setText(selectedBuyer.getContactNo() + "");
        email.setText(selectedBuyer.getEmail());

        mainAnchorPane.requestFocus();
    }

    @FXML
    private void printDetails() {
        if(!contactNoIsValid(contactNo.getText())){
            showAlert("Error", "Make sure the contact number is correct");
            return;
        }

        if (fieldsAreValid()) {


            String details = "Buyer: " + nameComboBox.getValue() + "\n"
                    + "Address: " + address.getText() + "\n"
                    + "Delivery Date: " + dateField.getValue() + "\n"
                    + "Delivery Time: " + timeComboBox.getValue() + "\n"
                    + "Order ID: " + currentId + "\n"
                    + "Payment Method: " + paymentMethodComboBox.getValue() + "\n"
                    + "Delivery Method: " + deliveryMethodComboBox.getValue();

            showOutput(details);


            for(Buyer buyer : previousBuyers){
                if(buyer.getName().equalsIgnoreCase(nameComboBox.getValue())){
                    int buyerId = getBuyerIdByName(buyer.getName());
                    removeBuyer(buyerId);
                }
            }

            saveToDatabase(myCart, totalAmount, nameComboBox.getValue());
            saveBuyer(nameComboBox.getValue(), address.getText(), contactNo.getText(), email.getText());

            
            for(Product currentProduct : allProducts){
                for (String cartItem : myCart.getItems()) {
                    if (cartItem.startsWith(currentProduct.getName())) {
                        // Update stock in the database
                        updateProductStock(currentProduct.getName(), currentProduct.getStock());
                
                        // Call updateProductAvailability to update isAvailable in the database
                        updateProductAvailability(currentProduct.getName(), determineIsAvailable(currentProduct.getStock()));
                    }
                }
            }

            closeWindow();
            addToGrid.clearCart(cartSet, myCart);
        } else {
            showAlert("Error", "Make sure all of the fields have been populated.");
        }
    }

    @FXML
    private void closeWindow() {
        Stage stage = (Stage) dateField.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handlePreviousBuyersButton(){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("PreviousBuyers.fxml"));
            Parent root = loader.load();

            PreviousBuyersController previousBuyersController = loader.getController();
            previousBuyersController.passVariables(previousBuyers, this);

            Stage stage = new Stage();
            stage.setTitle("Previous Buyers");
            stage.setScene(new Scene(root, 1200.0, 600.0));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void showOutput(String details) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("FXMLoutput.fxml"));
            Parent root = loader.load();

            FXMLoutputController outputController = loader.getController();
            outputController.outputReceipt(details, myCart, totalAmount);

            Stage stage = new Stage();
            stage.setTitle("Output");
            stage.setScene(new Scene(root, 389.0, 850.0));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveToDatabase(ListView<String> myCart, double totalAmount, String buyerName) {
        String url = "jdbc:mysql://localhost:3306/pos";
        String user = "root";
        String password = "";
    
        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            String sql = "INSERT INTO receipt (name, date, time, address, paymentMethod, deliveryMethod, products, quantity, individual_price, total_cost, total_amount)"
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, buyerName);
                preparedStatement.setDate(2, Date.valueOf(dateField.getValue().toString())); // Convert LocalDate to java.sql.Date
                preparedStatement.setString(3, timeComboBox.getValue());
                preparedStatement.setString(4, address.getText());
                preparedStatement.setString(5, paymentMethodComboBox.getValue());
                preparedStatement.setString(6, deliveryMethodComboBox.getValue());
    
                // Separate myCart into product, quantity, and total cost
                ObservableList<String> cartItems = myCart.getItems();
                StringBuilder productData = new StringBuilder();
                StringBuilder quantityData = new StringBuilder();
                StringBuilder individualPriceData = new StringBuilder();
                StringBuilder totalCostData = new StringBuilder();
    
                for (String cartItem : cartItems) {
                    String[] parts = cartItem.split(":|₱");
    
                    // Check if the array has enough elements before accessing them
                    if (parts.length == 3) {
                        String product = parts[0].trim();
                        float quantity = Float.parseFloat(parts[1].trim());
    
                        // Remove any non-numeric characters (including currency sign and commas) before parsing the total cost
                        String totalCostString = parts[2].replaceAll("[^\\d.]", "").trim();
                        double itemTotalCost = Double.parseDouble(totalCostString);
                        double individualPrice = itemTotalCost / quantity;
    
                        // Append product, quantity, and total cost to their respective StringBuilders
                        productData.append(product).append("\n");
                        quantityData.append(quantity).append("\n");
                        individualPriceData.append(String.format("%.2f", individualPrice)).append("\n");
                        totalCostData.append(String.format("%.2f", itemTotalCost)).append("\n");
                    } else {
                        // Handle the case where the format is incorrect
                        System.out.println("Invalid format for cart item: " + cartItem);
                    }
                }
    
                // Set the concatenated product data, quantity data, individual price data, and total cost data
                preparedStatement.setString(7, productData.toString().trim());
                preparedStatement.setString(8, quantityData.toString().trim());
                preparedStatement.setString(9, individualPriceData.toString().trim());
                preparedStatement.setString(10, totalCostData.toString().trim());
                preparedStatement.setDouble(11, totalAmount);
    
                // Execute the insert statement
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private int getNextId(){
        String url = "jdbc:mysql://localhost:3306/pos";
        String user = "root";
        String password = "";

        try (Connection connection = DriverManager.getConnection(url, user, password)) {

            Statement statement = connection.createStatement();

            String query = "SELECT MAX(id) FROM receipt";
            ResultSet resultSet = statement.executeQuery(query);

            int lastInsertedId = 0;

            if (resultSet.next()) {
                lastInsertedId = resultSet.getInt(1);
            } else {
                System.out.println("No records in the 'receipt' table");
            }

            if(lastInsertedId == 0){
                lastInsertedId = 1000;
            }

            // Close resources
            resultSet.close();
            statement.close();
            connection.close();
            
            return ++lastInsertedId;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        //default value
        return 1000;

    }

    private void saveBuyer(String name, String address, String contactNo, String email) {
        String url = "jdbc:mysql://localhost:3306/pos";
        String user = "root";
        String password = "";

        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            String sql = "INSERT INTO buyer (name, address, contactNo, email)"
                    +
                    "VALUES (?, ?, ?, ?)";

            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, name);
                preparedStatement.setString(2, address);
                preparedStatement.setString(3, contactNo);
                preparedStatement.setString(4, email);

                // Execute the insert statement
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void removeBuyer(int buyerId){
        String url = "jdbc:mysql://localhost:3306/pos";
        String user = "root";
        String password = "";
    
        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            String sql = "DELETE FROM buyer WHERE userId = ?";
    
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setInt(1, buyerId);
    
                // Execute the delete statement
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private int getBuyerIdByName(String buyerName) {
        String url = "jdbc:mysql://localhost:3306/pos";
        String user = "root";
        String password = "";
        int buyerId = 0; // Default value if no matching buyer found
    
        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            String sql = "SELECT userId FROM buyer WHERE name = ?";
    
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, buyerName);
    
                // Execute the select statement
                ResultSet resultSet = preparedStatement.executeQuery();
    
                // If a result is found, retrieve the buyerId
                if (resultSet.next()) {
                    buyerId = resultSet.getInt("userId");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    
        return buyerId;
    }

    private boolean fieldsAreValid() {

        return dateField.getValue() != null
                && timeComboBox.getValue() != null
                && paymentMethodComboBox.getValue() != null
                && deliveryMethodComboBox.getValue() != null;
    }

    private boolean contactNoIsValid(String phoneNumber){
        String regex = "^09\\d{9}$";
        Pattern pattern = Pattern.compile(regex);

        Matcher matcher = pattern.matcher(phoneNumber);
        return matcher.matches();
    }

    private void initializePreviousBuyers(){

        try {
            String url = "jdbc:mysql://localhost:3306/pos";
            String user = "root";
            String password = "";

            // Establish a connection to the database
            Connection connection = (Connection) DriverManager.getConnection(url, user, password);

            // Execute a query to retrieve products
            String selectQuery = "SELECT * FROM buyer";
            try (PreparedStatement preparedStatement = (PreparedStatement) connection.prepareStatement(selectQuery)) {
                ResultSet resultSet = preparedStatement.executeQuery();

                while (resultSet.next()) {
                    String name = resultSet.getString("name");
                    String address = resultSet.getString("address");
                    String contactNo = resultSet.getString("contactNo");
                    String email = resultSet.getString("email");

                    // Add the retrieved product to the list
                    previousBuyers.add(new Buyer(name, address, contactNo, email));
                }
            }
            // Close the database connection
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
} 