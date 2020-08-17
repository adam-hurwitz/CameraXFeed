package app

import android.app.Application
import app.cameraxfeed.dependencyinjection.DaggerComponent
import app.cameraxfeed.dependencyinjection.Module

class App: Application(){
    val component = DaggerComponent.builder().module(Module(this)).build()
}