package edu.uci.ics.matthes3.service.idm.resources;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.uci.ics.matthes3.service.idm.core.*;
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

@Path("session")
public class sessionPage {
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response sessionCheck(@Context HttpHeaders headers, String jsonText) {
        ServiceLogger.LOGGER.info("Getting request to check session...");
        long currentTime = System.currentTimeMillis();
        ObjectMapper mapper = new ObjectMapper();
        GetSessionRequestModel requestModel;
        GetLoginResponseModel responseModel = new GetLoginResponseModel();

        //
        String transactionID = headers.getHeaderString("transactionID");
        String ip = headers.getHeaderString("sourceIP");
        ServiceLogger.LOGGER.info("Source IP Received: " + ip);

        IPResponseModel ipResponseModel = Transactions.sendIpStackRequest(ip);

        Transactions.storeLocation(ipResponseModel);
        //

        if (jsonText.length() == 4) {
            ServiceLogger.LOGGER.warning("No json text found.");
            return Response.status(Status.INTERNAL_SERVER_ERROR).build();
        }

        try {
            requestModel = mapper.readValue(jsonText, GetSessionRequestModel.class);

            String email = requestModel.getEmail();
            String sessionID = requestModel.getSessionID();
            ServiceLogger.LOGGER.info("Email: " + requestModel.getEmail());

            Transactions.storeLocation(ipResponseModel);
            Transactions.storeTransaction(transactionID, sessionID, ip);

            /*
            -------------------------ERROR CHECKING FOR EMAIL/PASSWORD-------------------------
             */

            if (!ValidateSession.valLength(sessionID))
                responseModel = constructResponseModel(-13, null);

            else if (!ValidateEmail.valFormat(email))
                responseModel = constructResponseModel(-11, null);

            else if (!ValidateEmail.valLength(email))
                responseModel = constructResponseModel(-10, null);

            if (responseModel.getMessage() != null) {
                return Response.status(Status.BAD_REQUEST).entity(responseModel).build();
            }

            /*
            -----------------------------------------------------------------------------------

            ---------------------CHECKING EMAIL/PASSWORD SATISFY REQUIREMENTS------------------
             */
            if (!ValidateEmail.valUnique(email, 1)) {
                responseModel = constructResponseModel(14, null);
                return Response.status(Status.OK).entity(responseModel).build();
            }

            // Session not found    (134)
            if (!ValidateSession.valExistence(email, sessionID)) {
                responseModel = constructResponseModel(134, null);
                return Response.status(Status.OK).entity(responseModel).build();
            }

            // Session is expired   (131)
            if (ValidateSession.valExpired(email, sessionID)) {
                responseModel = constructResponseModel(131, null);
                return Response.status(Status.OK).entity(responseModel).build();
            }
            // Session is closed    (132)
            if (ValidateSession.valClosed(email, sessionID)) {
                responseModel = constructResponseModel(132, null);
                return Response.status(Status.OK).entity(responseModel).build();
            }

            // Session is timed out (135)
            if (ValidateSession.valTimedOut(email, sessionID, currentTime)) {
                responseModel = constructResponseModel(135, null);
                return Response.status(Status.OK).entity(responseModel).build();
            }

            // Session is revoked   (133)
            String s = ValidateSession.valRevoked(email, sessionID, currentTime);

            if (s == null) {
                responseModel = constructResponseModel(133, null);
                return Response.status(Status.OK).entity(responseModel).build();
            }

            // Session is active    (130)
            responseModel = constructResponseModel(130, s);
            return Response.status(Status.OK)
                    .header("email", email)
                    .header("sessionID", s)
                    .entity(responseModel).build();

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
            Transactions.storeLocation(ipResponseModel);
            Transactions.storeTransaction(transactionID, null, ip);

            return Response.status(Status.BAD_REQUEST).entity(responseModel).build();
        }
    }

    private GetLoginResponseModel constructResponseModel(int resultCode, String sessionID) {
        ResultCodes resultCodes = new ResultCodes();
        GetLoginResponseModel responseModel;
        if (resultCode != 130) {
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
