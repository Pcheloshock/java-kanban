package service;

import objects.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;

    public FileBackedTaskManager(File file) {
        this.file = file;
        try {
            if (!file.exists()) {
                Files.createFile(file.toPath());
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Не удалось создать файл", e);
        }
    }

    private void save() {
        List<String> lines = new ArrayList<>();
        lines.add(TaskCSVConverter.getHeader());

        // Сохраняем задачи
        for (Task task : getAllTasks()) {
            lines.add(TaskCSVConverter.toString(task));
        }

        // Сохраняем эпики
        for (Epic epic : getAllEpics()) {
            lines.add(TaskCSVConverter.toString(epic));
        }

        // Сохраняем подзадачи
        for (Subtask subtask : getAllSubtasks()) {
            lines.add(TaskCSVConverter.toString(subtask));
        }

        try {
            Files.write(file.toPath(), lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка сохранения в файл", e);
        }
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        if (!file.exists() || file.length() == 0) {
            return manager;
        }

        try {
            List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);

            if (lines.size() <= 1) {
                return manager; // Только заголовок или пустой файл
            }

            // Пропускаем заголовок
            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i).trim();
                if (line.isEmpty()) {
                    continue;
                }

                try {
                    Task task = TaskCSVConverter.fromString(line);
                    switch (task.getType()) {
                        case TASK:
                            manager.tasks.put(task.getId(), task);
                            break;
                        case EPIC:
                            Epic epic = (Epic) task;
                            manager.epics.put(epic.getId(), epic);
                            break;
                        case SUBTASK:
                            Subtask subtask = (Subtask) task;
                            manager.subtasks.put(subtask.getId(), subtask);

                            // Восстанавливаем связь с эпиком
                            Epic parentEpic = manager.epics.get(subtask.getEpicId());
                            if (parentEpic != null) {
                                parentEpic.addSubtaskId(subtask.getId());
                            }
                            break;
                    }

                    // Обновляем nextId
                    if (task.getId() >= manager.nextId) {
                        manager.nextId = task.getId() + 1;
                    }
                } catch (IllegalArgumentException e) {
                    System.err.println("Ошибка при загрузке строки: " + line + " - " + e.getMessage());
                    // Пропускаем некорректные строки и продолжаем загрузку
                }
            }

            // Обновляем статусы всех эпиков после загрузки
            for (Epic epic : manager.epics.values()) {
                manager.updateEpicStatus(epic.getId());
            }

        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка загрузки из файла", e);
        }

        return manager;
    }

    // Переопределение всех методов для автоматического сохранения
    @Override
    public int createTask(Task task) {
        int id = super.createTask(task);
        save();
        return id;
    }

    @Override
    public int createEpic(Epic epic) {
        int id = super.createEpic(epic);
        save();
        return id;
    }

    @Override
    public int createSubtask(Subtask subtask) {
        int id = super.createSubtask(subtask);
        save();
        return id;
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void deleteTask(int id) {
        super.deleteTask(id);
        save();
    }

    @Override
    public void deleteEpic(int id) {
        super.deleteEpic(id);
        save();
    }

    @Override
    public void deleteSubtask(int id) {
        super.deleteSubtask(id);
        save();
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }

    @Override
    public void deleteAllSubtasks() {
        super.deleteAllSubtasks();
        save();
    }
}