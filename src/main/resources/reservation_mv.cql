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

/* This file contains commands to alter definitions within the "reservation" keyspace to use
 * materialized views.
 */

DROP TABLE reservation.reservations_by_confirmation;

CREATE MATERIALIZED VIEW reservation.reservations_by_confirmation AS
    SELECT *
    FROM reservation.reservations_by_hotel_date
    WHERE confirmation_number IS NOT NULL AND hotel_id IS NOT NULL AND start_date IS NOT NULL AND room_number IS NOT NULL
    PRIMARY KEY (confirmation_number, hotel_id, start_date, room_number);
