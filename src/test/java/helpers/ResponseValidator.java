package helpers;

import io.restassured.response.Response;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ResponseValidator {

    public static void validateSuccessResponse(Response response, int expectedCode, String operation) {
        int actualCode = response.statusCode();
        assertThat(
                String.format("Operation '%s' failed: expected status %d but got %d",
                        operation, expectedCode, actualCode),
                actualCode,
                equalTo(expectedCode)
        );
    }

    public static void validateErrorResponse(Response response, int expectedCode, String expectedMessage) {
        validateSuccessResponse(response, expectedCode, "Error operation");

        String actualMessage = response.jsonPath().getString("message");
        assertThat(
                String.format("Error message mismatch: expected '%s' but got '%s'",
                        expectedMessage, actualMessage),
                actualMessage,
                equalTo(expectedMessage)
        );
    }

    public static void validateResponseContainsField(Response response, String fieldName) {
        Object fieldValue = response.jsonPath().get(fieldName);
        assertThat(
                String.format("Response should contain field '%s'", fieldName),
                fieldValue,
                notNullValue()
        );
    }

    public static void validateBooleanField(Response response, String fieldName, boolean expectedValue) {
        Boolean actualValue = response.jsonPath().getBoolean(fieldName);
        assertThat(
                String.format("Field '%s' should be %s but was %s",
                        fieldName, expectedValue, actualValue),
                actualValue,
                equalTo(expectedValue)
        );
    }

    public static void validateListIsNotEmpty(Response response, String fieldPath) {
        java.util.List<?> list = response.jsonPath().getList(fieldPath);
        assertThat(
                String.format("Field '%s' should not be empty", fieldPath),
                list,
                not(empty())
        );
    }

    public static void validateFieldEquals(Response response, String fieldPath, Object expectedValue) {
        Object actualValue = response.jsonPath().get(fieldPath);
        assertThat(
                String.format("Field '%s' should equal '%s' but was '%s'",
                        fieldPath, expectedValue, actualValue),
                actualValue,
                equalTo(expectedValue)
        );
    }

    public static void validateFieldMatchesPattern(Response response, String fieldPath, String pattern) {
        String actualValue = response.jsonPath().getString(fieldPath);
        assertThat(
                String.format("Field '%s' should match pattern '%s' but was '%s'",
                        fieldPath, pattern, actualValue),
                actualValue,
                matchesPattern(pattern)
        );
    }
}