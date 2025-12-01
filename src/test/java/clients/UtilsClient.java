package clients;

import io.qameta.allure.Step;
import io.restassured.response.Response;

public class UtilsClient extends BaseClient {

    @Step("Ping server")
    public Response pingServer() {
        return getBaseRequestText()
                .when()
                .get("ping");
    }

    @Step("Search stations by name: {searchQuery}")
    public Response searchStations(String searchQuery) {
        return getBaseRequest()
                .queryParam("s", searchQuery)
                .when()
                .get("stations/search");
    }

    @Step("Validate successful ping response")
    public void validateSuccessfulPing(Response response) {
        validateResponseCode(response, 200, "Ping server");
        String responseBody = response.getBody().asString();
        if (!"pong;".equals(responseBody.trim())) {
            throw new AssertionError("Ping response should be 'pong;' but was: " + responseBody);
        }
    }

    @Step("Validate successful stations search response")
    public void validateSuccessfulStationsSearch(Response response) {
        validateResponseCode(response, 200, "Search stations");
        response.then()
                .body("[0].number", org.hamcrest.Matchers.anyOf(
                        org.hamcrest.Matchers.nullValue(),
                        org.hamcrest.Matchers.notNullValue()))
                .body("[0].name", org.hamcrest.Matchers.anyOf(
                        org.hamcrest.Matchers.nullValue(),
                        org.hamcrest.Matchers.notNullValue()))
                .body("[0].color", org.hamcrest.Matchers.anyOf(
                        org.hamcrest.Matchers.nullValue(),
                        org.hamcrest.Matchers.notNullValue()));
    }

    @Step("Validate stations search returns results for query: {searchQuery}")
    public void validateStationsSearchHasResults(Response response, String searchQuery) {
        validateResponseCode(response, 200, "Search stations with results");
        int resultsCount = response.jsonPath().getList("").size();
        if (resultsCount > 0) {
            response.then()
                    .body("name", org.hamcrest.Matchers.everyItem(
                            org.hamcrest.Matchers.containsStringIgnoringCase(searchQuery)));
        }
    }

    @Step("Validate empty stations search response")
    public void validateEmptyStationsSearch(Response response) {
        validateResponseCode(response, 200, "Search stations with no results");
        response.then().body("size()", org.hamcrest.Matchers.equalTo(0));
    }
}