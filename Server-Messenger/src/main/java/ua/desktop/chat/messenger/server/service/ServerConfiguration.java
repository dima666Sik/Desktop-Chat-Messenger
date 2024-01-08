package ua.desktop.chat.messenger.server.service;

import ua.desktop.chat.messenger.prop.PropertiesFile;


public class ServerConfiguration {
    private static final String NAME_PROP_FILE = "server_connection.properties";
    private static final String PROP_VALUE_SERVER_PORT = "server.connection.port";

    private ServerConfiguration() {
    }

    public static int getServerPort() {
        return Integer.parseInt(PropertiesFile.getProp(NAME_PROP_FILE).getProperty(PROP_VALUE_SERVER_PORT));
    }
}

