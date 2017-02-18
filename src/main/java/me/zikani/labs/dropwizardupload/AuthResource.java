package me.zikani.labs.dropwizardupload;

import com.github.toastshaman.dropwizard.auth.jwt.hmac.HmacSHA256Signer;
import com.github.toastshaman.dropwizard.auth.jwt.model.JsonWebToken;
import com.github.toastshaman.dropwizard.auth.jwt.model.JsonWebTokenClaim;
import com.github.toastshaman.dropwizard.auth.jwt.model.JsonWebTokenHeader;
import org.joda.time.DateTime;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    private final Map<String, User> users;
    private final byte[] tokenSecret;

    public AuthResource(Map<String, User> users, byte[] tokenSecret) {
        this.users = users;
        this.tokenSecret = tokenSecret;
    }

    @POST
    public Response getToken(@Valid User user) {
        User userFound = users.get(user.getUsername());
        if (!Objects.isNull(userFound) &&
            userFound.getPassword().equalsIgnoreCase(user.getPassword())) {

            final HmacSHA256Signer signer = new HmacSHA256Signer(tokenSecret);

            final JsonWebToken token = JsonWebToken.builder()
                    .header(JsonWebTokenHeader.HS256())
                    .claim(JsonWebTokenClaim.builder()
                            .subject(user.getUsername())
                            .issuedAt(new DateTime())
                            .expiration(new DateTime().plusMinutes(30))
                            .build())
                    .build();

            return Response.ok()
                    .entity(Collections.singletonMap("token", signer.sign(token)))
                    .build();
        }

        return Response.status(Response.Status.UNAUTHORIZED)
                .entity(Collections.singletonMap("message", "Invalid username or password"))
                .build();
    }
}
