package service;

import com.sun.net.httpserver.HttpExchange;
import objects.Epic;
import objects.Subtask;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class EpicsHandler extends BaseHttpHandler {

    public EpicsHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            switch (method) {
                case "GET":
                    handleGetRequests(exchange, path);
                    break;
                case "POST":
                    handlePost(exchange);
                    break;
                default:
                    sendText(exchange, "Метод не поддерживается", 405);
            }
        } catch (Exception e) {
            sendInternalError(exchange);
        }
    }

    private void handleGetRequests(HttpExchange exchange, String path) throws IOException {
        if (path.equals("/epics")) {
            // GET /epics - получение всех эпиков (1 вызов)
            handleGetAllEpics(exchange);
        } else if (path.matches("/epics/\\d+/subtasks")) {
            // GET /epics/{id}/subtasks - получение подзадач эпика (1 вызов)
            handleGetEpicSubtasks(exchange);
        } else if (path.matches("/epics/\\d+")) {
            // GET /epics/{id} - получение эпика по ID (1 вызов - добавляет в историю)
            handleGetEpicById(exchange);
        } else {
            sendNotFound(exchange);
        }
    }

    private void handleGetAllEpics(HttpExchange exchange) throws IOException {
        List<Epic> epics = taskManager.getAllEpics();
        sendText(exchange, gson.toJson(epics));
    }

    private void handleGetEpicById(HttpExchange exchange) throws IOException {
        Optional<Integer> idOpt = getPathId(exchange);
        if (idOpt.isEmpty()) {
            sendBadRequest(exchange, "Некорректный ID");
            return;
        }

        // Один вызов - получаем эпик и автоматически добавляем в историю
        Epic epic = taskManager.getEpic(idOpt.get());
        if (epic != null) {
            sendText(exchange, gson.toJson(epic));
        } else {
            sendNotFound(exchange);
        }
    }

    private void handleGetEpicSubtasks(HttpExchange exchange) throws IOException {
        Optional<Integer> idOpt = getPathId(exchange);
        if (idOpt.isEmpty()) {
            sendBadRequest(exchange, "Некорректный ID");
            return;
        }

        // Проверяем существование эпика
        Epic epic = taskManager.getEpic(idOpt.get());
        if (epic == null) {
            sendNotFound(exchange);
            return;
        }

        // Один вызов - получаем подзадачи эпика
        List<Subtask> subtasks = taskManager.getEpicSubtasks(idOpt.get());
        sendText(exchange, gson.toJson(subtasks));
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        Optional<Epic> epicOpt = parseBody(exchange.getRequestBody(), Epic.class);
        if (epicOpt.isEmpty()) {
            sendBadRequest(exchange, "Некорректное тело запроса");
            return;
        }

        Epic epic = epicOpt.get();
        try {
            if (epic.getId() == 0) {
                // Один вызов - создаем эпик
                int id = taskManager.createEpic(epic);
                sendText(exchange, "{\"id\": " + id + "}", 201);
            } else {
                // Один вызов - обновляем эпик
                taskManager.updateEpic(epic);
                sendText(exchange, "Эпик обновлен", 201);
            }
        } catch (IllegalArgumentException e) {
            sendHasInteractions(exchange);
        }
    }
}




