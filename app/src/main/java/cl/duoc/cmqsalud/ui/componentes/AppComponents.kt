package cl.duoc.cmqsalud.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.duoc.cmqsalud.model.DocumentoFuncionario
import cl.duoc.cmqsalud.model.EstadoDocumento

val TealColor = Color(0xFF007A78)
val DarkNavyColor = Color(0xFF0B2545)

@Composable
fun BadgeEstado(
    estado: EstadoDocumento,
    onClick: (() -> Unit)? = null
) {
    val (bgColor, textColor) = when (estado) {
        EstadoDocumento.EN_REVISION -> Color(0xFFFFF3E0) to Color(0xFFE65100)
        EstadoDocumento.VALIDADO -> Color(0xFFE0F2F1) to Color(0xFF00695C)
        EstadoDocumento.PENDIENTE -> Color(0xFFECEFF1) to Color(0xFF546E7A)
        EstadoDocumento.RECHAZADO -> Color(0xFFFFEBEE) to Color(0xFFC62828)
    }

    Text(
        text = estado.etiqueta,
        color = textColor,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bgColor)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}

@Composable
fun TarjetaExpediente(
    doc: DocumentoFuncionario,
    puedeAprobarRechazar: Boolean,
    puedeEliminar: Boolean,
    onVerDetalle: () -> Unit,
    onEditar: () -> Unit,
    onEliminar: () -> Unit,
    onAprobar: () -> Unit,
    onRechazar: () -> Unit
) {
    // Limpia cualquier RUT entre paréntesis en documentos guardados anteriormente
    val revisorLimpio = doc.revisadoPor.substringBefore(" (").trim()

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onVerDetalle() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE0F2F1)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Description, contentDescription = null, tint = TealColor)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = doc.titulo,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = DarkNavyColor,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "v${doc.version}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TealColor
                        )
                    }
                    Text(
                        text = "${doc.categoria.etiqueta} • ${doc.centroSalud}",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "Fecha: ${doc.fecha}",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    BadgeEstado(estado = doc.estado)
                }

                IconButton(onClick = onEditar) {
                    Icon(Icons.Default.Edit, contentDescription = "Modificar / Corregir", tint = Color.Gray)
                }
                if (puedeEliminar) {
                    IconButton(onClick = onEliminar) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            if (doc.estado == EstadoDocumento.RECHAZADO && doc.motivoRechazo.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFFFEBEE))
                        .padding(10.dp)
                ) {
                    Text(
                        text = "Motivo de rechazo: ${doc.motivoRechazo}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFC62828)
                    )
                    Text(
                        text = "Revisado por: $revisorLimpio (${doc.fechaModificacion}) • Pulse el lápiz para corregir y reenviar.",
                        fontSize = 11.sp,
                        color = Color(0xFFB71C1C)
                    )
                }
            } else if (revisorLimpio.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Revisado por: $revisorLimpio (${doc.fechaModificacion})",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }

            if (puedeAprobarRechazar && doc.estado != EstadoDocumento.VALIDADO) {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = Color(0xFFEEEEEE))
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onRechazar,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFC62828)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Rechazar", fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onAprobar,
                        colors = ButtonDefaults.buttonColors(containerColor = TealColor),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Aprobar", fontSize = 12.sp, color = Color.White)
                    }
                }
            }
        }
    }
}