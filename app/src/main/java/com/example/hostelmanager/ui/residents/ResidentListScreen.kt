package com.example.hostelmanager.ui.residents

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.hostelmanager.R
import com.example.hostelmanager.data.ResidentWithRoom
import com.example.hostelmanager.ui.theme.AppBackground
import com.example.hostelmanager.ui.theme.AppBorder
import com.example.hostelmanager.ui.theme.AppCard
import com.example.hostelmanager.ui.theme.AppMuted
import com.example.hostelmanager.ui.theme.AppNavy
import com.example.hostelmanager.ui.theme.AppOrange
import com.example.hostelmanager.ui.theme.AppSoftBlue
import com.example.hostelmanager.ui.theme.AppSoftOrange
import com.example.hostelmanager.ui.theme.AppText
import java.io.File
import java.net.URLEncoder
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

private val WhatsAppGreen = Color(0xFF25D366)

@Composable
fun ResidentListScreen(
    hostelId: String,
    onAddClick: () -> Unit,
    onResidentClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ResidentListViewModel = viewModel()
) {
    LaunchedEffect(hostelId) { viewModel.setHostelId(hostelId) }

    val allResidents by viewModel.residents.collectAsState()
    val query by viewModel.query.collectAsState()

    val visibleResidents = remember(allResidents, query) {
        searchResidents(allResidents, query)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            if (allResidents.isNotEmpty()) {
                ResidentSearchDeck(
                    query = query,
                    resultCount = visibleResidents.size,
                    totalCount = allResidents.size,
                    onQueryChange = viewModel::setQuery,
                    onClearSearch = viewModel::clearSearch
                )
            }

            when {
                allResidents.isEmpty() -> EmptyResidentList()

                visibleResidents.isEmpty() -> NoMatchingResidents(
                    query = query,
                    onClearSearch = viewModel::clearSearch
                )

                else -> LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(top = 14.dp, bottom = 96.dp)
                ) {
                    items(visibleResidents, key = { it.person.id }) { residentWithRoom ->
                        ResidentCard(
                            residentWithRoom = residentWithRoom,
                            onClick = { onResidentClick(residentWithRoom.person.id) },
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onAddClick,
            shape = RoundedCornerShape(18.dp),
            containerColor = AppNavy,
            contentColor = Color.White,
            elevation = FloatingActionButtonDefaults.elevation(6.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
        ) {
            Icon(Icons.Default.PersonAdd, contentDescription = "Add Resident")
        }
    }
}

/* ------------------------------------------------------------------ */
/*  Search deck                                                        */
/* ------------------------------------------------------------------ */

@Composable
private fun ResidentSearchDeck(
    query: String,
    resultCount: Int,
    totalCount: Int,
    onQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit
) {
    Surface(
        color = AppCard,
        shadowElevation = 3.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 10.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                placeholder = {
                    Text("Name, room, ID or phone…", color = AppMuted, maxLines = 1)
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Clear search",
                                tint = AppMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                textStyle = MaterialTheme.typography.bodyLarge,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = AppText,
                    unfocusedTextColor = AppText,
                    focusedBorderColor = AppNavy,
                    unfocusedBorderColor = AppBorder,
                    focusedContainerColor = AppBackground,
                    unfocusedContainerColor = AppBackground,
                    cursorColor = AppNavy,
                    focusedLeadingIconColor = AppNavy,
                    unfocusedLeadingIconColor = AppMuted
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Showing $resultCount of $totalCount residents",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppMuted,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                if (query.isNotBlank()) {
                    Surface(
                        onClick = onClearSearch,
                        shape = RoundedCornerShape(50),
                        color = AppSoftOrange,
                        contentColor = AppOrange
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                "Clear search",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

/* ------------------------------------------------------------------ */
/*  Resident card                                                      */
/* ------------------------------------------------------------------ */

@Composable
fun ResidentCard(
    residentWithRoom: ResidentWithRoom,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val person = residentWithRoom.person
    val context = LocalContext.current
    var showMessageDialog by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AppCard),
        border = BorderStroke(1.dp, AppBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ResidentAvatar(photoPath = person.profilePhotoPath)

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    person.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(7.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(AppSoftBlue)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        "Room ${residentWithRoom.roomNumber}",
                        fontSize = 11.sp,
                        color = AppNavy,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.height(7.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        person.phone,
                        fontSize = 12.sp,
                        color = AppMuted,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        "${formatRupees(residentWithRoom.monthlyRent)}/mo",
                        fontSize = 12.sp,
                        color = AppNavy,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Call + Message side by side, larger icons.
            // Fixed 80.dp height keeps the card exactly as tall as before.
            Row(
                modifier = Modifier.height(80.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                IconButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${person.phone}"))
                        try {
                            context.startActivity(intent)
                        } catch (e: ActivityNotFoundException) {
                            Toast.makeText(context, "No dialer app found", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Call",
                        tint = AppNavy,
                        modifier = Modifier.size(26.dp)
                    )
                }
                IconButton(
                    onClick = { showMessageDialog = true },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Message,
                        contentDescription = "Message",
                        tint = AppNavy,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }
    }

    if (showMessageDialog) {
        val rentAmount = residentWithRoom.monthlyRent / 100
        MessageChoiceDialog(
            personName = person.name,
            phone = person.phone,
            rentAmount = rentAmount,
            onDismiss = { showMessageDialog = false },
            onWhatsAppClick = {
                val monthName = LocalDate.now().month.getDisplayName(TextStyle.FULL, Locale.getDefault())
                val message = "Hi ${person.name},\nPlease pay the rent ₹$rentAmount for $monthName.\nThanks!"
                val whatsappNumber = "91${person.phone}"
                val encodedMessage = URLEncoder.encode(message, "UTF-8")
                val whatsappUri = Uri.parse("https://wa.me/$whatsappNumber?text=$encodedMessage")
                val intent = Intent(Intent.ACTION_VIEW, whatsappUri)
                try {
                    context.startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(context, "WhatsApp not installed", Toast.LENGTH_SHORT).show()
                }
                showMessageDialog = false
            },
            onSmsClick = {
                val monthName = LocalDate.now().month.getDisplayName(TextStyle.FULL, Locale.getDefault())
                val message = "Hi ${person.name},\nPlease pay the rent ₹$rentAmount for $monthName.\nThanks!"
                val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${person.phone}"))
                intent.putExtra("sms_body", message)
                try {
                    context.startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(context, "No SMS app found", Toast.LENGTH_SHORT).show()
                }
                showMessageDialog = false
            }
        )
    }
}

/* ------------------------------------------------------------------ */
/*  Shared pieces (used by other screens — do not rename)              */
/* ------------------------------------------------------------------ */

@Composable
fun ResidentAvatar(photoPath: String?) {
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(AppSoftBlue),
        contentAlignment = Alignment.Center
    ) {
        if (photoPath.isNullOrBlank()) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = AppNavy,
                modifier = Modifier.size(26.dp)
            )
        } else {
            AsyncImage(
                model = File(photoPath),
                contentDescription = "Resident photo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
fun MessageChoiceDialog(
    personName: String,
    phone: String,
    rentAmount: Long,
    onDismiss: () -> Unit,
    onWhatsAppClick: () -> Unit,
    onSmsClick: () -> Unit
) {
    val bigStyle = MaterialTheme.typography.titleLarge

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = AppCard,
        titleContentColor = AppText,
        textContentColor = AppMuted,
        title = {
            Text("Send Message", style = bigStyle, fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text("Choose how to send the rent reminder to $personName:")
                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onWhatsAppClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WhatsAppGreen,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_whatsapp),
                        contentDescription = "WhatsApp",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("WhatsApp", style = bigStyle, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onSmsClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppNavy,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Sms,
                        contentDescription = "SMS",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("SMS", style = bigStyle, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = AppMuted)
            }
        }
    )
}

/* ------------------------------------------------------------------ */
/*  Empty states                                                       */
/* ------------------------------------------------------------------ */

@Composable
private fun EmptyResidentList() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(AppSoftBlue),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                tint = AppNavy,
                modifier = Modifier.size(40.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "No active residents in this hostel.",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = AppText,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            "Tap the + button to admit a new resident.",
            style = MaterialTheme.typography.bodyMedium,
            color = AppMuted
        )
    }
}

@Composable
private fun NoMatchingResidents(query: String, onClearSearch: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(AppSoftOrange),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.SearchOff,
                contentDescription = null,
                tint = AppOrange,
                modifier = Modifier.size(38.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "No residents match",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = AppText
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            "Nothing found for “$query”.\nTry a name, room number, Govt ID or phone number.",
            style = MaterialTheme.typography.bodyMedium,
            color = AppMuted,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(18.dp))
        Surface(
            onClick = onClearSearch,
            shape = RoundedCornerShape(14.dp),
            color = AppNavy,
            contentColor = Color.White
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Clear search", fontWeight = FontWeight.Bold)
            }
        }
    }
}

/* ------------------------------------------------------------------ */
/*  Helpers                                                            */
/* ------------------------------------------------------------------ */

private fun formatRupees(amountPaise: Long): String {
    val rupees = amountPaise / 100
    val formatted = NumberFormat.getNumberInstance(Locale("en", "IN")).apply {
        maximumFractionDigits = 0
        minimumFractionDigits = 0
    }.format(rupees)
    return "₹$formatted"
}