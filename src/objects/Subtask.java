package objects;

public class Subtask extends Task {
    private int epicId;

    // Конструктор для создания новой подзадачи (без id)
    public Subtask(String title, String description, int epicId) {
        super(title, description);
        this.epicId = epicId;
    }

    // Конструктор для создания подзадачи с id и статусом
    public Subtask(int id, String title, String description, Status status, int epicId) {
        super(id, title, description, status);
        this.epicId = epicId;
    }

    @Override
    public TaskType getType() {
        return TaskType.SUBTASK;
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