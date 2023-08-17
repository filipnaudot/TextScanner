package se.umu.fina0006.textscanner

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import se.umu.fina0006.textscanner.databinding.ActivityCameraBinding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * CameraActivity was inspired by Android's "Getting Started with CameraX" tutorial.
 * Link: https://developer.android.com/codelabs/camerax-getting-started#0
 * This class handle the CameraX instance but also contains function to analyze
 * in memory bitmap with MLKit.
 */
class CameraActivity : AppCompatActivity() {
    private var scannedText: String = ""
    private lateinit var viewBinding: ActivityCameraBinding
    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService
    private var flashMode: Int = ImageCapture.FLASH_MODE_OFF
    private val activityResultLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions())
        { permissions ->
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in REQUIRED_PERMISSIONS && !it.value)
                    permissionGranted = false
            }
            if (!permissionGranted) {
                Toast.makeText(baseContext,
                    "Permission request denied",
                    Toast.LENGTH_SHORT).show()
            } else {
                startCamera()
            }
        }

    /**
     * Initializes the user interface using view binding, checks for necessary permissions,
     * and initializes the camera if permissions are granted.
     * Also sets up a click listener on the image capture button to
     * take a photo when clicked. Lastly, initializes a single-threaded executor for camera
     * operations.
     *
     * @param savedInstanceState The saved instance state bundle.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissions()
        }

        viewBinding.imageCaptureButton.setOnClickListener {
            takePhoto()
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    /**
     * Initiates the process of taking a photo. Retrieves the image capture instance and
     * sets up an image capture listener that is triggered after the photo has been taken.
     * On successful image capture, the captured image data is converted into a
     * Bitmap and processed with ML Kit.
     */
    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Set up image capture listener, which is triggered after the photo has been taken
        imageCapture.takePicture(
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    val buffer = image.planes[0].buffer
                    val data = ByteArray(buffer.remaining())
                    buffer.get(data)

                    // Convert the ByteBuffer to a Bitmap
                    val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)

                    image.close()
                    setLoading()
                    processImageWithMLKit(bitmap)
                }
                override fun onError(exception: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exception.message}", exception)
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                }
            }
        )
    }

    /**
     * Sets the UI components to a loading state.
     * Hiding capture and flash buttons while displaying a spinner.
     */
    private fun setLoading() {
        viewBinding.buttonFlash.visibility = View.GONE
        viewBinding.imageCaptureButton.visibility = View.GONE
        viewBinding.viewFinder.visibility = View.GONE
        viewBinding.spinner.visibility = View.VISIBLE
    }

    /**
     * Processes a given Bitmap image using MLKit for text recognition. If text is detected,
     * each detected text block is appended to the scannedText list and logged.
     * If no text is detected a toast message is displayed.
     *
     * @param bitmap The input Bitmap image to be processed.
     */
    private fun processImageWithMLKit(bitmap: Bitmap) {
        val image = InputImage.fromBitmap(bitmap, 0)
        // Process the image with MLKit
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        recognizer.process(image).addOnSuccessListener { text ->
            if (text != null && text.textBlocks.isNotEmpty()) {
                for (block in text.textBlocks) {
                    scannedText += "${block.text}\n"
                    Log.d(TAG, "Text Block: ${block.text}")
                }
            } else {
                Log.d(TAG, "No text detected.")
                Toast.makeText(this, "No text detected", Toast.LENGTH_LONG).show()
            }

            val resultIntent = Intent()
            resultIntent.putExtra("scannedText", scannedText)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }.addOnFailureListener { e ->
            Log.e(TAG, "Text recognition failed: ${e.message}", e)
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }

    /**
     * Initializes and starts the camera using CameraX. Retrieves the camera provider instance
     * asynchronously and sets up camera use cases like preview and image capture.
     */
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewBinding.viewFinder.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder().setFlashMode(flashMode).build()

            setFlashModeListener()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)

            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    /**
     * Set the listener for flash button that controls the flash mode of the camera.
     */
    private fun setFlashModeListener() {
        viewBinding.buttonFlash.setOnClickListener {
            when (flashMode) {
                ImageCapture.FLASH_MODE_OFF -> {
                    flashMode = ImageCapture.FLASH_MODE_ON
                    viewBinding.buttonFlash.text = getString(R.string.flash_on)
                }
                ImageCapture.FLASH_MODE_ON -> {
                    flashMode = ImageCapture.FLASH_MODE_OFF
                    viewBinding.buttonFlash.text = getString(R.string.flash_off)
                }
            }
            // Update flash mode
            imageCapture?.flashMode = flashMode
        }
    }

    /**
     * Requests the necessary permissions for the application. Launches an activity result
     * contract to handle permission requests.
     */
    private fun requestPermissions() {
        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
    }

    /**
     * Checks if all the required permissions are granted.
     *
     * @return true if all required permissions are granted else false.
     */
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * An activity result contract class for capturing an image using the camera.
     * Creates an intent to launch the CameraActivity and handles parsing the result.
     */
    class TakePicture : ActivityResultContract<Unit, String?>() {
        override fun createIntent(context: Context, input: Unit): Intent {
            return Intent(context, CameraActivity::class.java)
        }
        override fun parseResult(resultCode: Int, intent: Intent?): String? {
            if (resultCode == Activity.RESULT_OK && intent != null) {
                return intent.getStringExtra("scannedText")
            }
            return ""
        }
    }

    /**
     * Performs cleanup operations such as shutting down the camera executor to release resources.
     */
    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "TextScannerCameraActivity"
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.CAMERA,
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }
}
