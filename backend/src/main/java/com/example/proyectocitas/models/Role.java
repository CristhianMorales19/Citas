package com.example.proyectocitas.models;

public enum Role {
    ADMIN("admin"),
    MEDICO("medico"),
    PACIENTE("paciente");

    private final String value;

    Role(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static Role fromValue(String value) {
        for (Role role : Role.values()) {
            if (role.getValue().equals(value)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Invalid role value: " + value);
    }
}
