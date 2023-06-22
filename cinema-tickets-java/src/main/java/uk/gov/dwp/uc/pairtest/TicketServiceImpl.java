package uk.gov.dwp.uc.pairtest;

import jakarta.validation.Valid;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

@Component
public class TicketServiceImpl implements TicketService {
    private static final Validator validator =
            Validation.byDefaultProvider()
                    .configure()
                    .messageInterpolator(new ParameterMessageInterpolator())
                    .buildValidatorFactory()
                    .getValidator();
    /**
     * Should only have private methods other than the one below.
     */

    @Autowired
    private TicketPaymentService ticketPaymentService;
    @Autowired
    private SeatReservationService seatReservationService;

    @Override
    public void purchaseTickets(Long accountId, @Valid TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {
        validateAccountId(accountId); // null or invalid account id
        validateTicketTypeRequests(ticketTypeRequests);
        Map<TicketTypeRequest.Type, TicketTypeRequest> ticketMap = Arrays.stream(ticketTypeRequests)
                .filter(ticketTypeRequest -> Objects.nonNull(ticketTypeRequest.getTicketType()))
                .collect(toMap(TicketTypeRequest::getTicketType, Function.identity(), TicketTypeRequest::add));
        int adultCount = getCount(ticketMap, TicketTypeRequest.Type.ADULT);
        int childCount = getCount(ticketMap, TicketTypeRequest.Type.CHILD);
        int infantCount = getCount(ticketMap, TicketTypeRequest.Type.INFANT);
        if (childCount > 0 && adultCount == 0) { // child tickets can be purchased only if there is an adult accompanying
            throw new InvalidPurchaseException("Child tickets cannot be purchased without adult ticket");
        } else if (infantCount > adultCount) { // infants to sit on the laps of adults
            throw new InvalidPurchaseException("Number of adults less than infants");
        } else if (adultCount == 0) { // no adults means
            throw new InvalidPurchaseException("No adult tickets purchased");
        } else if (adultCount + childCount + infantCount > 20) {
            throw new InvalidPurchaseException("Exceeded limit on number of tickets");
        }
        seatReservationService.reserveSeat(accountId, adultCount + childCount);
        ticketPaymentService.makePayment(accountId, adultCount * 20 + childCount * 10);
    }

    private int getCount(Map<TicketTypeRequest.Type, TicketTypeRequest> ticketMap, TicketTypeRequest.Type type) {
        int ticketCount = 0;
        if (ticketMap.containsKey(type)) {
            ticketCount = Optional.ofNullable(ticketMap.get(type)).orElse(new TicketTypeRequest(null, 0)).getNoOfTickets();
            if (ticketCount < 0) {
                throw new InvalidPurchaseException(String.format("%s count cannot be negative", type));
            }
        }
        return ticketCount;
    }

    private void validateTicketTypeRequests(TicketTypeRequest... ticketTypeRequests) {
        if (null == ticketTypeRequests || ticketTypeRequests.length == 0) {
            throw new InvalidPurchaseException("Ticket request cannot be null or empty");
        }
        // TODO : Ticket type request validator
    }

    private void validateAccountId(Long accountId) {
        if (null == accountId || accountId < 1) {
            throw new InvalidPurchaseException("Account id Invalid");
        }
    }

}
