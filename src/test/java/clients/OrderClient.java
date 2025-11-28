package clients;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import models.Order;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderClient extends BaseClient {

    @Step("Create order")
    public Response createOrder(Order order) {
        return getBaseRequest()
                .body(order)
                .when()
                .post("orders");
    }

    @Step("Get orders list")
    public Response getOrdersList() {
        return getBaseRequest()
                .when()
                .get("orders");
    }

    @Step("Get orders list with parameters")
    public Response getOrdersListWithParams(Integer courierId, List<String> nearestStation, Integer limit, Integer page) {
        Map<String, Object> params = new HashMap<>();

        if (courierId != null) {
            params.put("courierId", courierId);
        }

        if (nearestStation != null && !nearestStation.isEmpty()) {
            params.put("nearestStation", nearestStation);
        }

        if (limit != null) {
            params.put("limit", limit);
        }

        if (page != null) {
            params.put("page", page);
        }

        return getBaseRequest()
                .queryParams(params)
                .when()
                .get("orders");
    }

    @Step("Get order by track: {track}")
    public Response getOrderByTrack(Integer track) {
        return getBaseRequest()
                .queryParam("t", track)
                .when()
                .get("orders/track");
    }

    @Step("Accept order with id: {orderId} by courier: {courierId}")
    public Response acceptOrder(String orderId, String courierId) {
        return getBaseRequest()
                .queryParam("courierId", courierId)
                .when()
                .put("orders/accept/" + orderId);
    }

    @Step("Finish order with id: {orderId}")
    public Response finishOrder(String orderId) {
        Map<String, Object> requestBody = new HashMap<>();

        try {
            if (orderId != null && !orderId.trim().isEmpty()) {
                requestBody.put("id", Integer.parseInt(orderId));
            } else {
                requestBody.put("id", orderId);
            }
        } catch (NumberFormatException e) {
            requestBody.put("id", orderId);
        }

        return getBaseRequest()
                .body(requestBody)
                .when()
                .put("orders/finish/" + orderId);
    }

    @Step("Cancel order with track: {track}")
    public Response cancelOrder(String track) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("track", track);

        return getBaseRequest()
                .body(requestBody)
                .when()
                .put("orders/cancel");
    }

    @Step("Validate successful creation response")
    public void validateSuccessfulCreation(Response response) {
        validateResponseCode(response, 201, "Create order");
        response.then()
                .body("track", org.hamcrest.Matchers.notNullValue())
                .body("track", org.hamcrest.Matchers.greaterThan(0));
    }

    @Step("Validate creation error - missing required fields")
    public void validateCreationMissingFields(Response response) {
        validateResponseCode(response, 400, "Create order with missing fields");
        response.then().body("message", org.hamcrest.Matchers.equalTo("Недостаточно данных для создания учетной записи"));
    }

    @Step("Validate successful finish response")
    public void validateSuccessfulFinish(Response response) {
        validateResponseCode(response, 200, "Finish order");
        response.then().body("ok", org.hamcrest.Matchers.is(true));
    }

    @Step("Validate finish error - missing id")
    public void validateFinishMissingId(Response response) {
        validateResponseCode(response, 400, "Finish order without id");
        response.then().body("message", org.hamcrest.Matchers.equalTo("Недостаточно данных для поиска"));
    }

    @Step("Validate finish error - order not found")
    public void validateFinishOrderNotFound(Response response) {
        validateResponseCode(response, 404, "Finish non-existent order");
        response.then().body("message", org.hamcrest.Matchers.equalTo("Заказа с таким id не существует"));
    }

    @Step("Validate finish error - courier not found")
    public void validateFinishCourierNotFound(Response response) {
        validateResponseCode(response, 404, "Finish order with non-existent courier");
        response.then().body("message", org.hamcrest.Matchers.equalTo("Курьера с таким id не существует"));
    }

    @Step("Validate finish error - cannot finish order")
    public void validateFinishOrderConflict(Response response) {
        validateResponseCode(response, 409, "Finish order that cannot be finished");
        response.then().body("message", org.hamcrest.Matchers.equalTo("Этот заказ нельзя завершить"));
    }

    @Step("Validate successful cancel response")
    public void validateSuccessfulCancel(Response response) {
        validateResponseCode(response, 200, "Cancel order");
        response.then().body("ok", org.hamcrest.Matchers.is(true));
    }

    @Step("Validate cancel error - missing track")
    public void validateCancelMissingTrack(Response response) {
        validateResponseCode(response, 400, "Cancel order without track");
        response.then().body("message", org.hamcrest.Matchers.equalTo("Недостаточно данных для поиска"));
    }

    @Step("Validate cancel error - order not found")
    public void validateCancelOrderNotFound(Response response) {
        validateResponseCode(response, 404, "Cancel non-existent order");
        response.then().body("message", org.hamcrest.Matchers.equalTo("Заказ не найден"));
    }

    @Step("Validate cancel error - order in progress")
    public void validateCancelOrderInProgress(Response response) {
        validateResponseCode(response, 409, "Cancel order that is in progress");
        response.then().body("message", org.hamcrest.Matchers.equalTo("Этот заказ уже в работе"));
    }

    @Step("Validate successful orders list response")
    public void validateSuccessfulOrdersList(Response response) {
        validateResponseCode(response, 200, "Get orders list");
        response.then()
                .body("orders", org.hamcrest.Matchers.notNullValue())
                .body("pageInfo", org.hamcrest.Matchers.notNullValue())
                .body("availableStations", org.hamcrest.Matchers.notNullValue());
    }

    @Step("Validate orders list error - courier not found")
    public void validateOrdersListCourierNotFound(Response response, Integer courierId) {
        validateResponseCode(response, 404, "Get orders list with non-existent courier");
        response.then().body("message",
                org.hamcrest.Matchers.equalTo("Курьер с идентификатором " + courierId + " не найден"));
    }

    @Step("Validate orders list structure")
    public void validateOrdersListStructure(Response response) {
        validateResponseCode(response, 200, "Get orders list");
        response.then()
                .body("orders[0].id", org.hamcrest.Matchers.anyOf(
                        org.hamcrest.Matchers.nullValue(),
                        org.hamcrest.Matchers.notNullValue()))
                .body("orders[0].firstName", org.hamcrest.Matchers.anyOf(
                        org.hamcrest.Matchers.nullValue(),
                        org.hamcrest.Matchers.notNullValue()))
                .body("orders[0].lastName", org.hamcrest.Matchers.anyOf(
                        org.hamcrest.Matchers.nullValue(),
                        org.hamcrest.Matchers.notNullValue()))
                .body("orders[0].address", org.hamcrest.Matchers.anyOf(
                        org.hamcrest.Matchers.nullValue(),
                        org.hamcrest.Matchers.notNullValue()))
                .body("orders[0].metroStation", org.hamcrest.Matchers.anyOf(
                        org.hamcrest.Matchers.nullValue(),
                        org.hamcrest.Matchers.notNullValue()))
                .body("orders[0].phone", org.hamcrest.Matchers.anyOf(
                        org.hamcrest.Matchers.nullValue(),
                        org.hamcrest.Matchers.notNullValue()))
                .body("orders[0].track", org.hamcrest.Matchers.anyOf(
                        org.hamcrest.Matchers.nullValue(),
                        org.hamcrest.Matchers.notNullValue()))
                .body("orders[0].status", org.hamcrest.Matchers.anyOf(
                        org.hamcrest.Matchers.nullValue(),
                        org.hamcrest.Matchers.notNullValue()));
    }

    @Step("Validate page info structure")
    public void validatePageInfoStructure(Response response) {
        validateResponseCode(response, 200, "Get orders list");
        response.then()
                .body("pageInfo.page", org.hamcrest.Matchers.notNullValue())
                .body("pageInfo.total", org.hamcrest.Matchers.notNullValue())
                .body("pageInfo.limit", org.hamcrest.Matchers.notNullValue());
    }

    @Step("Validate successful get order by track response")
    public void validateSuccessfulGetOrderByTrack(Response response, Integer expectedTrack) {
        validateResponseCode(response, 200, "Get order by track");
        response.then()
                .body("order", org.hamcrest.Matchers.notNullValue())
                .body("order.track", org.hamcrest.Matchers.equalTo(expectedTrack));
    }

    @Step("Validate get order by track error - missing track")
    public void validateGetOrderByTrackMissingTrack(Response response) {
        validateResponseCode(response, 400, "Get order without track");
        response.then().body("message", org.hamcrest.Matchers.equalTo("Недостаточно данных для поиска"));
    }

    @Step("Validate get order by track error - order not found")
    public void validateGetOrderByTrackNotFound(Response response) {
        validateResponseCode(response, 404, "Get non-existent order by track");
        response.then().body("message", org.hamcrest.Matchers.equalTo("Заказ не найден"));
    }

    @Step("Validate order structure by track")
    public void validateOrderStructureByTrack(Response response) {
        validateResponseCode(response, 200, "Get order by track");
        response.then()
                .body("order.id", org.hamcrest.Matchers.notNullValue())
                .body("order.firstName", org.hamcrest.Matchers.anyOf(
                        org.hamcrest.Matchers.nullValue(),
                        org.hamcrest.Matchers.notNullValue()))
                .body("order.lastName", org.hamcrest.Matchers.anyOf(
                        org.hamcrest.Matchers.nullValue(),
                        org.hamcrest.Matchers.notNullValue()))
                .body("order.address", org.hamcrest.Matchers.anyOf(
                        org.hamcrest.Matchers.nullValue(),
                        org.hamcrest.Matchers.notNullValue()))
                .body("order.metroStation", org.hamcrest.Matchers.anyOf(
                        org.hamcrest.Matchers.nullValue(),
                        org.hamcrest.Matchers.notNullValue()))
                .body("order.phone", org.hamcrest.Matchers.anyOf(
                        org.hamcrest.Matchers.nullValue(),
                        org.hamcrest.Matchers.notNullValue()))
                .body("order.rentTime", org.hamcrest.Matchers.anyOf(
                        org.hamcrest.Matchers.nullValue(),
                        org.hamcrest.Matchers.notNullValue()))
                .body("order.deliveryDate", org.hamcrest.Matchers.anyOf(
                        org.hamcrest.Matchers.nullValue(),
                        org.hamcrest.Matchers.notNullValue()))
                .body("order.track", org.hamcrest.Matchers.notNullValue())
                .body("order.status", org.hamcrest.Matchers.anyOf(
                        org.hamcrest.Matchers.nullValue(),
                        org.hamcrest.Matchers.notNullValue()))
                .body("order.color", org.hamcrest.Matchers.anyOf(
                        org.hamcrest.Matchers.nullValue(),
                        org.hamcrest.Matchers.notNullValue()))
                .body("order.comment", org.hamcrest.Matchers.anyOf(
                        org.hamcrest.Matchers.nullValue(),
                        org.hamcrest.Matchers.notNullValue()))
                .body("order.cancelled", org.hamcrest.Matchers.anyOf(
                        org.hamcrest.Matchers.nullValue(),
                        org.hamcrest.Matchers.notNullValue()))
                .body("order.finished", org.hamcrest.Matchers.anyOf(
                        org.hamcrest.Matchers.nullValue(),
                        org.hamcrest.Matchers.notNullValue()))
                .body("order.inDelivery", org.hamcrest.Matchers.anyOf(
                        org.hamcrest.Matchers.nullValue(),
                        org.hamcrest.Matchers.notNullValue()))
                .body("order.courierFirstName", org.hamcrest.Matchers.anyOf(
                        org.hamcrest.Matchers.nullValue(),
                        org.hamcrest.Matchers.notNullValue()))
                .body("order.createdAt", org.hamcrest.Matchers.anyOf(
                        org.hamcrest.Matchers.nullValue(),
                        org.hamcrest.Matchers.notNullValue()))
                .body("order.updatedAt", org.hamcrest.Matchers.anyOf(
                        org.hamcrest.Matchers.nullValue(),
                        org.hamcrest.Matchers.notNullValue()));
    }

    @Step("Validate successful accept order response")
    public void validateSuccessfulAccept(Response response) {
        validateResponseCode(response, 200, "Accept order");
        response.then().body("ok", org.hamcrest.Matchers.is(true));
    }

    @Step("Validate accept error - missing order id")
    public void validateAcceptMissingOrderId(Response response) {
        validateResponseCode(response, 400, "Accept order without order id");
        response.then().body("message", org.hamcrest.Matchers.equalTo("Недостаточно данных для поиска"));
    }

    @Step("Validate accept error - missing courier id")
    public void validateAcceptMissingCourierId(Response response) {
        validateResponseCode(response, 400, "Accept order without courier id");
        response.then().body("message", org.hamcrest.Matchers.equalTo("Недостаточно данных для поиска"));
    }

    @Step("Validate accept error - order not found")
    public void validateAcceptOrderNotFound(Response response) {
        validateResponseCode(response, 404, "Accept non-existent order");
        response.then().body("message", org.hamcrest.Matchers.equalTo("Заказа с таким id не существует"));
    }

    @Step("Validate accept error - courier not found")
    public void validateAcceptCourierNotFound(Response response) {
        validateResponseCode(response, 404, "Accept order with non-existent courier");
        response.then().body("message", org.hamcrest.Matchers.equalTo("Курьера с таким id не существует"));
    }

    @Step("Validate accept error - order already in progress")
    public void validateAcceptOrderInProgress(Response response) {
        validateResponseCode(response, 409, "Accept order that is already in progress");
        response.then().body("message", org.hamcrest.Matchers.equalTo("Этот заказ уже в работе"));
    }
}