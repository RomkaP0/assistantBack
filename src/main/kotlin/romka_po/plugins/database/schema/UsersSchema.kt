package romka_po.plugins.database.schema

import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import kotlinx.serialization.Serializable
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*

@Serializable
data class ExposedUser(val email: String, val password: String, val token:String, val verify:Boolean)
class UserService(private val database: Database) {
    object Users : Table() {
        val id = integer("id").autoIncrement()
        val email = varchar("email", length = 50)
        val password = varchar("password", length = 50)
        val token = varchar("token", length = 100)
        val verify = bool("verify")

        override val primaryKey = PrimaryKey(id)
    }

    init {
        transaction(database) {
            SchemaUtils.create(Users)
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    suspend fun create(user: ExposedUser): Int = dbQuery {
        Users.insert {
            it[email] = user.email
            it[password] = user.password
            it[token] = user.token
            it[verify] = user.verify
        }[Users.id]
    }

    suspend fun read(data: String): ExposedUser? {
        return dbQuery {
            Users.select { (Users.email eq data) or(Users.token eq data) }
                .map { ExposedUser(it[Users.email], it[Users.password], it[Users.token], it[Users.verify]) }
                .singleOrNull()
        }
    }

    suspend fun update(id: Int, user: ExposedUser) {
        dbQuery {
            Users.update({ Users.id eq id }) {
                it[email] = user.email
                it[password] = user.password
                it[token] = user.token
                it[verify] = user.verify
            }
        }
    }

    suspend fun delete(id: Int) {
        dbQuery {
            Users.deleteWhere { Users.id.eq(id) }
        }
    }
}
