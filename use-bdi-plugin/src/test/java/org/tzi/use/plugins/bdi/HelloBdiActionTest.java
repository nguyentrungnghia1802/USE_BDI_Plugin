package org.tzi.use.plugins.bdi;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class HelloBdiActionTest {
    @Test
    void helloActionWorksWithoutLoadedModel() {
        HelloBdiAction action = new HelloBdiAction();

        assertTrue(action.shouldBeEnabled(null));
        assertTrue(HelloBdiAction.createMessage(null).contains("No UML/OCL model"));
    }
}
