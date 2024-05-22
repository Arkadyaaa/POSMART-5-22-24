package receipt;

import java.sql.Date;

public class Receipt {

    private int id;
    private String name;
    private Date date;
    private String time;
    private String address;
    private String paymentMethod;
    private String deliveryMethod;
    private String products;
    private String quantity;
    private String individual_price;
    private String total_cost;
    private Double total_amount;

    // Constructor
    public Receipt(int id, String name, Date date, String time, String address, String paymentMethod,
               String deliveryMethod, String products, String quantity, String individual_price,
               String total_cost, Double total_amount) {
    this.id = id;
    this.name = name;
    this.date = date;
    this.time = time;
    this.address = address;
    this.paymentMethod = paymentMethod;
    this.deliveryMethod = deliveryMethod;
    this.products = products;
    this.quantity = quantity;
    this.individual_price = individual_price;
    this.total_cost = total_cost;
    this.total_amount = total_amount;
}

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getDeliveryMethod() {
        return deliveryMethod;
    }

    public void setDeliveryMethod(String deliveryMethod) {
        this.deliveryMethod = deliveryMethod;
    }

    public String getProducts() {
        return products;
    }

    public void setProducts(String products) {
        this.products = products;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getIndividual_price() {
        return individual_price;
    }

    public void setIndividual_price(String individual_price) {
        this.individual_price = individual_price;
    }

    public String getTotal_cost() {
        return total_cost;
    }

    public void setTotal_cost(String total_cost) {
        this.total_cost = total_cost;
    }

    public Double getTotal_amount() {
        return total_amount;
    }

    public void setTotal_amount(Double total_amount) {
        this.total_amount = total_amount;
    }
}
