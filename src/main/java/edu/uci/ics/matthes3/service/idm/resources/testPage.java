package edu.uci.ics.matthes3.service.idm.resources;

import edu.uci.ics.matthes3.service.idm.logger.ServiceLogger;
import edu.uci.ics.matthes3.service.idm.security.Transactions;
import org.glassfish.grizzly.http.server.Request;

import org.glassfish.grizzly.servlet.HttpServletRequestImpl;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.net.InetAddress;
import java.util.StringTokenizer;


@Path("test")
public class testPage {
    @Path("hello")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response hello(@Context Request req) {
        ServiceLogger.LOGGER.info("IP: " + getClientIpAddr(req));

//        String ip = getClientIpAddress(req);
//        ServiceLogger.LOGGER.info("IP: " + ip);

        return Response.status(Status.OK).build();
    }

    public static String getClientIpAddr(Request request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

}
