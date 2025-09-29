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

    void deleteAllTasks();

    void deleteTask(int id);

    // Методы для Subtask
    List<Subtask> getAllSubtasks();

    Subtask getSubtask(int id);

    int createSubtask(Subtask subtask);

    void updateSubtask(Subtask subtask);

    void deleteAllSubtasks();

    void deleteSubtask(int id);

    // Методы для Epic
    List<Epic> getAllEpics();

    Epic getEpic(int id);

    int createEpic(Epic epic);

    void updateEpic(Epic epic);

    void deleteAllEpics();

    void deleteEpic(int id);

    // Дополнительные методы
    List<Subtask> getEpicSubtasks(int epicId);

    List<Task> getHistory();
}