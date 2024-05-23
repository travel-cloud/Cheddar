package com.clicktravel.common.security;

import static com.clicktravel.common.random.Randoms.randomString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;


public class SanitiserTest {


    @Test
    public void shouldSanitise() {
        // Given
        final String value = randomString(10);

        // When
        final String sanitisedValue = Sanitiser.sanitiseValue(value + "=");

        // Then
        assertEquals(value + "&#61;", sanitisedValue);
    }

    @Test
    public void shouldSanitise_multipleSuspectCharacters() {
        // Given
        final String value = randomString(10);

        // When
        final String sanitisedValue = Sanitiser.sanitiseValue("==" + value);

        // Then
        assertEquals(value, sanitisedValue);
    }

    @Test
    public void shouldSanitise_onlySuspectCharacters() {
        // Given
        final String value = "==";

        // When
        final String sanitisedValue = Sanitiser.sanitiseValue(value);

        // Then
        assertNotNull(
                "Expected the filter against suspect character to return empty string. Please check how we are doing this",
                sanitisedValue);
        assertTrue(
                "Expected the sanitised value to be an empty string, please check how we are removing any suspect characters",
                sanitisedValue.isEmpty());
    }

    @Test
    public void shouldSanitise_withValueHavingXSSAttackPrevented() {
        // Given
        final String value = "<a href='https://www.example.com/' onclick='alert(\"XSS Attack\")'>Click Me</a>";

        // When
        final String sanitisedValue = Sanitiser.sanitiseValue(value);

        // Then
        assertEquals("Click Me", sanitisedValue);
    }

    @Test
    public void shouldSanitise_withApostropheInValue() {
        // Given
        final String value = "O'Test";

        // When
        final String sanitisedValue = Sanitiser.sanitiseValue(value);

        // Then
        assertEquals("O'Test", sanitisedValue);
    }
}