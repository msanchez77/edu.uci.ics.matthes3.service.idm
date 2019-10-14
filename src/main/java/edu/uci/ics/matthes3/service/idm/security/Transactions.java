package edu.uci.ics.matthes3.service.idm.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.uci.ics.matthes3.service.idm.IdmService;
import edu.uci.ics.matthes3.service.idm.logger.ServiceLogger;
import edu.uci.ics.matthes3.service.idm.models.ipstack.IPResponseModel;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.servlet.HttpServletRequestImpl;
import org.glassfish.jersey.jackson.JacksonFeature;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;


public class Transactions {
    private static final String API_KEY = "06165ceea9847ade5475fe5f5f805619";

    public static IPResponseModel sendIpStackRequest(String ip) {
        ServiceLogger.LOGGER.info("Getting request to receive information from IP Stack...");

        // Create a new client
        Client client = ClientBuilder.newClient();
        client.register(JacksonFeature.class);


        try {
            // Build URI

            // Testing with local ip, since I couldn't figure out how to use VPN/IP spoofing to use a dummy client IP
//            InetAddress localHost;
//            String local_ip = "";
//            try {
//                localHost = InetAddress.getLocalHost();
//                local_ip = localHost.getHostAddress();
//            } catch (UnknownHostException e) {
//                e.printStackTrace();
//            }
//            String uri = "http://api.ipstack.com/" + local_ip + "?access_key=" + API_KEY + "&hostname=1";



            // This is the uri built with the client ip.
            String uri = "http://api.ipstack.com/" + ip + "?access_key=" + API_KEY + "&hostname=1";

            // Create a WebTarget and set a request
            WebTarget webTarget = client.target(uri);

            // Create an InvocationBuilder to create the HTTP request
            Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);

            // Send the request and read response into string
            Response response = invocationBuilder.get();
            String jsonText = response.readEntity(String.class);
            jsonText = jsonText.replace("[","").replace("]","");

            ServiceLogger.LOGGER.info("Received jsonText: " + jsonText);

            // Attempt to save response as a model
            ObjectMapper mapper = new ObjectMapper();
            IPResponseModel responseModel = mapper.readValue(jsonText, IPResponseModel.class);

            ServiceLogger.LOGGER.info("Successfully mapped response into a model!");

            return responseModel;

        } catch (IOException e) {
            e.printStackTrace();
            ServiceLogger.LOGGER.info("Error reading IP Response Model.");
        }

        return null;
    }


    public static void storeLocation(IPResponseModel ipResponseModel) {

        String insert_query =
                "INSERT INTO locations " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

        try {
            PreparedStatement insert_ps = IdmService.getCon().prepareStatement(insert_query);

            insert_ps.setString(1, ipResponseModel.getIp());
            insert_ps.setString(2, ipResponseModel.getHostname());
            insert_ps.setString(3, ipResponseModel.getType());
            insert_ps.setString(4, ipResponseModel.getContinent_code());
            insert_ps.setString(5, ipResponseModel.getContinent_name());
            insert_ps.setString(6, ipResponseModel.getCountry_code());
            insert_ps.setString(7, ipResponseModel.getCountry_name());
            insert_ps.setString(8, ipResponseModel.getRegion_code());
            insert_ps.setString(9, ipResponseModel.getRegion_name());
            insert_ps.setString(10, ipResponseModel.getCity());
            insert_ps.setString(11, ipResponseModel.getZip());
            insert_ps.setFloat(12, ipResponseModel.getLatitude());
            insert_ps.setFloat(13, ipResponseModel.getLongitude());


            ServiceLogger.LOGGER.info("Trying query: " + insert_ps.toString());
            insert_ps.executeUpdate();
            ServiceLogger.LOGGER.info("Query succeeded!");

        } catch (SQLException e) {
            e.printStackTrace();
            ServiceLogger.LOGGER.info("Error storing information into transactions.");
        }
    }


    public static void storeTransaction(String transactionID, String sessionID, String ip) {

        String insert_query =
                "INSERT INTO transactions (transactionid, sessionid, source_ip, request_time) " +
                        "VALUES (?, ?, ?, ?);";

        try {
            PreparedStatement insert_ps = IdmService.getCon().prepareStatement(insert_query);

            insert_ps.setString(1, transactionID);
            insert_ps.setString(2, sessionID);
            insert_ps.setString(3, ip);
            insert_ps.setTimestamp(4, new Timestamp(System.currentTimeMillis()));

            ServiceLogger.LOGGER.info("Trying query: " + insert_ps.toString());
            insert_ps.executeUpdate();
            ServiceLogger.LOGGER.info("Query succeeded!");

        } catch (SQLException e) {
            e.printStackTrace();
            ServiceLogger.LOGGER.info("Error storing information into transactions.");
        }
    }

    public static InetAddress getIp() {
        InetAddress ip;

        try {
            ip = InetAddress.getLocalHost();
            ServiceLogger.LOGGER.info("Current IP address: " + ip.getHostAddress());

            return ip;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        return null;
    }

}
