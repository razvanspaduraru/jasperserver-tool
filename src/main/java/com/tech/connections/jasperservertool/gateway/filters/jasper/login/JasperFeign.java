package com.tech.connections.jasperservertool.gateway.filters.jasper.login;

import feign.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(url = "${nexgen.jasper.uri}", name = "JasperFeign")
public interface JasperFeign {
    @PostMapping("/jasperserver/rest_v2/login")
    Response login(@RequestParam("j_username") String user,
                   @RequestParam("j_password") String password);
}
