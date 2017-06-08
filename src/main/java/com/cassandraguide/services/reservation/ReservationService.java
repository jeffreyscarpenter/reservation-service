/*
 * Copyright (C) 2017 Jeff Carpenter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cassandraguide.services.reservation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;


// TODO: DataStax Java Driver imports


@Component
public class ReservationService {

    private static final Logger logger = LoggerFactory.getLogger(ReservationService.class);

    // TODO: private variable to hold DataStax Java Driver Session - used for executing queries

    public ReservationService() {

        // TODO: Create cluster with connection to localhost

        // TODO: Create session for reservation keyspace
    }

    public String createReservation(Reservation reservation) {

        /*
         * Business Logic -
         *  you should not need to change this code, although in a real production service
         *  you'd likely do more validation than is done here
         */

        // Validate new reservation doesn't contain a confirmation number
        if (reservation.getConfirmationNumber() != null)
        {
            logger.error("Received new reservation containing confirmation number: " +
                    reservation.getConfirmationNumber());
            throw new IllegalArgumentException("New reservation cannot contain confirmation number");
        }

        // generate and set confirmation number
        reservation.setConfirmationNumber(getUniqueConfirmationNumber());

        /*
         * Data Manipulation Logic
         */

        // TODO: Construct SimpleStatement for inserting the reservation
        // For this exercise we will insert only into the reservations_by_confirmation table
        // Hint: use provided convenience function convertJavaLocalDateToDataStax for start and end dates

        // TODO: Execute the statement

        // Return the confirmation number that was created
        return reservation.getConfirmationNumber();
    }

    public Reservation retrieveReservation(String confirmationNumber) {

        Reservation reservation = null;

        /*
         * Data Manipulation Logic
         */

        // TODO: Construct SimpleStatement for retrieving the reservation from the reservations_by_confirmation table
        // Hint: Remember to use parameterization


        // TODO: Execute the statement

        // TODO: Process the results (ResultSet)
        // Hint: an empty result might not be an error as this method is sometimes used to check whether a
        // reservation with this confirmation number exists

            // Hint: If there is a result, create a new reservation object and set the values
            // Hint: use provided convenience function convertDataStaxLocalDateToJava for start and end dates
            // Bonus: factor the logic to extract a reservation from a row into a separate method
            // (you will reuse it again later in getAllReservations())

        return reservation;
    }

    public void updateReservation(Reservation reservation) {
        /*
         * Business Logic -
         *  you should not need to change this code, although in a real production service
         *  you'd likely do more validation than is done here
         */

        // verify reservation exists
        if (retrieveReservation(reservation.getConfirmationNumber()) == null)
        {
            String errorMsg = "Unable to update unknown reservation with confirmation number: " +
                    reservation.getConfirmationNumber();
            logger.error(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }

        /*
         * Data Manipulation Logic
         */

        // TODO: Construct SimpleStatement for updating the reservation
        // For this exercise we will insert only into the reservations_by_confirmation table

        // TODO: Execute the statement
    }

    public List<Reservation> getAllReservations() {

        // for populating return value
        List<Reservation> reservations = new ArrayList<Reservation>();

        /*
         * Data Manipulation Logic
         */

        // TODO: Construct SimpleStatement for retrieving the entire contents of the reservations_by_confirmation table

        // TODO: Execute the statement to get a result set

        // TODO: Iterate over the rows in the result set, creating a reservation for each one
        // Hint: find the logic you wrote for retrieveReservation() for processing a single row,
        // and refactor that into a method you can reuse here


        return reservations;
    }

    public List<Reservation> searchReservationsByHotelDate(String hotelId, LocalDate date) {

        // We will implement this in a later exercise
        throw new UnsupportedOperationException();
    }

    public void deleteReservation(String confirmationNumber) {
        /*
         * Data Manipulation Logic
         */

        // TODO: Construct SimpleStatement for deleting the selected item from the reservations_by_confirmation table

        // TODO: Execute the statement

    }

    // convenience method, you should not need to modify
    private String getUniqueConfirmationNumber()
    {
        String confirmationNumber;

        // Ensure uniqueness of the generated code by searching for a reservation with that code
        do {
            confirmationNumber = ConfirmationNumberGenerator.getConfirmationNumber();
        }
        while (retrieveReservation(confirmationNumber) != null);

        return confirmationNumber;
    }

/*
    // convenience method to be replaced in a later exercise by codecs
    private com.datastax.driver.core.LocalDate convertJavaLocalDateToDataStax(java.time.LocalDate date)
    {
        if (date == null) return null;
        int year=date.getYear();
        int month = date.getMonthValue();
        int day = date.getDayOfMonth();
        return com.datastax.driver.core.LocalDate.fromYearMonthDay(year, month, day);
    }

    // convenience method to be replaced in a later exercise by codecs
    private java.time.LocalDate convertDataStaxLocalDateToJava(com.datastax.driver.core.LocalDate date)
    {
        if (date == null) return null;
        return java.time.LocalDate.parse(date.toString());
    }
*/

}