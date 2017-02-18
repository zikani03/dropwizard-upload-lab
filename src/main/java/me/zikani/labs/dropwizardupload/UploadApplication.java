package me.zikani.labs.dropwizardupload;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.toastshaman.dropwizard.auth.jwt.JWTAuthFilter;
import com.github.toastshaman.dropwizard.auth.jwt.JsonWebTokenParser;
import com.github.toastshaman.dropwizard.auth.jwt.JsonWebTokenVerifier;
import com.github.toastshaman.dropwizard.auth.jwt.hmac.HmacSHA256Verifier;
import com.github.toastshaman.dropwizard.auth.jwt.model.JsonWebToken;
import com.github.toastshaman.dropwizard.auth.jwt.parser.DefaultJsonWebTokenParser;
import com.google.common.base.Optional;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.forms.MultiPartBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;

import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UploadApplication extends Application<UploadConfiguration> {
    final int KEY_LENGTH = 8;
    SecureRandom random = new SecureRandom();
    Map<String, User> users = new ConcurrentHashMap<>();

    public static void main(String... args) throws Exception {
        new UploadApplication().run(args);
    }

    @Override
    public void initialize(Bootstrap<UploadConfiguration> bootstrap) {
        initializeUsers();
        ObjectMapper mapper = bootstrap.getObjectMapper();

        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

        // Auto-register Jackson modules - mostly for JSR310 DateTime module
        mapper.findAndRegisterModules();

        bootstrap.addBundle(new AssetsBundle("/webapp", "/", "index.html"));

        bootstrap.addBundle(new MultiPartBundle());
    }

    private void initializeUsers() {
        users.put("zikani", new User("zikani", "zikani123"));
        users.put("foo", new User("foo", "bar"));
        users.put("john", new User("john", "john123"));
    }

    private byte[] tokenSecret() {
        byte[] secret = new byte[KEY_LENGTH];
        random.nextBytes(secret);
        return secret;
    }

    @Override
    public void run(UploadConfiguration uploadConfiguration, Environment environment) throws Exception {
        byte[] tokenSecretBytes = tokenSecret();
        environment.jersey().register(MultiPartFeature.class);
        environment.jersey().register(RolesAllowedDynamicFeature.class);
        environment.jersey().register(new AuthResource(users, tokenSecretBytes));
        environment.jersey().register(new UploadResource(uploadConfiguration.getUploadsDir()));

        environment.admin().addTask(new CleanTask(Paths.get(uploadConfiguration.getUploadsDir())));

        // Enable the @Auth annotation
        environment.jersey().register(new AuthValueFactoryProvider.Binder<>(User.class));
        JsonWebTokenParser tokenParser = new DefaultJsonWebTokenParser();
        JsonWebTokenVerifier tokenVerifier = new HmacSHA256Verifier(tokenSecretBytes);
        environment.jersey().register(new AuthDynamicFeature(
                new JWTAuthFilter.Builder<User>()
                    .setTokenParser(tokenParser)
                    .setTokenVerifier(tokenVerifier)
                    .setAuthenticator(new UploadAuthenticator())
                    .setUnauthorizedHandler(new DefaultUnauthorizedHandler())
                    .setRealm("me.zikani.labs")
                    .setPrefix("Bearer")
                    .buildAuthFilter()));
    }

    private final class UploadAuthenticator implements Authenticator<JsonWebToken, User> {
        @Override
        public Optional<User> authenticate(JsonWebToken jsonWebToken) throws AuthenticationException {

            String username = jsonWebToken.claim().subject();

            if (users.containsKey(username)) {
                return Optional.of(users.get(username));
            }
            return Optional.absent();
        }
    }
}
