package ifmo.poster.order.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ifmo.poster.order.client.EventClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class EventClientServiceTest {

    @Mock
    private EventClient eventClient;

    @Test
    void completesAfterReservationIsCancelled() {
        when(eventClient.cancelReservation("reservation-42")).thenReturn(Mono.empty());
        EventClientService service = new EventClientService(eventClient);

        StepVerifier.create(service.cancelReservation("reservation-42"))
                .verifyComplete();

        verify(eventClient).cancelReservation("reservation-42");
    }
}
