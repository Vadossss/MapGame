package com.example.mapgame;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.TextView;
import android.widget.ProgressBar;
import android.widget.VideoView;

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
import com.google.ar.core.Session;
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
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

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
    private int enemy_model = 0;


    private Button hit_btn;
    public ProgressBar progressBar;
    public TextView progressValue;
    private int lives; // Изначальное количество "жизней"



    private ProgressBar player_hp;
    private ProgressBar player_mana;
    private Button reject_1;
    private Button reject_2;
    private Button reject_3;
    private Button reject_4;
    private  Button[] rejects;

    private int pl_lives = 100;
    private int mana = 280;
    private Timer timer;
    private TimerTask t_task;
    Handler handler;
    private boolean rejected = true;
    private int time_count = 0;
    private boolean touched = false;

    private VideoView videoDialog;
    private String[] questInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);



        setContentView(R.layout.activity_ar);
        hit_btn = findViewById(R.id.hit);
        videoDialog=findViewById(R.id.cut_scene);

        Intent intentMain = getIntent();
        questInfo = intentMain.getStringArrayExtra("name");
        enemy_model = getResources().getIdentifier(questInfo[12], "raw", getPackageName());
        lives = Integer.parseInt(questInfo[13]);
        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(this, CardGameActivity.class);
            setResult(RESULT_CANCELED, intent);
            finish();
        });



        player_hp = findViewById(R.id.hp);
        player_mana = findViewById(R.id.mana);
        player_hp.setProgress(pl_lives);
        player_mana.setProgress(mana);

        reject_1 = findViewById(R.id.rej_1);
        reject_2 = findViewById(R.id.rej_2);
        reject_3 = findViewById(R.id.rej_3);
        reject_4 = findViewById(R.id.rej_4);
        rejects = new Button[] {reject_1, reject_2, reject_3, reject_4};
        for (Button btn:rejects){
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    for (Button btn:rejects){
                        btn.setEnabled(false);
                        btn.setBackgroundColor(Color.WHITE);
                    }
                    rejected=true;
                    time_count=  -20 + time_count;
                }
            });
            btn.setEnabled(false);
        }

        hit_btn.setOnClickListener(v -> {
            if(placed)
            {
                String textFromTextView = progressValue.getText().toString();
                try {
                    // Пытаемся преобразовать текст в целое число
                    int intValue = Integer.parseInt(textFromTextView);
                    lives -= 9;
                    progressBar.setProgress(lives);
                    progressValue.setText(String.valueOf(lives));
                    if (lives < 1) {
                        lives = 0;
                        progressBar.setProgress(lives);
                        dialog();
                    }


                    if(!touched)
                    {
                        mana=210;
                        player_mana.setProgress(mana);
                        touched=true;
                    }
                    else
                    {
                        mana -= 70;
                        player_mana.setProgress(mana);
                        if(mana < 1)
                        {
                            time_count = 0;
                            touched=false;
                            hit_btn.setEnabled(false);
                            timer = new Timer(false);
                            handler = new Handler();
                            t_task = new TimerTask() {
                                @Override
                                public void run() {
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {


                                            if (rejected && time_count == 25) {
                                                time_count = 0;
                                                rejected = false;
                                                Random rnd = new Random();
                                                int i = rnd.nextInt(4);
                                                rejects[i].setEnabled(true);
                                                rejects[i].setBackgroundColor(Color.RED);
                                            }
                                            if (!rejected) {
                                                if (time_count == 20) {
                                                    pl_lives -= 20;
                                                    player_hp.setProgress(pl_lives);
                                                    if (pl_lives < 1) {

                                                        looseDialog();

                                                    }
                                                    for (Button btn : rejects) {
                                                        btn.setEnabled(false);
                                                        btn.setBackgroundColor(Color.WHITE);
                                                    }
                                                    rejected = true;
                                                    time_count = 0;

                                                }
                                            }
                                            if (mana > 280) {
                                                time_count = 0;
                                                player_mana.setProgress(mana);
                                                hit_btn.setEnabled(true);
                                                timer.cancel();

                                            }
                                            mana += 2;
                                            player_mana.setProgress(mana);
                                            time_count += 1;
                                        }
                                    });
                                }
                            };
                            timer.schedule(t_task,0, 100);
                        }
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
                .setSource(this, enemy_model)
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
                                    progressBar.setMax(lives);
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


    private void looseDialog()
    {

        String questName = questInfo[0];
        if(questName =="quest1.2")
        {
            videoDialog.setVideoPath("android.resource://" + getPackageName() + "/" + R.raw.koshei_lose);
        }
        else {
            videoDialog.setVideoPath("android.resource://" + getPackageName() + "/" + R.raw.solovei_lose2);
        }

        setVideo();

        videoDialog.setOnCompletionListener( s -> {

            View dialog = new View(this);
            dialog = LayoutInflater.from(this).inflate(R.layout.dialog_lose, null);
            Dialog myDialog = new Dialog(this);
            TextView text = (TextView) dialog.findViewById(R.id.locationQuest);
            text.setText("Вы проиграли!");
            myDialog.setContentView(dialog);
            myDialog.setCancelable(false);
            myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            myDialog.getWindow().setWindowAnimations(R.style.dialog_animation_fade);
            myDialog.show();

            Button buttonReboot = (Button) dialog.findViewById(R.id.btnReboot);
            buttonReboot.setOnClickListener(v -> {
                myDialog.dismiss();
                Intent intent = new Intent(this, ArActivity.class);
                startActivity(intent);
                finish();
            });

            Button button = (Button) dialog.findViewById(R.id.btnBattle);
            button.setOnClickListener(v -> {
                myDialog.dismiss();
                finishGame(RESULT_CANCELED);
            });
        });
    }

    private void dialog() {
        FadePressets fade = new FadePressets(this, this);
        fade.fadeInDimmingView();
        String questName = questInfo[0];
        if(questName =="quest1.2")
        {
            videoDialog.setVideoPath("android.resource://" + getPackageName() + "/" + R.raw.koshei_win);
        }
        else {
            videoDialog.setVideoPath("android.resource://" + getPackageName() + "/" + R.raw.solovei_win2);
        }
        setVideo();
        videoDialog.setOnCompletionListener( v -> {
            videoDialog.stopPlayback();
            fade.fadeOutView();
            videoDialog.setVisibility(View.INVISIBLE);
            fade.fadeInDimmingView();
            View dialog = new View(this);
            dialog = LayoutInflater.from(this).inflate(R.layout.dialog_enemy, null);
            Dialog myDialog = new Dialog(this);
            TextView text = (TextView) dialog.findViewById(R.id.locationQuest);
            TextView questText = (TextView) dialog.findViewById(R.id.questText);
            questText.setText("Вы смогли победить! Враг бежит в страхе.");
            text.setText("Вы выиграли!");
            myDialog.setContentView(dialog);
            myDialog.setCancelable(false);
            myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            myDialog.getWindow().setWindowAnimations(R.style.dialog_animation_fade);
            myDialog.show();


            Button button = (Button) dialog.findViewById(R.id.btnBattle);
            button.setText("Выйти на карту");
            button.setOnClickListener(s -> {
                myDialog.dismiss();
                finishGame(RESULT_OK);
            });

        });
    }
    private void setVideo() {
        videoDialog.setOnPreparedListener( v-> {
            ViewGroup.LayoutParams layoutParams = videoDialog.getLayoutParams();
            View d = (View)findViewById(R.id.main);
            layoutParams.width = d.getLayoutParams().width;
            layoutParams.height = d.getLayoutParams().height;
            videoDialog.setLayoutParams(layoutParams);
            videoDialog.setClickable(true);
            videoDialog.setFocusable(true);
        });
        videoDialog.start();
        videoDialog.setVisibility(View.VISIBLE);
    }

    private void finishGame(int resultCode) {
        Intent intent = new Intent(this, MainActivity.class);
        setResult(resultCode, intent);
        finish();
    }
}
