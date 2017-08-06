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
import com.badlogic.gdx.utils.IntArray;

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

        //  Load score preferences
        _prefScore = Gdx.app.getPreferences("score");

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
        assetManager.load("img/btnBg.png", Texture.class);
        assetManager.load("img/btnClickedBg.png", Texture.class);
        assetManager.load("img/btnLargeBg.png", Texture.class);
        assetManager.load("img/btnLargeClickedBg.png", Texture.class);
        assetManager.load("img/iconPlay.png", Texture.class);
        assetManager.load("img/iconHowto.png", Texture.class);
        assetManager.load("img/iconCredits.png", Texture.class);
        assetManager.load("img/iconExit.png", Texture.class);
        assetManager.load("audio/select.ogg", Sound.class);

    }

    @Override
    public void unload() {

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
        assetManager.unload("img/btnBg.png");
        assetManager.unload("img/btnClickedBg.png");
        assetManager.unload("img/btnLargeBg.png");
        assetManager.unload("img/btnLargeClickedBg.png");
        assetManager.unload("img/iconPlay.png");
        assetManager.unload("img/iconHowto.png");
        assetManager.unload("img/iconCredits.png");
        assetManager.unload("img/iconExit.png");
        assetManager.unload("audio/select.ogg");

        // Set references to null
        _prefScore = null;
        _imgBackground = null;
        _txtPlayerName = null;
        _txtPlayerScore = null;
        _txtTopScoreTitle = null;
        _txtPlayerNamePos = null;
        _txtPlayerScorePos = null;
        _txtTopScoreTitlePos = null;
        _playButton.setNull();
        _howtoButton.setNull();
        _creditsButton.setNull();
        _quitButton.setNull();
        _lang = null;
        _selectSFX = null;
        _gems = null;

    }

    @Override
    public void assignResources() {

        AssetManager assetManager = _parent.getAssetManager();

        // Button textures and font
        TextureRegion buttonBackgroundLarge = new TextureRegion(assetManager.get("img/btnLargeBg.png", Texture.class));
        TextureRegion buttonBackgroundLargeClicked = new TextureRegion(assetManager.get("img/btnLargeClickedBg.png", Texture.class));
        TextureRegion buttonBackground = new TextureRegion(assetManager.get("img/btnBg.png", Texture.class));
        TextureRegion buttonBackgroundClicked = new TextureRegion(assetManager.get("img/btnClickedBg.png", Texture.class));
        TextureRegion iconPlay = new TextureRegion(assetManager.get("img/iconPlay.png", Texture.class));
        TextureRegion iconHowto = new TextureRegion(assetManager.get("img/iconHowto.png", Texture.class));
        TextureRegion iconCredits = new TextureRegion(assetManager.get("img/iconCredits.png", Texture.class));
        TextureRegion iconQuit = new TextureRegion(assetManager.get("img/iconExit.png", Texture.class));

        buttonBackground.flip(false, true);
        buttonBackgroundClicked.flip(false, true);
        buttonBackgroundLarge.flip(false, true);
        buttonBackgroundLargeClicked.flip(false, true);
        iconPlay.flip(false, true);
        iconHowto.flip(false, true);
        iconCredits.flip(false, true);
        iconQuit.flip(false, true);

        _lang = assetManager.get("i18n/stateMainMenu", I18NBundle.class);

        _imgBackground = new TextureRegion(assetManager.get("img/stateMainMenuBg.png", Texture.class));
        _imgBackground.flip(false, true);

        _selectSFX = assetManager.get("audio/select.ogg", Sound.class);

        _txtTopScoreTitle = _lang.get("Menu_TopScoreTitle");
        _txtPlayerName = _prefScore.getString("name", _lang.get("Menu_MissingName"));
        _txtPlayerScore = Integer.toString(_prefScore.getInteger("score", 0));

        _layout.setText(_parent._fontH1, _txtTopScoreTitle);
        _txtTopScoreTitlePos = new Vector2((EsthetiqueGems.VIRTUAL_WIDTH - _layout.width) / 2, 560);
        _layout.setText(_parent._fontH1, _txtPlayerName);
        _txtPlayerNamePos = new Vector2((EsthetiqueGems.VIRTUAL_WIDTH - _layout.width) / 2, 600);
        _layout.setText(_parent._fontH1, _txtPlayerScore);
        _txtPlayerScorePos = new Vector2((EsthetiqueGems.VIRTUAL_WIDTH - _layout.width) / 2, 640);

        _playButton = new Button(_parent, 156, 794, _lang.get("Menu_StartGame"));
        _quitButton = new Button(_parent, 156, 894, "");
        _howtoButton = new Button(_parent, 322, 894, "");
        _creditsButton = new Button(_parent, 496, 894, "");

        _playButton.setTextGab(120, 24);

        _playButton.setIcon(iconPlay);
        _playButton.setBackground(buttonBackgroundLarge);
        _playButton.setBackgroundClicked(buttonBackgroundLargeClicked);
        _playButton.setFont(_parent._fontH1);

        _howtoButton.setIcon(iconHowto);
        _howtoButton.setBackground(buttonBackground);
        _howtoButton.setBackgroundClicked(buttonBackgroundClicked);

        _creditsButton.setIcon(iconCredits);
        _creditsButton.setBackground(buttonBackground);
        _creditsButton.setBackgroundClicked(buttonBackgroundClicked);

        _quitButton.setIcon(iconQuit);
        _quitButton.setBackground(buttonBackground);
        _quitButton.setBackgroundClicked(buttonBackgroundClicked);

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
//                _animTime = _animLogoTime;
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
