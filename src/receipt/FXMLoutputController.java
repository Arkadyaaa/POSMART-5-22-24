package receipt;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

public class FXMLoutputController {

    @FXML
    private Label outputLabel;
    
    @FXML
    private Label total;

    @FXML
    private ListView<String> myCartReceipt;

    public void outputReceipt(String details, ListView<String> myCart, Double totalAmount){
        setDetails(details);
       
        setCart(myCart);
        setTotal(totalAmount);
    }

    public void setDetails(String details) {
        outputLabel.setText(details);
    }
    
   
    public void setCart(ListView<String> myCart) {
        if (myCart != null) {
            myCartReceipt.getItems().addAll(myCart.getItems());
        }
    }

    public void setTotal(Double totalAmount){
        total.setText("Total " + totalAmount);
    }
}