package main;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import admin.AdminPasswordChangeController;
import admin.AdminPasswordController;
import admin.WeeklyReportController;
import admin.WeeklyReportGenerator;
import category.Category;
import category.categoryController;
import category.categoryEditController;
import javafx.scene.control.Label;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import receipt.FXMLDocumentController;

public class POSController extends AppPanels {
    @FXML
    private Label titleLabel;

    @FXML
    private ImageView logoNavBar;

    @FXML
    private Label categoryText;

    @FXML
    private Button cancelBtn;

    @FXML
    private TextField categoryDesc;

    @FXML
    private TextField categoryName;

    @FXML
    private Button saveBtn;

    @FXML
    private Button uploadButton;

    @FXML
    private ImageView addCategoryButton;

    @FXML
    private Label cat1_label;

    @FXML
    private Label cat2_label;

    @FXML
    private Label cat3_label;

    @FXML
    private Label cat4_label;

    @FXML
    private Label cat5_label;

    @FXML
    private Label cat6_label;

    @FXML
    private ImageView exitButton;

    @FXML
    private ImageView search;

    @FXML
    private AnchorPane mainPane;

    @FXML
    private ImageView loadingGif;

    @FXML
    private TextArea searchText;

    @FXML
    private GridPane productsGrid;

    @FXML
    private ImageView salesButton;

    @FXML
    private Label salesLabel;

    @FXML
    private ImageView weeklyButton;

    @FXML
    private Label weeklyLabel;

    @FXML
    private Label passwordLabel;

    @FXML
    private VBox categoryContainer;

    @FXML
    private ImageView adminToggle;
    private Boolean adminMode = false;
    private String password = "admin";
    private List<Node> adminButtons;

    @FXML
    private ImageView passwordButton;

    @FXML
    private Label total;
    private double totalAmount = 0.0;

    @FXML
    private Button checkout;

    private List<Product> allProducts = new ArrayList<>();
    private List<Product> displayProducts = new ArrayList<>();
    private List<String> categories = new ArrayList<>();
    private List<ImageView> productButtons = new ArrayList<>();
    private List<ImageView> buttonImages;

    @FXML
    private ListView<String> myCart;
    private HashSet<String> cartSet = new HashSet<>();

    @FXML
    private ImageView addProductButton;

    @FXML
    private Button clearProductBTN;

    @FXML
    private void clearProductBTNClicked(ActionEvent event) {
        addToGrid.clearCart(cartSet, myCart);
    }

    // Image cache map so images load faster
    private Map<String, Image> imageCache = new HashMap<>();

    // Map to store the association between category name and Category object
    private Map<String, Category> categoryMap = new HashMap<>();
    private String firstCategory = "";

    private AddToGrid addToGrid = new AddToGrid(this);

    private WeeklyReportGenerator weeklyReportGenerator = new WeeklyReportGenerator();

    @FXML
    private void initialize() {
        try {

            // Click animation
            buttonImages = Arrays.asList(exitButton,
                    addProductButton, salesButton,
                    passwordButton, addCategoryButton, weeklyButton);
            buttonImages.forEach(this::buttonAnimation);
            // Admin Buttons
            adminButtons = Arrays.asList(salesButton, salesLabel, passwordButton, passwordLabel, 
                                        categoryText, addCategoryButton, addProductButton, weeklyButton, weeklyLabel);

            if (isDatabaseConnected()) {
                initializeDatabaseProducts();
                initializeDatabaseCategories();
            } else {
                System.out.println("Database loading failed");
            }

            for (Product product : allProducts) {
                displayProducts.add(product);
            }

            displayImages("/resources/icon.png", logoNavBar);

            displayImages("/resources/exit.png", exitButton);
            displayImages("/resources/search.png", search);
            displayImages("/resources/add.png", addProductButton);
            displayImages("/resources/adminOff.png", adminToggle);
            displayImages("/resources/sales.png", salesButton);
            displayImages("/resources/sales.png", weeklyButton);
            displayImages("/resources/password.png", passwordButton);
            displayImages("/resources/add_transparent.png", addCategoryButton);

            //Weekly report (Only runs every sunday)
            LocalDate currentDate = LocalDate.now();
            int weeklyReportId = getCurrentWeeklyId();
            LocalDate lastReport = getLastReportDate();
            System.out.println("Last Weekly Report: " + lastReport);

            //Run if it's sunday and a report hasnt been made yet
            if (currentDate.getDayOfWeek() == DayOfWeek.SUNDAY && !lastReport.isEqual(currentDate)) {
                System.out.println("Weekly Report...");
                System.out.println("Current weekly id: " + weeklyReportId);
                weeklyReportGenerator.passValues(allProducts);
                weeklyReportGenerator.generateReport(weeklyReportId++);
            }

            //adminMode = true; // used for debugging
            displayAllProducts();
            displayAllCategories();
            getAdminPasswordFromDB();
            displayAdminButtons();
        } catch (NullPointerException e) {
            System.err.println("Error initializing controller: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void displayAllProducts() {
        // Prevent stacking of images
        productsGrid.getChildren()
                .removeIf(node -> node instanceof ImageView || node instanceof Label || node instanceof TextArea); 
                // Clear existing grid content
        productButtons.clear();

        if (displayProducts.isEmpty()) {
            return;
        }

        int index = 0;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 4; col++) {
                if (index < displayProducts.size()) {
                    Product currentProduct = displayProducts.get(index);

                    String imageURL = "/resources/products/" + currentProduct.getImageName();
                    String backgroundURL = "/resources/category/Empty.png";
                    String editURL = "/resources/edit.png";
                    String productName = currentProduct.getName();
                    Double productPrice = currentProduct.getPrice();

                    System.out.println(currentProduct.getName());

                    addToGrid.addBackgroundToGrid(backgroundURL, productsGrid, row, col);
                    addToGrid.addProductToGrid(imageURL, productsGrid, row, col, productButtons, cartSet, myCart,
                            currentProduct, imageCache);
                    addToGrid.addEditToGrid(editURL, productsGrid, row, col, productButtons, currentProduct, imageCache,
                            allProducts, categories, adminMode);

                    addToGrid.addLabelToGrid(productName, productsGrid, row, col);
                    addToGrid.addPriceToGrid(productPrice, productsGrid, row, col);
                    index++;
                }
            }
        }
        productButtons.forEach(this::buttonAnimation);
        System.out.println("=====All products displayed");
        System.out.println();

    }

    public void displayAllCategories(){
        categoryContainer.getChildren().clear();
        System.out.println();

        for(Map.Entry<String, Category> entry : categoryMap.entrySet()){
            Category category = entry.getValue();
            addCategoryEdit(entry.getKey(), categoryContainer, categoryMap);
            addCategoryToContainer(entry.getKey(), category.getImage(), categoryContainer, categoryMap);
        }
        
    }

    public void setCategory(String category) {
        displayProducts.clear();

        for (Product product : allProducts) {
            if (product.getCategory().equalsIgnoreCase(category)) {
                displayProducts.add(product);
            }
        }
        
        displayAllProducts();
    }

    @FXML
    private void handleSales() {

        try {
            FXMLLoader loader = new FXMLLoader(getClass()
                    .getResource("/admin/sales.fxml"));
            Parent root = loader.load();

            // SalesController salesController = loader.getController();

            Stage stage = new Stage();
            stage.setTitle("Sales");
            stage.setScene(new Scene(root, 1200.0, 600.0));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @FXML
    private void handleWeekly() {

        try {
            FXMLLoader loader = new FXMLLoader(getClass()
                    .getResource("/admin/weeklyReport.fxml"));
            Parent root = loader.load();

            WeeklyReportController weeklyReportController = loader.getController();
            weeklyReportController.passValues(allProducts);
            weeklyReportController.displayReport();

            Stage stage = new Stage();
            stage.setTitle("Weekly Report");
            stage.setScene(new Scene(root, 1200.0, 600.0));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @FXML
    private void handleAddProduct() {

        try {
            FXMLLoader loader = new FXMLLoader(getClass()
                    .getResource("addProduct.fxml"));
            Parent root = loader.load();

            AddProductController addProductController = loader.getController();
            addProductController.addCategoriesToDropdown();
            addProductController.addProductsList(allProducts);
            addProductController.setPOSController(this);

            Stage stage = new Stage();
            stage.setTitle("Add Product");
            stage.setScene(new Scene(root, 390.0, 500.0));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void updateTotalAmount(double price) {
        totalAmount += price;
        total.setText("Total: ₱ " + totalAmount);
    }

    public void returnStockandQuantityAmount() {
        totalAmount = 0.0;
        total.setText("Total: ₱ " + totalAmount);

        for (String cartItem : myCart.getItems()) {
            String[] parts = cartItem.split(" ");
            String productName = parts[0];
            productName = productName.substring(0, productName.length() - 1);

            for (Product product : allProducts) {
                if (product.getName().equalsIgnoreCase(productName)) {
                    product.setStock(product.getStock() + product.getQuantity());
                    product.setQuantity(0);
                }
            }
        }
    }

    @FXML
    private void handleMainClick(MouseEvent event) {
        mainPane.requestFocus();
    }

    @FXML // Exit button
    private void handleExit(MouseEvent event) {
        closeWindow(titleLabel);
    }

    @FXML
    private void handleSearch(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            String enteredText = searchText.getText();

            // Prevents line break
            String searchedText = enteredText.substring(0, enteredText.length() - 1);
            searchText.setText(searchedText);
            searchText.positionCaret(searchText.getText().length());

            // Search output for debug
            System.out.println("Entered Text: " + enteredText);

            // display all if empty, then return since other code is not needed
            if (enteredText.isBlank()) {
                System.out.println("empty");
                displayAllProducts();
                return;
            }

            // The actual search function
            List<Product> filteredProducts = allProducts.stream()
                    .filter(product -> product.getName().toLowerCase().contains(enteredText.trim().toLowerCase()))
                    .collect(Collectors.toList());

            updateProductsGrid(filteredProducts);
        }
    }

    @FXML
    private void handleAdminToggle(MouseEvent event) {
        if (adminMode) {
            displayImages("/resources/adminOff.png", adminToggle);
            adminMode = false;
            displayAdminButtons();
            displayAllCategories();
            displayAllProducts();

            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass()
                    .getResource("/admin/adminPassword.fxml"));
            Parent root = loader.load();

            AdminPasswordController adminPasswordController = loader.getController();
            adminPasswordController.setPOSController(this);
            adminPasswordController.setPassword(password);
            adminPasswordController.setAdminToggle(adminToggle);

            Stage stage = new Stage();
            stage.setTitle("Admin Password");
            stage.setScene(new Scene(root, 390.0, 190.0));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setAdminMode(Boolean mode) {
        adminMode = mode;
    }

    private void getAdminPasswordFromDB() {
        // default password
        String passwordFromDB = "admin";

        try {
            String url = "jdbc:mysql://localhost:3306/pos";
            String user = "root";
            String password = "";

            // Establish a connection to the database
            Connection connection = (Connection) DriverManager.getConnection(url, user, password);

            // Execute a query to retrieve products
            String selectQuery = "SELECT * FROM admin";
            try (PreparedStatement preparedStatement = (PreparedStatement) connection.prepareStatement(selectQuery)) {
                ResultSet resultSet = preparedStatement.executeQuery();

                while (resultSet.next()) {
                    passwordFromDB = resultSet.getString("password");
                    this.password = passwordFromDB;
                }
            }

            // Close the database connection
            connection.close(); 
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handlePasswordChange() {

        try {
            FXMLLoader loader = new FXMLLoader(getClass()
                    .getResource("/admin/adminPasswordChange.fxml"));
            Parent root = loader.load();

            AdminPasswordChangeController adminPasswordChangeController = loader.getController();
            adminPasswordChangeController.setPOSController(this);
            adminPasswordChangeController.setPassword(password);

            Stage stage = new Stage();
            stage.setTitle("Admin Password");
            stage.setScene(new Scene(root, 390.0, 325.0));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void displayAdminButtons() {
        adminButtons.forEach(button -> {
            button.setVisible(adminMode);
        });
    }

    @FXML
    private void handleAddCategory() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/category/category.fxml"));
            Parent root = loader.load();

            // Get the controller instance from the loader
            categoryController categoryController = loader.getController();

            categoryController.setPOSController(this);
            categoryController.setCategoryContainer(categoryContainer);
            categoryController.setCategoryMap(categoryMap);

            // Show the category window
            Stage stage = new Stage();
            stage.setTitle("Add Category");
            stage.setScene(new Scene(root, 338, 400));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading category.fxml: " + e.getMessage());
        }
    }

    // This method is called when the user adds a new category
    public void addNewCategory(String categoryName, Image categoryImage) {
        try {
            
            addCategoryToContainer(categoryName, categoryImage, categoryContainer, categoryMap);

            Category newCategory = new Category(categoryName, categoryImage);
            categoryMap.put(categoryName, newCategory);

            displayAllCategories();
        } catch (Exception e) {
            System.err.println("Error adding new category: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void addToCategoryMap(String name, Category category){
        categoryMap.put(name, category);
    }
    
    
    // Method to add a new category
    public void addCategoryToContainer(String categoryName, Image uploadedImage, VBox categoryContainer, Map<String, Category> categoryMap) {
        try {
            if (categoryContainer != null && categoryName != null && uploadedImage != null) {

                // Load the Empty.png image
                ImageView emptyImageView = new ImageView(
                        new Image(getClass().getResourceAsStream("/resources/category/Empty.png")));
                emptyImageView.setFitWidth(73.0); // Set the width
                emptyImageView.setFitHeight(73.0); // Set the height

                // Create ImageView for the uploaded image
                ImageView uploadedImageView = new ImageView(uploadedImage);
                uploadedImageView.setFitHeight(73.0);
                uploadedImageView.setFitWidth(73.0);

                // Create Label for the category name
                Label label = new Label(categoryName);
                label.setPrefWidth(125.0);
                label.setAlignment(Pos.CENTER); // Center the label text

                // Stack the uploaded image on top of the empty image using a StackPane
                StackPane imageStackPane = new StackPane();
                imageStackPane.getChildren().addAll(emptyImageView, uploadedImageView);

                // Add category to the map
                Category category = new Category(categoryName, uploadedImage);
                categoryMap.put(categoryName, category);
                
                emptyImageView.setOnMouseClicked(event -> setCategory(categoryName));
                uploadedImageView.setOnMouseClicked(event -> setCategory(categoryName));

                buttonAnimation(emptyImageView);
                buttonAnimation(uploadedImageView);

                // Create a VBox to stack the imageStackPane and label vertically
                VBox vbox = new VBox();
                vbox.getChildren().addAll(imageStackPane, label);
                vbox.setAlignment(Pos.CENTER); // Center the VBox horizontally

                // Add padding to the top of the VBox containing the imageStackPane and label
                VBox.setMargin(label, new Insets(5, 0, 0, 0));

                // Add the new category to the container
                categoryContainer.getChildren().add(vbox);
            } else {
                System.err.println("Error: categoryContainer is null or categoryName/uploadedImage is null");
            }
        } catch (Exception e) {
            System.err.println("Error adding new category: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void addCategoryEdit(String categoryName, VBox categoryContainer, Map<String, Category> categoryMap){
        try {
            if(!adminMode){
                return;
            }

            if (categoryContainer != null && categoryName != null) {
    
                // Load the Edit.png image
                ImageView editImageView = new ImageView(
                        new Image(getClass().getResourceAsStream("/resources/edit.png")));
                editImageView.setFitWidth(25.0); // Set the width
                editImageView.setFitHeight(25.0); // Set the height
    
                // Position the editImageView in the top-right corner
                StackPane.setAlignment(editImageView, Pos.TOP_RIGHT);
                StackPane.setMargin(editImageView, new Insets(-110, 15, 0, 0));
    
                editImageView.setOnMouseClicked(event -> handleEditCategory(categoryName, categoryMap));
                buttonAnimation(editImageView);
    
                // Create a StackPane to hold the editImageView
                StackPane editStackPane = new StackPane();
                editStackPane.getChildren().add(editImageView);
    
                // Create a VBox to stack the imageStackPane and label vertically
                VBox vbox = new VBox();
                vbox.getChildren().add(editStackPane);
    
                // Add the new edit control to the container
                categoryContainer.getChildren().add(vbox);
            } else {
                System.err.println("Error: categoryContainer is null or categoryName/uploadedImage is null");
            }
        } catch (Exception e) {
            System.err.println("Error adding edit button: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void handleEditCategory(String categoryName, Map<String, Category> categoryMap) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/category/categoryEditor.fxml"));
            Parent root = loader.load();

            // Get the controller instance from the loader
            categoryEditController editController = loader.getController();

            // Pass categoryMap to the edit controller
            editController.setCategoryMap(categoryMap);
            editController.setPOSController(this);

            // Find the category object based on its name
            Category category = categoryMap.get(categoryName);
            if (category != null) {
                editController.setCategory(category);
                editController.setImage(category.getImage());

                // Show the edit category window
                Stage stage = new Stage();
                stage.setTitle("Edit Category");
                stage.setScene(new Scene(root, 338, 400));
                stage.show();
            } else {
                showAlert("Error", "Category not found!");
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading categoryEditor.fxml: " + e.getMessage());
        }
    }

    public void removeCategoryVBox(VBox vbox) {
        categoryContainer.getChildren().remove(vbox);
    }

    private void updateProductsGrid(List<Product> filteredProducts) {
        // Clear existing grid content
        productsGrid.getChildren()
                .removeIf(node -> node instanceof ImageView || node instanceof Label || node instanceof TextArea);

        // Reinitialize these to prevent them from disappearing
        addToGrid = new AddToGrid(this);
        productButtons.clear();

        if (filteredProducts.isEmpty()) {
            System.out.println("empty list");
        }

        int index = 0;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 4; col++) {
                if (index < filteredProducts.size()) {
                    Product currentProduct = filteredProducts.get(index);

                    String imageURL = "/resources/products/" + currentProduct.getImageName();
                    String backgroundURL = "/resources/category/Empty.png";
                    String productName = currentProduct.getName();
                    Double productPrice = currentProduct.getPrice();

                    System.out.println("Product Found: " + productName); // Debug statement

                    addToGrid.addBackgroundToGrid(backgroundURL, productsGrid, row, col);
                    addToGrid.addProductToGrid(imageURL, productsGrid, row, col, productButtons, cartSet, myCart,
                            currentProduct, imageCache);

                    addToGrid.addLabelToGrid(productName, productsGrid, row, col);
                    addToGrid.addPriceToGrid(productPrice, productsGrid, row, col);

                    index++;
                }
            }
        }

        productButtons.forEach(this::buttonAnimation);
    }

    private void initializeDatabaseProducts() {

        allProducts.clear();

        try {
            String url = "jdbc:mysql://localhost:3306/pos";
            String user = "root";
            String password = "";

            // Establish a connection to the database
            Connection connection = (Connection) DriverManager.getConnection(url, user, password);

            // Execute a query to retrieve products
            String selectQuery = "SELECT * FROM products";
            try (PreparedStatement preparedStatement = (PreparedStatement) connection.prepareStatement(selectQuery)) {
                ResultSet resultSet = preparedStatement.executeQuery();

                System.out.println("Retrieved products from the database:");
                while (resultSet.next()) {
                    String productName = resultSet.getString("productName");
                    String unitPricing = resultSet.getString("unitPricing");
                    double price = resultSet.getDouble("price");
                    String category = resultSet.getString("category");
                    String imageName = resultSet.getString("imageName");
                    int stock = resultSet.getInt("stock");
                    Boolean isAvailable = resultSet.getBoolean("isAvailable");

                    // get image
                    byte[] imageData = resultSet.getBytes("imageData");
                    if (imageData != null) {
                        saveProductImageLocally(productName, imageData);
                    } else {
                        // Handle the case when imageData is null (optional, depending on your
                        // requirements)
                        System.out.println("Warning: Image data is null for product: " + productName);
                    }

                    // Add the retrieved product to the list
                    allProducts.add(new Product(productName, unitPricing, price, category, imageName, stock, isAvailable));

                }
            }
            // Close the database connection
            System.out.println();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void initializeDatabaseCategories() {

        categoryMap.clear();

        try {
            String url = "jdbc:mysql://localhost:3306/pos";
            String user = "root";
            String password = "";

            // Establish a connection to the database
            Connection connection = (Connection) DriverManager.getConnection(url, user, password);

            // Execute a query to retrieve products
            String selectQuery = "SELECT * FROM category";
            try (PreparedStatement preparedStatement = (PreparedStatement) connection.prepareStatement(selectQuery)) {
                ResultSet resultSet = preparedStatement.executeQuery();

                System.out.println("Retrieved categories from the database:");
                while (resultSet.next()) {
                    String categoryName = resultSet.getString("name");

                    // get image
                    byte[] imageData = resultSet.getBytes("image");
                    if (imageData != null) {
                        saveCategoryImageLocally(categoryName, imageData);
                    } else {
                        // Handle the case when imageData is null (optional, depending on your
                        // requirements)
                        System.out.println("Warning: Image data is null for product: " + categoryName);
                    }

                    // Add the retrieved product to the list
                    Image image = convertByteArrayToImage(imageData);
                    addNewCategory(categoryName, image);
                    
                    if(firstCategory.equals("")){
                        firstCategory = categoryName;
                    }
                }
            }
            // Close the database connection
            System.out.println();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCheckoutButton(MouseEvent event) {

        try {
            FXMLLoader loader = new FXMLLoader(getClass()
                    .getResource("/receipt/FXMLDocument.fxml"));
            Parent root = loader.load();

            FXMLDocumentController fxmlDocumentController = loader.getController();
            fxmlDocumentController.passValues(cartSet, myCart, totalAmount, allProducts, addToGrid);

            Stage stage = new Stage();
            stage.setTitle("Checkout");
            stage.setScene(new Scene(root, 390.0, 500.0));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ListView<String> getCart() {
        return myCart;
    }

    public Map<String, Category> getCategoryMap(){
        return categoryMap;
    }

}
