package service;

import objects.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {
    protected int nextId = 1;
    protected final Map<Integer, Task> tasks = new HashMap<>();
    protected final Map<Integer, Subtask> subtasks = new HashMap<>();
    protected final Map<Integer, Epic> epics = new HashMap<>();
    protected final HistoryManager historyManager = Managers.getDefaultHistory();

    private final Set<Task> prioritizedTasks = new TreeSet<>(
            Comparator.comparing(Task::getStartTime,
                            Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparingInt(Task::getId)
    );

    protected void updateEpicStatus(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) return;

        List<Subtask> epicSubtasks = getEpicSubtasks(epicId);
        if (epicSubtasks.isEmpty()) {
            epic.setStatus(Status.NEW);
            epic.setDuration(Duration.ZERO);
            epic.setStartTime(null);
            epic.setEndTime(null);
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

        // Обновляем временные характеристики эпика
        updateEpicTime(epicId);
    }

    private void updateEpicTime(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) return;

        List<Subtask> epicSubtasks = getEpicSubtasks(epicId);
        if (epicSubtasks.isEmpty()) {
            epic.setDuration(Duration.ZERO);
            epic.setStartTime(null);
            epic.setEndTime(null);
            return;
        }

        // Суммируем продолжительности всех подзадач
        Duration totalDuration = epicSubtasks.stream()
                .map(Subtask::getDuration)
                .filter(Objects::nonNull)
                .reduce(Duration.ZERO, Duration::plus);
        epic.setDuration(totalDuration);

        // Находим самое раннее время начала
        Optional<LocalDateTime> earliestStart = epicSubtasks.stream()
                .map(Subtask::getStartTime)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo);
        epic.setStartTime(earliestStart.orElse(null));

        // Находим самое позднее время окончания
        Optional<LocalDateTime> latestEnd = epicSubtasks.stream()
                .map(Subtask::getEndTime)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo);
        epic.setEndTime(latestEnd.orElse(null));
    }

    private void addToPrioritized(Task task) {
        if (task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
    }

    private void removeFromPrioritized(Task task) {
        prioritizedTasks.remove(task);
    }

    private void updatePrioritizedTasks() {
        prioritizedTasks.clear();

        tasks.values().stream()
                .filter(task -> task.getStartTime() != null)
                .forEach(prioritizedTasks::add);

        subtasks.values().stream()
                .filter(subtask -> subtask.getStartTime() != null)
                .forEach(prioritizedTasks::add);
    }

    // Проверка пересечений по времени
    private boolean hasTimeOverlap(Task task1, Task task2) {
        if (task1.getStartTime() == null || task2.getStartTime() == null ||
                task1.getDuration() == null || task2.getDuration() == null) {
            return false;
        }

        LocalDateTime start1 = task1.getStartTime();
        LocalDateTime end1 = task1.getEndTime();
        LocalDateTime start2 = task2.getStartTime();
        LocalDateTime end2 = task2.getEndTime();

        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    private void validateNoTimeOverlap(Task newTask) {
        if (newTask.getStartTime() == null || newTask.getDuration() == null) {
            return;
        }

        boolean hasOverlap = getAllTasks().stream()
                .filter(task -> task.getId() != newTask.getId())
                .anyMatch(existingTask -> hasTimeOverlap(newTask, existingTask));

        if (hasOverlap) {
            throw new ManagerSaveException("Задача пересекается по времени с существующей задачей");
        }
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    // Методы для Task
    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public void deleteAllTasks() {
        tasks.values().forEach(this::removeFromPrioritized);
        tasks.keySet().forEach(historyManager::remove);
        tasks.clear();
    }

    @Override
    public void deleteTask(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            removeFromPrioritized(task);
            historyManager.remove(id);
            tasks.remove(id);
        }
    }

    @Override
    public Optional<Task> getTask(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            historyManager.add(task);
        }
        return Optional.ofNullable(task);
    }

    @Override
    public int createTask(Task task) {
        Objects.requireNonNull(task, "Task cannot be null");
        validateNoTimeOverlap(task);

        Task newTask = new Task(task.getTitle(), task.getDescription());
        newTask.setId(nextId++);
        newTask.setStatus(task.getStatus());
        newTask.setStartTime(task.getStartTime());
        newTask.setDuration(task.getDuration());
        tasks.put(newTask.getId(), newTask);

        addToPrioritized(newTask);
        return newTask.getId();
    }

    @Override
    public void updateTask(Task task) {
        Objects.requireNonNull(task, "Task cannot be null");
        if (tasks.containsKey(task.getId())) {
            validateNoTimeOverlap(task);

            Task existingTask = tasks.get(task.getId());
            removeFromPrioritized(existingTask);

            existingTask.setTitle(task.getTitle());
            existingTask.setDescription(task.getDescription());
            existingTask.setStatus(task.getStatus());
            existingTask.setStartTime(task.getStartTime());
            existingTask.setDuration(task.getDuration());

            addToPrioritized(existingTask);
        }
    }

    // Методы для Subtask
    @Override
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public void deleteAllSubtasks() {
        subtasks.values().forEach(this::removeFromPrioritized);
        subtasks.keySet().forEach(historyManager::remove);
        subtasks.clear();

        for (Epic epic : epics.values()) {
            epic.getSubtaskIds().clear();
            updateEpicStatus(epic.getId());
        }
        updatePrioritizedTasks();
    }

    @Override
    public void deleteSubtask(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            removeFromPrioritized(subtask);
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
    public Optional<Subtask> getSubtask(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            historyManager.add(subtask);
        }
        return Optional.ofNullable(subtask);
    }

    @Override
    public int createSubtask(Subtask subtask) {
        Objects.requireNonNull(subtask, "Subtask cannot be null");
        validateNoTimeOverlap(subtask);

        if (!epics.containsKey(subtask.getEpicId())) {
            throw new IllegalArgumentException("Epic with id " + subtask.getEpicId() + " not found");
        }

        Subtask newSubtask = new Subtask(subtask.getTitle(), subtask.getDescription(), subtask.getEpicId());
        newSubtask.setId(nextId++);
        newSubtask.setStatus(subtask.getStatus());
        newSubtask.setStartTime(subtask.getStartTime());
        newSubtask.setDuration(subtask.getDuration());
        subtasks.put(newSubtask.getId(), newSubtask);

        Epic epic = epics.get(subtask.getEpicId());
        epic.addSubtaskId(newSubtask.getId());
        updateEpicStatus(epic.getId());

        addToPrioritized(newSubtask);
        return newSubtask.getId();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        Objects.requireNonNull(subtask, "Subtask cannot be null");
        if (subtasks.containsKey(subtask.getId())) {
            validateNoTimeOverlap(subtask);

            Subtask existingSubtask = subtasks.get(subtask.getId());
            removeFromPrioritized(existingSubtask);

            existingSubtask.setTitle(subtask.getTitle());
            existingSubtask.setDescription(subtask.getDescription());
            existingSubtask.setStatus(subtask.getStatus());
            existingSubtask.setStartTime(subtask.getStartTime());
            existingSubtask.setDuration(subtask.getDuration());

            addToPrioritized(existingSubtask);
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
        subtasks.values().forEach(this::removeFromPrioritized);
        subtasks.keySet().forEach(historyManager::remove);
        subtasks.clear();

        epics.keySet().forEach(historyManager::remove);
        epics.clear();
        updatePrioritizedTasks();
    }

    @Override
    public void deleteEpic(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            for (Integer subtaskId : epic.getSubtaskIds()) {
                Subtask subtask = subtasks.get(subtaskId);
                if (subtask != null) {
                    removeFromPrioritized(subtask);
                }
                subtasks.remove(subtaskId);
                historyManager.remove(subtaskId);
            }
            historyManager.remove(id);
            epics.remove(id);
        }
    }

    @Override
    public Optional<Epic> getEpic(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            historyManager.add(epic);
        }
        return Optional.ofNullable(epic);
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
        return epics.values().stream()
                .filter(epic -> epic.getId() == epicId)
                .findFirst()
                .map(Epic::getSubtaskIds)
                .orElse(Collections.emptyList())
                .stream()
                .map(subtasks::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }
}