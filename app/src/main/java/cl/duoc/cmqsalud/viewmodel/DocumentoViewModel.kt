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
    val categoria: CategoriaDocumento = CategoriaDocumento.CERTIFICADO,
    val observaciones: String = "",
    val rutaFoto: String? = null,
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

    fun cargarParaEditar(doc: DocumentoFuncionario) {
        uiState = RegistroUiState(
            idEdicion = doc.id,
            titulo = doc.titulo,
            centroSalud = doc.centroSalud,
            categoria = doc.categoria,
            observaciones = doc.observaciones,
            rutaFoto = doc.rutaFoto
        )
    }

    fun limpiarFormulario() {
        uiState = RegistroUiState()
    }

    fun guardarDocumento(onExito: () -> Unit) {
        val errorTitulo = when {
            uiState.titulo.isBlank() -> "El título es obligatorio"
            uiState.titulo.trim().length < 4 -> "Mínimo 4 caracteres"
            else -> null
        }
        val errorFoto = if (uiState.rutaFoto == null) "Debe capturar la foto del documento" else null

        if (errorTitulo != null || errorFoto != null) {
            uiState = uiState.copy(errorTitulo = errorTitulo, errorFoto = errorFoto)
            return
        }

        viewModelScope.launch {
            val fechaActual = SimpleDateFormat("dd MMM yyyy", Locale("es", "CL")).format(Date())

            if (uiState.idEdicion != null) {
                val docActualizado = DocumentoFuncionario(
                    id = uiState.idEdicion!!,
                    titulo = uiState.titulo.trim(),
                    centroSalud = uiState.centroSalud,
                    categoria = uiState.categoria,
                    autor = "Funcionario APS",
                    fecha = fechaActual,
                    observaciones = uiState.observaciones.trim(),
                    estado = EstadoDocumento.EN_REVISION,
                    rutaFoto = uiState.rutaFoto
                )
                repository.actualizarDocumento(docActualizado)
            } else {
                val nuevoDoc = DocumentoFuncionario(
                    titulo = uiState.titulo.trim(),
                    centroSalud = uiState.centroSalud,
                    categoria = uiState.categoria,
                    autor = "Funcionario APS",
                    fecha = fechaActual,
                    observaciones = uiState.observaciones.trim(),
                    estado = EstadoDocumento.PENDIENTE,
                    rutaFoto = uiState.rutaFoto
                )
                repository.guardarDocumento(nuevoDoc)
            }
            limpiarFormulario()
            onExito()
        }
    }

    fun eliminarDocumento(documento: DocumentoFuncionario) {
        viewModelScope.launch {
            repository.eliminarDocumento(documento)
        }
    }

    fun cambiarSiguienteEstado(documento: DocumentoFuncionario) {
        val nuevoEstado = when (documento.estado) {
            EstadoDocumento.PENDIENTE -> EstadoDocumento.EN_REVISION
            EstadoDocumento.EN_REVISION -> EstadoDocumento.VALIDADO
            EstadoDocumento.VALIDADO -> EstadoDocumento.PENDIENTE
        }
        viewModelScope.launch {
            repository.actualizarDocumento(documento.copy(estado = nuevoEstado))
        }
    }

}