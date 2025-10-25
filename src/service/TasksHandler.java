package service;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import objects.Task;

import java.io.IOException;
import java.util.Optional;

public class TasksHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;

    public TasksHandler(TaskManager taskManager) {
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
            Task task = taskManager.getTask(idOpt.get());
            if (task != null) {
                sendText(exchange, gson.toJson(task));
            } else {
                sendNotFound(exchange);
            }
        } else {
            sendText(exchange, gson.toJson(taskManager.getAllTasks()));
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        Optional<Task> taskOpt = parseBody(exchange.getRequestBody(), Task.class);
        if (taskOpt.isEmpty()) {
            sendBadRequest(exchange, "Некорректное тело запроса");
            return;
        }

        Task task = taskOpt.get();
        try {
            if (task.getId() == 0) {
                int id = taskManager.createTask(task);
                sendText(exchange, "{\"id\": " + id + "}", 201);
            } else {
                taskManager.updateTask(task);
                sendText(exchange, "Задача обновлена", 201);
            }
        } catch (IllegalArgumentException e) {
            sendHasInteractions(exchange);
        }
    }

    private void handleDelete(HttpExchange exchange) throws IOException {
        Optional<Integer> idOpt = getPathId(exchange);
        if (idOpt.isPresent()) {
            Task task = taskManager.getTask(idOpt.get());
            if (task != null) {
                taskManager.deleteTask(idOpt.get());
                sendText(exchange, "Задача удалена");
            } else {
                sendNotFound(exchange);
            }
        } else {
            taskManager.deleteAllTasks();
            sendText(exchange, "Все задачи удалены");
        }
    }
}