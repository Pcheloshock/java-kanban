import java.util.List;  // Добавляем импорт для List

public class Main {
    public static void main(String[] args) {
        TaskManager manager = new TaskManager();

        System.out.println("=== Создание задач ===");
        Task task1 = new Task(0, "Помыть посуду", "Помыть всю посуду вечером", Status.NEW);
        int taskId1 = manager.createTask(task1);
        System.out.println("Создана задача с ID: " + taskId1);

        System.out.println("\n=== Создание эпиков ===");
        Epic epic1 = new Epic(0, "Переезд", "Организация переезда в другой город", Status.NEW);
        int epicId1 = manager.createEpic(epic1);
        System.out.println("Создан эпик с ID: " + epicId1);

        System.out.println("\n=== Создание подзадач ===");
        Subtask subtask1 = new Subtask(0, "Собрать коробки", "Купить и собрать коробки для переезда",
                Status.NEW, epicId1);
        int subtaskId1 = manager.createSubtask(subtask1);
        System.out.println("Создана подзадача с ID: " + subtaskId1 + " для эпика " + epicId1);

        Subtask subtask2 = new Subtask(0, "Упаковать вещи", "Аккуратно упаковать все вещи",
                Status.IN_PROGRESS, epicId1);
        int subtaskId2 = manager.createSubtask(subtask2);
        System.out.println("Создана подзадача с ID: " + subtaskId2 + " для эпика " + epicId1);

        System.out.println("\n=== Проверка статуса эпика ===");
        Epic updatedEpic = manager.getEpic(epicId1);
        System.out.println("Статус эпика: " + updatedEpic.getStatus());

        System.out.println("\n=== Обновление подзадачи ===");
        Subtask updatedSubtask = new Subtask(subtaskId2, "Упаковать вещи",
                "Аккуратно упаковать все вещи", Status.DONE, epicId1);
        manager.updateSubtask(updatedSubtask);
        System.out.println("Подзадача обновлена. Новый статус: " + updatedSubtask.getStatus());

        updatedEpic = manager.getEpic(epicId1);
        System.out.println("Новый статус эпика: " + updatedEpic.getStatus());

        System.out.println("\n=== Список подзадач эпика ===");
        List<Subtask> epicSubtasks = manager.getEpicSubtasks(epicId1);  // Теперь List распознаётся
        for (Subtask subtask : epicSubtasks) {
            System.out.println(subtask);
        }

        System.out.println("\n=== Удаление подзадачи ===");
        manager.deleteSubtask(subtaskId1);
        System.out.println("Подзадача " + subtaskId1 + " удалена");

        updatedEpic = manager.getEpic(epicId1);
        System.out.println("Статус эпика после удаления: " + updatedEpic.getStatus());
    }
}