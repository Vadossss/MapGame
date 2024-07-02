package com.example.mapgame;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.TextView;
import android.widget.ProgressBar;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentOnAttachListener;

import com.google.ar.core.Anchor;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.Trackable;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.SceneView;
import com.google.ar.sceneform.Sceneform;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.BaseArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.List;

public class ArActivity extends AppCompatActivity implements
        FragmentOnAttachListener,
        Scene.OnUpdateListener,
        BaseArFragment.OnSessionConfigurationListener,
        ArFragment.OnViewCreatedListener {

    private ImageView btnBack;
    private Button btnAR;


    private ArFragment arFragment;
    private Renderable model;
    private ViewRenderable life_bar;
    private boolean placed = false;
    private int koshey_model = 0;


    private Button hit_btn;
    public ProgressBar progressBar;
    public TextView progressValue;
    private int lives = 100; // Изначальное количество "жизней"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_ar);
        hit_btn = findViewById(R.id.hit_btn);

        Intent intentMain = getIntent();
        String[] questInfo = intentMain.getStringArrayExtra("name");
        koshey_model = getResources().getIdentifier(questInfo[12], "raw", getPackageName());
        lives = Integer.parseInt(questInfo[13]);
        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            setResult(RESULT_CANCELED, intent);
            finish();
        });

        hit_btn.setOnClickListener(v -> {
            if(placed)
            {
                String textFromTextView = progressValue.getText().toString();
                try {
                    // Пытаемся преобразовать текст в целое число
                    int intValue = Integer.parseInt(textFromTextView);
                    lives -= 10;
                    progressBar.setProgress(lives);
                    progressValue.setText(String.valueOf(lives));
                    if (lives < 1) {
                        lives = 0;
                        Intent intent = new Intent(this, MainActivity.class);
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                } catch (NumberFormatException e) {
                    // В случае ошибки преобразования текста в int
                    e.printStackTrace();
                }
            }
        });

        getSupportFragmentManager().addFragmentOnAttachListener(this);

        if (savedInstanceState == null) {
            if (Sceneform.isSupported(this)) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.arFragment, ArFragment.class, null)
                        .commit();
            }
        }
        loadModels();

    }

    @Override
    public void onAttachFragment(@NonNull FragmentManager fragmentManager, @NonNull Fragment fragment) {
        if (fragment.getId() == R.id.arFragment) {
            arFragment = (ArFragment) fragment;
            arFragment.setOnSessionConfigurationListener(this);
            arFragment.setOnViewCreatedListener(this);
        }
    }

    @Override
    public void onSessionConfiguration(Session session, Config config) {
        if (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
            config.setDepthMode(Config.DepthMode.AUTOMATIC);
        }

    }

    @Override
    public void onViewCreated(ArSceneView arSceneView) {
        arFragment.setOnViewCreatedListener(null);

        // Fine adjust the maximum frame rate
        arSceneView.setFrameRateFactor(SceneView.FrameRate.FULL);
        arFragment.getArSceneView().getScene().addOnUpdateListener(this);

    }

    public void loadModels() {
        WeakReference<ArActivity> weakActivity = new WeakReference<>(this);
        ModelRenderable.builder()
                .setSource(this, koshey_model)
                .setIsFilamentGltf(true)
                .setAsyncLoadEnabled(true)
                .build()
                .thenAccept(model -> {
                    ArActivity activity = weakActivity.get();
                    if (activity != null) {
                        activity.model = model;
                    }
                })
                .exceptionally(throwable -> {
                    Toast.makeText(
                            this, "Unable to load model", Toast.LENGTH_LONG).show();
                    return null;
                });


        ViewRenderable.builder()
                .setView(this, R.layout.lives)
                .build()
                .thenAccept(life_bar -> {
                    ArActivity activity = weakActivity.get();
                    if (activity != null) {
                        activity.life_bar = life_bar;
                    }
                })
                .exceptionally(throwable -> {
                    Toast.makeText(this, "Unable to load model", Toast.LENGTH_LONG).show();
                    return null;
                });


    }

    @Override
    public void onUpdate(FrameTime frameTime) {
        if (arFragment != null) {
            ArSceneView sceneView = arFragment.getArSceneView();
            if (sceneView != null) {
                Frame frame = sceneView.getArFrame();
                if (frame != null) {
                    if (!placed) {
                        Iterator<Plane> trackable = frame.getUpdatedTrackables(Plane.class).iterator();
                        if (trackable.hasNext()) {
                            Plane plane = (Plane) trackable.next();
                            if (plane.getTrackingState() == TrackingState.TRACKING) {

                                List<HitResult> hitTest = frame.hitTest(screenCenter(frame).x, screenCenter(frame).y);
                                Iterator<HitResult> hitTestIterator = hitTest.iterator();
                                if (hitTestIterator.hasNext()) {
                                    HitResult hitResult = hitTestIterator.next();
                                    Anchor modelAnchor = hitResult.createAnchor();
                                    AnchorNode anchorNode = new AnchorNode(modelAnchor);
                                    anchorNode.setParent(arFragment.getArSceneView().getScene());

                                    TransformableNode transformableNode = new TransformableNode(arFragment.getTransformationSystem());
                                    transformableNode.setParent(anchorNode);
                                    transformableNode.setRenderable(this.model)
                                            .animate(true).start();
                                    transformableNode.getRotationController().setEnabled(false);
                                    transformableNode.getScaleController().setEnabled(false);
                                    transformableNode.select();

                                    Node enemy_lives = new Node();
                                    enemy_lives.setParent(transformableNode);
                                    enemy_lives.setEnabled(false);
                                    enemy_lives.setLocalPosition(new Vector3(0.0f, 1.6f, 0.0f));
                                    enemy_lives.setRenderable(life_bar);
                                    enemy_lives.setEnabled(true);

                                    progressBar = life_bar.getView().findViewById(R.id.progressBar);
                                    progressValue = life_bar.getView().findViewById(R.id.textViewProgress);
                                    progressValue.setText(String.valueOf(lives));
                                    progressBar.setProgress(lives);


//                                    transformableNode.setWorldPosition(new Vector3(
//                                            modelAnchor.getPose().tx(),
//                                            modelAnchor.getPose().compose(Pose.makeTranslation(0f, 0.05f, 0f)).ty(),
//                                            modelAnchor.getPose().tz()
//                                    ));
                                    placed = true;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private Vector3 screenCenter(Frame frame) {
        View vw = findViewById(android.R.id.content);
        return new Vector3(vw.getWidth() / 2f, vw.getHeight() / 2f, 0f);
    }
}
