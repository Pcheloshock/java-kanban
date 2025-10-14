package serviceTest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import service.InMemoryTaskManager;
import static org.junit.Assert.*;
import objects.Task;

public class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {
    @Override
    protected InMemoryTaskManager createManager() {
        return new InMemoryTaskManager();
    }

    // Дополнительные специфичные тесты для InMemoryTaskManager
    @Test
    void testInMemoryTaskManagerSpecificBehavior() {
        Task task = new Task("Test", "Description");
        int taskId = manager.createTask(task);

        // After creation, the task SHOULD exist (should NOT be empty)
        Assertions.assertTrue(manager.getTask(taskId).isPresent(), "Задача должна существовать после создания");

        // Optional: Verify the task details are correct
        Task retrievedTask = manager.getTask(taskId).get();
        Assertions.assertEquals("Test", retrievedTask.getTitle(), "Название задачи должно совпадать");
        Assertions.assertEquals("Description", retrievedTask.getDescription(), "Описание задачи должно совпадать");
    }
}

