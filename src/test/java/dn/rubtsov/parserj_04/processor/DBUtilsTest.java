package dn.rubtsov.parserj_04.processor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DBUtilsTest {

    @Mock
    private Connection connection;

    @Mock
    private Statement statement;

    @Mock
    private PreparedStatement preparedStatement;

    @InjectMocks
    private DBUtils dbUtils;

    @BeforeEach
    public void setUp() throws SQLException {
        // Настройка моков
        when(DriverManager.getConnection(anyString(), anyString(), anyString())).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
    }

    @Test
    public void testCreateTableIfNotExists() throws SQLException {
        String tableName = "test_table";

        dbUtils.createTableIfNotExists(tableName);

        String expectedSQL = "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                "uid UUID PRIMARY KEY DEFAULT gen_random_uuid(), " +
                "insert_date TIMESTAMP WITH TIME ZONE DEFAULT NOW(), " +
                "accountingDate VARCHAR(255), messageId VARCHAR(255), " +
                "productid VARCHAR(255), dispatchStatus INTEGER DEFAULT 0," +
                "registerType VARCHAR(255), restIn VARCHAR(255))";

        verify(statement).execute(expectedSQL);
    }

    @Test
    public void testInsertRecords() throws SQLException {
        Map<String, Object> data = new HashMap<>();
        data.put("accountingDate", "2024-10-25");
        data.put("productId", "123");
        data.put("dispatchStatus", 0);

        String tableName = "test_table";
        String insertSQL = "INSERT INTO " + tableName + " (accountingDate, productId, dispatchStatus) VALUES (?, ?, ?)";

        dbUtils.insertRecords(data, tableName);

        verify(preparedStatement).setString(1, "2024-10-25");
        verify(preparedStatement).setString(2, "123");
        verify(preparedStatement).setInt(3, 0);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    public void testInsertRecordsWithEmptyData() throws SQLException {
        dbUtils.insertRecords(new HashMap<>(), "test_table");
        // Проверяем, что executeUpdate не был вызван
        verify(preparedStatement, never()).executeUpdate();
    }

    @Test
    public void testInsertRecordsWithNullValue() throws SQLException {
        Map<String, Object> data = new HashMap<>();
        data.put("productId", null);
        String tableName = "test_table";

        dbUtils.insertRecords(data, tableName);

        verify(preparedStatement).setNull(1, java.sql.Types.NULL);
        verify(preparedStatement).executeUpdate();
    }

}
