package romka_po

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import romka_po.plugins.*
import romka_po.plugins.database.configureDatabases

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureSecurity()
    configureHTTP()
    configureSerialization()
    configureDatabases()
    configureRouting()
}
