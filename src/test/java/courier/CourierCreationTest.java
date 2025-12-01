package courier;

import clients.CourierClient;
import helpers.CourierTestHelper;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import models.Courier;
import org.junit.After;
import org.junit.Test;
import utils.TestDataGenerator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class CourierCreationTest {
    private final CourierClient courierClient = new CourierClient();
    private final CourierTestHelper courierHelper = new CourierTestHelper();

    @After
    public void tearDown() {
        courierHelper.cleanupAllTestCouriers();
    }

    @Test
    @DisplayName("Создание курьера с валидными данными")
    @Description("Проверка успешного создания курьера с валидными данными: должен вернуться код ответа 201 и ok: true")
    public void createCourier_WithValidData_ShouldReturn201AndOkTrue() {
        Courier courier = TestDataGenerator.generateUniqueCourier();
        Response response = courierClient.createCourier(courier);
        courierClient.validateSuccessfulCreation(response);

        String courierId = courierClient.loginAndGetCourierId(courier);
        courierHelper.registerCourierForCleanup(courierId);

        assertThat("Courier should be successfully created",
                response.statusCode(), equalTo(201));
        assertThat("Response should contain ok: true",
                response.jsonPath().getBoolean("ok"), is(true));
    }

    @Test
    @DisplayName("Создание курьера с дублирующимся логином")
    @Description("Проверка создания курьера с уже существующим логином: должен вернуться код ответа 409")
    public void createCourier_WithDuplicateLogin_ShouldReturn409() {
        // Given
        Courier firstCourier = courierHelper.setupTestCourier();
        Courier duplicateCourier = new Courier(firstCourier.getLogin(), "different_password", "different_name");

        // When
        Response response = courierClient.createCourier(duplicateCourier);

        courierClient.validateCreationDuplicateLogin(response);

        assertThat("Duplicate courier creation should return 409",
                response.statusCode(), equalTo(409));
        assertThat("Error message should indicate login already in use",
                response.jsonPath().getString("message"), equalTo("Этот логин уже используется"));
    }

    @Test
    @DisplayName("Создание курьера без логина")
    @Description("Проверка создания курьера без указания логина: должен вернуться код ответа 400")
    public void createCourier_WithoutLogin_ShouldReturn400() {
        Courier courierWithoutLogin = new Courier(null, "valid_password", "Valid Name");
        Response response = courierClient.createCourier(courierWithoutLogin);
        courierClient.validateCreationMissingFields(response);

        assertThat("Creation without login should return 400",
                response.statusCode(), equalTo(400));
        assertThat("Error message should indicate missing data",
                response.jsonPath().getString("message"), equalTo("Недостаточно данных для создания учетной записи"));
    }

    @Test
    @DisplayName("Создание курьера без пароля")
    @Description("Проверка создания курьера без указания пароля: должен вернуться код ответа 400")
    public void createCourier_WithoutPassword_ShouldReturn400() {
        Courier courierWithoutPassword = new Courier("valid_login", null, "Valid Name");
        Response response = courierClient.createCourier(courierWithoutPassword);
        courierClient.validateCreationMissingFields(response);

        assertThat("Creation without password should return 400",
                response.statusCode(), equalTo(400));
        assertThat("Error message should indicate missing data",
                response.jsonPath().getString("message"), equalTo("Недостаточно данных для создания учетной записи"));
    }

    @Test
    @DisplayName("Создание курьера без имени")
    @Description("Проверка создания курьера без указания имени: должно быть успешное создание с кодом ответа 201")
    public void createCourier_WithoutFirstName_ShouldBeSuccessful() {
        Courier courierWithoutFirstName = new Courier(
                TestDataGenerator.generateUniqueLogin(),
                TestDataGenerator.generateUniquePassword()
        );

        Response response = courierClient.createCourier(courierWithoutFirstName);
        courierClient.validateSuccessfulCreation(response);
        String courierId = courierClient.loginAndGetCourierId(courierWithoutFirstName);
        courierHelper.registerCourierForCleanup(courierId);

        assertThat("Creation without firstName should return 201",
                response.statusCode(), equalTo(201));
        assertThat("Response should contain ok: true",
                response.jsonPath().getBoolean("ok"), is(true));
    }

    @Test
    @DisplayName("Проверка времени ответа при создании курьера")
    @Description("Проверка что время ответа при создании курьера должно быть разумным (менее 5 секунд)")
    public void createCourier_ResponseTimeShouldBeReasonable() {
        Courier courier = TestDataGenerator.generateUniqueCourier();
        Response response = courierClient.createCourier(courier);
        courierClient.validateSuccessfulCreation(response);

        String courierId = courierClient.loginAndGetCourierId(courier);
        courierHelper.registerCourierForCleanup(courierId);

        long responseTime = response.getTime();
        assertThat("Response time should be reasonable (less than 5 seconds)",
                responseTime, lessThan(5000L));
    }

    @Test
    @DisplayName("Создание двух курьеров с разными логинами")
    @Description("Проверка создания двух курьеров с разными логинами: оба должны быть успешно созданы с разными ID")
    public void createCourier_LoginShouldBeUnique() {
        Courier firstCourier = TestDataGenerator.generateUniqueCourier();
        Courier secondCourier = TestDataGenerator.generateUniqueCourier();

        Response firstResponse = courierClient.createCourier(firstCourier);
        Response secondResponse = courierClient.createCourier(secondCourier);

        courierClient.validateSuccessfulCreation(firstResponse);
        courierClient.validateSuccessfulCreation(secondResponse);

        String firstCourierId = courierClient.loginAndGetCourierId(firstCourier);
        String secondCourierId = courierClient.loginAndGetCourierId(secondCourier);

        courierHelper.registerCourierForCleanup(firstCourierId);
        courierHelper.registerCourierForCleanup(secondCourierId);

        assertThat("First courier should be created successfully",
                firstResponse.statusCode(), equalTo(201));
        assertThat("Second courier should be created successfully",
                secondResponse.statusCode(), equalTo(201));
        assertThat("Courier IDs should be different",
                firstCourierId, not(equalTo(secondCourierId)));
    }
}