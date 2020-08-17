package app.cameraxfeed.camera.state

sealed class CameraViewState {
    data class PostCameraViewState(
        val isLoading: Boolean,
        val isSuccess: Boolean? = null,
        val error: Int? = null
     ) : CameraViewState()
}