package com.example.videocall;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;
import io.agora.rtc.video.VideoEncoderConfiguration;

public class MainActivity extends AppCompatActivity {
    // Permissions
    private static final int PERMISSION_REQ_ID = 22;
    private static final String[] REQUESTED_PERMISSIONS =
            {Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA};

    private ImageView imageViewJoinBtn;
    private ImageView imageViewAudioBtn;
    private ImageView imageViewLeaveBtn;
    private ImageView imageViewCameraBtn;

    public static final String LOG_TAG = "LOG_TAG";

    private RtcEngine mRtcEngine;
    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {

        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {

            if(mRtcEngine != null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setupSession();

                        setUpLocalVideoFeed();

//                        mRtcEngine.muteLocalVideoStream(false);

                    }
                });
            }

        }

        @Override
        public void onLeaveChannel(RtcStats rtcStats) {
            RtcEngine.destroy();
            mRtcEngine = null;

        }

        // uid of remote user who sends the first frame of video
        @Override
        public void onRemoteVideoStateChanged(int uid, int state, int reason, int elapsed) {
            if (state == Constants.REMOTE_VIDEO_STATE_STARTING && reason == Constants.REMOTE_VIDEO_STATE_REASON_LOCAL_UNMUTED) {

            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i("agora","First remote video decoded, uid: " + (uid & 0xFFFFFFFFL));
                    try {
                        setupRemoteVideoFeed(uid);
                    } catch(Exception e) {
                        System.out.println("Error Set Up Remote Video: " + e.toString());
                    }

                }
            });

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageViewJoinBtn = findViewById(R.id.joinBtn);

        imageViewAudioBtn = findViewById(R.id.audioBtn);
        imageViewLeaveBtn = findViewById(R.id.leaveBtn);
        imageViewCameraBtn = findViewById(R.id.cameraBtn);


        // prompt user to allow permission to access audio and camera
        // System will only ask twice for each permission
        checkSelfPermission();


        // On clicking the join Btn
        imageViewJoinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRtcEngine != null) {
                    joinChannel();
                }

            }
        });

        imageViewAudioBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRtcEngine != null) {

                }
            }
        });

        imageViewCameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRtcEngine != null) {

                }
            }
        });

        imageViewLeaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRtcEngine != null) {
                    leaveChannel();
                }

            }
        });


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RtcEngine.destroy();
        mRtcEngine = null;
    }

    public void checkSelfPermission() {
        // granted is 0, not granted is -1
        if(ContextCompat.checkSelfPermission(this, REQUESTED_PERMISSIONS[0])
                + ContextCompat.checkSelfPermission(
                this, REQUESTED_PERMISSIONS[1])
                != PackageManager.PERMISSION_GRANTED) {
//            int grant = ContextCompat.checkSelfPermission(this, REQUESTED_PERMISSIONS[0])
//                    + ContextCompat.checkSelfPermission(
//                    this, REQUESTED_PERMISSIONS[1]);
//            Log.i(LOG_TAG, String.valueOf(grant));
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, PERMISSION_REQ_ID);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQ_ID: {
                if (grantResults[0] + grantResults[1] != PackageManager.PERMISSION_GRANTED) {
                    Log.i(LOG_TAG, "Need permissions " + Manifest.permission.RECORD_AUDIO + "/" + Manifest.permission.CAMERA);
                }
                // only initialize agora engine if both permissions are granted
                else {

                    try {
                        initAgoraEngine();
                    } catch (Exception e) {
                        Log.e(LOG_TAG, Log.getStackTraceString(e));

                        throw new RuntimeException("NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(e));
                    }

                }
                break;
            }
        }
    }



    private void initAgoraEngine() throws Exception{
        mRtcEngine = RtcEngine.create(getBaseContext(), getString(R.string.agora_app_id), mRtcEventHandler);
    }

    private void setupSession() {
        mRtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_COMMUNICATION);

        mRtcEngine.enableVideo();

        mRtcEngine.setVideoEncoderConfiguration(new VideoEncoderConfiguration(new VideoEncoderConfiguration.VideoDimensions(1080, 2220),
                VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_30,
                VideoEncoderConfiguration.STANDARD_BITRATE,
                VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT));
    }

    // engine automatically assign uid to local user
    private void joinChannel() {
        mRtcEngine.joinChannel(getString(R.string.app_temp_token), "Test Channel 2", "Extra Optional Data", 0);
    }

    private void leaveChannel() {
        mRtcEngine.leaveChannel();
    }

    private void setUpLocalVideoFeed() {
        FrameLayout videoContainer = findViewById(R.id.floating_video_container);
        SurfaceView videoSurface = RtcEngine.CreateRendererView(getBaseContext());
        // put the surface view in front so it becomes visible
        videoSurface.setZOrderMediaOverlay(true);
        // add the surface View to the video Container Layout
        videoContainer.addView(videoSurface);
        // set up local video into the surface view
        mRtcEngine.setupLocalVideo(new VideoCanvas(videoSurface, VideoCanvas.RENDER_MODE_FIT, 0));
    }

    // uid of the remote user
    private void setupRemoteVideoFeed(int uid) {
        Log.i(LOG_TAG, "setupRemoteVideoFeed");
        FrameLayout videoContainer = findViewById(R.id.bg_video_container);
        SurfaceView videoSurface = RtcEngine.CreateRendererView(getBaseContext());
        videoContainer.addView(videoSurface);
        mRtcEngine.setupRemoteVideo(new VideoCanvas(videoSurface, VideoCanvas.RENDER_MODE_FIT, uid));
//        mRtcEngine.setRemoteSubscribeFallbackOption(io.agora.rtc.Constants.STREAM_FALLBACK_OPTION_AUDIO_ONLY);
    }
}