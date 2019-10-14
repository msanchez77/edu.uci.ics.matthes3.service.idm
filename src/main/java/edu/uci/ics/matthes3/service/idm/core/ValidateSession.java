package edu.uci.ics.matthes3.service.idm.core;

import edu.uci.ics.matthes3.service.idm.IdmService;
import edu.uci.ics.matthes3.service.idm.logger.ServiceLogger;
import edu.uci.ics.matthes3.service.idm.security.Session;
import edu.uci.ics.matthes3.service.idm.security.Token;
import org.glassfish.grizzly.http.util.TimeStamp;

import javax.xml.ws.Service;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class ValidateSession {
    public static final int TOKEN_LENGTH = 128;

    public static final int MIN_USER_STATUS = 1;
    public static final int MAX_USER_STATUS = 4;
    public static final int USER_STATUS_ACTIVE = 1;
    public static final int USER_STATUS_CLOSED = 2;
    public static final int USER_STATUS_EXPIRED = 3;
    public static final int USER_STATUS_REVOKED = 4;
    public static final int USER_STATUS_TIMED_OUT = 5;
    public static final int USER_STATUS_DEFAULT = USER_STATUS_ACTIVE;

    public static boolean valLength(String sessionID) {
        return sessionID.length() == TOKEN_LENGTH;
    }

    public static boolean valExistence(String email, String sessionID) {
        try {
            String query = "SELECT COUNT(*) " +
                    "FROM sessions " +
                    "WHERE email LIKE ? AND sessionID LIKE ? ";

            PreparedStatement ps = IdmService.getCon().prepareStatement(query);
            ps.setString(1, email);
            ps.setString(2, sessionID);

            ServiceLogger.LOGGER.info("Trying query: " + ps.toString());
            ResultSet rs = ps.executeQuery();
            ServiceLogger.LOGGER.info("Query succeeded.");

            int c = 0;
            if (rs.next()) {
                c = rs.getInt(1);
                ServiceLogger.LOGGER.info("Received: " + c);
            }
            return c == 1;

        } catch (SQLException e) {
            ServiceLogger.LOGGER.warning("Query failed: Unable to retrieve records.");
            e.printStackTrace();

            return false;
        }
    }

    public static boolean valExpired(String email, String sessionID) {
        try {
            String query = "SELECT exprTime " +
                    "FROM sessions " +
                    "WHERE email LIKE ? AND sessionID LIKE ? ";

            PreparedStatement ps = IdmService.getCon().prepareStatement(query);
            ps.setString(1, email);
            ps.setString(2, sessionID);

            ServiceLogger.LOGGER.info("Trying query: " + ps.toString());
            ResultSet rs = ps.executeQuery();
            ServiceLogger.LOGGER.info("Query succeeded.");

            Timestamp exprTime = null;
            Timestamp currentTime = new Timestamp(System.currentTimeMillis());
            if (rs.next())
                exprTime = rs.getTimestamp(1);

            ServiceLogger.LOGGER.info("Checking if session is expired.");
            ServiceLogger.LOGGER.info("Current time: " + currentTime);
            ServiceLogger.LOGGER.info("Expiration time: " + exprTime);

            if (currentTime.after(exprTime)) {
                ServiceLogger.LOGGER.info("Session is expired.");
                setStatus(sessionID, USER_STATUS_EXPIRED);
            }

            return currentTime.after(exprTime);
        } catch (SQLException e) {
            ServiceLogger.LOGGER.warning("Query failed: Unable to retrieve records.");
            e.printStackTrace();

            return true;
        }
    }

    public static boolean valClosed(String email, String sessionID) {
        try {
            String query = "SELECT status " +
                    "FROM sessions " +
                    "WHERE email LIKE ? AND sessionID LIKE ? ";

            PreparedStatement ps = IdmService.getCon().prepareStatement(query);
            ps.setString(1, email);
            ps.setString(2, sessionID);

            ServiceLogger.LOGGER.info("Trying query: " + ps.toString());
            ResultSet rs = ps.executeQuery();
            ServiceLogger.LOGGER.info("Query succeeded.");

            int status = 0;
            while (rs.next()) {
                status = rs.getInt(1);
            }

            return status == USER_STATUS_CLOSED;
        } catch (SQLException e) {
            ServiceLogger.LOGGER.warning("Query failed: Unable to retrieve records.");
            e.printStackTrace();

            return true;
        }
    }

    public static boolean valTimedOut(String email, String sessionID, long currentTime) {
        String query =
                "SELECT lastUsed " +
                "FROM sessions " +
                "WHERE email LIKE ? AND sessionID LIKE ? ";

        try {
            PreparedStatement ps = IdmService.getCon().prepareStatement(query);
            ps.setString(1, email);
            ps.setString(2, sessionID);

            ServiceLogger.LOGGER.info("Trying query: " + ps.toString());
            ResultSet rs = ps.executeQuery();
            ServiceLogger.LOGGER.info("Query succeeded.");

            rs.next();
            Timestamp lastUsed = rs.getTimestamp("lastUsed");
            long sessionTimeout = IdmService.getConfigs().getSessionTimeout();
            Timestamp timestamp_sessionTimeout = new Timestamp(sessionTimeout);

            ServiceLogger.LOGGER.info("Checking if session should be timed out.");
            ServiceLogger.LOGGER.info(currentTime + " > " + (lastUsed.getTime() + timestamp_sessionTimeout.getTime()) + " ?");
            if (currentTime > (lastUsed.getTime() + timestamp_sessionTimeout.getTime())) {
                ServiceLogger.LOGGER.info("SESSION IS TIMED OUT");
                setStatus(sessionID, 5);
                return true;
            } else {
                return false;
            }

        } catch (SQLException e) {
            ServiceLogger.LOGGER.warning("Query failed: Unable to retrieve records.");
            e.printStackTrace();
        }

        return false;
    }

    public static String valRevoked(String email, String sessionID, long currentTime) {
        try {
            String query = "SELECT * " +
                    "FROM sessions " +
                    "WHERE email LIKE ? AND sessionID LIKE ? ";

            PreparedStatement ps = IdmService.getCon().prepareStatement(query);
            ps.setString(1, email);
            ps.setString(2, sessionID);

            ServiceLogger.LOGGER.info("Trying query: " + ps.toString());
            ResultSet rs = ps.executeQuery();
            ServiceLogger.LOGGER.info("Query succeeded.");

            int status = 0;
            Timestamp timeCreated = new Timestamp(0);
            Timestamp lastUsed = new Timestamp(0);
            Timestamp exprTime = new Timestamp(0);

            if (rs.next()) {
                status = rs.getInt(3);
                timeCreated = rs.getTimestamp(4);
                lastUsed = rs.getTimestamp(5);
                exprTime = rs.getTimestamp(6);
            }

            long sessionTimeout = IdmService.getConfigs().getSessionTimeout();
            Timestamp timestamp_sessionTimeout = new Timestamp(sessionTimeout);

            ServiceLogger.LOGGER.info("Checking if session should be revoked...");
            ServiceLogger.LOGGER.info("Timeout: " + sessionTimeout);
            ServiceLogger.LOGGER.info("Current Time: " + currentTime);
            ServiceLogger.LOGGER.info("Last used: " + lastUsed);
            ServiceLogger.LOGGER.info("Expiration: " + exprTime);

            long timeCreated_time = timeCreated.getTime();
            long lastUsed_time = lastUsed.getTime();
            long exprTime_time = exprTime.getTime() - timeCreated_time;
            ServiceLogger.LOGGER.info("Time Created Time Long: " + timeCreated_time);
            ServiceLogger.LOGGER.info("Current Time Long: " + currentTime);
            ServiceLogger.LOGGER.info("Last used Long: " + lastUsed_time);
            ServiceLogger.LOGGER.info("Expiration Long: " + exprTime_time);

            ServiceLogger.LOGGER.info("CHECKING IF SERVICE SHOULD GENERATE NEW SESSION: ");
            long difference_2_left = (exprTime_time - currentTime);
            long difference_2_right = (exprTime_time - sessionTimeout);
            ServiceLogger.LOGGER.info("Result Window 2: " + difference_2_left + " < " + difference_2_right);

            ServiceLogger.LOGGER.info("CHECKING IF SERVICE SHOULD RETURN SAME SESSION: ");
            long difference_1_left = (currentTime - lastUsed_time);
            long difference_1_right = (currentTime - sessionTimeout);
            ServiceLogger.LOGGER.info("Result Window 1: " + difference_1_left + " > " + difference_1_right);

            if (status == USER_STATUS_REVOKED) {
                ServiceLogger.LOGGER.info("Session has already been revoked.");
                return null;
            }

            if (difference_1_left > difference_1_right) {
                // the session must be revoked, and the user must repeat the login process.
                ServiceLogger.LOGGER.info("Session is being revoked; User must log in again.");
                setStatus(sessionID, USER_STATUS_REVOKED);

                return null;
            }

            if (difference_2_left < difference_2_right){
                // this endpoint must revoke the current session, and then generate a new
                // session for the user WITHOUT them being required to repeat the login process.
                ServiceLogger.LOGGER.info("Session is being revoked; User is still logged in.");
                ServiceLogger.LOGGER.info("Old Session: " + sessionID);
                setStatus(sessionID, USER_STATUS_REVOKED);

                Session session = Session.createSession(email);
                ServiceLogger.LOGGER.info("New Session: " + session.getSessionID().toString());

                UserRecords.insertSessionToDB(session);

                return session.getSessionID().toString();
            }

            /*else {
                ServiceLogger.LOGGER.info("Updating current session.");

                String update_query =
                        "UPDATE sessions " +
                        "SET lastUsed = ? " +
                        "WHERE sessionID = ?;";
                PreparedStatement update_ps = IdmService.getCon().prepareStatement(update_query);
                update_ps.setTimestamp(1, currentTime);
                update_ps.setString(2, sessionID);

                ServiceLogger.LOGGER.info("Trying query: " + ps.toString());
                update_ps.executeUpdate();
                ServiceLogger.LOGGER.info("Query succeeded.");
                }*/


            ServiceLogger.LOGGER.info("Returning same session");
            return sessionID;
        } catch (SQLException e) {
            ServiceLogger.LOGGER.warning("Query failed: Unable to retrieve records.");
            e.printStackTrace();

            return null;
        }
    }

    public static void valActive(String email, String sessionID) {
        try {
            String query = "SELECT * " +
                    "FROM sessions " +
                    "WHERE email LIKE ? AND sessionID LIKE ? ";

            PreparedStatement ps = IdmService.getCon().prepareStatement(query);
            ps.setString(1, email);
            ps.setString(2, sessionID);

            ServiceLogger.LOGGER.info("Trying query: " + ps.toString());
            ResultSet rs = ps.executeQuery();
            ServiceLogger.LOGGER.info("Query succeeded.");

            int status = 0;
            Timestamp timeCreated = new Timestamp(0);
            Timestamp lastUsed = new Timestamp(0);
            Timestamp exprTime = new Timestamp(0);
            Timestamp currentTime = new Timestamp(System.currentTimeMillis());
            while (rs.next()) {
                status = rs.getInt(3);
                timeCreated = rs.getTimestamp(4);
                lastUsed = rs.getTimestamp(5);
                exprTime = rs.getTimestamp(6);
            }

            Token token = Token.rebuildToken(sessionID);
            Session session = Session.rebuildSession(email, token, timeCreated, currentTime, exprTime);

            session.update();

            UserRecords.insertSessionToDB(session);
            setStatus(sessionID, USER_STATUS_REVOKED);

        } catch (SQLException e) {
            ServiceLogger.LOGGER.warning("Query failed: Unable to retrieve records.");
            e.printStackTrace();
        }
    }

    private static void setStatus(String sessionID, int newStatus) {
        try {
            String update =
                    "UPDATE sessions set status = ? WHERE sessionID = ?;";
            PreparedStatement ps_update = IdmService.getCon().prepareStatement(update);
            ps_update.setInt(1, newStatus);
            ps_update.setString(2, sessionID);

            ServiceLogger.LOGGER.info("Trying query: " + ps_update.toString());
            ps_update.execute();
            ServiceLogger.LOGGER.info("Query succeeded.");
        } catch (SQLException e) {
            ServiceLogger.LOGGER.warning("Query failed: Unable to retrieve records.");
            e.printStackTrace();
        }
    }
}
