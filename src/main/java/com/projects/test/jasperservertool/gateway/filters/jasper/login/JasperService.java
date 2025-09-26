package com.projects.test.jasperservertool.gateway.filters.jasper.login;

import feign.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class JasperService {
    @Value("${nexgen.jasper.formio-user}")
    private String jasperFormIoUser;
    @Value("${nexgen.jasper.formio-password}")
    private String jasperFormIoPassword;

    private final JasperFeign jasperFeign;

    public JasperService(@Lazy JasperFeign jasperFeign) {
        this.jasperFeign = jasperFeign;
    }

    public Response login() {
        String user = jasperFormIoUser;
        String password = jasperFormIoPassword;
        Response response = jasperFeign.login(user, password);
        return response;
    }
}
