package utils;

import clients.UtilsClient;
import io.restassured.response.Response;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class StationsSearchTest {
    private final UtilsClient utilsClient = new UtilsClient();

    @Test
    public void searchStations_WithValidQuery_ShouldReturnStations() {
        String searchQuery = "Сокол";

        Response response = utilsClient.searchStations(searchQuery);

        utilsClient.validateSuccessfulStationsSearch(response);
        utilsClient.validateStationsSearchHasResults(response, searchQuery);
    }

    @Test
    public void searchStations_WithEmptyQuery_ShouldReturnEmptyOrAllStations() {
        String emptyQuery = "";

        Response response = utilsClient.searchStations(emptyQuery);

        utilsClient.validateSuccessfulStationsSearch(response);

    }

    @Test
    public void searchStations_WithNullQuery_ShouldReturnEmptyOrAllStations() {
        String nullQuery = null;

        Response response = utilsClient.searchStations(nullQuery);

        utilsClient.validateSuccessfulStationsSearch(response);
        }

    @Test
    public void searchStations_WithNonExistentStation_ShouldReturnEmpty() {
        String nonExistentStation = "NonexistentStation12345";

        Response response = utilsClient.searchStations(nonExistentStation);

        utilsClient.validateSuccessfulStationsSearch(response);
        utilsClient.validateEmptyStationsSearch(response);
    }

    @Test
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
    public void searchStations_WithPartialMatch_ShouldReturnResults() {
        String partialQuery = "Соко";

        Response response = utilsClient.searchStations(partialQuery);

        utilsClient.validateSuccessfulStationsSearch(response);
        utilsClient.validateStationsSearchHasResults(response, partialQuery);
    }

    @Test
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
    public void searchStations_WithSpecialCharacters_ShouldHandleGracefully() {
        String specialCharsQuery = "!@#$%";

        Response response = utilsClient.searchStations(specialCharsQuery);

        utilsClient.validateSuccessfulStationsSearch(response);
        }

    @Test
    public void searchStations_WithVeryLongQuery_ShouldHandleGracefully() {
        String longQuery = "A".repeat(1000);

        Response response = utilsClient.searchStations(longQuery);

        int statusCode = response.statusCode();
        assertThat("Server should handle long query without server error",
                statusCode, not(greaterThanOrEqualTo(500)));
    }
}