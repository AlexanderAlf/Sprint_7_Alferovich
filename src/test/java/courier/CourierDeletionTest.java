package courier;

import clients.CourierClient;
import helpers.CourierTestHelper;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import models.Courier;
import org.junit.After;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class CourierDeletionTest {
    private final CourierClient courierClient = new CourierClient();
    private final CourierTestHelper courierHelper = new CourierTestHelper();

    @After
    public void tearDown() {
        courierHelper.cleanupAllTestCouriers();
    }

    @Test
    @DisplayName("Удаление курьера с валидным ID")
    @Description("Проверка успешного удаления курьера с валидным ID: должен вернуться код ответа 200 и ok: true")
    public void deleteCourier_WithValidId_ShouldReturn200AndOkTrue() {
        Courier courier = courierHelper.setupTestCourier();
        String createdCourierId = courierHelper.getCreatedCourierId();
        Response response = courierClient.deleteCourier(createdCourierId);
        courierClient.validateSuccessfulDeletion(response);

        assertThat("Response should contain ok: true",
                response.jsonPath().getBoolean("ok"), is(true));
    }

    @Test
    @DisplayName("Удаление курьера без ID")
    @Description("Проверка удаления курьера без указания ID: должен вернуться код ответа 400")
    public void deleteCourier_WithoutId_ShouldReturn400() {
        String emptyId = "";

        Response response = courierClient.deleteCourier(emptyId);

        courierClient.validateDeletionMissingId(response);

        assertThat("Empty ID deletion should return 400",
                response.statusCode(), equalTo(400));
        assertThat("Error message should indicate missing data for deletion",
                response.jsonPath().getString("message"), equalTo("Недостаточно данных для удаления курьера"));
    }

    @Test
    @DisplayName("Удаление курьера с null ID")
    @Description("Проверка удаления курьера с null ID: должен вернуться код ответа 400")
    public void deleteCourier_WithNullId_ShouldReturn400() {
        Response response = courierClient.deleteCourier(null);

        courierClient.validateDeletionMissingId(response);

        assertThat("Null ID deletion should return 400",
                response.statusCode(), equalTo(400));
    }

    @Test
    @DisplayName("Удаление несуществующего курьера")
    @Description("Проверка удаления несуществующего курьера: должен вернуться код ответа 404")
    public void deleteCourier_WithNonExistentId_ShouldReturn404() {
        String nonExistentId = "999999";

        Response response = courierClient.deleteCourier(nonExistentId);

        courierClient.validateDeletionNotFound(response);

        assertThat("Non-existent ID deletion should return 404",
                response.statusCode(), equalTo(404));
        assertThat("Error message should indicate courier not found",
                response.jsonPath().getString("message"), equalTo("Курьера с таким id нет"));
    }

    @Test
    @DisplayName("Повторное удаление уже удаленного курьера")
    @Description("Проверка повторного удаления уже удаленного курьера: должен вернуться код ответа 404")
    public void deleteCourier_AlreadyDeleted_ShouldReturn404() {
        Courier courier = courierHelper.setupTestCourier();
        String createdCourierId = courierHelper.getCreatedCourierId();

        Response firstDelete = courierClient.deleteCourier(createdCourierId);
        courierClient.validateSuccessfulDeletion(firstDelete);
        Response secondDelete = courierClient.deleteCourier(createdCourierId);
        courierClient.validateDeletionNotFound(secondDelete);

        assertThat("Second deletion should return 404",
                secondDelete.statusCode(), equalTo(404));
        assertThat("Error message should indicate courier not found",
                secondDelete.jsonPath().getString("message"), equalTo("Курьера с таким id нет"));
    }
}