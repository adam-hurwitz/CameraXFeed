package app.cameraxfeed.camera

import android.Manifest.permission.CAMERA
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_DENIED
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.hardware.display.DisplayManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.CameraSelector.LENS_FACING_BACK
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import app.App
import app.cameraxfeed.NavCameraDirections.Companion.actionCameraFragmentToFeedFragment
import app.cameraxfeed.R
import app.cameraxfeed.R.string.camera_permission_denied
import app.cameraxfeed.R.string.save_post_error_message
import app.cameraxfeed.camera.state.CameraView
import app.cameraxfeed.camera.state.CameraViewIntent
import app.cameraxfeed.camera.state.CameraViewState.PostCameraViewState
import app.cameraxfeed.databinding.FragmentCameraBinding
import app.cameraxfeed.dependencyinjection.Component
import app.cameraxfeed.feed.database.FeedDao
import app.cameraxfeed.feed.state.Post
import app.cameraxfeed.utils.ANIMATION_FAST_MILLIS
import app.cameraxfeed.utils.ANIMATION_SLOW_MILLIS
import app.cameraxfeed.utils.DATE_FORMAT
import app.cameraxfeed.utils.Event
import app.cameraxfeed.utils.PERMISSIONS_REQUEST_CODE
import app.cameraxfeed.utils.PHOTO_EXTENSION
import app.cameraxfeed.utils.RATIO_16_9_VALUE
import app.cameraxfeed.utils.RATIO_4_3_VALUE
import app.cameraxfeed.utils.USERNAME
import app.cameraxfeed.viewmodel.FeedViewModel
import app.cameraxfeed.viewmodel.navGraphSavedStateViewModels
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.LENGTH_LONG
import kotlinx.android.synthetic.main.activity_main.main
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

@ExperimentalCoroutinesApi
class CameraFragment : Fragment(), CameraView {

    @Inject
    lateinit var dao: FeedDao

    private val LOG_TAG = CameraFragment::class.java.simpleName
    private lateinit var component: Component
    private lateinit var binding: FragmentCameraBinding
    private lateinit var viewModel: FeedViewModel

    /** Blocking camera operations are performed using this executor */
    private lateinit var outputDirectory: File
    private var displayId: Int = -1
    private var preview: Preview? = null
    private var cameraExecutor: ExecutorService? = null
    private var imageCapture: ImageCapture? = null
    private var camera: Camera? = null
    private val intent = CameraViewIntent()

    private val displayManager by lazy {
        requireContext().getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE)
            if (PERMISSION_GRANTED == grantResults.firstOrNull())
                initCamera()
            else if (PERMISSION_DENIED == grantResults.firstOrNull()) {
                findNavController().navigate(actionCameraFragmentToFeedFragment())
                showSnackbar(camera_permission_denied)
            }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        component = (context.applicationContext as App).component
        component.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val feedViewModel: FeedViewModel by navGraphSavedStateViewModels(R.id.nav_root) { handle ->
            component.feedViewModelFactory().create()
        }
        this.viewModel = feedViewModel
        binding = FragmentCameraBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.bindCameraViewIntents(this)
        cameraExecutor = Executors.newSingleThreadExecutor()
        outputDirectory = getOutputDirectory()
        if (ContextCompat.checkSelfPermission(requireContext(), CAMERA) == PERMISSION_GRANTED)
            initCamera()
        else requestPermissions(arrayOf(CAMERA), PERMISSIONS_REQUEST_CODE)
        setClickListeners()
    }

    override fun onResume() {
        super.onResume()
        if (ContextCompat.checkSelfPermission(requireContext(), CAMERA) != PERMISSION_GRANTED)
            requestPermissions(arrayOf(CAMERA), PERMISSIONS_REQUEST_CODE)
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor?.shutdown()
        displayManager.unregisterDisplayListener(displayListener)
    }

    override fun initState() = intent.initState
    override fun savePost() = intent.savePost.filterNotNull()
    override fun render(state: PostCameraViewState) {
        if (state.isLoading)
            binding.progressbar?.visibility = VISIBLE
        if (state.isSuccess != null && state.isSuccess) {
            binding.progressbar?.visibility = GONE
        }
        state.error?.let {
            binding.progressbar?.visibility = GONE
            showSnackbar(state.error)
        }
    }

    private fun initCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener(Runnable {
            val cameraProvider = cameraProviderFuture.get()
                ?: throw IllegalStateException("Camera initialization failed.")
            val cameraSelector =
                CameraSelector.Builder().requireLensFacing(LENS_FACING_BACK).build()
            // Every time the orientation of device changes, update rotation for use cases
            displayManager.registerDisplayListener(displayListener, null)
            displayId = binding.viewFinder.display.displayId
            preview = Preview.Builder()
                .setTargetAspectRatio(aspectRatio())
                .setTargetRotation(binding.viewFinder.display.rotation)
                .build()
            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .setTargetAspectRatio(aspectRatio())
                .setTargetRotation(binding.viewFinder.display.rotation)
                .build()
            cameraProvider.unbindAll()
            try {
                camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )
                // Attach the viewfinder's surface provider to preview use case
                preview?.setSurfaceProvider(binding.viewFinder.createSurfaceProvider())
            } catch (exc: Exception) {
                Log.e(LOG_TAG, "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun setClickListeners() {
        binding.cameraCaptureButton?.setOnClickListener {
            // Create output file to hold the image.
            val photoFile = createFile(outputDirectory, PHOTO_EXTENSION)
            // Setup image capture metadata.
            val metadata = ImageCapture.Metadata()
            // Create output options object which contains the file and metadata.
            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile)
                .setMetadata(metadata)
                .build()
            // Setup image capture listener which is triggered after photo has been taken.
            if (imageCapture != null && cameraExecutor != null) {
                imageCapture?.takePicture(
                    outputOptions, cameraExecutor!!, object : ImageCapture.OnImageSavedCallback {
                        override fun onError(error: ImageCaptureException) {
                            Log.e(LOG_TAG, "Photo capture failed: ${error.localizedMessage}")
                            showSnackbar(save_post_error_message)
                        }

                        override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                            val savedUri = output.savedUri ?: Uri.fromFile(photoFile)
                            Log.d(LOG_TAG, "Photo capture succeeded: $savedUri")
                            // Implicit broadcasts will be ignored for devices running API level >= 24
                            // so if you only target API level 24+ you can remove this statement
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                                requireActivity().sendBroadcast(
                                    Intent(android.hardware.Camera.ACTION_NEW_PICTURE, savedUri)
                                )
                            }
                            try {
                                intent.savePost.value = Event(
                                    Post(
                                        username = USERNAME,
                                        profileImage = R.drawable.user_profile_placeholder,
                                        timestamp = getDate(),
                                        imageString = savedUri.toString()
                                    )
                                )
                            } catch (error: Exception) {
                                Log.e(LOG_TAG, "Room saved post error: ${error.localizedMessage}")
                                showSnackbar(save_post_error_message)
                            }
                            lifecycleScope.launch(Main) {
                                findNavController().navigate(actionCameraFragmentToFeedFragment())
                            }
                        }
                    })
                // Change the foreground Drawable using API level 23+ API
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    // Display flash animation to indicate that photo was captured
                    binding.cameraFragment.postDelayed({
                        binding.cameraFragment.foreground = ColorDrawable(Color.WHITE)
                        binding.cameraFragment.postDelayed(
                            { binding.cameraFragment.foreground = null }, ANIMATION_FAST_MILLIS)
                    }, ANIMATION_SLOW_MILLIS)
                }
            }
        }
    }

    /** Use external media if it is available, otherwise use the app's file directory */
    private fun getOutputDirectory(): File {
        val appContext = requireContext().applicationContext
        val mediaDir = requireContext().externalMediaDirs.firstOrNull()?.let {
            File(it, appContext.resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else appContext.filesDir
    }

    /** Helper function used to create a timestamped file */
    private fun createFile(baseFolder: File, extension: String) =
        File(baseFolder, getDate() + extension)

    private fun getDate() = SimpleDateFormat(DATE_FORMAT, Locale.US)
        .format(System.currentTimeMillis())

    /**
     *
     *  Detecting the most suitable ratio for dimensions provided in @params by counting absolute
     *  of preview ratio to one of the provided values.
     *
     *  @param width - preview width
     *  @param height - preview height
     *  @return suitable aspect ratio
     */
    private fun aspectRatio(): Int {
        val width = DisplayMetrics().widthPixels
        val height = DisplayMetrics().heightPixels
        val previewRatio = max(width, height).toDouble() / min(width, height)
        if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }

    /**
     * Display listener for orientation changes that do not trigger a configuration
     * change. For example, for 180-degree orientation changes.
     */
    private val displayListener = object : DisplayManager.DisplayListener {
        override fun onDisplayAdded(displayId: Int) = Unit
        override fun onDisplayRemoved(displayId: Int) = Unit
        override fun onDisplayChanged(displayId: Int) = view?.let { view ->
            if (displayId == this@CameraFragment.displayId) {
                imageCapture?.targetRotation = view.display.rotation
            }
        } ?: Unit
    }

    private fun showSnackbar(message: Int) {
        Snackbar.make(requireActivity().main, message, LENGTH_LONG).show()
    }
}