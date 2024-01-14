package romka_po.plugins

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader

@Serializable
data class Make(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("cyrillic-name") val cyrillicName: String,
    @SerialName("popular") val popular: Boolean,
    @SerialName("country") val country: String,
    @SerialName("models") val models: ArrayList<Models> = arrayListOf(),
)
@Serializable
data class Models(
    @SerialName("id") var id: String,
    @SerialName("name") var name: String,
    @SerialName("cyrillic-name") var cyrillicName: String,
    @SerialName("class") var type: String,
    @SerialName("year-from") var yearFrom:Int,
    @SerialName("year-to") var yearTo: Int?
)

fun Application.configureRouting() {
    routing {
        get("/") {

//            val classLoader = Thread.currentThread().contextClassLoader
//            val resourcePath = classLoader.getResource("cars.json").path
//            val file = File(resourcePath)
////            file.writeText("Hello, world!")
//
//            val string = try {
//
//                val bufferedReader = BufferedReader(InputStreamReader(file.inputStream()))
//                val stringBuilder = StringBuilder()
//                bufferedReader.useLines { lines ->
//                    lines.forEach {
//                        stringBuilder.append(it)
//                    }
//                }
//                stringBuilder.toString()
//            } catch (e: Exception) {
//                e.printStackTrace()
//                ""
//            }
//            Json.decodeFromString<List<Make>>(string).forEach {
//                println(it.models.size)
////                makes.add(it.copy(modelsCount = count))
//            }
            call.respondText("Hello World!")
        }
    }
}
