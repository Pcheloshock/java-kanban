package serviceTest;

import org.junit.jupiter.api.io.TempDir;
import service.FileBackedTaskManager;
import service.ManagerSaveException;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Test;
import objects.Task;      // Adjust based on your package
import objects.Epic;
import objects.Subtask;
import objects.Status;


import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {
    @TempDir
    Path tempDir;
    private File testFile;

    @Override
    protected FileBackedTaskManager createManager() {
        try {
            testFile = Files.createTempFile(tempDir, "test", ".csv").toFile();
            return new FileBackedTaskManager(testFile);
        } catch (Exception e) {
            throw new RuntimeException("Не удалось создать временный файл", e);
        }
    }

    // Тесты на обработку исключений при работе с файлами
    @Test
    void testSaveWithInvalidFile() {
        File invalidFile = new File("/invalid/path/tasks.csv");
        FileBackedTaskManager invalidManager = new FileBackedTaskManager(invalidFile);

        Task task = new Task("Test", "Description");

        assertThrows(ManagerSaveException.class, () -> invalidManager.createTask(task),
                "Должно быть исключение при сохранении в невалидный файл");
    }

    @Test
    void testLoadFromInvalidFile() {
        File invalidFile = new File("/invalid/path/tasks.csv");

        assertThrows(ManagerSaveException.class, () -> FileBackedTaskManager.loadFromFile(invalidFile),
                "Должно быть исключение при загрузке из несуществующего файла");
    }

    @Test
    void testLoadFromEmptyFile() {
        assertDoesNotThrow(() -> {
            FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(testFile);
            assertNotNull(loadedManager, "Менеджер должен быть создан даже из пустого файла");
            assertTrue(loadedManager.getAllTasks().isEmpty(), "Список задач должен быть пустым");
        }, "Не должно быть исключения при загрузке из пустого файла");
    }

    @Test
    void testLoadFromFileWithOnlyEpic() {
        // Создаем эпик без подзадач
        Epic epic = new Epic("Test Epic", "Description");
        manager.createEpic(epic);

        // Загружаем из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(testFile);

        assertEquals(1, loadedManager.getAllEpics().size(), "Должен быть загружен 1 эпик");
        assertEquals(0, loadedManager.getAllSubtasks().size(), "Не должно быть подзадач");
        assertEquals(Status.NEW, loadedManager.getAllEpics().get(0).getStatus(),
                "Статус эпика без подзадач должен быть NEW");
    }

    @Test
    void testFilePersistenceAfterMultipleOperations() {
        // Создаем задачи
        Task task = new Task("Task", "Description");
        int taskId = manager.createTask(task);

        Epic epic = new Epic("Epic", "Description");
        int epicId = manager.createEpic(epic);

        Subtask subtask = new Subtask("Subtask", "Description", epicId);
        int subtaskId = manager.createSubtask(subtask);

        // Обновляем статус
        Subtask updatedSubtask = new Subtask(subtaskId, "Subtask", "Description", Status.DONE, epicId);
        manager.updateSubtask(updatedSubtask);

        // Удаляем задачу
        manager.deleteTask(taskId);

        // Загружаем из файла и проверяем
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(testFile);

        assertAll(
                () -> assertTrue(loadedManager.getTask(taskId).isEmpty(), "Задача должна быть удалена"),
                () -> assertEquals(Status.DONE, loadedManager.getEpic(epicId).get().getStatus(),
                        "Статус эпика должен быть DONE"),
                () -> assertEquals(1, loadedManager.getAllSubtasks().size(), "Должна быть 1 подзадача")
        );
    }
}