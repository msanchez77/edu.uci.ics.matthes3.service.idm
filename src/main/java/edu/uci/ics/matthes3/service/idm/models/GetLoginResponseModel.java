package edu.uci.ics.matthes3.service.idm.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.uci.ics.matthes3.service.idm.core.ResultCodes;
import edu.uci.ics.matthes3.service.idm.logger.ServiceLogger;

@JsonInclude(JsonInclude.Include.NON_NULL) // Tells Jackson to ignore all fields with value of NULL
public class GetLoginResponseModel {
    @JsonProperty(required = true) // Forces this field to be required in the JSON
    private int resultCode;
    @JsonProperty(required = true) // Forces this field to be required in the JSON
    private String message;

    @JsonIgnore
    private String sessionID;

    ResultCodes resultCodes = new ResultCodes();

    public GetLoginResponseModel() {
    }

    public GetLoginResponseModel(
            @JsonProperty(value="resultCode", required = true) int resultCode,
            @JsonProperty(value="sessionID") String sessionID) {
        this.resultCode = resultCode;
        this.message = resultCodes.results.get(resultCode); // ResultCodes.setMessage is NOT required for your implementation
        this.sessionID = sessionID;
    }

    public GetLoginResponseModel(
            @JsonProperty(value="resultCode", required = true) int resultCode) {
        this.resultCode = resultCode;
        this.message = resultCodes.results.get(resultCode); // ResultCodes.setMessage is NOT required for your implementation
        this.sessionID = null;

        ServiceLogger.LOGGER.info("Message is set to: " + this.message);
    }

    @Override
    public String toString() {
        return "ResultCode: " + resultCode + ", Message: " + message + ", sessiondID: " + sessionID;
    }


    @JsonProperty("resultCode") // Forces Jackson to name the field "resultCode" when serializing this object
    public int getResultCode() {
        return resultCode;
    }

    @JsonProperty("message") // Forces Jackson to name the field "resultCode" when serializing this object
    public String getMessage() {
        return message;
    }

    @JsonProperty(value = "sessionID") // Forces Jackson to name the field "resultCode" when serializing this object
    public String getSessionID() {
        return sessionID;
    }
}
