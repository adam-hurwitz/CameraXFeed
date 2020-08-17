package app.cameraxfeed.dependencyinjection

import app.cameraxfeed.camera.CameraFragment
import app.cameraxfeed.feed.view.FeedFragment
import app.cameraxfeed.viewmodel.FeedViewModel
import dagger.Component
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Singleton

@Singleton
@Component(modules = [Module::class, AssistedInjectModule::class])
@ExperimentalCoroutinesApi
interface Component {

    fun inject(cameraFragment: CameraFragment)

    fun inject(feedFragment: FeedFragment)

    fun feedViewModelFactory(): FeedViewModel.Factory
}