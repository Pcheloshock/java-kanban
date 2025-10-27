package service;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

public class HistoryHandler extends BaseHttpHandler {

    public HistoryHandler(TaskManager taskManager) {
        super(taskManager); // Явный вызов конструктора родителя
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            sendText(exchange, "Метод не поддерживается", 405);
            return;
        }

        try {
            sendText(exchange, gson.toJson(taskManager.getHistory()));
        } catch (Exception e) {
            sendInternalError(exchange);
        }
    }
}