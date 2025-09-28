package objects;

public class Subtask extends Task {
    private int epicId;

    @Override
    public TaskType getType() {
        return TaskType.SUBTASK;
    }

    // Конструктор для создания новой подзадачи (без id и status)
    public Subtask(String title, String description, int epicId) {
        super(title, description);
        this.epicId = epicId;
    }

    // Конструктор для получения подзадачи из менеджера (с id и status)
    public Subtask(int id, String title, String description, Status status, int epicId) {
        super(title, description); // Используем базовый конструктор
        this.setId(id);            // Устанавливаем ID через сеттер
        this.setStatus(status);    // Устанавливаем статус через сеттер
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    public void setEpicId(int epicId) {
        if (this.getId() == epicId) {
            throw new IllegalArgumentException("Подзадача не может быть эпиком для самой себя");
        }
        this.epicId = epicId;
    }

    @Override
    public String toString() {
        return "Subtask{" +
                "id=" + getId() +
                ", title='" + getTitle() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", status=" + getStatus() +
                ", epicId=" + epicId +
                '}';
    }
}