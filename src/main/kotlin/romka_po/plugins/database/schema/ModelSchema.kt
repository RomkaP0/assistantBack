package romka_po.plugins.database.schema

import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.Serializable
import org.h2.engine.Mode
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

@Serializable
data class ExposedModel(
    val id: String,
    val mark: String,
    val name: String,
    val cyrillic_name: String,
    val myclass: String,
    val year_from: Int,
    val year_to: Int?,
    val image:String
)

class ModelService(private val database: Database) {
    object Model : Table() {
        val id = varchar("id", length = 50)
        val mark = varchar("mark", length = 50)
        val name = varchar("name", length = 50)
        val cyrillic_name = varchar("cyrillic_name", length = 50)
        val myclass = varchar("myclass", length = 50)
        val year_from = integer("year_from")
        val year_to = integer("year_to")
        val image = text("image")

        override val primaryKey = PrimaryKey(id)
    }

    init {
        transaction(database) {
            SchemaUtils.create(Model)
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    suspend fun create(model: ExposedModel): String = dbQuery {
        Model.insert {
            it[id] = model.id
            it[mark] = model.mark
            it[name] = model.name
            it[cyrillic_name] = model.cyrillic_name
            it[myclass] = model.myclass
            it[year_from] = model.year_from
            it[year_to] = model.year_to ?: 0
            it[image] = model.image
        }[Model.id]
    }

    suspend fun read(id: String): ExposedModel? {
        return dbQuery {
            Model.select { Model.id eq id }
                .map {
                    ExposedModel(
                        it[Model.id],
                        it[Model.mark],
                        it[Model.name],
                        it[Model.cyrillic_name],
                        it[Model.myclass],
                        it[Model.year_from],
                        it[Model.year_to],
                        it[Model.image]
                    )
                }
                .singleOrNull()
        }
    }
    suspend fun readAll() :List<ExposedModel> {
        return dbQuery {
            Model.selectAll().map {
                ExposedModel(
                    it[Model.id],
                    it[Model.mark],
                    it[Model.name],
                    it[Model.cyrillic_name],
                    it[Model.myclass],
                    it[Model.year_from],
                    it[Model.year_to],
                    it[Model.image]
                )
            }.filter { it.image=="" }
        }
    }

    suspend fun update(id: String, model: ExposedModel) {
        dbQuery {
            Model.update({ Model.id eq id }) {
                it[mark] = model.mark
                it[name] = model.name
                it[cyrillic_name] = model.cyrillic_name
                it[myclass] = model.myclass
                it[year_from] = model.year_from
                it[year_to] = model.year_to ?: 0
                it[image] = model.image
            }
        }
    }

    suspend fun delete(id: String) {
        dbQuery {
            Model.deleteWhere { Model.id.eq(id) }
        }
    }
}
