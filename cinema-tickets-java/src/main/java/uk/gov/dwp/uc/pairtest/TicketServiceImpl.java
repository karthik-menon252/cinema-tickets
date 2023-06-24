package uk.gov.dwp.uc.pairtest;


import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;
import constraints.ValidWrapper;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

@Component
public class TicketServiceImpl implements TicketService {
    private static final int ADULT_TICKET_PRICE = 20;
    private static final int CHILD_TICKET_PRICE = 10;
    private static final int INFANT_TICKET_PRICE = 0;
    private static final int MAX_TICKET_COUNT = 20;
    /**
     * Should only have private methods other than the one below.
     */
    @Autowired
    private TicketPaymentService ticketPaymentService;
    @Autowired
    private SeatReservationService seatReservationService;

    private Validator validator = Validation.byDefaultProvider()
            .configure()
            .messageInterpolator(new ParameterMessageInterpolator())
            .buildValidatorFactory()
            .getValidator();;

    @Override
    public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {
        validateAccountId(accountId); // null or invalid account id
        validateTicketTypeRequests(ticketTypeRequests);
        Map<TicketTypeRequest.Type, TicketTypeRequest> ticketMap = Arrays.stream(ticketTypeRequests)
                .collect(toMap(TicketTypeRequest::getTicketType, Function.identity(), TicketTypeRequest::add));
        int adultCount = getTicketCount(ticketMap, TicketTypeRequest.Type.ADULT);
        int childCount = getTicketCount(ticketMap, TicketTypeRequest.Type.CHILD);
        int infantCount = getTicketCount(ticketMap, TicketTypeRequest.Type.INFANT);
        validateTicketCounts(adultCount, childCount, infantCount);
        seatReservationService.reserveSeat(accountId, adultCount + childCount);
        ticketPaymentService.makePayment(accountId, adultCount * ADULT_TICKET_PRICE
                + childCount * CHILD_TICKET_PRICE
                + infantCount * INFANT_TICKET_PRICE);
    }

    private void validateTicketCounts(int adultCount, int childCount, int infantCount) {
        if (childCount > 0 && adultCount == 0) { // child tickets can be purchased only if there is an adult accompanying
            throw new InvalidPurchaseException("Child tickets cannot be purchased without adult ticket");
        } else if (infantCount > adultCount) { // infants to sit on the laps of adults
            throw new InvalidPurchaseException("Number of adults less than infants");
        } else if (adultCount == 0) { // no adults means
            throw new InvalidPurchaseException("No adult tickets purchased");
        } else if (adultCount + childCount + infantCount > MAX_TICKET_COUNT) {
            throw new InvalidPurchaseException("Exceeded limit on number of tickets");
        }
    }

    private int getTicketCount(Map<TicketTypeRequest.Type, TicketTypeRequest> ticketMap, TicketTypeRequest.Type type) {
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
        ValidWrapper<TicketTypeRequest> validWrapper = new ValidWrapper<>(ticketTypeRequests);
        Set<ConstraintViolation<ValidWrapper>> violations = validator.validate(validWrapper);
        String errorMessage = violations.stream().map(violation -> violation.getMessage())
                .collect(Collectors.joining(","));
        if (!(errorMessage == null || errorMessage.length() == 0)) {
            throw new InvalidPurchaseException(errorMessage);
        }
    }

    private void validateAccountId(Long accountId) {
        if (null == accountId || accountId < 1) {
            throw new InvalidPurchaseException("Account id Invalid");
        }
    }

}
