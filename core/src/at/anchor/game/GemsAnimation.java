package at.anchor.game;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class GemsAnimation {

    private EsthetiqueGems _game;
    private TextureRegion[] _imgGems;
    private int[] _posX;
    private int _posY;

    double _animTime;
    double _animTotalTime;

    public GemsAnimation(EsthetiqueGems esthetiqueGems) {
        _game = esthetiqueGems;

        AssetManager assetManager = _game.getAssetManager();

        _imgGems = new TextureRegion[7];
        _posX = new int[7];

        _imgGems[0] = new TextureRegion(assetManager.get("img/gemWhite.png", Texture.class));
        _imgGems[1] = new TextureRegion(assetManager.get("img/gemRed.png", Texture.class));
        _imgGems[2] = new TextureRegion(assetManager.get("img/gemPurple.png", Texture.class));
        _imgGems[3] = new TextureRegion(assetManager.get("img/gemOrange.png", Texture.class));
        _imgGems[4] = new TextureRegion(assetManager.get("img/gemGreen.png", Texture.class));
        _imgGems[5] = new TextureRegion(assetManager.get("img/gemYellow.png", Texture.class));
        _imgGems[6] = new TextureRegion(assetManager.get("img/gemBlue.png", Texture.class));


        for (int i = 0; i < 7; ++i) {
            _imgGems[i].flip(false, true);
            _posX[i] = EsthetiqueGems.VIRTUAL_WIDTH - 360 - (76 * 7) / 2 + i * 76;
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

