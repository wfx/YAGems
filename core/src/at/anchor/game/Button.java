package at.anchor.game;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.graphics.g2d.Animation;

public class Button {
    private EsthetiqueGems _game;
    private TextureRegion _imgNormal;
    private TextureRegion _imgNormalClicked;
    private TextureRegion _imgSwitch;
    private TextureRegion _imgSwitchClicked;
    private String _text;
    private Vector2 _textPos;
    private Vector2 _btnPos;
    private int _btnWidth;
    private int _btnHeight;
    private BitmapFont _font;
    private boolean _clicked;
    private boolean _modeOn;

    public Button(EsthetiqueGems game, int x, int y) {
        _game = game;
        _imgNormal = null;
        _imgNormalClicked = null;
        _imgSwitch = null;
        _imgSwitchClicked = null;
        _btnPos = new Vector2(x, y);
        _btnWidth = 0;
        _btnHeight = 0;
        _font = null;
        _text = null;
        _textPos = new Vector2();
        _clicked = false;
        _modeOn = false;
    }

    public void render() {
        SpriteBatch batch = _game.getSpriteBatch();

        if (!_clicked) {
            if (!_modeOn  && _imgNormal != null) {
                batch.draw(_imgNormal, _btnPos.x, _btnPos.y);
            } else if (_modeOn && _imgSwitch != null) {
                batch.draw(_imgSwitch, _btnPos.x, _btnPos.y);
            }
        } else if (!_modeOn && _imgNormalClicked != null) {
            batch.draw(_imgNormalClicked, _btnPos.x, _btnPos.y);
        } else if (_imgSwitchClicked != null) {
            batch.draw(_imgSwitchClicked, _btnPos.x, _btnPos.y);
        }

        if (_font != null) {
            _font.draw(batch, _text, _textPos.x, _textPos.y);
        }
    }

    public int getButtonX() {
        return (int) _btnPos.x;
    }

    public int getButtonY() {
        return (int) _btnPos.y;
    }

    public int getTextX() {
        return (int) _textPos.x;
    }

    public int getTextY() {
        return (int) _textPos.y;
    }

    public void setButtonPos(float x, float y) {
        _btnPos.x = x;
        _btnPos.y = y;
    }

    public void setTextPos(int x, int y) {
        _textPos.x = x;
        _textPos.y = y;
    }

    public String getText() {
        return _text;
    }

    public void setText(String text) {
        _text = text;
    }

    public void setModeOn(boolean mode) {
        _modeOn = mode;
    }

    public boolean getModeOn() {
        return _modeOn;
    }

    public void setNormal(TextureRegion btn, TextureRegion btnClicked) {
        _imgNormal = btn;
        _imgNormalClicked = btnClicked;

        if (btn != null && btnClicked != null) {
            // Total width and height: pos x + width...
            _btnWidth = (int) _btnPos.x +_imgNormal.getRegionWidth();
            _btnHeight = (int) _btnPos.y + _imgNormal.getRegionHeight();
        }
    }

    public void setSwitch(TextureRegion btn, TextureRegion btnClicked) {
        _imgSwitch = btn;
        _imgSwitchClicked = btnClicked;
    }

    public void setFont(BitmapFont font) {
        _font = font;
    }

    public void setNull() {
        _imgNormal = null;
        _imgNormalClicked = null;
        _imgSwitch = null;
        _imgSwitchClicked = null;
    }

    public boolean isClicked(int mX, int mY) {
        if (mX > _btnPos.x &&
                mX < _btnWidth &&
                mY > _btnPos.y &&
                mY < _btnHeight) {
            _clicked = true;
            return true;
        } else {
            return false;
        }
    }

    public void touchUp() {
        _clicked = false;
        /*
        try {
            //thread to sleep for the specified number of milliseconds
            Thread.sleep(500);
        } catch ( java.lang.InterruptedException ie) {
            System.out.println(ie);
        }
        */
    }
}