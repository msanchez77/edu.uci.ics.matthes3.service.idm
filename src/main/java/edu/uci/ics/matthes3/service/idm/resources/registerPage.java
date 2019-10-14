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
import edu.uci.ics.matthes3.service.idm.security.Transactions;
import org.apache.commons.codec.binary.Hex;
import org.glassfish.grizzly.http.server.Request;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.ValidationEvent;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;

@Path("register")
public class registerPage {
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
    public static final int USER_STATUS_LOCKED = 3;
    public static final int USER_STATUS_REVOKED = 4;
    public static final int USER_STATUS_TIMED_OUT = 5;
    public static final int USER_STATUS_DEFAULT = USER_STATUS_ACTIVE;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registerUser(@Context HttpHeaders headers, String jsonText) {
        ServiceLogger.LOGGER.info("Getting request to register user...");

        //
        String transactionID = headers.getHeaderString("transactionID");
        String sessionID = headers.getHeaderString("sessionID");
        String ip = headers.getHeaderString("sourceIP");
        ServiceLogger.LOGGER.info("Source IP Received: " + ip);

        IPResponseModel ipResponseModel = Transactions.sendIpStackRequest(ip);

        Transactions.storeLocation(ipResponseModel);
        Transactions.storeTransaction(transactionID,null, ip);
        //

        ServiceLogger.LOGGER.info("JSON Text: " + jsonText);
        ObjectMapper mapper = new ObjectMapper();
        GetRegisterRequestModel requestModel;
        GetRegisterResponseModel responseModel = new GetRegisterResponseModel();

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
            if (!ValidatePassword.valEmptyOrNull(password))
                responseModel = constructResponseModel(-12);

            else if (!ValidateEmail.valFormat(email))
                responseModel = constructResponseModel(-11);

            else if (!ValidateEmail.valLength(email))
                responseModel = constructResponseModel(-10);

            if (responseModel.getMessage() != null)
                return Response.status(Status.BAD_REQUEST).entity(responseModel).build();


            /*
            -----------------------------------------------------------------------------------

            ---------------------CHECKING EMAIL/PASSWORD SATISFY REQUIREMENTS------------------
             */
            if (!ValidatePassword.valLength(password))
                responseModel = constructResponseModel(12);
            else if (!ValidatePassword.valFormat(password))
                responseModel = constructResponseModel(13);
            else if (!ValidateEmail.valUnique(email, 0))
                responseModel = constructResponseModel(16);

            if (responseModel.getMessage() != null)
                return Response.status(Status.OK).entity(responseModel).build();



            /*
            -----------------------------------------------------------------------------------
             */

            responseModel = constructResponseModel(110);
            InsertUserRequestModel insertUserRequestModel = setupInsert(requestModel.getEmail(), requestModel.getPassword());

            return Response.status(Status.OK).entity(responseModel).build();
        } catch (IOException e) {
            e.printStackTrace();
            // CASE -3
            if (e instanceof JsonParseException) {
                responseModel = constructResponseModel(-3);
            } else if (e instanceof JsonMappingException) {
                responseModel = constructResponseModel(-2);
            } else {
                ServiceLogger.LOGGER.warning("IOException");
            }

            return Response.status(Status.BAD_REQUEST).entity(responseModel).build();
        }
    }

    private GetRegisterResponseModel constructResponseModel(int resultCode) {
        ResultCodes resultCodes = new ResultCodes();
        GetRegisterResponseModel responseModel = new GetRegisterResponseModel(
                resultCode,
                resultCodes.results.get(resultCode)
        );

        if (resultCode < -1)
            ServiceLogger.LOGGER.warning(resultCodes.results.get(resultCode));
        else if (resultCode > 0)
            ServiceLogger.LOGGER.info(resultCodes.results.get(resultCode));

        return responseModel;
    }

    private InsertUserRequestModel setupInsert(String email, char[] pword) {
        ServiceLogger.LOGGER.info("Received request to insert user into DB.");
        ObjectMapper mapper = new ObjectMapper();
        InsertUserRequestModel insertUserRequestModel = new InsertUserRequestModel();

        byte[] salt = Crypto.genSalt();
        String hashedPass = Hex.encodeHexString(Crypto.hashPassword(pword, salt, Crypto.ITERATIONS, Crypto.KEY_LENGTH));
        String encodedSalt = Hex.encodeHexString(salt);

        insertUserRequestModel.setEmail(email);
        insertUserRequestModel.setStatus(USER_STATUS_DEFAULT);
        insertUserRequestModel.setPlevel(PLEVEL_DEFAULT);
        insertUserRequestModel.setSalt(encodedSalt);
        insertUserRequestModel.setPword(hashedPass);


        if (UserRecords.insertUserToDB(insertUserRequestModel)) {
            ServiceLogger.LOGGER.info("Record successfully inserted into DB");
        } else {
            ServiceLogger.LOGGER.warning("Error occurred when inserting record into DB");
            ServiceLogger.LOGGER.warning(insertUserRequestModel.getEmail());
        }

        return insertUserRequestModel;
    }

    private String getHashedPass(byte[] hashedPassword) {
        StringBuffer buf = new StringBuffer();
        for (byte b : hashedPassword) {
            buf.append(format(Integer.toHexString(Byte.toUnsignedInt(b))));
        }
        return buf.toString();
    }

    private String format(String binS) {
        int length = 2 - binS.length();
        char[] padArray = new char[length];
        Arrays.fill(padArray, '0');
        String padString = new String(padArray);
        return padString + binS;
    }

}
