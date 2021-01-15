package com.example.videocall;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.Context;
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
import io.agora.media.RtcTokenBuilder;
import io.agora.media.RtcTokenBuilder.Role;
import io.agora.rtc.video.VideoCanvas;
import io.agora.rtc.video.VideoEncoderConfiguration;

public class MainActivity extends AppCompatActivity {
    // Permissions
    private static final int PERMISSION_REQ_ID = 22;
    private static final String[] REQUESTED_PERMISSIONS =
            {Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA};

    // View
    private ImageView imageViewJoinBtn;
    private ImageView imageViewAudioBtn;
    private ImageView imageViewLeaveBtn;
    private ImageView imageViewCameraBtn;


    private Context context = MainActivity.this;
    public static final String LOG_CAT = "LOG_CAT";

    // Click Logic
    private boolean videoMuted;
    private boolean audioMuted;

    // Token Generation Parameters
//    private String appID;
//    private String appCertificate;
//    private String appChannelName;
//    private Role userRole;
//    private Integer expirationTimeInSeconds;

    // Token
    private String channelToken;


    // engine and event handler

    private RtcEngine mRtcEngine;
    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {

        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {

            if(mRtcEngine != null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setupSession();

                        onLocalUserJoinChannelSuccess();

                        Log.i(LOG_CAT, "Channel name: " + channel + ", " + "Local uid: " + String.valueOf(uid&0xffffffffL));
                    }
                });
            }

        }

        @Override
        public void onLeaveChannel(RtcStats rtcStats) {
//            RtcEngine.destroy();
            mRtcEngine = null;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onLocalUserLeaveChannel();
                }
            });

        }

        // uid of remote user who sends the first frame of video
        // define what happen to the remote user video stream to local user
        @Override
        public void onRemoteVideoStateChanged(int uid, int state, int reason, int elapsed) {
            // On first frame of remote video stream decoded and video state changes (first time video enabled)
            if (state == Constants.REMOTE_VIDEO_STATE_STARTING && reason == Constants.REMOTE_VIDEO_STATE_REASON_INTERNAL) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                    Log.i("agora","First remote video decoded, uid: " + (uid & 0xFFFFFFFFL));
//                        try {
//                            Log.d(LOG_CAT, "state: " + String.valueOf(state) + ", " + "reason: " + String.valueOf(reason));
//
//
//                        } catch(Exception e) {
//                            System.out.println("Error Set Up Remote Video: " + e.toString());
//                        }

                        // the line below is important to remove the cover imageView when remote user join channel
                        onRemoteUserJoinChannelSuccess();

                        setupRemoteVideoFeed(uid);

                        Log.i(LOG_CAT, "Remote uid: " + String.valueOf(uid&0xffffffffL));

                        // Suggestion for voice call only, call onPauseRemoteVideoFeed() and onPauseLocalVideoFeed().
                        // Call mode is dictated by setupSession on launch activity and onclick call button (be it voice call or video call), ideally
                        // Request video on voice call feature


                    }
                });
            }

            // On remote user muted video
            else if(state == Constants.REMOTE_VIDEO_STATE_STOPPED && reason == Constants.REMOTE_VIDEO_STATE_REASON_REMOTE_MUTED) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        onPauseLocalVideoFeed();
                        onPauseRemoteVideoFeed();
                    }
                });

            }

            // or remote user leaves channel
            else if (state == Constants.REMOTE_VIDEO_STATE_STOPPED && reason == Constants.REMOTE_VIDEO_STATE_REASON_REMOTE_OFFLINE) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // wait for user to come online again
                        onRemoteUserLeaveChannel();
                    }
                });
            }

            // On remote user unmute video
            else if(state == Constants.REMOTE_VIDEO_STATE_DECODING && reason == Constants.REMOTE_VIDEO_STATE_REASON_REMOTE_UNMUTED) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        onResumeLocalVideoFeed();
                        onResumeRemoteVideoFeed();
                    }
                });
            }

            // other states of remote video stream

            else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(LOG_CAT, "state: " + String.valueOf(state) + ", " + "reason: " + String.valueOf(reason));
                    }
                });

            }

        }

        @Override
        public void onAudioRouteChanged(int routing) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(LOG_CAT, "Routing mode: " + String.valueOf(routing));
                    boolean speakerEnabled = mRtcEngine.isSpeakerphoneEnabled();

                    Log.d(LOG_CAT, "On Audio Route Changed: " + "speaker phone enabled: " + speakerEnabled); // want to see true
                }
            });
        }

        @Override
        public void onTokenPrivilegeWillExpire(String token) {
            Log.d(LOG_CAT, "Token expires in 30 seconds. Request a new token");
            super.onTokenPrivilegeWillExpire(token);
            mRtcEngine.renewToken(getToken());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageViewJoinBtn = findViewById(R.id.joinBtn);

        imageViewAudioBtn = findViewById(R.id.audioBtn);
        imageViewAudioBtn.setImageResource(R.drawable.icons8_unmute_96);

        imageViewLeaveBtn = findViewById(R.id.leaveBtn);
        imageViewLeaveBtn.setImageResource(R.drawable.icons8_cross_mark_96);

        imageViewCameraBtn = findViewById(R.id.cameraBtn);
        imageViewCameraBtn.setImageResource(R.drawable.icons8_video_call_96);


        // prompt user to allow permission to access audio and camera
        // System will only ask twice for each permission
        checkSelfPermission();


        // On clicking the join Btn
        imageViewJoinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_CAT, "Press join btn");
                if (mRtcEngine != null) {

//                    joinChannel(getToken(), getString(R.string.app_channel_name));
                    joinChannel(getString(R.string.app_temp_token), getString(R.string.app_channel_name));
                }
                // on second start up, check for permission, if granted, init engine, otherwise, do nothing
                // For better code, use SharedPreferences to retrieve mRtcEngine
                else {
                    if(ContextCompat.checkSelfPermission(context, REQUESTED_PERMISSIONS[0])
                            + ContextCompat.checkSelfPermission(
                            context, REQUESTED_PERMISSIONS[1])
                            == PackageManager.PERMISSION_GRANTED) {
                        try {
                        initAgoraEngine();

                        // Call setupSession() before joinChannel()
                        setupSession();

//                        joinChannel(getToken(), getString(R.string.app_channel_name));
                            joinChannel(getString(R.string.app_temp_token), getString(R.string.app_channel_name));
                        }
                        catch (Exception e) {
                            Log.e(LOG_CAT, "error init engine");
                        }
                    }
                }

            }
        });

        imageViewAudioBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRtcEngine != null) {
                    audioMuted = !audioMuted;
                    mRtcEngine.muteLocalAudioStream(audioMuted);

                    if (audioMuted) {
                        imageViewAudioBtn.setImageResource(R.drawable.icons8_mute_96);
                    } else {
                        imageViewAudioBtn.setImageResource(R.drawable.icons8_unmute_96);
                    }
                }
            }
        });

        imageViewCameraBtn.setOnClickListener(new View.OnClickListener() {
            // control local video state
            // setupSession() sets videoMuted to false at the start

            @Override
            public void onClick(View v) {
                if (mRtcEngine != null) {
                    videoMuted = !videoMuted;
                    mRtcEngine.muteLocalVideoStream(videoMuted);
                    // video muted
                    if (videoMuted) {
                        onPauseLocalVideoFeed();
                        imageViewCameraBtn.setImageResource(R.drawable.icons8_no_video_96);
                    }
                    // video not muted
                    else {
                        onResumeLocalVideoFeed();
                        imageViewCameraBtn.setImageResource(R.drawable.icons8_video_call_96);
                    }

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
        Log.d(LOG_CAT, "ON Destroy Activity");
        super.onDestroy();
        RtcEngine.destroy();
        mRtcEngine = null;
    }

    public void checkSelfPermission() {
        Log.d(LOG_CAT, "checkSelfPermission");
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
        Log.d(LOG_CAT, "check Permission Result");
        switch (requestCode) {
            case PERMISSION_REQ_ID: {
                if (grantResults[0] + grantResults[1] != PackageManager.PERMISSION_GRANTED) {
//                    Log.i(LOG_CAT, "Need permissions " + Manifest.permission.RECORD_AUDIO + "/" + Manifest.permission.CAMERA);
                }
                // only initialize agora engine if both permissions are granted
                else {

                    try {
                        initAgoraEngine();
                        setupSession();

                    } catch (Exception e) {
                        Log.e(LOG_CAT, "error init engine");

                        throw new RuntimeException("NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(e));
                    }



                }
                break;
            }
        }
    }



    private void initAgoraEngine() throws Exception{
        Log.d(LOG_CAT, "INIT ENGINE");
        mRtcEngine = RtcEngine.create(getBaseContext(), getString(R.string.agora_app_id), mRtcEventHandler);
    }

    private void setupSession() {
        mRtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_COMMUNICATION);


        mRtcEngine.enableVideo();


        // by default videoMuted is assigned false
        // the line below to dictates whether user joins with video muted or not
        videoMuted = false;

        mRtcEngine.setVideoEncoderConfiguration(new VideoEncoderConfiguration(new VideoEncoderConfiguration.VideoDimensions(1080, 2220),
                VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_30,
                VideoEncoderConfiguration.STANDARD_BITRATE,
                VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT));

        mRtcEngine.setDefaultAudioRoutetoSpeakerphone(true);
        audioMuted = false;
    }

    // engine automatically assign uid to local user
    private void joinChannel(String token, String name) {
//        mRtcEngine.joinChannel(getString(R.string.app_temp_token), "Test Channel 2", "Extra Optional Data", 0);

        mRtcEngine.joinChannel(token, name, "Extra Optional Data", 0);

    }

    private void leaveChannel() {
        Log.d(LOG_CAT, "leave channel");
        mRtcEngine.leaveChannel();
    }

    private void setupLocalVideoFeed() {
        Log.d(LOG_CAT, "Set up Local Video Feed");
        FrameLayout videoContainer = findViewById(R.id.floating_video_container);
        SurfaceView videoSurface = RtcEngine.CreateRendererView(getBaseContext());
        // put the surface view in front so it becomes visible
        videoSurface.setZOrderMediaOverlay(true);
        // add the surface View to the video Container Layout
        videoContainer.addView(videoSurface);
        // set up local video into the surface view
        mRtcEngine.setupLocalVideo(new VideoCanvas(videoSurface, VideoCanvas.RENDER_MODE_FIT, 0));
        // Make the join button invisible
        ImageView joinBtn = findViewById(R.id.joinBtn);
        joinBtn.setVisibility(View.INVISIBLE);
    }

    private void onPauseLocalVideoFeed() {
        FrameLayout videoContainer = findViewById(R.id.floating_video_container);
        ImageView noCamera = new ImageView(context);
        noCamera.setBackgroundResource(R.color.black);
        videoContainer.addView(noCamera);
    }

    private void onResumeLocalVideoFeed() {
        FrameLayout videoContainer = findViewById(R.id.floating_video_container);
        ImageView noCamera = (ImageView) videoContainer.getChildAt(1);
        videoContainer.removeView(noCamera);
    }

    // uid of the remote user
    // always add the surface view as the first child of the video container
    private void setupRemoteVideoFeed(int uid) {
        Log.i(LOG_CAT, "setupRemoteVideoFeed");
        FrameLayout videoContainer = findViewById(R.id.bg_video_container);
        SurfaceView videoSurface = RtcEngine.CreateRendererView(getBaseContext());
        videoContainer.addView(videoSurface);
        mRtcEngine.setupRemoteVideo(new VideoCanvas(videoSurface, VideoCanvas.RENDER_MODE_FIT, uid));
//        mRtcEngine.setRemoteSubscribeFallbackOption(io.agora.rtc.Constants.STREAM_FALLBACK_OPTION_AUDIO_ONLY);
    }

    private void onPauseRemoteVideoFeed() {
        Log.d(LOG_CAT, "onPauseRemoteVideoFeed");
        FrameLayout videoContainer = findViewById(R.id.bg_video_container);
        ImageView noCamera = new ImageView(context);
        noCamera.setBackgroundResource(R.color.black);
        videoContainer.addView(noCamera);

    }

    private void onResumeRemoteVideoFeed() {
        FrameLayout videoContainer = findViewById(R.id.bg_video_container);
        ImageView noCamera = (ImageView) videoContainer.getChildAt(1);
        videoContainer.removeView(noCamera);

    }

    private void onRemoteUserLeaveChannel() {
        FrameLayout videoContainer = findViewById(R.id.bg_video_container);
        videoContainer.removeAllViews();
        onWaitForRemoteUser();

    }

    private void onLocalUserLeaveChannel() {
        FrameLayout localVideoContainer = findViewById(R.id.floating_video_container);
        localVideoContainer.removeAllViews();

        FrameLayout remoteVideoContainer = findViewById(R.id.bg_video_container);
        remoteVideoContainer.removeAllViews();

        ImageView joinBtn = findViewById(R.id.joinBtn);
        joinBtn.setVisibility(View.VISIBLE);

        imageViewCameraBtn.setImageResource(R.drawable.icons8_video_call_96);
        imageViewAudioBtn.setImageResource(R.drawable.icons8_unmute_96);
    }

    // Waiting for remote user to come online
    private void onWaitForRemoteUser() {
        // The same as onPauseRemoteVideoFeed(): use an imageView to cover the remote video container
        FrameLayout videoContainer = findViewById(R.id.bg_video_container);
        ImageView noCamera = new ImageView(context);
        noCamera.setBackgroundResource(R.color.black);
        videoContainer.addView(noCamera);
    }

    // When remote user comes online, remove the cover ImageView, so now remote video container has no child

    private void onRemoteUserJoinChannelSuccess() {
        FrameLayout videoContainer = findViewById(R.id.bg_video_container);
        ImageView noCamera = (ImageView) videoContainer.getChildAt(0);
        videoContainer.removeView(noCamera);
    }

    private void onLocalUserJoinChannelSuccess() {
        onWaitForRemoteUser();
        setupLocalVideoFeed();

        int volume1 = 400;
        int volume2 = 400;
// Sets the playback audio level of all remote users.
        mRtcEngine.adjustPlaybackSignalVolume(volume1);

// Sets the volume of the recorded signal.
        mRtcEngine.adjustRecordingSignalVolume(volume2);

//        mRtcEngine.enableInEarMonitoring(true);

        mRtcEngine.setEnableSpeakerphone(true);

        boolean speakerEnabled = mRtcEngine.isSpeakerphoneEnabled();

        Log.d(LOG_CAT, "speaker phone enabled: " + speakerEnabled); // want to see true

//        mRtcEngine.enableInEarMonitoring(true);

        int volume3 = 25;
        // Sets the in-ear monitoring volume.
        mRtcEngine.setInEarMonitoringVolume(volume3);

    }

    private String getToken() {
        String appID = getString(R.string.agora_app_id);
        String appCertificate = getString(R.string.agora_app_certificate);
        String channelName = getString(R.string.app_channel_name);
        Integer expirationTimeInSeconds = getResources().getInteger(R.integer.app_channel_expiration_time_in_seconds);
        int uid = 0;
        Role role = Role.Role_Publisher;
        int timeStamps = (int) System.currentTimeMillis() / 1000 + expirationTimeInSeconds;
        return new RtcTokenBuilder().buildTokenWithUid(appID, appCertificate, channelName, uid, role, timeStamps);
    }
}