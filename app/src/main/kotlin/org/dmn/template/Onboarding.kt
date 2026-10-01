package org.dmn.template

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val stepTitles = listOf(
    "About you",
    "Identity",
    "Your car",
    "Car photos & papers",
    "Location",
    "Review"
)

private val stepIntros = listOf(
    "We use these to confirm who you are. Enter them exactly as they appear on your documents.",
    "Clear photos, all corners visible. Your selfie must be taken now so we know it is really you.",
    "Riders see these details before they get in.",
    "Papers can come from your gallery. Car photos must be taken now, with the number plate visible.",
    "Pick Up needs your location all the time you are online.",
    "Check everything, then send it for review."
)

@Composable
private fun FormField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    keyboard: KeyboardType = KeyboardType.Text,
    maxLength: Int = 60,
    digitsOnly: Boolean = false
) {
    Text(label, color = PuMuted, fontSize = 13.sp)
    Spacer(Modifier.height(6.dp))
    OutlinedTextField(
        value = value,
        onValueChange = { input ->
            if (input.length <= maxLength && (!digitsOnly || input.all { it.isDigit() })) onChange(input)
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboard),
        modifier = Modifier.fillMaxWidth(),
        colors = fieldColors()
    )
    Spacer(Modifier.height(14.dp))
}

/** A photo request. Camera always works; the gallery is offered only when [cameraOnly] is false. */
@Composable
private fun DocCard(
    title: String,
    hint: String,
    added: Boolean,
    cameraOnly: Boolean,
    onAdded: () -> Unit
) {
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) onAdded()
    }
    val gallery = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) onAdded()
    }

    InfoCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = PuInk, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(hint, color = PuMuted, fontSize = 12.sp)
            }
            Spacer(Modifier.width(8.dp))
            if (added) Pill("Added ✓", PuGood, Color.White) else Pill("Required", PuChip, PuMuted)
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlineButton(
                text = if (added) "Retake" else "Take photo",
                onClick = { camera.launch(null) },
                modifier = Modifier.weight(1f)
            )
            if (!cameraOnly) {
                OutlineButton(
                    text = "Gallery",
                    onClick = {
                        gallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
    Spacer(Modifier.height(12.dp))
}

@Composable
private fun ConsentRow(text: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.Top) {
        Checkbox(
            checked = checked,
            onCheckedChange = onChange,
            colors = CheckboxDefaults.colors(
                checkedColor = PuAmber,
                checkmarkColor = PuAmberInk,
                uncheckedColor = PuMuted
            )
        )
        Text(text, color = PuInk, fontSize = 14.sp, modifier = Modifier.padding(top = 12.dp))
    }
}

@Composable
fun OnboardingScreen(onSubmitted: () -> Unit) {
    val context = LocalContext.current
    var step by rememberSaveable { mutableStateOf(0) }

    var name by rememberSaveable { mutableStateOf("") }
    var nin by rememberSaveable { mutableStateOf("") }
    var licence by rememberSaveable { mutableStateOf("") }

    var docsAdded by rememberSaveable { mutableStateOf("") }

    var carModel by rememberSaveable { mutableStateOf("") }
    var carYear by rememberSaveable { mutableStateOf("") }
    var carColour by rememberSaveable { mutableStateOf("") }
    var plate by rememberSaveable { mutableStateOf("") }

    var agreed by rememberSaveable { mutableStateOf(false) }

    val locationOk = rememberAlwaysLocationGranted()
    val requestLocation = rememberLocationRequester()

    fun has(key: String): Boolean = docsAdded.split(",").contains(key)
    fun add(key: String) {
        if (!has(key)) docsAdded += "$key,"
    }

    val canNext = when (step) {
        0 -> name.trim().length >= 3 && nin.length == 11 && licence.length >= 8
        1 -> has("nin") && has("licence") && has("selfie")
        2 -> carModel.trim().length >= 2 && carYear.length == 4 &&
            carColour.trim().length >= 3 && plate.trim().length >= 5
        3 -> listOf("reg", "ins", "road", "front", "back", "inside").all { has(it) }
        4 -> locationOk
        else -> agreed
    }

    val scroll = rememberScrollState()
    LaunchedEffect(step) { scroll.scrollTo(0) }

    // Back goes to the previous step, not out of the wizard.
    BackHandler(enabled = step > 0) { step-- }

    Surface(modifier = Modifier.fillMaxSize(), color = PuBg) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .imePadding()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Step ${step + 1} of ${stepTitles.size}", color = PuMuted, fontSize = 13.sp)
                TextButton(onClick = onSubmitted) {
                    Text("Skip (demo)", color = PuMuted, fontSize = 12.sp)
                }
            }
            Text(stepTitles[step], color = PuInk, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(50))
                    .background(PuChip)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth((step + 1) / stepTitles.size.toFloat())
                        .height(6.dp)
                        .clip(RoundedCornerShape(50))
                        .background(PuAmber)
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(stepIntros[step], color = PuMuted, fontSize = 13.sp)
            Spacer(Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scroll)
            ) {
                when (step) {
                    0 -> {
                        FormField("Full name (as on your ID)", name, { name = it })
                        FormField(
                            "National ID number (NIN)", nin, { nin = it },
                            keyboard = KeyboardType.Number, maxLength = 11, digitsOnly = true
                        )
                        FormField(
                            "Driver's licence number", licence,
                            { licence = it.uppercase().filter { c -> c.isLetterOrDigit() } },
                            maxLength = 20
                        )
                    }
                    1 -> {
                        DocCard("National ID", "NIN slip, voter's card or international passport", has("nin"), false) { add("nin") }
                        DocCard("Driver's licence", "Front side", has("licence"), false) { add("licence") }
                        DocCard("Selfie", "Take it now in good light, facing the camera", has("selfie"), true) { add("selfie") }
                    }
                    2 -> {
                        FormField("Car make and model", carModel, { carModel = it })
                        FormField(
                            "Year", carYear, { carYear = it },
                            keyboard = KeyboardType.Number, maxLength = 4, digitsOnly = true
                        )
                        FormField("Colour", carColour, { carColour = it }, maxLength = 20)
                        FormField("Number plate", plate, { plate = it.uppercase() }, maxLength = 12)
                    }
                    3 -> {
                        DocCard("Vehicle registration", "Vehicle licence or registration papers", has("reg"), false) { add("reg") }
                        DocCard("Insurance", "Must be valid today", has("ins"), false) { add("ins") }
                        DocCard("Roadworthiness certificate", "Must be valid today", has("road"), false) { add("road") }
                        DocCard("Front of the car", "Number plate visible", has("front"), true) { add("front") }
                        DocCard("Back of the car", "Number plate visible", has("back"), true) { add("back") }
                        DocCard("Inside the car", "Seats and dashboard, clean and tidy", has("inside"), true) { add("inside") }
                    }
                    4 -> {
                        InfoCard {
                            Text("Allow location all the time", color = PuInk, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "Pick Up uses your phone's location while you are online, even when the app is closed " +
                                    "or the screen is off. Riders can find you, help can reach you fast, and every trip " +
                                    "is recorded for safety.",
                                color = PuMuted, fontSize = 13.sp
                            )
                            Spacer(Modifier.height(12.dp))
                            if (locationOk) {
                                Pill("On all the time ✓", PuGood, Color.White)
                            } else {
                                Pill("Not allowed yet", PuChip, PuMuted)
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        if (!locationOk) {
                            OutlineButton("Allow all the time", { requestLocation() })
                            TextButton(onClick = { openAppSettings(context) }) {
                                Text("Open phone settings", color = PuMuted)
                            }
                            Text(
                                "On the next screen, choose \"Allow all the time\".",
                                color = PuMuted, fontSize = 12.sp
                            )
                        }
                    }
                    else -> {
                        val docCount = docsAdded.split(",").count { it.isNotEmpty() }
                        InfoCard {
                            MoneyRow("Name", name)
                            MoneyRow("National ID", "•••••••" + nin.takeLast(4))
                            MoneyRow("Licence", "•••••" + licence.takeLast(4))
                            MoneyRow("Car", "$carModel · $plate")
                            MoneyRow("Documents and photos", "$docCount of 9")
                            MoneyRow("Location", "On all the time")
                        }
                        Spacer(Modifier.height(12.dp))
                        ConsentRow("I agree to the Terms and Privacy Policy.", agreed) { agreed = it }
                        Text(
                            "Trip records are kept for safety and may be shared with the authorities when the law requires it.",
                            color = PuMuted, fontSize = 12.sp
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(
                            "We check your documents. You cannot go online until you are approved.",
                            color = PuMuted, fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            PrimaryButton(
                text = if (step == stepTitles.size - 1) "Submit for review" else "Next",
                onClick = { if (step == stepTitles.size - 1) onSubmitted() else step++ },
                enabled = canNext
            )
            if (step > 0) {
                TextButton(
                    onClick = { step-- },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Back", color = PuMuted)
                }
            }
        }
    }
}

@Composable
private fun StatusRow(label: String, detail: String, state: Int) {
    Row(
        modifier = Modifier.padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(
                    when (state) {
                        0 -> PuGood
                        1 -> PuAmber
                        else -> PuChip
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                when (state) {
                    0 -> "✓"
                    1 -> "…"
                    else -> ""
                },
                color = if (state == 0) Color.White else PuAmberInk,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, color = PuInk, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text(detail, color = PuMuted, fontSize = 12.sp)
        }
    }
}

@Composable
fun VerificationPendingScreen(onDemoContinue: () -> Unit) {
    var showSupport by remember { mutableStateOf(false) }

    // Nothing to go back to: the driver waits here until approved.
    BackHandler { }

    Surface(modifier = Modifier.fillMaxSize(), color = PuBg) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Spacer(Modifier.height(24.dp))
            Text("We're reviewing your details", color = PuInk, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(
                "This usually takes less than a day. We'll message you as soon as it's done.",
                color = PuMuted, fontSize = 14.sp
            )
            Spacer(Modifier.height(20.dp))

            InfoCard {
                StatusRow("Documents received", "Thank you, everything arrived.", 0)
                StatusRow("Identity and licence check", "In progress", 1)
                StatusRow("Car and photos check", "Waiting", 2)
            }
            Spacer(Modifier.height(12.dp))
            Text(
                "You can't go online until you are approved. This keeps riders and drivers safe.",
                color = PuMuted, fontSize = 13.sp
            )

            Spacer(Modifier.weight(1f))

            OutlineButton("Contact support", { showSupport = true })
            Spacer(Modifier.height(10.dp))
            PrimaryButton("Demo: skip to the app", onDemoContinue)
        }
    }

    if (showSupport) {
        SkeletonDialog(message = "Support chat comes in a later batch.", onOk = { showSupport = false })
    }
}

@Composable
private fun DocStatusRow(title: String, status: String, good: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, color = PuInk, fontSize = 15.sp)
        if (good) Pill(status, PuGood, Color.White) else Pill(status, PuAmber, PuAmberInk)
    }
}

@Composable
fun ProfileScreen(onBack: () -> Unit) {
    var showUpdate by remember { mutableStateOf(false) }
    val rating = DriverRepository.performance(1).rating

    Surface(modifier = Modifier.fillMaxSize(), color = PuBg) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            TextButton(onClick = onBack) {
                Text("← Back", color = PuMuted)
            }
            Text("Profile & documents", color = PuInk, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(14.dp))

            InfoCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RiderAvatar("Driver", 56.dp)
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Your Name", color = PuInk, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("★ $rating", color = PuMuted, fontSize = 13.sp)
                    }
                    Pill("Verified ✓", PuGood, Color.White)
                }
            }
            Spacer(Modifier.height(12.dp))

            InfoCard {
                Text("Documents", color = PuInk, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                DocStatusRow("National ID", "Verified", true)
                DocStatusRow("Driver's licence", "Verified", true)
                DocStatusRow("Vehicle registration", "Verified", true)
                DocStatusRow("Insurance", "Expires in 12 days", false)
                DocStatusRow("Roadworthiness", "Verified", true)
                Spacer(Modifier.height(6.dp))
                Text(
                    "Expired papers stop you from going online, so renew early.",
                    color = PuMuted, fontSize = 12.sp
                )
            }
            Spacer(Modifier.height(12.dp))

            InfoCard {
                Text("Your car", color = PuInk, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                MoneyRow("Car", "Toyota Corolla, 2016")
                MoneyRow("Colour", "Silver")
                MoneyRow("Number plate", "ABC-123DE")
                MoneyRow("Air conditioner", "Working")
            }
            Spacer(Modifier.height(12.dp))

            Spacer(Modifier.height(16.dp))
            OutlineButton("Update a document", { showUpdate = true })
            Spacer(Modifier.height(8.dp))
        }
    }

    if (showUpdate) {
        SkeletonDialog(message = "Updating documents comes in a later batch.", onOk = { showUpdate = false })
    }
}
