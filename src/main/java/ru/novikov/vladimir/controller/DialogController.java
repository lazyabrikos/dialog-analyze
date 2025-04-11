package ru.novikov.vladimir.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.novikov.vladimir.model.Message;
import ru.novikov.vladimir.model.Response;
import ru.novikov.vladimir.service.DialogAnalyzerService;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Dialog Controller", description = "Операции с диалогами")
public class DialogController {

    private final DialogAnalyzerService dialogAnalyzerService;

    @PostMapping("/dialog")
    @Operation(summary = "Проанализировать диалог", description = "Принимает список сообщений и возвращает анализ")
    public List<Response> getDialogAnalyze(@RequestBody List<List<Message>> messages) {
        log.info("Got request with body type: {}", messages.getClass());
        return  dialogAnalyzerService.analyze(messages);

    }
}
