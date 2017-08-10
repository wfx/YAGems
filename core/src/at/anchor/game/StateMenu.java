package at.anchor.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.I18NBundle;


public class StateMenu extends State {

    // Possible menu states
    public enum State {
        Loading,
        TransitionIn,
        Active,
        TransitionOut
    }

    //  Current menu state
    private State _state;

    // i18n
    private I18NBundle _lang;

    // Loading feedback
    SplashLoad _loading;

    //  Preferences
    private Preferences _prefScore;

    GlyphLayout _layout;

    //  UI Buttons
    private Button _playButton;
    private Button _howtoButton;
    private Button _creditsButton;
    private Button _quitButton;

    //  Textures
    private TextureRegion _imgBackground;

    //  Sound
    private Sound _selectSFX;

    //  Text
    private String _txtPlayerName;
    private String _txtPlayerScore;
    private String _txtTopScoreTitle;
    private Vector2 _txtPlayerNamePos;
    private Vector2 _txtPlayerScorePos;
    private Vector2 _txtTopScoreTitlePos;

    // Gems animation
    GemsAnimation _gems;

    // Animation time
    private double _animTime;
    //    private double _animLogoTime;
    private double _animTotalTime;

    // Positions
    private Vector3 _mousePos;

    public StateMenu(EsthetiqueGems esthetiqueGems) {
        super(esthetiqueGems);

        // Initial state
        _state = State.Loading;

        _lang = new I18NBundle();

        _loading = new SplashLoad(_parent, "Loading_Menu");

        // Resources are initially null
        _imgBackground = null;

        // Mouse pos
        _mousePos = new Vector3();

        // Animation times
        _animTime = 0.0;
        _animTotalTime = 0.5;
//        _animLogoTime = 0.5;

        _layout = new GlyphLayout();
    }

    @Override
    public void load() {

        AssetManager assetManager = _parent.getAssetManager();
        assetManager.load("i18n/stateMainMenu", I18NBundle.class);
        assetManager.load("img/stateMainMenuBg.png", Texture.class);
        assetManager.load("img/gemWhite.png", Texture.class);
        assetManager.load("img/gemRed.png", Texture.class);
        assetManager.load("img/gemPurple.png", Texture.class);
        assetManager.load("img/gemOrange.png", Texture.class);
        assetManager.load("img/gemGreen.png", Texture.class);
        assetManager.load("img/gemYellow.png", Texture.class);
        assetManager.load("img/gemBlue.png", Texture.class);
        assetManager.load("img/btnStart.png", Texture.class);
        assetManager.load("img/btnStartClicked.png", Texture.class);
        assetManager.load("img/btnExit.png", Texture.class);
        assetManager.load("img/btnExitClicked.png", Texture.class);
        assetManager.load("img/btnHowto.png", Texture.class);
        assetManager.load("img/btnHowtoClicked.png", Texture.class);
        assetManager.load("img/btnCredits.png", Texture.class);
        assetManager.load("img/btnCreditsClicked.png", Texture.class);
        assetManager.load("audio/select.ogg", Sound.class);
    }

    @Override
    public void unload() {

        // Set references to null
        _prefScore = null;
        _imgBackground = null;
        _playButton.setNull();
        _howtoButton.setNull();
        _creditsButton.setNull();
        _quitButton.setNull();
        _txtPlayerName = null;
        _txtPlayerScore = null;
        _txtTopScoreTitle = null;
        _txtPlayerNamePos = null;
        _txtPlayerScorePos = null;
        _txtTopScoreTitlePos = null;
        _lang = null;
        _selectSFX = null;
        _gems = null;

        // Unload resources
        AssetManager assetManager = _parent.getAssetManager();
        assetManager.unload("i18n/stateMainMenu");
        assetManager.unload("img/stateMainMenuBg.png");
        assetManager.unload("img/gemWhite.png");
        assetManager.unload("img/gemRed.png");
        assetManager.unload("img/gemPurple.png");
        assetManager.unload("img/gemOrange.png");
        assetManager.unload("img/gemGreen.png");
        assetManager.unload("img/gemYellow.png");
        assetManager.unload("img/gemBlue.png");
        assetManager.unload("img/btnStart.png");
        assetManager.unload("img/btnStartClicked.png");
        assetManager.unload("img/btnExit.png");
        assetManager.unload("img/btnExitClicked.png");
        assetManager.unload("img/btnHowto.png");
        assetManager.unload("img/btnHowtoClicked.png");
        assetManager.unload("img/btnCredits.png");
        assetManager.unload("img/btnCreditsClicked.png");
        assetManager.unload("audio/select.ogg");

    }

    @Override
    public void assignResources() {

        AssetManager assetManager = _parent.getAssetManager();

        _lang = assetManager.get("i18n/stateMainMenu", I18NBundle.class);

        // Button Play
        TextureRegion btnPlay = new TextureRegion(assetManager.get("img/btnStart.png", Texture.class));
        TextureRegion btnPlayClicked = new TextureRegion(assetManager.get("img/btnStartClicked.png", Texture.class));
        btnPlay.flip(false,true);
        btnPlayClicked.flip(false,true);
        _playButton = new Button(_parent, 156, 794);
        _playButton.setNormal(btnPlay, btnPlayClicked);
        _playButton.setFont(_parent._fontH1);
        _playButton.setText(_lang.get("Menu_StartGame"));
        _playButton.setTextPos(156 + 74 + 66, 814 );

        // Button EXIT
        TextureRegion btnExit = new TextureRegion(assetManager.get("img/btnExit.png", Texture.class));
        TextureRegion btnExitClicked = new TextureRegion(assetManager.get("img/btnExitClicked.png", Texture.class));
        btnExit.flip(false,true);
        btnExitClicked.flip(false,true);
        _quitButton = new Button(_parent, 156, 894);
        _quitButton.setNormal(btnExit, btnExitClicked);

        // Button How to
        TextureRegion btnHowto = new TextureRegion(assetManager.get("img/btnHowto.png", Texture.class));
        TextureRegion btnHowtoClicked = new TextureRegion(assetManager.get("img/btnHowtoClicked.png", Texture.class));
        btnHowto.flip(false,true);
        btnHowtoClicked.flip(false,true);
        _howtoButton = new Button(_parent, 322, 894);
        _howtoButton.setNormal(btnHowto, btnHowtoClicked);

        // Button Credits
        TextureRegion btnCredits = new TextureRegion(assetManager.get("img/btnCredits.png", Texture.class));
        TextureRegion btnCreditsClicked = new TextureRegion(assetManager.get("img/btnCreditsClicked.png", Texture.class));
        btnCredits.flip(false,true);
        btnCreditsClicked.flip(false,true);
        _creditsButton = new Button(_parent, 496, 894);
        _creditsButton.setNormal(btnCredits, btnCreditsClicked);

        _imgBackground = new TextureRegion(assetManager.get("img/stateMainMenuBg.png", Texture.class));
        _imgBackground.flip(false, true);

        _selectSFX = assetManager.get("audio/select.ogg", Sound.class);

        //  Load score preferences
        _prefScore = Gdx.app.getPreferences("score");

        _txtTopScoreTitle = _lang.get("Menu_TopScoreTitle");
        _txtPlayerName = _prefScore.getString("name", _lang.get("Menu_MissingName"));
        _txtPlayerScore = Integer.toString(_prefScore.getInteger("score", 0));

        _layout.setText(_parent._fontH1, _txtTopScoreTitle);
        _txtTopScoreTitlePos = new Vector2((EsthetiqueGems.VIRTUAL_WIDTH - _layout.width) / 2, 560);
        _layout.setText(_parent._fontH1, _txtPlayerName);
        _txtPlayerNamePos = new Vector2((EsthetiqueGems.VIRTUAL_WIDTH - _layout.width) / 2, 610);
        _layout.setText(_parent._fontH1, _txtPlayerScore);
        _txtPlayerScorePos = new Vector2((EsthetiqueGems.VIRTUAL_WIDTH - _layout.width) / 2, 660);

        _gems = new GemsAnimation(_parent);

        Gdx.input.setInputProcessor(this);
    }

    @Override
    public void update(double deltaT) {
        if (_state == State.Loading) {
            if (_parent.getAssetManager().update()) {
                assignResources();
                _state = State.TransitionIn;
            }
            return;
        } else if (_state == State.TransitionIn) {
            if ((_animTime += deltaT) >= _animTotalTime) {
                _state = State.Active;
            }
        } else if (_state == State.Active) {

        } else if (_state == State.TransitionOut) {

        }
    }

    @Override
    public void render() {
        SpriteBatch batch = _parent.getSpriteBatch();

        // STATE LOADING - Just render loading
        if (_state == State.Loading) {
            _loading.draw();
            return;
        }

        batch.draw(_imgBackground, 0, 0);

        // Top High score
        _parent._fontH1.draw(batch,_txtTopScoreTitle, _txtTopScoreTitlePos.x, _txtTopScoreTitlePos.y);
        _parent._fontH1.draw(batch, _txtPlayerName, _txtPlayerNamePos.x, _txtPlayerNamePos.y);
        _parent._fontH1.draw(batch, _txtPlayerScore, _txtPlayerScorePos.x, _txtPlayerScorePos.y);


        // Draw buttons
        _playButton.render();
        _howtoButton.render();
        _creditsButton.render();
        _quitButton.render();

        _gems.draw(Gdx.graphics.getDeltaTime());

    }

    @Override
    public boolean touchDown(int arg0, int arg1, int arg2, int arg3) {
        if (arg3 == 0) { // Left mouse button clicked
            _mousePos.x = arg0;
            _mousePos.y = arg1;
            _parent.getCamera().unproject(_mousePos);
            _selectSFX.play();
            if (_playButton.isClicked((int) _mousePos.x, (int) _mousePos.y)) {
                _parent.changeState("StateGame");
            } else if (_howtoButton.isClicked((int) _mousePos.x, (int) _mousePos.y)) {
                _parent.changeState("StateHowto");
            } else if (_creditsButton.isClicked((int) _mousePos.x, (int) _mousePos.y)) {
                _parent.changeState("StateCredits");
            } else if (_quitButton.isClicked((int) _mousePos.x, (int) _mousePos.y)) {
                _parent.changeState("StateQuit");
            }
        }
        return false;
    }

    @Override
    public boolean touchUp(int arg0, int arg1, int arg2, int arg3) {
        if (arg3 == 0) { // Left mouse button clicked
            _mousePos.x = arg0;
            _mousePos.y = arg1;
            _parent.getCamera().unproject(_mousePos);

            _playButton.touchUp();
            _howtoButton.touchUp();
            _creditsButton.touchUp();
            _quitButton.touchUp();
        }
        return false;
    }

    @Override
    public boolean keyDown(int arg0) {
        if (arg0 == Input.Keys.BACK) {
            _parent.changeState("StateQuit");
        }

        return false;
    }

    @Override
    public void resume() {
        _state = State.Loading;
    }
}
