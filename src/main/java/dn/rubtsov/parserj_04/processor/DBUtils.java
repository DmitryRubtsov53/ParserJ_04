package dn.rubtsov.parserj_04.processor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.*;
@Service
@Slf4j
public class DBUtils implements DBService{
    private final String URL, USER, PASSWORD;

    public DBUtils(@Value("${spring.datasource.url}") String URL,
                   @Value("${spring.datasource.username}")String USER,
                   @Value("${spring.datasource.password}")String PASSWORD) {
        this.URL = URL;
        this.USER = USER;
        this.PASSWORD = PASSWORD;
    }

    @Override
    public void createTableIfNotExists(String tableName) {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                "uid UUID PRIMARY KEY DEFAULT gen_random_uuid(), " +
                "insert_date TIMESTAMP WITH TIME ZONE DEFAULT NOW(), " +
                "accountingDate VARCHAR(255), " +
                "messageId VARCHAR(255), " +
                "productid VARCHAR(255), " +
                "dispatchStatus INTEGER DEFAULT 0," +
                "registerType VARCHAR(255)," +
                "restIn VARCHAR(255))";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement statement = connection.createStatement()) {
            statement.execute(createTableSQL);
        } catch (SQLException e) {
            log.error("Ошибка при выполнении create-запроса: {}", e.getMessage(), e);
        }
    }

    @Override
    public void insertRecords(Map<String, Object> data, String tableName) {
        if (data == null || data.isEmpty()) {
            log.info("Нет данных для вставки");
            return;
        }
        // Создаем динамический SQL-запрос для вставки данных
        String insertDataSQL = createInsertSQL(tableName, new ArrayList<>(data.keySet()));
        System.out.println(insertDataSQL);

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(insertDataSQL)) {

            int i = 0;
            // Устанавливаем значения в запрос
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                Object value = entry.getValue();
                // Проверяем тип значения и устанавливаем в PreparedStatement
                if (value instanceof String) {
                    preparedStatement.setString(i + 1, (String) value);
                } else if (value instanceof Integer) {
                    preparedStatement.setInt(i + 1, (Integer) value);
                } else if (value instanceof Double) {
                    preparedStatement.setDouble(i + 1, (Double) value);
                } else if (value instanceof Boolean) {
                    preparedStatement.setBoolean(i + 1, (Boolean) value);
                } else if (value == null) {
                    preparedStatement.setNull(i + 1, java.sql.Types.NULL);
                } else {
                    throw new IllegalArgumentException("Unsupported data type: " + value.getClass().getName());
                }
                i++;
            }

            // Выполняем запрос на вставку
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            log.error("Ошибка при выполнении insert-запроса: {}", e.getMessage(), e);
        }
    }

    @Override
    public void dropTableIfExists(String tableName) {
        String dropSQL = "DROP TABLE IF EXISTS " + tableName + " CASCADE";
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement statement = connection.createStatement()) {
            statement.execute(dropSQL);
        } catch (SQLException e) {
            log.error("Ошибка при выполнении drop-запроса: {}", e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> getAndUpdateFirstRecordWithDispatchStatus() {

        String selectSQL = "SELECT uid, productid, messageid, accountingdate, registertype, restin FROM message_db WHERE dispatchStatus = 0 LIMIT 1";
        // SQL-запрос для обновления статуса записи в таблице
        String updateSQL = "UPDATE message_db SET dispatchStatus = 1 WHERE uid = ?";
        // Карта для хранения значений выборки
        Map<String, Object> resultMap = new LinkedHashMap<>();

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement selectStatement = connection.prepareStatement(selectSQL);
             PreparedStatement updateStatement = connection.prepareStatement(updateSQL);
             ResultSet resultSet = selectStatement.executeQuery()) {

            // Заполняем карту полями из результата выборки
            if (resultSet.next()) {
                resultMap.put("uid", resultSet.getString("uid"));
                resultMap.put("productid", resultSet.getString("productid"));
                resultMap.put("messageid", resultSet.getString("messageid"));
                resultMap.put("accountingdate", resultSet.getString("accountingdate"));
                resultMap.put("registerType", resultSet.getString("registertype"));
                resultMap.put("restIn", resultSet.getInt("restin"));

                // Обновляем значение dispatchStatus в таблице по uid
                updateStatement.setObject(1, UUID.fromString((String) resultMap.get("uid")));
                updateStatement.executeUpdate();
                // Удаляем, так как uid нам нужен только для изменения dispatchStatus
                resultMap.remove("uid");
            }

        } catch (SQLException e) {
            log.error("Ошибка при выполнении SQL-запроса: {}", e.getMessage(), e);
        }

        return resultMap;
    }
}
