package order;

import clients.OrderClient;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import models.Order;
import org.junit.After;
import org.junit.Test;
import utils.TestDataGenerator;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class OrderGetByTrackTest {
    private final OrderClient orderClient = new OrderClient();
    private Integer createdOrderTrack;

    @After
    public void tearDown() {
        if (createdOrderTrack != null) {
            try {
                orderClient.cancelOrder(String.valueOf(createdOrderTrack));
            } catch (Exception e) {
            }
        }
    }

    @Test
    @DisplayName("Получение заказа по валидному track номеру")
    @Description("Проверка получения заказа по валидному track номеру: должны возвращаться детали заказа")
    public void getOrderByTrack_WithValidTrack_ShouldReturnOrderDetails() {
        Order order = TestDataGenerator.generateOrderWithColors(Collections.singletonList("BLACK"));
        Response createResponse = orderClient.createOrder(order);
        orderClient.validateSuccessfulCreation(createResponse);
        createdOrderTrack = createResponse.jsonPath().getInt("track");

        Response getResponse = orderClient.getOrderByTrack(createdOrderTrack);

        orderClient.validateSuccessfulGetOrderByTrack(getResponse, createdOrderTrack);
        orderClient.validateOrderStructureByTrack(getResponse);
    }

    @Test
    @DisplayName("Получение заказа без track номера")
    @Description("Проверка получения заказа без указания track номера: должен вернуться код ответа 400")
    public void getOrderByTrack_WithoutTrack_ShouldReturn400() {
        Response response = orderClient.getOrderByTrack(null);

        orderClient.validateGetOrderByTrackMissingTrack(response);
    }

    @Test
    @DisplayName("Получение заказа по несуществующему track номеру")
    @Description("Проверка получения заказа по несуществующему track номеру: должен вернуться код ответа 404")
    public void getOrderByTrack_WithNonExistentTrack_ShouldReturn404() {
        Integer nonExistentTrack = 999999;

        Response response = orderClient.getOrderByTrack(nonExistentTrack);

        orderClient.validateGetOrderByTrackNotFound(response);
    }

    @Test
    @DisplayName("Получение заказа по track номеру")
    @Description("Проверка что ответ содержит корректные данные заказа при получении по track номеру")
    public void getOrderByTrack_ResponseContainsCorrectOrderData() {
        Order originalOrder = TestDataGenerator.generateOrderWithAllFields();
        Response createResponse = orderClient.createOrder(originalOrder);
        orderClient.validateSuccessfulCreation(createResponse);
        createdOrderTrack = createResponse.jsonPath().getInt("track");

        Response getResponse = orderClient.getOrderByTrack(createdOrderTrack);

        orderClient.validateSuccessfulGetOrderByTrack(getResponse, createdOrderTrack);

        String responseFirstName = getResponse.jsonPath().getString("order.firstName");
        String responseLastName = getResponse.jsonPath().getString("order.lastName");
        String responseAddress = getResponse.jsonPath().getString("order.address");
        String responsePhone = getResponse.jsonPath().getString("order.phone");

        assertThat("First name should not be null", responseFirstName, notNullValue());
        assertThat("Last name should not be null", responseLastName, notNullValue());
        assertThat("Address should not be null", responseAddress, notNullValue());
        assertThat("Phone should not be null", responsePhone, notNullValue());
    }

    @Test
    @DisplayName("Проверка наличия всех полей в заказе")
    @Description("Проверка что при получении заказа по track номеру присутствуют все поля")
    public void getOrderByTrack_CheckAllFieldsPresence() {
        Order order = TestDataGenerator.generateDefaultOrder();
        Response createResponse = orderClient.createOrder(order);
        orderClient.validateSuccessfulCreation(createResponse);
        createdOrderTrack = createResponse.jsonPath().getInt("track");

        Response getResponse = orderClient.getOrderByTrack(createdOrderTrack);

        orderClient.validateSuccessfulGetOrderByTrack(getResponse, createdOrderTrack);

        getResponse.then()
                .body("order.id", notNullValue())
                .body("order.track", notNullValue())
                .body("order.cancelled", anyOf(nullValue(), notNullValue()))
                .body("order.finished", anyOf(nullValue(), notNullValue()))
                .body("order.inDelivery", anyOf(nullValue(), notNullValue()))
                .body("order.createdAt", anyOf(nullValue(), notNullValue()))
                .body("order.updatedAt", anyOf(nullValue(), notNullValue()));
    }

    @Test
    @DisplayName("Получение отмененного заказа по track номеру")
    @Description("Проверка получения отмененного заказа по track номеру: заказ должен быть отмечен как отмененный")
    public void getOrderByTrack_AfterCancellation_ShouldReturnCancelledOrder() {
        Order order = TestDataGenerator.generateDefaultOrder();
        Response createResponse = orderClient.createOrder(order);
        orderClient.validateSuccessfulCreation(createResponse);
        createdOrderTrack = createResponse.jsonPath().getInt("track");

        Response cancelResponse = orderClient.cancelOrder(String.valueOf(createdOrderTrack));
        orderClient.validateSuccessfulCancel(cancelResponse);

        Response getResponse = orderClient.getOrderByTrack(createdOrderTrack);

        orderClient.validateSuccessfulGetOrderByTrack(getResponse, createdOrderTrack);

        Boolean cancelled = getResponse.jsonPath().getBoolean("order.cancelled");
        assertThat("Cancelled order should have cancelled=true", cancelled, is(true));
    }

    @Test
    @DisplayName("Получение заказа по track номеру 0")
    @Description("Проверка получения заказа по track номеру 0: должен вернуться код ответа 404")
    public void getOrderByTrack_WithZeroTrack_ShouldReturnError() {
        Integer zeroTrack = 0;

        Response response = orderClient.getOrderByTrack(zeroTrack);

        orderClient.validateGetOrderByTrackNotFound(response);
    }

    @Test
    @DisplayName("Получение заказа по отрицательному track номеру")
    @Description("Проверка получения заказа по отрицательному track номеру: должен вернуться код ответа 404")
    public void getOrderByTrack_WithNegativeTrack_ShouldReturnError() {
        Integer negativeTrack = -123;

        Response response = orderClient.getOrderByTrack(negativeTrack);

        orderClient.validateGetOrderByTrackNotFound(response);
    }
}