package serviceTest;

import objects.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.HistoryManager;
import service.InMemoryHistoryManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryHistoryManagerTest {
    private HistoryManager historyManager;

    @BeforeEach
    void setUp() {
        historyManager = new InMemoryHistoryManager();
    }

    @Test
    void testAddTaskToHistory() {
        Task task = new Task("Test", "Description");
        task.setId(1);

        historyManager.add(task);
        List<Task> history = historyManager.getHistory();

        assertEquals(1, history.size(), "Задача не добавлена в историю");
        assertEquals(task, history.get(0), "Задача в истории не совпадает с добавленной");
    }

    @Test
    void testRemoveTaskFromHistory() {
        Task task = new Task("Test", "Description");
        task.setId(1);

        historyManager.add(task);
        historyManager.remove(1);
        List<Task> history = historyManager.getHistory();

        assertTrue(history.isEmpty(), "Задача не удалена из истории");
    }

    @Test
    void testHistoryOrder() {
        Task task1 = new Task("Task1", "Description");
        task1.setId(1);
        Task task2 = new Task("Task2", "Description");
        task2.setId(2);

        historyManager.add(task1);
        historyManager.add(task2);
        List<Task> history = historyManager.getHistory();

        assertEquals(2, history.size(), "Неверное количество задач в истории");
        assertEquals(1, history.get(0).getId(), "Неверный порядок задач в истории");
        assertEquals(2, history.get(1).getId(), "Неверный порядок задач в истории");
    }

    @Test
    void testRemoveDuplicates() {
        Task task = new Task("Test", "Description");
        task.setId(1);

        // Добавляем одну и ту же задачу несколько раз
        for (int i = 0; i < 5; i++) {
            historyManager.add(task);
        }

        List<Task> history = historyManager.getHistory();

        assertEquals(1, history.size(), "В истории остались дубликаты");
        assertEquals(1, history.get(0).getId(), "Неверная задача в истории");
    }

    @Test
    void testHistoryUnlimitedSize() {
        // Добавляем больше задач, чем было в старом ограничении
        for (int i = 1; i <= 15; i++) {
            Task task = new Task("Task" + i, "Description");
            task.setId(i);
            historyManager.add(task);
        }

        List<Task> history = historyManager.getHistory();

        assertEquals(15, history.size(), "История имеет неверный размер");
        assertEquals(15, history.get(14).getId(), "Неверная задача в конце истории");
    }
}

