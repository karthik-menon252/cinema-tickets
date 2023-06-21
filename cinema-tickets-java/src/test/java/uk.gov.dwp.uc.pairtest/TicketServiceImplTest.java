package uk.gov.dwp.uc.pairtest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

@RunWith(MockitoJUnitRunner.class)
public class TicketServiceImplTest {

    private final TicketService ticketService = new TicketServiceImpl();

    @Test(expected = InvalidPurchaseException.class)
    public void testPurchaseTickets() {
        ticketService.purchaseTickets(null, new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2));
    }
}
