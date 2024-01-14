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
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.*
import romka_po.plugins.Make
import romka_po.plugins.database.schema.*
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.lang.Exception
import java.nio.channels.UnresolvedAddressException

@Serializable
data class User(
    val email: String,
    val password: String = "",
)

fun Application.configureDatabases() {
    val database = Database.connect(
//        url = "jdbc:postgresql://assistant-db:5432/postgres",
        url = "jdbc:postgresql://localhost:5432/postgres",
        driver = "org.postgresql.Driver",
        user = "postgres",
        password = "root"
    )
    val userService = UserService(database)
    val marksService = MarksService(database)
    val modelService = ModelService(database)

    val exc = CoroutineExceptionHandler { coroutineContext, throwable ->
        if (throwable is UnresolvedAddressException) {

        } else {

        }
    }
//
//    CoroutineScope(Dispatchers.Default + exc).launch() {
//        val string = try {
//            val file = File("./src/main/resources/cars.json").inputStream()
//
//            val bufferedReader = BufferedReader(InputStreamReader(file))
//            val stringBuilder = StringBuilder()
//            bufferedReader.useLines { lines ->
//                lines.forEach {
//                    stringBuilder.append(it)
//                }
//            }
//            stringBuilder.toString()
//        } catch (e: Exception) {
//            e.printStackTrace()
//            ""
//        }
//        Json.decodeFromString<List<Make>>(string).forEach { make ->
//            marksService.create(
//                ExposedMark(
//                    make.id,
//                    make.name,
//                    make.cyrillicName,
//                    make.popular,
//                    make.country,
//                    make.models.size
//                )
//            )
//            make.models.forEach {
//                modelService.create(
//                    ExposedModel(
//                        it.id,
//                        make.id,
//                        it.name, it.cyrillicName,
//                        it.type,
//                        it.yearFrom,
//                        it.yearTo,
//                        if (File("./src/main/resources/cars/${make.id}_${it.name}.jpg").exists()) "./src/main/resources/cars/${make.id}_${it.name}.jpg" else ""
//                    )
//                )
//            }
//        }
//    }

//    CoroutineScope(Dispatchers.Default+exc).launch() {
//        modelService.readAll().forEach {
//            if (it.image == "") {
//                try {
//
//                    val httpResponse = async {
//                        HttpClient(CIO).get("https://bing-image-search1.p.rapidapi.com/images/search?q=${it.mark}%20${it.name}&count=1") {
//                            headers {
//                                append("X-RapidAPI-Key", "ef86fcbcf8msh670c42d42c19481p1a3e58jsn5e6fa5619efa")
//                                append("X-RapidAPI-Host", "bing-image-search1.p.rapidapi.com")
//                            }
//                        }
//                    }.await()
//
//                    val data = async {
//                        httpResponse.bodyAsText().substringAfter("contentUrl\": \"").substringBefore("\",")
//                            .replace("\\", "")
//                    }.await()
//                    println(data)
//                    println(HttpClient(CIO).get(data).status)
//
//                    async {
//                        HttpClient(CIO).use { httpClient ->
//                            // Download pictures in the first way
//                            httpClient.download(
//                                data,
//                                File("./src/main/resources/cars/${it.mark}_${it.name}.jpg").outputStream()
//                            )
//                        }
//                    }.await()
//                    if (File("./src/main/resources/cars/${it.mark}_${it.name}.jpg").exists()) {
//                        modelService.update(
//                            it.id,
//                            it.copy(
//                                image = "./src/main/resources/cars/${it.mark}_${it.name}.jpg\""
//                            )
//                        )
//                    }
//                } catch (_: Exception) {
//
//                }
//            }
//        }
//    }

    suspend fun checkProfileExist(data: String): Boolean {
        val user = userService.read(data)
        return user != null
    }
    routing {
        post("users/token/send") {
            val token = call.receive<String>().replace("\"", "")
            if (checkProfileExist(token)) {
                call.respond(HttpStatusCode.Conflict)
            } else {
                //Поменять на номер телефона
                userService.create(
                    ExposedUser(
                        email = "", password = "", token = token, verify = true
                    )
                )
                call.respond(HttpStatusCode.OK)
            }
        }
        post("users/password/send") {
            val user = call.receive<User>()
//            if (checkProfileExist(user.email)) {
//                call.respond(HttpStatusCode.Conflict)
//            } else {
//                userService.create(
//                    ExposedUser(
//                        email = user.email, password = user.password, token = "", verify = false
//                    )
//                )
                call.respond(HttpStatusCode.OK)
//            }
        }
//        // Create user
//        post("/users") {
//            val user = call.receive<ExposedUser>()
//            val id = userService.create(user)
//            call.respond(HttpStatusCode.Created, id)
//        }
//        // Read user
//        get("/users/{id}") {
//            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
//            val user = userService.read(id)
//            if (user != null) {
//                call.respond(HttpStatusCode.OK, user)
//            } else {
//                call.respond(HttpStatusCode.NotFound)
//            }
//        }
//        // Update user
//        put("/users/{id}") {
//            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
//            val user = call.receive<ExposedUser>()
//            userService.update(id, user)
//            call.respond(HttpStatusCode.OK)
//        }
//        // Delete user
//        delete("/users/{id}") {
//            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
//            userService.delete(id)
//            call.respond(HttpStatusCode.OK)
//        }

        get("/marks") {
            val mark = marksService.readAll()
            call.respond(mark)
        }
        get("/marks/{id}") {
            val id = call.parameters["id"] ?: throw IllegalArgumentException("Invalid ID")
            val mark = marksService.read(id)
            call.respond(mark!!)
        }
        get("/models/{make_id}") {
            val makeId = call.parameters["make_id"] ?: throw IllegalArgumentException("Invalid ID")
            val models = modelService.readModelsFromMark(makeId)

            call.respond(models)
        }
        get("/models") {
            val models = modelService.readAll()
            call.respond(models)
        }

    }
}

suspend fun HttpClient.download(url: String, fileOutputStream: FileOutputStream) =
    withContext(SupervisorJob() + Dispatchers.IO) {
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

