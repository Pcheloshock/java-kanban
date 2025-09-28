package serviceTest;

import objects.*;
import org.junit.jupiter.api.Assertions;
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

    @BeforeEach
    void setUp() throws IOException {
        testFile = Files.createTempFile(tempDir, "test", ".csv").toFile();
        manager = new FileBackedTaskManager(testFile);
    }

    @Test
    void testSaveAndLoadEmptyFile() {
        // Сохраняем пустой менеджер
        manager.save();

        // Загружаем из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(testFile);

        // Проверяем, что все списки пустые
        assertTrue(loadedManager.getAllTasks().isEmpty(), "Список задач должен быть пустым");
        assertTrue(loadedManager.getAllEpics().isEmpty(), "Список эпиков должен быть пустым");
        assertTrue(loadedManager.getAllSubtasks().isEmpty(), "Список подзадач должен быть пустым");
        assertTrue(loadedManager.getHistory().isEmpty(), "История должна быть пустой");
    }

    @Test
    void testSaveAndLoadMultipleTasks() {
        // Создаем задачи
        Task task1 = new Task("Task 1", "Description 1");
        int taskId1 = manager.createTask(task1);

        Task task2 = new Task("Task 2", "Description 2");
        int taskId2 = manager.createTask(task2);

        Epic epic1 = new Epic("Epic 1", "Epic Description 1");
        int epicId1 = manager.createEpic(epic1);

        Subtask subtask1 = new Subtask("Subtask 1", "Subtask Description 1", epicId1);
        int subtaskId1 = manager.createSubtask(subtask1);

        Subtask subtask2 = new Subtask("Subtask 2", "Subtask Description 2", epicId1);
        int subtaskId2 = manager.createSubtask(subtask2);

        Epic epic2 = new Epic("Epic 2", "Epic Description 2");
        int epicId2 = manager.createEpic(epic2);

        // Загружаем из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(testFile);

        // Проверяем задачи
        List<Task> loadedTasks = loadedManager.getAllTasks();
        assertEquals(2, loadedTasks.size(), "Должно быть 2 задачи");

        List<Epic> loadedEpics = loadedManager.getAllEpics();
        assertEquals(2, loadedEpics.size(), "Должно быть 2 эпика");

        List<Subtask> loadedSubtasks = loadedManager.getAllSubtasks();
        assertEquals(2, loadedSubtasks.size(), "Должно быть 2 подзадачи");

        // Проверяем содержимое задач
        Task loadedTask1 = loadedManager.getTask(taskId1);
        assertNotNull(loadedTask1, "Задача 1 должна существовать");
        assertEquals("Task 1", loadedTask1.getTitle());
        assertEquals("Description 1", loadedTask1.getDescription());
        assertEquals(Status.NEW, loadedTask1.getStatus());

        // Проверяем эпики и их подзадачи
        Epic loadedEpic1 = loadedManager.getEpic(epicId1);
        assertNotNull(loadedEpic1, "Эпик 1 должен существовать");
        assertEquals(2, loadedEpic1.getSubtaskIds().size(), "У эпика 1 должно быть 2 подзадачи");

        List<Subtask> epicSubtasks = loadedManager.getEpicSubtasks(epicId1);
        assertEquals(2, epicSubtasks.size(), "Должно быть 2 подзадачи у эпика 1");
    }

    @Test
    void testSaveAndLoadWithStatusChanges() {
        // Создаем эпик и подзадачи
        Epic epic = new Epic("Epic", "Epic Description");
        int epicId = manager.createEpic(epic);

        Subtask subtask1 = new Subtask("Subtask 1", "Description 1", epicId);
        int subtaskId1 = manager.createSubtask(subtask1);

        Subtask subtask2 = new Subtask("Subtask 2", "Description 2", epicId);
        int subtaskId2 = manager.createSubtask(subtask2);

        // Обновляем статусы подзадач
        Subtask updatedSubtask1 = new Subtask(subtaskId1, "Subtask 1", "Description 1", Status.DONE, epicId);
        manager.updateSubtask(updatedSubtask1);

        Subtask updatedSubtask2 = new Subtask(subtaskId2, "Subtask 2", "Description 2", Status.IN_PROGRESS, epicId);
        manager.updateSubtask(updatedSubtask2);

        // Загружаем из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(testFile);

        // Проверяем статусы
        Subtask loadedSubtask1 = loadedManager.getSubtask(subtaskId1);
        assertEquals(Status.DONE, loadedSubtask1.getStatus(), "Подзадача 1 должна быть DONE");

        Subtask loadedSubtask2 = loadedManager.getSubtask(subtaskId2);
        assertEquals(Status.IN_PROGRESS, loadedSubtask2.getStatus(), "Подзадача 2 должна быть IN_PROGRESS");

        // Проверяем статус эпика
        Epic loadedEpic = loadedManager.getEpic(epicId);
        assertEquals(Status.IN_PROGRESS, loadedEpic.getStatus(), "Эпик должен быть IN_PROGRESS");
    }

    @Test
    void testSaveAndLoadAfterDeletion() {
        // Создаем задачи
        Task task1 = new Task("Task 1", "Description 1");
        int taskId1 = manager.createTask(task1);

        Task task2 = new Task("Task 2", "Description 2");
        int taskId2 = manager.createTask(task2);

        Epic epic = new Epic("Epic", "Epic Description");
        int epicId = manager.createEpic(epic);

        Subtask subtask = new Subtask("Subtask", "Subtask Description", epicId);
        int subtaskId = manager.createSubtask(subtask);

        // Удаляем некоторые задачи
        manager.deleteTask(taskId1);
        manager.deleteSubtask(subtaskId);

        // Загружаем из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(testFile);

        // Проверяем, что удаленные задачи отсутствуют
        assertNull(loadedManager.getTask(taskId1), "Задача 1 должна быть удалена");
        assertNull(loadedManager.getSubtask(subtaskId), "Подзадача должна быть удалена");

        // Проверяем, что оставшиеся задачи существуют
        assertNotNull(loadedManager.getTask(taskId2), "Задача 2 должна существовать");
        assertNotNull(loadedManager.getEpic(epicId), "Эпик должен существовать");

        // Проверяем, что у эпика нет подзадач
        Epic loadedEpic = loadedManager.getEpic(epicId);
        assertTrue(loadedEpic.getSubtaskIds().isEmpty(), "У эпика не должно быть подзадач");
        assertEquals(Status.NEW, loadedEpic.getStatus(), "Эпик должен быть NEW");
    }

    @Test
    void testSaveAndLoadWithEmptyEpic() {
        // Создаем эпик без подзадач
        Epic epic = new Epic("Empty Epic", "Epic without subtasks");
        int epicId = manager.createEpic(epic);

        // Загружаем из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(testFile);

        Epic loadedEpic = loadedManager.getEpic(epicId);
        assertNotNull(loadedEpic, "Эпик должен существовать");
        assertEquals("Empty Epic", loadedEpic.getTitle());
        assertEquals(Status.NEW, loadedEpic.getStatus(), "Пустой эпик должен быть NEW");
        assertTrue(loadedEpic.getSubtaskIds().isEmpty(), "У эпика не должно быть подзадач");
    }

    @Test
    void testFileFormat() throws IOException {
        // Создаем задачи разных типов
        Task task = new Task("Simple Task", "Task Description");
        manager.createTask(task);

        Epic epic = new Epic("Test Epic", "Epic Description");
        int epicId = manager.createEpic(epic);

        Subtask subtask = new Subtask("Test Subtask", "Subtask Description", epicId);
        manager.createSubtask(subtask);

        // Читаем содержимое файла
        String content = Files.readString(testFile.toPath());
        String[] lines = content.split("\n");

        // Проверяем заголовок
        assertEquals("id,type,name,status,description,epic", lines[0], "Неправильный заголовок");

        // Проверяем формат строк
        for (int i = 1; i < lines.length; i++) {
            String[] fields = lines[i].split(",", -1);
            assertEquals(6, fields.length, "Каждая строка должна содержать 6 полей");

            // Проверяем, что id - число
            assertDoesNotThrow(() -> Integer.parseInt(fields[0]), "ID должен быть числом");

            // Проверяем, что тип задачи корректен
            assertTrue(fields[1].equals("TASK") || fields[1].equals("EPIC") || fields[1].equals("SUBTASK"),
                    "Тип задачи должен быть TASK, EPIC или SUBTASK");

            // Проверяем, что статус корректен
            assertTrue(fields[3].equals("NEW") || fields[3].equals("IN_PROGRESS") || fields[3].equals("DONE"),
                    "Статус должен быть NEW, IN_PROGRESS или DONE");
        }
    }

    @Test
    void testHistoryNotSaved() {
        // Создаем задачи и добавляем их в историю
        Task task = new Task("Task", "Description");
        int taskId = manager.createTask(task);

        Epic epic = new Epic("Epic", "Description");
        int epicId = manager.createEpic(epic);

        // Просматриваем задачи (добавляем в историю)
        manager.getTask(taskId);
        manager.getEpic(epicId);

        // Загружаем из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(testFile);

        // История не должна сохраняться в файл (по условию задачи)
        assertTrue(loadedManager.getHistory().isEmpty(), "История не должна сохраняться в файл");
    }


    @Test
    void testSaveWithIOException() {
        File readOnlyFile = new File("read_only.csv");
        try {
            // Создаем файл только для чтения (если возможно)
            if (readOnlyFile.createNewFile()) {
                readOnlyFile.setReadOnly();

                FileBackedTaskManager readOnlyManager = new FileBackedTaskManager(readOnlyFile);

                assertThrows(ManagerSaveException.class, () -> {
                    readOnlyManager.createTask(new Task("Test", "Test"));
                }, "Должно выбрасываться ManagerSaveException при ошибке записи");
            }
        } catch (IOException e) {
            // Если не удалось создать read-only файл, пропускаем тест
            System.out.println("Не удалось создать read-only файл для теста");
        } finally {
            readOnlyFile.delete();
        }
    }
}