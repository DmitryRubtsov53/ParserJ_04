package dn.rubtsov.parserj_04.controller;

import dn.rubtsov.parserj_04.exception.EmptyFileException;
import dn.rubtsov.parserj_04.processor.ParserJson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/json")
@Slf4j
public class JsonController {

    @Autowired
    private ParserJson parserJson;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadJson(@RequestParam("file") MultipartFile file) {
        if(file.isEmpty()) {
            log.error("Прислали пустой файл");
            throw new EmptyFileException("Файл не должен быть пустым");
        }

        try {
            // Преобразуем содержимое файла в строку
            String jsonContent = new String(file.getBytes());

            // Передаем данные в сервис для обработки
           parserJson.processJson(jsonContent,"message_db");

            return new ResponseEntity<>("JSON данные успешно обработаны.", HttpStatus.OK);

        } catch (IOException e) {
            log.error("Ошибка при чтении файла: {}", e.getMessage(), e);
            return new ResponseEntity<>("Ошибка при чтении файла.", HttpStatus.BAD_REQUEST);

        } catch (Exception e) {
            log.error("Ошибка при обработке файла: {}", e.getMessage(), e);
            return new ResponseEntity<>("Ошибка при обработке файла.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
