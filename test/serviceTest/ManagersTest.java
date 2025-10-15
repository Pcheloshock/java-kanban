package serviceTest;

import org.junit.jupiter.api.Test;
import service.*;

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

    @Test
    void managersCanCreateMultipleInstances() {
        TaskManager manager1 = Managers.getDefault();
        TaskManager manager2 = Managers.getDefault();
        HistoryManager history1 = Managers.getDefaultHistory();
        HistoryManager history2 = Managers.getDefaultHistory();

        assertAll(
                () -> assertNotNull(manager1, "Первый менеджер должен быть создан"),
                () -> assertNotNull(manager2, "Второй менеджер должен быть создан"),
                () -> assertNotNull(history1, "Первый менеджер истории должен быть создан"),
                () -> assertNotNull(history2, "Второй менеджер истории должен быть создан"),
                () -> assertNotSame(manager1, manager2, "Должны создаваться разные экземпляры менеджера"),
                () -> assertNotSame(history1, history2, "Должны создаваться разные экземпляры менеджера истории")
        );
    }
}