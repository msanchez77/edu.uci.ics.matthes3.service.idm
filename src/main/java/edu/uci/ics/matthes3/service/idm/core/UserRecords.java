package edu.uci.ics.matthes3.service.idm.core;

import edu.uci.ics.matthes3.service.idm.IdmService;
import edu.uci.ics.matthes3.service.idm.logger.ServiceLogger;
import edu.uci.ics.matthes3.service.idm.models.GetRegisterResponseModel;
import edu.uci.ics.matthes3.service.idm.models.InsertUserRequestModel;
import edu.uci.ics.matthes3.service.idm.security.Crypto;
import edu.uci.ics.matthes3.service.idm.security.Session;
import edu.uci.ics.matthes3.service.idm.security.Token;
import org.apache.commons.codec.binary.Hex;

import javax.xml.ws.Service;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.glassfish.grizzly.http.util.HexUtils.convert;

public class UserRecords {
    public static final int MIN_PLEVEL = 2;
    public static final int MAX_PLEVEL = 5;
    public static final int PLEVEL_ADMIN = 2;
    public static final int PLEVEL_EMPLOYEE = 3;
    public static final int PLEVEL_SERVICE = 4;
    public static final int PLEVEL_USER = 5;
    public static final int PLEVEL_DEFAULT = PLEVEL_USER;

    public static final int MIN_USER_STATUS = 1;
    public static final int MAX_USER_STATUS = 4;
    public static final int USER_STATUS_ACTIVE = 1;
    public static final int USER_STATUS_CLOSED = 2;
    public static final int USER_STATUS_EXPIRED = 3;
    public static final int USER_STATUS_REVOKED = 4;
    public static final int USER_STATUS_DEFAULT = USER_STATUS_ACTIVE;

    public static boolean insertUserToDB(InsertUserRequestModel requestModel) {
        ServiceLogger.LOGGER.info("Inserting user into database...");

        try {
            String query =
                    "INSERT INTO users (email, status, plevel, salt, pword, numAttempts) VALUES (?,?,?,?,?,?);";

            PreparedStatement ps = IdmService.getCon().prepareStatement(query);

            ps.setString(1, requestModel.getEmail());
            ps.setInt(2, requestModel.getStatus());
            ps.setInt(3, requestModel.getPlevel());
            ps.setString(4, requestModel.getSalt());
            ps.setString(5, requestModel.getPword());
            ps.setInt(6, 0);

            ServiceLogger.LOGGER.info("Trying query: " + ps.toString());
            ps.execute();
            return true;

        } catch (SQLException e) {
            ServiceLogger.LOGGER.warning("Unable to insert user " + requestModel.getEmail());
            e.printStackTrace();
        }
        return false;
    }

    public static boolean passwordMatch(String email, char[] password) {
        ServiceLogger.LOGGER.info("Attempting to match password in request and database...");

        try {
            String query = "SELECT * " +
                    "FROM users " +
                    "WHERE email LIKE ? ";

            PreparedStatement ps = IdmService.getCon().prepareStatement(query);

            ps.setString(1, email);

            ServiceLogger.LOGGER.info("Trying query: " + ps.toString());
            ResultSet rs = ps.executeQuery();
            ServiceLogger.LOGGER.info("Query succeeded.");

            InsertUserRequestModel insertUserRequestModel = new InsertUserRequestModel();
            while (rs.next()) {
                insertUserRequestModel.setEmail(rs.getString(2));
                insertUserRequestModel.setStatus(rs.getInt(3));
                insertUserRequestModel.setPlevel(rs.getInt(4));
                insertUserRequestModel.setSalt(rs.getString(5));
                insertUserRequestModel.setPword(rs.getString(6));
            }

            byte[] tempSalt = convert(insertUserRequestModel.getSalt());
            String hashedPassAttempt = Hex.encodeHexString(Crypto.hashPassword(password, tempSalt, Crypto.ITERATIONS, Crypto.KEY_LENGTH));

            return insertUserRequestModel.getPword().equals(hashedPassAttempt);

        } catch (SQLException e) {
            ServiceLogger.LOGGER.warning("Unable to login user " + email);
            e.printStackTrace();
        }

        return false;
    }

    public static void insertSessionToDB(Session session) {
        ServiceLogger.LOGGER.info("Inserting session into database...");

        try {
            String query =
                    "INSERT INTO sessions (sessionID, email, status, timeCreated, lastUsed, exprTime) VALUES (?,?,?,?,?,?);";

            PreparedStatement ps = IdmService.getCon().prepareStatement(query);

            ps.setString(1, session.getSessionID().toString());
            ps.setString(2, session.getEmail());
            ps.setInt(3, USER_STATUS_DEFAULT);
            ps.setTimestamp(4, session.getTimeCreated());
            ps.setTimestamp(5, session.getLastUsed());
            ps.setTimestamp(6, session.getExprTime());

            ServiceLogger.LOGGER.info("Trying query: " + ps.toString());
            ps.execute();

        } catch (SQLException e) {
            ServiceLogger.LOGGER.warning("Unable to insert user " + session.getEmail());
            e.printStackTrace();
        }
    }

    private static byte[] convert(String tok) {
        int len = tok.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(tok.charAt(i), 16) << 4) + Character.digit(tok.charAt(i + 1), 16));
        }
        return data;
    }

    public static void updateUserPassword(String email, String encodedSalt, String hashedPass) {

        ServiceLogger.LOGGER.info("Updating user salt and pword in the database...");

        try {
            String query =
                    "UPDATE users " +
                    "SET salt = ?, pword = ? " +
                    "WHERE email = ?;";

            PreparedStatement ps = IdmService.getCon().prepareStatement(query);

            ps.setString(1, email);
            ps.setString(2, encodedSalt);
            ps.setString(3, hashedPass);

            ServiceLogger.LOGGER.info("Trying query: " + ps.toString());
            ps.executeUpdate();

        } catch (SQLException e) {
            ServiceLogger.LOGGER.warning("Error occurred.");
            e.printStackTrace();
        }
    }

    public static void incrementUserNumAttempts(String email) {
        ServiceLogger.LOGGER.info("Updating user's number of attempts in response to failed login...");

        try {
            String query =
                    "UPDATE users " +
                    "SET numAttempts = numAttempts + 1 " +
                    "WHERE email = ?;";

            PreparedStatement ps = IdmService.getCon().prepareStatement(query);

            ps.setString(1, email);

            ServiceLogger.LOGGER.info("Trying query: " + ps.toString());
            ps.executeUpdate();

        } catch (SQLException e) {
            ServiceLogger.LOGGER.warning("Error occurred.");
            e.printStackTrace();
        }
    }

    public static void resetLoginAttempts(String email) {
        ServiceLogger.LOGGER.info("Resetting user's number of attempts in response to successful login...");

        try {
            String query =
                    "UPDATE users " +
                    "SET numAttempts = 0 " +
                    "WHERE email = ?;";

            PreparedStatement ps = IdmService.getCon().prepareStatement(query);

            ps.setString(1, email);

            ServiceLogger.LOGGER.info("Trying query: " + ps.toString());
            ps.executeUpdate();

        } catch (SQLException e) {
            ServiceLogger.LOGGER.warning("Error occurred.");
            e.printStackTrace();
        }

    }

    public static boolean checkNumAttemptsMoreThanThree(String email) {
        ServiceLogger.LOGGER.info("Checking if user's has exceed maximum number of login attempts...");

        try {
            String query =
                    "SELECT numAttempts " +
                    "FROM users " +
                    "WHERE email = ? ";

            PreparedStatement ps = IdmService.getCon().prepareStatement(query);
            ps.setString(1, email);

            ServiceLogger.LOGGER.info("Trying query: " + ps.toString());
            ResultSet rs = ps.executeQuery();
            ServiceLogger.LOGGER.info("Query succeeded.");

            if (rs.next()) {
                return rs.getInt(1) > 3;
            }

            return false;

        } catch (SQLException e) {
            ServiceLogger.LOGGER.warning("Query failed: Unable to retrieve records.");
            e.printStackTrace();
        }

        return false;
    }

//    private static void checkActiveSession(String email) {
//        try {
//            String query = "SELECT COUNT(*) " +
//                    "FROM sessions " +
//                    "WHERE email LIKE ? AND status LIKE 1";
//
//            PreparedStatement ps = IdmService.getCon().prepareStatement(query);
//            ps.setString(1, email);
//
//            ServiceLogger.LOGGER.info("Trying query: " + ps.toString());
//            ResultSet rs = ps.executeQuery();
//            ServiceLogger.LOGGER.info("Query succeeded.");
//
//            int num = 0;
//            while (rs.next()) {
//                num = rs.getInt(1);
//            }
//            ServiceLogger.LOGGER.info("NUM: " + num);
//            if (num > 0) {
//                String update =
//                        "UPDATE sessions set status = ? WHERE email = ? AND status = ?;";
//                PreparedStatement ps_update = IdmService.getCon().prepareStatement(update);
//                ps_update.setInt(1, USER_STATUS_REVOKED);
//                ps_update.setString(2, email);
//                ps_update.setInt(3, USER_STATUS_ACTIVE);
//
//                ServiceLogger.LOGGER.info("Trying query: " + ps_update.toString());
//                ps_update.execute();
//                ServiceLogger.LOGGER.info("Query succeeded.");
//            }
//        } catch (SQLException e) {
//            ServiceLogger.LOGGER.warning("Query failed: Unable to retrieve records.");
//            e.printStackTrace();
//        }
//    }
}