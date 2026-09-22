package com.example.sa_event_hub

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

// A test runs our code and checks that the answer is what we expect.
class ValidatorsTest {

    @Test
    fun goodEmail_isAccepted() {
        assertTrue(Validators.isEmailValid("thabo@example.co.za"))
    }

    @Test
    fun emailWithoutAt_isRejected() {
        assertFalse(Validators.isEmailValid("thabo.example.com"))
    }

    @Test
    fun shortPassword_isRejected() {
        assertFalse(Validators.isPasswordValid("abc12"))
    }

    @Test
    fun passwordWithoutNumber_isRejected() {
        assertFalse(Validators.isPasswordValid("abcdefgh"))
    }

    @Test
    fun goodPassword_isAccepted() {
        assertTrue(Validators.isPasswordValid("Event2026"))
    }

    @Test
    fun differentPasswords_giveAnError() {
        assertNotNull(Validators.checkRegister("Thabo", "t@e.com", "Event2026", "Event2027"))
    }

    @Test
    fun goodRegisterForm_hasNoError() {
        assertNull(Validators.checkRegister("Thabo", "t@e.com", "Event2026", "Event2026"))
    }

    @Test
    fun emptyEmail_givesAnErrorOnLogin() {
        assertNotNull(Validators.checkLogin("", "Event2026"))
    }

    @Test
    fun passwordChange_sameAsOld_givesAnError() {
        assertNotNull(Validators.checkPasswordChange("Event2026", "Event2026", "Event2026"))
    }

    @Test
    fun passwordChange_mismatch_givesAnError() {
        assertNotNull(Validators.checkPasswordChange("Old12345", "Event2026", "Event2027"))
    }

    @Test
    fun passwordChange_goodInput_hasNoError() {
        assertNull(Validators.checkPasswordChange("Old12345", "Event2026", "Event2026"))
    }
}