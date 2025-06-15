package github.ag777.util.web.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import github.ag777.util.lang.collection.MapUtils;

import java.util.Date;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author ag777 <837915770@vip.qq.com>
 * @Description 针对jwt的二次封装类
 * @Date 2022/3/15 9:30
 */
public class JwtHelper {
    // 头部固定
    private static final Map<String, Object> headerMap = MapUtils.of(
            "alg", "HS256",
            "typ", "JWT"
    );
    // 加密方法
    private final Algorithm algorithm;

    /**
     *
     * @param key 密匙
     */
    public JwtHelper(String key) {
        this(Algorithm.HMAC256(key));
    }

    /**
     *
     * @param algorithm 算法
     */
    public JwtHelper(Algorithm algorithm) {
        this.algorithm = algorithm;
    }

    public Algorithm getAlgorithm() {
        return algorithm;
    }

    /**
     *
     * @param fillClaim 网jwt中填充数据,通过builder.withClaim
     * @param expireSeconds 过期时间
     * @return token
     * @throws IllegalArgumentException 算法为空
     * @throws JWTCreationException token创建异常
     */
    public String encode(Consumer<JWTCreator.Builder> fillClaim, int expireSeconds) throws IllegalArgumentException, JWTCreationException {
        JWTCreator.Builder builder = JWT.create()
                .withHeader(headerMap);
        fillClaim.accept(builder);
        return builder.withExpiresAt(
                new Date(System.currentTimeMillis()+expireSeconds* 1000L)
        )
        .sign(algorithm);
    }

    /**
     *
     * @param token jwt token
     * @return 解析结果
     * @throws JWTDecodeException 解析异常
     */
    public DecodedJWT decode(String token) throws JWTDecodeException {
        JWTVerifier verifier = JWT.require(algorithm).build();
        return verifier.verify(token);
    }

    public static void main(String[] args) {
        JwtHelper helper = new JwtHelper("key");
        String token = helper.encode(jwt -> {
            jwt.withClaim("id", 1)
                    .withClaim("name", "zhangsan");
        }, 60 * 60);  // 超时时间一小时

        DecodedJWT jwt = helper.decode(token);
        System.out.println(jwt.getClaim("id").asInt()); // 1
        System.out.println(jwt.getClaim("name").asString());    // zhangsan
        System.out.println(jwt.getClaim("a").asInt());  // null
    }

}
