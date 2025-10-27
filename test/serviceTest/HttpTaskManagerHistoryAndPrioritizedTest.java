package serviceTest;

import objects.Task;
import objects.Epic;
import objects.Subtask;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HttpTaskManagerHistoryAndPrioritizedTest extends BaseHttpTest {

    @Test
    void testGetHistory_ReturnsViewedTasks() throws IOException, InterruptedException {
        Task task = new Task("Task 1", "Description");
        int taskId = manager.createTask(task);

        Epic epic = new Epic("Epic 1", "Description");
        int epicId = manager.createEpic(epic);

        // Просматриваем задачи для добавления в историю
        manager.getTask(taskId);
        manager.getEpic(epicId);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/history"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        Task[] history = gson.fromJson(response.body(), Task[].class);
        assertEquals(2, history.length);
    }

    @Test
    void testGetPrioritized_ReturnsSortedTasks() throws IOException, InterruptedException {
        Task task1 = new Task("Task 1", "Description");
        Task task2 = new Task("Task 2", "Description");
        manager.createTask(task1);
        manager.createTask(task2);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/prioritized"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        Task[] prioritized = gson.fromJson(response.body(), Task[].class);
        assertTrue(prioritized.length >= 2);
    }
}