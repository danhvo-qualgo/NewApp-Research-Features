package net.qualgo.safeNest.features.phishingDetection.impl.presentation

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

class MlKitOcrExtractor {

    suspend fun extractText(imageUri: Uri, context: Context): Result<String> = runCatching {
        val image = InputImage.fromFilePath(context, imageUri)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        suspendCancellableCoroutine { cont ->
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    cont.resume(visionText.text)
                }
                .addOnFailureListener { exception ->
                    cont.resumeWithException(exception)
                }
            cont.invokeOnCancellation {
                recognizer.close()
            }
        }
    }
}
