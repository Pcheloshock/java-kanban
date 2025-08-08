import java.util.List;
import java.util.ArrayList;
public class Epic extends Task {
    private final List<Integer> subtaskIds = new ArrayList<>();

    public Epic(int id, String name, String description, Status status) {
        super(id, name, description, status);
    }

    public List<Integer> getSubtaskIds() { return subtaskIds; }
    public void addSubtaskId(int subtaskId) { subtaskIds.add(subtaskId); }
    public void removeSubtaskId(int subtaskId) { subtaskIds.remove((Integer) subtaskId); }

    @Override
    public String toString() {
        return "Epic{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", status=" + getStatus() +
                ", subtaskIds=" + subtaskIds +
                '}';
    }
}

enum Status {
    NEW, IN_PROGRESS, DONE
}