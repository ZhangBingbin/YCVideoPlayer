package org.yczbj.ycvideoplayer.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.transition.Transition;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;

import org.yczbj.ycvideoplayer.R;
import org.yczbj.ycvideoplayerlib.player.VideoViewManager;
import org.yczbj.ycvideoplayerlib.player.VideoPlayer;
import org.yczbj.ycvideoplayerlib.tool.utils.PlayerUtils;
import org.yczbj.ycvideoplayerlib.ui.view.BasisVideoController;

public class DetailActivity extends AppCompatActivity {

    private FrameLayout mPlayerContainer;
    private VideoPlayer mVideoView;
    public static final String VIEW_NAME_PLAYER_CONTAINER = "player_container";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_video);
        initView();
        initVideoView();
    }

    protected void initView() {
        mPlayerContainer = findViewById(R.id.player_container);
        ViewCompat.setTransitionName(mPlayerContainer, VIEW_NAME_PLAYER_CONTAINER);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP || !addTransitionListener()) {
            initVideoView();
        }
    }

    private void initVideoView() {
        //拿到VideoView实例
        VideoPlayer mVideoView = VideoViewManager.instance().get("seamless");
        //如果已经添加到某个父容器，就将其移除
        PlayerUtils.removeViewFormParent(mVideoView);
        //把播放器添加到页面的容器中
        mPlayerContainer.addView(mVideoView);
        //设置新的控制器
        BasisVideoController controller = new BasisVideoController(DetailActivity.this);
        mVideoView.setVideoController(controller);

        Intent intent = getIntent();
        boolean seamlessPlay = intent.getBooleanExtra(IntentKeys.SEAMLESS_PLAY, false);
        String title = intent.getStringExtra(IntentKeys.TITLE);
        controller.addDefaultControlComponent(title, false);
        if (seamlessPlay) {
            //无缝播放需还原Controller状态
            controller.setPlayState(mVideoView.getCurrentPlayState());
            controller.setPlayerState(mVideoView.getCurrentPlayerState());
        } else {
            //不是无缝播放的情况
            String url = intent.getStringExtra(IntentKeys.URL);
            mVideoView.setUrl(url);
            mVideoView.start();
        }
    }

    @RequiresApi(21)
    private boolean addTransitionListener() {
        final Transition transition = getWindow().getSharedElementEnterTransition();

        if (transition != null) {
            transition.addListener(new Transition.TransitionListener() {
                @Override
                public void onTransitionEnd(Transition transition) {
                    transition.removeListener(this);
                }

                @Override
                public void onTransitionStart(Transition transition) {
                    initVideoView();
                }

                @Override
                public void onTransitionCancel(Transition transition) {
                    transition.removeListener(this);
                }

                @Override
                public void onTransitionPause(Transition transition) {
                }

                @Override
                public void onTransitionResume(Transition transition) {
                }
            });
            return true;
        }
        return false;
    }

    @Override
    protected void onPause() {
        if (isFinishing()) {
            //移除Controller
            mVideoView.setVideoController(null);
            //将VideoView置空，其目的是不执行 super.onPause(); 和 super.onDestroy(); 中的代码
            mVideoView = null;
        }
        super.onPause();
    }
}
