package edu.uci.ics.matthes3.service.idm.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.uci.ics.matthes3.service.idm.core.ResultCodes;
import edu.uci.ics.matthes3.service.idm.core.UserRecords;
import edu.uci.ics.matthes3.service.idm.core.ValidateEmail;
import edu.uci.ics.matthes3.service.idm.core.ValidatePassword;
import edu.uci.ics.matthes3.service.idm.exceptions.ModelValidationException;
import edu.uci.ics.matthes3.service.idm.logger.ServiceLogger;
import edu.uci.ics.matthes3.service.idm.models.*;
import edu.uci.ics.matthes3.service.idm.security.Crypto;
import edu.uci.ics.matthes3.service.idm.security.Transactions;
import edu.uci.ics.matthes3.service.idm.utilities.ModelValidator;
import org.apache.commons.codec.binary.Hex;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

@Path("user")
public class userPage {
    @Path("updatePassword")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updatePassword(String jsonText) {

        UpdatePasswordRequestModel requestModel;
        GetRegisterResponseModel responseModel = new GetRegisterResponseModel();

        try {
            requestModel = (UpdatePasswordRequestModel) ModelValidator.verifyModel(jsonText, UpdatePasswordRequestModel.class);
        } catch (ModelValidationException e) {
            return ModelValidator.returnInvalidRequest(e, UpdatePasswordRequestModel.class, null, null, null);
        }

        String email = requestModel.getEmail();
        char[] newPassword = requestModel.getNewpword();
        char[] oldPassword = requestModel.getOldpword();

        if (!ValidatePassword.valEmptyOrNull(newPassword)) {
            responseModel = constructResponseModel(-12);
            return Response.status(Status.BAD_REQUEST).entity(responseModel).build();
        }

        else if (!ValidateEmail.valFormat(email)) {
            responseModel = constructResponseModel(-11);
            return Response.status(Status.BAD_REQUEST).entity(responseModel).build();
        }

        else if (!ValidateEmail.valLength(email)) {
            responseModel = constructResponseModel(-10);
            return Response.status(Status.BAD_REQUEST).entity(responseModel).build();
        }

        if (responseModel.getMessage() != null)
            return Response.status(Status.BAD_REQUEST).entity(responseModel).build();

        /*
        -----------------------------------------------------------------------------------

        ---------------------CHECKING EMAIL/PASSWORD SATISFY REQUIREMENTS------------------
        */

        if (!UserRecords.passwordMatch(email, oldPassword)) {
            responseModel = constructResponseModel(11);
            return Response.status(Status.OK).entity(responseModel).build();
        }
        if (!ValidatePassword.valLength(newPassword)) {
            responseModel = constructResponseModel(12);
            return Response.status(Status.OK).entity(responseModel).build();
        }
        if (!ValidatePassword.valFormat(newPassword)) {
            responseModel = constructResponseModel(13);
            return Response.status(Status.OK).entity(responseModel).build();
        }
        if (!ValidateEmail.valUnique(email, 1)) {
            responseModel = constructResponseModel(14);
            return Response.status(Status.OK).entity(responseModel).build();
        }


        responseModel = constructResponseModel(150);
        return Response.status(Status.OK).entity(responseModel).build();

    }

    private void setupInsert(String email, char[] pword) {
        ServiceLogger.LOGGER.info("Received request to update user password in DB.");

        byte[] salt = Crypto.genSalt();
        String hashedPass = Hex.encodeHexString(Crypto.hashPassword(pword, salt, Crypto.ITERATIONS, Crypto.KEY_LENGTH));
        String encodedSalt = Hex.encodeHexString(salt);


        UserRecords.updateUserPassword(email, encodedSalt, hashedPass);
    }

    private GetRegisterResponseModel constructResponseModel(int resultCode) {
        ResultCodes resultCodes = new ResultCodes();
        GetRegisterResponseModel responseModel;

        responseModel = new GetRegisterResponseModel(
                resultCode,
                resultCodes.results.get(resultCode)
        );

        if (resultCode < -1)
            ServiceLogger.LOGGER.warning(resultCodes.results.get(resultCode));
        else if (resultCode > 0)
            ServiceLogger.LOGGER.info(resultCodes.results.get(resultCode));

        return responseModel;
    }

}
