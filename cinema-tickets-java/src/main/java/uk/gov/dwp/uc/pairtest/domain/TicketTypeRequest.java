package uk.gov.dwp.uc.pairtest.domain;

import jakarta.validation.constraints.Min;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;
import validator.TicketType;

/**
 * Immutable Object
 */

public final class TicketTypeRequest {

    @Min(value = 0, message = "number of tickets cannot be less than 0")
    private final int noOfTickets;

    @TicketType(oneof = {Type.ADULT, Type.CHILD, Type.INFANT})
    private final Type type;

    public TicketTypeRequest(Type type, int noOfTickets) {
        this.type = type;
        this.noOfTickets = noOfTickets;
    }

    public int getNoOfTickets() {
        return noOfTickets;
    }

    public TicketTypeRequest add(TicketTypeRequest ticketTypeRequest) {
        if (ticketTypeRequest.getTicketType() == this.getTicketType()) {
            return new TicketTypeRequest(this.getTicketType(), this.getNoOfTickets() + ticketTypeRequest.getNoOfTickets());
        }
        throw new InvalidPurchaseException("Cannot add different type of tickets");
    }

    public Type getTicketType() {
        return type;
    }

    public enum Type {
        ADULT, CHILD, INFANT
    }

}
