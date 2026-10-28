package cl.duoc.cmqsalud.ui.login

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.duoc.cmqsalud.ui.componentes.DarkNavyColor
import cl.duoc.cmqsalud.ui.componentes.TealColor

/**
 * Valida un RUT chileno tanto en formato (ej: 12345678-K o 12.345.678-K)
 * como en el cálculo del dígito verificador (módulo 11).
 */
fun validarRutChileno(rutCompleto: String): Boolean {
    val limpio = rutCompleto.trim().replace(".", "").replace("-", "").uppercase()
    if (limpio.length !in 8..9) return false

    val cuerpo = limpio.substring(0, limpio.length - 1)
    val dv = limpio.last()

    // El cuerpo debe contener únicamente números
    if (!cuerpo.all { it.isDigit() }) return false

    // Algoritmo Módulo 11
    var suma = 0
    var multiplicador = 2
    for (i in cuerpo.indices.reversed()) {
        suma += (cuerpo[i] - '0') * multiplicador
        multiplicador = if (multiplicador == 7) 2 else multiplicador + 1
    }

    val resto = 11 - (suma % 11)
    val dvEsperado = when (resto) {
        11 -> '0'
        10 -> 'K'
        else -> resto.digitToChar()
    }

    return dv == dvEsperado
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(onLoginSuccess: (usuario: String, centro: String) -> Unit) {
    var rut by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var centroSeleccionado by remember { mutableStateOf("CESFAM Quilpué") }
    var expandidoCentro by remember { mutableStateOf(false) }
    var mostrarDialogoRecuperar by remember { mutableStateOf(false) }

    val centros = listOf("CESFAM Quilpué", "CESFAM Belloto Sur", "CESFAM Aviador Acevedo", "SAPU Pompeya")

    var errorRut by remember { mutableStateOf<String?>(null) }
    var errorPass by remember { mutableStateOf<String?>(null) }

    val campoTextoColores = OutlinedTextFieldDefaults.colors(
        focusedTextColor = DarkNavyColor,
        unfocusedTextColor = DarkNavyColor,
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        focusedLabelColor = TealColor,
        unfocusedLabelColor = Color.Gray,
        focusedBorderColor = TealColor,
        unfocusedBorderColor = Color.LightGray
    )

    if (mostrarDialogoRecuperar) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoRecuperar = false },
            title = { Text("Recuperar Contraseña", fontWeight = FontWeight.Bold, color = DarkNavyColor) },
            text = {
                Text("Para restablecer su clave institucional de acceso APS, comuníquese con el Departamento de Informática o Mesa de Ayuda de la Corporación Municipal de Quilpué.")
            },
            confirmButton = {
                Button(
                    onClick = { mostrarDialogoRecuperar = false },
                    colors = ButtonDefaults.buttonColors(containerColor = TealColor)
                ) {
                    Text("Entendido")
                }
            }
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF7F9FB)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.HealthAndSafety,
                    contentDescription = null,
                    tint = TealColor,
                    modifier = Modifier.size(68.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("CMQ Salud", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = DarkNavyColor)
                Text("Gestión de Documentos", fontSize = 13.sp, color = Color.Gray)

                Spacer(modifier = Modifier.height(24.dp))
                Text("Iniciar Sesión", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = DarkNavyColor)
                Text("Acceso para personal de salud municipal", fontSize = 13.sp, color = Color.Gray)

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = rut,
                    onValueChange = { nuevoTexto ->
                        // Filtrar caracteres: solo números, puntos, guion y letras k/K
                        val filtrado = nuevoTexto.filter { it.isDigit() || it == '-' || it == '.' || it == 'k' || it == 'K' }
                        // Evitar que escriban más de 12 caracteres (ej: 12.345.678-K)
                        if (filtrado.length <= 12) {
                            rut = filtrado
                            errorRut = null
                        }
                    },
                    label = { Text("RUT (ej: 12345678-K)") },
                    placeholder = { Text("12345678-K", color = Color.LightGray) },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = TealColor) },
                    isError = errorRut != null,
                    supportingText = { errorRut?.let { Text(it) } },
                    shape = RoundedCornerShape(12.dp),
                    colors = campoTextoColores,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; errorPass = null },
                    label = { Text("Contraseña") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = TealColor) },
                    visualTransformation = PasswordVisualTransformation(),
                    isError = errorPass != null,
                    supportingText = { errorPass?.let { Text(it) } },
                    shape = RoundedCornerShape(12.dp),
                    colors = campoTextoColores,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                ExposedDropdownMenuBox(
                    expanded = expandidoCentro,
                    onExpandedChange = { expandidoCentro = !expandidoCentro },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = centroSeleccionado,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Centro de Salud") },
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
                                    centroSeleccionado = opcion
                                    expandidoCentro = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        val rutValido = validarRutChileno(rut)
                        val passErr = if (password.isBlank() || password.length < 4) "Contraseña mínima 4 caracteres" else null

                        errorRut = if (!rutValido) "RUT inválido (formato o dígito verificador erróneo)" else null
                        errorPass = passErr

                        if (rutValido && passErr == null) {
                            onLoginSuccess(rut.trim().uppercase(), centroSeleccionado)
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TealColor),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Text("Ingresar", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))
                TextButton(onClick = { mostrarDialogoRecuperar = true }) {
                    Text("Olvidé mi contraseña", color = Color.Gray, fontSize = 13.sp)
                }
            }

            Text(
                "Departamento de Salud • Municipalidad de Quilpué",
                fontSize = 11.sp,
                color = Color.LightGray,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }
    }
}