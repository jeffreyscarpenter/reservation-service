/*
 * Copyright (C) 2016-2020 Jeff Carpenter
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
 
/* This file contains a slightly modified version of the "reservation" keyspace and table definitions
 * for the example defined in Chapter 5 of Cassandra: The Definitive Guide, 2nd and 3nd Editions.
 * The changes are to facilitate development exercises.
 */
  
CREATE KEYSPACE reservation
    WITH replication = {'class': 'SimpleStrategy', 'replication_factor' : 1};

CREATE TABLE reservation.reservations_by_hotel_date (
    hotel_id text,
    start_date date,
    end_date date,
    room_number smallint,
    confirmation_number text,
    guest_id uuid,
    PRIMARY KEY ((hotel_id, start_date), room_number)
);

CREATE TABLE reservation.reservations_by_confirmation (
    confirmation_number text PRIMARY KEY,
    hotel_id text,
    start_date date,
    end_date date,
    room_number smallint,
    guest_id uuid
);

/*
 The following tables are provided for completeness with the book text, but they are not used in the current
 implementation of the Reservation Service
 */

CREATE TABLE reservation.reservations_by_guest (
    guest_last_name text,
    hotel_id text,
    start_date date,
    end_date date,
    room_number smallint,
    confirmation_number text,
    guest_id uuid,
    PRIMARY KEY ((guest_last_name), hotel_id)
);

CREATE TYPE reservation.address (
    street text,
    city text,
    state_or_province text,
    postal_code text,
    country text
);

CREATE TABLE reservation.guests (
    guest_id uuid PRIMARY KEY,
    first_name text,
    last_name text,
    title text,
    emails set<text>,
    phone_numbers list<text>,
    addresses map<text, frozen<address>>,
    confirmation_number text
);

