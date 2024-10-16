package dn.rubtsov.parserj_04.processor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dn.rubtsov.parserj_04.config.MappingConfiguration;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

@Component
@Slf4j
public class ParserJson {
    private final MappingConfiguration mappingConfiguration;
    @Autowired
    JsonProducer jsonProducer;
    @Autowired
    DBUtils dbUtils;
    @Autowired
    public ParserJson(MappingConfiguration mappingConfiguration) {
        this.mappingConfiguration = mappingConfiguration;
    }

    public void processJson(String json, String tableName) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(json);

        // Используем список для хранения всех записей
        List<Map<String, Object>> records = new ArrayList<>();

        // Получаем одиночные поля
        String accountingDate = rootNode.at(mappingConfiguration.getFieldMappings().get("accountingDate")).asText(null);
        String messageId = rootNode.at(mappingConfiguration.getFieldMappings().get("messageId")).asText(null);
        String productId = rootNode.at(mappingConfiguration.getFieldMappings().get("productId")).asText(null);

        // Проверяем обязательные поля
        if ( productId == null) {
            log.info("Забрали строку с productId = {}", productId);
            log.warn("Пропускаем запись: обязательные поля не заполнены.");
            return; 
        }

        // Обрабатываем массив registers
        JsonNode registersNode = rootNode.at(mappingConfiguration.getFieldMappings().get("registers"));
        if (registersNode.isArray()) {
            for (JsonNode register : registersNode) {
                // Создаем запись для каждого элемента массива
                Map<String, Object> record = new LinkedHashMap<>();
                record.put("productId", productId);
                record.put("messageId", messageId);
                record.put("accountingDate", accountingDate);
                record.put("registerType", register.at(mappingConfiguration.getFieldMappings().get("registerType")).asText(null));
                record.put("restIn", register.at(mappingConfiguration.getFieldMappings().get("restIn")).asText(null));

                // Проверяем наличие обязательных полей перед добавлением записи
                if (record.get("registerType") == null || record.get("restIn") == null) {
                    log.info("Забрали строку с registerType = {}, restIn = {}",record.get("registerType"),record.get("restIn"));
                    log.warn("Пропускаем запись: обязательные поля не заполнены.");
                    continue; 
                }
                // Добавляем запись в список
                records.add(record);
            }
        }
        // Выводим список записей для отладки
        System.out.println(records);

        // Обработка для вставки в базу данных
        for (Map<String, Object> record : records) {
            dbUtils.insertRecords(record, tableName);
        }
    }

    /** Метод замены значений полей test2.json на значения одноименных
     * полей записи из таблицы message_db БД и отправки в kafka.
     */
    @Scheduled(cron = "1/10 * * * * ?")
    public void MessageDBToJson() {
        try {
            // Получаем требуемые данные из базы
            Map<String,Object> messageDB = dbUtils.getAndUpdateFirstRecordWithDispatchStatus();
            if (messageDB.isEmpty()) {
                log.info("Нет данных для обработки.");
                return;
            }

            // Читаем шаблон JSON из файла
            ObjectMapper objectMapper = new ObjectMapper();
            File jsonFile = Paths.get("src", "main", "resources", "test2.json").toFile();

            // Преобразовываем JSON файл в объект JsonNode
            JsonNode jsonTemplate = null;
            try {
                jsonTemplate = objectMapper.readTree(jsonFile);
            } catch (IOException e) {
                throw new FileNotFoundException("Файла шаблона Json по указанному пути нет");
            }

            // Рекурсивно мап пим объект messageDB на JSON-шаблон
            mapFieldsToJson(messageDB, jsonTemplate);

            // Преобразуем итоговый объект JsonNode обратно в строку
            String json = objectMapper.writeValueAsString(jsonTemplate);
            log.info("Сообщение: {}", json);
            jsonProducer.sendMessage(json);

        } catch (IOException | IllegalAccessException e) {
            log.error("Ошибка при обработке JSON или данных из базы.",e);
        }
    }

    // Метод для рекурсивного маппинг полей объекта на JSON
    private static void mapFieldsToJson(Map<String, Object> messageDB, JsonNode jsonNode) throws IllegalAccessException {
        for (Map.Entry<String,Object> field : messageDB.entrySet()) {
            Object value = field.getValue();  

            if (value != null) {
                replaceValueInJson(jsonNode, field.getKey(), value);
            }
        }
    }

    // Метод для замены значения в JSON с учетом массивов
    private static void replaceValueInJson(JsonNode jsonNode, String fieldName, Object value) {

        if (jsonNode.isObject()) {
            // Проверяем наличие ключа без учета регистра
            for (Iterator<String> it = ((ObjectNode) jsonNode).fieldNames(); it.hasNext(); ) {
                String key = it.next();
                if (key.equalsIgnoreCase(fieldName)) {
                    // Заменяем значение, если ключ найден
                    ((ObjectNode) jsonNode).put(fieldName, value.toString());
                    return; 
                }
            }
        }

        // Проходим по дочерним узлам и проверяем массивы
        for (JsonNode childNode : jsonNode) {
            if (childNode.isObject()) {
                replaceValueInJson(childNode, fieldName, value);
            } else if (childNode.isArray()) {
                // Если узел является массивом, проходим по каждому элементу массива
                for (JsonNode arrayItem : childNode) {
                    if (arrayItem.isObject()) {
                        replaceValueInJson(arrayItem, fieldName, value);
                    }
                }
            }
        }
    }

}
