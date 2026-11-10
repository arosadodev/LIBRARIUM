package ovh.emserv.librarium.service;

import io.jsonwebtoken.Jwts;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.List;

/**
 * @author mit
 * @date 9/11/26 20:15
 */
@Service
public class ServicioJwt {

    private final SecretKey claveSecreta;
    private final long tiempoExpiracionMs = 86400000; // 24 horas

    public ServicioJwt(@Value("${librarium.jwt.secret}") String secretoBase64) {
        try {
            byte[] secreto = Base64.getDecoder().decode(secretoBase64);
            if (secreto.length < 32) {
                throw new IllegalArgumentException("La clave JWT debe tener al menos 256 bits");
            }
            this.claveSecreta = new SecretKeySpec(secreto, "HmacSHA256");
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("La propiedad librarium.jwt.secret no contiene una clave Base64 válida", exception);
        }
    }

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
