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

public class OrderCancelTest {
    private final OrderClient orderClient = new OrderClient();
    private String createdOrderTrack;

    @After
    public void tearDown() {
        if (createdOrderTrack != null) {
            try {
                orderClient.cancelOrder(createdOrderTrack);
            } catch (Exception e) {
            }
        }
    }

    @Test
    @DisplayName("Отмена заказа с валидным track номером")
    @Description("Проверка отмены заказа с валидным track номером: должен вернуться код ответа 200 и ok: true")
    public void cancelOrder_WithValidTrack_ShouldReturn200AndOkTrue() {
        Order order = TestDataGenerator.generateOrderWithColors(Collections.singletonList("BLACK"));
        Response createResponse = orderClient.createOrder(order);
        orderClient.validateSuccessfulCreation(createResponse);
        createdOrderTrack = String.valueOf(createResponse.jsonPath().getInt("track"));

        Response cancelResponse = orderClient.cancelOrder(createdOrderTrack);

        orderClient.validateSuccessfulCancel(cancelResponse);
    }

    @Test
    @DisplayName("Отмена заказа с пустым track номером")
    @Description("Проверка отмены заказа с пустым track номером: должен вернуться код ответа 400")
    public void cancelOrder_WithEmptyTrack_ShouldReturn400() {
        String emptyTrack = "";
        Response response = orderClient.cancelOrder(emptyTrack);
        orderClient.validateCancelMissingTrack(response);
    }

    @Test
    @DisplayName("Отмена заказа с null track номером")
    @Description("Проверка отмены заказа с null track номером: должен вернуться код ответа 400")
    public void cancelOrder_WithNullTrack_ShouldReturn400() {
        Response response = orderClient.cancelOrder(null);

        orderClient.validateCancelMissingTrack(response);
    }

    @Test
    @DisplayName("Отмена заказа с несуществующим track номером")
    @Description("Проверка отмены заказа с несуществующим track номером: должен вернуться код ответа 404")
    public void cancelOrder_WithNonExistentTrack_ShouldReturn404() {
        String nonExistentTrack = "999999";

        Response response = orderClient.cancelOrder(nonExistentTrack);

        orderClient.validateCancelOrderNotFound(response);
    }

    @Test
    @DisplayName("Отмена заказа с невалидным форматом track номера")
    @Description("Проверка отмены заказа с невалидным форматом track номера: должен вернуться код ответа 400")
    public void cancelOrder_WithInvalidTrackFormat_ShouldReturn400() {
        String invalidTrack = "invalid_track";
        Response response = orderClient.cancelOrder(invalidTrack);
        orderClient.validateCancelMissingTrack(response);
    }

    @Test
    @DisplayName("Повторная отмена уже отмененного заказа")
    @Description("Проверка повторной отмены уже отмененного заказа: должен вернуться код ответа 404")
    public void cancelOrder_AlreadyCancelled_ShouldReturn404() {
        Order order = TestDataGenerator.generateOrderWithColors(Collections.singletonList("GREY"));
        Response createResponse = orderClient.createOrder(order);
        orderClient.validateSuccessfulCreation(createResponse);
        String track = String.valueOf(createResponse.jsonPath().getInt("track"));

        Response firstCancel = orderClient.cancelOrder(track);
        orderClient.validateSuccessfulCancel(firstCancel);

        Response secondCancel = orderClient.cancelOrder(track);

        orderClient.validateCancelOrderNotFound(secondCancel);
    }

    @Test
    @DisplayName("Отмена заказа в процессе выполнения")
    @Description("Проверка отмены заказа который находится в процессе выполнения: должен вернуться код ответа 409")
    public void cancelOrder_InProgress_ShouldReturn409() {
        String orderTrackInProgress = "123456";

        Response response = orderClient.cancelOrder(orderTrackInProgress);

        orderClient.validateCancelOrderInProgress(response);
    }

    @Test
    @DisplayName("Отмена заказа")
    @Description("Проверка структуры ответа при отмене заказа")
    public void cancelOrder_ResponseHasCorrectStructure() {
        Order order = TestDataGenerator.generateDefaultOrder();
        Response createResponse = orderClient.createOrder(order);
        orderClient.validateSuccessfulCreation(createResponse);
        String track = String.valueOf(createResponse.jsonPath().getInt("track"));

        Response cancelResponse = orderClient.cancelOrder(track);

        orderClient.validateSuccessfulCancel(cancelResponse);

        Boolean okField = cancelResponse.jsonPath().getBoolean("ok");
        assertThat("ok field should be true", okField, is(true));
    }
}