package service;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import objects.Task;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class PrioritizedHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;

    public PrioritizedHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            sendText(exchange, "Метод не поддерживается", 405);
            return;
        }

        try {
            // Собираем все задачи и сортируем по приоритету (по id для простоты)
            List<Task> allTasks = taskManager.getAllTasks();
            allTasks.addAll(taskManager.getAllSubtasks());

            List<Task> prioritized = allTasks.stream()
                    .sorted(Comparator.comparingInt(Task::getId))
                    .collect(Collectors.toList());

            sendText(exchange, gson.toJson(prioritized));
        } catch (Exception e) {
            sendInternalError(exchange);
        }
    }
}