package uk.gov.dwp.uc.pairtest.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.paymentgateway.TicketPaymentServiceImpl;
import thirdparty.seatbooking.SeatReservationService;
import thirdparty.seatbooking.SeatReservationServiceImpl;

@Configuration
public class AppConfig {
    @Bean
    public TicketPaymentService ticketPaymentService() {
        return new TicketPaymentServiceImpl();
    }

    @Bean
    public SeatReservationService seatReservationService() {
        return new SeatReservationServiceImpl();
    }

}
