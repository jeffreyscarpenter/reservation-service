package com.cassandraguide.reservation.api;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * We redirect to the API documentation page
 */
@RestController
@RequestMapping("/")
public class HomeResource {
    
    @RequestMapping(value = "/", method = RequestMethod.GET, produces = "text/html")
    public String redirectToDoc() {
        return new StringBuilder(""
                + "<html>"
                + " <head>"
                + "  <meta http-equiv=\"refresh\" content=\"0; url=swagger-ui.html\" />"
                + " </head>"
                + " <body/></html>").toString();
    }

}
