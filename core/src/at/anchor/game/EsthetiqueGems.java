package at.anchor.game;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.Logger;

import java.util.HashMap;

public class EsthetiqueGems extends ApplicationAdapter {

    private Logger _logger = null;

    // Video
    private SpriteBatch _batch = null;
    private OrthographicCamera _camera = null;
    private Rectangle _viewport = null;

    public static final int VIRTUAL_WIDTH = 720;
    public static final int VIRTUAL_HEIGHT = 1280;
    private static final float ASPECT_RATIO = 1.7777f; // 0.5625f

    // States
    private HashMap<String, State> _states = null;
    private State _currentState = null;
    private State _nextState = null;
    private State _oldState = null;

    // Assets
    private AssetManager _assetManager = null;

    // UI Buttons
    private TextureAtlas _atlasUI;
    private TextureAtlas _atlasButtons;

    // Gems / Particles
    private TextureAtlas _atlasGems;
    private TextureAtlas _atlasParticle;

    // i18n
    private I18NBundle _lang = null;

    public BitmapFont _fontSplashLoad = null;
    public BitmapFont _fontH1 = null;
    public BitmapFont _fontP = null;
    public BitmapFont _fontPBold = null;

    // Mouse pointer
    private TextureRegion _imgMouse = null;
    private Vector3 _mousePos = null;

    @Override
    public void create() {
        // Logger
        _logger = new Logger("EsthetiqueGems");

        //Gdx
        _batch = new SpriteBatch();
        _camera = new OrthographicCamera(VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
        _camera.setToOrtho(true, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);

        // Create states table
        _states = new HashMap<String, State>();

        // Create assets manager
        _assetManager = new AssetManager();

        // Fonts
        _fontSplashLoad = new BitmapFont();
        _fontH1 = new BitmapFont();
        _fontP = new BitmapFont();
        _fontPBold = new BitmapFont();
        initFont();

        // Mouse pos
        _mousePos = new Vector3();

        // Load general assets
        _assetManager.load("i18n/stateLoad", I18NBundle.class);
        _assetManager.finishLoading();

        // UI Buttons
        _atlasUI = new TextureAtlas(Gdx.files.internal("img/UI.pack"));
        _atlasButtons = new TextureAtlas(Gdx.files.internal("img/Buttons.pack"));

        // Gems
        _atlasGems = new TextureAtlas(Gdx.files.internal("img/Gems.pack"));
        _atlasParticle = new TextureAtlas(Gdx.files.internal("img/Partc.pack"));

        // Get general assets
        _imgMouse = new TextureRegion(_atlasUI.findRegion("pointer"));
        _imgMouse.flip(false, true);

        // Mouse hidden
        Gdx.input.setCursorCatched(true);

        // Create states
        _states.put("StateMenu", new StateMenu(this));
        _states.put("StateGame", new StateGame(this));
        _states.put("StateHowto", new StateHowto(this));
        _states.put("StateCredits", new StateCredits(this));

        // Assign initial state
        changeState("StateMenu");
    }

    @Override
    public void render() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        _camera.update();

        Gdx.gl.glViewport((int) _viewport.x, (int) _viewport.y,
                (int) _viewport.width, (int) _viewport.height);

        // Start rendering
        _batch.begin();
        _batch.setProjectionMatrix(_camera.combined);
        // Update and render current state
        if (_currentState != null) {
            _currentState.update(Gdx.graphics.getDeltaTime());
            _currentState.render();
        }
        // Render mouse on top
        if (Gdx.app.getType() != Application.ApplicationType.Android) {
            _mousePos.x = Gdx.input.getX();
            _mousePos.y = Gdx.input.getY();
            _camera.unproject(_mousePos);
            _batch.draw(_imgMouse, _mousePos.x, _mousePos.y);
        }
        _batch.end();

        // Perform pending memory unloading, safely
        performPendingAssetsUnloading();

        // Perform pending state changes, memory safe
        performPendingStateChange();

    }

    @Override
    public void resize(int arg0, int arg1) {
        _logger.info("Resizing to: " + arg0 + "x" + arg1);

        // calculate new viewport
        float aspectRatio = (float) arg0 / (float) arg1;
        float scale;
        Vector2 crop = new Vector2(0f, 0f);

        if (aspectRatio > ASPECT_RATIO) {
            scale = (float) arg1 / (float) VIRTUAL_HEIGHT;
            crop.x = (arg0 - VIRTUAL_WIDTH * scale) / 2.0f;
        } else if (aspectRatio < ASPECT_RATIO) {
            scale = (float) arg0 / (float) VIRTUAL_WIDTH;
            crop.y = (arg1 - VIRTUAL_HEIGHT * scale) / 2.0f;
        } else {
            scale = (float) arg0 / (float) VIRTUAL_WIDTH;
        }

        float w = (float) VIRTUAL_WIDTH * scale;
        float h = (float) VIRTUAL_HEIGHT * scale;
        _viewport = new Rectangle(crop.x, crop.y, w, h);
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
        _assetManager.dispose();
        _atlasUI.dispose();
        _atlasButtons.dispose();
        _atlasGems.dispose();
        _atlasParticle.dispose();
        _fontP.dispose();
        _fontH1.dispose();
        _fontSplashLoad.dispose();
        _fontPBold.dispose();
		_batch.dispose();
    }

    @Override
    public void pause() {
    }

    public boolean changeState(String stateName) {
        // Check if it's exit state
        if (stateName.equals("StateQuit")) {
            Gdx.app.exit();
        }

        // Fetch new state and, therefore, schedule safe change
        _nextState = _states.get(stateName);

        return _nextState != null;

    }

    public AssetManager getAssetManager() {
        return _assetManager;
    }

    public TextureAtlas getAtlasUI() {
        return _atlasUI;
    }

    public TextureAtlas getAtlasButtons() {
        return _atlasButtons;
    }

    public TextureAtlas getAtlasGems() {
        return _atlasGems;
    }

    public TextureAtlas getAtlasParticle() {
        return _atlasParticle;
    }

    public SpriteBatch getSpriteBatch() {
        return _batch;
    }

    public OrthographicCamera getCamera() {
        return _camera;
    }

    private void performPendingStateChange() {
        if (_nextState != null) {
            if (_currentState != null) {
                // Pause current state
                _currentState.pause();
            }

            // Cancel input processor
            Gdx.input.setInputProcessor(null);

            // Schedule resource unload
            _oldState = _currentState;

            // Assign new state
            _currentState = _nextState;

            // Load new state
            _currentState.load();

            // Nullify scheduled state change
            _nextState = null;

            // Resume state
            _currentState.resume();

            // Listen to back key (android)
            Gdx.input.setCatchBackKey(true);
        }
    }

    private void performPendingAssetsUnloading() {
        // Unload old state if there was one and it's not the same one as the current one
        if (_oldState != null && _oldState != _currentState) {
            _oldState.unload();
            _oldState = null;
        }
    }

    public void initFont() {
        FreeTypeFontGenerator fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("font/CaviarDreamsBold.ttf"));
        FreeTypeFontParameter fontParameter = new FreeTypeFontParameter();

        fontParameter.size = 42;
        fontParameter.genMipMaps = true;
        fontParameter.magFilter = Texture.TextureFilter.Linear;
        fontParameter.minFilter = Texture.TextureFilter.Linear;
        fontParameter.color = new Color(1f, 1f, 1f, 1f);
        fontParameter.shadowColor = new Color(0.2f, 0.2f, 0.2f, 0.6f);
        fontParameter.shadowOffsetY = 3;
        fontParameter.flip = true;
        _fontSplashLoad = fontGenerator.generateFont(fontParameter);
        _fontSplashLoad.setUseIntegerPositions(false);
        fontGenerator.dispose();

        fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("font/CaviarDreamsBold.ttf"));
        fontParameter = new FreeTypeFontParameter();

        fontParameter.size = 42;
        fontParameter.genMipMaps = true;
        fontParameter.magFilter = Texture.TextureFilter.Linear;
        fontParameter.minFilter = Texture.TextureFilter.Linear;
        fontParameter.color = new Color(0f, 0f, 0f, 1f);
        fontParameter.shadowColor = new Color(0.8f, 0.8f, 0.8f, 0.6f);
        fontParameter.shadowOffsetY = 3;
        fontParameter.flip = true;
        _fontH1 = fontGenerator.generateFont(fontParameter);
        _fontH1.setUseIntegerPositions(false);
        fontGenerator.dispose();

        fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("font/CaviarDreamsBold.ttf"));
        fontParameter = new FreeTypeFontParameter();

        fontParameter.size = 32;
        fontParameter.genMipMaps = true;
        fontParameter.magFilter = Texture.TextureFilter.Linear;
        fontParameter.minFilter = Texture.TextureFilter.Linear;
        fontParameter.color = new Color(0f, 0f, 0f, 1f);
        fontParameter.shadowColor = new Color(0.8f, 0.8f, 0.8f, 0.6f);
        fontParameter.shadowOffsetY = 3;
        fontParameter.flip = true;
        _fontP = fontGenerator.generateFont(fontParameter);
        _fontP.setUseIntegerPositions(false);
        fontGenerator.dispose();

    }
}
