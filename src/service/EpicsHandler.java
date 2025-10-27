package service;

import com.sun.net.httpserver.HttpExchange;
import objects.Epic;
import objects.Subtask;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class EpicsHandler extends BaseHttpHandler {

    public EpicsHandler(TaskManager taskManager) {
        super(taskManager); // Явный вызов конструктора родителя
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            if ("GET".equals(method)) {
                if (path.matches("/epics/\\d+/subtasks")) {
                    handleGetEpicSubtasks(exchange);
                } else if (path.matches("/epics/\\d+")) {
                    handleGetEpicById(exchange);
                } else if ("/epics".equals(path)) {
                    handleGetAllEpics(exchange);
                } else {
                    sendNotFound(exchange);
                }
            } else if ("POST".equals(method) && "/epics".equals(path)) {
                handlePost(exchange);
            } else if ("DELETE".equals(method)) {
                handleDelete(exchange);
            } else {
                sendText(exchange, "Метод не поддерживается", 405);
            }
        } catch (Exception e) {
            sendInternalError(exchange);
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

        Epic epic = taskManager.getEpic(idOpt.get());
        if (epic == null) {
            sendNotFound(exchange);
            return;
        }

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
                int id = taskManager.createEpic(epic);
                sendText(exchange, "{\"id\": " + id + "}", 201);
            } else {
                taskManager.updateEpic(epic);
                sendText(exchange, "Эпик обновлен", 201);
            }
        } catch (IllegalArgumentException e) {
            sendHasInteractions(exchange);
        }
    }

    private void handleDelete(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();

        if ("/epics".equals(path)) {
            taskManager.deleteAllEpics();
            sendText(exchange, "Все эпики удалены");
        } else if (path.matches("/epics/\\d+")) {
            Optional<Integer> idOpt = getPathId(exchange);
            if (idOpt.isEmpty()) {
                sendBadRequest(exchange, "Некорректный ID");
                return;
            }

            Epic epic = taskManager.getEpic(idOpt.get());
            if (epic != null) {
                taskManager.deleteEpic(idOpt.get());
                sendText(exchange, "Эпик удален");
            } else {
                sendNotFound(exchange);
            }
        } else {
            sendNotFound(exchange);
        }
    }
}