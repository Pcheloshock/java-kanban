package service;

import objects.*;

public class TaskCSVConverter {

    private TaskCSVConverter() {
        // приватный конструктор
    }

    public static String getHeader() {
        return "id,type,name,status,description,epic";
    }

    public static String toString(Task task) {
        if (task == null) {
            return "";
        }

        String[] fields = new String[6];
        fields[0] = String.valueOf(task.getId());
        fields[1] = task.getType().toString();
        fields[2] = escape(task.getTitle());
        fields[3] = task.getStatus().toString();
        fields[4] = escape(task.getDescription());

        if (task.getType() == TaskType.SUBTASK) {
            fields[5] = String.valueOf(((Subtask) task).getEpicId());
        } else {
            fields[5] = "";
        }

        return String.join(",", fields);
    }

    public static Task fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Пустая строка не может быть преобразована в задачу");
        }

        String[] fields = value.split(",", 6); // ограничиваем до 6 полей

        if (fields.length < 6) {
            throw new IllegalArgumentException("Недостаточно полей в CSV строке: " + value);
        }

        try {
            int id = Integer.parseInt(fields[0].trim());
            TaskType type = TaskType.valueOf(fields[1].trim());
            String name = unescape(fields[2]);
            Status status = Status.valueOf(fields[3].trim());
            String description = unescape(fields[4]);
            String epicIdStr = fields[5].trim();

            switch (type) {
                case TASK:
                    return new Task(id, name, description, status);
                case EPIC:
                    return new Epic(id, name, description, status);
                case SUBTASK:
                    if (epicIdStr.isEmpty()) {
                        throw new IllegalArgumentException("Для подзадачи не указан epicId");
                    }
                    int epicId = Integer.parseInt(epicIdStr);
                    return new Subtask(id, name, description, status, epicId);
                default:
                    throw new IllegalArgumentException("Неизвестный тип задачи: " + type);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Неверный формат числа в CSV строке: " + value, e);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Неверный формат данных в CSV строке: " + value, e);
        }
    }

    private static String escape(String text) {
        if (text == null) return "";
        // Экранируем запятые и кавычки
        return text.replace("\"", "\"\"")
                .replace(",", "\\,")
                .replace("\n", "\\n");
    }

    private static String unescape(String text) {
        if (text == null) return "";
        return text.replace("\"\"", "\"")
                .replace("\\,", ",")
                .replace("\\n", "\n");
    }
}