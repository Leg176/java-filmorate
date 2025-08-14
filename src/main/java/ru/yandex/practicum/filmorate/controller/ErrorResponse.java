package ru.yandex.practicum.filmorate.controller;

public class ErrorResponse {
    // название ошибки
    String error;

    public ErrorResponse(String error) {
        this.error = error;
    }

    // геттеры необходимы, чтобы Spring Boot мог получить значения полей
    public String getError() {
        return error;
    }
}
