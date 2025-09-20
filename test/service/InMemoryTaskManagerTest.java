import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import objects.*;
import service.*;

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
        Task savedTask = manager.getTask(taskId);

        assertNotNull(savedTask, "Задача не найдена");
        assertEquals(task.getTitle(), savedTask.getTitle(), "Названия задач не совпадают");
        assertEquals(task.getDescription(), savedTask.getDescription(), "Описания задач не совпадают");
        assertEquals(Status.NEW, savedTask.getStatus(), "Статус задачи не NEW");
    }

    @Test
    void testUpdateTask() {
        Task task = new Task("Test", "Description");
        int taskId = manager.createTask(task);

        // Создаем обновленную задачу с использованием сеттеров
        Task updatedTask = new Task("Updated", "Updated");
        updatedTask.setId(taskId);
        updatedTask.setStatus(Status.DONE);

        manager.updateTask(updatedTask);

        Task savedTask = manager.getTask(taskId);
        assertEquals("Updated", savedTask.getTitle(), "Название задачи не обновлено");
        assertEquals(Status.DONE, savedTask.getStatus(), "Статус задачи не обновлен");
    }

    @Test
    void testDeleteTask() {
        Task task = new Task("Test", "Description");
        int taskId = manager.createTask(task);
        manager.deleteTask(taskId);

        assertNull(manager.getTask(taskId), "Задача не удалена");
        assertTrue(manager.getAllTasks().isEmpty(), "Список задач не пуст");
    }

    // Тесты для Epic
    @Test
    void testAddAndGetEpic() {
        Epic epic = new Epic("Test Epic", "Description");
        int epicId = manager.createEpic(epic);
        Epic savedEpic = manager.getEpic(epicId);

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

        Subtask savedSubtask = manager.getSubtask(subtaskId);
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
        assertNull(manager.getSubtask(subtaskId), "Подзадачи не удалены");
        assertNull(manager.getEpic(epicId), "Эпик не удален");
    }

    // Тесты для истории
    @Test
    void testHistoryLimit() {
        for (int i = 0; i < 15; i++) {
            Task task = new Task("Task" + i, "Description");
            int taskId = manager.createTask(task);
            manager.getTask(taskId);
        }

        assertEquals(10, manager.getHistory().size(), "История превышает лимит");
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

        assertEquals(Status.NEW, manager.getEpic(epicId).getStatus(), "Статус эпика должен быть NEW");
    }

    @Test
    void testEpicStatusAllDone() {
        Epic epic = new Epic("Test Epic", "Description");
        int epicId = manager.createEpic(epic);
        Subtask subtask = new Subtask("Test", "Description", epicId);
        int subtaskId = manager.createSubtask(subtask);

        // Создаем обновленную подзадачу с использованием сеттеров
        Subtask updated = new Subtask("Test", "Description", epicId);
        updated.setId(subtaskId);
        updated.setStatus(Status.DONE);

        manager.updateSubtask(updated);

        assertEquals(Status.DONE, manager.getEpic(epicId).getStatus(), "Статус эпика должен быть DONE");
    }

    @Test
    void testEpicStatusInProgress() {
        Epic epic = new Epic("Test Epic", "Description");
        int epicId = manager.createEpic(epic);
        Subtask subtask1 = new Subtask("Test1", "Description", epicId);
        Subtask subtask2 = new Subtask("Test2", "Description", epicId);
        int id1 = manager.createSubtask(subtask1);
        int id2 = manager.createSubtask(subtask2);

        // Создаем обновленную подзадачу с использованием сеттеров
        Subtask updated1 = new Subtask("Test1", "Description", epicId);
        updated1.setId(id1);
        updated1.setStatus(Status.DONE);

        manager.updateSubtask(updated1);

        assertEquals(Status.IN_PROGRESS, manager.getEpic(epicId).getStatus(), "Статус эпика должен быть IN_PROGRESS");
    }
}