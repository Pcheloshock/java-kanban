package service;

import objects.Task;
import java.util.*;

public class InMemoryHistoryManager implements HistoryManager {
    private final List<Task> history = new LinkedList<>();
    private static final int MAX_HISTORY_SIZE = 10;

    @Override
    public void add(Task task) {
        if (task == null) return;
        Task taskCopy = createTaskCopy(task);
        history.add(taskCopy);
        if (history.size() > MAX_HISTORY_SIZE) {
            history.remove(0);
        }
    }

    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(history);
    }

    @Override
    public void remove(int id) {
        history.removeIf(task -> task.getId() == id);
    }

    private Task createTaskCopy(Task task) {
        Task copy = new Task(task.getTitle(), task.getDescription());
        copy.setId(task.getId());
        copy.setStatus(task.getStatus());
        return copy;
    }
}