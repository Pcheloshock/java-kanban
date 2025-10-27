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

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {
    @TempDir
    Path tempDir;
    private File testFile;
    private FileBackedTaskManager manager;

    @BeforeEach
    void setUp() throws IOException {
        testFile = Files.createTempFile(tempDir, "test", ".csv").toFile();
        manager = new FileBackedTaskManager(testFile);
    }

    @Test
    void testSaveAndLoadEmptyFile() {
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(testFile);

        assertAll(
                () -> assertTrue(loadedManager.getAllTasks().isEmpty()),
                () -> assertTrue(loadedManager.getAllEpics().isEmpty()),
                () -> assertTrue(loadedManager.getAllSubtasks().isEmpty())
        );
    }

    @Test
    void testSaveAndLoadTasks() {
        Task task = new Task("Test Task", "Task Description");
        manager.createTask(task);

        Epic epic = new Epic("Test Epic", "Epic Description");
        int epicId = manager.createEpic(epic);

        Subtask subtask = new Subtask("Test Subtask", "Subtask Description", epicId);
        manager.createSubtask(subtask);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(testFile);

        assertAll(
                () -> assertEquals(1, loadedManager.getAllTasks().size()),
                () -> assertEquals(1, loadedManager.getAllEpics().size()),
                () -> assertEquals(1, loadedManager.getAllSubtasks().size())
        );
    }

    @Test
    void testSaveWithIOException() {
        File directory = new File(tempDir.toFile(), "directory");
        directory.mkdir();

        FileBackedTaskManager failingManager = new FileBackedTaskManager(directory);

        assertThrows(ManagerSaveException.class, () -> {
            failingManager.createTask(new Task("Test", "Description"));
        });
    }
}