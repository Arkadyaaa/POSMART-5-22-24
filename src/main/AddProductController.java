package main;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

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

public class AddProductController extends AppPanels {

    @FXML
    private TextField productNameField;

    @FXML
    private TextField priceField;

    @FXML
    private ComboBox<String> UnitPrice;

    @FXML
    private ComboBox<String> categoryDropdown;

    @FXML
    private Button uploadButton;

    @FXML
    private Button addProductButton;

    @FXML
    private Button cancel;

    @FXML
    private ImageView uploadedImage;

    @FXML
    private TextField stockField;

    @FXML
    private ImageView loadingGif;

    private List<Product> allProducts = new ArrayList<>();
    private POSController posController;

    @FXML
    private void initialize() {

        // Placeholder image until user adds an image
        URL imageFilePlaceholder = getClass().getResource("/resources/addImage.png");
        Image imagePlaceholder = new Image(imageFilePlaceholder.toExternalForm());
        uploadedImage.setImage(imagePlaceholder);

        // Initialize loading gif
        loadingGif.setImage(getLoadingGif());
        loadingGif.setVisible(false);

        // Initialize UnitPrice ComboBox with 'pcs' and 'kg'
        UnitPrice.getItems().addAll("pcs", "kg");
    }

    public void addCategoriesToDropdown() {
        categoryDropdown.getItems().clear(); // Clear existing items

        try {
            String url = "jdbc:mysql://localhost:3306/pos";
            String user = "root";
            String password = "";

            // Establish a connection to the database
            Connection connection = (Connection) DriverManager.getConnection(url, user, password);

            // Execute a query to retrieve categories
            String selectQuery = "SELECT name FROM category";
            try (PreparedStatement preparedStatement = (PreparedStatement) connection.prepareStatement(selectQuery)) {
                ResultSet resultSet = preparedStatement.executeQuery();

                System.out.println("Retrieved categories from the database:");
                while (resultSet.next()) {
                    String categoryName = resultSet.getString("name");
                    categoryDropdown.getItems().add(categoryName);
                }
            }
            // Close the database connection
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addProductsList(List<Product> allProducts) {
        this.allProducts = allProducts;
    }

    public void setPOSController(POSController posController) {
        this.posController = posController;
    }

    @FXML
    private void printDetails(ActionEvent event) {
        System.out.println("Product Name: " + productNameField.getText());
        System.out.println("Unit Price: " + UnitPrice.getValue());
        System.out.println("Price: " + priceField.getText());
        System.out.println("Category: " + categoryDropdown.getValue());
        System.out.println("Stock: " + stockField.getText());

        // Get stock from the input field
        int stock;
        try {
            stock = Integer.parseInt(stockField.getText());
        } catch (NumberFormatException e) {
            showAlert("Error", "Please enter a valid stock.");
            return;
        }

        // Check if fields are not empty
        if (!fieldsAreValid()) {
            showAlert("Error", "Make sure all of the fields have been populated.");
            return;
        }
        if (productExists(allProducts, productNameField.getText())) {
            showAlert("Error", "Product name already exists");
            return;
        }

        // Check if price is only numbers
        String priceText = priceField.getText();
        if (!priceIsValid(priceText)) {
            showAlert("Error", "Make sure the price is valid");
            return;
        }

        // Check if stock is a valid number
        String stockText = stockField.getText();
        if (!stockIsValid(stockText)) {
            showAlert("Error", "Make sure the stock is a valid number");
            return;
        }

        String productName = productNameField.getText();
        String category = categoryDropdown.getValue();
        String UnitPricing = UnitPrice.getValue();
        double price = Double.parseDouble(priceText);

        saveCroppedImage(uploadedImage.getImage(), productName);
        String imageName = productName + ".png";
        allProducts.add(new Product(productName, UnitPricing, price, category, imageName, stock, determineIsAvailable(stock)));

        // Make sure the image is loaded first before continuing
        setLoadingGif(true);
        boolean success = false;
        while (!success) {
            try {
                System.out.println(
                        "Image Path: " + getClass().getResource("/resources/products/" + imageName).toExternalForm());
                posController.setCategory(category);
                posController.displayAllProducts();

                saveToDatabase(productName, UnitPricing, price, category, imageName, uploadedImage.getImage(), stock,
                        determineIsAvailable(stock));
                saveToFile(productName,UnitPricing, price, category, imageName);

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

    private boolean stockIsValid(String stockText) {
        return stockText.matches("\\d+");
    }

    @FXML
    private void handleUploadButton(MouseEvent event) {
        // FileChooser
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Image File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.webp"));

        // Show the FileChooser
        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            // Load the selected image
            Image selectedImage = new Image(selectedFile.toURI().toString());

            // crop image to square
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
        try {
            // Get the snapshot of the ImageView
            ImageView imageView = new ImageView(image);
            Image snapshot = imageView.snapshot(null, null);

            // Define the path where the image will be saved
            String savePath = "src/resources/products/" + productName + ".png";

            // Convert the snapshot to a file and save it
            File outputFile = new File(savePath);
            ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png", outputFile);

            System.out.println("Cropped image saved to: " + savePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean fieldsAreValid() {
        return !productNameField.getText().trim().isEmpty()
                && !priceField.getText().trim().isEmpty()
                && categoryDropdown.getValue() != null
                && UnitPrice.getValue() != null;
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
}
