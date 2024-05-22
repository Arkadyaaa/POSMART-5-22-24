package category;

import java.io.File;
import java.util.Map;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javafx.stage.FileChooser;
import main.AppPanels;
import main.POSController;

public class categoryEditController extends AppPanels{

    @FXML
    private Button cancelBtn;

    @FXML
    private ImageView imageCategory;

    @FXML
    private TextField newCategoryName;

    @FXML
    private Button newImage;

    @FXML
    private Button removeCategory;

    @FXML
    private Button saveChanges;

    // Path to the placeholder image
    private final String placeholderImagePath = "/resources/category/Grocery.png";

    private Map<String, Category> categoryMap; // Declare categoryMap variable
    private Category category; // Declare category variable

    // Setter method for categoryMap
    public void setCategoryMap(Map<String, Category> categoryMap) {
        this.categoryMap = categoryMap;
    }

    // Setter method for category
    public void setCategory(Category category) {
        this.category = category;
        newCategoryName.setText(category.getName());
    }

    public void setImage(Image image){
        this.imageCategory.setImage(image);
    }

    private POSController posController;

    public void setPOSController(POSController posController){
        this.posController = posController; 
    }

    @FXML
    public void initialize() {
        // Set the placeholder image if category doesnt have image
        Image placeholderImage = new Image(getClass().getResourceAsStream(placeholderImagePath));
        imageCategory.setImage(placeholderImage);
    }

    @FXML
    private void saveChangesAction() {
        if (category != null) {
            // Retrieve the new category name from the TextField
            String oldName = category.getName();
            String newName = newCategoryName.getText().trim();

            // Check for errors
            if(categoryMap.containsKey(newName)){
                showAlert("Error", newName + " already exists");
                return;
            }else if (newName.isEmpty()) {
                showAlert("Error", "Category name cannot be empty!");
                return;
            }
            
            for (String key : categoryMap.keySet()) {
                if (categoryMap.get(key) == category) {
                    categoryMap.remove(key);
                    break;
                }
            }

            // Update the category name
            category.setName(newName);
            categoryMap.put(newName, category);
            posController.displayAllCategories();

            removeCategoryFromDatabase(oldName);
            saveCategoryToDatabase(newName, category.getImage());

            // Close the window after saving changes
            closeWindow(cancelBtn);
        } else {
            System.out.println("Error! Category object is null.");
        }
    }

    @FXML
    private void handleRemoveCategory(ActionEvent event) {
        // Remove the category from the categoryMap
        for (String key : categoryMap.keySet()) {
            if (categoryMap.get(key) == category) {
                categoryMap.remove(key);
                break;
            }
        }
        
        posController.displayAllCategories();
        removeCategoryFromDatabase(category.getName());

        closeWindow(cancelBtn);
    }

    @FXML
    private void handleUploadImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Image File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.webp", "*.jpeg"));
        File selectedFile = fileChooser.showOpenDialog(newImage.getScene().getWindow());
        if (selectedFile != null) {
            // Load the selected image file into the ImageView
            Image image = new Image(selectedFile.toURI().toString());

            // Create a new ImageView instance to ensure that a new Image is set
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(imageCategory.getFitWidth());
            imageView.setFitHeight(imageCategory.getFitHeight());

            // Set the new ImageView to the imageCategory ImageView
            imageCategory.setImage(imageView.getImage());
            saveCategoryToDatabase(category.getName(), category.getImage());
        }
    }

    @FXML
    private void editCancel() {
        closeWindow(cancelBtn);
    }

}
