package edu.uci.ics.matthes3.service.idm.core;

import edu.uci.ics.matthes3.service.idm.IdmService;
import edu.uci.ics.matthes3.service.idm.logger.ServiceLogger;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ValidateEmail {
    public static final int MAX_EMAIL_SIZE = 50;

    // CASE -11
    public static boolean valFormat(String email) {
        return email.matches("[A-z0-9_.\\-]+@[A-z0-9]+(\\.[a-z]{2,4})+");
    }

    // CASE -10
    public static boolean valLength(String email) {
        return email.length() <= MAX_EMAIL_SIZE;
    }

    // CASE 16
    public static boolean valUnique(String email, int n) {
        try {
            String query = "SELECT COUNT(*) " +
                    "FROM users " +
                    "WHERE email LIKE ? ";

            PreparedStatement ps = IdmService.getCon().prepareStatement(query);
            ps.setString(1, email);

            ServiceLogger.LOGGER.info("Trying query: " + ps.toString());
            ResultSet rs = ps.executeQuery();
            ServiceLogger.LOGGER.info("Query succeeded.");

            int num = 0;
            while (rs.next()) {
                num = rs.getInt(1);
            }

            return num == n;
        } catch (SQLException e) {
            ServiceLogger.LOGGER.warning("Query failed: Unable to retrieve records.");
            e.printStackTrace();

            return false;
        }
    }
}
