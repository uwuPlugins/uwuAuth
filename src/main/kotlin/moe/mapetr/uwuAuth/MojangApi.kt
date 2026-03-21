package moe.mapetr.uwuAuth

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

object MojangApi {
    private val client = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(5))
        .build()

    fun isPremium(username: String): Boolean {
        return try {
            val request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.mojang.com/users/profiles/minecraft/$username"))
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build()
            val response = client.send(request, HttpResponse.BodyHandlers.ofString())
            response.statusCode() == 200
        } catch (_: Exception) {
            false
        }
    }
}
