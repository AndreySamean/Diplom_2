import client.SBUserClient;
import io.restassured.response.ValidatableResponse;
import model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static model.constants.UserCredentials.*;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.hasKey;

@DisplayName("Тесты на авторизацию пользователя")
public class LoginUserTest {

    private SBUserClient client;
    private User user;
    private static String bearerToken;

    @BeforeEach
    public void setUp(){
        client = new SBUserClient();
        user = new User(USER_EMAIL, USER_PASSWORD, USER_NAME);
        ValidatableResponse createdUserResponse = client.createUser(user);
        bearerToken = client.getBearerToken(createdUserResponse);
    }

    @AfterEach
    public void cleanUp(){
            client.deleteUser(bearerToken);
    }

    @Test
    @DisplayName("Авторизация пользователя - успех")
    public void loginUser_success(){
        ValidatableResponse response = client.loginUser(user);

        response.assertThat().statusCode(200)
                .and().body("success", equalTo(true))
                .and().body("accessToken", notNullValue())
                .and().body("refreshToken", notNullValue())
                .and().body("user", hasKey("email"))
                .and().body("user.email", equalTo(USER_EMAIL))
                .and().body("user", hasKey("name"))
                .and().body("user.name", equalTo(USER_NAME));
    }

    @Test
    @DisplayName("Авторизация пользователя с неверным логином и паролем - ошибка")
    public void loginUser_invalidEmailAndPassword_expectError(){
        User userInvalidCredentials =
                new User(USER_INVALID_EMAIL, USER_INVALID_PASSWORD, USER_NAME);
        ValidatableResponse response = client.loginUser(userInvalidCredentials);

        response.assertThat().statusCode(401)
                .and().body("success", equalTo(false))
                .and().body("message", equalTo("email or password are incorrect"));
    }
}
