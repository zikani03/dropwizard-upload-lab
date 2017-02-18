package me.zikani.labs.dropwizardupload;

import io.dropwizard.auth.UnauthorizedHandler;
import org.eclipse.jetty.http.HttpStatus;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collections;

public class DefaultUnauthorizedHandler implements UnauthorizedHandler {
    @Override
    public Response buildResponse(String prefix, String realm) {
        return notAuthorized();
    }

    public static Response notAuthorized() {
        return Response.status(HttpStatus.UNAUTHORIZED_401)
                .type(MediaType.APPLICATION_JSON)
                .entity(Collections.singletonMap("message", "Not Authorized to access resource"))
                .build();
    }
}
