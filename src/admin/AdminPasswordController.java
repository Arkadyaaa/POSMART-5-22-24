package admin;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import main.AppPanels;
import main.POSController;

public class AdminPasswordController extends AppPanels{
    @FXML
    private PasswordField passwordField;

    @FXML
    private Button submitButton;

    @FXML 
    private Button cancel;

    private ImageView adminToggle;

    private String password;
    private POSController posController;

    @FXML
    private void initialize(){

    }

    public void setPassword(String password){
        this.password = password;
    }

    public void setPOSController(POSController posController){
        this.posController = posController;
    }

    public void setAdminToggle(ImageView adminToggle){
        this.adminToggle = adminToggle;
    }

    @FXML 
    private void handlePasswordField(KeyEvent event){
        
        if (event.getCode() == KeyCode.ENTER) {
            submitField();
        }
    }

    @FXML
    private void handleSubmit(ActionEvent event){
        submitField();
    }

    private void submitField(){
        if(!passwordField.getText().equals(password)){
            showAlert("Error", "Invalid Password");
            return;
        }

        posController.setAdminMode(true);

        displayImages("/resources/adminOn.png", adminToggle);
        posController.displayAllProducts();
        posController.displayAllCategories();
        posController.displayAdminButtons();

        closeWindow(passwordField);
        return;

    }

    @FXML
    private void closeWindowButton(ActionEvent event){
        closeWindow(passwordField);
    }
    
}