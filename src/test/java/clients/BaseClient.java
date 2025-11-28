package clients;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;

public class BaseClient {
    protected static final String BASE_URL = "https://qa-scooter.praktikum-services.ru/api/v1/";

    protected RequestSpecification getBaseRequest() {
        return given()
                .header("Content-type", "application/json")
                .baseUri(BASE_URL)
                .log().all();
    }

    protected RequestSpecification getBaseRequestText() {
        return given()
                .baseUri(BASE_URL)
                .log().all();
    }

    protected void validateResponseCode(Response response, int expectedCode, String operation) {
        int actualCode = response.statusCode();
        if (actualCode != expectedCode) {
            String responseBody = response.getBody().asString();
            throw new AssertionError(
                    String.format("Запрос '%s' с ошибкой: ожидался статус %d но пришёл %d. Ответ: %s",
                            operation, expectedCode, actualCode, responseBody)
            );
        }
    }

    protected void validateResponseTime(Response response, long maxTimeMs, String operation) {
        long responseTime = response.getTime();
        if (responseTime > maxTimeMs) {
            throw new AssertionError(
                    String.format("Ожидание ответа '%s' превышено: %d ms (max: %d ms)",
                            operation, responseTime, maxTimeMs)
            );
        }
    }
}