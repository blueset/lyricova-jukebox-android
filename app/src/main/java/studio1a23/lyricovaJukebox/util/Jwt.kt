package studio1a23.lyricovaJukebox.util

import com.google.gson.GsonBuilder
import com.philjay.jwt.Base64Decoder
import com.philjay.jwt.JWT
import com.philjay.jwt.JWTAuthHeader
import com.philjay.jwt.JWTAuthPayload
import com.philjay.jwt.JWTToken
import com.philjay.jwt.JsonDecoder
import java.util.Date
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class JWTAuthPayloadWithExp(
    /** the issuer of the token (team id found in developer member center) */
    iss: String,
    /** token issued at timestamp in seconds since Epoch (UTC) */
    iat: Long,
    /** token expiration timestamp in seconds since Epoch (UTC) */
    val exp: Long,
) : JWTAuthPayload(iss, iat)


@OptIn(ExperimentalEncodingApi::class)
fun getJwtExpiryDate(jwtToken: String): Date? {
    val gson = GsonBuilder().create()
    val jsonDecoder = object : JsonDecoder<JWTAuthHeader, JWTAuthPayloadWithExp> {

        override fun headerFrom(json: String): JWTAuthHeader {
            return gson.fromJson(json, JWTAuthHeader::class.java)
        }

        override fun payloadFrom(json: String): JWTAuthPayloadWithExp {
            return gson.fromJson(json, JWTAuthPayloadWithExp::class.java)
        }
    }
    // Base64 decoder using apache commons
    val decoder = object : Base64Decoder {
        override fun decode(bytes: ByteArray): ByteArray {
            return Base64.UrlSafe.decode(bytes)
        }

        override fun decode(string: String): ByteArray {
            return Base64.UrlSafe.decode(string)
        }
    }
    val t: JWTToken<JWTAuthHeader, JWTAuthPayloadWithExp>? = JWT.decode(jwtToken, jsonDecoder, decoder)
    val timestamp = t?.payload?.exp
    return if (timestamp != null) {
        Date(timestamp * 1000)
    } else {
        null
    }
}