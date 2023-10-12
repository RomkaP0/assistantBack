package romka_po.plugins.database

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.ContentDisposition.Companion.File
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import io.ktor.utils.io.streams.*
import kotlinx.coroutines.*
import org.jetbrains.exposed.sql.*
import romka_po.plugins.database.schema.*
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import java.nio.channels.UnresolvedAddressException

fun Application.configureDatabases() {
    val database = Database.connect(
        url = "jdbc:postgresql://localhost:5432/postgres",
        user = "postgres",
        password = "root",
        driver = "org.postgresql.Driver",
    )
    val userService = UserService(database)
    val marksService = MarksService(database)
    val modelService = ModelService(database)

    val exc = CoroutineExceptionHandler { coroutineContext, throwable ->
        if (throwable is UnresolvedAddressException){

        }else{

        }
    }

    CoroutineScope(Dispatchers.Default+exc).launch() {
        modelService.readAll().forEach {
            if (it.image == "") {
                try {

                    val httpResponse = async {
                        HttpClient(CIO).get("https://bing-image-search1.p.rapidapi.com/images/search?q=${it.mark}%20${it.name}&count=1") {
                            headers {
                                append("X-RapidAPI-Key", "ef86fcbcf8msh670c42d42c19481p1a3e58jsn5e6fa5619efa")
                                append("X-RapidAPI-Host", "bing-image-search1.p.rapidapi.com")
                            }
                        }
                    }.await()

                    val data = async {
                        httpResponse.bodyAsText().substringAfter("contentUrl\": \"").substringBefore("\",")
                            .replace("\\", "")
                    }.await()
                    println(data)
                    println(HttpClient(CIO).get(data).status)

                    async {
                        HttpClient(CIO).use { httpClient ->
                            // Download pictures in the first way
                            httpClient.download(
                                data,
                                File("./src/main/resources/cars/${it.mark}_${it.name}.jpg").outputStream()
                            )
                        }
                    }.await()
                    if (File("./src/main/resources/cars/${it.mark}_${it.name}.jpg").exists()) {
                        modelService.update(
                            it.id,
                            it.copy(
                                image = "./src/main/resources/cars/${it.mark}_${it.name}.jpg\""
                            )
                        )
                    }
                } catch (_: Exception) {

                }
            }
        }
    }
    routing {
        get("/"){
            println("Hel")
            call.respondText("Hello World!")
        }
        // Create user
        post("/users") {
            val user = call.receive<ExposedUser>()
            val id = userService.create(user)
            call.respond(HttpStatusCode.Created, id)
        }
        // Read user
        get("/users/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            val user = userService.read(id)
            if (user != null) {
                call.respond(HttpStatusCode.OK, user)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
        // Update user
        put("/users/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            val user = call.receive<ExposedUser>()
            userService.update(id, user)
            call.respond(HttpStatusCode.OK)
        }
        // Delete user
        delete("/users/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            userService.delete(id)
            call.respond(HttpStatusCode.OK)
        }

        post("/mark") {
            val mark = call.receive<ExposedMark>()
            val id = marksService.create(mark)
            call.respond(HttpStatusCode.Created, id)
        }

        // Delete user
        delete("/users/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            userService.delete(id)
            call.respond(HttpStatusCode.OK)
        }


    }
}

suspend fun HttpClient.download(url: String, fileOutputStream: FileOutputStream) = withContext(SupervisorJob()+ Dispatchers.IO) {
    this@download.prepareGet(url).execute { httpResponse ->
        val channel: ByteReadChannel = httpResponse.body<ByteReadChannel>()
        while (!channel.isClosedForRead) {
            val packet = channel.readRemaining(DEFAULT_BUFFER_SIZE.toLong())
            if (!packet.isEmpty) {
                fileOutputStream.writePacket(packet)
            }
        }
    }
}

