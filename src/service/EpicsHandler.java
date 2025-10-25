package service;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import objects.Epic;
import objects.Subtask;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class EpicsHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;

    public EpicsHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            switch (method) {
                case "GET":
                    handleGet(exchange);
                    break;
                case "POST":
                    handlePost(exchange);
                    break;
                case "DELETE":
                    handleDelete(exchange);
                    break;
                default:
                    sendText(exchange, "Метод не поддерживается", 405);
            }
        } catch (Exception e) {
            sendInternalError(exchange);
        }
    }

    private void handleGet(HttpExchange exchange) throws IOException {
        Optional<Integer> idOpt = getPathId(exchange);
        if (idOpt.isPresent()) {
            Epic epic = taskManager.getEpic(idOpt.get());
            if (epic != null) {
                // Для GET /epics/{id} возвращаем эпик и его подзадачи
                EpicResponse response = new EpicResponse(epic, taskManager.getEpicSubtasks(epic.getId()));
                sendText(exchange, gson.toJson(response));
            } else {
                sendNotFound(exchange);
            }
        } else {
            sendText(exchange, gson.toJson(taskManager.getAllEpics()));
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        Optional<Epic> epicOpt = parseBody(exchange.getRequestBody(), Epic.class);
        if (epicOpt.isEmpty()) {
            sendBadRequest(exchange, "Некорректное тело запроса");
            return;
        }

        Epic epic = epicOpt.get();
        if (epic.getId() == 0) {
            int id = taskManager.createEpic(epic);
            sendText(exchange, "{\"id\": " + id + "}", 201);
        } else {
            taskManager.updateEpic(epic);
            sendText(exchange, "Эпик обновлен", 201);
        }
    }

    private void handleDelete(HttpExchange exchange) throws IOException {
        Optional<Integer> idOpt = getPathId(exchange);
        if (idOpt.isPresent()) {
            Epic epic = taskManager.getEpic(idOpt.get());
            if (epic != null) {
                taskManager.deleteEpic(idOpt.get());
                sendText(exchange, "Эпик удален");
            } else {
                sendNotFound(exchange);
            }
        } else {
            taskManager.deleteAllEpics();
            sendText(exchange, "Все эпики удалены");
        }
    }

    // Вспомогательный класс для ответа с эпиком и его подзадачами
    private static class EpicResponse {
        public Epic epic;
        public List<Subtask> subtasks;

        public EpicResponse(Epic epic, List<Subtask> subtasks) {
            this.epic = epic;
            this.subtasks = subtasks;
        }
    }
}