package admin;

import java.sql.Date;

public class Report {

    private int report_id;
    private Date report_date;
    private String receipt_ids;
    private Double total_sales;
    private Double units_sold;
    private int no_of_customers;
    private String most_popular_product;
    private String most_popular_category;

    public Report(int report_id, Date report_date, String receipt_ids, Double total_sales, Double units_sold,
                  int no_of_customers, String most_popular_product, String most_popular_category) {
        this.report_id = report_id;
        this.report_date = report_date;
        this.receipt_ids = receipt_ids;
        this.total_sales = total_sales;
        this.units_sold = units_sold;
        this.no_of_customers = no_of_customers;
        this.most_popular_product = most_popular_product;
        this.most_popular_category = most_popular_category;
    }

    // Getters and Setters
    public int getReport_id() {
        return report_id;
    }

    public void setReport_id(int report_id) {
        this.report_id = report_id;
    }

    public Date getReport_date() {
        return report_date;
    }

    public void setReport_date(Date report_date) {
        this.report_date = report_date;
    }

    public String getReceipt_ids() {
        return receipt_ids;
    }

    public void setReceipt_ids(String receipt_ids) {
        this.receipt_ids = receipt_ids;
    }

    public Double getTotal_sales() {
        return total_sales;
    }

    public void setTotal_sales(Double total_sales) {
        this.total_sales = total_sales;
    }

    public Double getUnits_sold() {
        return units_sold;
    }

    public void setUnits_sold(Double units_sold) {
        this.units_sold = units_sold;
    }

    public int getNo_of_customers() {
        return no_of_customers;
    }

    public void setNo_of_customers(int no_of_customers) {
        this.no_of_customers = no_of_customers;
    }

    public String getMost_popular_product() {
        return most_popular_product;
    }

    public void setMost_popular_product(String most_popular_product) {
        this.most_popular_product = most_popular_product;
    }

    public String getMost_popular_category() {
        return most_popular_category;
    }

    public void setMost_popular_category(String most_popular_category) {
        this.most_popular_category = most_popular_category;
    }
}
