package courier;

import clients.CourierClient;
import helpers.CourierTestHelper;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import models.Courier;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class CourierOrdersCountTest {
    private final CourierClient courierClient = new CourierClient();
    private final CourierTestHelper courierHelper = new CourierTestHelper();
    private String createdCourierId;

    @Before
    public void setUp() {
        Courier testCourier = courierHelper.setupTestCourier();
        createdCourierId = courierHelper.getCreatedCourierId();
    }

    @After
    public void tearDown() {
        courierHelper.cleanupAllTestCouriers();
    }

    @Test
    @DisplayName("Получение количества заказов курьера с валидным ID")
    @Description("Проверка получения количества заказов курьера с валидным ID: должен вернуться код ответа 200 и количество заказов")
    public void getCourierOrdersCount_WithValidId_ShouldReturn200AndOrdersCount() {

        Response response = courierClient.getCourierOrdersCount(createdCourierId);
        courierClient.validateSuccessfulOrdersCount(response, createdCourierId);
        String ordersCount = response.jsonPath().getString("ordersCount");
        assertThat("Orders count should not be null", ordersCount, notNullValue());
        assertThat("Orders count should be a valid number",
                Integer.parseInt(ordersCount), greaterThanOrEqualTo(0));
    }

    @Test
    @DisplayName("Получение количества заказов курьера без ID")
    @Description("Проверка получения количества заказов курьера без ID: должен вернуться код ответа 400")
    public void getCourierOrdersCount_WithEmptyId_ShouldReturn400() {
        String emptyId = "";
        Response response = courierClient.getCourierOrdersCount(emptyId);
        courierClient.validateOrdersCountMissingId(response);

        assertThat("Empty ID should return 400",
                response.statusCode(), equalTo(400));
        assertThat("Error message should indicate missing data for search",
                response.jsonPath().getString("message"), equalTo("Недостаточно данных для поиска"));
    }

    @Test
    @DisplayName("Получение количества заказов курьера с null ID")
    @Description("Проверка получения количества заказов курьера с null ID: должен вернуться код ответа 400")
    public void getCourierOrdersCount_WithNullId_ShouldReturn400() {

        Response response = courierClient.getCourierOrdersCount(null);
        courierClient.validateOrdersCountMissingId(response);
        assertThat("Null ID should return 400",
                response.statusCode(), equalTo(400));
    }

    @Test
    @DisplayName("Получение количества заказов несуществующего курьера")
    @Description("Проверка получения количества заказов несуществующего курьера: должен вернуться код ответа 404")
    public void getCourierOrdersCount_WithNonExistentId_ShouldReturn404() {
        String nonExistentId = "999999";
        Response response = courierClient.getCourierOrdersCount(nonExistentId);
        courierClient.validateOrdersCountNotFound(response);

        assertThat("Non-existent ID should return 404",
                response.statusCode(), equalTo(404));
        assertThat("Error message should indicate courier not found",
                response.jsonPath().getString("message"), equalTo("Курьер не найден"));
    }

    @Test
    @DisplayName("Получение количества заказов удаленного курьера")
    @Description("Проверка получения количества заказов удаленного курьера: должен вернуться код ответа 404")
    public void getCourierOrdersCount_WithDeletedCourier_ShouldReturn404() {
        Response deleteResponse = courierClient.deleteCourier(createdCourierId);
        courierClient.validateSuccessfulDeletion(deleteResponse);

        Response ordersCountResponse = courierClient.getCourierOrdersCount(createdCourierId);

        courierClient.validateOrdersCountNotFound(ordersCountResponse);

        assertThat("Deleted courier should return 404",
                ordersCountResponse.statusCode(), equalTo(404));
    }

    @Test
    @DisplayName("Проверка корректности ID в ответе при получении количества заказов")
    @Description("Проверка что в ответе при получении количества заказов содержится корректный ID курьера")
    public void getCourierOrdersCount_ResponseContainsCorrectId() {
        Response response = courierClient.getCourierOrdersCount(createdCourierId);

        courierClient.validateSuccessfulOrdersCount(response, createdCourierId);

        String responseId = response.jsonPath().getString("id");
        assertThat("Response should contain correct courier ID",
                responseId, equalTo(createdCourierId));
    }

    @Test
    @DisplayName("Проверка что новый курьер имеет 0 заказов")
    @Description("Проверка что новый созданный курьер имеет 0 заказов в системе")
    public void getCourierOrdersCount_NewCourierHasZeroOrders() {
        Response response = courierClient.getCourierOrdersCount(createdCourierId);

        courierClient.validateSuccessfulOrdersCount(response, createdCourierId);

        String ordersCount = response.jsonPath().getString("ordersCount");
        assertThat("New courier should have zero orders",
                ordersCount, equalTo("0"));
    }
}