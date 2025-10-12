package serviceTest;

import objects.Epic;
import objects.Status;
import objects.Subtask;
import objects.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.InMemoryTaskManager;
import service.TaskManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {
    private TaskManager manager;

    @BeforeEach
    void setUp() {
        manager = new InMemoryTaskManager();
    }

    // Тесты для Task
    @Test
    void testAddAndGetTask() {
        Task task = new Task("Test", "Description");
        int taskId = manager.createTask(task);
        Task savedTask = manager.getTask(taskId).orElseThrow(); // ← ДОБАВЬТЕ .orElseThrow()

        assertNotNull(savedTask, "Задача не найдена");
        assertEquals(task.getTitle(), savedTask.getTitle(), "Названия задач не совпадают");
        assertEquals(task.getDescription(), savedTask.getDescription(), "Описания задач не совпадают");
        assertEquals(Status.NEW, savedTask.getStatus(), "Статус задачи не NEW");
    }

    @Test
    void testUpdateTask() {
        Task task = new Task("Test", "Description");
        int taskId = manager.createTask(task);

        Task updatedTask = new Task("Updated", "Updated");
        updatedTask.setId(taskId);
        updatedTask.setStatus(Status.DONE);

        manager.updateTask(updatedTask);

        Task savedTask = manager.getTask(taskId).orElseThrow(); // ← ДОБАВЬТЕ .orElseThrow()
        assertEquals("Updated", savedTask.getTitle(), "Название задачи не обновлено");
        assertEquals(Status.DONE, savedTask.getStatus(), "Статус задачи не обновлен");
    }

    @Test
    void testDeleteTask() {
        Task task = new Task("Test", "Description");
        int taskId = manager.createTask(task);
        manager.deleteTask(taskId);

        // Replace assertNull with assertTrue and isEmpty()
        assertTrue(manager.getTask(taskId).isEmpty(), "Задача не удалена");
        assertTrue(manager.getAllTasks().isEmpty(), "Список задач не пуст");
    }

    // Тесты для Epic
    @Test
    void testAddAndGetEpic() {
        Epic epic = new Epic("Test Epic", "Description");
        int epicId = manager.createEpic(epic);
        Epic savedEpic = manager.getEpic(epicId).orElseThrow(); // ← ДОБАВЬТЕ .orElseThrow()

        assertNotNull(savedEpic, "Эпик не найден");
        assertEquals(Status.NEW, savedEpic.getStatus(), "Статус эпика не NEW");
    }

    // Тесты для Subtask
    @Test
    void testAddAndGetSubtask() {
        Epic epic = new Epic("Test Epic", "Description");
        int epicId = manager.createEpic(epic);
        Subtask subtask = new Subtask("Test", "Description", epicId);
        int subtaskId = manager.createSubtask(subtask);

        Subtask savedSubtask = manager.getSubtask(subtaskId).orElseThrow(); // ← ДОБАВЬТЕ .orElseThrow()
        assertNotNull(savedSubtask, "Подзадача не найдена");
        assertEquals(epicId, savedSubtask.getEpicId(), "ID эпика не совпадает");
    }

    @Test
    void testDeleteEpicRemovesSubtasks() {
        Epic epic = new Epic("Test Epic", "Description");
        int epicId = manager.createEpic(epic);
        Subtask subtask = new Subtask("Test", "Description", epicId);
        int subtaskId = manager.createSubtask(subtask);

        manager.deleteEpic(epicId);

        // Check that both Optional objects are empty
        assertTrue(manager.getSubtask(subtaskId).isEmpty(), "Подзадачи не удалены");
        assertTrue(manager.getEpic(epicId).isEmpty(), "Эпик не удален");
    }

    @Test
    void testHistoryRemoveDuplicates() {
        Task task = new Task("Test", "Description");
        int taskId = manager.createTask(task);

        // Добавляем задачу в историю multiple times
        for (int i = 0; i < 5; i++) {
            manager.getTask(taskId);
        }

        // Должна остаться только одна запись
        assertEquals(1, manager.getHistory().size(), "История содержит дубликаты");
        assertEquals(taskId, manager.getHistory().get(0).getId(), "Неверная задача в истории");
    }

    @Test
    void testHistoryOrderWithDuplicates() {
        Task task1 = new Task("Task1", "Description");
        Task task2 = new Task("Task2", "Description");
        int id1 = manager.createTask(task1);
        int id2 = manager.createTask(task2);

        // Добавляем в разном порядке
        manager.getTask(id1);
        manager.getTask(id2);
        manager.getTask(id1); // Повторный просмотр

        List<Task> history = manager.getHistory();
        assertEquals(2, history.size(), "Неверное количество задач в истории");
        assertEquals(id2, history.get(0).getId(), "Неверный порядок в истории");
        assertEquals(id1, history.get(1).getId(), "Неверный порядок в истории");
    }

    @Test
    void testHistoryOrder() {
        Task task1 = new Task("Task1", "Description");
        Task task2 = new Task("Task2", "Description");
        int id1 = manager.createTask(task1);
        int id2 = manager.createTask(task2);

        manager.getTask(id1);
        manager.getTask(id2);

        List<Task> history = manager.getHistory();
        assertEquals(2, history.size(), "Неверное количество задач в истории");
        assertEquals(id1, history.get(0).getId(), "Неверный порядок в истории");
        assertEquals(id2, history.get(1).getId(), "Неверный порядок в истории");
    }

    // Тесты для статусов Epic
    @Test
    void testEpicStatusAllNew() {
        Epic epic = new Epic("Test Epic", "Description");
        int epicId = manager.createEpic(epic);
        Subtask subtask = new Subtask("Test", "Description", epicId);
        manager.createSubtask(subtask);

        assertEquals(Status.NEW, manager.getEpic(epicId).orElseThrow().getStatus(), "Статус эпика должен быть NEW");
        // ↑ ДОБАВЬТЕ .orElseThrow() перед .getStatus()
    }

    @Test
    void testEpicStatusAllDone() {
        Epic epic = new Epic("Test Epic", "Description");
        int epicId = manager.createEpic(epic);
        Subtask subtask = new Subtask("Test", "Description", epicId);
        int subtaskId = manager.createSubtask(subtask);

        Subtask updated = new Subtask("Test", "Description", epicId);
        updated.setId(subtaskId);
        updated.setStatus(Status.DONE);

        manager.updateSubtask(updated);

        assertEquals(Status.DONE, manager.getEpic(epicId).orElseThrow().getStatus(), "Статус эпика должен быть DONE");
        // ↑ ДОБАВЬТЕ .orElseThrow() перед .getStatus()
    }

    @Test
    void testEpicStatusInProgress() {
        Epic epic = new Epic("Test Epic", "Description");
        int epicId = manager.createEpic(epic);
        Subtask subtask1 = new Subtask("Test1", "Description", epicId);
        Subtask subtask2 = new Subtask("Test2", "Description", epicId);
        int id1 = manager.createSubtask(subtask1);
        int id2 = manager.createSubtask(subtask2);

        Subtask updated1 = new Subtask("Test1", "Description", epicId);
        updated1.setId(id1);
        updated1.setStatus(Status.DONE);

        manager.updateSubtask(updated1);

        assertEquals(Status.IN_PROGRESS, manager.getEpic(epicId).orElseThrow().getStatus(), "Статус эпика должен быть IN_PROGRESS");
        // ↑ ДОБАВЬТЕ .orElseThrow() перед .getStatus()
    }
}