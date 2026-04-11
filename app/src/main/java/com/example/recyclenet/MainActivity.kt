package com.example.recyclenet

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.recyclenet.ui.theme.RecycleNetTheme
import java.util.concurrent.Executors

/**
 * MainActivity is the entry point of the RecycleNet application.
 * It handles theme initialization, permissions, and top-level navigation.
 */
class MainActivity : ComponentActivity() {
    private lateinit var modelRunner: ModelRunner

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("MainActivity", "onCreate started")
        // Initialize the splash screen before calling super.onCreate()
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        
        Log.d("MainActivity", "Initializing ModelRunner")
        modelRunner = ModelRunner(this)
        
        // Enables edge-to-edge drawing for a modern, immersive UI
        enableEdgeToEdge()
        Log.d("MainActivity", "Setting content")
        
        setContent {
            Log.d("MainActivity", "Composition started")
            RecycleNetTheme {
                // Permission state management
                var hasCameraPermission by remember {
                    mutableStateOf(
                        ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                    )
                }

                // Launcher for requesting camera permission
                val launcher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                    onResult = { granted -> 
                        Log.d("MainActivity", "Permission result: $granted")
                        hasCameraPermission = granted 
                    }
                )

                // Trigger permission request on first launch if not already granted
                LaunchedEffect(Unit) {
                    Log.d("MainActivity", "LaunchedEffect: Checking permission")
                    if (!hasCameraPermission) {
                        Log.d("MainActivity", "Launching permission request")
                        launcher.launch(Manifest.permission.CAMERA)
                    }
                }

                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    if (hasCameraPermission) {
                        Log.d("MainActivity", "Showing AppNavigation")
                        AppNavigation(modelRunner = modelRunner)
                    } else {
                        // Fallback UI if camera permission is denied
                        Log.d("MainActivity", "Showing Permission screen")
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Camera permission required", style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(onClick = { launcher.launch(Manifest.permission.CAMERA) }) {
                                    Text("Grant Permission")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Handles the high-level navigation between the Home, Live Scanner, and Image Analysis screens.
 * Uses AnimatedContent for smooth cross-fade transitions.
 */
@Composable
fun AppNavigation(modelRunner: ModelRunner) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = { fadeIn().togetherWith(fadeOut()) },
        label = "ScreenTransition"
    ) { screen ->
        when (screen) {
            is Screen.Home -> HomeScreen(onNavigate = { currentScreen = it })
            is Screen.LiveCamera -> LiveInferenceScreen(modelRunner = modelRunner, onBack = { currentScreen = Screen.Home })
            is Screen.ImageAnalysis -> ImageUploadScreen(modelRunner = modelRunner, onBack = { currentScreen = Screen.Home })
        }
    }
}

sealed class Screen {
    object Home : Screen()
    object LiveCamera : Screen()
    object ImageAnalysis : Screen()
}

/**
 * The Home Screen of the application.
 * Features a clean layout with large cards to navigate to main features.
 */
@Composable
fun HomeScreen(onNavigate: (Screen) -> Unit) {
    Scaffold { padding ->
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            // Background Pattern or Subtle Gradient can go here
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "RecycleNet",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Intelligent Waste Classifier",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(64.dp))

                SectionCard(
                    title = "Live Scanner",
                    description = "Identify materials in real-time",
                    icon = Icons.Default.PlayArrow,
                    color = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    onClick = { onNavigate(Screen.LiveCamera) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                SectionCard(
                    title = "Image Analysis",
                    description = "Upload photos for inspection",
                    icon = Icons.Default.PhotoLibrary,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    onClick = { onNavigate(Screen.ImageAnalysis) }
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Text(
                    text = "Help save the planet, one bottle at a time.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}

/**
 * A reusable card component for sections on the Home Screen.
 * Implements a shadowless Material 3 design with custom colors.
 */
@Composable
fun SectionCard(
    title: String, 
    description: String, 
    icon: ImageVector, 
    color: Color, 
    contentColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = color, contentColor = contentColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color.White.copy(alpha = 0.2f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(28.dp))
                }
            }
            Spacer(modifier = Modifier.width(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(text = description, style = MaterialTheme.typography.bodyMedium, color = contentColor.copy(alpha = 0.8f))
            }
            Icon(
                imageVector = Icons.Default.PlayArrow, // Using as a chevron
                contentDescription = null,
                modifier = Modifier.size(20.dp).graphicsLayer(rotationZ = 0f) 
            )
        }
    }
}

/**
 * LiveInferenceScreen manages the real-time camera feed and object detection display.
 * It uses CameraX for image capture and ModelRunner for AI inference.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveInferenceScreen(modelRunner: ModelRunner, onBack: () -> Unit) {
    var isRunning by remember { mutableStateOf(true) }
    var detections by remember { mutableStateOf(emptyList<Detection>()) }
    var frameWidth by remember { mutableIntStateOf(640) }
    var frameHeight by remember { mutableIntStateOf(640) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Live Scanner", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            CameraWithAnalysis(
                isRunning = isRunning,
                onDetections = { newDetections, width, height ->
                    detections = newDetections
                    frameWidth = width
                    frameHeight = height
                },
                modelRunner = modelRunner
            )

            DetectionOverlay(detections, frameWidth, frameHeight, modelRunner.classes)

            // Detected material display at the bottom center
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 110.dp)
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                detections.distinctBy { it.classIndex }.sortedByDescending { it.score }.take(1).forEach { detection ->
                    val label = if (detection.classIndex < modelRunner.classes.size) modelRunner.classes[detection.classIndex] else "Unknown"
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(32.dp),
                        border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                        shadowElevation = 0.dp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 32.dp, vertical = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "DETECTED",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp
                            )
                            Text(
                                text = label,
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                LinearProgressIndicator(
                                    progress = { detection.score },
                                    modifier = Modifier.weight(1f).height(12.dp).clip(RoundedCornerShape(6.dp)),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = "${(detection.score * 100).toInt()}%",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.BottomCenter) {
                Button(
                    onClick = { isRunning = !isRunning },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = if (isRunning) ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error) 
                             else ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(if (isRunning) Icons.Default.PlayArrow else Icons.Default.CameraAlt, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (isRunning) "Pause Scanner" else "Resume Scanner", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * ImageUploadScreen allows users to analyze static images from their gallery or take a photo.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageUploadScreen(modelRunner: ModelRunner, onBack: () -> Unit) {
    val context = LocalContext.current
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var detections by remember { mutableStateOf(emptyList<Detection>()) }
    var isAnalyzing by remember { mutableStateOf(false) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                isAnalyzing = true
                val loadedBitmap = loadBitmapFromUri(context, it)
                bitmap = loadedBitmap
                detections = modelRunner.analyzeImage(loadedBitmap)
                isAnalyzing = false
            }
        }
    )

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview(),
        onResult = { btm ->
            btm?.let {
                isAnalyzing = true
                val processedBtm = it.copy(Bitmap.Config.ARGB_8888, true)
                bitmap = processedBtm
                detections = modelRunner.analyzeImage(processedBtm)
                isAnalyzing = false
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Image Analysis", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = { cameraLauncher.launch(null) },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Capture")
                }
                FilledTonalButton(
                    onClick = { imagePicker.launch("image/*") },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Upload")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (isAnalyzing) {
                Box(modifier = Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                bitmap?.let { btm ->
                    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).aspectRatio(btm.width.toFloat() / btm.height)) {
                        Image(
                            bitmap = btm.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                        DetectionOverlay(detections, btm.width, btm.height, modelRunner.classes)
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Analysis Results", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleLarge)
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            if (detections.isEmpty()) {
                                Surface(
                                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        "No recyclable materials identified. Try a clearer photo with better lighting.",
                                        modifier = Modifier.padding(16.dp),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            } else {
                                detections.sortedByDescending { it.score }.forEach { detection ->
                                    val label = if (detection.classIndex < modelRunner.classes.size) modelRunner.classes[detection.classIndex] else "Unknown"
                                    
                                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(label, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                            Text("${(detection.score * 100).toInt()}% Confidence", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        LinearProgressIndicator(
                                            progress = { detection.score },
                                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                            color = MaterialTheme.colorScheme.primary,
                                            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                } ?: Box(
                    modifier = Modifier.fillMaxWidth().height(300.dp).clip(RoundedCornerShape(20.dp)).background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Capture or upload an image to start", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

fun loadBitmapFromUri(context: android.content.Context, uri: Uri): Bitmap {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val source = ImageDecoder.createSource(context.contentResolver, uri)
        ImageDecoder.decodeBitmap(source).copy(Bitmap.Config.ARGB_8888, true)
    } else {
        @Suppress("DEPRECATION")
        MediaStore.Images.Media.getBitmap(context.contentResolver, uri).copy(Bitmap.Config.ARGB_8888, true)
    }
}

/**
 * Renders the camera preview using CameraX and triggers the ModelRunner for analysis.
 */
@Composable
fun CameraWithAnalysis(isRunning: Boolean, onDetections: (List<Detection>, Int, Int) -> Unit, modelRunner: ModelRunner) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }
    val analysisActive = remember { mutableStateOf(isRunning) }
    
    DisposableEffect(Unit) {
        onDispose {
            analysisExecutor.shutdown()
        }
    }

    LaunchedEffect(isRunning) { analysisActive.value = isRunning }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply { scaleType = PreviewView.ScaleType.FILL_CENTER }
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also { it.surfaceProvider = previewView.surfaceProvider }
                val imageAnalysis = ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build()
                imageAnalysis.setAnalyzer(analysisExecutor) { imageProxy ->
                    if (analysisActive.value) {
                        try {
                            val bitmap = imageProxy.toBitmap()
                            val rotatedBitmap = bitmap.rotate(imageProxy.imageInfo.rotationDegrees.toFloat())
                            val results = modelRunner.analyzeImage(rotatedBitmap)
                            onDetections(results, rotatedBitmap.width, rotatedBitmap.height)
                        } catch (e: Exception) { Log.e("Analysis", "Error", e) }
                    }
                    imageProxy.close()
                }
                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalysis)
                } catch (e: Exception) { Log.e("Camera", "Binding failed", e) }
            }, ContextCompat.getMainExecutor(ctx))
            previewView
        },
        modifier = Modifier.fillMaxSize()
    )
}

/**
 * Draws the bounding box and corners over the detected objects in the camera/image view.
 * Provides a "tech" look by focusing on corners instead of full boxes.
 */
@Composable
fun DetectionOverlay(detections: List<Detection>, frameWidth: Int, frameHeight: Int, classes: Array<String>) {
    val boxColor = MaterialTheme.colorScheme.primary
    Canvas(modifier = Modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        
        val scale = maxOf(canvasWidth / frameWidth, canvasHeight / frameHeight)
        val offsetX = (canvasWidth - frameWidth * scale) / 2f
        val offsetY = (canvasHeight - frameHeight * scale) / 2f

        detections.forEach { detection ->
            val x1 = (detection.boundingBox.left * (frameWidth / 640f) * scale) + offsetX
            val y1 = (detection.boundingBox.top * (frameHeight / 640f) * scale) + offsetY
            val x2 = (detection.boundingBox.right * (frameWidth / 640f) * scale) + offsetX
            val y2 = (detection.boundingBox.bottom * (frameHeight / 640f) * scale) + offsetY
            
            val width = x2 - x1
            val height = y2 - y1
            val cornerLen = minOf(width, height) * 0.2f
            
            // Draw corners instead of full rectangle for a cleaner "tech" look
            // Top Left
            drawLine(boxColor, Offset(x1, y1), Offset(x1 + cornerLen, y1), 3.dp.toPx())
            drawLine(boxColor, Offset(x1, y1), Offset(x1, y1 + cornerLen), 3.dp.toPx())
            
            // Top Right
            drawLine(boxColor, Offset(x2, y1), Offset(x2 - cornerLen, y1), 3.dp.toPx())
            drawLine(boxColor, Offset(x2, y1), Offset(x2, y1 + cornerLen), 3.dp.toPx())
            
            // Bottom Left
            drawLine(boxColor, Offset(x1, y2), Offset(x1 + cornerLen, y2), 3.dp.toPx())
            drawLine(boxColor, Offset(x1, y2), Offset(x1, y2 - cornerLen), 3.dp.toPx())
            
            // Bottom Right
            drawLine(boxColor, Offset(x2, y2), Offset(x2 - cornerLen, y2), 3.dp.toPx())
            drawLine(boxColor, Offset(x2, y2), Offset(x2, y2 - cornerLen), 3.dp.toPx())
            
            // Subtle fill
            drawRect(
                color = boxColor.copy(alpha = 0.1f),
                topLeft = Offset(x1, y1),
                size = androidx.compose.ui.geometry.Size(width, height)
            )
        }
    }
}

fun Bitmap.rotate(degrees: Float): Bitmap {
    if (degrees == 0f) return this
    val matrix = Matrix().apply { postRotate(degrees) }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}
