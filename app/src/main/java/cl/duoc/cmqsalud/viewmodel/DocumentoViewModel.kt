package cl.duoc.cmqsalud.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.cmqsalud.data.local.AppDatabase
import cl.duoc.cmqsalud.data.repositorio.DocumentoRepository
import cl.duoc.cmqsalud.model.CategoriaDocumento
import cl.duoc.cmqsalud.model.DocumentoFuncionario
import cl.duoc.cmqsalud.model.EstadoDocumento
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class RegistroUiState(
    val idEdicion: Int? = null,
    val titulo: String = "",
    val centroSalud: String = "CESFAM Quilpué",
    val categoria: CategoriaDocumento = CategoriaDocumento.CONTRATO,
    val observaciones: String = "",
    val rutaFoto: String? = null,
    val versionActual: Int = 1,
    val errorTitulo: String? = null,
    val errorFoto: String? = null
)

class DocumentoViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = DocumentoRepository(
        AppDatabase.getDatabase(application).documentoDao()
    )

    val documentos: StateFlow<List<DocumentoFuncionario>> = repository.documentos
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    var uiState by mutableStateOf(RegistroUiState())
        private set

    private fun obtenerFechaHoraActual(): String {
        return SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "CL")).format(Date())
    }

    fun onTituloChange(valor: String) {
        uiState = uiState.copy(titulo = valor, errorTitulo = null)
    }

    fun onCategoriaChange(valor: CategoriaDocumento) {
        uiState = uiState.copy(categoria = valor)
    }

    fun onObservacionesChange(valor: String) {
        uiState = uiState.copy(observaciones = valor)
    }

    fun onFotoCapturada(uriString: String) {
        uiState = uiState.copy(rutaFoto = uriString, errorFoto = null)
    }

    fun onCentroChange(valor: String) {
        uiState = uiState.copy(centroSalud = valor)
    }

    fun cargarParaEditar(doc: DocumentoFuncionario) {
        uiState = RegistroUiState(
            idEdicion = doc.id,
            titulo = doc.titulo,
            centroSalud = doc.centroSalud,
            categoria = doc.categoria,
            observaciones = doc.observaciones,
            rutaFoto = doc.rutaFoto,
            versionActual = doc.version
        )
    }

    fun limpiarFormulario() {
        uiState = RegistroUiState()
    }

    fun guardarDocumento(usuarioActivo: String, onExito: () -> Unit) {
        val errorTitulo = when {
            uiState.titulo.isBlank() -> "El título es obligatorio"
            uiState.titulo.trim().length < 4 -> "Mínimo 4 caracteres"
            else -> null
        }
        val errorFoto = if (uiState.rutaFoto == null) "Debe adjuntar o capturar el respaldo documental" else null

        if (errorTitulo != null || errorFoto != null) {
            uiState = uiState.copy(errorTitulo = errorTitulo, errorFoto = errorFoto)
            return
        }

        viewModelScope.launch {
            val ahora = obtenerFechaHoraActual()

            if (uiState.idEdicion != null) {
                // RF12: Al modificar o corregir un documento, vuelve a "Pendiente de revisión" y sube la versión
                val docActualizado = DocumentoFuncionario(
                    id = uiState.idEdicion!!,
                    titulo = uiState.titulo.trim(),
                    centroSalud = uiState.centroSalud,
                    categoria = uiState.categoria,
                    autor = usuarioActivo,
                    fecha = ahora,
                    observaciones = uiState.observaciones.trim(),
                    estado = EstadoDocumento.PENDIENTE,
                    rutaFoto = uiState.rutaFoto,
                    motivoRechazo = "",
                    revisadoPor = "Reenviado por $usuarioActivo",
                    fechaModificacion = ahora,
                    version = uiState.versionActual + 1
                )
                repository.actualizarDocumento(docActualizado)
            } else {
                val nuevoDoc = DocumentoFuncionario(
                    titulo = uiState.titulo.trim(),
                    centroSalud = uiState.centroSalud,
                    categoria = uiState.categoria,
                    autor = usuarioActivo,
                    fecha = ahora,
                    observaciones = uiState.observaciones.trim(),
                    estado = EstadoDocumento.PENDIENTE,
                    rutaFoto = uiState.rutaFoto,
                    fechaModificacion = ahora,
                    version = 1
                )
                repository.guardarDocumento(nuevoDoc)
            }
            limpiarFormulario()
            onExito()
        }
    }

    fun aprobarDocumento(documento: DocumentoFuncionario, revisor: String) {
        viewModelScope.launch {
            val ahora = obtenerFechaHoraActual()
            repository.actualizarDocumento(
                documento.copy(
                    estado = EstadoDocumento.VALIDADO,
                    motivoRechazo = "",
                    revisadoPor = revisor,
                    fechaModificacion = ahora
                )
            )
        }
    }

    fun rechazarDocumento(documento: DocumentoFuncionario, motivo: String, revisor: String) {
        viewModelScope.launch {
            val ahora = obtenerFechaHoraActual()
            repository.actualizarDocumento(
                documento.copy(
                    estado = EstadoDocumento.RECHAZADO,
                    motivoRechazo = motivo.trim(),
                    revisadoPor = revisor,
                    fechaModificacion = ahora
                )
            )
        }
    }

    fun eliminarDocumento(documento: DocumentoFuncionario) {
        viewModelScope.launch {
            repository.eliminarDocumento(documento)
        }
    }
}