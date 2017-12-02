package com.raspberry.camera.dto;

import com.raspberry.camera.entity.DatabaseType;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

/**
 * Klasa służąca do transferu ustawień bazy danych
 */
public class DatabaseConfigDTO {

    @NotNull
    private DatabaseType databaseType;

    @NotNull
    @NotEmpty
    private String host;

    @NotNull
    private Integer port;

    @NotNull
    @NotEmpty
    private String databaseName;

    @NotNull
    @NotEmpty
    private String user;
    private String password;

    public DatabaseType getDatabaseType() {
        return databaseType;
    }

    public void setDatabaseType(DatabaseType databaseType) {
        this.databaseType = databaseType;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDatabaseUrl() throws Exception {
        StringBuilder url = new StringBuilder();
        String portNameSeparator;
        url.append("jdbc:");
        switch (databaseType) {
            case MYSQL:
                url.append("mysql://");
                portNameSeparator = "/";
                break;
            case POSTGRES:
                url.append("postgresql://");
                portNameSeparator = "/";
                break;
            default:
                throw new Exception("Database type not supported!");
        }
        url.append(host);
        url.append(":");
        url.append(port);
        url.append(portNameSeparator);
        url.append(databaseName);
        return url.toString();
    }
}
