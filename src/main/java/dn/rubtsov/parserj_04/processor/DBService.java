package dn.rubtsov.parserj_04.processor;

import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public interface DBService {

    /** Метод динамического создания таблицы для объекта. */
    void createTableIfNotExists(String tableName);

    /** Метод вставки данных объектов в таблицу. */
    void insertRecords(Map<String, Object> data, String tableName);

    /** Метод для создания динамического SQL-запроса для вставки. */
    default String createInsertSQL(String tableName, List<String> fieldNames) {
        String columns = String.join(", ", fieldNames);
        String valuesPlaceholder = String.join(", ", Collections.nCopies(fieldNames.size(), "?"));
        return "INSERT INTO " + tableName + " (" + columns + ") VALUES (" + valuesPlaceholder + ")";
    }

    /** Универсальный метод для удаления таблицы. */
    void dropTableIfExists(String tableName);

    /** Метод считывания 1-й записи БД с dispatchStatus = 0 и замены его на 1.
     * @return карта с парами ключ-значение полей считанной записи.
     */
    Map<String, Object> getAndUpdateFirstRecordWithDispatchStatus();
}
