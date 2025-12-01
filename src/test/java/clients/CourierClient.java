package clients;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import models.Courier;
import models.CourierCredentials;

import java.util.HashMap;
import java.util.Map;

public class CourierClient extends BaseClient {

    @Step("Create courier: {courier.login}")
    public Response createCourier(Courier courier) {
        return getBaseRequest()
                .body(courier)
                .when()
                .post("courier");
    }

    @Step("Login courier: {credentials.login}")
    public Response loginCourier(CourierCredentials credentials) {
        return getBaseRequest()
                .body(credentials)
                .when()
                .post("courier/login");
    }

    @Step("Delete courier with id: {courierId}")
    public Response deleteCourier(String courierId) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("id", courierId);

        return getBaseRequest()
                .body(requestBody)
                .when()
                .delete("courier");
    }

    @Step("Get courier orders count: {courierId}")
    public Response getCourierOrdersCount(String courierId) {
        return getBaseRequest()
                .when()
                .get("courier/" + courierId + "/ordersCount");
    }

    @Step("Login and get courier ID")
    public String loginAndGetCourierId(Courier courier) {
        Response loginResponse = loginCourier(new CourierCredentials(courier));
        validateResponseCode(loginResponse, 200, "Login after creation");
        return loginResponse.jsonPath().getString("id");
    }

    @Step("Validate successful creation response")
    public void validateSuccessfulCreation(Response response) {
        validateResponseCode(response, 201, "Create courier");
        response.then().body("ok", org.hamcrest.Matchers.is(true));
    }

    @Step("Validate creation error - missing fields")
    public void validateCreationMissingFields(Response response) {
        validateResponseCode(response, 400, "Create courier with missing fields");
        response.then().body("message", org.hamcrest.Matchers.equalTo("Недостаточно данных для создания учетной записи"));
    }

    @Step("Validate creation error - duplicate login")
    public void validateCreationDuplicateLogin(Response response) {
        validateResponseCode(response, 409, "Create courier with duplicate login");
        response.then().body("message", org.hamcrest.Matchers.equalTo("Этот логин уже используется"));
    }

    @Step("Validate successful login response")
    public void validateSuccessfulLogin(Response response) {
        validateResponseCode(response, 200, "Login courier");
        response.then().body("id", org.hamcrest.Matchers.notNullValue());
    }

    @Step("Validate login error - missing fields")
    public void validateLoginMissingFields(Response response) {
        validateResponseCode(response, 400, "Login with missing fields");
        response.then().body("message", org.hamcrest.Matchers.equalTo("Недостаточно данных для входа"));
    }

    @Step("Validate login error - account not found")
    public void validateLoginAccountNotFound(Response response) {
        validateResponseCode(response, 404, "Login with invalid credentials");
        response.then().body("message", org.hamcrest.Matchers.equalTo("Учетная запись не найдена"));
    }

    @Step("Validate successful deletion response")
    public void validateSuccessfulDeletion(Response response) {
        validateResponseCode(response, 200, "Delete courier");
        response.then().body("ok", org.hamcrest.Matchers.is(true));
    }

    @Step("Validate deletion error - missing id")
    public void validateDeletionMissingId(Response response) {
        validateResponseCode(response, 400, "Delete courier without id");
        response.then().body("message", org.hamcrest.Matchers.equalTo("Недостаточно данных для удаления курьера"));
    }

    @Step("Validate deletion error - courier not found")
    public void validateDeletionNotFound(Response response) {
        validateResponseCode(response, 404, "Delete non-existent courier");
        response.then().body("message", org.hamcrest.Matchers.equalTo("Курьера с таким id нет"));
    }

    @Step("Validate successful orders count response")
    public void validateSuccessfulOrdersCount(Response response, String expectedCourierId) {
        validateResponseCode(response, 200, "Get courier orders count");
        response.then()
                .body("id", org.hamcrest.Matchers.equalTo(expectedCourierId))
                .body("ordersCount", org.hamcrest.Matchers.notNullValue());
    }

    @Step("Validate orders count error - missing id")
    public void validateOrdersCountMissingId(Response response) {
        validateResponseCode(response, 400, "Get orders count without id");
        response.then().body("message", org.hamcrest.Matchers.equalTo("Недостаточно данных для поиска"));
    }

    @Step("Validate orders count error - courier not found")
    public void validateOrdersCountNotFound(Response response) {
        validateResponseCode(response, 404, "Get orders count for non-existent courier");
        response.then().body("message", org.hamcrest.Matchers.equalTo("Курьер не найден"));
    }
}