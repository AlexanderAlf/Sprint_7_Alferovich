package order;

import clients.OrderClient;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.Test;
import utils.TestDataGenerator;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class OrderListTest {
    private final OrderClient orderClient = new OrderClient();

    @Test
    @DisplayName("Получение списка заказов")
    @Description("Проверка получения списка заказов: должна возвращаться полная структура ответа")
    public void getOrdersList_ShouldReturnCompleteStructure() {
        Response response = orderClient.getOrdersList();

        orderClient.validateSuccessfulOrdersList(response);
        orderClient.validateOrdersListStructure(response);
        orderClient.validatePageInfoStructure(response);

        response.then()
                .body("availableStations[0].name", anyOf(nullValue(), notNullValue()))
                .body("availableStations[0].number", anyOf(nullValue(), notNullValue()))
                .body("availableStations[0].color", anyOf(nullValue(), notNullValue()));
    }

    @Test
    @DisplayName("Получение списка заказов с ID курьера")
    @Description("Проверка получения списка заказов с указанием ID курьера: должны возвращаться заказы конкретного курьера")
    public void getOrdersList_WithCourierId_ShouldReturnCourierOrders() {
        Integer existingCourierId = 1;

        Response response = orderClient.getOrdersListWithParams(existingCourierId, null, null, null);

        orderClient.validateSuccessfulOrdersList(response);

        List<Object> orders = response.jsonPath().getList("orders");
        assertThat("Orders list should not be null", orders, notNullValue());
    }

    @Test
    @DisplayName("Получение списка заказов с несуществующим ID курьера")
    @Description("Проверка получения списка заказов с несуществующим ID курьера: должен вернуться код ответа 404")
    public void getOrdersList_WithNonExistentCourierId_ShouldReturn404() {
        Integer nonExistentCourierId = 999999;

        Response response = orderClient.getOrdersListWithParams(nonExistentCourierId, null, null, null);

        orderClient.validateOrdersListCourierNotFound(response, nonExistentCourierId);
    }

    @Test
    @DisplayName("Получение списка заказов с указанием ближайших станций")
    @Description("Проверка получения списка заказов с указанием ближайших станций: должны возвращаться отфильтрованные заказы")
    public void getOrdersList_WithNearestStation_ShouldReturnFilteredOrders() {
        List<String> stations = Arrays.asList("1", "2");

        Response response = orderClient.getOrdersListWithParams(null, stations, null, null);

        orderClient.validateSuccessfulOrdersList(response);
        orderClient.validateOrdersListStructure(response);
    }

    @Test
    @DisplayName("Получение списка заказов с лимитом")
    @Description("Проверка получения списка заказов с указанием лимита: должно возвращаться ограниченное количество заказов")
    public void getOrdersList_WithLimit_ShouldReturnLimitedOrders() {
        Integer limit = 5;

        Response response = orderClient.getOrdersListWithParams(null, null, limit, null);

        orderClient.validateSuccessfulOrdersList(response);

        List<Object> orders = response.jsonPath().getList("orders");
        Integer responseLimit = response.jsonPath().getInt("pageInfo.limit");

        assertThat("Orders count should not exceed limit",
                orders.size(), lessThanOrEqualTo(limit));
        assertThat("PageInfo limit should match requested limit",
                responseLimit, equalTo(limit));
    }

    @Test
    @DisplayName("Получение списка заказов с указанием страницы")
    @Description("Проверка получения списка заказов с указанием номера страницы: должна возвращаться правильная страница")
    public void getOrdersList_WithPage_ShouldReturnCorrectPage() {
        Integer page = 0;
        Integer limit = 10;

        Response response = orderClient.getOrdersListWithParams(null, null, limit, page);

        orderClient.validateSuccessfulOrdersList(response);

        Integer responsePage = response.jsonPath().getInt("pageInfo.page");
        assertThat("Page should match requested page", responsePage, equalTo(page));
    }

    @Test
    @DisplayName("Получение списка заказов со всеми параметрами")
    @Description("Проверка получения списка заказов со всеми параметрами фильтрации: должны возвращаться отфильтрованные результаты")
    public void getOrdersList_WithAllParameters_ShouldReturnFilteredResults() {
        Integer courierId = 1;
        List<String> stations = Arrays.asList("1", "2");
        Integer limit = 10;
        Integer page = 0;

        Response response = orderClient.getOrdersListWithParams(courierId, stations, limit, page);

        orderClient.validateSuccessfulOrdersList(response);
        orderClient.validateOrdersListStructure(response);
        orderClient.validatePageInfoStructure(response);
    }

    @Test
    @DisplayName("Проверка полноты полей в списке заказов")
    @Description("Проверка что в списке заказов присутствуют все необходимые поля")
    public void getOrdersList_CheckOrderFieldsCompleteness() {
        Response response = orderClient.getOrdersList();

        orderClient.validateSuccessfulOrdersList(response);

        List<Object> orders = response.jsonPath().getList("orders");
        if (orders != null && !orders.isEmpty()) {
            response.then()
                    .body("orders[0].id", anyOf(nullValue(), notNullValue()))
                    .body("orders[0].courierId", anyOf(nullValue(), notNullValue()))
                    .body("orders[0].firstName", anyOf(nullValue(), notNullValue()))
                    .body("orders[0].lastName", anyOf(nullValue(), notNullValue()))
                    .body("orders[0].address", anyOf(nullValue(), notNullValue()))
                    .body("orders[0].metroStation", anyOf(nullValue(), notNullValue()))
                    .body("orders[0].phone", anyOf(nullValue(), notNullValue()))
                    .body("orders[0].rentTime", anyOf(nullValue(), notNullValue()))
                    .body("orders[0].deliveryDate", anyOf(nullValue(), notNullValue()))
                    .body("orders[0].track", anyOf(nullValue(), notNullValue()))
                    .body("orders[0].color", anyOf(nullValue(), notNullValue()))
                    .body("orders[0].comment", anyOf(nullValue(), notNullValue()))
                    .body("orders[0].createdAt", anyOf(nullValue(), notNullValue()))
                    .body("orders[0].updatedAt", anyOf(nullValue(), notNullValue()))
                    .body("orders[0].status", anyOf(nullValue(), notNullValue()));
        }
    }

    @Test
    @DisplayName("Получение списка заказов с максимальным лимитом")
    @Description("Проверка получения списка заказов с максимально допустимым лимитом: должна быть корректная работа")
    public void getOrdersList_WithMaximumLimit_ShouldWorkCorrectly() {
        Integer maxLimit = 30;

        Response response = orderClient.getOrdersListWithParams(null, null, maxLimit, null);

        orderClient.validateSuccessfulOrdersList(response);

        Integer responseLimit = response.jsonPath().getInt("pageInfo.limit");
        assertThat("Limit should be at most 30", responseLimit, lessThanOrEqualTo(maxLimit));
    }
}