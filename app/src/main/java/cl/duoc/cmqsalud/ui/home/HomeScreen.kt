package cl.duoc.cmqsalud.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import cl.duoc.cmqsalud.model.DocumentoFuncionario
import cl.duoc.cmqsalud.ui.componentes.BadgeEstado
import cl.duoc.cmqsalud.ui.componentes.DarkNavyColor
import cl.duoc.cmqsalud.ui.componentes.TarjetaExpediente
import cl.duoc.cmqsalud.ui.componentes.TealColor

@Composable
fun HomeScreen(
    usuarioActivo: String,
    centroActivo: String,
    documentos: List<DocumentoFuncionario>,
    onNuevoDocumento: () -> Unit,
    onEditarDocumento: (DocumentoFuncionario) -> Unit,
    onEliminarDocumento: (DocumentoFuncionario) -> Unit,
    onCambiarEstado: (DocumentoFuncionario) -> Unit,
    onCerrarSesion: () -> Unit
) {
    var filtro by remember { mutableStateOf("") }
    var pestanaSeleccionada by remember { mutableIntStateOf(0) }

    // Estados para diálogos emergentes
    var docParaEliminar by remember { mutableStateOf<DocumentoFuncionario?>(null) }
    var docParaVer by remember { mutableStateOf<DocumentoFuncionario?>(null) }

    val documentosFiltrados = remember(filtro, documentos) {
        if (filtro.isBlank()) documentos
        else documentos.filter { it.titulo.contains(filtro, ignoreCase = true) }
    }

    // confirmación de eliminación
    if (docParaEliminar != null) {
        AlertDialog(
            onDismissRequest = { docParaEliminar = null },
            title = { Text("Eliminar Expediente", fontWeight = FontWeight.Bold, color = DarkNavyColor) },
            text = { Text("¿Está seguro de eliminar '${docParaEliminar?.titulo}'? Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(
                    onClick = {
                        docParaEliminar?.let { onEliminarDocumento(it) }
                        docParaEliminar = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { docParaEliminar = null }) {
                    Text("Cancelar", color = Color.Gray)
                }
            }
        )
    }

    // vista rápida con imagen completa y notas
    if (docParaVer != null) {
        Dialog(onDismissRequest = { docParaVer = null }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = docParaVer?.titulo ?: "",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = DarkNavyColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${docParaVer?.categoria?.etiqueta} • ${docParaVer?.fecha}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (!docParaVer?.rutaFoto.isNullOrBlank()) {
                        AsyncImage(
                            model = docParaVer?.rutaFoto,
                            contentDescription = "Foto expediente",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .background(Color(0xFFF0F0F0), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Sin imagen adjunta", color = Color.Gray)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (!docParaVer?.observaciones.isNullOrBlank()) "Notas: ${docParaVer?.observaciones}" else "Sin observaciones adicionales.",
                        fontSize = 13.sp,
                        color = DarkNavyColor,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { docParaVer = null },
                        colors = ButtonDefaults.buttonColors(containerColor = TealColor),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cerrar")
                    }
                }
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            if (pestanaSeleccionada == 0 || pestanaSeleccionada == 2) {
                FloatingActionButton(
                    onClick = onNuevoDocumento,
                    containerColor = TealColor,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Subir Expediente")
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = pestanaSeleccionada == 0,
                    onClick = { pestanaSeleccionada = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
                    label = { Text("Inicio") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNuevoDocumento,
                    icon = { Icon(Icons.Default.DocumentScanner, contentDescription = "Escanear") },
                    label = { Text("Escanear") }
                )
                NavigationBarItem(
                    selected = pestanaSeleccionada == 2,
                    onClick = { pestanaSeleccionada = 2 },
                    icon = { Icon(Icons.Default.Inbox, contentDescription = "Bandeja") },
                    label = { Text("Bandeja") }
                )
                NavigationBarItem(
                    selected = pestanaSeleccionada == 3,
                    onClick = { pestanaSeleccionada = 3 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Ajustes") },
                    label = { Text("Ajustes") }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF7F9FB))
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            // Encabezado institucional
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "CMQ Salud", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = TealColor)
                    Text(text = centroActivo, fontSize = 12.sp, color = Color.Gray)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(end = 8.dp)) {
                        Text(text = "Funcionario", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DarkNavyColor)
                        Text(text = usuarioActivo, fontSize = 11.sp, color = Color.Gray)
                    }
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(TealColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (usuarioActivo.isNotEmpty()) usuarioActivo.take(1).uppercase() else "F",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (pestanaSeleccionada) {
                0, 2 -> {
                    Text(
                        if (pestanaSeleccionada == 0) "Mis Documentos" else "Bandeja de Entrada",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkNavyColor
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = filtro,
                        onValueChange = { filtro = it },
                        label = { Text("Buscar documentos...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (documentosFiltrados.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No hay expedientes registrados", color = Color.Gray)
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(documentosFiltrados, key = { it.id }) { doc ->
                                TarjetaExpediente(
                                    doc = doc,
                                    onVerDetalle = { docParaVer = doc },
                                    onEditar = { onEditarDocumento(doc) },
                                    onEliminar = { docParaEliminar = doc },
                                    onCambiarEstado = { onCambiarEstado(doc) }
                                )
                            }
                        }
                    }
                }

                3 -> {
                    Text("Ajustes del Sistema", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = DarkNavyColor)
                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Información de Sesión", fontWeight = FontWeight.Bold, color = DarkNavyColor)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("RUT: $usuarioActivo", fontSize = 14.sp)
                            Text("Establecimiento: $centroActivo", fontSize = 14.sp)
                            Text("Base de Datos: SQLite (Room Local)", fontSize = 14.sp, color = Color.Gray)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = onCerrarSesion,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cerrar Sesión")
                    }
                }
            }
        }
    }
}