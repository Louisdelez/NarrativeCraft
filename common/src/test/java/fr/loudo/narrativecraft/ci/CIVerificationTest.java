package fr.loudo.narrativecraft.ci;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * CI Verification Test - Used to verify CI pipeline catches failures.
 *
 * This test file is used in the ci/intentional-failure branch to verify
 * that the CI pipeline correctly blocks merges when tests fail.
 *
 * DO NOT MERGE this branch - it exists only for CI verification.
 */
@DisplayName("CI Verification Tests")
class CIVerificationTest {

    @Test
    @DisplayName("CI should pass this test")
    void ciShouldPassThisTest() {
        assertTrue(true, "This test should always pass");
    }

    @Test
    @DisplayName("CI verification - this test should pass in normal branches")
    void verifyTestInfrastructureWorks() {
        assertEquals(4, 2 + 2, "Basic math should work");
    }
}
