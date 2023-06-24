package uk.gov.dwp.uc.pairtest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import static org.mockito.Mockito.times;



@RunWith(MockitoJUnitRunner.class)
public class TicketServiceImplTest {
    private static final int ADULT_TICKET_PRICE = 20;
    private static final int CHILD_TICKET_PRICE = 10;
    private static final int INFANT_TICKET_PRICE = 0;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @InjectMocks
    TicketService ticketService = new TicketServiceImpl();

    @Mock
    private TicketPaymentService ticketPaymentService;

    @Mock
    private SeatReservationService seatReservationService;


    @Test
    public void testPurchaseTicketsForNullAccountId() {
        thrown.expect(InvalidPurchaseException.class);
        thrown.expectMessage("Account id Invalid");

        ticketService.purchaseTickets(null, new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2));
    }

    @Test
    public void testPurchaseTicketsForAccountIdLessThanOne() {
        thrown.expect(InvalidPurchaseException.class);
        thrown.expectMessage("Account id Invalid");

        ticketService.purchaseTickets(0L, new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2));
    }

    @Test
    public void testPurchaseTicketsForValidAccountId() {
        TicketTypeRequest[] ticketTypeRequests = new TicketTypeRequest[1];
        ticketTypeRequests[0] = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1);
        ticketService.purchaseTickets(1L, ticketTypeRequests);
    }

    @Test
    public void testPurchaseTicketsWithoutRequest() {
        thrown.expect(InvalidPurchaseException.class);
        thrown.expectMessage("Ticket request cannot be null or empty");

        ticketService.purchaseTickets(1L);
    }

    @Test
    public void testPurchaseTicketsWithNullRequest() {
        thrown.expect(InvalidPurchaseException.class);
        thrown.expectMessage("Ticket request cannot be null or empty");

        ticketService.purchaseTickets(1L, null);
    }

    @Test
    public void testPurchaseTicketsWithoutRequestWithTicketTypeNullAndCount() {
        thrown.expect(InvalidPurchaseException.class);
        thrown.expectMessage("Invalid number of tickets: cannot be less than 0");
        thrown.expectMessage("Invalid ticket type: must be one of [ADULT, CHILD, INFANT]");

        ticketService.purchaseTickets(1L, new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2), new TicketTypeRequest(null, 3), new TicketTypeRequest(TicketTypeRequest.Type.ADULT, -1));
    }

    @Test
    public void testPurchaseTicketsWithValidAdultTicketCount() {
        TicketTypeRequest request1 = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2);
        TicketTypeRequest request2 = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 3);
        ticketService.purchaseTickets(1L, request1, request2);
        Mockito.verify(seatReservationService, times(1)).reserveSeat(1L, request1.getNoOfTickets() + request2.getNoOfTickets());
        Mockito.verify(ticketPaymentService, times(1)).makePayment(1L, (request1.getNoOfTickets() + request2.getNoOfTickets()) * ADULT_TICKET_PRICE);
    }

    @Test
    public void testPurchaseTicketsWithValidAdultAndChildTickets() {
        TicketTypeRequest request1 = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2);
        TicketTypeRequest request2 = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 3);
        ticketService.purchaseTickets(1L, request1, request2);
        Mockito.verify(seatReservationService, times(1)).reserveSeat(1L, 5);
        Mockito.verify(ticketPaymentService, times(1)).makePayment(1L, request1.getNoOfTickets() * ADULT_TICKET_PRICE + request2.getNoOfTickets() * CHILD_TICKET_PRICE);
    }

    @Test
    public void testPurchaseTicketsWithValidAdultAndInfantTickets() {
        TicketTypeRequest request1 = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2);
        TicketTypeRequest request2 = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 2);
        ticketService.purchaseTickets(1L, request1, request2);
        Mockito.verify(seatReservationService, times(1)).reserveSeat(1L, request1.getNoOfTickets());
        Mockito.verify(ticketPaymentService, times(1)).makePayment(1L, request1.getNoOfTickets() * ADULT_TICKET_PRICE + request2.getNoOfTickets() * INFANT_TICKET_PRICE);
    }

    @Test
    public void testPurchaseTicketWithValidAdultChildAndInfant() {
        TicketTypeRequest request1 = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 3);
        TicketTypeRequest request2 = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 3);
        TicketTypeRequest request3 = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 3);
        ticketService.purchaseTickets(1L, request1, request2, request3);
        Mockito.verify(seatReservationService, times(1)).reserveSeat(1L,request3.getNoOfTickets() + request2.getNoOfTickets());
        Mockito.verify(ticketPaymentService, times(1)).makePayment(1L, request3.getNoOfTickets() * ADULT_TICKET_PRICE + request2.getNoOfTickets() * CHILD_TICKET_PRICE + request3.getNoOfTickets() * INFANT_TICKET_PRICE);
    }

    @Test
    public void testPurchaseTicketWithInvalidAdultAndInfantTickets() {
        thrown.expect(InvalidPurchaseException.class);
        thrown.expectMessage("Number of adults less than infants");

        TicketTypeRequest request1 = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2);
        TicketTypeRequest request2 = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 3);
        ticketService.purchaseTickets(1L, request1, request2);
        Mockito.verify(seatReservationService, times(0)).reserveSeat(1L, Mockito.anyInt());
        Mockito.verify(ticketPaymentService, times(0)).makePayment(1L, Mockito.anyInt());
    }

    @Test
    public void testPurchaseTicketWithInvalidAdultAndChildTickets() {
        thrown.expect(InvalidPurchaseException.class);
        thrown.expectMessage("Child tickets cannot be purchased without adult ticket");

        TicketTypeRequest request1 = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 0);
        TicketTypeRequest request2 = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 3);
        ticketService.purchaseTickets(1L, request1, request2);
        Mockito.verify(seatReservationService, times(0)).reserveSeat(1L, Mockito.anyInt());
        Mockito.verify(ticketPaymentService, times(0)).makePayment(1L, Mockito.anyInt());
    }

    @Test
    public void testPurchaseTicketWithOnlyChildTickets() {
        thrown.expect(InvalidPurchaseException.class);
        thrown.expectMessage("Child tickets cannot be purchased without adult ticket");

        TicketTypeRequest request2 = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 3);
        ticketService.purchaseTickets(1L, request2);
        Mockito.verify(seatReservationService, times(0)).reserveSeat(1L, Mockito.anyInt());
        Mockito.verify(ticketPaymentService, times(0)).makePayment(1L, Mockito.anyInt());
    }

    @Test
    public void testPurchaseTicketWithNoAdultTickets() {
        thrown.expect(InvalidPurchaseException.class);
        thrown.expectMessage("No adult tickets purchased");

        TicketTypeRequest request2 = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 0);
        ticketService.purchaseTickets(1L, request2);
        Mockito.verify(seatReservationService, times(0)).reserveSeat(1L, Mockito.anyInt());
        Mockito.verify(ticketPaymentService, times(0)).makePayment(1L, Mockito.anyInt());
    }

    @Test
    public void testPurchaseTicketWithOnlyInfantTickets() {
        thrown.expect(InvalidPurchaseException.class);
        thrown.expectMessage("Number of adults less than infants");

        TicketTypeRequest request1 = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 3);
        ticketService.purchaseTickets(1L, request1);
        Mockito.verify(seatReservationService, times(0)).reserveSeat(1L, Mockito.anyInt());
        Mockito.verify(ticketPaymentService, times(0)).makePayment(1L, Mockito.anyInt());
    }

    @Test
    public void testPurchaseTicketWithMoreThan20Tickets() {
        thrown.expect(InvalidPurchaseException.class);
        thrown.expectMessage("Exceeded limit on number of tickets");

        TicketTypeRequest request1 = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 5);
        TicketTypeRequest request2 = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 15);
        TicketTypeRequest request3 = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 2);
        ticketService.purchaseTickets(1L, request1, request2, request3);
        Mockito.verify(seatReservationService, times(0)).reserveSeat(1L, Mockito.anyInt());
        Mockito.verify(ticketPaymentService, times(0)).makePayment(1L, Mockito.anyInt());
    }
}
