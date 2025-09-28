package service;

import objects.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;

    public FileBackedTaskManager(File file) {
        this.file = file;
        // Создаем файл, если он не существует
        try {
            if (!file.exists()) {
                Files.createFile(file.toPath());
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Не удалось создать файл", e);
        }
    }

    // Сохранение текущего состояния в файл
    public void save() {
        try (Writer writer = new FileWriter(file, StandardCharsets.UTF_8)) {
            writer.write("id,type,name,status,description,epic\n");

            for (Task task : getAllTasks()) {
                writer.write(toString(task) + "\n");
            }
            for (Epic epic : getAllEpics()) {
                writer.write(toString(epic) + "\n");
            }
            for (Subtask subtask : getAllSubtasks()) {
                writer.write(toString(subtask) + "\n");
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка сохранения в файл", e);
        }
    }

    // Преобразование задачи в CSV строку
    private String toString(Task task) {
        String[] fields = {
                String.valueOf(task.getId()),
                task.getType().toString(),
                task.getTitle(),
                task.getStatus().toString(),
                task.getDescription(),
                task instanceof Subtask ? String.valueOf(((Subtask) task).getEpicId()) : ""
        };
        return String.join(",", fields);
    }

    // Создание задачи из CSV строки
    private static Task fromString(String value) {
        String[] fields = value.split(",", -1); // -1 чтобы сохранить пустые поля
        int id = Integer.parseInt(fields[0]);
        TaskType type = TaskType.valueOf(fields[1]);
        String name = fields[2];
        Status status = Status.valueOf(fields[3]);
        String description = fields[4];
        String epicId = fields.length > 5 ? fields[5] : "";

        switch (type) {
            case TASK:
                Task task = new Task(name, description);
                task.setId(id);
                task.setStatus(status);
                return task;
            case EPIC:
                Epic epic = new Epic(name, description);
                epic.setId(id);
                epic.setStatus(status);
                return epic;
            case SUBTASK:
                Subtask subtask = new Subtask(name, description, Integer.parseInt(epicId));
                subtask.setId(id);
                subtask.setStatus(status);
                return subtask;
            default:
                throw new IllegalArgumentException("Неизвестный тип задачи: " + type);
        }
    }

    // Восстановление состояния из файла
    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        if (!file.exists())
            throw new ManagerSaveException("Файл не существует: " + file.getAbsolutePath());
        if (!file.exists())
            throw new ManagerSaveException("Файл был удален во время загрузки: " + file.getAbsolutePath());

        try {
            String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);

            if (content.trim().isEmpty()) {
                return manager;
            }

            String[] lines = content.split("\n");

            // Skip header and process tasks
            for (int i = 1; i < lines.length; i++) {
                if (lines[i].isBlank()) continue;

                Task task = fromString(lines[i]);
                switch (task.getType()) {
                    case TASK:
                        manager.tasks.put(task.getId(), task);
                        break;
                    case EPIC:
                        manager.epics.put(task.getId(), (Epic) task);
                        break;
                    case SUBTASK:
                        Subtask subtask = (Subtask) task;
                        manager.subtasks.put(subtask.getId(), subtask);
                        Epic epic = manager.epics.get(subtask.getEpicId());
                        if (epic != null) {
                            epic.addSubtaskId(subtask.getId());
                        }
                        break;
                }

                if (task.getId() >= manager.nextId) {
                    manager.nextId = task.getId() + 1;
                }
            }

            // Restore epic statuses
            for (Epic epic : manager.epics.values()) {
                manager.updateEpicStatus(epic.getId());
            }

        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка загрузки из файла", e);
        }

        return manager;
    }

    // Переопределенные методы с вызовом сохранения
    @Override
    public int createTask(Task task) {
        int id = super.createTask(task);
        save();
        return id;
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void deleteTask(int id) {
        super.deleteTask(id);
        save();
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    @Override
    public int createEpic(Epic epic) {
        int id = super.createEpic(epic);
        save();
        return id;
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void deleteEpic(int id) {
        super.deleteEpic(id);
        save();
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }

    @Override
    public int createSubtask(Subtask subtask) {
        int id = super.createSubtask(subtask);
        save();
        return id;
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void deleteSubtask(int id) {
        super.deleteSubtask(id);
        save();
    }

    @Override
    public void deleteAllSubtasks() {
        super.deleteAllSubtasks();
        save();
    }
}