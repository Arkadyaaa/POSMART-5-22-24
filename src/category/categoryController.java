// CategoryController.java

package category;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import main.AppPanels;
import main.POSController;
import javafx.scene.image.Image;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class categoryController extends AppPanels {

    @FXML
    private Button cancelBtn;

    @FXML
    private TextField categoryName;

    @FXML
    private Button saveBtn;

    @FXML
    private Button uploadButton;

    @FXML
    private ImageView imageCategory;

    @FXML
    private VBox categoryContainer;

    // Path to the placeholder image
    private final String placeholderImagePath = "/resources/category/Grocery.png";

    // Reference to the POSController
    private POSController posController;

    public void setCategoryContainer(VBox categoryContainer) {
        this.categoryContainer = categoryContainer;
    }

    // Set the reference to POSController
    public void setPOSController(POSController posController) {
        this.posController = posController;
    }

    // Map to store the association between category name and Category object
    private Map<String, Category> categoryMap = new HashMap<>();

    public void setCategoryMap(Map<String, Category> categoryMap){
        this.categoryMap = categoryMap;
    }

    @FXML
    public void initialize() {
        // Set the placeholder image initially
        Image placeholderImage = new Image(getClass().getResourceAsStream(placeholderImagePath));
        imageCategory.setImage(placeholderImage);

        if (categoryContainer == null) {
            categoryContainer = new VBox();
        }
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) cancelBtn.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleUploadImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Image File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.webp", "*.jpeg"));
        File selectedFile = fileChooser.showOpenDialog(uploadButton.getScene().getWindow());
        if (selectedFile != null) {
            // Load the selected image file into the ImageView
            Image image = new Image(selectedFile.toURI().toString());

            // Create a new ImageView instance to ensure that a new Image is set
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(imageCategory.getFitWidth());
            imageView.setFitHeight(imageCategory.getFitHeight());

            // Set the new ImageView to the imageCategory ImageView
            imageCategory.setImage(imageView.getImage());
        }
    }

    @FXML
    private void handleSaveCategory() {
        String categoryValue = categoryName.getText();
        Image image = imageCategory.getImage();

        if(categoryValue == null || categoryValue.isEmpty() || image == null){
            showAlert("Error", "Please provide both category name and image.");
            return;
        } else if(categoryMap.containsKey(categoryValue)){
            showAlert("Error", categoryValue + " already exists");
            return;
        }

        // Add category
        posController.addNewCategory(categoryValue, image);
        saveCategoryToDatabase(categoryValue, image);

        // Close category window
        Stage stage = (Stage) saveBtn.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleRemoveCategory() {
        // Remove the elements of the category from the UI
    }

}
