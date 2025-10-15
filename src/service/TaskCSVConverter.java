package service;

import objects.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TaskCSVConverter {

    private TaskCSVConverter() {
        // приватный конструктор
    }

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public static String getHeader() {
        return "id,type,name,status,description,epic,startTime,duration";
    }

    public static String toString(Task task) {
        if (task == null) {
            return "";
        }

        String[] fields = new String[8];
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

        // Сериализуем startTime и duration
        if (task.getStartTime() != null) {
            fields[6] = task.getStartTime().format(DATE_TIME_FORMATTER);
        } else {
            fields[6] = "";
        }

        if (task.getDuration() != null) {
            fields[7] = String.valueOf(task.getDuration().toMinutes());
        } else {
            fields[7] = "";
        }

        return String.join(",", fields);
    }

    public static Task fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Пустая строка не может быть преобразована в задачу");
        }

        String[] fields = value.split(",", 8); // ограничиваем до 8 полей

        if (fields.length < 8) {
            throw new IllegalArgumentException("Недостаточно полей в CSV строке: " + value);
        }

        try {
            int id = Integer.parseInt(fields[0].trim());
            TaskType type = TaskType.valueOf(fields[1].trim());
            String name = unescape(fields[2]);
            Status status = Status.valueOf(fields[3].trim());
            String description = unescape(fields[4]);
            String epicIdStr = fields[5].trim();

            // Десериализуем startTime и duration
            LocalDateTime startTime = null;
            Duration duration = null;

            if (!fields[6].trim().isEmpty()) {
                startTime = LocalDateTime.parse(fields[6].trim(), DATE_TIME_FORMATTER);
            }

            if (!fields[7].trim().isEmpty()) {
                long minutes = Long.parseLong(fields[7].trim());
                duration = Duration.ofMinutes(minutes);
            }

            switch (type) {
                case TASK:
                    return new Task(id, name, description, status, startTime, duration);
                case EPIC:
                    Epic epic = new Epic(id, name, description, status, startTime, duration, null);
                    // Для эпика endTime будет рассчитан позже
                    return epic;
                case SUBTASK:
                    if (epicIdStr.isEmpty()) {
                        throw new IllegalArgumentException("Для подзадачи не указан epicId");
                    }
                    int epicId = Integer.parseInt(epicIdStr);
                    return new Subtask(id, name, description, status, epicId, startTime, duration);
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