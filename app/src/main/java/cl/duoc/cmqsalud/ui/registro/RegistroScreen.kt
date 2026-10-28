package cl.duoc.cmqsalud.ui.registro

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import cl.duoc.cmqsalud.model.CategoriaDocumento
import cl.duoc.cmqsalud.ui.componentes.DarkNavyColor
import cl.duoc.cmqsalud.ui.componentes.TealColor
import cl.duoc.cmqsalud.viewmodel.RegistroUiState
import java.io.File

fun crearArchivoUri(context: Context): Pair<File, Uri> {
    val dir = File(context.getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES), "documentos_cmq")
    if (!dir.exists()) dir.mkdirs()
    val archivo = File(dir, "DOC_${System.currentTimeMillis()}.jpg")
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", archivo)
    return Pair(archivo, uri)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroScreen(
    state: RegistroUiState,
    onTituloChange: (String) -> Unit,
    onCentroChange: (String) -> Unit = {},
    onCategoriaChange: (CategoriaDocumento) -> Unit,
    onObservacionesChange: (String) -> Unit,
    onFotoCapturada: (String) -> Unit,
    onGuardar: () -> Unit,
    onVolver: () -> Unit
) {
    val context = LocalContext.current
    var uriTemp by remember { mutableStateOf<Uri?>(null) }
    var expandidoCentro by remember { mutableStateOf(false) }
    val centros = listOf("CESFAM Quilpué", "CESFAM Belloto Sur", "CESFAM Aviador Acevedo", "SAPU Pompeya")

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { exitoso ->
        if (exitoso && uriTemp != null) {
            onFotoCapturada(uriTemp.toString())
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            try {
                val (_, uri) = crearArchivoUri(context)
                uriTemp = uri
                cameraLauncher.launch(uri)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val fileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uriSeleccionado ->
        uriSeleccionado?.let {
            onFotoCapturada(it.toString())
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.idEdicion != null) "Editar Expediente" else "Nuevo Expediente") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Selector de Centro de Salud emisor
            Text("Centro de Salud:", fontWeight = FontWeight.Bold, color = DarkNavyColor)
            Spacer(modifier = Modifier.height(6.dp))

            ExposedDropdownMenuBox(
                expanded = expandidoCentro,
                onExpandedChange = { expandidoCentro = !expandidoCentro },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = state.centroSalud,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Centro Emisor") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandidoCentro) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandidoCentro,
                    onDismissRequest = { expandidoCentro = false }
                ) {
                    centros.forEach { opcion ->
                        DropdownMenuItem(
                            text = { Text(opcion) },
                            onClick = {
                                onCentroChange(opcion)
                                expandidoCentro = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = state.titulo,
                onValueChange = onTituloChange,
                label = { Text("Título del Documento *") },
                isError = state.errorTitulo != null,
                supportingText = { state.errorTitulo?.let { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text("Categoría de Documento:", fontWeight = FontWeight.Bold, color = DarkNavyColor)
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(CategoriaDocumento.values()) { cat ->
                    FilterChip(
                        selected = state.categoria == cat,
                        onClick = { onCategoriaChange(cat) },
                        label = { Text(cat.etiqueta, fontSize = 12.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Respaldo Físico o Digital *", fontWeight = FontWeight.Bold, color = DarkNavyColor)
            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFECEFF1))
                    .border(
                        1.dp,
                        if (state.errorFoto != null) MaterialTheme.colorScheme.error else Color.LightGray,
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (state.rutaFoto != null) {
                    AsyncImage(
                        model = state.rutaFoto,
                        contentDescription = "Documento cargado",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.UploadFile, contentDescription = null, tint = TealColor, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Ningún archivo adjunto", color = Color.Gray, fontSize = 13.sp)
                    }
                }
            }

            if (state.errorFoto != null) {
                Text(
                    text = state.errorFoto,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { permissionLauncher.launch(android.Manifest.permission.CAMERA) },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Cámara", fontSize = 13.sp)
                }

                OutlinedButton(
                    onClick = { fileLauncher.launch("image/*") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Subir Archivo", fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = state.observaciones,
                onValueChange = onObservacionesChange,
                label = { Text("Observaciones / Notas") },
                maxLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onGuardar,
                colors = ButtonDefaults.buttonColors(containerColor = TealColor),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text(if (state.idEdicion != null) "Guardar Cambios" else "Subir Documento", fontSize = 16.sp)
            }
        }
    }
}