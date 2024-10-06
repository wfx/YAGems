package at.anchor.yagems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import at.anchor.yagems.Main;

public class GemsAnimation {

    private Main _game;
    private TextureRegion[] _imgGems;
    private TextureAtlas _atlasGems;
    private int[] _posX;
    private int _posY;

    double _animTime;
    double _animTotalTime;

    public GemsAnimation(Main mainYAGems) {
        _game = mainYAGems;

        AssetManager assetManager = _game.getAssetManager();

        _imgGems = new TextureRegion[7];
        _posX = new int[7];

        _imgGems[0] = new TextureRegion(_game.getAtlasGems().findRegion("gemWhite"));
        _imgGems[1] = new TextureRegion(_game.getAtlasGems().findRegion("gemRed"));
        _imgGems[2] = new TextureRegion(_game.getAtlasGems().findRegion("gemPurple"));
        _imgGems[3] = new TextureRegion(_game.getAtlasGems().findRegion("gemOrange"));
        _imgGems[4] = new TextureRegion(_game.getAtlasGems().findRegion("gemGreen"));
        _imgGems[5] = new TextureRegion(_game.getAtlasGems().findRegion("gemYellow"));
        _imgGems[6] = new TextureRegion(_game.getAtlasGems().findRegion("gemBlue"));


        for (int i = 0; i < 7; ++i) {
            _imgGems[i].flip(false, true);
            _posX[i] = Main.VIRTUAL_WIDTH - 360 - (76 * 7) / 2 + i * 76;
        }

        _posY = 432;

        _animTime = 0.0;
        _animTotalTime = 0.5;
    }

    public void draw(double deltaT) {
        if(_animTime < 7 * 5 + _animTotalTime)
            _animTime += deltaT;

        SpriteBatch batch = _game.getSpriteBatch();

        for(int i = 0; i < 7; ++i) {
            double composedTime = _animTime - i * _animTotalTime / 7.0f;
            if (composedTime < 0) {
                continue;
            }

            if (composedTime < _animTotalTime) {
                batch.draw(_imgGems[i], _posX[i], Animation.easeOutCubic((float)composedTime, 600.0f, (float)_posY - 600.0f, _animTotalTime));
            }else{
                batch.draw(_imgGems[i], _posX[i], _posY);
            }
        }
    }
}

