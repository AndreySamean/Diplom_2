import client.SBUserClient;
import io.restassured.response.ValidatableResponse;
import model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static model.constants.UserCredentials.*;
import static org.hamcrest.Matchers.*;

@DisplayName("Тесты на изменение данных пользователя")
public class UpdateUserCredentialsTest {

    private SBUserClient client;
    private static String bearerToken;
    private ValidatableResponse createdUserResponse;
    private User userInitial;
    private User userUpdated;

    @BeforeEach
    public void setUp(){
        client = new SBUserClient();
        userInitial = new User(USER_EMAIL, USER_PASSWORD, USER_NAME);
        createdUserResponse = client.createUser(userInitial);
        bearerToken = client.getBearerToken(createdUserResponse);
    }

    @AfterEach
    public void cleanUp(){
        try {
            ValidatableResponse updatedUser =  client.loginUser(userUpdated);
            bearerToken = client.getBearerToken(updatedUser);
            client.deleteUser(bearerToken);
        } catch (Exception e) {
            bearerToken = client.getBearerToken(createdUserResponse);
            ValidatableResponse initialUser =  client.loginUser(userInitial);
            bearerToken = client.getBearerToken(initialUser);
            client.deleteUser(bearerToken);
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("dataProvider")
    @DisplayName("Изменение данных с авторизацией")
    public void updateUserCredentialsWithAuth_newCredentials_success(String description, String email
            , String password, String name){
        userUpdated = new User(email, password, name);
        ValidatableResponse response = client.changeUserCredentials(userUpdated, bearerToken);

        response.assertThat().statusCode(200)
                .and().body("success", equalTo(true))
                .and().body("user", hasKey("email"))
                .and().body("user.email", equalToIgnoringCase(email))
                .and().body("user", hasKey("name"))
                .and().body("user.name", equalTo(name));

    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("dataProvider")
    @DisplayName("Изменение данных без авторизации")
    public void updateUserCredentialsWithoutAuth_newCredentials_success(String description, String email
            , String password, String name){
        userUpdated = new User(email, password, name);
        ValidatableResponse response = client.changeUserCredentials(userUpdated, null);

        response.assertThat().statusCode(401)
                .and().body("success", equalTo(false))
                .and().body("message", equalTo("You should be authorised"));
    }

    static Stream<Arguments> dataProvider(){
        return Stream.of(
                Arguments.of("Изменили email", USER_NEW_EMAIL, USER_PASSWORD, USER_NAME),
                Arguments.of("Изменили name", USER_EMAIL, USER_PASSWORD, USER_NEW_NAME),
                Arguments.of("Изменили email и name", USER_NEW_EMAIL, USER_PASSWORD, USER_NEW_NAME)
        );
    }
}
