package romka_po.plugins.database.schema

import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

@Serializable
data class ExposedMark(
    val id: String,
    val name: String,
    val cyrillic_name: String,
    val popular: Boolean,
    val country: String,
    val modelsCount: Int,
)

class MarksService(private val database: Database) {
    object Marks : Table() {
        val id = varchar("id", length = 50)
        val name = varchar("name", length = 50)
        val cyrillic_name = varchar("cyrillic_name", length = 50)
        val popular = bool("popular")
        val country = varchar("country", length = 50)
        val modelsCount = integer("modelsCount")

        override val primaryKey = PrimaryKey(id)
    }

    init {
        transaction(database) {
            SchemaUtils.create(Marks)
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    suspend fun create(mark: ExposedMark): String = dbQuery {
        Marks.insert {
            it[id] = mark.id
            it[name] = mark.name
            it[cyrillic_name] = mark.cyrillic_name
            it[popular] = mark.popular
            it[country] = mark.country
            it[modelsCount] = mark.modelsCount
        }[Marks.id]
    }

    suspend fun read(id: String): ExposedMark? {
        return dbQuery {
            Marks.select { Marks.id eq id }
                .map {
                    ExposedMark(
                        it[Marks.id],
                        it[Marks.name],
                        it[Marks.cyrillic_name],
                        it[Marks.popular],
                        it[Marks.country],
                        it[Marks.modelsCount]
                    )
                }
                .singleOrNull()
        }
    }
    suspend fun readAll():List<ExposedMark>{
        return dbQuery {
            Marks.selectAll().map (::resultRowToMark)
        }
    }
    private fun resultRowToMark(row: ResultRow) = ExposedMark(
        id = row[Marks.id],
        name = row[Marks.name],
        cyrillic_name = row[Marks.cyrillic_name],
        popular = row[Marks.popular],
        country = row[Marks.country],
        modelsCount = row[Marks.modelsCount]
    )

    suspend fun update(id: String, mark: ExposedMark) {
        dbQuery {
            Marks.update({ Marks.id eq id }) {
                it[name] = mark.name
                it[cyrillic_name] = mark.cyrillic_name
                it[popular] = mark.popular
                it[country] = mark.country
                it[modelsCount] = mark.modelsCount
            }
        }
    }

    suspend fun delete(id: String) {
        dbQuery {
            Marks.deleteWhere { Marks.id.eq(id) }
        }
    }
}
