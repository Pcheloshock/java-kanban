package service;

import com.sun.net.httpserver.HttpExchange;
import objects.Task;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class PrioritizedHandler extends BaseHttpHandler {

    public PrioritizedHandler(TaskManager taskManager) {
        super(taskManager); // Явный вызов конструктора родителя
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            sendText(exchange, "Метод не поддерживается", 405);
            return;
        }

        try {
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