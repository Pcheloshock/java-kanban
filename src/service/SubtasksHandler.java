package service;

import com.sun.net.httpserver.HttpExchange;
import objects.Subtask;

import java.io.IOException;
import java.util.Optional;

public class SubtasksHandler extends BaseHttpHandler {

    public SubtasksHandler(TaskManager taskManager) {
        super(taskManager); // Явный вызов конструктора родителя
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();

            if ("GET".equals(method)) {
                handleGet(exchange);
            } else if ("POST".equals(method)) {
                handlePost(exchange);
            } else {
                sendText(exchange, "Метод не поддерживается", 405);
            }
        } catch (Exception e) {
            sendInternalError(exchange);
        }
    }

    private void handleGet(HttpExchange exchange) throws IOException {
        Optional<Integer> idOpt = getPathId(exchange);
        if (idOpt.isPresent()) {
            Subtask subtask = taskManager.getSubtask(idOpt.get());
            if (subtask != null) {
                sendText(exchange, gson.toJson(subtask));
            } else {
                sendNotFound(exchange);
            }
        } else {
            sendText(exchange, gson.toJson(taskManager.getAllSubtasks()));
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        Optional<Subtask> subtaskOpt = parseBody(exchange.getRequestBody(), Subtask.class);
        if (subtaskOpt.isEmpty()) {
            sendBadRequest(exchange, "Некорректное тело запроса");
            return;
        }

        Subtask subtask = subtaskOpt.get();
        try {
            if (subtask.getId() == 0) {
                int id = taskManager.createSubtask(subtask);
                sendText(exchange, "{\"id\": " + id + "}", 201);
            } else {
                taskManager.updateSubtask(subtask);
                sendText(exchange, "Подзадача обновлена", 201);
            }
        } catch (IllegalArgumentException e) {
            sendHasInteractions(exchange);
        }
    }
}