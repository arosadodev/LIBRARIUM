package ovh.emserv.librarium.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

/**
 * @author mit
 * @date 9/11/26 20:15
 */
@Service
public class ServicioJwt {

    // Clave secreta segura generada para firmar los tokens[cite: 17]
    private final SecretKey claveSecreta = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private final long tiempoExpiracionMs = 86400000; // 24 horas

    public String generarToken(Authentication credencialesValidadas) {
        // Obtenemos los roles (ej. ["ROLE_USER"])[cite: 17]
        List<String> roles = credencialesValidadas.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();

        return Jwts.builder().setSubject(credencialesValidadas.getName()) // El email del usuario[cite: 17]
                .setIssuer("librarium-api") // Emisor del token[cite: 17]
                .setIssuedAt(new Date()) // Fecha de creación[cite: 17]
                .setExpiration(new Date(System.currentTimeMillis() + tiempoExpiracionMs)) // Fecha límite[cite: 17]
                .claim("roles", roles) // Guardamos los roles como información extra[cite: 17]
                .signWith(claveSecreta) // Firmamos[cite: 17]
                .compact();
    }

    public SecretKey getClaveSecreta() {
        return claveSecreta;
    }

}

