package model;

import lombok.Value;

@Value
public class User {
    String email;
    String password;
    String name;
}
