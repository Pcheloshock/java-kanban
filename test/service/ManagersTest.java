package service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ManagersTest {

    @Test
    void getDefaultReturnsInitializedTaskManager() {
        TaskManager manager = Managers.getDefault();
        assertNotNull(manager, "Менеджер задач не инициализирован");
        assertTrue(manager instanceof InMemoryTaskManager, "Неверная реализация TaskManager");
    }

    @Test
    void getDefaultHistoryReturnsInitializedHistoryManager() {
        HistoryManager history = Managers.getDefaultHistory();
        assertNotNull(history, "Менеджер истории не инициализирован");
        assertTrue(history instanceof InMemoryHistoryManager, "Неверная реализация HistoryManager");
    }
}