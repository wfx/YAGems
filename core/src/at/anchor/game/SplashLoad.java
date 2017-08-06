package at.anchor.game;

import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.I18NBundle;

public class SplashLoad {

    private EsthetiqueGems _game;

    private I18NBundle _lang;

    String _loadingText;

    // Positions
    private Vector2 _fntPos;

    GlyphLayout _layout;

    public SplashLoad(EsthetiqueGems game, String keyText) {

        _game = game;

        _lang = new I18NBundle();
        _lang = _game.getAssetManager().get("i18n/stateLoad", I18NBundle.class);
        _loadingText =  _lang.get(keyText);

        // Centered
        _layout = new GlyphLayout();
        _layout.setText(_game._fontH1, _loadingText);

        _fntPos = new Vector2((EsthetiqueGems.VIRTUAL_WIDTH - _layout.width) / 2, (EsthetiqueGems.VIRTUAL_HEIGHT - _layout.height) / 2);

    }

    public void draw(){
        SpriteBatch batch = _game.getSpriteBatch();
        _game._fontSplashLoad.draw(batch, _loadingText, _fntPos.x, _fntPos.y);
        return;
    }
}
