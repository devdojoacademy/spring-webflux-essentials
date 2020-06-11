package academy.devdojo.webflux;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import reactor.blockhound.BlockHound;

@SpringBootApplication
public class SpringWebfluxEssentialsApplication {

    static {
        BlockHound.install(
            builder -> builder.allowBlockingCallsInside("java.util.UUID", "randomUUID")
        );
    }

    public static void main(String[] args) {
        System.out.println(PasswordEncoderFactories.createDelegatingPasswordEncoder().encode("devdojo"));
        SpringApplication.run(SpringWebfluxEssentialsApplication.class, args);
    }

}
