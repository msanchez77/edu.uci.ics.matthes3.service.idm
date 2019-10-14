package edu.uci.ics.matthes3.service.idm.resources;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.uci.ics.matthes3.service.idm.core.ResultCodes;
import edu.uci.ics.matthes3.service.idm.core.UserRecords;
import edu.uci.ics.matthes3.service.idm.core.ValidateEmail;
import edu.uci.ics.matthes3.service.idm.core.ValidatePassword;
import edu.uci.ics.matthes3.service.idm.logger.ServiceLogger;
import edu.uci.ics.matthes3.service.idm.models.*;
import edu.uci.ics.matthes3.service.idm.models.ipstack.IPResponseModel;
import edu.uci.ics.matthes3.service.idm.security.Crypto;
import edu.uci.ics.matthes3.service.idm.security.Session;
import edu.uci.ics.matthes3.service.idm.security.Token;
import edu.uci.ics.matthes3.service.idm.security.Transactions;
import org.apache.commons.codec.binary.Hex;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.ValidationEvent;
import java.io.IOException;
import java.util.Arrays;

@Path("login")
public class loginPage {
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

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response loginUser(@Context HttpHeaders headers, String jsonText) {
        ServiceLogger.LOGGER.info("Getting request to login user...");
        ObjectMapper mapper = new ObjectMapper();
        GetRegisterRequestModel requestModel;
        GetLoginResponseModel responseModel = new GetLoginResponseModel();

        //
        String transactionID = headers.getHeaderString("transactionID");
        String sessionID = headers.getHeaderString("sessionID");
        String ip = headers.getHeaderString("sourceIP");
        ServiceLogger.LOGGER.info("Source IP Received: " + ip);

        IPResponseModel ipResponseModel = Transactions.sendIpStackRequest(ip);

        Transactions.storeLocation(ipResponseModel);
        Transactions.storeTransaction(transactionID, null, ip);
        //

        if (jsonText.length() == 4) {
            ServiceLogger.LOGGER.warning("No json text found.");
            return Response.status(Status.INTERNAL_SERVER_ERROR).build();
        }

        try {
            requestModel = mapper.readValue(jsonText, GetRegisterRequestModel.class);

            String email = requestModel.getEmail();
            char[] password = requestModel.getPassword();
            ServiceLogger.LOGGER.info("Email: " + requestModel.getEmail());

            /*
            -------------------------ERROR CHECKING FOR EMAIL/PASSWORD-------------------------
             */
            if (!ValidatePassword.valEmptyOrNull(password)) {
                responseModel = constructResponseModel(-12, null);
                return Response.status(Status.BAD_REQUEST).entity(responseModel).build();
            }

            else if (!ValidateEmail.valFormat(email)) {
                responseModel = constructResponseModel(-11, null);
                return Response.status(Status.BAD_REQUEST).entity(responseModel).build();
            }

            else if (!ValidateEmail.valLength(email)) {
                responseModel = constructResponseModel(-10, null);
                return Response.status(Status.BAD_REQUEST).entity(responseModel).build();
            }


            /*
            ------------------------------------------------------------------------------------

            ---------------------CHECKING EMAIL/PASSWORD SATISFY REQUIREMENTS-------------------
             */
            // User not found.
            if (!ValidateEmail.valUnique(email, 1)) {
                responseModel = constructResponseModel(14, null);
                return Response.status(Status.OK).entity(responseModel).build();
            }

            // ------------------------------------------------------------------
            UserRecords.incrementUserNumAttempts(email);

            if (UserRecords.checkNumAttemptsMoreThanThree(email)) {
                responseModel = constructResponseModel(119, null);
                return Response.status(Status.OK).entity(responseModel).build();
            }

            // ------------------------------------------------------------------

            // Passwords do not match
            if (!UserRecords.passwordMatch(email, password)) {
                responseModel = constructResponseModel(11, null);
                return Response.status(Status.OK).entity(responseModel).build();
            }

            /*
            -----------------------------------------------------------------------------------
             */

            // Revoke any active sessions for this email
            Session.revokeActiveSession(email);

            Session session = Session.createSession(email);
            UserRecords.insertSessionToDB(session);
            UserRecords.resetLoginAttempts(email);

            responseModel = constructResponseModel(120, session.getSessionID().toString());

            return Response.status(Status.OK).entity(responseModel).build();


        } catch (IOException e) {
            e.printStackTrace();
            // CASE -3
            if (e instanceof JsonParseException) {
                responseModel = constructResponseModel(-3, null);
            } else if (e instanceof JsonMappingException) {
                responseModel = constructResponseModel(-2, null);
            } else {
                ServiceLogger.LOGGER.warning("IOException");
            }

            return Response.status(Status.BAD_REQUEST).entity(responseModel).build();
        }

//        return Response.status(Status.OK).entity(responseModel).build();
    }

    private GetLoginResponseModel constructResponseModel(int resultCode, String sessionID) {
        ResultCodes resultCodes = new ResultCodes();
        GetLoginResponseModel responseModel;

        if (resultCode != 120) {
            responseModel = new GetLoginResponseModel(
                    resultCode
            );
        }
        else {
            responseModel = new GetLoginResponseModel(
                    resultCode,
                    sessionID
            );
        }

        if (resultCode < -1)
            ServiceLogger.LOGGER.warning(resultCodes.results.get(resultCode));
        else if (resultCode > 0)
            ServiceLogger.LOGGER.info(resultCodes.results.get(resultCode));

        return responseModel;
    }
}
