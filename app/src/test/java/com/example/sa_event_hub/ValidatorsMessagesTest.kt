package com.example.sa_event_hub

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

// Checks that each wrong input gives the RIGHT message (as a string resource).
// Because the messages are resources, they are translated automatically.
class ValidatorsMessagesTest {

    // ----- Login -----
    @Test
    fun login_emptyEmail_asksForEmail() {
        assertEquals(R.string.err_enter_email, Validators.checkLogin("", "Password1")?.resId)
    }

    @Test
    fun login_badEmail_saysEmailLooksWrong() {
        assertEquals(R.string.err_email_invalid, Validators.checkLogin("not-an-email", "Password1")?.resId)
    }

    @Test
    fun login_emptyPassword_asksForPassword() {
        assertEquals(R.string.err_enter_password, Validators.checkLogin("a@b.com", "")?.resId)
    }

    @Test
    fun login_goodInput_hasNoProblem() {
        assertNull(Validators.checkLogin("a@b.com", "Password1"))
    }

    // ----- Register -----
    @Test
    fun register_emptyName_asksForName() {
        assertEquals(
            R.string.err_enter_name,
            Validators.checkRegister("", "a@b.com", "Password1", "Password1")?.resId
        )
    }

    @Test
    fun register_shortPassword_showsThePasswordRules() {
        assertEquals(
            R.string.err_password_rules,
            Validators.checkRegister("Thandi", "a@b.com", "short1", "short1")?.resId
        )
    }

    @Test
    fun register_passwordsThatDoNotMatch_areRejected() {
        assertEquals(
            R.string.err_passwords_mismatch,
            Validators.checkRegister("Thandi", "a@b.com", "Password1", "Password2")?.resId
        )
    }

    @Test
    fun register_goodInput_hasNoProblem() {
        assertNull(Validators.checkRegister("Thandi", "a@b.com", "Password1", "Password1"))
    }

    // ----- Change password -----
    @Test
    fun changePassword_emptyCurrent_asksForCurrent() {
        assertEquals(
            R.string.err_enter_current_password,
            Validators.checkPasswordChange("", "Newpass12", "Newpass12")?.resId
        )
    }

    @Test
    fun changePassword_sameAsOld_isRejected() {
        assertEquals(
            R.string.err_new_password_same,
            Validators.checkPasswordChange("Password1", "Password1", "Password1")?.resId
        )
    }

    @Test
    fun changePassword_goodInput_hasNoProblem() {
        assertNull(Validators.checkPasswordChange("Password1", "Newpass12", "Newpass12"))
    }

    // ----- UiText -----
    @Test
    fun uiText_keepsTheValuesToInsert() {
        val message = UiText(R.string.err_search, 502)
        assertEquals(R.string.err_search, message.resId)
        assertEquals(502, message.args[0])
    }
}
