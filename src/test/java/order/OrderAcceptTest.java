package order;

import clients.CourierClient;
import clients.OrderClient;
import helpers.CourierTestHelper;
import helpers.OrderTestHelper;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import models.Courier;
import models.Order;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class OrderAcceptTest {
    private final CourierClient courierClient = new CourierClient();
    private final OrderClient orderClient = new OrderClient();
    private final CourierTestHelper courierHelper = new CourierTestHelper();
    private final OrderTestHelper orderHelper = new OrderTestHelper();

    private String testCourierId;
    private String createdOrderId;
    private Integer createdOrderTrack;

    @Before
    public void setUp() {
        Courier courier = courierHelper.setupTestCourier();
        testCourierId = courierHelper.getCreatedCourierId();

        Order order = orderHelper.createDefaultTestOrder();
        createdOrderId = orderHelper.getCurrentOrderId();
        createdOrderTrack = orderHelper.getCurrentOrderTrack();
    }

    @After
    public void tearDown() {
        orderHelper.cancelAllTestOrders();
        courierHelper.cleanupAllTestCouriers();
    }

    @Test
    @DisplayName("Принятие заказа с валидными ID")
    @Description("Проверка принятия заказа с валидными ID заказа и курьера: должен вернуться код ответа 200 и ok: true")
    public void acceptOrder_WithValidIds_ShouldReturn200AndOkTrue() {
        Response response = orderClient.acceptOrder(createdOrderId, testCourierId);

        orderClient.validateSuccessfulAccept(response);

        assertThat("Order acceptance should return 200",
                response.statusCode(), equalTo(200));
        assertThat("Response should contain ok: true",
                response.jsonPath().getBoolean("ok"), is(true));
    }

    @Test
    @DisplayName("Принятие заказа без ID заказа")
    @Description("Проверка принятия заказа без указания ID заказа: должен вернуться код ответа 400")
    public void acceptOrder_WithoutOrderId_ShouldReturn400() {
        String emptyOrderId = "";

        Response response = orderClient.acceptOrder(emptyOrderId, testCourierId);

        orderClient.validateAcceptMissingOrderId(response);
    }

    @Test
    @DisplayName("Принятие заказа без ID курьера")
    @Description("Проверка принятия заказа без указания ID курьера: должен вернуться код ответа 400")
    public void acceptOrder_WithoutCourierId_ShouldReturn400() {
        String emptyCourierId = "";
        Response response = orderClient.acceptOrder(createdOrderId, emptyCourierId);
        orderClient.validateAcceptMissingCourierId(response);
    }

    @Test
    @DisplayName("Принятие заказа с null ID заказа")
    @Description("Проверка принятия заказа с null ID заказа: должен вернуться код ответа 400")
    public void acceptOrder_WithNullOrderId_ShouldReturn400() {
        String nullOrderId = null;
        Response response = orderClient.acceptOrder(nullOrderId, testCourierId);
        orderClient.validateAcceptMissingOrderId(response);
    }

    @Test
    @DisplayName("Принятие заказа с null ID курьера")
    @Description("Проверка принятия заказа с null ID курьера: должен вернуться код ответа 400")
    public void acceptOrder_WithNullCourierId_ShouldReturn400() {
        String nullCourierId = null;
        Response response = orderClient.acceptOrder(createdOrderId, nullCourierId);
        orderClient.validateAcceptMissingCourierId(response);
    }

    @Test
    @DisplayName("Принятие заказа с несуществующим ID заказа")
    @Description("Проверка принятия заказа с несуществующим ID заказа: должен вернуться код ответа 404")
    public void acceptOrder_WithNonExistentOrderId_ShouldReturn404() {
        String nonExistentOrderId = "999999";
        Response response = orderClient.acceptOrder(nonExistentOrderId, testCourierId);
        orderClient.validateAcceptOrderNotFound(response);
    }

    @Test
    @DisplayName("Принятие заказа с несуществующим ID курьера")
    @Description("Проверка принятия заказа с несуществующим ID курьера: должен вернуться код ответа 404")
    public void acceptOrder_WithNonExistentCourierId_ShouldReturn404() {
        String nonExistentCourierId = "888888";
        Response response = orderClient.acceptOrder(createdOrderId, nonExistentCourierId);
        orderClient.validateAcceptCourierNotFound(response);
    }

    @Test
    @DisplayName("Принятие уже принятого заказа")
    @Description("Проверка принятия уже принятого заказа: должен вернуться код ответа 409")
    public void acceptOrder_AlreadyAccepted_ShouldReturn409() {
        Response firstAccept = orderClient.acceptOrder(createdOrderId, testCourierId);
        orderClient.validateSuccessfulAccept(firstAccept);
        Response secondAccept = orderClient.acceptOrder(createdOrderId, testCourierId);
        orderClient.validateAcceptOrderInProgress(secondAccept);
    }

    @Test
    @DisplayName("Принятие заказа и получение информации о заказе")
    @Description("Проверка что после принятия заказа в информации о заказе отображается назначение курьера")
    public void acceptOrder_ThenGetOrder_ShouldShowCourierAssignment() {
        Response acceptResponse = orderClient.acceptOrder(createdOrderId, testCourierId);
        orderClient.validateSuccessfulAccept(acceptResponse);

        Response getResponse = orderClient.getOrderByTrack(createdOrderTrack);

        orderClient.validateSuccessfulGetOrderByTrack(getResponse, createdOrderTrack);

        String courierFirstName = getResponse.jsonPath().getString("order.courierFirstName");
        Boolean inDelivery = getResponse.jsonPath().getBoolean("order.inDelivery");

        assertThat("Courier first name should not be null after acceptance",
                courierFirstName, notNullValue());
        assertThat("Order should be in delivery after acceptance",
                inDelivery, is(true));
    }

    @Test
    @DisplayName("Принятие заказа с невалидным форматом ID заказа")
    @Description("Проверка принятия заказа с невалидным форматом ID заказа: должен вернуться код ответа 400")
    public void acceptOrder_WithInvalidOrderIdFormat_ShouldReturn400() {
        String invalidOrderId = "invalid_id";
        Response response = orderClient.acceptOrder(invalidOrderId, testCourierId);
        orderClient.validateAcceptMissingOrderId(response);
    }

    @Test
    @DisplayName("Принятие заказа с невалидным форматом ID курьера")
    @Description("Проверка принятия заказа с невалидным форматом ID курьера: должен вернуться код ответа 400")
    public void acceptOrder_WithInvalidCourierIdFormat_ShouldReturn400() {
        String invalidCourierId = "invalid_courier_id";
        Response response = orderClient.acceptOrder(createdOrderId, invalidCourierId);
        orderClient.validateAcceptMissingCourierId(response);
    }

    @Test
    @DisplayName("Принятие заказа и его завершение")
    @Description("Проверка корректной последовательности действий: принятие заказа и его последующее завершение")
    public void acceptOrder_ThenFinish_ShouldWorkCorrectly() {
        Response acceptResponse = orderClient.acceptOrder(createdOrderId, testCourierId);
        orderClient.validateSuccessfulAccept(acceptResponse);
        Response finishResponse = orderClient.finishOrder(createdOrderId);
        orderClient.validateSuccessfulFinish(finishResponse);
    }

    @Test
    @DisplayName("Принятие отмененного заказа")
    @Description("Проверка принятия отмененного заказа: должен вернуться код ответа 404")
    public void acceptOrder_CancelledOrder_ShouldReturn404() {
        Response cancelResponse = orderClient.cancelOrder(String.valueOf(createdOrderTrack));
        orderClient.validateSuccessfulCancel(cancelResponse);
        Response acceptResponse = orderClient.acceptOrder(createdOrderId, testCourierId);
        orderClient.validateAcceptOrderNotFound(acceptResponse);
    }
}