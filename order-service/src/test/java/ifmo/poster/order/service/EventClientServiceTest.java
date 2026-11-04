package ifmo.poster.order.service;

import static org.mockito.Mockito.verify;

import ifmo.poster.order.client.EventClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class EventClientServiceTest {

    @Mock
    private EventClient eventClient;

    @Test
    void completesAfterReservationIsCancelled() {
        EventClientService service = new EventClientService(eventClient);

        StepVerifier.create(service.cancelReservation("reservation-42"))
                .verifyComplete();

        verify(eventClient).cancelReservation("reservation-42");
    }
}
