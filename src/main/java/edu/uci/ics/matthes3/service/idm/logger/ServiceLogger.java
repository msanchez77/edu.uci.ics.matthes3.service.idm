package edu.uci.ics.matthes3.service.idm.logger;

import java.io.File;
import java.io.IOException;
import java.util.logging.*;

public class ServiceLogger {
    public static final Logger LOGGER = Logger.getLogger(ServiceLogger.class.getName());
    private static FileHandler fileHandler;
    private static Formatter formatter;

    public static void initLogger(String outputDir, String outputFile) throws IOException {
        System.err.println("Initializing logger...");
        // Remove the default ConsoleHandler
        LOGGER.getParent().removeHandler(LOGGER.getParent().getHandlers()[0]);

        // Create directory for logs
        File logDir = new File(outputDir);
        if (!(logDir.exists())) {
            logDir.mkdir();
        }

        // Create FileHandler
        fileHandler = new FileHandler(outputDir + outputFile);
        // Create simple formatter
        formatter = new ServiceFormatter();
        // Assign handler to logger
        LOGGER.addHandler(fileHandler);
        // Set formatter to the handler
        fileHandler.setFormatter(formatter);
        // Create new ConsoleHandler
        ConsoleHandler consoleHandler = new ConsoleHandler();
        // Set log level of console handler
        consoleHandler.setLevel(Level.CONFIG);
        // Add console handler to logger
        LOGGER.addHandler(consoleHandler);
        // Set formatter for console handler
        consoleHandler.setFormatter(formatter);

        // Setting Level to ALL
        fileHandler.setLevel(Level.ALL);
        LOGGER.setLevel(Level.ALL);

        LOGGER.config("Logging initialized.");
    }
}
