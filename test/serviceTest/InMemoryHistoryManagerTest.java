package serviceTest;

import objects.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.HistoryManager;
import service.InMemoryHistoryManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {
    private HistoryManager historyManager;

    @BeforeEach
    void setUp() {
        historyManager = new InMemoryHistoryManager();
    }

    @Test
    void testEmptyHistory() {
        List<Task> history = historyManager.getHistory();
        assertTrue(history.isEmpty(), "История должна быть пустой");
    }

    @Test
    void testAddDuplicateTasks() {
        Task task = new Task("Test Task", "Description");
        task.setId(1);

        historyManager.add(task);
        historyManager.add(task); // Дубликат
        historyManager.add(task); // Еще один дубликат

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(), "История должна содержать только одну задачу при дублировании");
        assertEquals(1, history.get(0).getId(), "ID задачи должен быть 1");
    }

    @Test
    void testRemoveFromBeginning() {
        Task task1 = new Task("Task 1", "Description");
        task1.setId(1);
        Task task2 = new Task("Task 2", "Description");
        task2.setId(2);
        Task task3 = new Task("Task 3", "Description");
        task3.setId(3);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        // Удаляем из начала
        historyManager.remove(1);

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size(), "Должно остаться 2 задачи");
        assertEquals(2, history.get(0).getId(), "Первой должна быть задача 2");
        assertEquals(3, history.get(1).getId(), "Второй должна быть задача 3");
    }

    @Test
    void testRemoveFromMiddle() {
        Task task1 = new Task("Task 1", "Description");
        task1.setId(1);
        Task task2 = new Task("Task 2", "Description");
        task2.setId(2);
        Task task3 = new Task("Task 3", "Description");
        task3.setId(3);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        // Удаляем из середины
        historyManager.remove(2);

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size(), "Должно остаться 2 задачи");
        assertEquals(1, history.get(0).getId(), "Первой должна быть задача 1");
        assertEquals(3, history.get(1).getId(), "Второй должна быть задача 3");
    }

    @Test
    void testRemoveFromEnd() {
        Task task1 = new Task("Task 1", "Description");
        task1.setId(1);
        Task task2 = new Task("Task 2", "Description");
        task2.setId(2);
        Task task3 = new Task("Task 3", "Description");
        task3.setId(3);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        // Удаляем из конца
        historyManager.remove(3);

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size(), "Должно остаться 2 задачи");
        assertEquals(1, history.get(0).getId(), "Первой должна быть задача 1");
        assertEquals(2, history.get(1).getId(), "Второй должна быть задача 2");
    }

    @Test
    void testRemoveNonExistentTask() {
        Task task = new Task("Task", "Description");
        task.setId(1);
        historyManager.add(task);

        // Удаляем несуществующую задачу
        historyManager.remove(999);

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(), "История не должна измениться при удалении несуществующей задачи");
    }

    @Test
    void testHistoryOrderPreservation() {
        Task task1 = new Task("Task 1", "Description");
        task1.setId(1);
        Task task2 = new Task("Task 2", "Description");
        task2.setId(2);
        Task task3 = new Task("Task 3", "Description");
        task3.setId(3);

        // Добавляем в разном порядке
        historyManager.add(task1);
        historyManager.add(task3);
        historyManager.add(task2);
        historyManager.add(task1); // Дубликат - должен переместиться в конец

        List<Task> history = historyManager.getHistory();
        assertEquals(3, history.size(), "Должно быть 3 уникальных задачи");
        assertEquals(3, history.get(0).getId(), "Первой должна быть задача 3");
        assertEquals(2, history.get(1).getId(), "Второй должна быть задача 2");
        assertEquals(1, history.get(2).getId(), "Третьей должна быть задача 1 (последний просмотр)");
    }

    @Test
    void testAddNullTask() {
        assertDoesNotThrow(() -> historyManager.add(null),
                "Добавление null не должно вызывать исключение");

        List<Task> history = historyManager.getHistory();
        assertTrue(history.isEmpty(), "История должна остаться пустой после добавления null");
    }
}