package ifmo.poster.event.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "event")
public record EventProperties(
        Inventory inventory,
        Reservation reservation
) {
    public record Inventory(int defaultSeatsPerTicketType) {
    }

    public record Reservation(int minQuantity, int maxQuantity) {
    }
}
