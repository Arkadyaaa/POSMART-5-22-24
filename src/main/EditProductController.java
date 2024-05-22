package main;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;

public class EditProductController extends AppPanels {

    @FXML
    private TextField productNameField;

    @FXML
    private TextField priceField;

    @FXML
    private ComboBox<String> categoryDropdown;

    @FXML
    private Button uploadButton;

    @FXML
    private Button addProductButton;

    @FXML
    private Button removeProductButton;

    @FXML
    private Button cancel;

    @FXML
    private ImageView uploadedImage;

    @FXML
    private ImageView loadingGif;

    @FXML
    private TextField stockField;

    private List<Product> allProducts = new ArrayList<>();
    private Product currentProduct;
    private POSController posController;
    private Map<String, Image> imageCache = new HashMap<>();
    private String originalProductName;

    @FXML
    private void initialize() {

        // Initialize loading gif
        loadingGif.setImage(getLoadingGif());
        loadingGif.setVisible(false);

        // Make sure product gets initiated first before populating fields
        Platform.runLater(() -> {
            productNameField.setText("" + currentProduct.getName());
            categoryDropdown.setValue("" + currentProduct.getCategory());

            // Use for deleting
            originalProductName = productNameField.getText();
            

            String priceString = "" + currentProduct.getPrice();
            String trimmedPrice = priceString.substring(0, priceString.length() - 2);
            priceField.setText(trimmedPrice);

            stockField.setText(String.valueOf(currentProduct.getStock()));

            String imagePath = "/resources/products/" + currentProduct.getImageName();
            displayImages(imagePath, uploadedImage);
        });

    }

    public void addCategoriesToDropdown(List<String> categories) {
        categoryDropdown.getItems().addAll(categories);
    }

    public void addProductsList(List<Product> allProducts) {
        this.allProducts = allProducts;
    }

    public void addCurrentProduct(Product currentProduct) {
        this.currentProduct = currentProduct;
    }

    public void setPOSController(POSController posController) {
        this.posController = posController;
    }

    public void addImageCache(Map<String, Image> imageCache) {
        this.imageCache = imageCache;
    }

    @FXML
    private void printDetails(ActionEvent event) {
        System.out.println("Product Name: " + productNameField.getText());
        System.out.println("Price: " + priceField.getText());
        System.out.println("Category: " + categoryDropdown.getValue());

        // Check if fields are not empty
        if (!fieldsAreValid()) {
            showAlert("Error", "Make sure all of the fields have been populated.");
            return;
        }

        // Check if price and stock are valid numbers
        String priceText = priceField.getText();
        String stockText = stockField.getText();
        if (!priceIsValid(priceText)) {
            showAlert("Error", "Make sure the price is valid");
            return;
        }

        if (!stockIsValid(stockText)){
            showAlert("Error", "Make sure the stock is valid");
            return;
        }

        String productName = productNameField.getText();
        String oldName = currentProduct.getName();
        String category = categoryDropdown.getValue();
        double price = Double.parseDouble(priceText);
        int stock = Integer.parseInt(stockText);

        saveCroppedImage(uploadedImage.getImage(), productName);
        String imageName = productName + ".png";

        // Update product details including stock and isAvailable
        boolean isAvailable = determineIsAvailable(stock);
        editProduct(allProducts, oldName, productName, category, price, stock, isAvailable);

        // Make sure the image is loaded first before continuing
        setLoadingGif(true);
        boolean success = false;
        while (!success) {
            try {
                System.out.println(
                        "Image Path: " + getClass().getResource("/resources/products/" + imageName).toExternalForm());

                // Remove image from cache
                String oldImageKey = "/resources/products/" + oldName + ".png";
                imageCache.remove(oldImageKey);

                // Convert Image to byte array
                byte[] imageData = convertImageToByteArray(uploadedImage.getImage());

                // Update database including stock and isAvailable
                editDatabase(oldName, productName, price, category, imageData, imageName, stock, isAvailable);

                Platform.runLater(() -> {
                    posController.displayAllProducts();
                });

                success = true;
                closeWindow(productNameField);
            } catch (NullPointerException e) {
                System.err.println("Failed load image: " + e.getMessage());

                // Sleep for a while before retrying
                try {
                    Thread.sleep(1000); // Sleep for 1 second, adjust if needed
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        if (!success) {
            setLoadingGif(false);
            showAlert("Error:", "Failed loading image, please retry");
        }
    }

    @FXML
    private void handleRemoveProduct(ActionEvent event) {
        System.out.println("Removed product: " + originalProductName);
        String currentCategory = currentProduct.getCategory();
        removeProduct(allProducts, originalProductName);
        removeFromDatabase(originalProductName);

        // Remove image from cache
        String oldImageKey = "/resources/products/" + originalProductName + ".png";
        imageCache.remove(oldImageKey);

        Platform.runLater(() -> {
            posController.setCategory(currentCategory);
            posController.displayAllProducts();
        });
        closeWindow(productNameField);

    }

    @FXML
    private void handleUploadButton(MouseEvent event) {
        // Create a FileChooser
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Image File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.webp"));

        // Show the FileChooser dialog
        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            // Load the selected image
            Image selectedImage = new Image(selectedFile.toURI().toString());

            // Call the cropToSquare method to handle the cropping
            cropToSquare(selectedImage, uploadedImage);
        }
    }

    private void cropToSquare(Image selectedImage, ImageView imageView) {
        // Calculate the dimensions for the square crop
        double minSide = Math.min(selectedImage.getWidth(), selectedImage.getHeight());
        double x = (selectedImage.getWidth() - minSide) / 2;
        double y = (selectedImage.getHeight() - minSide) / 2;

        // Create a rectangle to define the viewport
        Rectangle2D viewportRect = new Rectangle2D(x, y, minSide, minSide);

        // Apply the square crop to the image
        imageView.setImage(selectedImage);
        imageView.setViewport(viewportRect);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.setCache(true);
    }

    private void saveCroppedImage(Image image, String productName) {

        // Define the path where the image will be saved
        String savePath = "src/resources/products/" + productName + ".png";

        try {
            // Get the snapshot of the ImageView
            ImageView imageView = new ImageView(image);
            Image snapshot = imageView.snapshot(null, null);

            // Convert the snapshot to a file and save it
            File outputFile = new File(savePath);
            ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png", outputFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Cropped image saved to: " + savePath);
    }

    private boolean fieldsAreValid() {
        return !productNameField.getText().trim().isEmpty()
                && !priceField.getText().trim().isEmpty()
                && categoryDropdown.getValue() != null;
    }

    private boolean stockIsValid(String stock) {
        return stock.matches("\\d+");
    }

    private boolean priceIsValid(String price) {
        return price.matches("\\d+");
    }

    private void setLoadingGif(Boolean state) {
        loadingGif.setVisible(state);
    }

    @FXML
    private void closeWindowButton(ActionEvent event) {
        closeWindow(productNameField);
    }

    private void editDatabase(String oldName, String newName, double price, String category, byte[] imageData,
            String imageName, int stock, boolean isAvailable) {
        try {
            String url = "jdbc:mysql://localhost:3306/pos";
            String user = "root";
            String password = "";
            Connection connection = DriverManager.getConnection(url, user, password);

            // Prepare the SQL statement for updating data in the database
            String updateQuery = "UPDATE products SET price = ?, category = ?, productName = ?, imageData = ?, imageName = ?, stock = ?, isAvailable = ? WHERE productName = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {
                preparedStatement.setDouble(1, price);
                preparedStatement.setString(2, category);
                preparedStatement.setString(3, newName);
                preparedStatement.setBytes(4, imageData);
                preparedStatement.setString(5, imageName);
                preparedStatement.setInt(6, stock);
                preparedStatement.setBoolean(7, isAvailable);
                preparedStatement.setString(8, oldName);

                // Execute the SQL UPDATE query
                preparedStatement.executeUpdate();
            }

            connection.close();
            showInfo("Success", "Product details updated in the database.");

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println(e);
            showAlert("Error", "Failed to update data in the database.");
        }
    }

    private void removeFromDatabase(String productName) {
        try {
            String url = "jdbc:mysql://localhost:3306/pos";
            String user = "root";
            String password = "";
            Connection connection = DriverManager.getConnection(url, user, password);

            // Prepare the SQL statement for deleting data from the database
            String deleteQuery = "DELETE FROM products WHERE productName = '" + productName + "'";

            try (PreparedStatement preparedStatement = connection.prepareStatement(deleteQuery)) {
                // No need to set parameters for DELETE statement

                // Execute the SQL DELETE query
                int rowsAffected = preparedStatement.executeUpdate();

                if (rowsAffected > 0) {
                    showAlert("Success", "Product removed from the database.");
                } else {
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

}