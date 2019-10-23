package nl.simeonvandersteen.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.MDC.MDCCloseable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Signal;
import reactor.core.publisher.SignalType;
import reactor.util.context.Context;

import java.time.Duration;
import java.util.function.Consumer;

import static nl.simeonvandersteen.demo.ObservabilityFilter.CORRELATION_ID;
import static nl.simeonvandersteen.demo.ObservabilityFilter.REQUEST_ID;

@RestController
@RequestMapping
public class Controller {

    private static final Logger LOG = LoggerFactory.getLogger(Controller.class);

    @GetMapping("/hello")
    public Mono<String> hello() {
        return Mono.just("world\n")
                .doOnEach(logOnNext(s -> LOG.info("hello")))
                .delayElement(Duration.ofMillis(100))
                .doOnEach(logOnNext(s -> LOG.info("world")));
    }

    private static <T> Consumer<Signal<T>> logOnNext(Consumer<T> statement) {
        return signal -> {
            if (signal.getType() != SignalType.ON_NEXT) return;

            Context context = signal.getContext();

            String correlationId = context.get(CORRELATION_ID);
            String requestId = context.get(REQUEST_ID);

            try (MDCCloseable ignored = MDC.putCloseable(CORRELATION_ID, correlationId);
                 MDCCloseable ignored2 = MDC.putCloseable(REQUEST_ID, requestId)) {
                statement.accept(signal.get());
            }
        };
    }
}
