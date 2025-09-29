package service;

import objects.Epic;
import objects.Status;
import objects.Subtask;
import objects.Task;

import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    protected int nextId = 1;
    protected final Map<Integer, Task> tasks = new HashMap<>();
    protected final Map<Integer, Subtask> subtasks = new HashMap<>();
    protected final Map<Integer, Epic> epics = new HashMap<>();
    protected final HistoryManager historyManager = Managers.getDefaultHistory();

    protected void updateEpicStatus(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) return;

        List<Subtask> epicSubtasks = getEpicSubtasks(epicId);
        if (epicSubtasks.isEmpty()) {
            epic.setStatus(Status.NEW);
            return;
        }

        boolean allDone = true;
        boolean allNew = true;

        for (Subtask subtask : epicSubtasks) {
            if (subtask.getStatus() != Status.DONE) {
                allDone = false;
            }
            if (subtask.getStatus() != Status.NEW) {
                allNew = false;
            }
        }

        if (allDone) {
            epic.setStatus(Status.DONE);
        } else if (allNew) {
            epic.setStatus(Status.NEW);
        } else {
            epic.setStatus(Status.IN_PROGRESS);
        }
    }

    // Методы для Task
    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public void deleteAllTasks() {
        for (Integer taskId : tasks.keySet()) {
            historyManager.remove(taskId);
        }
        tasks.clear();
    }

    @Override
    public void deleteTask(int id) {
        if (tasks.containsKey(id)) {
            historyManager.remove(id);
            tasks.remove(id);
        }
    }

    @Override
    public Task getTask(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            historyManager.add(task);
        }
        return task;
    }

    @Override
    public int createTask(Task task) {
        Objects.requireNonNull(task, "Task cannot be null");
        Task newTask = new Task(task.getTitle(), task.getDescription());
        newTask.setId(nextId++);
        newTask.setStatus(task.getStatus());
        tasks.put(newTask.getId(), newTask);
        return newTask.getId();
    }

    @Override
    public void updateTask(Task task) {
        Objects.requireNonNull(task, "Task cannot be null");
        if (tasks.containsKey(task.getId())) {
            Task existingTask = tasks.get(task.getId());
            existingTask.setTitle(task.getTitle());
            existingTask.setDescription(task.getDescription());
            existingTask.setStatus(task.getStatus());
        }
    }

    // Методы для Subtask
    @Override
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public void deleteAllSubtasks() {
        for (Integer subtaskId : subtasks.keySet()) {
            historyManager.remove(subtaskId);
        }
        subtasks.clear();

        for (Epic epic : epics.values()) {
            epic.getSubtaskIds().clear();
            updateEpicStatus(epic.getId());
        }
    }

    @Override
    public void deleteSubtask(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.getSubtaskIds().remove((Integer) id);
                updateEpicStatus(epic.getId());
            }
            historyManager.remove(id);
            subtasks.remove(id);
        }
    }

    @Override
    public Subtask getSubtask(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            historyManager.add(subtask);
        }
        return subtask;
    }

    @Override
    public int createSubtask(Subtask subtask) {
        Objects.requireNonNull(subtask, "Subtask cannot be null");

        if (!epics.containsKey(subtask.getEpicId())) {
            throw new IllegalArgumentException("Epic with id " + subtask.getEpicId() + " not found");
        }

        Subtask newSubtask = new Subtask(subtask.getTitle(), subtask.getDescription(), subtask.getEpicId());
        newSubtask.setId(nextId++);
        newSubtask.setStatus(subtask.getStatus());
        subtasks.put(newSubtask.getId(), newSubtask);

        Epic epic = epics.get(subtask.getEpicId());
        epic.addSubtaskId(newSubtask.getId());
        updateEpicStatus(epic.getId());

        return newSubtask.getId();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        Objects.requireNonNull(subtask, "Subtask cannot be null");
        if (subtasks.containsKey(subtask.getId())) {
            Subtask existingSubtask = subtasks.get(subtask.getId());
            existingSubtask.setTitle(subtask.getTitle());
            existingSubtask.setDescription(subtask.getDescription());
            existingSubtask.setStatus(subtask.getStatus());
            updateEpicStatus(existingSubtask.getEpicId());
        }
    }

    // Методы для Epic
    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public void deleteAllEpics() {
        for (Integer subtaskId : subtasks.keySet()) {
            historyManager.remove(subtaskId);
        }
        subtasks.clear();

        for (Integer epicId : epics.keySet()) {
            historyManager.remove(epicId);
        }
        epics.clear();
    }

    @Override
    public void deleteEpic(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            for (Integer subtaskId : epic.getSubtaskIds()) {
                subtasks.remove(subtaskId);
                historyManager.remove(subtaskId);
            }
            historyManager.remove(id);
            epics.remove(id);
        }
    }

    @Override
    public Epic getEpic(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            historyManager.add(epic);
        }
        return epic;
    }

    @Override
    public int createEpic(Epic epic) {
        Objects.requireNonNull(epic, "Epic cannot be null");
        Epic newEpic = new Epic(epic.getTitle(), epic.getDescription());
        newEpic.setId(nextId++);
        epics.put(newEpic.getId(), newEpic);
        return newEpic.getId();
    }

    @Override
    public void updateEpic(Epic epic) {
        Objects.requireNonNull(epic, "Epic cannot be null");
        if (epics.containsKey(epic.getId())) {
            Epic existingEpic = epics.get(epic.getId());
            existingEpic.setTitle(epic.getTitle());
            existingEpic.setDescription(epic.getDescription());
        }
    }

    // Дополнительные методы
    @Override
    public List<Subtask> getEpicSubtasks(int epicId) {
        if (!epics.containsKey(epicId)) {
            return Collections.emptyList();
        }

        Epic epic = epics.get(epicId);
        List<Subtask> result = new ArrayList<>();
        for (Integer subtaskId : epic.getSubtaskIds()) {
            Subtask subtask = subtasks.get(subtaskId);
            if (subtask != null) {
                result.add(subtask);
            }
        }
        return result;
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }
}