import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Extracting Colors from the JSON
val GradientStart = Color(0xFFD5D9F9)
val TitlePurple = Color(0xFF4338CA)
val TextDark = Color(0xFF1C1D22)
val TextGray = Color(0xFF84899A)
val SurfaceLightGray = Color(0xFFF9F9F9)
val PrimaryIndigo = Color(0xFF4F46E5)
val MediaTextColor = Color(0xFF454955)

@Composable
fun ScamAnalyzerScreen() {
    // Scaffold provides the structural layout for the top content and bottom dockbar
    Scaffold(
        containerColor = Color.Transparent, // Let the background box show through
    ) { paddingValues ->

        // Background Gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(GradientStart, Color.White)
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues) // Respects bottom bar
                    .padding(top = 50.dp) // Status bar padding from JSON
            ) {
                TopHeader()

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    TextInputArea()
                    MediaActionsRow()
                    AnalyzeButton()
                }
            }
        }
    }
}

@Composable
private fun TopHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 64.dp, bottom = 24.dp, start = 24.dp, end = 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Scam Analyzer",
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = TitlePurple,
            lineHeight = 42.sp
        )
        Text(
            text = "Identify scams in messages, links, or images using our AI-powered analyzer.",
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            color = TextDark,
            lineHeight = 24.sp
        )
    }
}

@Composable
private fun TextInputArea() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 12.dp)
        ) {
            Text(
                text = "Message, link, or text here to analyze",
                fontSize = 16.sp,
                color = TextGray,
                modifier = Modifier.weight(1f)
            )

            // Paste from clipboard button
            Surface(
                modifier = Modifier.clickable { /* Handle paste */ },
                shape = CircleShape,
                color = SurfaceLightGray
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentPaste,
                        contentDescription = "Paste",
                        tint = TextDark,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Paste from clipboard",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextDark
                    )
                }
            }
        }
    }
}

@Composable
private fun MediaActionsRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MediaActionButton(
            modifier = Modifier.weight(1f),
            icon = Icons.Outlined.Image,
            text = "Attach\nimage",
            onClick = { /* Handle image attachment */ }
        )
        MediaActionButton(
            modifier = Modifier.weight(1f),
            icon = Icons.Outlined.Mic,
            text = "Record\nAudio",
            onClick = { /* Handle record */ }
        )
        MediaActionButton(
            modifier = Modifier.weight(1f),
            icon = Icons.Outlined.FileUpload,
            text = "Upload\nAudio",
            onClick = { /* Handle audio upload */ }
        )
    }
}

@Composable
private fun MediaActionButton(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(120.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TextDark,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = text,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MediaTextColor,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun AnalyzeButton() {
    Button(
        onClick = { /* Handle Analysis */ },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
        shape = RoundedCornerShape(9999.dp) // Fully rounded pill shape
    ) {
        Text(
            text = "Analyze Now",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Preview
@Composable
fun preview(){
    Surface {
        ScamAnalyzerScreen()
    }
}