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

    // Clave secreta segura generada para firmar los tokens
    private final SecretKey claveSecreta = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private final long tiempoExpiracionMs = 86400000; // 24 horas

    public String generarToken(Authentication credencialesValidadas) {
        // Obtenemos los roles (ej. ["ROLE_USER"])
        List<String> roles = credencialesValidadas.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();

        return Jwts.builder().setSubject(credencialesValidadas.getName()) // El email del usuario
                .setIssuer("librarium-api") // Emisor del token
                .setIssuedAt(new Date()) // Fecha de creación
                .setExpiration(new Date(System.currentTimeMillis() + tiempoExpiracionMs)) // Fecha límite
                .claim("roles", roles) // Guardamos los roles como información extra
                .signWith(claveSecreta) // Firmamos
                .compact();
    }

    public SecretKey getClaveSecreta() {
        return claveSecreta;
    }

}

