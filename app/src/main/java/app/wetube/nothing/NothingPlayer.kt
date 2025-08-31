package app.wetube.nothing

import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener

open class NothingPlayer : YouTubePlayer {
    override fun addListener(listener: YouTubePlayerListener): Boolean {
        return false
    }

    override fun cueVideo(videoId: String, startSeconds: Float) {
        
    }

    override fun loadVideo(videoId: String, startSeconds: Float) {
        
    }

    override fun mute() {
        
    }

    fun nextVideo() {
        
    }

    override fun pause() {
        
    }

    override fun play() {
        
    }

     fun playVideoAt(index: Int) {
        
    }

     fun previousVideo() {
        
    }

    override fun removeListener(listener: YouTubePlayerListener): Boolean {
        return false
    }

    override fun seekTo(time: Float) {
        
    }

      fun setLoop(loop: Boolean) {
        
    }

    override fun setPlaybackRate(playbackRate: PlayerConstants.PlaybackRate) {
        
    }

      fun setShuffle(shuffle: Boolean) {
        
    }

    override fun setVolume(volumePercent: Int) {
        
    }

     override fun toggleFullscreen() {
        
    }

    override fun unMute() {
        
    }


    companion object{
        @JvmStatic
        fun newInstance() = object: NothingPlayer(){}
    }
} 
