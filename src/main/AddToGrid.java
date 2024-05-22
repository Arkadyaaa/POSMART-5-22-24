package main;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AddToGrid extends AppPanels {

    private POSController posController; // Add a reference to POSController

    public AddToGrid(POSController posController) {
        this.posController = posController;
    }

    public void addBackgroundToGrid(String resourcePath, GridPane productsGrid,
            int row, int col) {
        URL imageFile = getClass().getResource(resourcePath);
        ImageView imageView = new ImageView(imageFile.toExternalForm());

        // Specify row and column indices
        GridPane.setRowIndex(imageView, row);
        GridPane.setColumnIndex(imageView, col);

        // Add the ImageView to the GridPane
        productsGrid.getChildren().add(imageView);

        // Size and position
        imageView.setFitWidth(96);
        imageView.setFitHeight(95);

        setMiddle(imageView, productsGrid, col);
        imageView.setTranslateY(-25);
    }

    public void addProductToGrid(String resourcePath, GridPane productsGrid, int row, int col,
            List<ImageView> productButtons, HashSet<String> cartSet, ListView<String> myCart, Product currentProduct, Map<String, Image> imageCache) {

        ImageView imageView = new ImageView();
        ImageView outOfStock = new ImageView();

        // cache
        loadImage(imageCache, resourcePath, imageView);
        loadImage(imageCache, "/resources/outOfStock.png", outOfStock);

        // Specify row and column indices
        GridPane.setRowIndex(imageView, row);
        GridPane.setColumnIndex(imageView, col);

        GridPane.setRowIndex(outOfStock, row);
        GridPane.setColumnIndex(outOfStock, col);

        // Add the ImageView to the GridPane
        productsGrid.getChildren().add(imageView);

        if(!determineIsAvailable(currentProduct.getStock())){ 
            productsGrid.getChildren().add(outOfStock);

            outOfStock.setOnMouseClicked(event -> handleProductClick(currentProduct, cartSet, myCart));
        }

        imageView.setOnMouseClicked(event -> handleProductClick(currentProduct, cartSet, myCart));

        productButtons.add(imageView);

        // Size and position
        imageView.setFitWidth(80);
        imageView.setFitHeight(80);

        outOfStock.setFitWidth(80);
        outOfStock.setFitHeight(80);

        setMiddle(imageView, productsGrid, col);
        setMiddle(outOfStock, productsGrid, col);
        imageView.setTranslateY(-25);
        outOfStock.setTranslateY(-25);
    }

    private void handleProductClick(Product currentProduct, HashSet<String> cartSet, ListView<String> myCart) {
    TextInputDialog dialog = new TextInputDialog("1");
    dialog.setTitle("Enter the quantity for " + currentProduct.getName());
    dialog.setHeaderText("Stock: " + currentProduct.getStock());
    dialog.setContentText("Quantity:");
    

    Optional<String> result = dialog.showAndWait();

    result.ifPresent(quantityStr -> {
        try {
            int quantity = Integer.parseInt(quantityStr);
            if (quantity <= 0) {
                showAlert("Invalid Quantity", "Please enter a valid quantity.");
                return;
            }

            String productName = currentProduct.getName();

            // Check if the product is still in stock
            if (currentProduct.getStock() >= quantity) {
                if (!cartSet.contains(productName)) {
                    cartSet.add(productName);
                } else {
                    for (String cartItem : myCart.getItems()) {
                        if (cartItem.startsWith(productName)) {
                            myCart.getItems().remove(cartItem);
                            break;
                        }
                    }
                }

                currentProduct.setQuantity(currentProduct.getQuantity() + quantity);
                currentProduct.setStock(currentProduct.getStock() - quantity); // Decrement stock

                // Check if the new stock is 0 and update availability accordingly
                if (currentProduct.getStock() == 0) {
                    posController.displayAllProducts();
                    currentProduct.setisAvailable(false);
                }

                float cartProductQuantity = currentProduct.getQuantity();
                double productPrice = currentProduct.getPrice();
                double totalPrice = productPrice * cartProductQuantity;

                // Format the total price to display currency symbol and align to the right
                String formattedPrice = String.format("₱ %.2f", totalPrice);
                String cartItemText = String.format(
                        "%s: %.2f %60s",
                        productName,
                        cartProductQuantity,
                        formattedPrice);

                myCart.getItems().add(cartItemText);

                // Update the total amount in POSController
                posController.updateTotalAmount(currentProduct.getPrice() * quantity);
            } else {
                showAlert("Insufficient Stock", "There is not enough stock for the requested quantity.");
            }
        } catch (NumberFormatException e) {
            showAlert("Invalid Quantity", "Please enter a valid number for quantity.");
        }
    });
}

    public void addEditToGrid(String resourcePath ,GridPane productsGrid, int row, int col, List<ImageView> productButtons,
            Product currentProduct, Map<String, Image> imageCache, List<Product> allProducts, List<String> categories, Boolean adminMode){

        if(!adminMode){
            return;
        }

        ImageView imageView = new ImageView();
        imageView.getStyleClass().add("imageButton");
        
        loadImage(imageCache, resourcePath, imageView);

        // Specify row and column indices
        GridPane.setRowIndex(imageView, row);
        GridPane.setColumnIndex(imageView, col);

        // Add the ImageView to the GridPane
        productsGrid.getChildren().add(imageView);

        imageView.setOnMouseClicked(event -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass()
                        .getResource("editProduct.fxml"));
                Parent root = loader.load();

                EditProductController editProductController = loader.getController();
                editProductController.setPOSController(this.posController);
                editProductController.addCategoriesToDropdown(categories);
                editProductController.addProductsList(allProducts);
                editProductController.addCurrentProduct(currentProduct);
                editProductController.addImageCache(imageCache);

                Stage stage = new Stage();
                stage.setTitle("Edit Product");
                stage.setScene(new Scene(root, 390.0, 500.0));
                stage.show();
            } catch (Exception e) {
                e.printStackTrace();
            }

        });

        productButtons.add(imageView);

        // Size and position
        imageView.setFitWidth(30);
        imageView.setFitHeight(30);

        setMiddle(imageView, productsGrid, col);
        Platform.runLater(() -> {
            imageView.setTranslateX(imageView.getTranslateX() + 45);
            imageView.setTranslateY(-65);
        });
    }

    public void addLabelToGrid(String name, GridPane productsGrid, int row, int col) {
        Label productName = new Label(name);
        productName.setId("product-label"); // css

        // Specify row and column indices
        GridPane.setRowIndex(productName, row);
        GridPane.setColumnIndex(productName, col);

        // Add the ImageView to the GridPane
        productsGrid.getChildren().add(productName);

        // pos
        setMiddle(productName, productsGrid, col);
        productName.setTranslateY(37);
    }

    public void addPriceToGrid(Double price, GridPane productsGrid, int row, int col) {
        Label productPrice = new Label("P" + price);
        productPrice.setId("product-price"); // css

        // Specify row and column indices
        GridPane.setRowIndex(productPrice, row);
        GridPane.setColumnIndex(productPrice, col);

        // Add the ImageView to the GridPane
        productsGrid.getChildren().add(productPrice);

        // pos
        setMiddle(productPrice, productsGrid, col);
        productPrice.setTranslateY(55);
    }

    // Used to center the label
    private void setMiddle(Node node, GridPane productsGrid, int col) {
        // Sets the translation after the layout pass
        node.boundsInLocalProperty().addListener((observable, oldBounds, newBounds) -> {
            double gridCenter = 80; // fixed for now since .getWidth() / 2 won't work properly
            double middle = gridCenter - newBounds.getWidth() / 2;

            // Use newBounds.getWidth() directly for translation
            node.setTranslateX(middle);
        });

        Platform.runLater(() -> {
            Bounds bounds = node.getLayoutBounds();
            double gridCenter = 80; // fixed for now since .getWidth() / 2 wont work properly
            double middle = gridCenter - bounds.getWidth() / 2;

            node.setTranslateX(middle);
        });
    }

    public void clearCart(HashSet<String> cartSet, ListView<String> myCart) {
        posController.returnStockandQuantityAmount();
        posController.displayAllProducts();

        cartSet.clear();
        myCart.getItems().clear();
    }
}
