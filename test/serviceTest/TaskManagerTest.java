package serviceTest;

import objects.Epic;
import objects.Status;
import objects.Subtask;
import objects.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.TaskManager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public abstract class TaskManagerTest<T extends TaskManager> {
    protected T manager;

    protected abstract T createManager();

    @BeforeEach
    void setUp() {
        manager = createManager();
    }

    // Тесты для статуса Epic
    @Test
    void testEpicStatusAllNew() {
        Epic epic = new Epic("Test Epic", "Description");
        int epicId = manager.createEpic(epic);

        Subtask subtask1 = new Subtask("Subtask 1", "Description", epicId);
        Subtask subtask2 = new Subtask("Subtask 2", "Description", epicId);
        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);

        assertEquals(Status.NEW, manager.getEpic(epicId).orElseThrow().getStatus(),
                "Статус эпика должен быть NEW, когда все подзадачи NEW");
    }

    @Test
    void testEpicStatusAllDone() {
        Epic epic = new Epic("Test Epic", "Description");
        int epicId = manager.createEpic(epic);

        Subtask subtask1 = new Subtask("Subtask 1", "Description", epicId);
        Subtask subtask2 = new Subtask("Subtask 2", "Description", epicId);
        int subtaskId1 = manager.createSubtask(subtask1);
        int subtaskId2 = manager.createSubtask(subtask2);

        // Обновляем обе подзадачи в DONE
        Subtask updated1 = new Subtask(subtaskId1, "Subtask 1", "Description", Status.DONE, epicId);
        Subtask updated2 = new Subtask(subtaskId2, "Subtask 2", "Description", Status.DONE, epicId);
        manager.updateSubtask(updated1);
        manager.updateSubtask(updated2);

        assertEquals(Status.DONE, manager.getEpic(epicId).orElseThrow().getStatus(),
                "Статус эпика должен быть DONE, когда все подзадачи DONE");
    }

    @Test
    void testEpicStatusMixedNewAndDone() {
        Epic epic = new Epic("Test Epic", "Description");
        int epicId = manager.createEpic(epic);

        Subtask subtask1 = new Subtask("Subtask 1", "Description", epicId);
        Subtask subtask2 = new Subtask("Subtask 2", "Description", epicId);
        int subtaskId1 = manager.createSubtask(subtask1);
        int subtaskId2 = manager.createSubtask(subtask2);

        // Обновляем одну подзадачу в DONE, оставляя другую NEW
        Subtask updated1 = new Subtask(subtaskId1, "Subtask 1", "Description", Status.DONE, epicId);
        manager.updateSubtask(updated1);

        assertEquals(Status.IN_PROGRESS, manager.getEpic(epicId).orElseThrow().getStatus(),
                "Статус эпика должен быть IN_PROGRESS, когда есть подзадачи NEW и DONE");
    }

    @Test
    void testEpicStatusAllInProgress() {
        Epic epic = new Epic("Test Epic", "Description");
        int epicId = manager.createEpic(epic);

        Subtask subtask1 = new Subtask("Subtask 1", "Description", epicId);
        Subtask subtask2 = new Subtask("Subtask 2", "Description", epicId);
        int subtaskId1 = manager.createSubtask(subtask1);
        int subtaskId2 = manager.createSubtask(subtask2);

        // Обновляем обе подзадачи в IN_PROGRESS
        Subtask updated1 = new Subtask(subtaskId1, "Subtask 1", "Description", Status.IN_PROGRESS, epicId);
        Subtask updated2 = new Subtask(subtaskId2, "Subtask 2", "Description", Status.IN_PROGRESS, epicId);
        manager.updateSubtask(updated1);
        manager.updateSubtask(updated2);

        assertEquals(Status.IN_PROGRESS, manager.getEpic(epicId).orElseThrow().getStatus(),
                "Статус эпика должен быть IN_PROGRESS, когда все подзадачи IN_PROGRESS");
    }

    @Test
    void testEpicStatusEmptySubtasks() {
        Epic epic = new Epic("Test Epic", "Description");
        int epicId = manager.createEpic(epic);

        assertEquals(Status.NEW, manager.getEpic(epicId).orElseThrow().getStatus(),
                "Статус эпика должен быть NEW, когда нет подзадач");
    }

    // Тесты на пересечение временных интервалов
    @Test
    void testTaskTimeOverlapDetection() {
        LocalDateTime baseTime = LocalDateTime.of(2024, 1, 1, 10, 0);

        Task task1 = new Task("Task 1", "Description");
        task1.setStartTime(baseTime);
        task1.setDuration(Duration.ofMinutes(60));
        manager.createTask(task1);

        Task task2 = new Task("Task 2", "Description");
        task2.setStartTime(baseTime.plusMinutes(30)); // Пересекается с task1
        task2.setDuration(Duration.ofMinutes(60));

        assertThrows(service.ManagerSaveException.class, () -> manager.createTask(task2),
                "Должно быть исключение при пересечении временных интервалов");
    }

    @Test
    void testTaskNoTimeOverlap() {
        LocalDateTime baseTime = LocalDateTime.of(2024, 1, 1, 10, 0);

        Task task1 = new Task("Task 1", "Description");
        task1.setStartTime(baseTime);
        task1.setDuration(Duration.ofMinutes(60));
        manager.createTask(task1);

        Task task2 = new Task("Task 2", "Description");
        task2.setStartTime(baseTime.plusMinutes(90)); // Не пересекается
        task2.setDuration(Duration.ofMinutes(60));

        assertDoesNotThrow(() -> manager.createTask(task2),
                "Не должно быть исключения при отсутствии пересечения временных интервалов");
    }

    @Test
    void testTaskWithoutTimeCanBeAdded() {
        Task task1 = new Task("Task 1", "Description");
        manager.createTask(task1);

        Task task2 = new Task("Task 2", "Description");
        assertDoesNotThrow(() -> manager.createTask(task2),
                "Не должно быть исключения при добавлении задач без времени");
    }

    // Стандартные тесты для всех методов TaskManager
    @Test
    void testCreateAndGetTask() {
        Task task = new Task("Test Task", "Description");
        int taskId = manager.createTask(task);

        assertTrue(manager.getTask(taskId).isPresent(), "Задача должна быть найдена");
        assertEquals("Test Task", manager.getTask(taskId).get().getTitle(), "Название задачи должно совпадать");
    }

    @Test
    void testCreateAndGetEpic() {
        Epic epic = new Epic("Test Epic", "Description");
        int epicId = manager.createEpic(epic);

        assertTrue(manager.getEpic(epicId).isPresent(), "Эпик должен быть найден");
        assertEquals("Test Epic", manager.getEpic(epicId).get().getTitle(), "Название эпика должно совпадать");
    }

    @Test
    void testCreateAndGetSubtask() {
        Epic epic = new Epic("Test Epic", "Description");
        int epicId = manager.createEpic(epic);

        Subtask subtask = new Subtask("Test Subtask", "Description", epicId);
        int subtaskId = manager.createSubtask(subtask);

        assertTrue(manager.getSubtask(subtaskId).isPresent(), "Подзадача должна быть найдена");
        assertEquals("Test Subtask", manager.getSubtask(subtaskId).get().getTitle(), "Название подзадачи должно совпадать");
        assertEquals(epicId, manager.getSubtask(subtaskId).get().getEpicId(), "ID эпика должен совпадать");
    }

    @Test
    void testGetAllTasks() {
        Task task1 = new Task("Task 1", "Description");
        Task task2 = new Task("Task 2", "Description");
        manager.createTask(task1);
        manager.createTask(task2);

        assertEquals(2, manager.getAllTasks().size(), "Должно быть 2 задачи");
    }

    @Test
    void testGetAllEpics() {
        Epic epic1 = new Epic("Epic 1", "Description");
        Epic epic2 = new Epic("Epic 2", "Description");
        manager.createEpic(epic1);
        manager.createEpic(epic2);

        assertEquals(2, manager.getAllEpics().size(), "Должно быть 2 эпика");
    }

    @Test
    void testGetAllSubtasks() {
        Epic epic = new Epic("Epic", "Description");
        int epicId = manager.createEpic(epic);

        Subtask subtask1 = new Subtask("Subtask 1", "Description", epicId);
        Subtask subtask2 = new Subtask("Subtask 2", "Description", epicId);
        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);

        assertEquals(2, manager.getAllSubtasks().size(), "Должно быть 2 подзадачи");
    }

    @Test
    void testUpdateTask() {
        Task task = new Task("Original", "Description");
        int taskId = manager.createTask(task);

        Task updated = new Task(taskId, "Updated", "New Description", Status.IN_PROGRESS);
        manager.updateTask(updated);

        Task saved = manager.getTask(taskId).get();
        assertEquals("Updated", saved.getTitle(), "Название должно обновиться");
        assertEquals(Status.IN_PROGRESS, saved.getStatus(), "Статус должен обновиться");
    }

    @Test
    void testDeleteTask() {
        Task task = new Task("Test", "Description");
        int taskId = manager.createTask(task);

        manager.deleteTask(taskId);

        assertTrue(manager.getTask(taskId).isEmpty(), "Задача должна быть удалена");
    }

    @Test
    void testDeleteAllTasks() {
        Task task1 = new Task("Task 1", "Description");
        Task task2 = new Task("Task 2", "Description");
        manager.createTask(task1);
        manager.createTask(task2);

        manager.deleteAllTasks();

        assertTrue(manager.getAllTasks().isEmpty(), "Все задачи должны быть удалены");
    }

    @Test
    void testGetEpicSubtasks() {
        Epic epic = new Epic("Epic", "Description");
        int epicId = manager.createEpic(epic);

        Subtask subtask1 = new Subtask("Subtask 1", "Description", epicId);
        Subtask subtask2 = new Subtask("Subtask 2", "Description", epicId);
        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);

        List<Subtask> epicSubtasks = manager.getEpicSubtasks(epicId);
        assertEquals(2, epicSubtasks.size(), "Должно быть 2 подзадачи у эпика");
    }

    @Test
    void testHistoryManagement() {
        Task task = new Task("Test", "Description");
        int taskId = manager.createTask(task);

        // Добавляем в историю
        manager.getTask(taskId);

        assertEquals(1, manager.getHistory().size(), "История должна содержать 1 задачу");
        assertEquals(taskId, manager.getHistory().get(0).getId(), "ID задачи в истории должен совпадать");
    }

    @Test
    void testPrioritizedTasks() {
        LocalDateTime baseTime = LocalDateTime.of(2024, 1, 1, 10, 0);

        Task task1 = new Task("Task 1", "Description");
        task1.setStartTime(baseTime);
        task1.setDuration(Duration.ofMinutes(60));
        manager.createTask(task1);

        Task task2 = new Task("Task 2", "Description");
        task2.setStartTime(baseTime.plusMinutes(90)); // Start after task1 ends
        task2.setDuration(Duration.ofMinutes(60));
        manager.createTask(task2);

        List<Task> prioritized = manager.getPrioritizedTasks();
        assertEquals(2, prioritized.size(), "Должно быть 2 задачи в приоритетном списке");
        assertEquals("Task 1", prioritized.get(0).getTitle(), "Первой должна быть задача с более ранним временем");
    }
}