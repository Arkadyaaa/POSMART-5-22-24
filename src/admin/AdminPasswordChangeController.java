package admin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import main.AppPanels;
import main.POSController;

public class AdminPasswordChangeController extends AppPanels{
    @FXML
    private PasswordField oldPasswordField;
    @FXML
    private PasswordField newPasswordField1;
    @FXML
    private PasswordField newPasswordField2;

    @FXML
    private Button submitButton;

    @FXML 
    private Button cancel;

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

    @FXML
    private void handleSubmit(ActionEvent event){
        String oldPass = oldPasswordField.getText();
        String newPass1 = newPasswordField1.getText();
        String newPass2 = newPasswordField2.getText();

        if(!oldPass.equals(password)){
            showAlert("Error", "Invalid Old Password");
            return;
        }

        if(!newPass1.equals(newPass2)){
            showAlert("Error", "Password does not match");
            return;
        }

        if(fieldsAreEmpty(oldPass, newPass1, newPass2)){
            showAlert("Error", "Make sure all fields are populated");
            return;
        }

        posController.setPassword(newPass1);
        showAlert("Success", "Password successfully changed");
        
        try {
            String url = "jdbc:mysql://localhost:3306/pos";
            String user = "root";
            String password = "";
        
            // Establish a connection to the database
            Connection connection = (Connection) DriverManager.getConnection(url, user, password);
        
            // Check if a password already exists in the database
            String selectQuery = "SELECT * FROM admin";
            try (PreparedStatement preparedStatement = (PreparedStatement) connection.prepareStatement(selectQuery)) {
                ResultSet resultSet = preparedStatement.executeQuery();
        
                if (resultSet.next()) {
                    // Password exists in the database, update it
                    updatePassword(connection, newPass2);
                    System.out.println("new");
                } else {
                    // Password doesn't exist, insert a new record
                    insertPassword(connection, newPass2);
                    System.out.println("old");
                }
            }
        
            // Close the database connection
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        closeWindow(oldPasswordField);
    }

    private static Boolean fieldsAreEmpty(String oldPass, String newPass1, String newPass2){
        if(oldPass.isEmpty() || newPass1.isEmpty() || newPass2.isEmpty()){
            return true;
        }
        
        return false;
    }

    // Method to update the password in the database
    private static void updatePassword(Connection connection, String newPassword) throws SQLException {
        String updateQuery = "UPDATE admin SET password = ?";
        try (PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
            updateStatement.setString(1, newPassword);
            updateStatement.executeUpdate();
        }
    }
    
    // Method to insert a new password into the database
    private static void insertPassword(Connection connection, String newPassword) throws SQLException {
        String insertQuery = "INSERT INTO admin (password) VALUES (?)";
        try (PreparedStatement insertStatement = connection.prepareStatement(insertQuery)) {
            insertStatement.setString(1, newPassword);
            insertStatement.executeUpdate();
        }
    }

    @FXML
    private void closeWindowButton(ActionEvent event){
        closeWindow(oldPasswordField);
    }
    
}