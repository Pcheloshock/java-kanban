package service;

import objects.Epic;
import objects.Subtask;
import objects.Task;

import java.util.List;

public interface TaskManager {
    // Методы для Task
    List<Task> getAllTasks();

    Task getTask(int id);

    int createTask(Task task);

    void updateTask(Task task);

    void deleteAllTasks(); // Добавлено

    void deleteTask(int id); // Добавлено

    // Методы для Subtask
    List<Subtask> getAllSubtasks();

    Subtask getSubtask(int id);

    int createSubtask(Subtask subtask);

    void updateSubtask(Subtask subtask);

    void deleteAllSubtasks(); // Добавлено

    void deleteSubtask(int id); // Добавлено

    // Методы для Epic
    List<Epic> getAllEpics();

    Epic getEpic(int id);

    int createEpic(Epic epic);

    void updateEpic(Epic epic);

    void deleteAllEpics(); // Добавлено

    void deleteEpic(int id); // Добавлено

    // Дополнительные методы
    List<Subtask> getEpicSubtasks(int epicId);

    List<Task> getHistory();

}