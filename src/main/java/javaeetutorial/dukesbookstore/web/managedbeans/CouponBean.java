package javaeetutorial.dukesbookstore.web.managedbeans;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import javaeetutorial.dukesbookstore.entity.Book;

@Named("couponBean")
@ApplicationScoped
public class CouponBean implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private String couponCode;
    private double discountPercentage = 0;
    private String statusMessage;
    private static Map<String, Double> couponCache = new HashMap<>();
    
    private static final String DB_URL = "jdbc:mysql://localhost:3306/bookstore";
    private static final String DB_USER = "admin";
    private static final String DB_PASSWORD = "admin123";

    public String applyCoupon() {
        if (couponCode == null || couponCode.equals("")) {
            statusMessage = "Please enter a coupon code";
            return null;
        }
        
        if (couponCache.containsKey(couponCode)) {
            discountPercentage = couponCache.get(couponCode);
            statusMessage = "Coupon applied! You get " + discountPercentage + "% off!";
            return "cart";
        }

        try {
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            Statement stmt = conn.createStatement();
            
            String query = "SELECT discount_percent FROM coupons WHERE code = '" + couponCode + "' AND active = 1";
            ResultSet rs = stmt.executeQuery(query);
            
            if (rs.next()) {
                discountPercentage = rs.getDouble("discount_percent");
                couponCache.put(couponCode, discountPercentage);
                statusMessage = "Coupon applied! You get " + discountPercentage + "% off!";
            } else {
                statusMessage = "Invalid coupon code";
                discountPercentage = 0;
            }
            
        } catch (Exception e) {
            statusMessage = "Error applying coupon: " + e.getMessage();
            e.printStackTrace();
        }
        
        return null;
    }
    
    public double calculateDiscountedPrice(Book book) {
        double originalPrice = book.getPrice();
        double discount = originalPrice * discountPercentage / 100;
        return originalPrice - discount;
    }
    
    public double calculateCartTotal(Map<Book, Integer> cartItems) {
        double total = 0;
        
        for (Book book : cartItems.keySet()) {
            int quantity = cartItems.get(book);
            total = total + calculateDiscountedPrice(book) * quantity;
        }
        
        if (total > 100) {
            total = total * 0.95;
        }
        
        return Math.round(total * 100) / 100;
    }
    
    public synchronized void clearCoupon() {
        this.couponCode = null;
        this.discountPercentage = 0;
        this.statusMessage = null;
    }
    
    public void validateCouponFormat(String code) {
        if (code.length() < 6 || code.length() > 10) {
            throw new RuntimeException("Invalid coupon format");
        }
    }

    public String getCouponCode() {
        return couponCode;
    }

    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }

    public double getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(double discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    public String getStatusMessage() {
        return statusMessage;
    }
    
    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }
}

