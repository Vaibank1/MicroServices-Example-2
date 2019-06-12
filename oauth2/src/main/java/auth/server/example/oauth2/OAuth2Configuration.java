
package auth.server.example.oauth2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;

import java.util.logging.Level;
import java.util.logging.Logger;

@Configuration
@EnableAuthorizationServer

public class OAuth2Configuration extends AuthorizationServerConfigurerAdapter {


    @Autowired
    @Qualifier("authenticationManagerBean")
    private AuthenticationManager authenticationManager;

    private final static Logger log = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);


    @Bean
    protected JwtAccessTokenConverter jwtTokenEnhancer() {
        KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(new ClassPathResource("jwt.jks"), "mySecretKey".toCharArray());
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        converter.setKeyPair(keyStoreKeyFactory.getKeyPair("jwt"));
        log.log(Level.INFO, "The value of converter is "+ converter);
        return converter;
    }
    @Bean
    public TokenStore tokenStore() {


        log.log(Level.INFO, "tokenStore() CALLED");
        return new JwtTokenStore(jwtTokenEnhancer());
    }
    @Override
    public  void configure(AuthorizationServerEndpointsConfigurer endpoints) throws  Exception {
        endpoints.tokenStore(tokenStore()).tokenEnhancer(jwtTokenEnhancer())
        .authenticationManager(authenticationManager);

    }
    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception
    {
        clients.inMemory()
                .withClient("web_app")
                .scopes("FOO")
                .autoApprove(true)
                .authorities("FOO_READ","FOO_WRITE")
                .authorizedGrantTypes("implicit","refresh_token", "password", "authorization_code")
        .secret("{noop}secret")
        ;


    }
}

