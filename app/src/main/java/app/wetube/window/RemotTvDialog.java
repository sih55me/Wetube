package app.wetube.window;

import android.annotation.SuppressLint;
import android.app.Presentation;
import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaRouter;
import android.os.Bundle;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.NumberPicker;



import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import app.wetube.R;
import app.wetube.databinding.ProyektorVidBinding;
import app.wetube.pryektr.PlayerProyektor;

public class RemotTvDialog  extends Paper implements YouTubePlayerListener {
    
    @NotNull
    public final MediaRouter router;

    @Nullable
    public PlayerProyektor presentation;
    
    @NotNull
    private final ProyektorVidBinding bin;

    

    public RemotTvDialog(@NotNull Context context, @NotNull MediaRouter med, @Nullable PlayerProyektor p) {
        super(context);
        bin = ProyektorVidBinding.inflate(getLayoutInflater());
        router = med;
        presentation = p;
    }


    @Nullable
    YouTubePlayer yt () {
        if(presentation ==null){
            return null;
        }
        return presentation.ytp;
    }





    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(bin.getRoot());
        bin.pick.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                when(yt(), () -> Objects.requireNonNull(yt()).seekTo((float) newVal));
            }
        });
    }




    private final MediaRouter.SimpleCallback routerCallback =
            new MediaRouter.SimpleCallback() {

                // BEGIN_INCLUDE(SimpleCallback)
                /**
                 * A new route has been selected as active. Disable the current
                 * route and enable the new one.
                 */
                @Override
                public void onRouteSelected(MediaRouter router, int type, MediaRouter.RouteInfo info) {
                    updatePresentation();
                }

                /**
                 * The route has been unselected.
                 */
                @Override
                public void onRouteUnselected(MediaRouter router, int type, MediaRouter.RouteInfo info) {
                    updatePresentation();

                }

                /**
                 * The route's presentation display has changed. This callback
                 * is called when the presentation has been activated, removed
                 * or its properties have changed.
                 */
                @Override
                public void onRoutePresentationDisplayChanged(MediaRouter router, MediaRouter.RouteInfo info) {
                    updatePresentation();
                }
                // END_INCLUDE(SimpleCallback)
            };

    private void updatePresentation() {
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(R.string.play);
        menu.add(R.string.pause);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NotNull MenuItem item) {
        YouTubePlayer yt = yt();
        switch (Objects.requireNonNullElse(item.getTitle(), "").toString()){
            case "Play":
                when(yt, () -> Objects.requireNonNull(yt).play());
                break;
            case "Pause":
                when(yt, () -> Objects.requireNonNull(yt).pause());
                break;
        }
        return super.onOptionsItemSelected(item);
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

    }

    @Override
    public void onVideoDuration(@NotNull YouTubePlayer youTubePlayer, float v) {

    }

    @Override
    public void onVideoLoadedFraction(@NotNull YouTubePlayer youTubePlayer, float v) {

    }

    @Override
    public void onVideoId(@NotNull YouTubePlayer youTubePlayer, @NotNull String s) {

    }

    void when(@Nullable Object o, @NotNull Runnable r){
        if(o != null){
            r.run();
        }
    }
}
