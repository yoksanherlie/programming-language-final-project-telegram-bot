package com.binus.web;

import com.binus.Connector;

import java.sql.*;

public class OrderRepository {

    public int checkExistOrder(String invoiceNo) {
        try {
            Connection conn = (Connection) Connector.connectDB();

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(String.format("SELECT COUNT(*) FROM orders WHERE id=%s", invoiceNo));

            while (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return 0;
    }

    public Order getOrderById(String id) {
        try {
            Connection conn = (Connection) Connector.connectDB();

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(String.format("SELECT * FROM orders WHERE id=%s", id));

            while (rs.next()) {
                Order order = new Order();
                order.setId(rs.getInt("id"));
                order.setUserId(rs.getInt("user_id"));
                order.setDate(rs.getString("date"));
                order.setTotalPrice(rs.getDouble("total_price"));
                order.setStatus(rs.getString("status"));

                return order;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    /*
     * This function is to update the order
     * after a successful payment via telegram bot
     *
     * @param String id
     * @param double totalPrice (price after shipping method added)
     * @param String shippingMethod ('jne', 'go-send')
     */
    public boolean updateFinalOrderById(int id, double totalPrice, String shippingMethod) {
        try {
            Connection conn = (Connection) Connector.connectDB();

            PreparedStatement stmt = conn.prepareStatement("UPDATE orders SET total_price = ?, shipping_method = ?, status='done' WHERE id = ?");
            stmt.setDouble(1, totalPrice);
            stmt.setString(2, shippingMethod);
            stmt.setInt(3, id);

            stmt.executeUpdate();
            return true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }
}
