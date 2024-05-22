package main;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.RandomAccessFile;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import admin.Report;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import receipt.Receipt;

public class AppPanels {
        
    public static String url = "jdbc:mysql://localhost:3306/pos";
    public static String user = "root";
    public static String password = "";
    
    private static ObservableList<Receipt> receiptList = FXCollections.observableArrayList();
    private static ObservableList<Report> reportList = FXCollections.observableArrayList();

    public void closeWindow(Node node) {
        // Get a reference to the current stage and close it
        Stage stage = (Stage) node.getScene().getWindow();
        stage.close();
    }

    public void buttonAnimation(ImageView imageView) {
        if (imageView != null) {
            imageView.setOnMousePressed(event -> {
                // Reduce the size of the ImageView
                imageView.setScaleX(imageView.getScaleX() * 0.90);
                imageView.setScaleY(imageView.getScaleY() * 0.90);
            });

            imageView.setOnMouseReleased(event -> {
                // Reset the size of the ImageView
                imageView.setScaleX(imageView.getScaleX() * 1.11);
                imageView.setScaleY(imageView.getScaleY() * 1.11);
            });
        } else {
            System.err.println("ImageView is null. Cannot set mouse event handlers.");
        }
    }

    public void makeImageVisible(Node node) {
        node.setVisible(true);
    }

    public void makeImageInvisible(Node node) {
        node.setVisible(false);
    }

    public Image getLoadingGif() {
        URL imageFileLoading = getClass().getResource("/resources/loading.gif");
        Image imageLoading = new Image(imageFileLoading.toExternalForm());
        return imageLoading;
    }

    // For displaying images on ImageView
    // public void displayImages(String resourcePath, ImageView imageView) {
    // URL imageFile = getClass().getResource(resourcePath);
    // Image image = new Image(imageFile.toExternalForm());
    // imageView.setImage(image);
    // }

    public void displayImages(String resourcePath, ImageView imageView) {
        URL imageFile = getClass().getResource(resourcePath);
        Image image = new Image(imageFile.toExternalForm());
        if (imageView != null) {
            imageView.setImage(image);
        } else {
            System.err.println("ImageView is null. Cannot set image.");
        }
    }

    public void showAlert(String title, String content) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void showInfo(String title, String content) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public Boolean productExists(List<Product> allProducts, String productName) {
        for (Product product : allProducts) {
            if (productName.equalsIgnoreCase(product.getName())) {
                return true;
            }
        }
        return false;
    }

    public static void editProduct(List<Product> productList, String productName, String newName, String newCategory,
            double newPrice, int newstock, boolean isAvailable) {
        // Locate the product
        Product foundProduct = null;
        for (Product product : productList) {
            if (product.getName().equals(productName)) {
                foundProduct = product;
                break;
            }
        }

        // Update the fields
        if (foundProduct != null) {
            foundProduct.setName(newName);
            foundProduct.setCategory(newCategory);
            foundProduct.setPrice(newPrice);
            foundProduct.setImageName(newName + ".png");
            foundProduct.setStock(newstock);
            foundProduct.setisAvailable(isAvailable);

            System.out.println("Product updated successfully");
        } else {
            System.out.println("Product not found");
        }
    }

    public static void removeProduct(List<Product> productList, String productName) {
        // Locate the product
        Product foundProduct = null;
        for (Product product : productList) {
            if (product.getName().equals(productName)) {
                foundProduct = product;
                break;
            }
        }

        // Remove product
        if (foundProduct != null) {
            productList.remove(foundProduct);
            System.out.println("Product removed successfully");
        } else {
            System.out.println("Product not found");
        }

    }

    public boolean determineIsAvailable(float f) {
        // Add your logic here to determine isAvailable based on stock
        // For example, if stock > 0, then isAvailable is true, otherwise false
        return f >= 1;
    }

    public byte[] convertImageToByteArray(Image image) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Image convertByteArrayToImage(byte[] byteArray) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(byteArray);
            BufferedImage bufferedImage = ImageIO.read(inputStream);
            return SwingFXUtils.toFXImage(bufferedImage, null);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void saveProductImageLocally(String productName, byte[] imageData){
        saveImageLocally("src/resources/products/" + productName + ".png", imageData);
    }

    public void saveCategoryImageLocally(String categoryName, byte[] imageData){
        saveImageLocally("src/resources/category/" + categoryName + ".png", imageData);
    }

    public void saveImageLocally(String path, byte[] imageData) {
        try {
            // Convert the byte array to a JavaFX Image
            Image image = convertByteArrayToImage(imageData);

            // Convert the JavaFX Image to a BufferedImage
            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);

            // Save the BufferedImage locally
            String savePath = path;
            File outputFile = new File(savePath);

            // Ensure the directory exists
            outputFile.getParentFile().mkdirs();

            // Save the BufferedImage
            ImageIO.write(bufferedImage, "png", outputFile);

            System.out.println("Database image saved to: " + savePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadImage(Map<String, Image> imageCache, String resourcePath, ImageView imageView) {

        if (imageCache.containsKey(resourcePath)) {
            imageView.setImage(imageCache.get(resourcePath));
            return;
        }
        // Load the image from the resource
        URL imageFile = getClass().getResource(resourcePath);
        Image image = new Image(imageFile.toExternalForm());
        imageView.setImage(image);

        // Cache the loaded image
        imageCache.put(resourcePath, image);

    }

    private String filePath = "src/resources/products.txt";

    public void saveToFile(String productName, String unitPrice, double price, String category, String imagePath) {
        try {
            RandomAccessFile file = new RandomAccessFile(filePath, "rw");

            // Write the current date and time at the top of the file
            String currentDateAndTime = getCurrentDateAndTime();
            file.seek(0);
            file.writeBytes(currentDateAndTime + "\n");

            // Move the file pointer to the end for appending product data
            file.seek(file.length());

            // Append the product data to the file on a new line
            file.writeBytes(productName + " | " +
                    price + " | " +
                    category + " | " +
                    imagePath + "\n");

            file.close();
            System.out.println("Product data saved to: " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Date getDateFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line = reader.readLine();
            if (line != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                return dateFormat.parse(line);
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updateProductAvailability(String productName, boolean isAvailable) {
        try {
            Connection connection = DriverManager.getConnection(url, user, password);

            // Prepare the SQL statement for updating the isAvailable column
            String updateAvailabilityQuery = "UPDATE products SET isAvailable = ? WHERE productName = ?";
            try (PreparedStatement updateAvailabilityStatement = connection.prepareStatement(updateAvailabilityQuery)) {
                updateAvailabilityStatement.setBoolean(1, isAvailable);
                updateAvailabilityStatement.setString(2, productName);

                // Execute the SQL UPDATE query
                updateAvailabilityStatement.executeUpdate();
            }

            connection.close();
            System.out.println("Product availability updated successfully");

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println(e);
        }
    }

    // Add this method to update the product stock and availability in the database
    public void updateProductStock(String productName, float f) {
        try {
            Connection connection = DriverManager.getConnection(url, user, password);

            String updateStockQuery = "UPDATE products SET stock = ?, isAvailable = ? WHERE productName = ?";
            try (PreparedStatement updateStockStatement = connection.prepareStatement(updateStockQuery)) {
                updateStockStatement.setFloat(1, f);
                updateStockStatement.setBoolean(2, determineIsAvailable(f));
                updateStockStatement.setString(3, productName);

                updateStockStatement.executeUpdate();
            }

            // Check if the new stock is 0 and update availability accordingly
            if (f == 0) {
                updateProductAvailability(productName, false); // Set availability to false
            }

            connection.close();
            System.out.println("Product stock and availability updated successfully");

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println(e);
        }
    }

    private String getCurrentDateAndTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date now = new Date();
        return dateFormat.format(now);
    }

    public String getCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date now = new Date();
        return dateFormat.format(now);
    }

    public void saveToDatabase(String productName, String UnitPricing, double price, String category, String imageName, Image image,
            int stock, boolean isAvailable) {
        try {

            try (Connection connection = DriverManager.getConnection(url, user, password)) {
                // Check if the product already exists in the database
                String checkIfExistsQuery = "SELECT * FROM products WHERE productName = ?";
                try (PreparedStatement checkIfExistsStatement = connection.prepareStatement(checkIfExistsQuery)) {
                    checkIfExistsStatement.setString(1, productName);
                    ResultSet resultSet = checkIfExistsStatement.executeQuery();

                    if (resultSet.next()) {
                        // Product already exists, update the stock
                        int existingstock = resultSet.getInt("stock");

                        // Prepare the SQL statement for updating the existing record
                        String updateQuery = "UPDATE products SET productName = ?, UnitPricing = ?, price = ?, category = ?, imageData = ?, imageName = ?, stock = ?, isAvailable = ? WHERE productName = ?";
                        try (PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
                            updateStatement.setString(1, productName);
                            updateStatement.setString(2, productName);
                            updateStatement.setDouble(3, price);
                            updateStatement.setString(4, category);
                            updateStatement.setBytes(5, convertImageToByteArray(image));
                            updateStatement.setString(6, imageName);
                            updateStatement.setInt(7, existingstock + stock); // This should be the new total
                                                                              // stock
                            updateStatement.setBoolean(8, isAvailable);
                            // Execute the SQL UPDATE query
                            updateStatement.executeUpdate();
                        }

                        System.out.println("Product updated successfully");
                    } else {
                        // Product doesn't exist, insert a new record
                        String insertQuery = "INSERT INTO products (productName, UnitPricing, price, category, imageData, imageName , stock, isAvailable) VALUES (?, ?, ?, ?, ?, ?, ?,?)";
                        try (PreparedStatement insertStatement = connection.prepareStatement(insertQuery)) {
                            insertStatement.setString(1, productName);
                            insertStatement.setString(2, UnitPricing);
                            insertStatement.setDouble(3, price);
                            insertStatement.setString(4, category);
                            insertStatement.setBytes(5, convertImageToByteArray(image));
                            insertStatement.setString(6, imageName);
                            insertStatement.setInt(7, stock);
                            insertStatement.setBoolean(8, isAvailable);
                           

                            // Execute the SQL INSERT query
                            insertStatement.executeUpdate();
                        }

                        System.out.println("Product inserted successfully");
                    }
                }

            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Database error: " + e.getMessage());
        }
    }

    public void saveCategoryToDatabase(String categoryName, Image image){
        try {

            try (Connection connection = DriverManager.getConnection(url, user, password)) {
                // Check if the product already exists in the database
                String checkIfExistsQuery = "SELECT * FROM category WHERE name = ?";
                try (PreparedStatement checkIfExistsStatement = connection.prepareStatement(checkIfExistsQuery)) {
                    checkIfExistsStatement.setString(1, categoryName);
                    ResultSet resultSet = checkIfExistsStatement.executeQuery();

                    if (resultSet.next()) {
                        // Prepare the SQL statement for updating the existing record
                        String updateQuery = "UPDATE category SET name = ?, image = ? WHERE name = ?";
                        try (PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
                            updateStatement.setString(1, categoryName);
                            updateStatement.setBytes(2, convertImageToByteArray(image));

                            // Execute the SQL UPDATE query
                            updateStatement.executeUpdate();
                        }

                        System.out.println("Product updated successfully");
                    } else {
                        // Product doesn't exist, insert a new record
                        String insertQuery = "INSERT INTO category (name, image) VALUES (?, ?)";
                        try (PreparedStatement insertStatement = connection.prepareStatement(insertQuery)) {
                            insertStatement.setString(1, categoryName);
                            insertStatement.setBytes(2, convertImageToByteArray(image));

                            // Execute the SQL INSERT query
                            insertStatement.executeUpdate();
                        }

                        System.out.println("Product inserted successfully");
                    }
                }

            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Database error: " + e.getMessage());
        }

    }

    public void removeCategoryFromDatabase(String categoryName) {
        try {
            Connection connection = DriverManager.getConnection(url, user, password);

            // Prepare the SQL statement for deleting data from the database
            String deleteQuery = "DELETE FROM category WHERE name = '" + categoryName + "'";

            try (PreparedStatement preparedStatement = connection.prepareStatement(deleteQuery)) {
                // No need to set parameters for DELETE statement

                // Execute the SQL DELETE query
                int rowsAffected = preparedStatement.executeUpdate();

                if (rowsAffected <= 0) {
                    showAlert("Warning", "No product found with the given name in the database.");
                }
            }

            connection.close();

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println(e);
            showAlert("Error", "Failed to remove data from the database.");
        }
    }

    public void clearDB() {
        String tableName = "products";

        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            String deleteQuery = "DELETE FROM " + tableName;

            try (PreparedStatement preparedStatement = connection.prepareStatement(deleteQuery)) {
                int rowsAffected = preparedStatement.executeUpdate();
                System.out.println("Deleted " + rowsAffected + " rows from the table.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public static boolean isDatabaseConnected() {
        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            // If the connection is successful, return true
            return true;
        } catch (SQLException e) {
            // If an exception occurs, print an error message and return false
            System.err.println("Failed to connect to the database: " + e.getMessage());
            return false;
        }
    }

    public static String latestSave(Date dateFromFile, Date dateFromDB) {

        // Return if both have no date
        if (dateFromFile == null && dateFromDB == null) {
            return null;
        }

        // Use one of other is null
        if (dateFromFile == null || dateFromDB == null) {
            if (dateFromFile == null && dateFromDB != null) {
                return "db";
            } else if (dateFromFile != null && dateFromDB == null) {
                return "file";
            }
        }

        // Check which is latest
        int comparisonResult = dateFromFile.compareTo(dateFromDB);

        if (comparisonResult < 0) {
            System.out.println("Loaded from database");
            return "db";
        } else if (comparisonResult > 0) {
            System.out.println("Loaded from file");
            return "file";
        } else {
            System.out.println("Dates from file and DB are equal");
            return "db";
        }
    }

    public int getCurrentWeeklyId(){
        String query = "SELECT report_id FROM weekly_report ORDER BY report_date DESC LIMIT 1";
        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            if (resultSet.next()) {
                return resultSet.getInt("report_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public LocalDate getLastReportDate(){
        String query = "SELECT report_date FROM weekly_report ORDER BY report_date DESC LIMIT 1";
        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            if (resultSet.next()) {
                return resultSet.getDate("report_date").toLocalDate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public ObservableList<Receipt> getReceipts(){
        receiptList.clear();

        try {
            // Establish a connection to the database
            Connection connection = (Connection) DriverManager.getConnection(url, user, password);

            // Prepare SQL query
            String query = "SELECT * FROM receipt";
            PreparedStatement statement = connection.prepareStatement(query);

            // Execute query and get result set
            ResultSet resultSet = statement.executeQuery();

            // Process result set and populate receiptList
            while (resultSet.next()) {
                Receipt receipt = new Receipt(
                    resultSet.getInt("id"),
                    resultSet.getString("name"),
                    resultSet.getDate("date"),
                    resultSet.getString("time"),
                    resultSet.getString("address"),
                    resultSet.getString("paymentMethod"),
                    resultSet.getString("deliveryMethod"),
                    resultSet.getString("products"),
                    resultSet.getString("quantity"),
                    resultSet.getString("individual_price"),
                    resultSet.getString("total_cost"),
                    resultSet.getDouble("total_amount")
                );
                receiptList.add(receipt);
            }

            // Close resources
            resultSet.close();
            statement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle exception
        }

        return receiptList;
    }

    public ObservableList<Report> getReports(){
        reportList.clear();

        try {
            // Establish a connection to the database
            Connection connection = (Connection) DriverManager.getConnection(url, user, password);

            // Prepare SQL query
            String query = "SELECT * FROM weekly_report";
            PreparedStatement statement = connection.prepareStatement(query);

            // Execute query and get result set
            ResultSet resultSet = statement.executeQuery();

            // Process result set and populate receiptList
            while (resultSet.next()) {
                Report report = new Report(
                    resultSet.getInt("report_id"),
                    resultSet.getDate("report_date"),
                    resultSet.getString("receipt_ids"),
                    resultSet.getDouble("total_sales"),
                    resultSet.getDouble("units_sold"),
                    resultSet.getInt("no_of_customers"),
                    resultSet.getString("most_popular_product"),
                    resultSet.getString("most_popular_category")
                );
                reportList.add(report);
            }

            // Close resources
            resultSet.close();
            statement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle exception
        }

        return reportList;
    }
}
