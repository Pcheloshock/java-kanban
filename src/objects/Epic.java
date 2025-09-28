package objects;

import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private List<Integer> subtaskIds;

    @Override
    public TaskType getType() {
        return TaskType.EPIC;
    }

    // Конструктор для создания нового эпика (без id и status)
    public Epic(String title, String description) {
        super(title, description);
        this.subtaskIds = new ArrayList<>();
    }

    // Конструктор для получения эпика из менеджера (с id и status)
    public Epic(int id, String title, String description, Status status) {
        super(title, description); // Используем базовый конструктор
        this.setId(id);            // Устанавливаем ID через сеттер
        this.setStatus(status);    // Устанавливаем статус через сеттер
        this.subtaskIds = new ArrayList<>();
    }

    public List<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    public void addSubtaskId(int subtaskId) {
        subtaskIds.add(subtaskId);
    }

    public void removeSubtaskId(int subtaskId) {
        subtaskIds.remove(Integer.valueOf(subtaskId));
    }

    @Override
    public String toString() {
        return "Epic{" +
                "id=" + getId() +
                ", title='" + getTitle() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", status=" + getStatus() +
                ", subtaskIds=" + subtaskIds +
                '}';
    }
}