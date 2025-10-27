package serviceTest;

import objects.Status;
import objects.Task;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HttpTaskManagerTasksTest extends BaseHttpTest {

    @Test
    void testGetAllTasks_WhenNoTasks_ReturnsEmptyList() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tasks"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        Task[] tasks = gson.fromJson(response.body(), Task[].class);
        assertEquals(0, tasks.length);
    }

    @Test
    void testGetAllTasks_WithTasks_ReturnsTasksList() throws IOException, InterruptedException {
        // Создаем задачу через менеджер
        Task task = new Task("Test Task", "Description");
        int taskId = manager.createTask(task);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tasks"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        Task[] tasks = gson.fromJson(response.body(), Task[].class);
        assertEquals(1, tasks.length);
        assertEquals("Test Task", tasks[0].getTitle());
    }

    @Test
    void testGetTaskById_WhenExists_ReturnsTask() throws IOException, InterruptedException {
        Task task = new Task("Test Task", "Description");
        int taskId = manager.createTask(task);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tasks/" + taskId))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        Task responseTask = gson.fromJson(response.body(), Task.class);
        assertEquals(taskId, responseTask.getId());
        assertEquals("Test Task", responseTask.getTitle());
    }

    @Test
    void testGetTaskById_WhenNotExists_Returns404() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tasks/999"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }

    @Test
    void testCreateTask_ValidTask_Returns201() throws IOException, InterruptedException {
        String taskJson = """
            {
                "title": "New Task",
                "description": "New Description",
                "status": "NEW"
            }
            """;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        List<Task> tasks = manager.getAllTasks();
        assertEquals(1, tasks.size());
        assertEquals("New Task", tasks.get(0).getTitle());
    }

    @Test
    void testUpdateTask_ValidTask_UpdatesSuccessfully() throws IOException, InterruptedException {
        Task task = new Task("Original Task", "Original Description");
        int taskId = manager.createTask(task);

        String updatedTaskJson = String.format("""
            {
                "id": %d,
                "title": "Updated Task",
                "description": "Updated Description",
                "status": "IN_PROGRESS"
            }
            """, taskId);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(updatedTaskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        Task updatedTask = manager.getTask(taskId);
        assertEquals("Updated Task", updatedTask.getTitle());
        assertEquals(Status.IN_PROGRESS, updatedTask.getStatus());
    }

    @Test
    void testDeleteTask_WhenExists_DeletesSuccessfully() throws IOException, InterruptedException {
        Task task = new Task("Task to delete", "Description");
        int taskId = manager.createTask(task);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tasks/" + taskId))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertNull(manager.getTask(taskId));
    }

    @Test
    void testDeleteAllTasks_WhenTasksExist_DeletesAll() throws IOException, InterruptedException {
        manager.createTask(new Task("Task 1", "Description 1"));
        manager.createTask(new Task("Task 2", "Description 2"));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tasks"))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals(0, manager.getAllTasks().size());
    }
}