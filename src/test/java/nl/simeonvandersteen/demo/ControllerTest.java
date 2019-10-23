package nl.simeonvandersteen.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;
import reactor.util.context.Context;

import static nl.simeonvandersteen.demo.ObservabilityFilter.CORRELATION_ID;
import static nl.simeonvandersteen.demo.ObservabilityFilter.REQUEST_ID;

class ControllerTest {

    private Controller underTest;

    @BeforeEach
    void setUp() {
        underTest = new Controller();
    }

    @Test
    void itReturnsWorld() {
        Context context = Context.of(REQUEST_ID, "123", CORRELATION_ID, "456");

        StepVerifier.create(underTest.hello().subscriberContext(context))
                .expectNext("world\n")
                .verifyComplete();
    }
}