package model;

import com.google.gson.Gson;
import lombok.Value;

import java.util.Collections;
import java.util.Map;

@Value
public class User {
    String email;
    String password;
    String name;

    public String getFieldAsJson(String fieldName) {
        String value;

        switch (fieldName) {
            case "email":
                value = this.email;
                break;
            case "password":
                value = this.password;
                break;
            case "name":
                value = this.name;
                break;
            default:
                throw new IllegalArgumentException("Unknown field: " + fieldName);
        }

        Map<String, String> fieldMap = Collections.singletonMap(fieldName, value);
        return new Gson().toJson(fieldMap);
    }
}
