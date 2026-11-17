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
import cl.duoc.cmqsalud.model.CategoriaDocumento
import cl.duoc.cmqsalud.model.DocumentoFuncionario
import cl.duoc.cmqsalud.model.PerfilUsuario
import cl.duoc.cmqsalud.ui.componentes.DarkNavyColor
import cl.duoc.cmqsalud.ui.componentes.TarjetaExpediente
import cl.duoc.cmqsalud.ui.componentes.TealColor

@Composable
fun HomeScreen(
    usuarioActivo: String,
    centroActivo: String,
    perfilActivo: PerfilUsuario,
    documentos: List<DocumentoFuncionario>,
    onNuevoDocumento: () -> Unit,
    onEditarDocumento: (DocumentoFuncionario) -> Unit,
    onEliminarDocumento: (DocumentoFuncionario) -> Unit,
    onAprobarDocumento: (DocumentoFuncionario) -> Unit,
    onRechazarDocumento: (DocumentoFuncionario, String) -> Unit,
    onCerrarSesion: () -> Unit
) {
    var filtro by remember { mutableStateOf("") }
    var pestanaSeleccionada by remember { mutableIntStateOf(0) }

    var docParaEliminar by remember { mutableStateOf<DocumentoFuncionario?>(null) }
    var docParaVer by remember { mutableStateOf<DocumentoFuncionario?>(null) }
    var docParaRechazar by remember { mutableStateOf<DocumentoFuncionario?>(null) }
    var motivoRechazoInput by remember { mutableStateOf("") }
    var errorMotivo by remember { mutableStateOf(false) }

    val puedeAprobarRechazar = perfilActivo == PerfilUsuario.RECURSOS_HUMANOS || perfilActivo == PerfilUsuario.JEFATURA
    val puedeEliminar = perfilActivo == PerfilUsuario.RECURSOS_HUMANOS || perfilActivo == PerfilUsuario.ADMINISTRADOR

    val documentosFiltrados = remember(filtro, documentos, perfilActivo, usuarioActivo, centroActivo) {
        val porPerfil = when (perfilActivo) {
            PerfilUsuario.FUNCIONARIO -> documentos.filter { it.autor == usuarioActivo }
            PerfilUsuario.JEFATURA -> documentos.filter {
                it.centroSalud == centroActivo && it.categoria != CategoriaDocumento.ANTECEDENTES
            }
            PerfilUsuario.ADMINISTRATIVO -> documentos.filter {
                it.centroSalud == centroActivo && it.categoria != CategoriaDocumento.ANTECEDENTES
            }
            PerfilUsuario.RECURSOS_HUMANOS, PerfilUsuario.ADMINISTRADOR -> documentos
        }
        if (filtro.isBlank()) porPerfil
        else porPerfil.filter {
            it.titulo.contains(filtro, ignoreCase = true) ||
                    it.categoria.etiqueta.contains(filtro, ignoreCase = true) ||
                    it.autor.contains(filtro, ignoreCase = true)
        }
    }

    if (docParaRechazar != null) {
        AlertDialog(
            onDismissRequest = {
                docParaRechazar = null
                motivoRechazoInput = ""
                errorMotivo = false
            },
            containerColor = Color.White,
            title = { Text("Rechazar Documento", fontWeight = FontWeight.Bold, color = DarkNavyColor) },
            text = {
                Column {
                    Text(
                        "Indique las observaciones para que el funcionario pueda corregir y reenviar el documento.",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = motivoRechazoInput,
                        onValueChange = {
                            motivoRechazoInput = it
                            errorMotivo = false
                        },
                        label = { Text("Motivo del rechazo *") },
                        placeholder = { Text("Ej: Falta firma o timbre legible") },
                        isError = errorMotivo,
                        supportingText = {
                            if (errorMotivo) Text("Debe indicar el motivo del rechazo")
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (motivoRechazoInput.trim().length < 4) {
                            errorMotivo = true
                        } else {
                            docParaRechazar?.let { onRechazarDocumento(it, motivoRechazoInput) }
                            docParaRechazar = null
                            motivoRechazoInput = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Confirmar Rechazo", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    docParaRechazar = null
                    motivoRechazoInput = ""
                }) {
                    Text("Cancelar", color = Color.Gray)
                }
            }
        )
    }

    if (docParaEliminar != null) {
        AlertDialog(
            onDismissRequest = { docParaEliminar = null },
            containerColor = Color.White,
            title = { Text("Eliminar Expediente", fontWeight = FontWeight.Bold, color = DarkNavyColor) },
            text = { Text("¿Está seguro de eliminar '${docParaEliminar?.titulo}'? Esta acción no se puede deshacer.", color = DarkNavyColor) },
            confirmButton = {
                Button(
                    onClick = {
                        docParaEliminar?.let { onEliminarDocumento(it) }
                        docParaEliminar = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { docParaEliminar = null }) {
                    Text("Cancelar", color = Color.Gray)
                }
            }
        )
    }

    if (docParaVer != null) {
        val revisorModalLimpio = docParaVer?.revisadoPor?.substringBefore(" (")?.trim() ?: ""
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
                        text = "${docParaVer?.titulo} (v${docParaVer?.version})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = DarkNavyColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${docParaVer?.categoria?.etiqueta} • ${docParaVer?.centroSalud}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "Fecha de carga: ${docParaVer?.fecha}",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (perfilActivo == PerfilUsuario.ADMINISTRADOR && docParaVer?.categoria == CategoriaDocumento.ANTECEDENTES) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .background(Color(0xFFFFEBEE), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Contenido documental sensible restringido para el perfil Administrador del Sistema.",
                                color = Color(0xFFC62828),
                                fontSize = 12.sp
                            )
                        }
                    } else if (!docParaVer?.rutaFoto.isNullOrBlank()) {
                        AsyncImage(
                            model = docParaVer?.rutaFoto,
                            contentDescription = "Foto expediente",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (!docParaVer?.observaciones.isNullOrBlank()) "Observaciones: ${docParaVer?.observaciones}" else "Sin observaciones adicionales.",
                        fontSize = 13.sp,
                        color = DarkNavyColor,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (revisorModalLimpio.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Revisado por: $revisorModalLimpio (${docParaVer?.fechaModificacion})",
                            fontSize = 12.sp,
                            color = TealColor,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { docParaVer = null },
                        colors = ButtonDefaults.buttonColors(containerColor = TealColor),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cerrar", color = Color.White)
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
                    label = { Text("Expedientes") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNuevoDocumento,
                    icon = { Icon(Icons.Default.DocumentScanner, contentDescription = "Cargar") },
                    label = { Text("Cargar") }
                )
                NavigationBarItem(
                    selected = pestanaSeleccionada == 2,
                    onClick = { pestanaSeleccionada = 2 },
                    icon = { Icon(Icons.Default.VerifiedUser, contentDescription = "Revisión") },
                    label = { Text("Revisión") }
                )
                NavigationBarItem(
                    selected = pestanaSeleccionada == 3,
                    onClick = { pestanaSeleccionada = 3 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Perfil") },
                    label = { Text("Mi Perfil") }
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.HealthAndSafety,
                        contentDescription = "Logo CMQ",
                        tint = TealColor,
                        modifier = Modifier.size(38.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(text = "CMQ Salud", fontWeight = FontWeight.ExtraBold, fontSize = 19.sp, color = TealColor)
                        Text(text = centroActivo, fontSize = 12.sp, color = Color.Gray)
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(end = 8.dp)) {
                        Text(text = perfilActivo.etiqueta, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = DarkNavyColor)
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
                            text = perfilActivo.etiqueta.take(1).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            when (pestanaSeleccionada) {
                0, 2 -> {
                    Text(
                        if (pestanaSeleccionada == 0) "Expedientes (${perfilActivo.etiqueta})" else "Revisión de Solicitudes",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkNavyColor
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = filtro,
                        onValueChange = { filtro = it },
                        label = { Text("Buscar por título o categoría...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

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
                                    puedeAprobarRechazar = puedeAprobarRechazar,
                                    puedeEliminar = puedeEliminar,
                                    onVerDetalle = { docParaVer = doc },
                                    onEditar = { onEditarDocumento(doc) },
                                    onEliminar = { docParaEliminar = doc },
                                    onAprobar = { onAprobarDocumento(doc) },
                                    onRechazar = { docParaRechazar = doc }
                                )
                            }
                        }
                    }
                }

                3 -> {
                    Text("Control de Acceso y Perfil", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DarkNavyColor)
                    Spacer(modifier = Modifier.height(14.dp))

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Credenciales de Sesión Activa", fontWeight = FontWeight.Bold, color = DarkNavyColor)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("RUT Funcionario: $usuarioActivo", fontSize = 14.sp, color = DarkNavyColor)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Perfil Oficial: ${perfilActivo.etiqueta}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TealColor)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Sucursal: $centroActivo", fontSize = 14.sp, color = DarkNavyColor)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = onCerrarSesion,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cerrar Sesión / Cambiar Perfil", color = Color.White)
                    }
                }
            }
        }
    }
}