package service;

import com.sun.net.httpserver.HttpExchange;
import objects.Task;

import java.io.IOException;
import java.util.Optional;

public class TasksHandler extends BaseHttpHandler {

    public TasksHandler(TaskManager taskManager) {
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
}