package serviceTest;

import objects.Epic;
import objects.Subtask;
import objects.Status;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HttpTaskManagerSubtasksTest extends BaseHttpTest {
    private int createEpicForSubtask() {
        Epic epic = new Epic("Parent Epic", "Description");
        return manager.createEpic(epic);
    }

    @Test
    void testCreateSubtask_ValidSubtask_Returns201() throws IOException, InterruptedException {
        int epicId = createEpicForSubtask();

        String subtaskJson = String.format("""
            {
                "title": "New Subtask",
                "description": "New Subtask Description",
                "epicId": %d
            }
            """, epicId);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/subtasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        List<Subtask> subtasks = manager.getAllSubtasks();
        assertEquals(1, subtasks.size());
        assertEquals("New Subtask", subtasks.get(0).getTitle());
        assertEquals(epicId, subtasks.get(0).getEpicId());
    }

    @Test
    void testUpdateSubtask_UpdatesStatusAndEpicStatus() throws IOException, InterruptedException {
        int epicId = createEpicForSubtask();
        Subtask subtask = new Subtask("Subtask", "Description", epicId);
        int subtaskId = manager.createSubtask(subtask);

        String updatedSubtaskJson = String.format("""
            {
                "id": %d,
                "title": "Updated Subtask",
                "description": "Updated Description",
                "status": "DONE",
                "epicId": %d
            }
            """, subtaskId, epicId);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/subtasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(updatedSubtaskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        Subtask updatedSubtask = manager.getSubtask(subtaskId);
        assertEquals(Status.DONE, updatedSubtask.getStatus());
        assertEquals(Status.DONE, manager.getEpic(epicId).getStatus());
    }

    @Test
    void testGetSubtaskById_ReturnsSubtask() throws IOException, InterruptedException {
        int epicId = createEpicForSubtask();
        Subtask subtask = new Subtask("Test Subtask", "Description", epicId);
        int subtaskId = manager.createSubtask(subtask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/subtasks/" + subtaskId))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        Subtask responseSubtask = gson.fromJson(response.body(), Subtask.class);
        assertEquals(subtaskId, responseSubtask.getId());
        assertEquals(epicId, responseSubtask.getEpicId());
    }
}