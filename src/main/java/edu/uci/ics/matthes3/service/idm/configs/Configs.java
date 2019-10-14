package edu.uci.ics.matthes3.service.idm.configs;

import edu.uci.ics.matthes3.service.idm.logger.ServiceLogger;
import edu.uci.ics.matthes3.service.idm.models.ConfigsModel;

public class Configs {
    private final int MIN_SERVICE_PORT = 1024;
    private final int MAX_SERVICE_PORT = 65535;
    // Default service configs
    private final String DEFAULT_SCHEME = "http://";
    private final String DEFAULT_HOSTNAME = "0.0.0.0";
    private final int    DEFAULT_PORT = 8297;
    private final String DEFAULT_PATH = "/api/idm";
    // Default logger configs
    private final String DEFAULT_OUTPUTDIR = "./logs/";
    private final String DEFAULT_OUTPUTFILE = "idm.log";

    // Service configs
    private String scheme;
    private String hostName;
    private int    port;
    private String path;
    // Logger configs
    private String outputDir;
    private String outputFile;
    // Database configs
    private String dbUsername;
    private String dbPassword;
    private String dbHostname;
    private int    dbPort;
    private String dbName;
    private String dbDriver;
    private String dbSettings;

    private boolean dbConfigValid = true;

    // Session configs
    private long timeout;
    private long expiration;

    private boolean sessionConfigValid = true;

    public Configs() {
        scheme = DEFAULT_SCHEME;
        hostName = DEFAULT_HOSTNAME;
        port = DEFAULT_PORT;
        path = DEFAULT_PATH;
        outputDir = DEFAULT_OUTPUTDIR;
        outputFile = DEFAULT_OUTPUTFILE;
        dbConfigValid = false;
        sessionConfigValid = false;
    }

    public Configs(ConfigsModel cm) throws NullPointerException {
        if (cm == null) {
            throw new NullPointerException("Unable to create Configs from ConfigsModel.");
        } else {
            // Set service configs
            scheme = cm.getServiceConfig().get("scheme");
            if (scheme == null) {
                scheme = DEFAULT_SCHEME;
                System.err.println("Scheme not found in configuration file. Using default.");
            } else {
                System.err.println("Scheme: " + scheme);
            }

            hostName = cm.getServiceConfig().get("hostName");
            if (hostName == null) {
                hostName = DEFAULT_HOSTNAME;
                System.err.println("Hostname not found in configuration file. Using default.");
            } else {
                System.err.println("Hostname: " + hostName);
            }

            port = Integer.parseInt(cm.getServiceConfig().get("port"));
            if (port == 0) {
                port = DEFAULT_PORT;
                System.err.println("Port not found in configuration file. Using default.");
            } else if (port < MIN_SERVICE_PORT || port > MAX_SERVICE_PORT) {
                port = DEFAULT_PORT;
                System.err.println("Port is not within valid range. Using default.");
            } else {
                System.err.println("Port: " + port);
            }

            path = cm.getServiceConfig().get("path");
            if (path == null) {
                path = DEFAULT_PATH;
                System.err.println("Path not found in configuration file. Using default.");
            } else {
                System.err.println("Path: " + path);
            }

            // Set logger configs
            outputDir = cm.getLoggerConfig().get("outputDir");
            if (outputDir == null) {
                outputDir = DEFAULT_OUTPUTDIR;
                System.err.println("Logging output directory not found in configuration file. Using default.");
            } else {
                System.err.println("Logging output directory: " + outputDir);
            }

            outputFile = cm.getLoggerConfig().get("outputFile");
            if (outputFile == null) {
                outputFile = DEFAULT_OUTPUTFILE;
                System.err.println("Logging output file not found in configuration file. Using default.");
            } else {
                System.err.println("Logging output file: " + outputFile);
            }

            // Set database configs
            dbUsername = cm.getDatabaseConfig().get("dbUsername");
            if (dbUsername == null) {
                System.err.println("No database username found in configuration file.");
                dbConfigValid = false;
            } else {
                System.err.println("Database username: " + dbUsername);
            }

            dbPassword = cm.getDatabaseConfig().get("dbPassword");
            if (dbPassword == null) {
                System.err.println("No database password found in configuration file.");
                dbConfigValid = false;
            } else {
                System.err.println("Database password found in configuration file.");
            }

            dbHostname = cm.getDatabaseConfig().get("dbHostname");
            if (dbHostname == null) {
                System.err.println("No database hostname found in configuration file.");
                dbConfigValid = false;
            } else {
                System.err.println("Database hostname: " + dbHostname);
            }

            dbPort = Integer.parseInt(cm.getDatabaseConfig().get("dbPort"));
            if (dbPort == 0) {
                System.err.println("No database port found in configuration file.");
                dbConfigValid = false;
            } else if (dbPort < MIN_SERVICE_PORT || dbPort > MAX_SERVICE_PORT) {
                System.err.println("Database port is not within a valid range.");
                dbConfigValid = false;
            } else {
                System.err.println("Database port: " + dbPort);
            }

            dbName = cm.getDatabaseConfig().get("dbName");
            if (dbName == null) {
                System.err.println("No database name found in configuration file.");
                dbConfigValid = false;
            } else {
                System.err.println("Database name: " + dbName);
            }

            dbDriver = cm.getDatabaseConfig().get("dbDriver");
            if (dbDriver == null) {
                System.err.println("No driver found in configuration file.");
                dbConfigValid = false;
            } else {
                System.err.println("Database driver: " + dbDriver);
            }

            dbSettings = cm.getDatabaseConfig().get("dbSettings");
            if (dbSettings == null) {
                System.err.println("No connection settings found in configuration file.");
                dbConfigValid = false;
            } else {
                System.err.println("Database connection settings: " + dbSettings);
            }

            timeout = Long.parseLong(cm.getSessionConfig().get("timeout"));
            if (timeout <= 0) {
                System.err.println("No timeout settings found in configuration file.");
                sessionConfigValid = false;
            } else {
                System.err.println("Session timeout settings: " + timeout);
            }

            expiration = Long.parseLong(cm.getSessionConfig().get("expiration"));
            if (expiration <= 0) {
                System.err.println("No expiration settings found in configuration file.");
                sessionConfigValid = false;
            } else {
                System.err.println("Session expiration settings: " + expiration);
            }
        }
    }

    public void currentConfigs() {
        ServiceLogger.LOGGER.config("Scheme: " + scheme);
        ServiceLogger.LOGGER.config("Hostname: " + hostName);
        ServiceLogger.LOGGER.config("Port: " + port);
        ServiceLogger.LOGGER.config("Path: " + path);
        ServiceLogger.LOGGER.config("Logger output directory: " + outputDir);
        ServiceLogger.LOGGER.config("Logger output file: " + outputFile);
        ServiceLogger.LOGGER.config("Database hostname: " + dbHostname);
        ServiceLogger.LOGGER.config("Database port: " + dbPort);
        ServiceLogger.LOGGER.config("Database username: " + dbUsername);
        ServiceLogger.LOGGER.config("Database password provided? " + (dbPassword != null));
        ServiceLogger.LOGGER.config("Database name: " + dbName);
        ServiceLogger.LOGGER.config("Database driver: " + dbDriver);
        ServiceLogger.LOGGER.config("Database connection settings: " + dbSettings);
        ServiceLogger.LOGGER.config("Session timeout: " + timeout);
        ServiceLogger.LOGGER.config("Session expiration: " + expiration);
    }

    public String getDbUrl() {
        return "jdbc:" + dbDriver + "://" + dbHostname + ":" + dbPort + "/" + dbName + dbSettings;
    }

    public String getScheme() {
        return scheme;
    }

    public String getHostName() {
        return hostName;
    }

    public int getPort() {
        return port;
    }

    public String getPath() {
        return path;
    }

    public String getOutputDir() {
        return outputDir;
    }

    public String getOutputFile() {
        return outputFile;
    }

    public String getDbUsername() {
        return dbUsername;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public String getDbHostname() {
        return dbHostname;
    }

    public int getDbPort() {
        return dbPort;
    }

    public String getDbName() {
        return dbName;
    }

    public String getDbDriver() {
        return dbDriver;
    }

    public String getDbSettings() {
        return dbSettings;
    }

    public boolean isDbConfigValid() {
        return dbConfigValid;
    }

    public long getSessionTimeout() {
        return timeout;
    }

    public long getSessionExpiration() {
        return expiration;
    }
}
