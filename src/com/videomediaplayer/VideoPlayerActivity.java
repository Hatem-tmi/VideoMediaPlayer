package com.videomediaplayer;

import java.io.IOException;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.FrameLayout;
import com.videomediaplayer.R;

public class VideoPlayerActivity extends Activity implements SurfaceHolder.Callback, MediaPlayer.OnPreparedListener,
		VideoControllerView.MediaPlayerControl {
	private static final String TAG = VideoPlayerActivity.class.getSimpleName();

	private SurfaceView videoSurface;
	private MediaPlayer player;
	private VideoControllerView controller;

	/** The current activities configuration used to test screen orientation. */
	private Configuration configuration;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_video_player);
		configuration = getResources().getConfiguration();

		videoSurface = (SurfaceView) findViewById(R.id.videoSurface);
		SurfaceHolder videoHolder = videoSurface.getHolder();
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
			videoHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		videoHolder.addCallback(this);

		// Manage click on video surface
		videoSurface.setOnClickListener(new OnClickListener() {
			@TargetApi(Build.VERSION_CODES.HONEYCOMB)
			@Override
			public void onClick(View v) {
				// show the Video Controller
				controller.show();

				if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
					View rootView = findViewById(android.R.id.content).getRootView();

					// Hide System UI NAVIGATION
					if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH)
						rootView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
				}
			}
		});

		player = new MediaPlayer();
		controller = new VideoControllerView(this) {
			@Override
			public boolean dispatchKeyEvent(KeyEvent event) {
				if (event.getKeyCode() == KeyEvent.KEYCODE_BACK)
					finish();

				return super.dispatchKeyEvent(event);
			}
		};
		updateLayout();

		try {
			player.setAudioStreamType(AudioManager.STREAM_MUSIC);
			player.setDataSource(this, Uri.parse("http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4"));
			player.setOnPreparedListener(this);
		} catch (IllegalArgumentException e) {
			Log.e(TAG, "", e);
		} catch (SecurityException e) {
			Log.e(TAG, "", e);
		} catch (IllegalStateException e) {
			Log.e(TAG, "", e);
		} catch (IOException e) {
			Log.e(TAG, "", e);
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		Log.d(TAG, "onConfigurationChanged");
		super.onConfigurationChanged(newConfig);

		updateLayout();
	}

	/**
	 * Update layout views
	 */
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private void updateLayout() {
		Log.d(TAG, "updateLayout");
		View rootView = findViewById(android.R.id.content).getRootView();
		WindowManager.LayoutParams attrs = getWindow().getAttributes();

		// Show the Video controller
		controller.show();

		switch (configuration.orientation) {
		case Configuration.ORIENTATION_LANDSCAPE:
			// Hide the status bar
			attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
			getWindow().setAttributes(attrs);

			// Hide System UI NAVIGATION
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH)
				rootView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

			// Hide the whole view except the video
			findViewById(R.id.bottomView).setVisibility(View.GONE);
			break;
		case Configuration.ORIENTATION_PORTRAIT:
			// Show the status bar
			attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
			getWindow().setAttributes(attrs);

			// Show System UI NAVIGATION
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH)
				rootView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);

			// Show the whole view with the video
			findViewById(R.id.bottomView).setVisibility(View.VISIBLE);
			break;
		default:
			break;
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		player.setDisplay(holder);
		player.prepareAsync();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		Log.d(TAG, "onPrepared");

		controller.setMediaPlayer(this);
		controller.setAnchorView((FrameLayout) findViewById(R.id.videoSurfaceContainer));

		player.start();
	}

	@Override
	public boolean canPause() {
		return true;
	}

	@Override
	public boolean canSeekBackward() {
		return true;
	}

	@Override
	public boolean canSeekForward() {
		return true;
	}

	@Override
	public int getBufferPercentage() {
		return 0;
	}

	@Override
	public int getCurrentPosition() {
		return player.getCurrentPosition();
	}

	@Override
	public int getDuration() {
		return player.getDuration();
	}

	@Override
	public boolean isPlaying() {
		return player.isPlaying();
	}

	@Override
	public void pause() {
		player.pause();
	}

	@Override
	public void seekTo(int i) {
		player.seekTo(i);
	}

	@Override
	public void start() {
		player.start();
	}

	@Override
	public boolean isFullScreen() {
		return configuration.orientation == Configuration.ORIENTATION_LANDSCAPE;
	}

	@Override
	public void toggleFullScreen() {
	}
}
