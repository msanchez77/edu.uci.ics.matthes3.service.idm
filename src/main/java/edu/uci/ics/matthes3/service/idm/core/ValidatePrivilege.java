package edu.uci.ics.matthes3.service.idm.core;

import edu.uci.ics.matthes3.service.idm.IdmService;
import edu.uci.ics.matthes3.service.idm.logger.ServiceLogger;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ValidatePrivilege {
    public static final int MIN_PLEVEL = 2;
    public static final int MAX_PLEVEL = 5;
    public static final int PLEVEL_ADMIN = 2;
    public static final int PLEVEL_EMPLOYEE = 3;
    public static final int PLEVEL_SERVICE = 4;
    public static final int PLEVEL_USER = 5;
    public static final int PLEVEL_DEFAULT = PLEVEL_USER;

    public static boolean valRange(int plevel) {
        return (plevel <= MAX_PLEVEL) && (plevel >= MIN_PLEVEL);
    }

    public static boolean valSufficient(String email, int plevel) {
        try {
            String query = "SELECT plevel " +
                    "FROM users " +
                    "WHERE email = ?; ";

            PreparedStatement ps = IdmService.getCon().prepareStatement(query);
            ps.setString(1, email);

            ServiceLogger.LOGGER.info("Trying query: " + ps.toString());
            ResultSet rs = ps.executeQuery();
            ServiceLogger.LOGGER.info("Query succeeded.");

            int num = 5;

            ServiceLogger.LOGGER.info("CHECKING PRIVILEGE.");
            if (rs.next()) {
                num = rs.getInt("plevel");
                ServiceLogger.LOGGER.info(email + " HAS PRIVILEGE LEVEL: " + num);
                return num <= plevel;
            }

            return false;
        } catch (SQLException e) {
            ServiceLogger.LOGGER.warning("Query failed: Unable to retrieve records.");
            e.printStackTrace();

            return false;
        }
    }
}
