package courier;

import clients.CourierClient;
import helpers.CourierTestHelper;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import models.Courier;
import models.CourierCredentials;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import utils.TestDataGenerator;

public class CourierLoginTest {
    private final CourierClient courierClient = new CourierClient();
    private final CourierTestHelper courierHelper = new CourierTestHelper();
    private Courier existingCourier;
    private String existingCourierId;

    @Before
    public void setUp() {
        existingCourier = courierHelper.setupTestCourier();
        existingCourierId = courierHelper.getCreatedCourierId();
    }

    @After
    public void tearDown() {
        courierHelper.cleanupTestCourier();
    }

    @Test
    @DisplayName("Логин курьера с валидными учетными данными")
    @Description("Проверка успешного логина курьера с валидными учетными данными: должен вернуться код ответа 200 и ID курьера")
    public void loginCourier_WithValidCredentials_ShouldReturn200AndId() {
        CourierCredentials validCredentials = new CourierCredentials(existingCourier);
        Response response = courierClient.loginCourier(validCredentials);
        courierClient.validateSuccessfulLogin(response);
    }

    @Test
    @DisplayName("Логин курьера с неверным паролем")
    @Description("Проверка логина курьера с неверным паролем: должен вернуться код ответа 404")
    public void loginCourier_WithWrongPassword_ShouldReturn404() {
        CourierCredentials wrongCredentials = new CourierCredentials(existingCourier.getLogin(), "wrong_password");
        Response response = courierClient.loginCourier(wrongCredentials);
        courierClient.validateLoginAccountNotFound(response);
    }

    @Test
    @DisplayName("Логин курьера с неверным логином")
    @Description("Проверка логина курьера с неверным логином: должен вернуться код ответа 404")
    public void loginCourier_WithWrongLogin_ShouldReturn404() {
        CourierCredentials wrongCredentials = new CourierCredentials("nonexistent_login", existingCourier.getPassword());
        Response response = courierClient.loginCourier(wrongCredentials);
        courierClient.validateLoginAccountNotFound(response);
    }

    @Test
    @DisplayName("Логин курьера без логина")
    @Description("Проверка логина курьера без указания логина: должен вернуться код ответа 400")
    public void loginCourier_WithoutLogin_ShouldReturn400() {
        CourierCredentials credentialsWithoutLogin = new CourierCredentials(null, existingCourier.getPassword());
        Response response = courierClient.loginCourier(credentialsWithoutLogin);
        courierClient.validateLoginMissingFields(response);
    }

    @Test
    @DisplayName("Логин курьера без пароля")
    @Description("Проверка логина курьера без указания пароля: должен вернуться код ответа 400")
    public void loginCourier_WithoutPassword_ShouldReturn400() {
        CourierCredentials credentialsWithoutPassword = new CourierCredentials(existingCourier.getLogin(), null);
        Response response = courierClient.loginCourier(credentialsWithoutPassword);
        courierClient.validateLoginMissingFields(response);
    }

    @Test
    @DisplayName("Логин несуществующего курьера")
    @Description("Проверка логина несуществующего курьера: должен вернуться код ответа 404")
    public void loginCourier_WithNonExistentUser_ShouldReturn404() {
        Courier nonExistentCourier = TestDataGenerator.generateUniqueCourier();
        CourierCredentials nonExistentCredentials = new CourierCredentials(nonExistentCourier);
        Response response = courierClient.loginCourier(nonExistentCredentials);
        courierClient.validateLoginAccountNotFound(response);
    }
}