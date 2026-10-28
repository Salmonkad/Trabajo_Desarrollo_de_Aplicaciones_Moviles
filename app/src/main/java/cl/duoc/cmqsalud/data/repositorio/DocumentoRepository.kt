package cl.duoc.cmqsalud.data.repositorio

import cl.duoc.cmqsalud.data.local.DocumentoDao
import cl.duoc.cmqsalud.model.DocumentoFuncionario
import kotlinx.coroutines.flow.Flow

class DocumentoRepository(private val dao: DocumentoDao) {
    val documentos: Flow<List<DocumentoFuncionario>> = dao.obtenerTodos()

    suspend fun guardarDocumento(documento: DocumentoFuncionario) = dao.insertar(documento)
    suspend fun actualizarDocumento(documento: DocumentoFuncionario) = dao.actualizar(documento)
    suspend fun eliminarDocumento(documento: DocumentoFuncionario) = dao.eliminar(documento)
}