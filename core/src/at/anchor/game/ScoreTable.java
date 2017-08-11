package at.anchor.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.I18NBundle;

public class ScoreTable implements Input.TextInputListener {

    public enum State {RequestPlayerName, ShowScores};

    // State, game and points
    private State _state;
    private EsthetiqueGems _game;
    private I18NBundle _lang;
    private int _points;

    // Resources
    private BitmapFont _fontTitle;
    private BitmapFont _fontText;

    // Used strings
    String _titleText;

    // Settings & Highscore
    Preferences _prefConfig = Gdx.app.getPreferences("config");
    Preferences _prefScore = Gdx.app.getPreferences("score");

    // Positions
    private Vector2 _titlePos;
    private Vector2 _firstScorePos;

    GlyphLayout _layout;

    public ScoreTable(EsthetiqueGems game, int points) {
        // Initial state and game
        _game = game;
        _state = State.RequestPlayerName;
        _points = points;

        // Language
        _lang = new I18NBundle();

        // Parse scores
        parseScore();

        _layout = new GlyphLayout();

        AssetManager assetManager = _game.getAssetManager();
        _lang = assetManager.get("i18n/stateScoreTable", I18NBundle.class);
        _titleText = _lang.get("ScoreTable_Title");

        // Positions
        _layout.setText(_game._fontH1, _titleText);
        _titlePos = new Vector2((EsthetiqueGems.VIRTUAL_WIDTH - _layout.width) / 2, 652);
        _layout.setText(_game._fontP, _titleText);
        _firstScorePos = new Vector2((EsthetiqueGems.VIRTUAL_WIDTH - _layout.width - 132) / 2, 752);

        Integer _playerScore = _prefScore.getInteger("score", 0);

        if (_points > _playerScore) {
            String _defaultName = _prefConfig.getString("defaultName", _lang.get("ScoreTable_MissingName"));
            Gdx.input.getTextInput(this, _lang.get("ScoreTable_EnterName"), _defaultName, "");
        }
        else
        {
            _state = State.ShowScores;
        }
    }

    public void draw() {
        SpriteBatch batch = _game.getSpriteBatch();

        if (_state == State.ShowScores) {
            // Render title
            _game._fontH1.draw(batch, _titleText, (int)_titlePos.x, (int)_titlePos.y);

            // Render table
            String _playerName = _prefScore.getString("name", ".....");
            Integer _playerScore = _prefScore.getInteger("score", 0);
            _game._fontP.draw(batch, _playerName, (int)_firstScorePos.x + 1, (int)_firstScorePos.y);
            _game._fontP.draw(batch, Integer.toString(_playerScore), (int)_firstScorePos.x + 300, (int)_firstScorePos.y);
        }
    }

    public void parseScore() {
        // Pass
    }

    public void saveScore() {
        // Pass
    }

    @Override
    public void canceled() {
        // Show scores
        _state = State.ShowScores;
    }

    @Override
    public void input(String text) {

        if (text.length() > 15) {
            text = text.substring(0, 15);
        }

        // Save scores
        _prefScore.putString("name", text);
        _prefScore.putInteger("score", _points);
        _prefScore.flush();

        _prefConfig.putString("defaultName", text);
        _prefConfig.flush();

        saveScore();

        // Show scores
        _state = State.ShowScores;
    }
}
