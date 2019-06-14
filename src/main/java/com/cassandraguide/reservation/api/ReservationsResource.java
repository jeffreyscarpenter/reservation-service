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
package com.cassandraguide.reservation.api;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cassandraguide.reservation.model.Reservation;
import com.cassandraguide.reservation.service.ReservationRepository;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * Operations on a list of reservations, list, search.
 */
@RestController
@Api(value = "/api/v1/reservations",  
     description = "Reservation Services Rest Resources")
@RequestMapping("/api/v1/reservations")
public class ReservationsResource {

    /** Logger for the class. */
    private static final Logger logger = LoggerFactory.getLogger(ReservationsResource.class);
    
    /** Service implementation Injection. */
    private ReservationRepository reservationService;

    /**
     * Best practice : Inversion of Control through constructor and no More @Inject nor @Autowired
     * 
     * @param reservationService
     *      service implementation
     */
    public ReservationsResource(ReservationRepository reservationService) {
        this.reservationService = reservationService;
    }
    
    /**
     * List all reservation in DB. Please not there is no implementation of paging. As such result can be
     * really large. If you query tables with large number of rows, please use Paging.
     *  
     * @return
     *      list all {@link Reservation} available in the table 
     */
    @RequestMapping(
            value = "/", 
            method = GET, 
            produces = APPLICATION_JSON_VALUE)
    @ApiOperation(
            value = "List all reservations available in the table", response = List.class)
    @ApiResponse(
            code = 200,
            message = "List all reservations available in the table")
    public ResponseEntity<List<Reservation>> findAll() {
        logger.debug("Fetching all reservations");
        // Returning an empty list is better than 204 code (meaning no valued expected)
        return ResponseEntity.ok(reservationService.findAll());
    }

    /**
     * List reservation for an hotel id on a particular date.
     *
     * @param hotelId
     *      uniquement hotel identifier
     * @param date
     *      target date
     * @return
     */
    @RequestMapping(
            value = "/searchByHotelDate", 
            method = GET, 
            produces = APPLICATION_JSON_VALUE)
    @ApiOperation(
            value = "Access Reservation information for an hotel", 
            response = Reservation.class)
    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid Parameter Hotel id is blank or contains invalid characters "
                    + "(expecting AlphaNumeric) or invalid date format expecting yyyy-MM-dd"),
            @ApiResponse(code = 200, message = "Returnings Reservation")})
    public ResponseEntity<List<Reservation>> searchReservationsByHotelDate(
            @RequestParam("hotelId") 
            @ApiParam(name="hotelId", value="Unique hotel identifier", required=true)
            String hotelId,
            @RequestParam(name="date", required=false) 
            @ApiParam(name="date", value="ISO value for date yyyy-MM-dd", required=false )
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) 
            LocalDate date) {
        if (null == hotelId || hotelId.isEmpty()) {
            throw new IllegalArgumentException("hotelId should not be null nor empty");
        }
        // Error in date format would be detected on LocalDate Marshalling, no extra controls
        logger.debug("Receive request for hotelId:{}, {}", hotelId, date);
        return ResponseEntity.ok(reservationService.findByHotelAndDate(hotelId, date));
    }

}
