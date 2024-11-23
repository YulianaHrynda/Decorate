package ua.edu.ucu.apps;

import java.time.Duration;
import java.time.Instant;

public class TimedDocument extends AbstractDecorator {

    public TimedDocument(Document document) {
        super(document);
    }

    @Override
    public String parse() {
        Instant startTime = Instant.now();
        String result = super.parse();
        Instant endTime = Instant.now();

        long durationInMillis = Duration.between(startTime, endTime).toMillis();
        System.out.printf("Parse time: %d ms%n", durationInMillis);

        return result;
    }
}
