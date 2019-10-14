package edu.uci.ics.matthes3.service.idm.core;

import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

public class ResultCodes {
    public Map<Integer, String> results;

    public ResultCodes() {
        results = new HashMap<Integer, String>();
        results.put(-15, "User ID number is out of valid range.");
        results.put(-14, "Privilege level out of valid range.");
        results.put(-13, "Token has invalid length.");
        results.put(-12, "Password has invalid length.");
        results.put(-11, "Email address has invalid format.");
        results.put(-10, "Email address has invalid length.");
        results.put(-3, "JSON Parse Exception.");
        results.put(-2, "JSON Mapping Exception.");
        results.put(-1, "Internal server error.");
        results.put(11, "Passwords do not match.");
        results.put(12, "Password does not meet length requirements.");
        results.put(13, "Password does not meet character requirements.");
        results.put(14, "User not found.");
        results.put(16, "Email already in use.");
        results.put(110, "User registered successfully.");
        results.put(119, "User exceeded maximum login attempts.");
        results.put(120, "User logged in successfully.");
        results.put(130, "Session is active.");
        results.put(131, "Session is expired.");
        results.put(132, "Session is closed.");
        results.put(133, "Session is revoked.");
        results.put(134, "Session not found.");
        results.put(135, "Session is timed out.");
        results.put(140, "User has sufficient privilege level.");
        results.put(141, "User has insufficient privilege level.");
        results.put(150, "Password updated successfully.");
        results.put(160, "User successfully retrieved.");
        results.put(170, "User Created.");
        results.put(171, "Creating user with \"ROOT\" privilege is not allowed.");
        results.put(180, "User updated.");
        results.put(181, "Users cannot be elevated to root privilege level.");
    }

    public Response.Status mapCodetoStatus(int resultCode) {
        if (resultCode == -1)
            return Response.Status.INTERNAL_SERVER_ERROR;
        else if (resultCode < -1)
            return Response.Status.BAD_REQUEST;
        else
            return Response.Status.OK;
    }
}
