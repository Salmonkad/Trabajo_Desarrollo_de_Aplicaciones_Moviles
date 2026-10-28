package cl.duoc.cmqsalud.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
    onVerDetalle: () -> Unit,
    onEditar: () -> Unit,
    onEliminar: () -> Unit,
    onCambiarEstado: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onVerDetalle() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
                Text(text = doc.titulo, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = DarkNavyColor)
                Text(text = "${doc.fecha} •${doc.centroSalud}", color = Color.Gray, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                BadgeEstado(
                    estado = doc.estado,
                    onClick = onCambiarEstado
                )
            }

            IconButton(onClick = onEditar) {
                Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color.Gray)
            }
            IconButton(onClick = onEliminar) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}