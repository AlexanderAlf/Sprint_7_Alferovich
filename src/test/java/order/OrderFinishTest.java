package order;

import clients.OrderClient;
import io.restassured.response.Response;
import org.junit.Test;

public class OrderFinishTest {
    private final OrderClient orderClient = new OrderClient();

    @Test
    public void finishOrder_WithValidId_ShouldReturn200AndOkTrue() {
        String validOrderId = "123";
        Response response = orderClient.finishOrder(validOrderId);
        orderClient.validateSuccessfulFinish(response);
    }

    @Test
    public void finishOrder_WithEmptyId_ShouldReturn400() {
        String emptyId = "";

        Response response = orderClient.finishOrder(emptyId);

        orderClient.validateFinishMissingId(response);
    }

    @Test
    public void finishOrder_WithNullId_ShouldReturn400() {
        Response response = orderClient.finishOrder(null);

        orderClient.validateFinishMissingId(response);
    }

    @Test
    public void finishOrder_WithNonExistentOrderId_ShouldReturn404() {
        String nonExistentOrderId = "999999";

        Response response = orderClient.finishOrder(nonExistentOrderId);

        orderClient.validateFinishOrderNotFound(response);
    }

    @Test
    public void finishOrder_WithNonExistentCourier_ShouldReturn404() {
        String orderIdWithNonExistentCourier = "456"; // Должен быть заказ с несуществующим курьером

        Response response = orderClient.finishOrder(orderIdWithNonExistentCourier);

        orderClient.validateFinishCourierNotFound(response);
    }

    @Test
    public void finishOrder_ThatCannotBeFinished_ShouldReturn409() {
        String orderIdThatCannotBeFinished = "789";

        Response response = orderClient.finishOrder(orderIdThatCannotBeFinished);

        orderClient.validateFinishOrderConflict(response);
    }

    @Test
    public void finishOrder_WithInvalidIdFormat_ShouldReturn400() {
        String invalidId = "invalid_id";

        Response response = orderClient.finishOrder(invalidId);

        orderClient.validateFinishMissingId(response);
    }
}