package utils;

import clients.UtilsClient;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class StationsSearchTest {
    private final UtilsClient utilsClient = new UtilsClient();

    @Test
    @DisplayName("Поиск станций с валидным запросом")
    @Description("Проверка поиска станций с валидным запросом: должны возвращаться результаты поиска")
    public void searchStations_WithValidQuery_ShouldReturnStations() {
        String searchQuery = "Сокол";

        Response response = utilsClient.searchStations(searchQuery);

        utilsClient.validateSuccessfulStationsSearch(response);
        utilsClient.validateStationsSearchHasResults(response, searchQuery);
    }

    @Test
    @DisplayName("Поиск станций с пустым запросом")
    @Description("Проверка поиска станций с пустым запросом: должен возвращаться пустой список или все станции")
    public void searchStations_WithEmptyQuery_ShouldReturnEmptyOrAllStations() {
        String emptyQuery = "";

        Response response = utilsClient.searchStations(emptyQuery);

        utilsClient.validateSuccessfulStationsSearch(response);

    }

    @Test
    @DisplayName("Поиск станций с null запросом")
    @Description("Проверка поиска станций с null запросом: должен возвращаться пустой список или все станции")
    public void searchStations_WithNullQuery_ShouldReturnEmptyOrAllStations() {
        String nullQuery = null;

        Response response = utilsClient.searchStations(nullQuery);

        utilsClient.validateSuccessfulStationsSearch(response);
    }

    @Test
    @DisplayName("Поиск несуществующей станции")
    @Description("Проверка поиска несуществующей станции: должен возвращаться пустой список")
    public void searchStations_WithNonExistentStation_ShouldReturnEmpty() {
        String nonExistentStation = "NonexistentStation12345";

        Response response = utilsClient.searchStations(nonExistentStation);

        utilsClient.validateSuccessfulStationsSearch(response);
        utilsClient.validateEmptyStationsSearch(response);
    }

    @Test
    @DisplayName("Проверка структуры станции в результатах поиска")
    @Description("Проверка что в результатах поиска станций присутствуют все необходимые поля")
    public void searchStations_CheckStationStructure() {
        String searchQuery = "Сокол";

        Response response = utilsClient.searchStations(searchQuery);

        utilsClient.validateSuccessfulStationsSearch(response);

        response.then()
                .body("[0].number", notNullValue())
                .body("[0].name", notNullValue())
                .body("[0].color", notNullValue());
    }

    @Test
    @DisplayName("Поиск станций с частичным совпадением")
    @Description("Проверка поиска станций с частичным совпадением запроса: должны возвращаться результаты")
    public void searchStations_WithPartialMatch_ShouldReturnResults() {
        String partialQuery = "Соко";

        Response response = utilsClient.searchStations(partialQuery);

        utilsClient.validateSuccessfulStationsSearch(response);
        utilsClient.validateStationsSearchHasResults(response, partialQuery);
    }

    @Test
    @DisplayName("Поиск станций с регистронезависимым запросом")
    @Description("Проверка что поиск станций работает регистронезависимо: должны возвращаться результаты независимо от регистра")
    public void searchStations_WithCaseInsensitiveQuery_ShouldReturnResults() {
        String lowerCaseQuery = "сокол";
        String upperCaseQuery = "СОКОЛ";

        Response lowerCaseResponse = utilsClient.searchStations(lowerCaseQuery);
        Response upperCaseResponse = utilsClient.searchStations(upperCaseQuery);

        utilsClient.validateSuccessfulStationsSearch(lowerCaseResponse);
        utilsClient.validateSuccessfulStationsSearch(upperCaseResponse);

        assertThat("Lower case query should return results",
                lowerCaseResponse.jsonPath().getList("").size(), greaterThan(0));
        assertThat("Upper case query should return results",
                upperCaseResponse.jsonPath().getList("").size(), greaterThan(0));
    }

    @Test
    @DisplayName("Проверка валидности данных в результатах поиска")
    @Description("Проверка что данные станций в результатах поиска валидны: имя, номер и цвет должны соответствовать ожиданиям")
    public void searchStations_ResponseContainsValidData() {
        String searchQuery = "Сокол";

        Response response = utilsClient.searchStations(searchQuery);

        utilsClient.validateSuccessfulStationsSearch(response);

        String firstStationName = response.jsonPath().getString("[0].name");
        String firstStationNumber = response.jsonPath().getString("[0].number");
        String firstStationColor = response.jsonPath().getString("[0].color");

        assertThat("Station name should not be empty", firstStationName, not(emptyString()));
        assertThat("Station number should not be empty", firstStationNumber, not(emptyString()));
        assertThat("Station color should be valid hex color", firstStationColor, matchesPattern("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$"));
    }

    @Test
    @DisplayName("Поиск станций со специальными символами")
    @Description("Проверка поиска станций со специальными символами в запросе: сервер должен корректно обрабатывать такие запросы")
    public void searchStations_WithSpecialCharacters_ShouldHandleGracefully() {
        String specialCharsQuery = "!@#$%";

        Response response = utilsClient.searchStations(specialCharsQuery);

        utilsClient.validateSuccessfulStationsSearch(response);
    }

    @Test
    @DisplayName("Поиск станций с очень длинным запросом")
    @Description("Проверка поиска станций с очень длинным запросом: сервер должен корректно обрабатывать длинные запросы без ошибок сервера")
    public void searchStations_WithVeryLongQuery_ShouldHandleGracefully() {
        String longQuery = "A".repeat(1000);

        Response response = utilsClient.searchStations(longQuery);

        int statusCode = response.statusCode();
        assertThat("Server should handle long query without server error",
                statusCode, not(greaterThanOrEqualTo(500)));
    }
}