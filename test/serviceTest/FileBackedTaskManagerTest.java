package serviceTest;

import objects.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import service.FileBackedTaskManager;
import service.ManagerSaveException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {
    @TempDir
    Path tempDir;
    private File testFile;
    private FileBackedTaskManager manager;

    private int taskId;
    private int epicId;
    private int subtaskId;


    @Test
    void testSaveAndLoadEmptyFile() {
        // Удаляем все задачи
        manager.deleteTask(taskId);
        manager.deleteSubtask(subtaskId);
        manager.deleteEpic(epicId);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(testFile);

        assertAll(
                () -> assertTrue(loadedManager.getAllTasks().isEmpty()),
                () -> assertTrue(loadedManager.getAllEpics().isEmpty()),
                () -> assertTrue(loadedManager.getAllSubtasks().isEmpty())
        );
    }


    @Test
    void testSaveAndLoadTasks() {
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(testFile);

        assertAll(
                () -> assertEquals(1, loadedManager.getAllTasks().size()),
                () -> assertEquals(1, loadedManager.getAllEpics().size()),
                () -> assertEquals(1, loadedManager.getAllSubtasks().size())
        );
    }

    @Test
    void testSaveAndLoadWithStatusChanges() {
        Subtask updatedSubtask = new Subtask(subtaskId, "Test Subtask", "Subtask Description", Status.DONE, epicId);
        manager.updateSubtask(updatedSubtask);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(testFile);

        assertAll(
                // Use orElseThrow on the Optional to get the object before calling getStatus()
                () -> assertEquals(Status.DONE, loadedManager.getSubtask(subtaskId).orElseThrow().getStatus()),
                () -> assertEquals(Status.DONE, loadedManager.getEpic(epicId).orElseThrow().getStatus())
        );
    }


    @Test
    void testSaveAndLoadAfterDeletion() {
        // Удаляем задачу и подзадачу
        manager.deleteTask(taskId);
        manager.deleteSubtask(subtaskId);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(testFile);

        assertAll(
                () -> assertTrue(loadedManager.getTask(taskId).isEmpty(), "Задача не удалена"),
                () -> assertTrue(loadedManager.getSubtask(subtaskId).isEmpty(), "Подзадачи не удалены"),
                () -> assertTrue(loadedManager.getEpic(epicId).isPresent(), "Эпик не найден"),
                () -> assertTrue(loadedManager.getEpic(epicId).orElseThrow().getSubtaskIds().isEmpty()),
                () -> assertEquals(Status.NEW, loadedManager.getEpic(epicId).orElseThrow().getStatus())
        );
    }

    @Test
    void testFileFormat() throws IOException {
        String content = Files.readString(testFile.toPath());
        String[] lines = content.split("\n");

        // ОБНОВЛЕННЫЙ заголовок - теперь с полями startTime и duration
        assertEquals("id,type,name,status,description,epic,startTime,duration", lines[0].trim());

        // Проверяем, что есть как минимум 4 строки (заголовок + 3 задачи)
        assertTrue(lines.length >= 4);

        // Проверяем формат каждой строки с данными
        for (int i = 1; i < lines.length; i++) {
            if (lines[i].trim().isEmpty()) continue;

            // ОБНОВЛЕНО: теперь 8 полей вместо 6
            String[] fields = lines[i].split(",", 8);
            assertEquals(8, fields.length, "Строка должна содержать 8 полей: " + lines[i]);

            // Проверяем, что id - число
            assertDoesNotThrow(() -> Integer.parseInt(fields[0].trim()));

            // Проверяем типы задач
            assertTrue(fields[1].equals("TASK") || fields[1].equals("EPIC") || fields[1].equals("SUBTASK"),
                    "Неверный тип задачи: " + fields[1]);

            // Проверяем статусы
            assertTrue(fields[3].equals("NEW") || fields[3].equals("IN_PROGRESS") || fields[3].equals("DONE"),
                    "Неверный статус: " + fields[3]);
        }
    }

    @Test
    void testLoadFromNonExistentFile() {
        File nonExistentFile = new File(tempDir.toFile(), "nonexistent.csv");

        assertDoesNotThrow(() -> {
            FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(nonExistentFile);
            assertNotNull(loadedManager);
        });
    }


    @Test
    void testSaveAndLoadTaskContent() {
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(testFile);

        // Use orElseThrow to get the value or fail the test if not found
        Task loadedTask = loadedManager.getTask(taskId).orElseThrow();
        assertAll(
                () -> assertNotNull(loadedTask),
                () -> assertEquals("Test Task", loadedTask.getTitle()),
                () -> assertEquals("Task Description", loadedTask.getDescription()),
                () -> assertEquals(Status.NEW, loadedTask.getStatus())
        );
    }

    @Test
    void testTaskRelationshipsPreserved() {
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(testFile);

        // Use orElseThrow to get the actual Subtask and Epic objects
        Subtask loadedSubtask = loadedManager.getSubtask(subtaskId).orElseThrow();
        Epic loadedEpic = loadedManager.getEpic(epicId).orElseThrow();

        assertAll(
                () -> assertEquals(epicId, loadedSubtask.getEpicId()),
                () -> assertTrue(loadedEpic.getSubtaskIds().contains(subtaskId))
        );
    }

    @Test
    void testUpdateTask() {
        // Обновляем задачу
        Task updatedTask = new Task(taskId, "Updated Task", "Updated Description", Status.IN_PROGRESS);
        manager.updateTask(updatedTask);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(testFile);
        Task loadedTask = loadedManager.getTask(taskId).orElseThrow();

        assertAll(
                () -> assertEquals("Updated Task", loadedTask.getTitle()),
                () -> assertEquals("Updated Description", loadedTask.getDescription()),
                () -> assertEquals(Status.IN_PROGRESS, loadedTask.getStatus())
        );
    }

    @BeforeEach
    void setUp() throws IOException {
        testFile = Files.createTempFile(tempDir, "test", ".csv").toFile();
        manager = new FileBackedTaskManager(testFile);

        // Create one task of each type
        Task task = new Task("Test Task", "Task Description");
        taskId = manager.createTask(task);

        Epic epic = new Epic("Test Epic", "Epic Description");
        epicId = manager.createEpic(epic);

        Subtask subtask = new Subtask("Test Subtask", "Subtask Description", epicId);
        subtaskId = manager.createSubtask(subtask);
    }


    @Test
    void testSaveWithIOException() {
        // Создаем директорию вместо файла, чтобы вызвать ошибку записи
        File directory = new File(tempDir.toFile(), "directory");
        directory.mkdir();

        FileBackedTaskManager failingManager = new FileBackedTaskManager(directory);

        assertThrows(ManagerSaveException.class, () -> {
            failingManager.createTask(new Task("Test", "Description"));
        });
    }
}