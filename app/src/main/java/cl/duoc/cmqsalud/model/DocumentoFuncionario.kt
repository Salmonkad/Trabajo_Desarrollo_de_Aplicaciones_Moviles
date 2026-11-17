package cl.duoc.cmqsalud.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class PerfilUsuario(val etiqueta: String) {
    FUNCIONARIO("Funcionario"),
    JEFATURA("Jefatura"),
    ADMINISTRATIVO("Administrativo"),
    RECURSOS_HUMANOS("Recursos Humanos"),
    ADMINISTRADOR("Administrador del Sistema")
}

enum class CategoriaDocumento(val etiqueta: String) {
    CONTRATO("Contrato y Anexos"),
    CERTIFICADO("Certificado Laboral"),
    PERMISO("Solicitud de Permiso"),
    LICENCIA("Licencia Médica"),
    ANTECEDENTES("Antecedentes Personales"),
    HISTORIAL("Informe / Historial")
}

enum class EstadoDocumento(val etiqueta: String) {
    PENDIENTE("Pendiente de Revisión"),
    EN_REVISION("En Revisión"),
    VALIDADO("Aprobado / Validado"),
    RECHAZADO("Rechazado")
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
    val rutaFoto: String? = null,
    // Campos RF12: Trazabilidad, versión y rechazo
    val motivoRechazo: String = "",
    val revisadoPor: String = "",
    val fechaModificacion: String = "",
    val version: Int = 1
)