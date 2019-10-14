package edu.uci.ics.matthes3.service.idm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import edu.uci.ics.matthes3.service.idm.configs.Configs;
import edu.uci.ics.matthes3.service.idm.logger.ServiceLogger;
import edu.uci.ics.matthes3.service.idm.models.ConfigsModel;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.internal.util.ExceptionUtils;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.core.UriBuilder;
import java.io.File;
import java.io.IOException;
import java.net.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class IdmService {
    public static edu.uci.ics.matthes3.service.idm.IdmService idmService;
    private static final String MYSQL_DRIVER = "com.mysql.cj.jdbc.Driver";

    private static Connection con = null;
    private static Configs configs = new Configs();

    // Logic handlers
//    public static RegisterUser registerUser = new RegisterUser();
//    public static LoginUser loginUser = new LoginUser();
//    public static VerifySession verifySession = new VerifySession();
//    public static VerifyPrivilege verifyPrivilege = new VerifyPrivilege();
//    public static UpdatePassword updatePassword = new UpdatePassword();

    public static void main(String[] args) {
        idmService = new edu.uci.ics.matthes3.service.idm.IdmService();
        idmService.initService(args);
    }

    private void initService(String[] args) {
        // Validate arguments
        validateArguments(args);
        // Exec the arguments
        execArguments(args);
        // Initialize logging
        initLogging();
        ServiceLogger.LOGGER.config("Starting service...");
        configs.currentConfigs();
        // Connect to database
        connectToDatabase();
        // Initialize HTTP server
        initHTTPServer();

        if (con != null) {
            ServiceLogger.LOGGER.config("Service initialized.");
        } else {
            ServiceLogger.LOGGER.config("Service initialized with error(s)");
        }
    }

    private void validateArguments(String[] args) {
        boolean isConfigOptionSet = false;
        for (int i = 0; i < args.length; ++i) {
            switch (args[i]) {
                case "--default":
                case "-d":
                    if (i + 1 < args.length) {
                        exitAppFailure("Invalid arg after " + args[i] + " option: " + args[i + 1]);
                    }
                case "--config":
                case "-c":
                    if (!isConfigOptionSet) {
                        isConfigOptionSet = true;
                        ++i;
                    } else {
                        exitAppFailure("Conflicting configuration file arguments.");
                    }
                    break;

                default:
                    exitAppFailure("Unrecognized argument: " + args[i]);
            }
        }
    }

    private void execArguments(String[] args) {
        if (args.length > 0) {
            for (int i = 0; i < args.length; ++i) {
                switch (args[i]) {
                    case "--config":
                    case "-c":
                        // Config file specified. Load it.
                        getConfigFile(args[i + 1]);
                        ++i;
                        break;
                    case "--default":
                    case "-d":
                        System.err.println("Default config options selected.");
                        configs = new Configs();
                        break;
                    default:
                        exitAppFailure("Unrecognized argument: " + args[i]);
                }
            }
        } else {
            System.err.println("No config file specified. Using default values.");
            configs = new Configs();
        }
    }

    private void getConfigFile(String configFile) {
        try {
            System.err.println("Config file name: " + configFile);
            configs = new Configs(loadConfigs(configFile));
            System.err.println("Configuration file successfully loaded.");
        } catch (NullPointerException e) {
            System.err.println("Config file not found. Using default values.");
            configs = new Configs();
        }
    }

    private ConfigsModel loadConfigs(String file) {
        System.err.println("Loading configuration file...");
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        ConfigsModel configs = null;

        try {
            configs = mapper.readValue(new File(file), ConfigsModel.class);
        } catch (IOException e) {
            exitAppFailure("Unable to load configuration file.");
        }
        return configs;
    }

    private void initLogging() {
        try {
            ServiceLogger.initLogger(configs.getOutputDir(), configs.getOutputFile());
        } catch (IOException e) {
            exitAppFailure("Unable to initialize logging.");
        }
    }

    private void initHTTPServer() {
        ServiceLogger.LOGGER.config("Initializing HTTP server...");
        String scheme = configs.getScheme();
        String hostName = configs.getHostName();
        int port = configs.getPort();
        String path = configs.getPath();

        try {
            ServiceLogger.LOGGER.config("Building URI from configs...");
            URI uri = UriBuilder.fromUri(scheme + hostName + path).port(port).build();
            ServiceLogger.LOGGER.config("Final URI: " + uri.toString());
            ResourceConfig rc = new ResourceConfig().packages("edu.uci.ics.matthes3.service.idm.resources");
            ServiceLogger.LOGGER.config("Set Jersey resources.");
            rc.register(JacksonFeature.class);
            ServiceLogger.LOGGER.config("Set Jackson as serializer.");
            ServiceLogger.LOGGER.config("Starting HTTP server...");
            HttpServer server = GrizzlyHttpServerFactory.createHttpServer(uri, rc, false);
            server.start();
            ServiceLogger.LOGGER.config("HTTP server started.");
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    private void connectToDatabase() {
        ServiceLogger.LOGGER.config("Connecting to database...");
        String driver = "";

        if (!configs.isDbConfigValid()) {
            ServiceLogger.LOGGER.config("Database configurations not valid. Cannot connect to database.");
            return;
        }

        switch (configs.getDbDriver().toLowerCase()) {
            case "mysql":
                ServiceLogger.LOGGER.config("Using MySQL driver: " + MYSQL_DRIVER);
                driver = MYSQL_DRIVER;
                break;

            default:
                ServiceLogger.LOGGER.severe("No database driver found. Cannot connect to a database.");
                return;
        }

        try {
//            driver = configs.getDbDriver().toLowerCase();
            ServiceLogger.LOGGER.config("Using driver: " + driver);
            Class.forName(driver);
            ServiceLogger.LOGGER.config("Database URL: " + configs.getDbUrl());
            con = DriverManager.getConnection(configs.getDbUrl(), configs.getDbUsername(), configs.getDbPassword());
            ServiceLogger.LOGGER.config("Connected to database: " + configs.getDbUrl());
        } catch (ClassNotFoundException | SQLException | NullPointerException e) {
            ServiceLogger.LOGGER.severe("Unable to connect to database.\n" + ExceptionUtils.exceptionStackTraceAsString(e));
        }
    }

    private void exitAppFailure(String message) {
        System.err.println("ERROR: " + message);
        System.err.println("Usage options: ");
        System.err.println("\tSpecify configuration file:");
        System.err.println("\t\t--config [file]");
        System.err.println("\t\t-c");
        System.err.println("\tUse default configuration:");
        System.err.println("\t\t--default");
        System.err.println("\t\t-d");
        System.exit(-1);
    }

    public static Connection getCon() {
        return con;
    }

    public static Configs getConfigs() {
        return configs;
    }
}

