package cl.duoc.cmqsalud.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import cl.duoc.cmqsalud.model.DocumentoFuncionario
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentoDao {
    @Query("SELECT * FROM documentos ORDER BY id DESC")
    fun obtenerTodos(): Flow<List<DocumentoFuncionario>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(documento: DocumentoFuncionario): Long

    @Update
    suspend fun actualizar(documento: DocumentoFuncionario)

    @Delete
    suspend fun eliminar(documento: DocumentoFuncionario)
}