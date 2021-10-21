package com.doopp.findroute.controller;

import com.doopp.gutty.annotation.Controller;
import com.doopp.findroute.message.MyResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import lombok.extern.log4j.Log4j2;

import javax.ws.rs.*;

@Log4j2
@Controller
@Path("/api")
public class LoginController {

    @POST
    @Path("/my-headers")
    @Produces("application/json")
    public MyResponse<HttpHeaders> myHeaders(HttpRequest request) {
        return MyResponse.ok(request.headers());
    }
}
