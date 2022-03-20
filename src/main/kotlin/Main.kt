import io.ktor.client.*
import io.ktor.client.features.websocket.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import sun.misc.Signal
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.util.concurrent.atomic.AtomicInteger

val accessToken: String by lazy { System.getenv("HOME_ASSISTANT_ACCESS_TOKEN") }

val JSON = Json { ignoreUnknownKeys = true }

var id = AtomicInteger(0)

@Serializable
data class TypedMessage(val type: String)

@Serializable
data class HaAuthorization(val type: String, val access_token: String)

@Serializable
data class Subscribe(val id: Int, val type: String)

suspend fun DefaultClientWebSocketSession.outputMessages() {
    try {
        for (message in incoming) {
            message as? Frame.Text ?: continue
            val receivedText = message.readText()
            println(receivedText)

            val typedMessage = JSON.decodeFromString<TypedMessage>(receivedText)
            println(typedMessage.type)

            if (typedMessage.type == "auth_required") {
                send(JSON.encodeToString(HaAuthorization(type = "auth", access_token = accessToken)))
                continue
            }

            if (typedMessage.type == "auth_ok") {
                id.set(id.get() + 1)
                send(JSON.encodeToString(Subscribe(id = id.get(), type = "subscribe_events")))
                continue
            }
        }
    } catch (e: CancellationException) {
        println("coroutine cancelled")
        throw e
    } catch (e: Exception) {
        println("Error while receiving: " + e.localizedMessage)
        throw e
    }
}

fun main() {
    val client = HttpClient {
        install(WebSockets)
    }

    var job: Job? = null
    Signal.handle(Signal("INT")) {
        job?.cancel()
        client.close()
        println("Connection closed. Goodbye!")
    }

    runBlocking {
        client.webSocket(
            method = HttpMethod.Get,
            host = "homeassistant.local",
            port = 8123,
            path = "/api/websocket"
        ) {
            job = launch { outputMessages() }
            job?.join()
        }
    }

    client.close()
}
