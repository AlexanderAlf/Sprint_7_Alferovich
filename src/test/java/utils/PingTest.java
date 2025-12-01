package utils;

import clients.UtilsClient;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class PingTest {
    private final UtilsClient utilsClient = new UtilsClient();

    @Test
    @DisplayName("Пинг сервера")
    @Description("Проверка пинга сервера: должен вернуться код ответа 200 и текст 'pong;'")
    public void pingServer_ShouldReturn200AndPong() {
        Response response = utilsClient.pingServer();

        utilsClient.validateSuccessfulPing(response);

        String responseBody = response.getBody().asString().trim();
        assertThat("Response should be 'pong;'", responseBody, equalTo("pong;"));
    }

    @Test
    @DisplayName("Проверка времени ответа при пинге")
    @Description("Проверка что время ответа при пинге сервера должно быть разумным (менее 5 секунд)")
    public void pingServer_ResponseTimeShouldBeReasonable() {
        Response response = utilsClient.pingServer();

        utilsClient.validateSuccessfulPing(response);

        long responseTime = response.getTime();
        assertThat("Response time should be less than 5 seconds",
                responseTime, lessThan(5000L));
    }

    @Test
    @DisplayName("Проверка Content-Type при пинге")
    @Description("Проверка что Content-Type ответа при пинге сервера должен быть text/plain")
    public void pingServer_ContentTypeShouldBeText() {
        Response response = utilsClient.pingServer();

        utilsClient.validateSuccessfulPing(response);

        String contentType = response.getContentType();
        assertThat("Content-Type should be text/plain",
                contentType, containsString("text/plain"));
    }
}