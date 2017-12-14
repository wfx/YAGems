package at.anchor.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.I18NBundle;

public class StateHowto extends State {

    private enum State {Loading, Active}

    // State
    private State _state;

    // Resources
    private TextureRegion _imgBackground;
    private Sound _selectSFX;

    // Lang bundle
    private I18NBundle _lang;

    // Loading feedback
    SplashLoad _loading;

    // Strings
    String _textH1;
    String _textP;

    // Positions
    private Vector2 _posH1;
    private Vector2 _posP;

    private boolean _readyToChange;

    GlyphLayout _layout;

    public StateHowto (EsthetiqueGems esthetiqueGems) {
        super(esthetiqueGems);

        // Lang bundle
        _lang = new I18NBundle();

        _loading = new SplashLoad(_parent, "Loading_Howto");

        // Initial state
        _state = State.Loading;

        // Resources are initially null
        _imgBackground = null;

        // Set loading position
        _layout = new GlyphLayout();
        _readyToChange = false;
    }


    @Override
    public void load() {

        AssetManager assetManager = _parent.getAssetManager();
        assetManager.load("i18n/stateHowto", I18NBundle.class);
        assetManager.load("img/stateHowtoBg.png", Texture.class);
        assetManager.load("audio/select.ogg", Sound.class);

    }

    @Override
    public void unload() {

        // Set references to null
        _imgBackground = null;
        _selectSFX = null;

        // Unload resources
        AssetManager assetManager = _parent.getAssetManager();
        assetManager.unload("i18n/stateHowto");
        assetManager.unload("img/stateHowtoBg.png");
        assetManager.unload("audio/select.ogg");
    }

    @Override
    public void assignResources() {

        // Retrieve resources
        AssetManager assetManager = _parent.getAssetManager();
        _imgBackground = new TextureRegion(assetManager.get("img/stateHowtoBg.png", Texture.class));
        _selectSFX = assetManager.get("audio/select.ogg", Sound.class);

        _imgBackground.flip(false, true);

        _lang = assetManager.get("i18n/stateHowto", I18NBundle.class);
        // Load strings
        _textH1 = _lang.get("Howto_Title");
        _textP = _lang.get("Howto_Text");

        // Set positions now that we now about sizes
        _layout.setText(_parent._fontH1, _textH1);
        _posH1 = new Vector2((EsthetiqueGems.VIRTUAL_WIDTH - _layout.width) / 2, 272);
        _posP = new Vector2(52, 552);

        Gdx.input.setInputProcessor(this);
    }

    @Override
    public void update(double deltaT) {

        if (_state == State.Loading) {
            if (_parent.getAssetManager().update()) {
                assignResources();
                _state = State.Active;
            }

            return;
        }
    }

    @Override
    public void render () {

        SpriteBatch batch = _parent.getSpriteBatch();

        // STATE LOADING - Just render loading
        if (_state == State.Loading) {
            _loading.draw();
            return;
        }

        // STATE ACTIVE
        batch.draw(_imgBackground, 0, 0);

        _parent._fontH1.draw(batch, _textH1, _posH1.x, _posH1.y);
        _parent._fontP.draw(batch, _textP, _posP.x, _posP.y, EsthetiqueGems.VIRTUAL_WIDTH - 104, Align.left, true);

    }

    @Override
    public boolean touchDown(int arg0, int arg1, int arg2, int arg3) {

        _readyToChange = true;
        _selectSFX.play();

        return false;
    }

    @Override
    public boolean touchUp(int arg0, int arg1, int arg2, int arg3) {

        // Left click
        if (arg3 == 0 && _readyToChange) {
            _parent.changeState("StateMenu");
        }

        return false;
    }

    @Override
    public boolean keyDown(int arg0) {

        if(arg0 == Input.Keys.BACK){
            _parent.changeState("StateMenu");
        }

        return false;
    }

    @Override
    public void resume() {

        _readyToChange = false;
        _state = State.Loading;
    }
}