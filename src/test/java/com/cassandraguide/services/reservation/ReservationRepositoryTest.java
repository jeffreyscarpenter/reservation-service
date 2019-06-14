package com.cassandraguide.services.reservation;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.cassandraguide.reservation.model.Reservation;
import com.cassandraguide.reservation.service.ReservationRepository;

@RunWith(JUnitPlatform.class)
@ExtendWith(SpringExtension.class)
@TestPropertySource(locations = "/application.properties")
@ContextConfiguration(classes = {ReservationRepository.class})
public class ReservationRepositoryTest {

    @Autowired
    private ReservationRepository reservationRepo;
    
	@Test
	@DisplayName("Create a new reservation")
	public void insertNewReservation_shouldUpdateTables() {
	    Reservation r1 = new Reservation();
	    r1.setEndDate(LocalDate.of(2020, 12, 20));
	    r1.setStartDate(LocalDate.now());
	    r1.setHotelId("12345");
	    r1.setGuestId(UUID.randomUUID());
	    r1.setRoomNumber(Short.valueOf("42"));
	    reservationRepo.upsert(r1);
	}

}
