package order;

import clients.OrderClient;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.Test;

public class OrderFinishTest {
    private final OrderClient orderClient = new OrderClient();

    @Test
    @DisplayName("Завершение заказа с валидным ID")
    @Description("Проверка завершения заказа с валидным ID: должен вернуться код ответа 200 и ok: true")
    public void finishOrder_WithValidId_ShouldReturn200AndOkTrue() {
        String validOrderId = "123";
        Response response = orderClient.finishOrder(validOrderId);
        orderClient.validateSuccessfulFinish(response);
    }

    @Test
    @DisplayName("Завершение заказа с пустым ID")
    @Description("Проверка завершения заказа с пустым ID: должен вернуться код ответа 400")
    public void finishOrder_WithEmptyId_ShouldReturn400() {
        String emptyId = "";

        Response response = orderClient.finishOrder(emptyId);

        orderClient.validateFinishMissingId(response);
    }

    @Test
    @DisplayName("Завершение заказа с null ID")
    @Description("Проверка завершения заказа с null ID: должен вернуться код ответа 400")
    public void finishOrder_WithNullId_ShouldReturn400() {
        Response response = orderClient.finishOrder(null);

        orderClient.validateFinishMissingId(response);
    }

    @Test
    @DisplayName("Завершение заказа с несуществующим ID")
    @Description("Проверка завершения заказа с несуществующим ID: должен вернуться код ответа 404")
    public void finishOrder_WithNonExistentOrderId_ShouldReturn404() {
        String nonExistentOrderId = "999999";

        Response response = orderClient.finishOrder(nonExistentOrderId);

        orderClient.validateFinishOrderNotFound(response);
    }

    @Test
    @DisplayName("Завершение заказа с несуществующим курьером")
    @Description("Проверка завершения заказа с несуществующим курьером: должен вернуться код ответа 404")
    public void finishOrder_WithNonExistentCourier_ShouldReturn404() {
        String orderIdWithNonExistentCourier = "456";

        Response response = orderClient.finishOrder(orderIdWithNonExistentCourier);

        orderClient.validateFinishCourierNotFound(response);
    }

    @Test
    @DisplayName("Завершение заказа который нельзя завершить")
    @Description("Проверка завершения заказа который нельзя завершить: должен вернуться код ответа 409")
    public void finishOrder_ThatCannotBeFinished_ShouldReturn409() {
        String orderIdThatCannotBeFinished = "789";

        Response response = orderClient.finishOrder(orderIdThatCannotBeFinished);

        orderClient.validateFinishOrderConflict(response);
    }

    @Test
    @DisplayName("Завершение заказа с невалидным форматом ID")
    @Description("Проверка завершения заказа с невалидным форматом ID: должен вернуться код ответа 400")
    public void finishOrder_WithInvalidIdFormat_ShouldReturn400() {
        String invalidId = "invalid_id";

        Response response = orderClient.finishOrder(invalidId);

        orderClient.validateFinishMissingId(response);
    }
}