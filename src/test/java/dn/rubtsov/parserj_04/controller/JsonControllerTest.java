package dn.rubtsov.parserj_04.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import dn.rubtsov.parserj_04.processor.DBService;
import dn.rubtsov.parserj_04.processor.ParserJson;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
@WebMvcTest(JsonController.class)
public class JsonControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    ParserJson parserJson;
    @MockBean
    DBService dbService;

    @Test
    @DisplayName("Файл пустой -> возвращает 400")
    public void uploadJson_WhenFileIsEmpty_ShouldReturnBadRequest() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "",
                "application/json", new byte[]{});

        mockMvc.perform(multipart("/api/json/upload")
                        .file(file))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Файл валидный -> возвращает 200")
    public void uploadJson_WhenFileIsValid_ShouldReturnOk() throws Exception {
        String jsonContent = "{\"key\": \"value\"}";
        MockMultipartFile file = new MockMultipartFile("file", "test.json",
                MediaType.APPLICATION_JSON_VALUE, jsonContent.getBytes());

        doNothing().when(parserJson).processJson(anyString(), anyString());

        mockMvc.perform(multipart("/api/json/upload")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(content().string("JSON данные успешно обработаны."));
    }

    @Test
    @DisplayName("Файл не читаемый -> возвращает 400")
    public void uploadJson_WhenIOExceptionOccurs_ShouldReturnBadRequest() throws Exception {
        String jsonContent = "{\"key\": \"value\"}";
        MockMultipartFile file = new MockMultipartFile("file", "test.json",
                MediaType.APPLICATION_JSON_VALUE, jsonContent.getBytes());

        doThrow(new IOException("Ошибка при чтении файла")).when(parserJson).processJson(anyString(), anyString());

        mockMvc.perform(multipart("/api/json/upload")
                        .file(file))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Ошибка при чтении файла."));
    }

    @Test
    @DisplayName("Ошибка сервера -> возвращает 500")
    public void uploadJson_WhenOtherExceptionOccurs_ShouldReturnInternalServerError() throws Exception {
        String jsonContent = "{\"key\": \"value\"}";
        MockMultipartFile file = new MockMultipartFile("file", "test.json",
                MediaType.APPLICATION_JSON_VALUE, jsonContent.getBytes());

        doThrow(new RuntimeException("Ошибка на стороне сервера.")).when(parserJson)
                .processJson(anyString(), anyString());

        mockMvc.perform(multipart("/api/json/upload")
                        .file(file))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Ошибка на стороне сервера."));
    }
}
