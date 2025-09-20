package objects;

import service.Managers;
import service.TaskManager;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== ДЕМОНСТРАЦИЯ РАБОТЫ ТРЕКЕРА ЗАДАЧ ===\n");

        // Создаем менеджер через утилитарный класс
        TaskManager manager = Managers.getDefault();

        // Этап 1: Создание задач
        System.out.println("=== ЭТАП 1: СОЗДАНИЕ ЗАДАЧ ===");

        // Создаем обычные задачи
        Task task1 = new Task("Помыть посуду", "Помыть всю посуду вечером");
        int taskId1 = manager.createTask(task1);

        Task task2 = new Task("Сделать уроки", "Выполнить домашнее задание по математике");
        int taskId2 = manager.createTask(task2);

        Epic epic1 = new Epic("Переезд", "Организация переезда в другой город");
        int epicId1 = manager.createEpic(epic1);

        Epic epic2 = new Epic("Ремонт", "Капитальный ремонт квартиры");
        int epicId2 = manager.createEpic(epic2);

        Subtask subtask1 = new Subtask("Собрать коробки", "Купить и собрать коробки для переезда", epicId1);
        int subtaskId1 = manager.createSubtask(subtask1);

        Subtask subtask2 = new Subtask("Упаковать вещи", "Аккуратно упаковать все вещи", epicId1);
        int subtaskId2 = manager.createSubtask(subtask2);

        Subtask subtask3 = new Subtask("Купить материалы", "Закупить строительные материалы", epicId2);
        int subtaskId3 = manager.createSubtask(subtask3);
        System.out.println("Создана подзадача с ID: " + subtaskId3 + " для эпика " + epicId2);

        printAllTasks(manager);

        // Этап 2: Просмотр задач (заполняем историю)
        System.out.println("\n=== ЭТАП 2: ПРОСМОТР ЗАДАЧ (заполнение истории) ===");

        System.out.println("Просматриваем задачу 1...");
        manager.getTask(taskId1);

        System.out.println("Просматриваем эпик 1...");
        manager.getEpic(epicId1);

        System.out.println("Просматриваем подзадачу 2...");
        manager.getSubtask(subtaskId2);

        System.out.println("Просматриваем задачу 2...");
        manager.getTask(taskId2);

        System.out.println("Просматриваем эпик 2...");
        manager.getEpic(epicId2);

        System.out.println("Текущая история просмотров:");
        for (Task task : manager.getHistory()) {
            System.out.println(" - " + task);
        }

        // Этап 3: Обновление статусов
        System.out.println("\n=== ЭТАП 3: ОБНОВЛЕНИЕ СТАТУСОВ ===");

        // Обновляем подзадачу
        Subtask updatedSubtask = new Subtask(subtaskId2, "Упаковать вещи",
                "Аккуратно упаковать все вещи", Status.DONE, epicId1);
        manager.updateSubtask(updatedSubtask);
        System.out.println("Обновлена подзадача " + subtaskId2 + " -> статус DONE");

        // Проверяем автоматическое обновление статуса эпика
        System.out.println("Статус эпика " + epicId1 + " после обновления подзадачи: " +
                manager.getEpic(epicId1).getStatus());

        printAllTasks(manager);



        System.out.println("История после удаления (должны автоматически удалиться удаленные задачи):");
        for (Task task : manager.getHistory()) {
            System.out.println(" - " + task);
        }

        // Этап 5: Дополнительные просмотры (проверка ограничения истории)
        System.out.println("\n=== ЭТАП 5: ПРОВЕРКА ОГРАНИЧЕНИЯ ИСТОРИИ (10 элементов) ===");

        System.out.println("Добавляем дополнительные просмотры...");
        manager.getTask(taskId2); // повторный просмотр
        manager.getSubtask(subtaskId3);
        manager.getEpic(epicId1);
        manager.getSubtask(subtaskId2);
        manager.getEpic(epicId2);
        manager.getTask(taskId2); // еще раз

        System.out.println("Финальная история (максимум 10 элементов):");
        for (Task task : manager.getHistory()) {
            System.out.println(" - " + task);
        }
        System.out.println("Размер истории: " + manager.getHistory().size());

        // Финальный вывод всех задач
        System.out.println("\n=== ФИНАЛЬНОЕ СОСТОЯНИЕ ВСЕХ ЗАДАЧ ===");
        printAllTasks(manager);
    }

    private static void printAllTasks(TaskManager manager) {
        System.out.println("\n--- ВСЕ ЗАДАЧИ ---");

        System.out.println("Задачи:");
        for (Task task : manager.getAllTasks()) {
            System.out.println("  " + task);
        }

        System.out.println("Эпики:");
        for (Epic epic : manager.getAllEpics()) {
            System.out.println("  " + epic);

            for (Subtask subtask : manager.getEpicSubtasks(epic.getId())) {
                System.out.println("    --> " + subtask);
            }
        }

        System.out.println("Подзадачи:");
        for (Subtask subtask : manager.getAllSubtasks()) {
            System.out.println("  " + subtask);
        }

        System.out.println("История просмотров:");
        for (Task task : manager.getHistory()) {
            System.out.println("  - " + task);
        }
        System.out.println("------------------\n");
    }
}

