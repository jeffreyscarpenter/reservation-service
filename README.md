# reservation-service
This repository provides a sample microservice implementation based on the
reservation data model from the O'Reilly book [Cassandra: The Definitive Guide, 2nd Edition](http://shop.oreilly.com/product/0636920043041.do).

![Book Cover](images/cassandra-tdg.jpg)

## Overview
The goal of this project is to provide a minimally functional implementation of a microservice that uses 
Apache Cassandra for its data storage. The reservation service is implemented as a RESTful service using Spring Boot.
I found [this tutorial][tutorial] helpful in getting this implementation up and running quickly.

This service leverages the [reservation schema][schema] developed in the book, based on the data model shown here:

![Book Cover](images/cass_05_reservation_physical.png)

If you'd like to understand more about the motivation behind this design, you can access the data modeling chapter 
from the book for free at the [O'Reilly website][chapter].

## Disclaimers
This service has a couple of minor shortcomings that I would consider inappropriate for a production-ready service
implementation:

- There is minimal data validation
- There is minimal handling of fault cases
- The schema makes use of Strings as identifiers instead of UUIDs

With respect to that last point about UUIDs: for this service I take the same approach that I did for the book.
When working with small scale examples, it is simpler to deal with IDs that are human readable strings. 
If I were intending to build an actual system out of this or even implement more of the "ecosystem" of services
implied by the book's data model, I would move toward using UUIDs for identifiers. For more of my thinking on this 
topic, please read the [Identity blog post][identity] from my [Data Model Meets World][dmmw] series. 

Comments, improvements and feedback are welcome.

Copyright 2017 Jeff Carpenter

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

[tutorial]: http://www.springboottutorial.com/creating-rest-service-with-spring-boot
[schema]: /src/main/resources/reservation.cql
[dmmw]: https://medium.com/@jscarp/data-model-meets-world-c67a46681b39
[identity]: https://medium.com/@jscarp/data-model-meets-world-part-ii-identity-crisis-d517d3d4c39a
[chapter]: https://www.oreilly.com/ideas/cassandra-data-modeling
