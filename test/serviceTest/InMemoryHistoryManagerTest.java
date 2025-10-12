import service.HistoryManager;
import service.InMemoryHistoryManager;
import objects.Task; // ← ДОБАВЬТЕ ЭТОТ ИМПОРТ
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
        assertTrue(history.isEmpty());
    }

    @Test
    void testDuplicateTasks() {
        Task task = new Task("Test Task", "Description");
        task.setId(1);

        historyManager.add(task);
        historyManager.add(task); // Дубликат

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size());
    }

    @Test
    void testRemoveFromHistory() {
        Task task1 = new Task("Task 1", "Description 1");
        task1.setId(1);
        Task task2 = new Task("Task 2", "Description 2");
        task2.setId(2);
        Task task3 = new Task("Task 3", "Description 3");
        task3.setId(3);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        // Удаление из начала
        historyManager.remove(1);
        assertEquals(2, historyManager.getHistory().size());
        assertEquals(2, historyManager.getHistory().get(0).getId());

        // Удаление из конца
        historyManager.remove(3);
        assertEquals(1, historyManager.getHistory().size());
        assertEquals(2, historyManager.getHistory().get(0).getId());

        // Удаление из середины
        Task task4 = new Task("Task 4", "Description 4");
        task4.setId(4);
        historyManager.add(task1);
        historyManager.add(task4);

        historyManager.remove(2);
        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(1, history.get(0).getId());
        assertEquals(4, history.get(1).getId());
    }
}