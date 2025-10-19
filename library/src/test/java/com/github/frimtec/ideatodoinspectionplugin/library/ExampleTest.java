package com.github.frimtec.ideatodoinspectionplugin.library;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExampleTest {
    @Test
    void call() {
        Example fasel = new Example();

        long result = fasel.call();

        assertEquals(0L, result);
    }
}