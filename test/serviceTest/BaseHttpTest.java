package serviceTest;

import com.google.gson.Gson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import service.HttpTaskServer;
import service.Managers;
import service.TaskManager;

import java.io.IOException;
import java.net.http.HttpClient;

public abstract class BaseHttpTest {
    protected TaskManager manager;
    protected HttpTaskServer taskServer;
    protected HttpClient client;
    protected Gson gson;
    protected static final String BASE_URL = "http://localhost:8080";

    @BeforeEach
    void setUpBase() throws IOException {
        manager = Managers.getDefault();
        taskServer = new HttpTaskServer(manager);
        taskServer.start();
        client = HttpClient.newHttpClient();
        gson = HttpTaskServer.getGson();

        // Очищаем данные перед каждым тестом
        manager.deleteAllTasks();
        manager.deleteAllSubtasks();
        manager.deleteAllEpics();
    }

    @AfterEach
    void tearDownBase() {
        if (taskServer != null) {
            taskServer.stop();
        }
    }
}