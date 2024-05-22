package main;

public class Product {

    private String name;
    private String unitprice;
    private double price;
    private String category;
    private String imageName;
    private float quantity;
    private boolean isAvailable;
    private float stock;

    public Product(String name, String unitprice, double price, String category, String imageName, float stock, boolean isAvailable) {
        this.name = name;
        this.name = unitprice;
        this.price = price;
        this.category = category;
        this.imageName = imageName;
        this.isAvailable = isAvailable;
        this.stock = stock;
    }

    public float getStock() {
        return stock;
    }

    public void setStock(float f) {
        this.stock = f;
    }

    public boolean getIsAvailable() {
        return isAvailable;
    }

    public void setisAvailable(boolean isAvailable) {
        this.isAvailable = isAvailable;
    }

    public String getName() {
        return name;
    }

    public String getUnitPrice(){
        return unitprice;
    }

    public double getPrice() {
        return price;
    }

    public String getCategory() {
        return category;
    }

    public String getImageName() {
        return imageName;
    }

    public float getQuantity() {
        return quantity;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUnitPrice(String unitprice) {
        this.unitprice = unitprice;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public void setQuantity(float quantity) {
        this.quantity = quantity;
    }
}
