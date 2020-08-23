package ai.hual.labrador.local.remote;

import ai.hual.labrador.dialog.accessors.DatabaseAccessor;
import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class RemoteDatabaseAccessor implements DatabaseAccessor {

    private BasicDataSource dataSource;

    public RemoteDatabaseAccessor(String driver, String url, String username, String password) {
        if (driver != null && url != null && username != null && password != null) {
            dataSource = new BasicDataSource();
            dataSource.setDriverClassName(driver);
            dataSource.setUrl(url);
            dataSource.setUsername(username);
            dataSource.setPassword(password);
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        if (dataSource == null) {
            return null;
        }
        return dataSource.getConnection();
    }

}
