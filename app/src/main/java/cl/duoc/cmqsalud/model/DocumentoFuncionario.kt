package cl.duoc.cmqsalud.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class CategoriaDocumento(val etiqueta: String) {
    CERTIFICADO("Certificado"),
    INFORME("Informe Sanitario"),
    HISTORIAL("Historial Clínico"),
    LICENCIA("Licencia Médica"),
    INSUMOS("Solicitud Insumos"),
    OTRO("Otro")
}

enum class EstadoDocumento(val etiqueta: String) {
    PENDIENTE("Pendiente"),
    EN_REVISION("En Revisión"),
    VALIDADO("Validado")
}

@Entity(tableName = "documentos")
data class DocumentoFuncionario(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val titulo: String,
    val centroSalud: String,
    val categoria: CategoriaDocumento,
    val autor: String,
    val fecha: String,
    val observaciones: String,
    val estado: EstadoDocumento = EstadoDocumento.PENDIENTE,
    val rutaFoto: String? = null
)