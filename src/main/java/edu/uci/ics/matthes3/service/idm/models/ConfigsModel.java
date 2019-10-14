package edu.uci.ics.matthes3.service.idm.models;

import java.util.Map;

public class ConfigsModel {
    private Map<String,String> serviceConfig;
    private Map<String,String> loggerConfig;
    private Map<String,String> databaseConfig;
    private Map<String,String> sessionConfig;

    public ConfigsModel() { }

    public Map<String, String> getServiceConfig() {
        return serviceConfig;
    }

    public void setServiceConfig(Map<String, String> serviceConfig) {
        this.serviceConfig = serviceConfig;
    }

    public Map<String, String> getLoggerConfig() {
        return loggerConfig;
    }

    public void setLoggerConfig(Map<String, String> loggerConfig) {
        this.loggerConfig = loggerConfig;
    }

    public Map<String, String> getDatabaseConfig() {
        return databaseConfig;
    }

    public void setDatabaseConfig(Map<String, String> databaseConfig) {
        this.databaseConfig = databaseConfig;
    }

    public Map<String, String> getSessionConfig() {
        return sessionConfig;
    }

    public void setSessionConfig(Map<String, String> sessionConfig) {
        this.sessionConfig = sessionConfig;
    }

}
