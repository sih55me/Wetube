package app.wetube.pryektr;

import android.app.Presentation;
import android.content.Context;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;


import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerCallback;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import app.wetube.core.SetheKt;
import app.wetube.nothing.NothingPlayer;

public class PlayerProyektor extends Presentation implements YouTubePlayerListener {
    @NotNull
    public String vid = "";

    @NotNull
    private YouTubePlayerView yt = new YouTubePlayerView(getContext());

    @Nullable
    public YouTubePlayer ytp;
    @Nullable
    public YouTubePlayer usedYt;
    @NotNull
    public Connection con;
    public PlayerProyektor(@NotNull Connection nect, Display display, @Nullable String vid) {
        super(nect.getContextAble(), display);
        if(nect.getContextAble() instanceof YouTubePlayerListener){
            yt.addYouTubePlayerListener((YouTubePlayerListener) nect.getContextAble());
        }
        con = nect;
        if (vid != null) {
            this.vid = vid;
        }
    }

    private boolean realMute = false;

    public float time = 0F;

    public void mute() {
        realMute = true;
        if (ytp == null){
            return;
        }
        ytp.mute();
    }

    public void unMute() {
        realMute = false;
        if (ytp == null){
            return;
        }
        ytp.unMute();
    }




    public void play() {
        assert ytp != null;
        ytp.play();
    }

    public void pause() {
        assert ytp != null;
        ytp.pause();
    }

    @NotNull
    public YouTubePlayerView getYt() {
        return yt;
    }

    @Override
    protected void onStop() {
        super.onStop();
        yt.release();
    }

    @Nullable
    public YouTubePlayerListener li;

    @Override
    public void dismiss() {
        ytp = usedYt;
        if(ytp != null){
            if (li != null) {
                ytp.removeListener(li);
            }
            li = null;
            con.setYPlayer(ytp);
        }
        super.dismiss();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(yt);
        yt.addYouTubePlayerListener(this);
        if(li != null){
            yt.addYouTubePlayerListener(li);
        }

        yt.getYouTubePlayerWhenReady(youTubePlayer -> {
            ytp = youTubePlayer;
            ytp.loadVideo(vid, 0F);
            if(realMute){
                ytp.mute();
            }else{
                ytp.unMute();
            }
        });

    }

    @Override
    public void onApiChange(@NotNull YouTubePlayer youTubePlayer) {

    }

    @Override
    public void onReady(@NotNull YouTubePlayer youTubePlayer) {


    }

    @Override
    public void onStateChange(@NotNull YouTubePlayer youTubePlayer, @NotNull PlayerConstants.PlayerState playerState) {

    }

    @Override
    public void onPlaybackQualityChange(@NotNull YouTubePlayer youTubePlayer, @NotNull PlayerConstants.PlaybackQuality playbackQuality) {

    }

    @Override
    public void onPlaybackRateChange(@NotNull YouTubePlayer youTubePlayer, @NotNull PlayerConstants.PlaybackRate playbackRate) {

    }

    @Override
    public void onError(@NotNull YouTubePlayer youTubePlayer, @NotNull PlayerConstants.PlayerError playerError) {

    }

    @Override
    public void onCurrentSecond(@NotNull YouTubePlayer youTubePlayer, float v) {
        time = v;
        if(usedYt != null){
            usedYt.seekTo(v);
        }
    }

    @Override
    public void onVideoDuration(@NotNull YouTubePlayer youTubePlayer, float v) {
    }

    @Override
    public void onVideoLoadedFraction(@NotNull YouTubePlayer youTubePlayer, float v) {

    }

    @Override
    public void onVideoId(@NotNull YouTubePlayer youTubePlayer, @NotNull String s) {
        vid = s;
    }

    void when(@Nullable Object o, @NotNull Runnable r){
        if(o != null){
            r.run();
        }
    }

    public interface Connection{
        Context getContextAble();
        void setYPlayer(@NotNull YouTubePlayer yt);
        @NotNull
        YouTubePlayer getYPlayer();
    }

}
