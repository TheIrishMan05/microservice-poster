package ifmo.poster.order.client;

import feign.FeignException;
import ifmo.poster.contracts.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactivefeign.client.ReactiveFeignException;

public final class RemoteCallErrors {

    private RemoteCallErrors() {
    }

    public static boolean isNotFound(Throwable cause) {
        Throwable current = cause;
        while (current != null) {
            if (current instanceof ResourceNotFoundException) {
                return true;
            }
            if (current instanceof FeignException feignException && isNotFoundStatus(feignException.status())) {
                return true;
            }
            if (current instanceof WebClientResponseException webClientException
                    && isNotFoundStatus(webClientException.getStatusCode().value())) {
                return true;
            }
            if (current instanceof ReactiveFeignException reactiveFeignException) {
                Integer status = extractStatus(reactiveFeignException);
                if (status != null && isNotFoundStatus(status)) {
                    return true;
                }
            }
            current = current.getCause();
        }
        return false;
    }

    public static Throwable mapNotFound(Throwable cause, String notFoundMessage) {
        if (cause instanceof ResourceNotFoundException) {
            return cause;
        }
        if (isNotFound(cause)) {
            return new ResourceNotFoundException(notFoundMessage);
        }
        return cause;
    }

    private static boolean isNotFoundStatus(int status) {
        return status == 404;
    }

    private static Integer extractStatus(ReactiveFeignException exception) {
        Throwable cause = exception.getCause();
        if (cause instanceof WebClientResponseException webClientException) {
            HttpStatusCode status = webClientException.getStatusCode();
            return status.value();
        }
        if (cause instanceof FeignException feignException) {
            return feignException.status();
        }
        return null;
    }
}
