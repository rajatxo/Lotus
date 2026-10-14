package com.rajatxo.lotus.playback

import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

class LotusPlaybackService : MediaSessionService() {
    override fun onCreate() {
        super.onCreate()
        // Will be implemented in Step 2
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = null
}
