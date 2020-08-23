package ai.hual.labrador.dialog.accessors;

import java.sql.Connection;
import java.sql.SQLException;

public interface DatabaseAccessor {

    Connection getConnection() throws SQLException;

}
