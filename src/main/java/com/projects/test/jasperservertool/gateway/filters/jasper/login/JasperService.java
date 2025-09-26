package com.projects.test.jasperservertool.gateway.filters.jasper.login;

import feign.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class JasperService {
    @Value("${jasper.jasper.access-user}")
    private String jasperaccessUser;
    @Value("${jasper.jasper.access-password}")
    private String jasperaccessPassword;

    private final JasperFeign jasperFeign;

    public JasperService(@Lazy JasperFeign jasperFeign) {
        this.jasperFeign = jasperFeign;
    }

    public Response login() {
        String user = jasperaccessUser;
        String password = jasperaccessPassword;
        Response response = jasperFeign.login(user, password);
        return response;
    }
}
