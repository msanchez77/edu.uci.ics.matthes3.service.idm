package edu.uci.ics.matthes3.service.idm.resources;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.uci.ics.matthes3.service.idm.core.*;
import edu.uci.ics.matthes3.service.idm.logger.ServiceLogger;
import edu.uci.ics.matthes3.service.idm.models.*;
import edu.uci.ics.matthes3.service.idm.models.ipstack.IPResponseModel;
import edu.uci.ics.matthes3.service.idm.security.Crypto;
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

@Path("privilege")
public class privilegePage {
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response privilegeCheck(@Context HttpHeaders headers, String jsonText) {
        ServiceLogger.LOGGER.info("Getting request to check privilege...");
        ObjectMapper mapper = new ObjectMapper();
        GetVerifyRequestModel requestModel;
        GetRegisterResponseModel responseModel = new GetRegisterResponseModel();

        //
        String transactionID = headers.getHeaderString("transactionID");
        String sessionID = headers.getHeaderString("sessionID");
        String ip = headers.getHeaderString("sourceIP");
        ServiceLogger.LOGGER.info("Source IP Received: " + ip);

        IPResponseModel ipResponseModel = Transactions.sendIpStackRequest(ip);

        Transactions.storeLocation(ipResponseModel);
        Transactions.storeTransaction(transactionID, sessionID, ip);
        //

        if (jsonText.length() == 4) {
            ServiceLogger.LOGGER.warning("No json text found.");
            return Response.status(Status.INTERNAL_SERVER_ERROR).build();
        }

        try {
            requestModel = mapper.readValue(jsonText, GetVerifyRequestModel.class);

            String email = requestModel.getEmail();
            int plevel = requestModel.getPlevel();
            ServiceLogger.LOGGER.info("Email: " + requestModel.getEmail());

            /*
            -------------------------ERROR CHECKING FOR EMAIL/PASSWORD-------------------------
             */
            if (!ValidatePrivilege.valRange(plevel))
                responseModel = constructResponseModel(-14);

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
            if (!ValidateEmail.valUnique(email, 1)) {
                responseModel = constructResponseModel(14);
                return Response.status(Status.OK).entity(responseModel).build();
            }

            if (!ValidatePrivilege.valSufficient(email, plevel))
                responseModel = constructResponseModel(141);
            else
                responseModel = constructResponseModel(140);

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

}
